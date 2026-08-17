package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.auth.*;
import com.zerotrust.iot.entity.Role;
import com.zerotrust.iot.entity.User;
import com.zerotrust.iot.exception.BadRequestException;
import com.zerotrust.iot.repository.RoleRepository;
import com.zerotrust.iot.repository.UserRepository;
import com.zerotrust.iot.security.JwtTokenProvider;
import com.zerotrust.iot.security.SiweVerifier;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final SiweVerifier siweVerifier;
    private final AuditService auditService;

    // Nonce memory cache for SIWE challenge-response (nonce -> walletAddress)
    private final Map<String, String> nonceCache = new ConcurrentHashMap<>();

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            SiweVerifier siweVerifier,
            AuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.siweVerifier = siweVerifier;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);

        auditService.logAction(
                userPrincipal.getId(),
                "USER_LOGIN",
                "User",
                userPrincipal.getId(),
                "User logged in via password authentication"
        );

        return AuthResponse.builder()
                .token(jwt)
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .walletAddress(userPrincipal.getWalletAddress())
                .roles(userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        Role userRole = roleRepository.findByName("ROLE_DEVICE_OWNER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_DEVICE_OWNER").description("Device Owner").build()));

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .walletAddress(registerRequest.getWalletAddress())
                .status("ACTIVE")
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .build();

        User savedUser = userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String jwt = tokenProvider.generateTokenFromUserId(
                savedUser.getId(),
                savedUser.getUsername(),
                userPrincipal.getAuthorities()
        );

        auditService.logAction(
                savedUser.getId(),
                "USER_REGISTERED",
                "User",
                savedUser.getId(),
                "User registered with username: " + savedUser.getUsername()
        );

        return AuthResponse.builder()
                .token(jwt)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .walletAddress(savedUser.getWalletAddress())
                .roles(Collections.singletonList(userRole.getName()))
                .build();
    }

    public SiweNonceResponse generateSiweNonce(String walletAddress) {
        String nonce = CryptoUtils.generateRandomNonce(16);
        nonceCache.put(nonce, walletAddress.toLowerCase());

        String message = String.format(
                "Zero Trust IoT Security Framework requests you to sign in with your Ethereum account:\n%s\n\nURI: http://localhost:5173\nVersion: 1\nChain ID: 31337\nNonce: %s\nIssued At: %s",
                walletAddress,
                nonce,
                new Date().toInstant().toString()
        );

        return SiweNonceResponse.builder()
                .nonce(nonce)
                .message(message)
                .domain("localhost:5173")
                .uri("http://localhost:5173")
                .chainId("31337")
                .build();
    }

    @Transactional
    public AuthResponse verifySiwe(SiweVerifyRequest request) {
        boolean isValid = siweVerifier.verifySignature(request.getMessage(), request.getSignature(), request.getAddress());
        if (!isValid) {
            throw new BadRequestException("Invalid cryptographic signature for wallet: " + request.getAddress());
        }

        String normalizedAddress = request.getAddress().toLowerCase();

        User user = userRepository.findByWalletAddressIgnoreCase(normalizedAddress)
                .orElseGet(() -> {
                    Role role = roleRepository.findByName("ROLE_DEVICE_OWNER")
                            .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_DEVICE_OWNER").description("Device Owner").build()));

                    String generatedUsername = "wallet_" + normalizedAddress.substring(2, 8);
                    return userRepository.save(User.builder()
                            .username(generatedUsername)
                            .email(normalizedAddress + "@zerotrust-wallet.eth")
                            .walletAddress(normalizedAddress)
                            .status("ACTIVE")
                            .roles(new HashSet<>(Collections.singletonList(role)))
                            .build());
                });

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String jwt = tokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getUsername(),
                userPrincipal.getAuthorities()
        );

        auditService.logAction(
                user.getId(),
                "WALLET_SIWE_LOGIN",
                "User",
                user.getId(),
                "User logged in via MetaMask SIWE with wallet: " + normalizedAddress
        );

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .walletAddress(user.getWalletAddress())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();
    }
}

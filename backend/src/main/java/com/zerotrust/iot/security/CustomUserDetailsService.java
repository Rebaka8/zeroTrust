package com.zerotrust.iot.security;

import com.zerotrust.iot.entity.User;
import com.zerotrust.iot.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmailOrWallet) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmailOrWallet)
                .or(() -> userRepository.findByEmail(usernameOrEmailOrWallet))
                .or(() -> userRepository.findByWalletAddressIgnoreCase(usernameOrEmailOrWallet))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + usernameOrEmailOrWallet));

        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));

        return UserPrincipal.create(user);
    }
}

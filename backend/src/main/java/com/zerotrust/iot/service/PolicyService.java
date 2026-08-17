package com.zerotrust.iot.service;

import com.zerotrust.iot.dto.policy.AccessDecisionResponse;
import com.zerotrust.iot.dto.policy.AccessEvaluateRequest;
import com.zerotrust.iot.dto.policy.PolicyCreateRequest;
import com.zerotrust.iot.dto.policy.PolicyResponse;
import com.zerotrust.iot.entity.AccessPolicy;
import com.zerotrust.iot.entity.AccessRequest;
import com.zerotrust.iot.entity.Device;
import com.zerotrust.iot.entity.DidDocument;
import com.zerotrust.iot.entity.VerifiableCredential;
import com.zerotrust.iot.exception.BadRequestException;
import com.zerotrust.iot.exception.ResourceNotFoundException;
import com.zerotrust.iot.pdp.PolicyDecisionPoint;
import com.zerotrust.iot.pdp.PolicyDecisionPoint.DecisionResult;
import com.zerotrust.iot.repository.AccessPolicyRepository;
import com.zerotrust.iot.repository.AccessRequestRepository;
import com.zerotrust.iot.repository.DidDocumentRepository;
import com.zerotrust.iot.repository.VerifiableCredentialRepository;
import com.zerotrust.iot.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final AccessPolicyRepository policyRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final DidDocumentRepository didDocumentRepository;
    private final VerifiableCredentialRepository vcRepository;
    private final PolicyDecisionPoint policyDecisionPoint;
    private final TrustScoreService trustScoreService;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;

    public PolicyService(
            AccessPolicyRepository policyRepository,
            AccessRequestRepository accessRequestRepository,
            DidDocumentRepository didDocumentRepository,
            VerifiableCredentialRepository vcRepository,
            PolicyDecisionPoint policyDecisionPoint,
            TrustScoreService trustScoreService,
            AuditService auditService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.policyRepository = policyRepository;
        this.accessRequestRepository = accessRequestRepository;
        this.didDocumentRepository = didDocumentRepository;
        this.vcRepository = vcRepository;
        this.policyDecisionPoint = policyDecisionPoint;
        this.trustScoreService = trustScoreService;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public PolicyResponse createPolicy(PolicyCreateRequest request, UserPrincipal currentUser) {
        if (policyRepository.findByPolicyName(request.getPolicyName()).isPresent()) {
            throw new BadRequestException("Policy with name '" + request.getPolicyName() + "' already exists!");
        }

        AccessPolicy policy = AccessPolicy.builder()
                .policyName(request.getPolicyName())
                .description(request.getDescription())
                .minimumTrustScore(request.getMinimumTrustScore())
                .requiredCredentialType(request.getRequiredCredentialType())
                .allowedTopics(request.getAllowedTopics())
                .actionAllowed(request.getActionAllowed())
                .isActive(true)
                .build();

        AccessPolicy saved = policyRepository.save(policy);

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                "POLICY_CREATED",
                "AccessPolicy",
                saved.getId(),
                "Created policy: " + saved.getPolicyName() + " (Min Trust: " + saved.getMinimumTrustScore() + ")"
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getAllActivePolicies() {
        return policyRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PolicyResponse> getAllPoliciesPaged(Pageable pageable) {
        return policyRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public AccessDecisionResponse evaluateAccess(AccessEvaluateRequest request) {
        DidDocument didDoc = didDocumentRepository.findByDidUri(request.getDidUri())
                .orElseThrow(() -> new ResourceNotFoundException("DID not found: " + request.getDidUri()));

        Device device = didDoc.getDevice();

        // 1. Recalculate dynamic trust score in real-time
        trustScoreService.evaluateDeviceTrust(device.getId());

        int currentScore = device.getCurrentTrustScore();

        // 2. Fetch active credentials
        VerifiableCredential activeVc = vcRepository.findFirstBySubjectDidAndIsRevokedFalse(request.getDidUri()).orElse(null);

        // 3. Fetch active ABAC policies
        List<AccessPolicy> activePolicies = policyRepository.findByIsActiveTrue();

        // 4. Run Policy Decision Point algorithm
        DecisionResult result = policyDecisionPoint.evaluate(
                device,
                didDoc,
                activeVc,
                currentScore,
                request.getResource(),
                request.getAction(),
                activePolicies
        );

        // 5. Persist access audit record
        AccessRequest accessLog = AccessRequest.builder()
                .device(device)
                .policy(result.getMatchedPolicy())
                .requestedResource(request.getResource())
                .requestedAction(request.getAction())
                .trustScoreAtRequest(currentScore)
                .decision(result.getDecision())
                .reason(result.getReason())
                .requestedAt(Instant.now())
                .build();

        AccessRequest savedRequest = accessRequestRepository.save(accessLog);

        AccessDecisionResponse response = AccessDecisionResponse.builder()
                .requestId(savedRequest.getId())
                .didUri(request.getDidUri())
                .resource(request.getResource())
                .action(request.getAction())
                .currentTrustScore(currentScore)
                .decision(result.getDecision())
                .granted(result.isGranted())
                .reason(result.getReason())
                .evaluatedAt(savedRequest.getRequestedAt())
                .build();

        // 6. Broadcast real-time decision
        messagingTemplate.convertAndSend("/topic/decisions", response);

        return response;
    }

    private PolicyResponse mapToResponse(AccessPolicy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyName(policy.getPolicyName())
                .description(policy.getDescription())
                .minimumTrustScore(policy.getMinimumTrustScore())
                .requiredCredentialType(policy.getRequiredCredentialType())
                .allowedTopics(policy.getAllowedTopics())
                .actionAllowed(policy.getActionAllowed())
                .isActive(policy.getIsActive())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}

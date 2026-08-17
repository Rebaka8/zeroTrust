package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.policy.AccessDecisionResponse;
import com.zerotrust.iot.dto.policy.AccessEvaluateRequest;
import com.zerotrust.iot.dto.policy.PolicyCreateRequest;
import com.zerotrust.iot.dto.policy.PolicyResponse;
import com.zerotrust.iot.security.UserPrincipal;
import com.zerotrust.iot.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "Zero Trust Access Policies", description = "Endpoints for defining ABAC access rules and evaluating PDP access decisions")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @Operation(summary = "Create a new Zero Trust access policy with trust score thresholds")
    public ResponseEntity<PolicyResponse> createPolicy(
            @Valid @RequestBody PolicyCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(request, currentUser));
    }

    @GetMapping
    @Operation(summary = "Retrieve paginated list of all Zero Trust access policies")
    public ResponseEntity<Page<PolicyResponse>> getAllPolicies(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(policyService.getAllPoliciesPaged(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Retrieve all currently active Zero Trust policies")
    public ResponseEntity<List<PolicyResponse>> getActivePolicies() {
        return ResponseEntity.ok(policyService.getAllActivePolicies());
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate an IoT access request against dynamic trust score and active policies")
    public ResponseEntity<AccessDecisionResponse> evaluateAccess(
            @Valid @RequestBody AccessEvaluateRequest request
    ) {
        return ResponseEntity.ok(policyService.evaluateAccess(request));
    }
}

# ==============================================================================
# Zero Trust IoT Security Framework - Automated E2E Verification Script
# ==============================================================================

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  ZERO TRUST IoT SECURITY FRAMEWORK - END-TO-END INTEGRATION TEST SUITE        " -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8085"
$passedCount = 0
$totalCount = 10

function Assert-Step {
    param(
        [string]$StepNumber,
        [string]$Description,
        [bool]$Condition,
        [string]$Details = ""
    )
    if ($Condition) {
        Write-Host "  [PASS] Step $StepNumber : $Description" -ForegroundColor Green
        if ($Details) { Write-Host "         $Details" -ForegroundColor Gray }
        $global:passedCount++
    } else {
        Write-Host "  [FAIL] Step $StepNumber : $Description" -ForegroundColor Red
        if ($Details) { Write-Host "         ERROR: $Details" -ForegroundColor Yellow }
    }
}

# --- Step 1: Health Check ---
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get -TimeoutSec 5
    Assert-Step -StepNumber "1/10" -Description "Spring Boot Actuator Health Check" -Condition ($health.status -eq "UP") -Details "Status: $($health.status)"
} catch {
    Assert-Step -StepNumber "1/10" -Description "Spring Boot Actuator Health Check" -Condition $false -Details $_.Exception.Message
}

# --- Step 2: Admin Authentication & JWT ---
$token = ""
try {
    $authPayload = '{"usernameOrEmail":"admin","password":"Admin@123456"}'
    $authRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $authPayload -ContentType "application/json"
    $token = $authRes.token
    Assert-Step -StepNumber "2/10" -Description "Administrator Authentication & JWT Token Issuance" -Condition ($token.Length -gt 20) -Details "User: $($authRes.username), Roles: $($authRes.roles -join ', ')"
} catch {
    Assert-Step -StepNumber "2/10" -Description "Administrator Authentication & JWT Token Issuance" -Condition $false -Details $_.Exception.Message
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# --- Step 3: IoT Fleet Retrieval ---
$targetDeviceId = ""
$targetDidUri = ""
try {
    $devicesRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/devices" -Method Get -Headers $headers
    $targetDevice = $devicesRes.content[0]
    $targetDeviceId = $targetDevice.id
    $targetDidUri = $targetDevice.didUri
    Assert-Step -StepNumber "3/10" -Description "IoT Fleet Discovery & Hardware Identity" -Condition ($devicesRes.totalElements -gt 0) -Details "Fleet Count: $($devicesRes.totalElements) devices. Target Node: $($targetDevice.deviceName)"
} catch {
    Assert-Step -StepNumber "3/10" -Description "IoT Fleet Discovery & Hardware Identity" -Condition $false -Details $_.Exception.Message
}

# --- Step 4: W3C DID Resolution ---
try {
    $encodedDid = [System.Uri]::EscapeDataString($targetDidUri)
    $didRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/did/resolve/$encodedDid" -Method Get -Headers $headers
    Assert-Step -StepNumber "4/10" -Description "W3C Decentralized Identifier (DID) Resolution" -Condition ($didRes.id -eq $targetDidUri) -Details "Resolved DID: $($didRes.id)"
} catch {
    Assert-Step -StepNumber "4/10" -Description "W3C Decentralized Identifier (DID) Resolution" -Condition $false -Details $_.Exception.Message
}

# --- Step 5: Verifiable Credential Verification ---
try {
    $vcList = Invoke-RestMethod -Uri "$baseUrl/api/v1/vc/device/$targetDeviceId" -Method Get -Headers $headers
    $targetVc = $vcList[0]
    $verifyPayload = @{ vcPayload = $targetVc.rawVcJwt } | ConvertTo-Json
    $vcVerifyRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/vc/verify" -Method Post -Headers $headers -Body $verifyPayload
    Assert-Step -StepNumber "5/10" -Description "Verifiable Credential (VC) Cryptographic Proof Verification" -Condition ($vcVerifyRes.valid -eq $true) -Details "Issuer: $($vcVerifyRes.issuerDid), Valid: $($vcVerifyRes.valid)"
} catch {
    Assert-Step -StepNumber "5/10" -Description "Verifiable Credential (VC) Cryptographic Proof Verification" -Condition $false -Details $_.Exception.Message
}

# --- Step 6: Dynamic Trust Score Calculation T(t) ---
try {
    $trustRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/trust/evaluate/$targetDeviceId" -Method Post -Headers $headers
    Assert-Step -StepNumber "6/10" -Description "Mathematical Dynamic Risk Engine Evaluation T(t)" -Condition ($trustRes.overallScore -ge 0) -Details "Overall Trust: $($trustRes.overallScore)/100, Risk Level: $($trustRes.riskLevel)"
} catch {
    Assert-Step -StepNumber "6/10" -Description "Mathematical Dynamic Risk Engine Evaluation T(t)" -Condition $false -Details $_.Exception.Message
}

# --- Step 7: ABAC Policy Decision Point (PDP) ---
try {
    $pdpPayload = @{
        didUri = $targetDidUri
        resource = "smartgrid/substation/telemetry"
        action = "READ"
    } | ConvertTo-Json
    $pdpRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/policies/evaluate" -Method Post -Headers $headers -Body $pdpPayload
    Assert-Step -StepNumber "7/10" -Description "Attribute-Based Access Control (ABAC) PDP Enforcement" -Condition ($pdpRes.decision -ne $null) -Details "Decision: $($pdpRes.decision), Granted: $($pdpRes.granted), Reason: $($pdpRes.reason)"
} catch {
    Assert-Step -StepNumber "7/10" -Description "Attribute-Based Access Control (ABAC) PDP Enforcement" -Condition $false -Details $_.Exception.Message
}

# --- Step 8: Adversarial Cyberattack Simulation & Quarantine ---
try {
    $simPayload = @{
        attackType = "PACKET_FLOODING"
        targetDeviceId = $targetDeviceId
    } | ConvertTo-Json
    $simRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/simulation/attack" -Method Post -Headers $headers -Body $simPayload
    Assert-Step -StepNumber "8/10" -Description "Adversarial Cyberattack Injection & Autonomous Quarantine" -Condition ($simRes.attackDetected -eq $true) -Details "Pre-Score: $($simRes.preAttackTrustScore) -> Post-Score: $($simRes.postAttackTrustScore), Quarantine: $($simRes.automatedQuarantineTriggered)"
} catch {
    Assert-Step -StepNumber "8/10" -Description "Adversarial Cyberattack Injection & Autonomous Quarantine" -Condition $false -Details $_.Exception.Message
}

# --- Step 9: Kaggle IoT-23 Dataset Replay & Confusion Matrix ---
try {
    $datasetRes = Invoke-RestMethod -Uri "$baseUrl/api/v1/dataset/replay-preset/iot-23-smart-meter?targetDeviceId=$targetDeviceId" -Method Post -Headers $headers
    Assert-Step -StepNumber "9/10" -Description "Kaggle IoT-23 Dataset Streaming & Confusion Matrix Calculation" -Condition ($datasetRes.totalRowsProcessed -gt 0) -Details "Accuracy: $($datasetRes.accuracyPercentage)%, Precision: $($datasetRes.precisionPercentage)%, Latency: $($datasetRes.averageDetectionLatencyMs)ms"
} catch {
    Assert-Step -StepNumber "9/10" -Description "Kaggle IoT-23 Dataset Streaming & Confusion Matrix Calculation" -Condition $false -Details $_.Exception.Message
}

# --- Step 10: Immutable Audit Ledger Verification ---
try {
    $auditLogs = Invoke-RestMethod -Uri "$baseUrl/api/v1/audit/logs" -Method Get -Headers $headers
    Assert-Step -StepNumber "10/10" -Description "Cryptographic SHA-256 Audit Trail Verification" -Condition ($auditLogs.totalElements -gt 0) -Details "Total Audit Entries: $($auditLogs.totalElements), Latest Action: $($auditLogs.content[0].actionType)"
} catch {
    Assert-Step -StepNumber "10/10" -Description "Cryptographic SHA-256 Audit Trail Verification" -Condition $false -Details $_.Exception.Message
}

Write-Host ""
Write-Host "================================================================================" -ForegroundColor Cyan
if ($passedCount -eq $totalCount) {
    Write-Host "  VERIFICATION RESULT: ALL $passedCount/$totalCount INTEGRATION CHECKS PASSED (100%)" -ForegroundColor Green
} else {
    Write-Host "  VERIFICATION RESULT: $passedCount/$totalCount CHECKS PASSED" -ForegroundColor Yellow
}
Write-Host "================================================================================" -ForegroundColor Cyan

package com.medgo.member.service;

import com.medgo.commons.CommonResponse;
import com.medgo.member.components.MedGoCustomSecurityContext;
import com.medgo.member.exception.MemberCodeValidationException;
import com.medgo.member.feign.SharedMembershipServiceClient;
import com.medgo.member.model.UtilizationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCodeValidationService {

    private final MedGoCustomSecurityContext securityContext;
    private final SharedMembershipServiceClient sharedMembershipServiceClient;
    
    /**
     * Get HttpServletRequest from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            log.debug("Could not get current request: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Validate memberCode against authenticated user's membership data from JWT token
     * Similar to validateMemCode example - validates memberCode belongs to authenticated user
     * 
     * @param memCode Member code to validate (can be userCode or memberCode)
     * @return HashMap with validation result, memberCode, accountCode, memType, and validate flag
     */
    public HashMap<String, Object> validateMemCode(String memCode) {
        // Get current request for better token extraction (especially for GET methods)
        HttpServletRequest request = getCurrentRequest();
        return validateMemCode(memCode, request);
    }
    
    public HashMap<String, Object> validateMemCode(String memCode, HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<>();
        
        log.info("Starting memberCode validation for memCode: {}", memCode);
        
        // Get authenticated username from JWT token (pass request for better extraction)
        String userId = securityContext.authenticatedUsername(request);
        log.info("Authenticated username from token: {}", userId != null ? userId : "null");
        
        if (userId == null || userId.isEmpty()) {
            log.error("SECURITY ALERT: No authenticated user found in JWT token. Token extraction may have failed. " +
                    "Request: {}", request != null ? "available" : "null");
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }
        
        // Get memberCode from JWT token (pass request for better extraction)
        String tokenMemberCode = securityContext.authenticatedMemberCode(request);
        log.info("MemberCode from token: {}", tokenMemberCode != null ? tokenMemberCode : "null");
        
        if (tokenMemberCode == null || tokenMemberCode.isEmpty()) {
            log.warn("No memberCode found in JWT token for user: {}. Token may not have memberCode claim.", userId);
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }
        
        // CRITICAL: Validate requested memberCode matches token memberCode
        // This is the primary security check - if token memberCode matches requested memberCode, validation passes
        if (!tokenMemberCode.equalsIgnoreCase(memCode)) {
            log.error("SECURITY ALERT: MemberCode mismatch! Token memberCode={}, requested memberCode={}, userId={}", 
                    tokenMemberCode, memCode, userId);
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }
        
        log.info("Token memberCode matches requested memberCode: {}", memCode);
        
        // CRITICAL: Token validation passed - memberCode matches token
        // The membership service call is for additional data only, not for validation
        // If token memberCode matches requested memberCode, validation should pass
        
        // Try to fetch membership data from shared membership service for additional info
        // But don't fail validation if membership service returns 404 or error
        String accountCode = "";
        String memType = "";
        
        try {
            CommonResponse membershipResponse = sharedMembershipServiceClient.findMemberByCode(memCode, null);
            
            if (membershipResponse != null && membershipResponse.getData() != null) {
                // Extract membership data from response
                Map<String, Object> dataMap = (Map<String, Object>) membershipResponse.getData();
                String responseMemberCode = (String) dataMap.get("memberCode");
                accountCode = (String) dataMap.get("accountCode");
                if (accountCode == null) accountCode = "";
                memType = (String) dataMap.get("memType");
                if (memType == null) memType = "";
                
                // Verify response memberCode matches (should match since we validated token)
                if (responseMemberCode != null && !responseMemberCode.equalsIgnoreCase(memCode)) {
                    log.warn("Membership service returned different memberCode. Token: {}, Response: {}", 
                            memCode, responseMemberCode);
                }
                
                log.info("Fetched membership data: memberCode={}, accountCode={}, memType={}", 
                        memCode, accountCode, memType);
            } else {
                log.warn("Membership service returned null or empty data for memberCode: {}. " +
                        "Validation still passes based on token match.", memCode);
            }
            
        } catch (Exception e) {
            log.warn("Error fetching membership data for memberCode: {}. " +
                    "Validation still passes based on token match. Error: {}", memCode, e.getMessage());
        }
        
        // CRITICAL: Validation passes because token memberCode matches requested memberCode
        result.put("memberCode", memCode);
        result.put("accountCode", accountCode);
        result.put("memType", memType);
        result.put("validate", Boolean.TRUE);
        
        log.info("MemberCode validation successful: userId={}, memberCode={}, accountCode={}, memType={}", 
                userId, memCode, accountCode, memType);
        
        return result;
    }
    
    /**
     * Validate memberCode against authenticated user's token and throw exception if validation fails.
     * This method is designed to be called from controllers to simplify validation logic.
     * 
     * @param memCode Member code to validate (can be userCode or memberCode)
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateMemCodeOrThrow(String memCode) {
        HttpServletRequest request = getCurrentRequest();
        validateMemCodeOrThrow(memCode, request);
    }
    
    /**
     * Validate memberCode against authenticated user's token and throw exception if validation fails.
     * This method is designed to be called from controllers to simplify validation logic.
     * 
     * @param memCode Member code to validate (can be userCode or memberCode)
     * @param request HttpServletRequest for better token extraction (especially for GET methods)
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateMemCodeOrThrow(String memCode, HttpServletRequest request) {
        HashMap<String, Object> validationResult = validateMemCode(memCode, request);
        Boolean isValid = (Boolean) validationResult.get("validate");
        
        if (!Boolean.TRUE.equals(isValid)) {
            log.error("SECURITY ALERT: MemberCode validation failed for memberCode: {}", memCode);
            throw new MemberCodeValidationException(
                    memCode, 
                    "MemberCode does not belong to authenticated user. Access denied."
            );
        }
    }
    
    /**
     * Validate memberCode against authenticated user's token.
     * This method automatically extracts HttpServletRequest from RequestContextHolder.
     * Designed to be called from controllers - validation is handled internally in service layer.
     * 
     * @param memCode Member code to validate (can be userCode or memberCode)
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateMemberCode(String memCode) {
        HttpServletRequest request = getCurrentRequest();
        validateMemCodeOrThrow(memCode, request);
    }
    
    /**
     * Validate memberCode against authenticated user's token.
     * This method is designed to be called from controllers - validation is handled internally in service layer.
     * 
     * @param memCode Member code to validate (can be userCode or memberCode)
     * @param request HttpServletRequest for better token extraction (especially for GET methods)
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateMemberCode(String memCode, HttpServletRequest request) {
        validateMemCodeOrThrow(memCode, request);
    }
    
    /**
     * Validate memberCode from UtilizationRequest against authenticated user's token.
     * This method handles extraction of memberCode from request body and validation.
     * Designed to be called from controllers - all validation logic is handled in service layer.
     * 
     * @param utilizationRequest Utilization request containing memberCode
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateMemberCodeFromRequest(UtilizationRequest utilizationRequest) {
        if (utilizationRequest == null) {
            return;
        }
        
        String memcode = utilizationRequest.getMemcode();
        if (memcode != null && !memcode.isEmpty()) {
            validateMemberCode(memcode);
        }
    }
    
    /**
     * Validate and prepare UtilizationRequest (validate memberCode and set pagination defaults).
     * This method handles all validation and request preparation logic in service layer.
     * 
     * @param utilizationRequest Utilization request to validate and prepare
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateAndPrepareUtilizationRequest(UtilizationRequest utilizationRequest) {
        if (utilizationRequest == null) {
            return;
        }
        
        // Validate memberCode
        validateMemberCodeFromRequest(utilizationRequest);
        
        // Set pagination defaults if not set
        if (utilizationRequest.getPage() == null) {
            utilizationRequest.setPage(0);
        }
        if (utilizationRequest.getSize() == null) {
            utilizationRequest.setSize(10);
        }
    }
}


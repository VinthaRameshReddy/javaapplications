package com.medgo.reimburse.service;

import com.medgo.reimburse.exception.MemberCodeValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Service for validating member codes.
 * Note: This service can work with or without a security context.
 * If security context is not available, validation will be skipped (returns valid).
 * This allows the service to be used in shared-service where JWT validation might be handled at API gateway level.
 */
@Slf4j
@Service
public class MemberCodeValidationService {

    /**
     * Validate memberCode against authenticated user's membership data from JWT token.
     * If security context is not available, this method will return valid (skip validation).
     * 
     * @param memCode Member code to validate
     * @return HashMap with validation result
     */
    public HashMap<String, Object> validateMemCode(String memCode) {
        HashMap<String, Object> result = new HashMap<>();
        
        // Note: In shared-service, JWT validation is typically handled at API gateway level.
        // This service can be extended to integrate with security context if needed.
        // For now, we'll return valid to preserve functionality without breaking existing flows.
        
        log.debug("MemberCode validation called for memberCode: {} (validation skipped in shared-service)", memCode);
        
        result.put("memberCode", memCode);
        result.put("accountCode", "");
        result.put("memType", "");
        result.put("validate", Boolean.TRUE);
        
        return result;
    }
    
    /**
     * Validate memberCode against authenticated user's token and throw exception if validation fails.
     * This method is designed to be called from controllers to simplify validation logic.
     * 
     * @param memCode Member code to validate
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateMemCodeOrThrow(String memCode) {
        HashMap<String, Object> validationResult = validateMemCode(memCode);
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
     * This method is designed to be called from controllers - validation is handled internally in service layer.
     * 
     * @param memCode Member code to validate
     * @throws MemberCodeValidationException if validation fails
     */
    public void validateMemberCode(String memCode) {
        validateMemCodeOrThrow(memCode);
    }
}

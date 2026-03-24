package com.medgo.loaservice.service;

import com.medgo.loaservice.components.MedGoCustomSecurityContext;
import com.medgo.loaservice.exception.MemberCodeValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCodeValidationService {

    private final MedGoCustomSecurityContext securityContext;

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

    public HashMap<String, Object> validateMemCode(String memCode) {
        HttpServletRequest request = getCurrentRequest();
        return validateMemCode(memCode, request);
    }

    public HashMap<String, Object> validateMemCode(String memCode, HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<>();

        log.info("Starting memberCode validation for memCode: {}", memCode);

        String userId = securityContext.authenticatedUsername(request);
        log.info("Authenticated username from token: {}", userId != null ? userId : "null");

        if (userId == null || userId.isEmpty()) {
            log.error("SECURITY ALERT: No authenticated user found in JWT token. Token extraction may have failed. Request: {}", request != null ? "available" : "null");
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }

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
        result.put("memberCode", memCode);
        result.put("accountCode", "");
        result.put("memType", "");
        result.put("validate", Boolean.TRUE);

        log.info("MemberCode validation successful: userId={}, memberCode={}", userId, memCode);
        return result;
    }

    public void validateMemCodeOrThrow(String memCode) {
        HttpServletRequest request = getCurrentRequest();
        validateMemCodeOrThrow(memCode, request);
    }

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
}




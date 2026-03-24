package com.medgo.auditingutils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.bean.APIAuditLogger;
import com.medgo.utils.domain.MedgoEventAudit;
import com.medgo.utils.repository.MedgoEventAuditRepository;
import com.medgo.utils.service.MedgoEventAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auditing.aspect.enabled", havingValue = "true", matchIfMissing = true)
public class AuditingAspect implements ApplicationContextAware {

    private final MedgoEventAuditRepository medgoAuditLogsRepository;
    private final MedgoEventAuditService medgoEventAuditService;
    private final ObjectMapper objectMapper;
    private ApplicationContext applicationContext;


    // Cache to store email by otpRefId - used when session expires
    // Key: otpRefId, Value: email address
    // This ensures we can still identify the user even after session expiration
    private static final Map<String, String> emailCache = new ConcurrentHashMap<>();

    // Cache TTL: 30 minutes (same as OTP session TTL typically)
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Around("execution(* com..*.controller..*(..)) && @annotation(postMapping)")
    public Object auditPostEndpoints(ProceedingJoinPoint proceedingJoinPoint, PostMapping postMapping) throws Throwable {
        // Get API URL first to check if this endpoint should be audited
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        String apiUrl = "";
        if (attributes != null) {
            request = attributes.getRequest();
            apiUrl = request.getRequestURI();
        }

        if (!shouldAuditEndpoint(apiUrl, postMapping)) {
            return proceedingJoinPoint.proceed();
        }

        Object response = null;
        String userId = "";
        String requestJson = "";
        String responseJson = "";
        String apiName = proceedingJoinPoint.getSignature().getName();
        String status = "FAILED";
        String loginId = "";
        String deviceId = "";
        String platform = "";
        String eventType = apiName;
        String otpRefId = null;
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        try {
            Object[] args = proceedingJoinPoint.getArgs();
            if (args.length > 0) {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                requestJson = RequestAuditBuilder.buildRequestJson(args, objectMapper);

                // Extract otpRefId and other fields from request DTO
                for (Object arg : args) {
                    if (!RequestAuditBuilder.isFileOrFileCollection(arg)) {
                        try {
                            if (arg instanceof String) continue;
                            String json = objectMapper.writeValueAsString(arg);

                            JsonNode requestNode = objectMapper.readTree(json);
                            if (requestNode.has("otpRefId")) {
                                otpRefId = requestNode.get("otpRefId").asText(null);
                            }

                            APIAuditLogger auditLogRequest = objectMapper.readValue(json, APIAuditLogger.class);
                            if (auditLogRequest.getDeviceID() != null) {
                                loginId = auditLogRequest.getDeviceID();
                            }
                            if (auditLogRequest.getUserName() != null) {
                                userId = auditLogRequest.getUserName();
                            }
                            if (auditLogRequest.getUserId() != null) {
                                userId = auditLogRequest.getUserId();
                            }
                            if (auditLogRequest.getDeviceID() != null) {
                                deviceId = auditLogRequest.getDeviceID();
                            }
                            if (auditLogRequest.getDeviceOS() != null) {
                                platform = auditLogRequest.getDeviceOS();
                            }
                            if (auditLogRequest.getApiName() != null) {
                                eventType = auditLogRequest.getApiName();
                            }
                            break;
                        } catch (Exception ignored) {
                        }
                    }
                }

                // For setPassword endpoints, fetch email using otpRefId before API call
                // This ensures we have the email even if the API fails or session expires
                if (isSetPasswordEndpoint(apiUrl) && otpRefId != null && !otpRefId.isBlank()) {
                    try {
                        String cachedEmail = emailCache.get(otpRefId);
                        if (cachedEmail != null && !cachedEmail.isBlank()) {
                            userId = cachedEmail;
                        } else {
                            String fetchedEmail = fetchEmailByOtpRefId(otpRefId);
                            if (fetchedEmail != null && !fetchedEmail.isBlank()) {
                                userId = fetchedEmail;
                                emailCache.put(otpRefId, fetchedEmail);
                            } else {
                                log.warn("Email fetch returned null for otpRefId: {}", otpRefId);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Exception while fetching email using otpRefId {}: {}", otpRefId, e.getMessage(), e);
                        String cachedEmail = emailCache.get(otpRefId);
                        if (cachedEmail != null && !cachedEmail.isBlank()) {
                            userId = cachedEmail;
                        }
                    }
                } else if (isSetPasswordEndpoint(apiUrl)) {
                    log.warn("setPassword endpoint detected but otpRefId is null or blank");
                }
            }
            if (postMapping != null && postMapping.value().length > 0) {
                eventType = postMapping.value()[0];
            }
                if (request != null) {
                if (deviceId.isBlank()) {
                    deviceId = request.getHeader("X-Device-Id");
                }
                if (platform.isBlank()) {
                    String deviceOsHeader = request.getHeader("X-DEVICE-OS");
                    if (deviceOsHeader != null && !deviceOsHeader.isBlank()) {
                        platform = normalizePlatform(deviceOsHeader);
                    } else {
                        String platformHeader = request.getHeader("X-PLATFORM");
                        if (platformHeader != null && !platformHeader.isBlank()) {
                            platform = normalizePlatform(platformHeader);
                        } else {
                            String userAgent = request.getHeader("User-Agent");
                            if (userAgent != null && !userAgent.isBlank()) {
                                platform = normalizePlatformFromUserAgent(userAgent);
                            }
                        }
                    }
                } else {
                    platform = normalizePlatform(platform);
                }
            }


            response = proceedingJoinPoint.proceed();

            if (response != null) {
                responseJson = toSafeJson(response);

                try {
                    JsonNode root = objectMapper.readTree(responseJson);
                    JsonNode payload = root.has("body") ? root.get("body") : root;
                    String resp = payload.isObject() ? payload.path("response").asText("") : "";
                    String statusCodeFromPayload = payload.isObject() ? payload.path("statusCode").asText("") : "";

                    if ("SUCCESS".equalsIgnoreCase(resp)) {
                        status = "SUCCESS";
                    } else if ("error".equalsIgnoreCase(resp) || "FAILED".equalsIgnoreCase(resp)) {
                        status = "FAILED";
                    } else if ("200".equals(statusCodeFromPayload) || "000".equals(statusCodeFromPayload)) {
                        status = "SUCCESS";
                    } else if (root.path("status").asInt(0) == 200) {
                        status = "SUCCESS";
                    } else {
                        status = "FAILED";
                    }
                } catch (Exception e) {
                    log.warn("Could not parse response JSON: {}", e.getMessage());
                    status = "FAILED";
                }

                eventType = determineEventType(apiUrl, apiName, status);
            } else {
                status = "FAILED";
                eventType = determineEventType(apiUrl, apiName, status);
            }


        } catch (Throwable throwable) {
            status = "FAILED";
            eventType = determineEventType(apiUrl, apiName, status);

            if (response == null) {
                responseJson = "{\"error\":\"" + throwable.getClass().getSimpleName() + ": " +
                        (throwable.getMessage() != null ? throwable.getMessage() : "Unknown error") + "\"}";
            } else {
                try {
                    responseJson = toSafeJson(response);
                    JsonNode root = objectMapper.readTree(responseJson);
                    JsonNode payload = root.has("body") ? root.get("body") : root;
                    String resp = payload.path("response").asText("");
                    if ("SUCCESS".equalsIgnoreCase(resp)) {
                        status = "SUCCESS";
                    } else if ("error".equalsIgnoreCase(resp) || "failed".equalsIgnoreCase(resp)) {
                        status = "FAILED";
                    }
                    eventType = determineEventType(apiUrl, apiName, status);
                } catch (Exception e) {
                    log.warn("Could not parse exception response: {}", e.getMessage());
                }
            }

            log.error("Exception occurred in API call for [{}]: {}", apiUrl, throwable.getMessage());
            throw throwable;
        } finally {
            // Always attempt to save audit log, regardless of success or failure
            try {
                boolean isSetPassword = isSetPasswordEndpoint(apiUrl);

                if (isSetPassword) {
                    // If otpRefId is null, try to extract it from request arguments
                    if (otpRefId == null || otpRefId.isBlank()) {
                        try {
                            Object[] args = proceedingJoinPoint.getArgs();
                            for (Object arg : args) {
                                if (!RequestAuditBuilder.isFileOrFileCollection(arg) && !(arg instanceof String)) {
                                    try {
                                        String json = objectMapper.writeValueAsString(arg);
                                        JsonNode requestNode = objectMapper.readTree(json);
                                        if (requestNode.has("otpRefId")) {
                                            otpRefId = requestNode.get("otpRefId").asText(null);
                                            break;
                                        }
                                    } catch (Exception e) {
                                        log.debug("Failed to extract otpRefId from argument: {}", e.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Exception while extracting otpRefId: {}", e.getMessage());
                        }
                    }

                    // Fetch email using otpRefId if available
                    if (otpRefId != null && !otpRefId.isBlank()) {
                        String cachedEmail = emailCache.get(otpRefId);
                        if (cachedEmail != null && !cachedEmail.isBlank()) {
                            userId = cachedEmail;
                        } else {
                            try {
                                String fetchedEmail = fetchEmailByOtpRefId(otpRefId);
                                if (fetchedEmail != null && !fetchedEmail.isBlank()) {
                                    userId = fetchedEmail;
                                    emailCache.put(otpRefId, fetchedEmail);
                                } else {
                                    log.warn("Email not found in Redis for otpRefId: {}", otpRefId);
                                }
                            } catch (Exception e) {
                                log.warn("Exception while fetching email from Redis for otpRefId {}: {}", otpRefId, e.getMessage());
                                cachedEmail = emailCache.get(otpRefId);
                                if (cachedEmail != null && !cachedEmail.isBlank()) {
                                    userId = cachedEmail;
                                }
                            }
                        }
                    }
                }

                // Determine final username
                String finalUsername;
                if (isSetPasswordEndpoint(apiUrl)) {
                    // For setPassword endpoints, ensure we have email
                    if (userId == null || userId.isBlank() || "anonymous".equalsIgnoreCase(userId)) {
                        if (otpRefId != null && !otpRefId.isBlank()) {
                            String cachedEmail = emailCache.get(otpRefId);
                            if (cachedEmail != null && !cachedEmail.isBlank()) {
                                userId = cachedEmail;
                            } else {
                                try {
                                    String fetchedEmail = fetchEmailByOtpRefId(otpRefId);
                                    if (fetchedEmail != null && !fetchedEmail.isBlank()) {
                                        userId = fetchedEmail;
                                        emailCache.put(otpRefId, fetchedEmail);
                                    } else {
                                        log.error("Email fetch returned null for otpRefId: {}", otpRefId);
                                    }
                                } catch (Exception e) {
                                    log.error("Exception while fetching email for otpRefId {}: {}", otpRefId, e.getMessage(), e);
                                }
                            }
                        } else {
                            log.error("Cannot fetch email - otpRefId is null or blank for setPassword endpoint: {}", apiUrl);
                        }
                    }

                    if (userId != null && !userId.isBlank() && !"anonymous".equalsIgnoreCase(userId)) {
                        finalUsername = userId;
                    } else {
                        log.error("Email fetch failed for setPassword endpoint - otpRefId: {}, API: {}", otpRefId, apiUrl);
                        finalUsername = "";
                    }
                } else {
                    finalUsername = userId == null || userId.isBlank() ? "anonymous" : userId;
                }

                MedgoEventAudit medgoEventAudit = new MedgoEventAudit();
                medgoEventAudit.setUsername(finalUsername);
                medgoEventAudit.setEventType(eventType != null && !eventType.isBlank() ? eventType : (apiName != null ? apiName : "unknown"));
                medgoEventAudit.setDeviceId(deviceId != null ? deviceId : "");
                medgoEventAudit.setEventTime(now);
                String normalizedPlatform = (platform != null && !platform.isBlank()) ? normalizePlatform(platform) : "";
                medgoEventAudit.setPlatform(normalizedPlatform);

                if (medgoEventAuditService == null) {
                    log.warn("MedgoEventAuditService is null, attempting direct repository save");
                    try {
                        medgoAuditLogsRepository.saveAndFlush(medgoEventAudit);
                    } catch (Exception e) {
                        log.error("Direct repository save failed: {}", e.getMessage(), e);
                    }
                } else {
                    try {
                        medgoEventAuditService.saveAuditLog(medgoEventAudit);
                    } catch (Exception e) {
                        log.error("Service saveAuditLog failed for API {}: {}", apiUrl, e.getMessage(), e);
                        try {
                            medgoAuditLogsRepository.saveAndFlush(medgoEventAudit);
                        } catch (Exception fallbackEx) {
                            log.error("Fallback repository save failed: {}", fallbackEx.getMessage(), fallbackEx);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to save MedgoEventAudit entry for API: {}, EventType: {}, Status: {}, Error: {}",
                        apiUrl, eventType, status, e.getMessage(), e);
            }
        }
        return response;
    }


    private String toSafeJson(Object response) {
        try {
            if (response instanceof ResponseEntity<?> resp) {
                Object body = resp.getBody();
                Map<String, Object> safe = new LinkedHashMap<>();
                safe.put("status", resp.getStatusCode().value());
                safe.put("headers", resp.getHeaders());

                if (body instanceof org.springframework.core.io.Resource res) {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("type", res.getClass().getSimpleName());
                    info.put("filename", res.getFilename());
                    try {
                        info.put("contentLength", res.contentLength());
                    } catch (Exception ignored) {
                        // Ignore contentLength errors
                    }
                    safe.put("body", info);
                } else if (body instanceof byte[]) {
                    safe.put("body", "byte[" + ((byte[]) body).length + "]");
                } else {
                    safe.put("body", body);
                }

                return objectMapper.writeValueAsString(safe);
            } else {
                return objectMapper.writeValueAsString(response);
            }
        } catch (Exception e) {
            log.warn("Failed to serialize response safely: {}", e.getMessage());
            return "\"<unserializable>\"";
        }
    }

    private boolean isSetPasswordEndpoint(String apiUrl) {
        if (apiUrl == null || apiUrl.isBlank()) {
            return false;
        }
        String lowerApiUrl = apiUrl.toLowerCase();
        return lowerApiUrl.contains("/setpasswordmember") ||
                lowerApiUrl.contains("/setpasswordnonmember");
    }

    private boolean shouldAuditEndpoint(String apiUrl, PostMapping postMapping) {
        if (apiUrl == null || apiUrl.isBlank()) {
            if (postMapping != null && postMapping.value().length > 0) {
                String mappingValue = postMapping.value()[0];
                return mappingValue.equals("/login/member") ||
                        mappingValue.equals("/login/nonmember") ||
                        mappingValue.equals("/biometric/login") ||
                        mappingValue.equals("/setPasswordMember") ||
                        mappingValue.equals("/setPasswordNonMember");
            }
            return false;
        }

        String lowerApiUrl = apiUrl.toLowerCase();
        return lowerApiUrl.endsWith("/login/member") ||
                lowerApiUrl.endsWith("/login/nonmember") ||
                lowerApiUrl.endsWith("/biometric/login") ||
                lowerApiUrl.endsWith("/setpasswordmember") ||
                lowerApiUrl.endsWith("/setpasswordnonmember");
    }

    private String determineEventType(String apiUrl, String apiName, String status) {
        String lowerApiUrl = (apiUrl != null) ? apiUrl.toLowerCase() : "";

        if (lowerApiUrl.contains("/login/member") || lowerApiUrl.contains("/login/nonmember")
                || lowerApiUrl.contains("/biometric/login")) {
            return "SUCCESS".equals(status) ? "LOGIN SUCCESS" : "LOGIN FAIL";
        }

        if (isSetPasswordEndpoint(apiUrl)) {
            return "SUCCESS".equals(status) ? "REGISTRATION SUCCESS" : "REGISTRATION FAIL";
        }

        if (apiName != null && !apiName.isBlank()) {
            return apiName + " " + status.toLowerCase();
        }

        return "api " + status.toLowerCase();
    }

    /**
     * Fetches email using otpRefId from OtpService.
     * Uses reflection to avoid direct dependency on auth-service classes.
     *
     * @param otpRefId The OTP reference ID
     * @return Email address if found, null otherwise
     */
    @SuppressWarnings("unchecked")
    private String fetchEmailByOtpRefId(String otpRefId) {
        if (applicationContext == null) {
            log.warn("ApplicationContext is null, cannot fetch email");
            return null;
        }

        if (otpRefId == null || otpRefId.isBlank()) {
            log.warn("otpRefId is null or blank");
            return null;
        }

        Object otpService = null;
        String[] possibleBeanNames = {"otpServiceImpl", "otpService"};
        for (String beanName : possibleBeanNames) {
            try {
                otpService = applicationContext.getBean(beanName);
                if (otpService != null) {
                    break;
                }
            } catch (BeansException e) {
                log.debug("OtpService bean '{}' not found", beanName);
            }
        }

        if (otpService == null) {
            log.warn("OtpService bean not found in application context");
            return null;
        }

        try {
            java.lang.reflect.Method getUserDataMethod = otpService.getClass()
                    .getMethod("getUserDataByRefId", String.class);
            Object userDataObj = getUserDataMethod.invoke(otpService, otpRefId);

            if (userDataObj == null) {
                log.warn("No user data found for otpRefId: {}", otpRefId);
                return null;
            }

            // Extract email from user data Map
            if (userDataObj instanceof Map<?, ?> userData) {
                Object emailObj = userData.get("email");
                if (emailObj != null && !emailObj.toString().isBlank()) {
                    return emailObj.toString().trim();
                }
            }

            // Try to get email directly from Redis model
            try {
                java.lang.reflect.Field otpRedisTemplateField = otpService.getClass().getDeclaredField("otpRedisTemplate");
                otpRedisTemplateField.setAccessible(true);
                Object otpRedisTemplate = otpRedisTemplateField.get(otpService);

                if (otpRedisTemplate != null) {
                    java.lang.reflect.Method opsForValueMethod = otpRedisTemplate.getClass().getMethod("opsForValue");
                    Object valueOps = opsForValueMethod.invoke(otpRedisTemplate);
                    java.lang.reflect.Method getMethod = valueOps.getClass().getMethod("get", Object.class);
                    Object model = getMethod.invoke(valueOps, "otp:session:" + otpRefId);

                    if (model != null) {
                        try {
                            java.lang.reflect.Method getEmailMethod = model.getClass().getMethod("getEmail");
                            Object emailFromModel = getEmailMethod.invoke(model);
                            if (emailFromModel != null && !emailFromModel.toString().isBlank()) {
                                return emailFromModel.toString().trim();
                            }
                        } catch (NoSuchMethodException e) {
                            log.debug("getEmail method not found on model");
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not access UserOTPModel directly: {}", e.getMessage());
            }

            log.warn("Email not found in user data or model for otpRefId: {}", otpRefId);
            return null;
        } catch (NoSuchMethodException e) {
            log.error("getUserDataByRefId method not found: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Exception while fetching email using otpRefId {}: {}", otpRefId, e.getMessage(), e);
            return null;
        }
    }



    private String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return "";
        }

        String normalized = platform.trim().toLowerCase();

        if (normalized.contains("huawei") || normalized.contains("harmonyos") || normalized.contains("harmony")) {
            return "Huawei";
        }

        if (normalized.contains("android")) {
            return "Android";
        }

        if (normalized.contains("ios") || normalized.contains("iphone") || normalized.contains("ipad")) {
            return "iOS";
        }

        if ("android".equals(normalized) || "ios".equals(normalized)) {
            return normalized;
        }

        return "";
    }

    private String normalizePlatformFromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "";
        }

        String lowerUserAgent = userAgent.toLowerCase();

        if (lowerUserAgent.contains("huawei") || lowerUserAgent.contains("harmonyos") || lowerUserAgent.contains("harmony")) {
            return "Huawei";
        }

        if (lowerUserAgent.contains("android")) {
            return "Android";
        }

        if (lowerUserAgent.contains("iphone") || lowerUserAgent.contains("ipad") ||
                lowerUserAgent.contains("ios") || lowerUserAgent.contains("cfnetwork")) {
            return "iOS";
        }

        return "";
    }

}

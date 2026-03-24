package com.medgo.auth.serviceImpl;

import com.medgo.auth.clients.NotificationServiceClient;
import com.medgo.auth.commonutilitys.Utilitys;
import com.medgo.auth.constants.Constants;
import com.medgo.auth.domain.entity.medigo.OtpBypassMemberCode;
import com.medgo.auth.domain.entity.medigo.UserOTPModel;
import com.medgo.auth.domain.request.EmailRequest;
import com.medgo.auth.domain.request.ResendOtpRequest;
import com.medgo.auth.domain.request.VerifyOtpRequest;
import com.medgo.auth.domain.response.OtpResponse;
import com.medgo.auth.repository.medigo.OtpBypassMemberCodeRepository;
import com.medgo.auth.service.OtpService;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.enums.CustomStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtpServiceImpl.class);

    private static final int SESSION_TTL_MINUTES = 20;   // Session valid for 15 mins
    private static final int MAX_ATTEMPTS = 3;           // Max attempts per OTP
    private static final int RESEND_WAIT_SECONDS = 90;   // Min 90s wait between resend
    private static final int OTP_EXPIRY_SECONDS = 90;    // OTP valid for 90s
    private static final int ACCOUNT_LOCK_MINUTES = 15;

    private final RedisTemplate<String, UserOTPModel> otpRedisTemplate;
    private final NotificationServiceClient notificationServiceClient;
    private final OtpBypassMemberCodeRepository otpBypassMemberCodeRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    public OtpServiceImpl(RedisTemplate<String, UserOTPModel> otpRedisTemplate,
                          NotificationServiceClient notificationServiceClient,
                          OtpBypassMemberCodeRepository otpBypassMemberCodeRepository) {
        this.otpRedisTemplate = otpRedisTemplate;
        this.notificationServiceClient = notificationServiceClient;
        this.otpBypassMemberCodeRepository = otpBypassMemberCodeRepository;
    }

    private String generateOtpCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    @Override
    public CommonResponse generateOtp(String identifier, String email, String mobile, Map<String, Object> userData) {
        if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Identifier required"),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (Utilitys.isLocked(otpRedisTemplate, identifier)) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.LOCKED.value(),
                            "OTP entered maximum times. Your account has been locked for 15 minutes. Please try again after the lockout period."
                    ),
                    HttpStatus.LOCKED.value()
            );
        }

        String memberCode = userData != null ? (String) userData.get("memberCode") : null;
        boolean isBypassMember = false;
        String bypassOtp = null;

        if (memberCode != null && !memberCode.isBlank()) {
            Optional<OtpBypassMemberCode> bypassMemberOpt =
                    otpBypassMemberCodeRepository.findByMemberCodeAndIsActiveTrue(memberCode);
            if (bypassMemberOpt.isPresent()) {
                isBypassMember = true;
                bypassOtp = bypassMemberOpt.get().getDefaultOtp();
            }
        }

        String otp = generateOtpCode();
        String otpRefId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        if (userData == null) userData = new HashMap<>();
        String existingFlowType = userData.get("flowType") != null ? (String) userData.get("flowType") : null;
        if (existingFlowType == null) {
            userData.put("flowType", "MEMBER_REGISTRATION");
        }
        if (isBypassMember) {
            userData.put("isBypassMember", true);
            userData.put("bypassOtp", bypassOtp);
        }

        UserOTPModel model = UserOTPModel.builder()
                .otpRefId(otpRefId)
                .otp(otp)
                .attempts(0)
                .otpGenTime(now)
                .lastSentAt(now)
                .identifier(identifier)
                .data(userData)
                .otpValidated(false)
                .build();

        otpRedisTemplate.opsForValue()
                .set("otp:session:" + otpRefId, model, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        if (!isBypassMember) {
            if (email != null && !email.isBlank()) sendOtpEmail(email, otp);
            if (mobile != null && !mobile.isBlank()) LOGGER.info("OTP {} would be sent via SMS to {}", otp, mobile);
        } else {
            LOGGER.info("Bypass member {} - OTP email/SMS skipped. User should use default OTP: {}",
                    memberCode, bypassOtp);
        }

        return CommonResponse.success(
                Map.of("message", "OTP generated and sent successfully", "otpRefId", otpRefId)
        );
    }

    @Override
    public CommonResponse generateLoginOtp(String identifier, String email, String mobile, Map<String, Object> userData) {
        if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Identifier required"),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (Utilitys.isLocked(otpRedisTemplate, identifier)) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.LOCKED.value(),
                            "OTP entered maximum times. Your account has been locked for 15 minutes. Please try again after the lockout period."
                    ),
                    HttpStatus.LOCKED.value()
            );
        }

        // Check if member is in bypass table - if yes, don't send OTP email
        String memberCode = userData != null ? (String) userData.get("memberCode") : null;
        boolean isBypassMember = false;
        String bypassOtp = null;

        if (memberCode != null && !memberCode.isBlank()) {
            Optional<OtpBypassMemberCode> bypassMemberOpt =
                    otpBypassMemberCodeRepository.findByMemberCodeAndIsActiveTrue(memberCode);

            if (bypassMemberOpt.isPresent()) {
                isBypassMember = true;
                bypassOtp = bypassMemberOpt.get().getDefaultOtp();
                LOGGER.info("Member code {} is in bypass table. Default OTP: {}. Skipping OTP email/SMS.",
                        memberCode, bypassOtp);
            }
        }

        String otp = generateOtpCode();
        String otpRefId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        if (userData == null) userData = new HashMap<>();
        userData.put("flowType", "LOGIN");

        // Clean up previous LOGIN OTP sessions for this identifier to ensure only the newest otpRefId is valid.
        try {
            Set<String> keys = otpRedisTemplate.keys("otp:session:*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    UserOTPModel existing = otpRedisTemplate.opsForValue().get(key);
                    if (existing == null) continue;
                    if (identifier != null && identifier.equals(existing.getIdentifier())) {
                        Map<String, Object> d = existing.getData();
                        String existingFlow = d != null && d.get("flowType") != null ? String.valueOf(d.get("flowType")) : null;
                        if (existingFlow != null && "LOGIN".equalsIgnoreCase(existingFlow)) {
                            otpRedisTemplate.delete(key);
                            LOGGER.info("Removed previous LOGIN OTP session {} for identifier={}", key, identifier);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to clean previous LOGIN OTP sessions for identifier={}: {}", identifier, e.getMessage());
        }
        if (isBypassMember) {
            userData.put("isBypassMember", true);
            userData.put("bypassOtp", bypassOtp);
        }

        UserOTPModel model = UserOTPModel.builder()
                .otpRefId(otpRefId)
                .otp(otp)
                .attempts(0)
                .otpGenTime(now)
                .lastSentAt(now)
                .identifier(identifier)
                .data(userData)
                .otpValidated(false)
                .build();

        otpRedisTemplate.opsForValue()
                .set("otp:session:" + otpRefId, model, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        if (!isBypassMember) {
            if (email != null && !email.isBlank()) sendLoginOtpEmail(email, otp);
            if (mobile != null && !mobile.isBlank()) LOGGER.info("OTP {} would be sent via SMS to {}", otp, mobile);
        } else {
            LOGGER.info("Bypass member {} - OTP email/SMS skipped. User should use default OTP: {}",
                    memberCode, bypassOtp);
        }

        return CommonResponse.success(
                Map.of("message", "OTP generated and sent successfully", "otpRefId", otpRefId)
        );
    }

    @Override
    public CommonResponse generatePasswordResetOtp(String identifier, String email, String mobile, Map<String, Object> userData) {
        if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Identifier required"),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (Utilitys.isLocked(otpRedisTemplate, identifier)) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.LOCKED.value(),
                            "OTP entered maximum times. Your account has been locked for 15 minutes. Please try again after the lockout period."
                    ),
                    HttpStatus.LOCKED.value()
            );
        }

        String memberCode = userData != null ? (String) userData.get("memberCode") : null;
        boolean isBypassMember = false;
        String bypassOtp = null;

        if (memberCode != null && !memberCode.isBlank()) {
            Optional<OtpBypassMemberCode> bypassMemberOpt =
                    otpBypassMemberCodeRepository.findByMemberCodeAndIsActiveTrue(memberCode);
            if (bypassMemberOpt.isPresent()) {
                isBypassMember = true;
                bypassOtp = bypassMemberOpt.get().getDefaultOtp();
            }
        }

        String otp = generateOtpCode();
        String otpRefId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        if (userData == null) userData = new HashMap<>();
        userData.put("flowType", "PASSWORD_RESET");
        if (isBypassMember) {
            userData.put("isBypassMember", true);
            userData.put("bypassOtp", bypassOtp);
        }

        // Cleanup previous PASSWORD_RESET sessions for this identifier so only newest works
        try {
            Set<String> keys = otpRedisTemplate.keys("otp:session:*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    UserOTPModel existing = otpRedisTemplate.opsForValue().get(key);
                    if (existing == null) continue;
                    if (identifier != null && identifier.equals(existing.getIdentifier())) {
                        Map<String, Object> d = existing.getData();
                        String existingFlow = d != null && d.get("flowType") != null ? String.valueOf(d.get("flowType")) : null;
                        if (existingFlow != null && "PASSWORD_RESET".equalsIgnoreCase(existingFlow)) {
                            otpRedisTemplate.delete(key);
                            LOGGER.info("Removed previous PASSWORD_RESET OTP session {} for identifier={}", key, identifier);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to clean previous PASSWORD_RESET OTP sessions for identifier={}: {}", identifier, e.getMessage());
        }

        UserOTPModel model = UserOTPModel.builder()
                .otpRefId(otpRefId)
                .otp(otp)
                .attempts(0)
                .otpGenTime(now)
                .lastSentAt(now)
                .identifier(identifier)
                .data(userData)
                .otpValidated(false)
                .build();

        otpRedisTemplate.opsForValue()
                .set("otp:session:" + otpRefId, model, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        if (!isBypassMember) {
            if (email != null && !email.isBlank()) sendOtpEmail(email, otp);
            if (mobile != null && !mobile.isBlank()) LOGGER.info("OTP {} would be sent via SMS to {}", otp, mobile);
        } else {
            LOGGER.info("Bypass member {} - OTP email/SMS skipped. User should use default OTP: {}",
                    memberCode, bypassOtp);
        }

        return CommonResponse.success(
                Map.of("message", "OTP generated and sent successfully", "otpRefId", otpRefId)
        );
    }

    @Override
    public CommonResponse verifyOtp(VerifyOtpRequest request) {
        String otpRefId = request.otpRefId();
        LOGGER.info("Starting OTP verification for otpRefId={}", otpRefId);

        UserOTPModel session = otpRedisTemplate.opsForValue().get("otp:session:" + otpRefId);

        ErrorResponse otpExpired = new ErrorResponse(CustomStatusCode.EXPIRED_OTP.getCode(),
                CustomStatusCode.EXPIRED_OTP.getMessage()
        );

        // Step 1: Session check
        if (session == null) {
            return CommonResponse.error(otpExpired, HttpStatus.UNAUTHORIZED.value());
        }
        LOGGER.info("OTP session found for otpRefId={}, identifier={}, attempts={}",
                otpRefId, session.getIdentifier(), session.getAttempts()
        );

        // Step 1.5: Flow type check - require the client-supplied flowType to match the session's flowType.
        // If the session does not contain a flowType (older sessions), prefer the client's requested flow so
        // login verifications continue to work without needing a new OTP.
        String requestedFlowType = request.flowType() != null ? request.flowType().trim() : null;
        String sessionFlowType = null;
        Map<String, Object> sessionData = session.getData();
        if (sessionData != null && sessionData.get("flowType") != null) {
            sessionFlowType = String.valueOf(sessionData.get("flowType")).trim();
        }
        // Strict check: session must contain a flowType and it must match the requested flowType exactly (case-insensitive).
        if (sessionFlowType == null || sessionFlowType.isBlank()
                || requestedFlowType == null || requestedFlowType.isBlank()
                || !sessionFlowType.equalsIgnoreCase(requestedFlowType)) {
            LOGGER.warn("OTP flowType mismatch for otpRefId={}. sessionFlowType={}, requestedFlowType={}",
                    otpRefId, sessionFlowType, requestedFlowType);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Please select valid flow type."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Step 2: Max attempts check - prevent further attempts if already locked or at limit
        if (session.getAttempts() >= MAX_ATTEMPTS) {
            LOGGER.error("OTP attempt limit exceeded for identifier={} (otpRefId={})",
                    session.getIdentifier(), otpRefId
            );

            Utilitys.lockAccount(otpRedisTemplate, session.getIdentifier(), ACCOUNT_LOCK_MINUTES);

            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_ATTEMPT_EXCEEDED.getCode(),
                            "Maximum OTP attempts reached. Please try again after 15 minutes."
                    ),
                    HttpStatus.LOCKED.value()
            );
        }

        // Step 3: Check if member code is in bypass table (needed for OTP validation)
        String memberCode = (String) session.getData().get("memberCode");
        boolean isBypassMember = false;
        String expectedOtp = session.getOtp(); // Default to generated OTP

        LOGGER.info("OTP verification - memberCode from session: {}, otpRefId: {}",
                memberCode, otpRefId);

        if (memberCode != null && !memberCode.isBlank()) {
            Optional<OtpBypassMemberCode> bypassMemberOpt =
                    otpBypassMemberCodeRepository.findByMemberCodeAndIsActiveTrue(memberCode);

            if (bypassMemberOpt.isPresent()) {
                isBypassMember = true;
                expectedOtp = bypassMemberOpt.get().getDefaultOtp();
                LOGGER.info("Member code {} is in bypass table. Using default OTP: {} (entered OTP: {})",
                        memberCode, expectedOtp, request.otp());
            } else {
                LOGGER.info("Member code {} is NOT in bypass table. Using generated OTP.", memberCode);
            }
        } else {
            LOGGER.info("No memberCode found in session data. Using generated OTP.");
        }

        // Step 4: OTP match check FIRST - if wrong OTP, return INVALID_OTP (even if expired)
        if (!Objects.equals(expectedOtp, request.otp())) {
            session.setAttempts(session.getAttempts() + 1);
            otpRedisTemplate.opsForValue().set("otp:session:" + otpRefId, session, SESSION_TTL_MINUTES,
                    TimeUnit.MINUTES
            );

            LOGGER.info("Invalid OTP entered for identifier={} (otpRefId={}, attempts={}, expected={}, isBypass={})",
                    session.getIdentifier(), otpRefId, session.getAttempts(), expectedOtp, isBypassMember
            );

            if (session.getAttempts() >= MAX_ATTEMPTS) {
                LOGGER.error("OTP entered maximum times ({} attempts) for identifier={} (otpRefId={}). Locking account for {} minutes.",
                        session.getAttempts(), session.getIdentifier(), otpRefId, ACCOUNT_LOCK_MINUTES
                );
                Utilitys.lockAccount(otpRedisTemplate, session.getIdentifier(), ACCOUNT_LOCK_MINUTES);
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.OTP_ATTEMPT_EXCEEDED.getCode(),
                                "Maximum OTP attempts reached. Please try again after 15 minutes."
                        ),
                        HttpStatus.LOCKED.value()
                );
            }

            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.INVALID_OTP.getCode(),
                            CustomStatusCode.INVALID_OTP.getMessage()
                    ),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        // Step 5: Expiry check (only after OTP is validated as correct)
        if (session.getOtpGenTime().plusSeconds(OTP_EXPIRY_SECONDS).isBefore(LocalDateTime.now())) {
            LOGGER.info("OTP expired for identifier={} (otpRefId={}, generatedAt={})",
                    session.getIdentifier(), otpRefId, session.getOtpGenTime()
            );
            return CommonResponse.error(otpExpired, HttpStatus.UNAUTHORIZED.value());
        }

        // Step 6: Successful verification
        String newOtpRefId = UUID.randomUUID().toString();

        // Mark this OTP session as successfully validated
        session.setOtpValidated(true);

        Map<String, Object> userData = session.getData();
        LOGGER.info("Copying OTP session - oldRefId: {}, newRefId: {}, userData keys: {}, isBypass: {}",
                otpRefId, newOtpRefId,
                userData != null ? userData.keySet() : "null",
                isBypassMember);

        // Verify critical data is present
        if (userData != null) {
            Object userIdObj = userData.get("userId");
            LOGGER.info("UserData contains userId: {} (type: {})",
                    userIdObj,
                    userIdObj != null ? userIdObj.getClass().getName() : "null");
        }

        otpRedisTemplate.opsForValue()
                .set("otp:session:" + newOtpRefId, session, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        otpRedisTemplate.delete("otp:session:" + otpRefId);
        LOGGER.info("OTP verified successfully for identifier={} (oldRefId={}, newRefId={}, isBypass={})",
                session.getIdentifier(), otpRefId, newOtpRefId, isBypassMember
        );

        return CommonResponse.success(new OtpResponse(Constants.OTP_VERIFIED_SUCCESS, newOtpRefId));
    }


    // Resend OTP
    @Override
    public CommonResponse resendOtp(ResendOtpRequest request) {
        String otpRefId = request.otpRefId();
        if (otpRefId == null || otpRefId.isBlank()) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            CustomStatusCode.OTP_GENERATION_FAILED.getMessage()
                    ),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        UserOTPModel session = otpRedisTemplate.opsForValue().get("otp:session:" + otpRefId);
        if (session == null) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.SESSION_EXPIRED.getCode(),
                            CustomStatusCode.SESSION_EXPIRED.getMessage()
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        CommonResponse LOCKED = ifUserLocked(session.getIdentifier());
        if (LOCKED != null) return LOCKED;

        // Enforce resend wait
        if (session.getLastSentAt().plusSeconds(RESEND_WAIT_SECONDS).isAfter(LocalDateTime.now())) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.WAIT_BEFORE_NEXT_OTP.getCode(),
                            CustomStatusCode.WAIT_BEFORE_NEXT_OTP.getMessage()
                    ),
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );
        }

        if (session.getAttempts() >= MAX_ATTEMPTS) {
            Utilitys.lockAccount(otpRedisTemplate, session.getIdentifier(), ACCOUNT_LOCK_MINUTES);
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_ATTEMPT_EXCEEDED.getCode(),
                            "OTP entered maximum times. Your account has been locked for 15 minutes. Please try again after the lockout period."
                    ),

                    HttpStatus.LOCKED.value()
            );
        }

        // Generate new OTP
        String otp = generateOtpCode();
        LocalDateTime now = LocalDateTime.now();
        session.setOtp(otp);
        session.setOtpGenTime(now);
        session.setLastSentAt(now);
        session.setAttempts(session.getAttempts() + 1);
        session.setOtpValidated(false);

        otpRedisTemplate.opsForValue().set("otp:session:" + otpRefId, session, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        String email = (String) session.getData().get("email");
        String mobile = (String) session.getData().get("mobile");
        LOGGER.info("Resending OTP {} for identifier {}", otp, session.getData().toString());

        Map<String, Object> userData = session.getData();
        String flowType = userData != null ? (String) userData.get("flowType") : null;
        boolean isLoginFlow = "LOGIN".equals(flowType);

        // Check if this is a bypass member - bypass members should NOT receive OTP emails
        Boolean isBypassMember = userData != null && userData.get("isBypassMember") != null
                ? (Boolean) userData.get("isBypassMember") : false;
        String bypassOtp = userData != null ? (String) userData.get("bypassOtp") : null;

        if (!isBypassMember) {
            if (email != null && !email.isBlank()) {
                if (isLoginFlow) {
                    sendLoginOtpEmail(email, otp);
                } else {
                    sendOtpEmail(email, otp);
                }
            }
            if (mobile != null && !mobile.isBlank()) LOGGER.info("OTP {} would be sent via SMS to {}", otp, mobile);
        } else {
            LOGGER.info("Bypass member - OTP email/SMS skipped on resend. User should use default OTP: {}", bypassOtp);
        }

        return CommonResponse.success(Map.of(
                        "otpRefId", otpRefId,
                        "message", "OTP resent successfully"
                )
        );
    }

    @Override
    public CommonResponse ifUserLocked(String identifier) {
        if (Utilitys.isLocked(otpRedisTemplate, identifier)) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.LOCKED.value(),
                            "OTP entered maximum times. Your account has been locked for 15 minutes. Please try again after the lockout period."
                    ),
                    HttpStatus.LOCKED.value()
            );
        }
        return null;
    }

    @Override
    public Map<String, Object> getUserDataByRefId(String otpRefId) {
        LOGGER.info("Fetching user data for otpRefId={}", otpRefId);

        UserOTPModel model = otpRedisTemplate.opsForValue().get("otp:session:" + otpRefId);
        LOGGER.info("OTP session fetched from Redis for otpRefId={}, model={}", otpRefId, model);
        if (model == null) {
            LOGGER.info("No OTP session found in Redis for otpRefId={}", otpRefId);
            return Collections.emptyMap();
        }

        Map<String, Object> data = model.getData();
        if (data == null || data.isEmpty()) {
            LOGGER.error("OTP session found but no user data attached for otpRefId={}, model={}", otpRefId, model);
            return Collections.emptyMap();
        }

        LOGGER.info("User data retrieved for otpRefId={}, keys={}", otpRefId, data.keySet());
        return data;
    }

    @Override
    public boolean isOtpValidated(String otpRefId) {
        if (otpRefId == null || otpRefId.isBlank()) {
            LOGGER.warn("isOtpValidated called with null or blank otpRefId");
            return false;
        }

        UserOTPModel model = otpRedisTemplate.opsForValue().get("otp:session:" + otpRefId);
        if (model == null) {
            LOGGER.info("No OTP session found in Redis for otpRefId={} when checking validation", otpRefId);
            return false;
        }

        boolean validated = model.isOtpValidated();
        LOGGER.info("OTP validation status for otpRefId={} is {}", otpRefId, validated);
        return validated;
    }

    @Override
    public void cleanupOtp(String otpRefId) {
        if (otpRefId == null || otpRefId.isBlank()) return;
        otpRedisTemplate.delete("otp:session:" + otpRefId);
        LOGGER.info("Cleaned up OTP session for refId={}", otpRefId);
    }

    @Override
    public void markRegistrationValidatedForMember(String memberCode) {
        if (memberCode == null || memberCode.isBlank()) return;
        try {
            Set<String> keys = otpRedisTemplate.keys("otp:session:*");
            if (keys == null || keys.isEmpty()) return;
            for (String key : keys) {
                UserOTPModel model = otpRedisTemplate.opsForValue().get(key);
                if (model == null) continue;
                Map<String, Object> data = model.getData();
                if (data == null) continue;
                Object mc = data.get("memberCode");
                if (mc != null && memberCode.equals(String.valueOf(mc))) {
                    data.put("registrationValidated", true);
                    model.setData(data);
                    otpRedisTemplate.opsForValue().set(key, model, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
                    LOGGER.info("Marked registrationValidated for OTP session key={}, memberCode={}", key, memberCode);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to mark registrationValidated for memberCode={}", memberCode, e);
        }
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            var body = String.format(Constants.EMAIL_BODY_TEMPLATE, otp);

            var emailRequest = new EmailRequest(
                    Constants.EMAIL_SUBJECT_OTP,
                    body,
                    Collections.singletonList(email),
                    Constants.EMAIL_TYPE_OTP_VERIFICATION,
                    null,
                    null,
                    Constants.EMAIL_CONTENT_TYPE,
                    null
            );
            LOGGER.info("Generated OTP {} for identifier {}", otp, email);
            notificationServiceClient.sendEmail(emailRequest);
        } catch (Exception e) {
            LOGGER.error("Failed to send OTP email to {}", email, e);
        }

    }

    private void sendLoginOtpEmail(String email, String otp) {
        try {
            var body = String.format(Constants.EMAIL_BODY_TEMPLATE, otp);

            var emailRequest = new EmailRequest(
                    Constants.LOGIN_EMAIL_SUBJECT_OTP,                   // subject: "Your Login OTP Code"
                    body,                                         // body
                    Collections.singletonList(email),            // toEmails
                    Constants.EMAIL_TYPE_OTP_VERIFICATION,       // type
                    null,                                        // ccEmails
                    null,                                        // bccEmails
                    Constants.EMAIL_CONTENT_TYPE,                // contentType
                    null                                         // attachments
            );
            LOGGER.info("Generated OTP {} for identifier {}", otp, email);
            notificationServiceClient.sendEmail(emailRequest);
        } catch (Exception e) {
            LOGGER.error("Failed to send OTP email to {}", email, e);
        }

    }
}










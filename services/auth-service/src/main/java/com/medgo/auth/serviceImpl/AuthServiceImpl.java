package com.medgo.auth.serviceImpl;

import com.medgo.auth.clients.NotificationServiceClient;
import com.medgo.auth.commonutilitys.LoginAttemptTracker;
import com.medgo.auth.constants.Constants;
import com.medgo.auth.domain.entity.medigo.*;
import com.medgo.auth.domain.entity.membership.MembershipModel;
import com.medgo.auth.domain.request.*;
import com.medgo.auth.domain.response.OtpResponse;
import com.medgo.auth.repository.medigo.InvalidBirthdateLogRepository;
import com.medgo.auth.repository.medigo.NonMemberUserRepositry;
import com.medgo.auth.repository.medigo.TrustedDeviceRepository;
import com.medgo.auth.repository.medigo.UserRepository;
import com.medgo.auth.repository.membership.MembershipModelRepository;
import com.medgo.auth.service.ConsentService;
import com.medgo.auth.service.OtpService;
import com.medgo.auth.service.RegistrationService;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.commons.RequestContext;
import com.medgo.enums.CustomStatusCode;
import com.medgo.exception.CustomException;
import com.medgo.jwt.JwtTokenUtil;
import com.medgo.jwt.JwtUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
@Repository
public class AuthServiceImpl implements RegistrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Value("${auth.mock.enabled:true}")
    private boolean mockEnabled;

    private final NonMemberUserRepositry nonMemberUserRepositry;
    private final PasswordEncoder passwordEncoder;
    private final InvalidBirthdateLogRepository invalidBirthdateLogRepository;
    private final MembershipModelRepository membershipModelRepository;
    private final UserRepository userRepository;
    private OtpService otpService;
    private TrustedDeviceRepository trustedDeviceRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final StringRedisTemplate redisTemplate;
    private final ConsentService consentService;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHALLENGE_PREFIX = "biometric:challenge:";
    private static final int CHALLENGE_EXPIRY_SECONDS = 120;

    public AuthServiceImpl(UserRepository userRepository,
                           NonMemberUserRepositry nonMemberUserRepositry,
                           PasswordEncoder passwordEncoder,
                           InvalidBirthdateLogRepository invalidBirthdateLogRepository,
                           MembershipModelRepository membershipModelRepository,
                           OtpService otpService,
                           TrustedDeviceRepository trustedDeviceRepository,
                           NotificationServiceClient notificationServiceClient,
                           JwtTokenUtil jwtTokenUtil,
                           JwtUserDetailsService jwtUserDetailsService,
                           StringRedisTemplate redisTemplate,
                           ConsentService consentService) {
        this.userRepository = userRepository;
        this.nonMemberUserRepositry = nonMemberUserRepositry;
        this.passwordEncoder = passwordEncoder;
        this.invalidBirthdateLogRepository = invalidBirthdateLogRepository;
        this.membershipModelRepository = membershipModelRepository;
        this.otpService = otpService;
        this.trustedDeviceRepository = trustedDeviceRepository;
        this.notificationServiceClient = notificationServiceClient;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.redisTemplate = redisTemplate;
        this.consentService = consentService;

    }


    @Override
    public CommonResponse setPassword(PasswordRequest request) {
        if (request.password().isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), Constants.PASSWORD_NOT_ALLOWED),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Ensure OTP has been successfully verified before allowing password set
        if (!otpService.isOtpValidated(request.otpRefId())) {
            LOGGER.warn("Set password attempt with unverified or expired OTP for otpRefId={}", request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "OTP not verified or session expired. Please verify OTP before setting your password."),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        Map<String, Object> userData = otpService.getUserDataByRefId(request.otpRefId());
        if (userData == null || userData.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.SESSION_EXPIRED.getCode(), "Session expired or invalid"),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // (no flow-type check here; handled in password-reset endpoints)

        // Allow password set only for member registration flows.
        String flowType = userData.get("flowType") != null ? String.valueOf(userData.get("flowType")) : null;
        if (flowType == null || flowType.isBlank()) flowType = "MEMBER_REGISTRATION";
        boolean allowedForSetPassword = "MEMBER_REGISTRATION".equalsIgnoreCase(flowType);
        if (!allowedForSetPassword) {
            LOGGER.warn("Set password blocked due to invalid flowType={} for otpRefId={}", flowType, request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Invalid OTP reference for setting password. Please verify OTP with flow type MEMBER_REGISTRATION."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // registrationValidated check removed: accept verified MEMBER_REGISTRATION OTPs for setting password.

        String memberCode = (String) userData.get("memberCode");
        String email = (String) userData.get("email");
        String mobile = (String) userData.get("mobile");
        Integer sex = userData.get("sex") != null ? (userData.get("sex") instanceof Integer ? (Integer) userData.get("sex") : Integer.valueOf(userData.get("sex").toString())) : null;
        LocalDate birthDate = null;
        if (userData.get("birthDate") != null) {
            birthDate = LocalDate.parse(userData.get("birthDate").toString());
        } else if (userData.get("dob") != null) { // backward compatibility
            birthDate = LocalDate.parse(userData.get("dob").toString());
        }
        String firstName = userData.get("firstName") != null ? userData.get("firstName").toString() : "";


        UserModel user = new UserModel();

        user.setMemberCode(memberCode);
        if (birthDate != null)
            user.setBirthDate(birthDate);
        if (email != null)
            user.setEmail(email);
        if (mobile != null)
            user.setMobile(mobile);
        if (sex != null) {
            user.setSex(sex);
        }

        if (userData.get("firstName") != null)
            user.setFirstName((String) userData.get("firstName"));
        if (userData.get("lastName") != null)
            user.setLastName((String) userData.get("lastName"));
        if (userData.get("middleName") != null)
            user.setMiddleName((String) userData.get("middleName"));


        String username = email != null && !email.isBlank()
                ? email
                : (mobile != null && !mobile.isBlank()
                ? mobile
                : (memberCode != null ? memberCode : ""));
        user.setUsername(username);
        user.setStatus(MedGoUserStatusEnum.ACTIVE);
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
        otpService.cleanupOtp(request.otpRefId());

        if (email != null && !email.isBlank()) {
            sendWelcomeEmail(email);
        }

        Integer sexValue = user.getSex();
        if (sexValue == null && memberCode != null) {
            Optional<MembershipModel> membership = membershipModelRepository.findByMemberCode(memberCode);
            if (membership.isPresent() && membership.get().getSex() != null) {
                sexValue = membership.get().getSex();
            }
        }

        LocalDate birthDateValue = user.getBirthDate();
        if (birthDateValue == null && memberCode != null) {
            Optional<MembershipModel> membership = membershipModelRepository.findByMemberCode(memberCode);
            if (membership.isPresent() && membership.get().getBirthDate() != null) {
                birthDateValue = membership.get().getBirthDate().toLocalDate();
            }
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", Constants.PASSWORD_SET_SUCCESS_MEMBER);
        return CommonResponse.success(responseData);

    }

    @Override
    public CommonResponse setNonMemberPassword(PasswordRequest request) {
        if (request.password().isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), Constants.PASSWORD_NOT_ALLOWED),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Ensure OTP has been successfully verified before allowing password set
        if (!otpService.isOtpValidated(request.otpRefId())) {
            LOGGER.warn("Set non-member password attempt with unverified or expired OTP for otpRefId={}", request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "OTP not verified or session expired. Please verify OTP before setting your password."),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        Map<String, Object> userData = otpService.getUserDataByRefId(request.otpRefId());
        if (userData == null || userData.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Session expired or invalid"),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // (removed accidental PASSWORD_RESET check here)

        // Allow non-member password set only for member registration flows.
        String flowType = userData.get("flowType") != null ? String.valueOf(userData.get("flowType")) : null;
        if (flowType == null || flowType.isBlank()) flowType = "MEMBER_REGISTRATION";
        boolean allowedForSetPassword = "MEMBER_REGISTRATION".equalsIgnoreCase(flowType);
        if (!allowedForSetPassword) {
            LOGGER.warn("Set non-member password blocked due to invalid flowType={} for otpRefId={}", flowType, request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Invalid OTP reference for setting password. Please verify OTP with flow type MEMBER_REGISTRATION."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // registrationValidated check removed: accept verified MEMBER_REGISTRATION OTPs for setting password.

        String email = (String) userData.get("email");
        String mobile = (String) userData.get("mobile");

        NonMemberUserModel user = new NonMemberUserModel();
        if (email != null) user.setEmail(email);
        if (mobile != null) user.setMobile(mobile);

        String username = email != null && !email.isBlank()
                ? email
                : (mobile != null && !mobile.isBlank() ? mobile : "");
        user.setUsername(username);

        user.setStatus(MedGoUserStatusEnum.ACTIVE);
        user.setPassword(passwordEncoder.encode(request.password()));

        if (user.getNonMemberCode() == null || user.getNonMemberCode().isEmpty()) {
            String generatedCode = generateNextNonMemberCode();
            user.setNonMemberCode(generatedCode);
            LOGGER.info("Generated nonMemberCode: {} for new non-member user", generatedCode);
        }

        nonMemberUserRepositry.save(user);
        otpService.cleanupOtp(request.otpRefId());

        if (email != null && !email.isBlank()) {
            sendWelcomeEmail(email);
        }
        return CommonResponse.success(
                Constants.PASSWORD_SET_SUCCESS_NONMEMBER

        );
    }

    @Override
    public CommonResponse resetMemberPassword(ResetPasswordRequest request) {
        if (request.newPassword().isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), Constants.PASSWORD_NOT_ALLOWED),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Ensure OTP has been successfully verified before allowing password reset
        if (!otpService.isOtpValidated(request.otpRefId())) {
            LOGGER.warn("Password reset attempt with unverified or expired OTP for otpRefId={}", request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "OTP not verified Please verify OTP before resetting your password."),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        Map<String, Object> userData = otpService.getUserDataByRefId(request.otpRefId());
        if (userData == null || userData.isEmpty()) {
            LOGGER.warn("Password reset failed for member: OTP session not found or expired for otpRefId={}. " +
                            "User may need to request a new OTP, verify it, and use the new otpRefId from verification response.",
                    request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "OTP session expired or invalid. Please request a new OTP, verify it, and use the new otpRefId from the verification response to reset your password."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Ensure OTP flow type is PASSWORD_RESET for password reset operations
        String flowTypeForMemberReset = userData.get("flowType") != null ? String.valueOf(userData.get("flowType")) : null;
        if (flowTypeForMemberReset == null || !flowTypeForMemberReset.equalsIgnoreCase("PASSWORD_RESET")) {
            LOGGER.warn("Password reset blocked due to invalid flowType={} for otpRefId={}", flowTypeForMemberReset, request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Please select valid flow type."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        String memberCode = (String) userData.get("memberCode");
        return userRepository.findByMemberCode(memberCode)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(request.newPassword()));
                    LoginAttemptTracker.resetFailedAttempts(user);
                    LOGGER.info("Account unlocked for member {} after password reset", memberCode);

                    userRepository.save(user);
                    otpService.cleanupOtp(request.otpRefId());
                    return CommonResponse.success(
                            Constants.PASSWORD_RESET_SUCCESS_MEMBER

                    );
                })
                .orElse(CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                                Constants.USER_NOT_FOUND + memberCode),

                        HttpStatus.NOT_FOUND.value()
                ));
    }

    @Override
    public CommonResponse resetNonMemberPassword(ResetPasswordRequest request) {
        if (request.newPassword().isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), Constants.PASSWORD_NOT_ALLOWED),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Ensure OTP has been successfully verified before allowing password reset
        if (!otpService.isOtpValidated(request.otpRefId())) {
            LOGGER.warn("Non-member password reset attempt with unverified or expired OTP for otpRefId={}", request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "OTP not verified or session expired. Please verify OTP before resetting your password."),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        Map<String, Object> userData = otpService.getUserDataByRefId(request.otpRefId());
        if (userData == null || userData.isEmpty()) {
            LOGGER.warn("Password reset failed for non-member: OTP session not found or expired for otpRefId={}. " +
                            "User may need to request a new OTP, verify it, and use the new otpRefId from verification response.",
                    request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "OTP session expired or invalid. Please request a new OTP, verify it, and use the new otpRefId from the verification response to reset your password."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        // Ensure OTP flow type is PASSWORD_RESET for password reset operations
        String flowTypeForNonMemberReset = userData.get("flowType") != null ? String.valueOf(userData.get("flowType")) : null;
        if (flowTypeForNonMemberReset == null || !flowTypeForNonMemberReset.equalsIgnoreCase("PASSWORD_RESET")) {
            LOGGER.warn("Non-member password reset blocked due to invalid flowType={} for otpRefId={}", flowTypeForNonMemberReset, request.otpRefId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Please select valid flow type."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        String email = (String) userData.get("email");
        String mobile = (String) userData.get("mobile");

        Optional<NonMemberUserModel> optionalUser = Optional.empty();
        if (email != null && !email.isBlank()) {
            optionalUser = nonMemberUserRepositry.findByEmail(email);
        } else if (mobile != null && !mobile.isBlank()) {
            optionalUser = nonMemberUserRepositry.findByMobile(mobile);
        }

        return optionalUser.map(user -> {
            user.setPassword(passwordEncoder.encode(request.newPassword()));

            LoginAttemptTracker.resetFailedAttempts(user);
            LOGGER.info("Account unlocked for non-member {} after password reset",
                    email != null ? email : mobile);

            nonMemberUserRepositry.save(user);
            otpService.cleanupOtp(request.otpRefId());
            return CommonResponse.success(
                    Constants.PASSWORD_RESET_SUCCESS_NONMEMBER

            );
        }).orElse(CommonResponse.error(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                        Constants.USER_NOT_FOUND + (email != null ? email : mobile)),
                HttpStatus.NOT_FOUND.value()
        ));
    }

    @Override
    public CommonResponse loginMember(LoginMemberRequest request) {
        LOGGER.info(Constants.LOG_PROCESSING_LOGIN_MEMBER, request.userId());

        // Case 1: Check if account is locked due to OTP failures (existing OTP service lock)
        CommonResponse otpLockCheck = otpService.ifUserLocked(request.userId());
        if (otpLockCheck != null) {
            LOGGER.warn(Constants.LOG_OTP_LOCK_ACTIVE, request.userId());
            return otpLockCheck;
        }

        // Case 2: Validate user exists
        UserModel user = userRepository.findByEmailOrMobile(request.userId(), request.userId())
                .orElseThrow(() -> new CustomException(CustomStatusCode.USER_NOT_FOUND.getCode(),
                        Constants.EMAIL_NOT_REGISTERED_NON_MEMBER
                ));

        // Case 3: Check if account is locked due to failed password login attempts (new password lock)
        if (LoginAttemptTracker.isAccountLocked(user)) {
            long remainingMinutes = LoginAttemptTracker.getRemainingLockTimeMinutes(user);
            userRepository.save(user); // Constants.COMMENT_SAVE_TO_RESET_LOCK
            if (remainingMinutes > 0) {
                LOGGER.warn(Constants.LOG_PASSWORD_LOCK_ACTIVE,
                        request.userId(), remainingMinutes);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.LOCKED.value(),
                                String.format(Constants.ACCOUNT_LOCKED_MESSAGE_WITH_TIME, remainingMinutes)),
                        HttpStatus.LOCKED.value()
                );
            }
        }

        // Case 4: Validate password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            LOGGER.warn(Constants.LOG_INVALID_PASSWORD_MEMBER, request.userId());

            // Record failed attempt
            LoginAttemptTracker.recordFailedAttempt(user);
            userRepository.save(user);

            // Check if account is now locked
            if (LoginAttemptTracker.isAccountLocked(user)) {
                long lockMinutes = LoginAttemptTracker.getRemainingLockTimeMinutes(user);
                LOGGER.error(Constants.LOG_ACCOUNT_LOCKED,
                        request.userId(), user.getFailedLoginAttempts());
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.LOCKED.value(),
                                String.format(Constants.ACCOUNT_LOCKED_PASSWORD_ATTEMPTS_WITH_COUNT,
                                        user.getFailedLoginAttempts(), lockMinutes)
                        ),
                        HttpStatus.LOCKED.value()
                );
            }
            throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                    CustomStatusCode.INVALID_CREDENTIALS.getMessage()
            );
        }

        // Case 5: Successful password validation - reset failed attempts
        LoginAttemptTracker.resetFailedAttempts(user);
        userRepository.save(user);
        LOGGER.info(Constants.LOG_PASSWORD_VALIDATED, request.userId());

        // Generate JWT token after successful password validation using deviceId
        String deviceId = RequestContext.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            LOGGER.error("DeviceId is missing from request header");
            throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                    "DeviceId header is required");
        }
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);

        String userIdentifier = user.getEmail() != null ? user.getEmail() : user.getMobile();
        String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
        String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier, memberCode);
        LOGGER.info("JWT token generated for member user with deviceId: {}, username: {}, memberCode: {}", deviceId, userIdentifier, memberCode);

        // Case 6: Handle device trust & OTP flow
        return handleDeviceTrust(user, accessToken);
    }


    private CommonResponse handleDeviceTrust(UserModel user, String accessToken) {
        Long userId = user.getId();
        String deviceId = RequestContext.getDeviceId();

        Optional<TrustDeviceEntity> trustedDeviceOpt =
                trustedDeviceRepository.findByUserIdAndUserType(userId, "MEMBER");

        if (trustedDeviceOpt.isPresent()
                && deviceId.equals(trustedDeviceOpt.get().getDeviceId())
                && "Y".equalsIgnoreCase(trustedDeviceOpt.get().getStatus())) {


            if (deviceId == null || deviceId.isEmpty()) {
                LOGGER.error("DeviceId is missing from request header");
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            String memberCode = user.getMemberCode() == null ? "" : user.getMemberCode();
            Integer sex = user.getSex() == null ? 2 : user.getSex();
            String birthDate = user.getBirthDate() == null ? "" : user.getBirthDate().toString();
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            LOGGER.info("Login successful for user {} from trusted device {}", userId, deviceId);

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = userRepository.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            return CommonResponse.success(Map.of(
                    "message", "Login Successful",
                    "accessToken", accessToken
            ));
        }
        return initiateOtpFlow(user, deviceId);
    }


    private CommonResponse initiateOtpFlow(UserModel user, String deviceId) {
        LOGGER.info("Untrusted device {} for user {}. Initiating OTP flow.", deviceId, user);

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("userId", user.getId().toString());
        otpData.put("userType", "MEMBER");
        otpData.put("deviceId", deviceId);
        otpData.put("email", user.getEmail());
        otpData.put("mobile", user.getMobile());
        if (user.getMemberCode() != null) {
            otpData.put("memberCode", user.getMemberCode());
        }

        CommonResponse otpResponse = otpService.generateLoginOtp(
                user.getEmail(), user.getEmail(), user.getMobile(), otpData
        );

        Object otpResultData = otpResponse.getData();
        LOGGER.info("OTP Response Data in LOGIN_API: {}", otpResultData);

        String memberCode = user.getMemberCode() == null ? "" : user.getMemberCode();
        Integer sex = user.getSex() == null ? 2 : user.getSex();
        String birthDate = user.getBirthDate() == null ? "" : user.getBirthDate().toString();
        String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
        String email = user.getEmail() != null ? user.getEmail() : "";
        String mobile = user.getMobile() != null ? user.getMobile() : "";


        boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
        if (isConsented) {
            consentService.revalidateUserConsentFlag(user.getId().intValue());
            user = userRepository.findById(user.getId()).orElse(user);
            isConsented = user.getIsConsented() != null && user.getIsConsented();
        }

        if (otpResultData instanceof Map<?, ?> map) {
            LOGGER.info("OTP Response Data is a valid Map");
            String otpRefId = (String) map.get("otpRefId");
            return CommonResponse.success(Map.of(
                    "message", "Device not trusted, please verify using OTP.",
                    "otpRefId", otpRefId
            ));
        }

        if (otpResultData instanceof ErrorResponse error) {
            return CommonResponse.error(error, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        LOGGER.error("Unexpected OTP response: {}", otpResultData);
        return CommonResponse.error(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected Login response"),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }


    @Override
    public CommonResponse verifyLoginOtpAndTrustDevice(VerifyOtpRequest verifyRequest) {
        LOGGER.info("Starting OTP + Trust Device flow for otpRefId={}, deviceId={}",
                verifyRequest.otpRefId(), RequestContext.getDeviceId()
        );

        // Step 1: Verify OTP
        CommonResponse verifyResponse = otpService.verifyOtp(verifyRequest);
        if (!verifyResponse.isSuccess()) {
            LOGGER.info("OTP verification failed for otpRefId={} : {}",
                    verifyRequest.otpRefId(), verifyResponse.getData()
            );
            return verifyResponse;
        }

        if (!verifyRequest.flowType().equals("LOGIN")) {
            return verifyResponse;
        }

        LOGGER.info("OTP verification successful for otpRefId={}", verifyRequest.otpRefId());

        // Step 2: Extract OTP verification data
        OtpResponse otpDataFromVerification = (OtpResponse) verifyResponse.getData();
        String newOtpRefId = otpDataFromVerification.otpRefId();
        LOGGER.info("New OTP refId={} generated after verification", newOtpRefId);

        // Step 3: Fetch user data by new refId
        Map<String, Object> userData = otpService.getUserDataByRefId(newOtpRefId);
        if (userData.isEmpty()) {
            LOGGER.error("No user data found for otpRefId={}", newOtpRefId);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "OTP session data not found or expired. Please try logging in again."
                    ),
                    HttpStatus.BAD_REQUEST.value()
            );
        }
        LOGGER.info("User data retrieved for otpRefId={}: {}", newOtpRefId, userData);

        // Step 4: Extract user details
        Object userIdObj = userData.get("userId");
        Long userId = null;

        if (userIdObj == null) {
            LOGGER.error("userId is missing from userData for otpRefId={}. userData keys: {}",
                    newOtpRefId, userData.keySet());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "User ID not found in OTP session. Please try logging in again."
                    ),
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                userId = Long.valueOf((String) userIdObj);
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid userId format in userData for otpRefId={}. userId: {}",
                        newOtpRefId, userIdObj);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                                "Invalid user ID format. Please try logging in again."
                        ),
                        HttpStatus.BAD_REQUEST.value()
                );
            }
        } else {
            LOGGER.error("Unexpected type for userId in userData for otpRefId={}. Type: {}, Value: {}",
                    newOtpRefId, userIdObj != null ? userIdObj.getClass().getName() : "null", userIdObj);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                            "Invalid user ID type. Please try logging in again."
                    ),
                    HttpStatus.BAD_REQUEST.value()
            );
        }
        String userType = (String) userData.get("userType");

        String deviceId = RequestContext.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            LOGGER.error("DeviceId is missing from request header");
            throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                    "DeviceId header is required");
        }

        // Step 5: Trust Device handling
        TrustDeviceEntity trustedDevice = trustedDeviceRepository
                .findByUserIdAndUserType(userId, userType)
                .orElse(TrustDeviceEntity.builder().userId(userId).userType(userType).build());

        if (trustedDevice.getId() == null) {
            LOGGER.info("No existing trusted device found for userId={}, userType={}. Creating new entry.",
                    userId, userType
            );
        } else {
            LOGGER.info("Updating existing trusted device (id={}) for userId={}, userType={}",
                    trustedDevice.getId(), userId, userType
            );
        }

        trustedDevice.setDeviceId(deviceId);
        trustedDevice.setStatus("Y");
        trustedDeviceRepository.save(trustedDevice);
        LOGGER.info("Device={} successfully trusted for userId={}, userType={}",
                deviceId, userId, userType
        );

        // Step 6: Cleanup old OTP session
        otpService.cleanupOtp(verifyRequest.otpRefId());
        LOGGER.info("Old OTP session {} cleaned up", verifyRequest.otpRefId());

        // Step 7: Fetch user details to get username and memberCode for token generation
        String userIdentifier = null;
        String memberCode = null;
        if ("MEMBER".equals(userType)) {
            Optional<UserModel> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                LOGGER.error("Member user not found for userId={}", userId);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found."),
                        HttpStatus.NOT_FOUND.value()
                );
            }
            UserModel user = userOpt.get();
            userIdentifier = user.getEmail() != null ? user.getEmail() : user.getMobile();
            memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
        } else if ("NON_MEMBER".equals(userType)) {
            Optional<NonMemberUserModel> userOpt = nonMemberUserRepositry.findById(userId);
            if (userOpt.isEmpty()) {
                LOGGER.error("Non-member user not found for userId={}", userId);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found."),
                        HttpStatus.NOT_FOUND.value()
                );
            }
            NonMemberUserModel user = userOpt.get();
            userIdentifier = user.getEmail() != null ? user.getEmail() : user.getMobile();
            memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
        }

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
        String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier, memberCode != null ? memberCode : "");
        LOGGER.info("JWT token generated for user with deviceId: {}, username: {}, memberCode: {} after successful device trust",
                deviceId, userIdentifier, memberCode);

        // Step 8: Fetch user details for response based on userType
        if ("MEMBER".equals(userType)) {
            Optional<UserModel> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                LOGGER.error("Member user not found for userId={}", userId);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found."),
                        HttpStatus.NOT_FOUND.value()
                );
            }

            UserModel user = userOpt.get();
            memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            Integer sex = user.getSex() == null ? 2 : user.getSex();
            String birthDate = user.getBirthDate() == null ? "" : user.getBirthDate().toString();
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = userRepository.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }

            // Step 8: Response with accessToken generated after successful device trust
            return CommonResponse.success(Map.of(
                    "message", "OTP verified and device trusted successfully",
                    "accessToken", accessToken
            ));
        } else if ("NON_MEMBER".equals(userType)) {
            Optional<NonMemberUserModel> userOpt = nonMemberUserRepositry.findById(userId);
            if (userOpt.isEmpty()) {
                LOGGER.error("Non-member user not found for userId={}", userId);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found."),
                        HttpStatus.NOT_FOUND.value()
                );
            }

            NonMemberUserModel user = userOpt.get();
            memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String nonMemberCode = user.getNonMemberCode() != null ? user.getNonMemberCode() : "";
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = nonMemberUserRepositry.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            // Step 8: Response with accessToken generated after successful device trust
            return CommonResponse.success(Map.of(
                    "message", "OTP verified and device trusted successfully",
                    "accessToken", accessToken
            ));
        } else {
            LOGGER.error("Unknown userType: {} for userId={}", userType, userId);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid user type."),
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }


    @Override
    public CommonResponse loginNonMember(LoginMemberRequest request) {
        LOGGER.info(Constants.LOG_PROCESSING_LOGIN, request.userId());

        // Case 1: Check if account is locked due to OTP failures (existing OTP service lock)
        CommonResponse otpLockCheck = otpService.ifUserLocked(request.userId());
        if (otpLockCheck != null) {
            LOGGER.warn("Login blocked for non-member: {} - OTP service lock active", request.userId());
            return otpLockCheck;
        }

        // Case 2: Validate user exists
        NonMemberUserModel user = nonMemberUserRepositry.findByEmailOrMobile(request.userId(), request.userId())
                .orElse(null);

        if (user == null) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                            Constants.EMAIL_NOT_REGISTERED_NON_MEMBER
                    ),
                    HttpStatus.NOT_FOUND.value()
            );
        }
        // Case 3: Check if account is locked due to failed password login attempts (new password lock)
        if (LoginAttemptTracker.isAccountLocked(user)) {
            long remainingMinutes = LoginAttemptTracker.getRemainingLockTimeMinutes(user);
            nonMemberUserRepositry.save(user); // Save to reset if lock expired

            if (remainingMinutes > 0) {
                LOGGER.warn("Login attempt for locked non-member account: {}, remaining lock time: {} minutes",
                        request.userId(), remainingMinutes);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.LOCKED.value(),
                                String.format(Constants.ACCOUNT_LOCKED_MESSAGE_WITH_TIME, remainingMinutes)
                        ),
                        HttpStatus.LOCKED.value()
                );
            }
        }
// Case 4: Validate password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            LOGGER.warn(Constants.LOG_INVALID_PASSWORD, request.userId());

            // Record failed attempt
            LoginAttemptTracker.recordFailedAttempt(user);
            nonMemberUserRepositry.save(user);

            // Check if account is now locked
            if (LoginAttemptTracker.isAccountLocked(user)) {
                long lockMinutes = LoginAttemptTracker.getRemainingLockTimeMinutes(user);
                LOGGER.error("Account locked for non-member: {} due to {} failed attempts",
                        request.userId(), user.getFailedLoginAttempts());
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.LOCKED.value(),
                                String.format(Constants.ACCOUNT_LOCKED_MESSAGE_WITH_TIME, lockMinutes)
                        ),
                        HttpStatus.LOCKED.value()
                );
            }

            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                            CustomStatusCode.INVALID_CREDENTIALS.getMessage()
                    ),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        // Successful password validation - reset failed attempts
        LoginAttemptTracker.resetFailedAttempts(user);
        nonMemberUserRepositry.save(user);
        LOGGER.info("Password validated successfully for non-member: {}", request.userId());

        if (!Constants.ACTIVE.equals(user.getStatus().name())) {
            LOGGER.info(Constants.LOG_INACTIVE_USER, request.userId());
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                            CustomStatusCode.INACTIVE_USER.getMessage()
                    ),

                    HttpStatus.FORBIDDEN.value()
            );
        }

        // Generate JWT token after successful password validation using deviceId
        String deviceId = RequestContext.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            LOGGER.error("DeviceId is missing from request header");
            throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                    "DeviceId header is required");
        }
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
        String userIdentifier = user.getEmail() != null ? user.getEmail() : user.getMobile();
        String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier);
        LOGGER.info("JWT token generated for non-member user with deviceId: {}, username: {}", deviceId, userIdentifier);

        Long userId = user.getId();

        Optional<TrustDeviceEntity> trustedDeviceOpt =
                trustedDeviceRepository.findByUserIdAndUserType(userId, "NON_MEMBER");

        if (trustedDeviceOpt.isPresent() && deviceId.equals(trustedDeviceOpt.get().getDeviceId())
                && "Y".equals(trustedDeviceOpt.get().getStatus())) {

            if (deviceId == null || deviceId.isEmpty()) {
                LOGGER.error("DeviceId is missing from request header");
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            LOGGER.info("JWT token generated for non-member user with deviceId: {}", deviceId);

            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String nonMemberCode = user.getNonMemberCode() != null ? user.getNonMemberCode() : "";
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = nonMemberUserRepositry.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            return CommonResponse.success(Map.of(
                    "message", "Login Successful",
                    "accessToken", accessToken
            ));
        } else {
            LOGGER.info("Untrusted device {} for non-member {}. Initiating OTP flow.", deviceId, userId);

            Map<String, Object> otpData = new HashMap<>();
            otpData.put("userId", userId != null ? userId.toString() : null);
            otpData.put("userType", "NON_MEMBER");
            otpData.put("deviceId", deviceId);
            otpData.put("email", user.getEmail());
            otpData.put("mobile", user.getMobile());


            CommonResponse otpResponse = otpService.generateLoginOtp(
                    user.getEmail() != null ? user.getEmail() : user.getMobile(),
                    user.getEmail(),
                    user.getMobile(),
                    otpData
            );

            Map<String, Object> otpResultData = (Map<String, Object>) otpResponse.getData();
            String otpRefId = (String) otpResultData.get("otpRefId");

            return CommonResponse.success(
                    Map.of(
                            "message", "Device not trusted, please verify using OTP.",
                            "otpRefId", otpRefId
                    )
            );
        }
    }

    @Override
    public CommonResponse registerNonMember(NonMemberRegistrationRequest request) {
        if (!StringUtils.hasText(request.email())) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.INVALID_CREDENTIALS
                    ),
                    400
            );
        }

        throwIfConflictedRegistration(request.email(), request.mobileNumber());

        Map<String, Object> userData = new HashMap<>();
        if (request.email() != null) userData.put("email", request.email());
        if (request.mobileNumber() != null) userData.put("mobile", request.mobileNumber());

        return otpService.generateOtp(
                request.email() != null ? request.email() : request.mobileNumber(),
                request.email(),
                request.mobileNumber(),
                userData
        );
    }


    @Override
    public CommonResponse registerMember(MemberRegistrationRequest request) {
        if (!StringUtils.hasText(request.email()) && !StringUtils.hasText(request.mobileNumber())) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.EMAIL_OR_MOBILE_REQUIRED
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        throwIfConflictedRegistration(request.email(), request.mobileNumber());

        Optional<MembershipModel> existingUser = membershipModelRepository.findByMemberCode(request.memberCode());
        if (existingUser.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.INVALID_MEMBER_DETAILS
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        LocalDate dob = existingUser.get().getBirthDate().toLocalDate();
        String firstName = existingUser.get().getFirstName();
        String lastName = existingUser.get().getLastName();
        String middleName = existingUser.get().getMiddleName();
        Integer sex = existingUser.get().getSex();

        Map<String, Object> userData = new HashMap<>();
        userData.put("memberCode", request.memberCode());
        if (dob != null) userData.put("birthDate", dob.toString()); // store birthDate from DB
        if (request.email() != null) userData.put("email", request.email());
        if (request.mobileNumber() != null) userData.put("mobile", request.mobileNumber());
        if (firstName != null) userData.put("firstName", firstName);
        if (lastName != null) userData.put("lastName", lastName);
        if (middleName != null) userData.put("middleName", middleName);
        if (sex != null) userData.put("sex", sex); // store sex as Integer from membership table

        String otpIdentifier = request.email() != null ? request.email() : request.mobileNumber();
        return otpService.generateOtp(otpIdentifier, request.email(), request.mobileNumber(), userData);
    }

    @Override
    public CommonResponse validateMemberRegistration(MemberValidationRequest memRegister) {
        LOGGER.info("API INSIDE CALL");

        // check If user already exists
        Optional<UserModel> existingUser = userRepository.findByMemberCode(memRegister.memberCode());
        if (existingUser.isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT.value(),
                    Constants.USER_ALREADY_EXISTS + memRegister.memberCode());
        }

        // Fetch membership record
        MembershipModel member = membershipModelRepository.findByMemberCode(memRegister.memberCode())
                .orElse(null);
        if (member == null) {
            throw new CustomException(CustomStatusCode.MEMBER_NOT_FOUND.getCode(),
                    CustomStatusCode.MEMBER_NOT_FOUND.getMessage());
        }

        //Compare birthdates
        if (!member.getBirthDate().toLocalDate().equals(memRegister.birthDate())) {

            // Fetch existing invalid birthdate record or create a new one
            InvalidBirthdateLogModel record = invalidBirthdateLogRepository
                    .findByMemberCode(memRegister.memberCode())
                    .orElseGet(() -> {
                        InvalidBirthdateLogModel newRecord = new InvalidBirthdateLogModel();
                        newRecord.setMemberCode(memRegister.memberCode());
                        newRecord.setBirthDate(memRegister.birthDate());
                        newRecord.setWrongCount(0);
                        return newRecord;
                    });

            // Increment wrong attempt count
            record.setWrongCount(record.getWrongCount() + 1);
            invalidBirthdateLogRepository.save(record);

            int attempt = record.getWrongCount();

            // switch expression for Wrong ATTEMPTS
            return switch (attempt) {
                case 1 -> CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), Constants.MEMBER_WRONG_BIRTHDATE_1),
                        HttpStatus.NOT_FOUND.value());

                case 2, 3 -> CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), Constants.MEMBER_WRONG_BIRTHDATE_2),
                        HttpStatus.NOT_FOUND.value());

                default -> CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), Constants.MEMBER_WRONG_BIRTHDATE_MORE_THEN_THREEATTEMPTS),
                        HttpStatus.NOT_FOUND.value());
            };
        } else {
            //If BIRTH_DATE is correct, reset wrongCount to 0
            invalidBirthdateLogRepository.findByMemberCode(memRegister.memberCode())
                    .ifPresent(record -> {
                        record.setWrongCount(0);
                        invalidBirthdateLogRepository.save(record);
                        LOGGER.info("Wrong attempt count reset to 0 for memberCode: {}", memRegister.memberCode());
                    });
        }

        //Validate membership status
        if (Constants.RESIGN.equals(member.getMemStatus()) || Constants.INACTIVE.equals(member.getMemStatus())) {
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.CONFLICT.value(), Constants.MEMBERSHIP_INACTIVE),
                    HttpStatus.CONFLICT.value());
        }

        //If all validations pass → success
        // Mark any active OTP sessions for this member as registration-validated so the registration OTP can be used for setPassword.
        otpService.markRegistrationValidatedForMember(memRegister.memberCode());

        return CommonResponse.successWithMemberCode(
                memRegister.memberCode(),
                Constants.MEMBER_VALIDATED
        );
    }


    @Override
    public CommonResponse requestMemberOtp(MemberOtpRequest request) {
        Optional<UserModel> userOpt = userRepository.findByMemberCodeAndBirthDate(request.memberCode(),
                request.birthDate()
        );
        if (userOpt.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.INVALID_MEMBER_DETAILS
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        UserModel user = userOpt.get();
        String email = user.getEmail();
        String mobile = user.getMobile();

        Map<String, Object> userData = new HashMap<>();
        userData.put("memberCode", request.memberCode());
        userData.put("userType", "MEMBER");
        userData.put("userId", email);
        userData.put("deviceId", RequestContext.getDeviceId());
        userData.put("email", email);
        userData.put("mobile", mobile);


        // Delegate OTP generation to OtpService (PASSWORD_RESET flow)
        CommonResponse otpResponse = otpService.generatePasswordResetOtp(
                email != null ? email : mobile, // identifier
                email,
                mobile, userData
        );

        // Add email and mobile to response for member OTP requests
        if (otpResponse.getData() instanceof Map<?, ?> responseData) {

            Map<String, Object> dataMap = (Map<String, Object>) responseData;
            Map<String, Object> otpResponseData = new HashMap<>(dataMap);
//            if (email != null && !email.isBlank()) {
//                otpResponseData.put("email", email);
//            }
//            if (mobile != null && !mobile.isBlank()) {
//                otpResponseData.put("mobile", mobile);
//            }
            return CommonResponse.success(otpResponseData);
        }

        return otpResponse;
    }

    @Override
    public CommonResponse requestNonMemberOtp(NonMemberOtpRequest request) {

        String email = request.email();
        String mobile = request.mobile();

        if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.EMAIL_OR_MOBILE_REQUIRED
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Optional<NonMemberUserModel> userOpt;
        if (email != null && !email.isBlank()) {
            userOpt = nonMemberUserRepositry.findByEmail(email);
        } else {
            userOpt = nonMemberUserRepositry.findByMobile(mobile);
        }

        if (userOpt.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.INVALID_NONMEMBER_DETAILS
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        NonMemberUserModel user = userOpt.get();
        Map<String, Object> userData = new HashMap<>();
        userData.put("userType", "MEMBER");
        userData.put("deviceId", RequestContext.getDeviceId());

        if (request.email() != null && !request.email().isBlank()) {
            userData.put("email", request.email());
            userData.put("userId", userOpt.get().getId());

        }
        if (request.mobile() != null && !request.mobile().isBlank()) {
            userData.put("mobile", request.mobile());
            userData.put("userId", userOpt.get().getId());
        }

        // Delegate to OtpService (PASSWORD_RESET flow)
        return otpService.generatePasswordResetOtp(
                email != null ? email : mobile, // identifier
                email,
                mobile, userData
        );
    }

    @Override
    public CommonResponse requestPasswordResetMember(MemberOtpRequest request) {
        // Reuse member lookup but generate a PASSWORD_RESET OTP
        Optional<UserModel> userOpt = userRepository.findByMemberCodeAndBirthDate(request.memberCode(),
                request.birthDate()
        );
        if (userOpt.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.INVALID_MEMBER_DETAILS
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        UserModel user = userOpt.get();
        String email = user.getEmail();
        String mobile = user.getMobile();

        Map<String, Object> userData = new HashMap<>();
        userData.put("memberCode", request.memberCode());
        userData.put("userType", "MEMBER");
        userData.put("userId", email);
        userData.put("deviceId", RequestContext.getDeviceId());
        userData.put("email", email);
        userData.put("mobile", mobile);

        return otpService.generatePasswordResetOtp(
                email != null ? email : mobile, // identifier
                email,
                mobile, userData
        );
    }

    @Override
    public CommonResponse requestPasswordResetNonMember(NonMemberOtpRequest request) {
        String email = request.email();
        String mobile = request.mobile();

        if ((email == null || email.isBlank()) && (mobile == null || mobile.isBlank())) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.EMAIL_OR_MOBILE_REQUIRED
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        Optional<NonMemberUserModel> userOpt;
        if (email != null && !email.isBlank()) {
            userOpt = nonMemberUserRepositry.findByEmail(email);
        } else {
            userOpt = nonMemberUserRepositry.findByMobile(mobile);
        }

        if (userOpt.isEmpty()) {
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                            Constants.INVALID_NONMEMBER_DETAILS
                    ),

                    HttpStatus.BAD_REQUEST.value()
            );
        }

        NonMemberUserModel user = userOpt.get();
        Map<String, Object> userData = new HashMap<>();
        userData.put("userType", "NON_MEMBER");
        userData.put("deviceId", RequestContext.getDeviceId());
        if (email != null && !email.isBlank()) {
            userData.put("email", email);
            userData.put("userId", user.getId());
        }
        if (mobile != null && !mobile.isBlank()) {
            userData.put("mobile", mobile);
            userData.put("userId", user.getId());
        }

        return otpService.generatePasswordResetOtp(
                email != null ? email : mobile, // identifier
                email,
                mobile, userData
        );
    }

    private void throwIfConflictedRegistration(String email, String mobile) {
        if (userRepository.existsByEmailAndOptionalMobile(email, mobile)) {
            throw new CustomException(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                    Constants.USER_ALREADY_REGISTERED_MEMBER
            );
        }

        if (nonMemberUserRepositry.existsByEmailAndOptionalMobile(email, mobile)) {
            throw new CustomException(CustomStatusCode.OTP_GENERATION_FAILED.getCode(),
                    Constants.USER_ALREADY_REGISTERED
            );
        }
    }

    private void sendWelcomeEmail(String email) {
        try {
            String body = String.format(Constants.EMAIL_BODY_WELCOME_TEMPLATE, Constants.SURVEY_LINK_PLACEHOLDER);

            var emailRequest = new EmailRequest(
                    Constants.EMAIL_SUBJECT_WELCOME,                    // subject
                    body,                                               // body
                    Collections.singletonList(email),                  // toEmails
                    Constants.EMAIL_TYPE_WELCOME,                       // type
                    null,                                               // ccEmails
                    null,                                               // bccEmails
                    Constants.EMAIL_CONTENT_TYPE,                      // contentType
                    null                                                // attachments
            );
            LOGGER.info("Sending welcome email to {}", email);
            notificationServiceClient.sendEmail(emailRequest);
        } catch (Exception e) {
            LOGGER.error("Failed to send welcome email to {}", email, e);
        }
    }

    @Override
    public CommonResponse checkBiometric(BiometricCheckRequest request) {
        return verifyBiometricForCheck(request);
    }

    private CommonResponse verifyBiometricForCheck(BiometricCheckRequest request) {

        // Try to find user in MEMBER table first
        Optional<UserModel> memberUserOpt = userRepository.findByEmailOrMobile(request.userId(), request.userId());
        if (memberUserOpt.isPresent()) {
            UserModel user = memberUserOpt.get();

            // Verify passkey
            if (user.getPasskey() == null || !user.getPasskey().equals(request.passkey())) {
                LOGGER.warn("Biometric verification failed - Invalid passkey for user: {}", request.userId());
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                                "Invalid biometric passkey"
                        ),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Check if user is active
            if (!Constants.ACTIVE.equals(user.getStatus().name())) {
                LOGGER.warn("Biometric verification failed - Inactive user: {}", request.userId());
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                                CustomStatusCode.INACTIVE_USER.getMessage()
                        ),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // Generate JWT token
            String deviceId = RequestContext.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                LOGGER.error("DeviceId is missing from request header");
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);

            String userIdentifier = request.userId();
            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier, memberCode);

            Integer sex = user.getSex() == null ? 2 : user.getSex();
            String birthDate = user.getBirthDate() == null ? "" : user.getBirthDate().toString();

            LOGGER.info("Biometric verification successful for member user: {}", request.userId());
            return CommonResponse.success(Map.of(
                    "message", "Login Successful",
                    "memberCode", memberCode,
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "sex", sex,
                    "birthDate", birthDate,
                    "accessToken", accessToken
            ));
        }

        // Try to find user in NON_MEMBER table
        Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepositry.findByEmailOrMobile(request.userId(), request.userId());

        if (nonMemberUserOpt.isPresent()) {
            NonMemberUserModel user = nonMemberUserOpt.get();

            // Verify passkey
            if (user.getPasskey() == null || !user.getPasskey().equals(request.passkey())) {
                LOGGER.warn("Biometric verification failed - Invalid passkey for non-member user: {}", request.userId());
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                                "Invalid biometric passkey"
                        ),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Check if user is active
            if (!Constants.ACTIVE.equals(user.getStatus().name())) {
                LOGGER.warn("Biometric verification failed - Inactive non-member user: {}", request.userId());
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                                CustomStatusCode.INACTIVE_USER.getMessage()
                        ),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // Generate JWT token
            String deviceId = RequestContext.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                LOGGER.error("DeviceId is missing from request header");
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
            String userIdentifier = user.getEmail() != null ? user.getEmail() :
                    (user.getMobile() != null ? user.getMobile() : request.userId());
            String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier);

            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String nonMemberCode = user.getNonMemberCode() != null ? user.getNonMemberCode() : "";
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = nonMemberUserRepositry.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            return CommonResponse.success(Map.of(
                    "message", "Login Successful",
                    "memberCode", memberCode,
                    "nonMemberCode", nonMemberCode,
                    "isWhitelisted", isWhitelisted,
                    "email", email,
                    "mobile", mobile,
                    "accessToken", accessToken,
                    "isConsented", isConsented
            ));
        }

        return CommonResponse.error(
                new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                        CustomStatusCode.USER_NOT_FOUND.getMessage()
                ),
                HttpStatus.NOT_FOUND.value()
        );
    }

    @Override
    public CommonResponse registerBio(RegisterBioRequest request) {
        LOGGER.info("Processing biometric registration for userId: {}", request.userId());

        // Try to find user in MEMBER table first
        Optional<UserModel> memberUserOpt = userRepository.findByEmailOrMobile(request.userId(), request.userId());

        if (memberUserOpt.isPresent()) {
            UserModel user = memberUserOpt.get();
            user.setPasskey(request.passkey());
            user.setBiometricEnabled(true);
            userRepository.save(user);
            LOGGER.info("Biometric passkey registered for member user: {}", request.userId());
            return CommonResponse.success("Biometric passkey registered successfully");
        }

        // Try to find user in NON_MEMBER table
        Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepositry.findByEmailOrMobile(request.userId(), request.userId());

        if (nonMemberUserOpt.isPresent()) {
            NonMemberUserModel user = nonMemberUserOpt.get();
            user.setPasskey(request.passkey());
            nonMemberUserRepositry.save(user);
            LOGGER.info("Biometric passkey registered for non-member user: {}", request.userId());
            return CommonResponse.success("Biometric passkey registered successfully");
        }

        // User not found in either table
        LOGGER.warn("Biometric registration failed - User not found: {}", request.userId());
        return CommonResponse.error(
                new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                        CustomStatusCode.USER_NOT_FOUND.getMessage()
                ),
                HttpStatus.NOT_FOUND.value()
        );
    }

    @Override
    public CommonResponse verifyBiometric(BiometricVerifyRequest request) {
        LOGGER.info("Processing biometric verification for userId: {}", request.userId());

        // Try to find user in MEMBER table first
        Optional<UserModel> memberUserOpt = userRepository.findByEmailOrMobile(request.userId(), request.userId());

        if (memberUserOpt.isPresent()) {
            UserModel user = memberUserOpt.get();

            // Verify passkey
            if (user.getPasskey() == null || !user.getPasskey().equals(request.passkey())) {
                LOGGER.warn("Biometric verification failed - Invalid passkey for user: {}", request.userId());
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                                "Invalid biometric passkey"
                        ),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Check if user is active
            if (!Constants.ACTIVE.equals(user.getStatus().name())) {
                LOGGER.warn("Biometric verification failed - Inactive user: {}", request.userId());
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                                CustomStatusCode.INACTIVE_USER.getMessage()
                        ),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // Generate JWT token
            String deviceId = RequestContext.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                LOGGER.error("DeviceId is missing from request header");
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
            String userIdentifier = request.userId(); // userId is email/mobile
            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier, memberCode);
            Integer sex = user.getSex() == null ? 2 : user.getSex();
            String birthDate = user.getBirthDate() == null ? "" : user.getBirthDate().toString();

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = userRepository.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }

            return CommonResponse.success(Map.of(
                    "message", "Login Successful",
                    "memberCode", memberCode,
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "sex", sex,
                    "birthDate", birthDate,
                    "accessToken", accessToken,
                    "isConsented", isConsented
            ));
        }

        Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepositry.findByEmailOrMobile(request.userId(), request.userId());

        if (nonMemberUserOpt.isPresent()) {
            NonMemberUserModel user = nonMemberUserOpt.get();

            // Verify passkey
            if (user.getPasskey() == null || !user.getPasskey().equals(request.passkey())) {
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                                "Invalid biometric passkey"
                        ),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Check if user is active
            if (!Constants.ACTIVE.equals(user.getStatus().name())) {
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                                CustomStatusCode.INACTIVE_USER.getMessage()
                        ),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // Generate JWT token
            String deviceId = RequestContext.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
            String accessToken = jwtTokenUtil.generateToken(userDetails);

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = nonMemberUserRepositry.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }

            LOGGER.info("Biometric verification successful for non-member user: {}", request.userId());
            return CommonResponse.success(Map.of(
                    "message", "Login Successful",
                    "accessToken", accessToken,
                    "isConsented", isConsented
            ));
        }

        LOGGER.warn("Biometric verification failed - User not found: {}", request.userId());
        return CommonResponse.error(
                new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                        CustomStatusCode.USER_NOT_FOUND.getMessage()
                ),
                HttpStatus.NOT_FOUND.value()
        );
    }

    @Override
    public CommonResponse generateBiometricChallenge(BiometricChallengeRequest request) {
        LOGGER.info("Generating biometric challenge for email: {}", request.email());

        Optional<UserModel> memberUserOpt = userRepository.findByEmailOrMobile(request.email(), request.email());
        Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepositry.findByEmailOrMobile(request.email(), request.email());

        if (memberUserOpt.isEmpty() && nonMemberUserOpt.isEmpty()) {
            LOGGER.warn("Challenge generation failed - User not found: {}", request.email());
            return CommonResponse.error(
                    new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                            CustomStatusCode.USER_NOT_FOUND.getMessage()),
                    HttpStatus.NOT_FOUND.value()
            );
        }

        boolean biometricEnabled = false;
        if (memberUserOpt.isPresent()) {
            biometricEnabled = Boolean.TRUE.equals(memberUserOpt.get().getBiometricEnabled());
        } else if (nonMemberUserOpt.isPresent()) {
            biometricEnabled = nonMemberUserOpt.get().getPasskey() != null && !nonMemberUserOpt.get().getPasskey().isEmpty();
        }

        if (!biometricEnabled) {
            LOGGER.warn("Challenge generation failed - Biometric not enabled for user: {}", request.email());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.FORBIDDEN.value(),
                            "Biometric authentication is not enabled for this user"),
                    HttpStatus.FORBIDDEN.value()
            );
        }

        byte[] challengeBytes = new byte[32];
        RANDOM.nextBytes(challengeBytes);
        String challenge = Base64.getEncoder().encodeToString(challengeBytes);

        String challengeKey = CHALLENGE_PREFIX + request.email();
        redisTemplate.opsForValue().set(challengeKey, challenge, Duration.ofSeconds(CHALLENGE_EXPIRY_SECONDS));

        LOGGER.info("Biometric challenge generated for email: {}, expires in {} seconds", request.email(), CHALLENGE_EXPIRY_SECONDS);
        return CommonResponse.success(Map.of(
                "challenge", challenge,
                "expiresIn", CHALLENGE_EXPIRY_SECONDS // seconds
        ));
    }

    @Override
    public CommonResponse biometricLogin(BiometricLoginRequest request) {
        LOGGER.info("Processing biometric login for email: {}", request.email());

        // Retrieve challenge from Redis
        String challengeKey = CHALLENGE_PREFIX + request.email();
        String storedChallenge = redisTemplate.opsForValue().get(challengeKey);

        if (storedChallenge == null || !storedChallenge.equals(request.challenge())) {
            LOGGER.warn("Biometric login failed - Invalid or expired challenge for email: {}", request.email());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "Invalid or expired challenge"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        Optional<UserModel> memberUserOpt = userRepository.findByEmailOrMobile(request.email(), request.email());

        if (memberUserOpt.isPresent()) {
            UserModel user = memberUserOpt.get();

            if (!Boolean.TRUE.equals(user.getBiometricEnabled())) {
                LOGGER.warn("Biometric login failed - Biometric not enabled for user: {}", request.email());
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.FORBIDDEN.value(),
                                "Biometric authentication is not enabled"),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // Check if user is active
            if (!Constants.ACTIVE.equals(user.getStatus().name())) {
                LOGGER.warn("Biometric login failed - Inactive user: {}", request.email());
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                                CustomStatusCode.INACTIVE_USER.getMessage()),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            String passkeyHash = user.getPasskey();
            if (passkeyHash == null || passkeyHash.isEmpty()) {
                LOGGER.warn("Biometric login failed - Passkey not set for user: {}", request.email());
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "Biometric passkey not configured"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            String expectedSignature = computeHmac(passkeyHash, request.challenge());
            if (!expectedSignature.equals(request.signature())) {
                redisTemplate.delete(challengeKey);
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                                "Invalid biometric signature"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Delete used challenge
            redisTemplate.delete(challengeKey);

            // Generate JWT token
            String deviceId = RequestContext.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                LOGGER.error("DeviceId is missing from request header");
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
            String userIdentifier = request.email();
            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier, memberCode);
            LOGGER.info("JWT token generated for member user with deviceId: {}, email: {}, memberCode: {}", deviceId, userIdentifier, memberCode);

            Integer sex = user.getSex() == null ? 2 : user.getSex();
            String birthDate = user.getBirthDate() == null ? "" : user.getBirthDate().toString();
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            LOGGER.info("Biometric login successful for member user: {}", request.email());

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = userRepository.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            return CommonResponse.success(Map.of(
                    "message", "Biometric Login Successful",
                    "accessToken", accessToken
            ));
        }

        Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepositry.findByEmailOrMobile(request.email(), request.email());

        if (nonMemberUserOpt.isPresent()) {
            NonMemberUserModel user = nonMemberUserOpt.get();

            // Check if passkey exists
            String passkeyHash = user.getPasskey();
            if (passkeyHash == null || passkeyHash.isEmpty()) {
                LOGGER.warn("Biometric login failed - Passkey not set for non-member user: {}", request.email());
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "Biometric passkey not configured"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Verify HMAC signature
            String expectedSignature = computeHmac(passkeyHash, request.challenge());
            if (!expectedSignature.equals(request.signature())) {
                LOGGER.warn("Biometric login failed - Invalid signature for non-member user: {}", request.email());
                redisTemplate.delete(challengeKey);
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                                "Invalid biometric signature"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Delete used challenge
            redisTemplate.delete(challengeKey);

            // Generate JWT token
            String deviceId = RequestContext.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                LOGGER.error("DeviceId is missing from request header");
                throw new CustomException(CustomStatusCode.INVALID_CREDENTIALS.getCode(),
                        "DeviceId header is required");
            }

            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(deviceId);
            String userIdentifier = user.getEmail() != null ? user.getEmail() : user.getMobile();
            String accessToken = jwtTokenUtil.generateToken(userDetails, userIdentifier);
            LOGGER.info("JWT token generated for non-member user with deviceId: {}, username: {}", deviceId, userIdentifier);

            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String nonMemberCode = user.getNonMemberCode() != null ? user.getNonMemberCode() : "";
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            LOGGER.info("Biometric login successful for non-member user: {}", request.email());

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = nonMemberUserRepositry.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            return CommonResponse.success(Map.of(
                    "message", "Login Successful",
                    "memberCode", memberCode,
                    "nonMemberCode", nonMemberCode,
                    "isWhitelisted", isWhitelisted,
                    "email", email,
                    "mobile", mobile,
                    "accessToken", accessToken,
                    "isConsented", isConsented
            ));
        }

        // User not found
        LOGGER.warn("Biometric login failed - User not found: {}", request.email());
        redisTemplate.delete(challengeKey);
        return CommonResponse.error(
                new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                        CustomStatusCode.USER_NOT_FOUND.getMessage()),
                HttpStatus.NOT_FOUND.value()
        );
    }

    @Override
    public CommonResponse storePasskeyHash(StorePasskeyHashRequest request) {
        LOGGER.info("Storing passkey hash for email: {}", request.email());

        Optional<UserModel> memberUserOpt = userRepository.findByEmailOrMobile(request.email(), request.email());
        if (memberUserOpt.isPresent()) {
            UserModel user = memberUserOpt.get();
            user.setPasskey(request.passkeyHash()); // Store passkeyHash in passkey column
            user.setBiometricEnabled(true);
            user.setPasskeyCreatedAt(LocalDateTime.now()); // Set timestamp for auditing
            userRepository.save(user);
            LOGGER.info("Passkey hash stored for member user: {} at {}", request.email(), LocalDateTime.now());
            return CommonResponse.success("Passkey hash stored successfully");
        }

        // Try to find user in NON_MEMBER table
        Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepositry.findByEmailOrMobile(request.email(), request.email());
        if (nonMemberUserOpt.isPresent()) {
            NonMemberUserModel user = nonMemberUserOpt.get();
            user.setPasskey(request.passkeyHash()); // Store passkeyHash in passkey column
            user.setPasskeyCreatedAt(LocalDateTime.now()); // Set timestamp for auditing
            nonMemberUserRepositry.save(user);
            LOGGER.info("Passkey hash stored for non-member user: {} at {}", request.email(), LocalDateTime.now());
            return CommonResponse.success("Passkey hash stored successfully");
        }

        // User not found
        LOGGER.warn("Passkey hash storage failed - User not found: {}", request.email());
        return CommonResponse.error(
                new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                        CustomStatusCode.USER_NOT_FOUND.getMessage()),
                HttpStatus.NOT_FOUND.value()
        );
    }

    private String computeHmac(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] hmacBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            LOGGER.error("Error computing HMAC signature", e);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error computing signature");
        }
    }


    private String generateNextNonMemberCode() {
        Optional<String> maxCodeOpt = nonMemberUserRepositry.findMaxNonMemberCode();

        if (maxCodeOpt.isEmpty() || maxCodeOpt.get() == null) {
            return "NM-0200000";
        }

        String maxCode = maxCodeOpt.get();
        if (maxCode.startsWith("NM-") && maxCode.length() > 3) {
            try {
                String numericPart = maxCode.substring(3);
                int currentNumber = Integer.parseInt(numericPart);
                int nextNumber = currentNumber + 1;
                return String.format("NM-%07d", nextNumber);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid nonMemberCode format found: {}. Starting from NM-0200000", maxCode);
                return "NM-0200000";
            }
        } else {
            LOGGER.warn("Invalid nonMemberCode format found: {}. Starting from NM-0200000", maxCode);
            return "NM-0200000";
        }
    }

    @Override
    public CommonResponse getUserDetails(UserDetailsRequest request) {
        LOGGER.info("GetUserDetailsRequest: {}", request.userId());

        // Step 1: Validate JWT token from request header (security context check)
        String token = getTokenFromRequest();
        if (token == null || token.isEmpty()) {
            LOGGER.warn("JWT token is missing from request header");
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "JWT token is required. Please provide token in Authorization header or X-Access-Token header"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        // Validate token and extract username from token
        String tokenUsername = null;
        String tokenDeviceId = null;
        try {
            // Step 1: Get userId header (which should contain deviceId)
            String headerUserId = getUserIdFromRequest();
            if (headerUserId == null || headerUserId.isEmpty()) {
                LOGGER.error("userId header is missing from request");
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "Missing mandatory header: userId"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Step 2: Extract deviceId from token (token subject contains deviceId)
            tokenDeviceId = jwtTokenUtil.getUsernameFromToken(token);
            if (tokenDeviceId == null || tokenDeviceId.isEmpty()) {
                LOGGER.warn("DeviceId not found in token");
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "Invalid token: deviceId not found in token"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Step 3: Validate that userId header matches deviceId from token
            if (!headerUserId.equals(tokenDeviceId)) {
                LOGGER.warn("userId header mismatch: headerUserId={}, tokenDeviceId={}", headerUserId, tokenDeviceId);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "userId header does not match token deviceId"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }

            // Step 4: Validate token with deviceId
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(tokenDeviceId);
            if (!jwtTokenUtil.validateToken(token, userDetails)) {
                LOGGER.warn("Invalid JWT token provided");
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "Invalid or expired JWT token"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }
            
            // Step 5: Extract username from token claim
            tokenUsername = jwtTokenUtil.getUsernameFromTokenClaim(token);
            if (tokenUsername == null || tokenUsername.isEmpty()) {
                LOGGER.warn("Username not found in token claims");
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                                "Invalid token: username not found in token"),
                        HttpStatus.UNAUTHORIZED.value()
                );
            }
        } catch (Exception e) {
            LOGGER.error("Error validating JWT token: {}", e.getMessage());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                            "Invalid JWT token"),
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        // Step 2: Validate that the userId in request matches the username from token
        if (!tokenUsername.equalsIgnoreCase(request.userId())) {
            LOGGER.warn("Token username mismatch: tokenUsername={}, requestUserId={}", tokenUsername, request.userId());
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.FORBIDDEN.value(),
                            "Token does not belong to the requested user"),
                    HttpStatus.FORBIDDEN.value()
            );
        }

        // Step 3: Get user details based on token username (no password validation needed)
        // Try member first
        Optional<UserModel> memberUserOpt = userRepository.findByEmailOrMobile(tokenUsername, tokenUsername);
        if (memberUserOpt.isPresent()) {
            UserModel user = memberUserOpt.get();

            // Check if user is active
            if (!Constants.ACTIVE.equals(user.getStatus().name())) {
                LOGGER.warn("Inactive member user: {}", tokenUsername);
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                                CustomStatusCode.INACTIVE_USER.getMessage()),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // Generate refreshed JWT token using validated deviceId from token
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(tokenDeviceId);
            String userIdentifier = user.getEmail() != null ? user.getEmail() : user.getMobile();
            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String refreshedToken = jwtTokenUtil.generateToken(userDetails, userIdentifier, memberCode);
            LOGGER.info("Refreshed JWT token generated for member user with deviceId: {}, username: {}, memberCode: {}", tokenDeviceId, userIdentifier, memberCode);

            Integer sex = user.getSex() == null ? 2 : user.getSex();
            String birthDate = user.getBirthDate() == null ? "" : user.getBirthDate().toString();
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = userRepository.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            // Store refreshed token in response data temporarily (will be moved to header in controller)
            return CommonResponse.success(Map.of(
                    "memberCode", memberCode,
                    "isWhitelisted", isWhitelisted,
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "sex", sex,
                    "birthDate", birthDate,
                    "email", email,
                    "mobile", mobile,
                    "isConsented", isConsented,
                    "accessToken", refreshedToken
            ));
        }

        // Try non-member
        Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepositry.findByEmailOrMobile(tokenUsername, tokenUsername);
        if (nonMemberUserOpt.isPresent()) {
            NonMemberUserModel user = nonMemberUserOpt.get();

            // Check if user is active
            if (!Constants.ACTIVE.equals(user.getStatus().name())) {
                LOGGER.warn("Inactive non-member user: {}", tokenUsername);
                return CommonResponse.error(
                        new ErrorResponse(CustomStatusCode.INACTIVE_USER.getCode(),
                                CustomStatusCode.INACTIVE_USER.getMessage()),
                        HttpStatus.FORBIDDEN.value()
                );
            }

            // Generate refreshed JWT token using validated deviceId from token
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(tokenDeviceId);
            String userIdentifier = user.getEmail() != null ? user.getEmail() : user.getMobile();
            String refreshedToken = jwtTokenUtil.generateToken(userDetails, userIdentifier);
            LOGGER.info("Refreshed JWT token generated for non-member user with deviceId: {}, username: {}", tokenDeviceId, userIdentifier);

            String memberCode = user.getMemberCode() != null ? user.getMemberCode() : "";
            String nonMemberCode = user.getNonMemberCode() != null ? user.getNonMemberCode() : "";
            String isWhitelisted = user.getIsWhitelisted() != null ? user.getIsWhitelisted() : "N";
            String email = user.getEmail() != null ? user.getEmail() : "";
            String mobile = user.getMobile() != null ? user.getMobile() : "";

            boolean isConsented = user.getIsConsented() != null && user.getIsConsented();
            if (isConsented) {
                consentService.revalidateUserConsentFlag(user.getId().intValue());
                user = nonMemberUserRepositry.findById(user.getId()).orElse(user);
                isConsented = user.getIsConsented() != null && user.getIsConsented();
            }
            LOGGER.info("Fetched isConsented: {}", isConsented);

            // Store refreshed token in response data temporarily (will be moved to header in controller)
            return CommonResponse.success(Map.of(
                    "memberCode", memberCode,
                    "nonMemberCode", nonMemberCode,
                    "isWhitelisted", isWhitelisted,
                    "email", email,
                    "mobile", mobile,
                    "isConsented", isConsented,
                    "accessToken", refreshedToken
            ));
        }

        // User not found
        LOGGER.warn("User not found: {}", tokenUsername);
        return CommonResponse.error(
                new ErrorResponse(CustomStatusCode.USER_NOT_FOUND.getCode(),
                        Constants.EMAIL_NOT_REGISTERED_NON_MEMBER),
                HttpStatus.NOT_FOUND.value()
        );
    }

    /**
     * Extract JWT token from request header (Authorization or X-Access-Token)
     */
    private String getTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    // Try Authorization header first (Bearer token)
                    String bearerToken = request.getHeader("Authorization");
                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        return bearerToken.substring(7);
                    }
                    // Try X-Access-Token header
                    String accessToken = request.getHeader("X-Access-Token");
                    if (accessToken != null && !accessToken.isEmpty()) {
                        return accessToken;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not extract token from request: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract userId header from request (which should contain deviceId)
     */
    private String getUserIdFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    // Try different case variations of userId header
                    String userId = request.getHeader("userId");
                    if (userId == null || userId.isEmpty()) {
                        userId = request.getHeader("UserId");
                    }
                    if (userId == null || userId.isEmpty()) {
                        userId = request.getHeader("USERID");
                    }
                    if (userId == null || userId.isEmpty()) {
                        userId = request.getHeader("user-id");
                    }
                    return userId;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not extract userId from request: {}", e.getMessage());
        }
        return null;
    }

}

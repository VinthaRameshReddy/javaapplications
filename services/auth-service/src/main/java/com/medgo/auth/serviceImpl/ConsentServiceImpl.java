package com.medgo.auth.serviceImpl;

import com.medgo.auth.domain.entity.medigo.Consent;
import com.medgo.auth.domain.entity.medigo.NonMemberUserModel;
import com.medgo.auth.domain.entity.medigo.UserConsent;
import com.medgo.auth.domain.entity.medigo.UserModel;
import com.medgo.auth.domain.request.ListConsentsRequest;
import com.medgo.auth.domain.request.StoreUserConsentRequest;
import com.medgo.auth.repository.medigo.ConsentRepository;
import com.medgo.auth.repository.medigo.NonMemberUserRepositry;
import com.medgo.auth.repository.medigo.UserConsentRepository;
import com.medgo.auth.repository.medigo.UserRepository;
import com.medgo.auth.service.ConsentService;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsentServiceImpl implements ConsentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsentServiceImpl.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ConsentRepository consentRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserRepository userRepository;
    private final NonMemberUserRepositry nonMemberUserRepository;

    public ConsentServiceImpl(ConsentRepository consentRepository,
                              UserConsentRepository userConsentRepository,
                              UserRepository userRepository,
                              NonMemberUserRepositry nonMemberUserRepository) {
        this.consentRepository = consentRepository;
        this.userConsentRepository = userConsentRepository;
        this.userRepository = userRepository;
        this.nonMemberUserRepository = nonMemberUserRepository;
    }

    @Override
    public CommonResponse listConsents(ListConsentsRequest request) {
        LOGGER.info("Listing available consents");

        try {
            List<Consent> consents = consentRepository.findAllEnabledConsents();
            List<Map<String, Object>> mandatoryConsents = new java.util.ArrayList<>();
            List<Map<String, Object>> optionalConsents = new java.util.ArrayList<>();

            for (Consent consent : consents) {
                Map<String, Object> consentMap = new HashMap<>();
                consentMap.put("id", consent.getId());
                consentMap.put("type", consent.getType());
                consentMap.put("versionNo", consent.getVersionNo());
                consentMap.put("header", consent.getHeader());
                consentMap.put("content", consent.getContent());
                consentMap.put("enabled", consent.getEnabled());
                consentMap.put("required", consent.getRequired());
                consentMap.put("createdOn", consent.getCreatedOn() != null ?
                        consent.getCreatedOn().format(DATE_TIME_FORMATTER) : null);

                if (consent.getRequired() != null && consent.getRequired() == 1) {
                    mandatoryConsents.add(consentMap);
                } else {
                    optionalConsents.add(consentMap);
                }
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("consents", consents.stream()
                    .map(consent -> {
                        Map<String, Object> consentMap = new HashMap<>();
                        consentMap.put("id", consent.getId());
                        consentMap.put("type", consent.getType());
                        consentMap.put("versionNo", consent.getVersionNo());
                        consentMap.put("header", consent.getHeader());
                        consentMap.put("content", consent.getContent());
                        consentMap.put("enabled", consent.getEnabled());
                        consentMap.put("required", consent.getRequired());
                        consentMap.put("createdOn", consent.getCreatedOn() != null ?
                                consent.getCreatedOn().format(DATE_TIME_FORMATTER) : null);
                        return consentMap;
                    })
                    .collect(Collectors.toList()));
            responseData.put("mandatoryConsents", mandatoryConsents);
            responseData.put("optionalConsents", optionalConsents);
            responseData.put("totalCount", consents.size());
            responseData.put("mandatoryCount", mandatoryConsents.size());
            responseData.put("optionalCount", optionalConsents.size());

            LOGGER.info("Found {} enabled consents ({} mandatory, {} optional)",
                    consents.size(), mandatoryConsents.size(), optionalConsents.size());
            return CommonResponse.success(responseData);
        } catch (Exception e) {
            LOGGER.error("Error listing consents", e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to retrieve consents"),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public CommonResponse storeUserConsent(StoreUserConsentRequest request) {
        LOGGER.info("Storing user consent for email: {}, consentIds: {}, agreed: {}",
                request.userId(), request.consentIds(), request.agreed());

        try {
            String userEmail = request.userId();
            String memberCode = null;
            String userMobile = null;

            Optional<UserModel> memberUserOpt = userRepository.findByEmail(userEmail);
            if (memberUserOpt.isPresent()) {
                UserModel user = memberUserOpt.get();
                memberCode = user.getMemberCode();
                userMobile = user.getMobile();
                LOGGER.info("Found member user with email: {}, memberCode: {}, mobile: {}", userEmail, memberCode, userMobile);
            } else {
                Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepository.findByEmail(userEmail);
                if (nonMemberUserOpt.isPresent()) {
                    NonMemberUserModel user = nonMemberUserOpt.get();
                    memberCode = user.getMemberCode();
                    userMobile = user.getMobile();
                    LOGGER.info("Found non-member user with email: {}, memberCode: {}, mobile: {}", userEmail, memberCode, userMobile);
                }
            }

            if (memberCode == null) {
                LOGGER.warn("User not found for email: {}", userEmail);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                                "User not found"),
                        HttpStatus.NOT_FOUND.value()
                );
            }

            List<UserConsent> existingConsents = userConsentRepository.findByUserId(userEmail);
            List<Integer> existingConsentIds = existingConsents.stream()
                    .filter(uc -> uc.getAgreed() != null && uc.getAgreed() == 1)
                    .map(UserConsent::getConsentId)
                    .collect(Collectors.toList());
            LOGGER.info("User {} already has {} accepted consents: {}", userEmail, existingConsentIds.size(), existingConsentIds);

            List<Integer> newConsentIds = request.consentIds().stream()
                    .filter(consentId -> !existingConsentIds.contains(consentId))
                    .collect(Collectors.toList());

            if (newConsentIds.isEmpty()) {
                LOGGER.info("All consent IDs provided are already stored for user: {}", userEmail);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("userId", userEmail);
                responseData.put("memberCode", memberCode);
                responseData.put("mobile", userMobile != null ? userMobile : "");
                responseData.put("consentIds", request.consentIds());
                responseData.put("newConsentIdsStored", 0);
                responseData.put("message", "All consents are already stored");
                return CommonResponse.success(responseData);
            }

            int storedCount = 0;
            int skippedCount = 0;
            List<Integer> storedConsentIds = new java.util.ArrayList<>();
            List<Integer> skippedConsentIds = new java.util.ArrayList<>();

            for (Integer consentId : newConsentIds) {
                Optional<Consent> consentOpt = consentRepository.findById(consentId);
                if (consentOpt.isEmpty()) {
                    LOGGER.warn("Consent not found for consentId: {}, skipping", consentId);
                    skippedCount++;
                    skippedConsentIds.add(consentId);
                    continue;
                }

                Consent consent = consentOpt.get();
                if (consent.getEnabled() == null || consent.getEnabled() != 1) {
                    LOGGER.warn("Consent is not enabled for consentId: {}, skipping", consentId);
                    skippedCount++;
                    skippedConsentIds.add(consentId);
                    continue;
                }

                Optional<UserConsent> existingUserConsentOpt =
                        userConsentRepository.findByUserIdAndConsentId(userEmail, consentId);

                if (existingUserConsentOpt.isPresent()) {
                    UserConsent userConsent = existingUserConsentOpt.get();
                    userConsent.setAgreed(request.agreed());
                    userConsent.setMemberCode(memberCode);
                    userConsent.setMobile(userMobile);
                    userConsent.setUpdatedOn(LocalDateTime.now());
                    userConsent.setUpdatedBy("System");
                    userConsentRepository.save(userConsent);
                    storedCount++;
                    storedConsentIds.add(consentId);
                    LOGGER.info("Updated existing user consent for userId: {}, consentId: {}, memberCode: {}, mobile: {}",
                            userEmail, consentId, memberCode, userMobile);
                } else {
                    UserConsent userConsent = new UserConsent();
                    userConsent.setUserId(userEmail);
                    userConsent.setConsentId(consentId);
                    userConsent.setAgreed(request.agreed());
                    userConsent.setMemberCode(memberCode);
                    userConsent.setMobile(userMobile);
                    userConsent.setCreatedOn(LocalDateTime.now());
                    userConsent.setCreatedBy("System");
                    userConsentRepository.save(userConsent);
                    storedCount++;
                    storedConsentIds.add(consentId);
                    LOGGER.info("Created new user consent for userId: {}, consentId: {}, memberCode: {}, mobile: {}",
                            userEmail, consentId, memberCode, userMobile);
                }
            }

            updateUserConsentedFlag(userEmail);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", userEmail);
            responseData.put("memberCode", memberCode != null ? memberCode : "");
            responseData.put("email", userEmail);
            responseData.put("mobile", userMobile != null ? userMobile : "");
            responseData.put("totalConsentIdsProvided", request.consentIds().size());
            responseData.put("alreadyStoredCount", existingConsentIds.size());
            responseData.put("newConsentIdsStored", storedCount);
            responseData.put("skippedCount", skippedCount);
            responseData.put("storedConsentIds", storedConsentIds);
            if (!skippedConsentIds.isEmpty()) {
                responseData.put("skippedConsentIds", skippedConsentIds);
            }
            responseData.put("message", String.format("Stored %d new consent(s) successfully", storedCount));

            LOGGER.info("User consent stored successfully for userId: {}, memberCode: {}, mobile: {}, stored: {}, skipped: {}",
                    userEmail, memberCode, userMobile, storedCount, skippedCount);
            return CommonResponse.success(responseData);
        } catch (Exception e) {
            LOGGER.error("Error storing user consent", e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to store user consent"),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public boolean isUserConsented(Integer userId) {
        if (userId == null) {
            return false;
        }

        try {
            Optional<UserModel> userOpt = userRepository.findById(userId.longValue());
            String userIdIdentifier = null;
            if (userOpt.isPresent()) {
                userIdIdentifier = userOpt.get().getEmail();
            } else {
                Optional<NonMemberUserModel> nonMemberOpt = nonMemberUserRepository.findById(userId.longValue());
                if (nonMemberOpt.isPresent()) {
                    userIdIdentifier = nonMemberOpt.get().getEmail();
                }
            }

            if (userIdIdentifier == null) {
                LOGGER.warn("User not found for userId: {}", userId);
                return false;
            }

            List<Consent> requiredConsents = consentRepository.findRequiredConsents();

            if (requiredConsents.isEmpty()) {
                LOGGER.info("No required consents found. User {} is considered consented.", userId);
                return true;
            }

            List<Integer> agreedConsentIds = userConsentRepository.findAgreedConsentIdsByUserId(userIdIdentifier);

            List<Integer> requiredConsentIds = requiredConsents.stream()
                    .map(consent -> consent.getId().intValue())
                    .collect(Collectors.toList());

            boolean allRequiredAccepted = agreedConsentIds.containsAll(requiredConsentIds);

            return allRequiredAccepted;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateUserConsentedFlag(String userIdIdentifier) {
        try {
            // Find user by email/mobile identifier
            Optional<UserModel> userOpt = userRepository.findByEmail(userIdIdentifier);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmailOrMobile(userIdIdentifier, userIdIdentifier);
            }

            if (userOpt.isPresent()) {
                UserModel user = userOpt.get();
                List<Consent> requiredConsents = consentRepository.findRequiredConsents();
                boolean allRequiredAccepted = true;

                if (!requiredConsents.isEmpty()) {
                    List<Integer> agreedConsentIds = userConsentRepository.findAgreedConsentIdsByUserId(userIdIdentifier);
                    List<Integer> requiredConsentIds = requiredConsents.stream()
                            .map(consent -> consent.getId().intValue())
                            .collect(Collectors.toList());
                    allRequiredAccepted = agreedConsentIds.containsAll(requiredConsentIds);
                }

                user.setIsConsented(allRequiredAccepted);
                userRepository.save(user);
                LOGGER.info("Updated isConsented flag to {} for member userId: {}", allRequiredAccepted, userIdIdentifier);
                return;
            }

            // Try to find user in NON_MEMBER table
            Optional<NonMemberUserModel> nonMemberUserOpt = nonMemberUserRepository.findByEmail(userIdIdentifier);
            if (nonMemberUserOpt.isEmpty()) {
                nonMemberUserOpt = nonMemberUserRepository.findByEmailOrMobile(userIdIdentifier, userIdIdentifier);
            }

            if (nonMemberUserOpt.isPresent()) {
                NonMemberUserModel user = nonMemberUserOpt.get();
                List<Consent> requiredConsents = consentRepository.findRequiredConsents();
                boolean allRequiredAccepted = true;

                if (!requiredConsents.isEmpty()) {
                    List<Integer> agreedConsentIds = userConsentRepository.findAgreedConsentIdsByUserId(userIdIdentifier);
                    List<Integer> requiredConsentIds = requiredConsents.stream()
                            .map(consent -> consent.getId().intValue())
                            .collect(Collectors.toList());
                    allRequiredAccepted = agreedConsentIds.containsAll(requiredConsentIds);
                }

                user.setIsConsented(allRequiredAccepted);
                nonMemberUserRepository.save(user);
                return;
            }

            LOGGER.warn("User not found for userId: {} when updating isConsented flag", userIdIdentifier);
        } catch (Exception e) {
            LOGGER.error("Error updating isConsented flag for userId: {}", userIdIdentifier, e);
        }
    }

    @Override
    public void revalidateUserConsentFlag(Integer userId) {
        LOGGER.info("Re-validating consent flag for userId: {}", userId);
        Optional<UserModel> userOpt = userRepository.findById(userId.longValue());
        if (userOpt.isPresent()) {
            updateUserConsentedFlag(userOpt.get().getEmail());
        } else {
            Optional<NonMemberUserModel> nonMemberOpt = nonMemberUserRepository.findById(userId.longValue());
            if (nonMemberOpt.isPresent()) {
                updateUserConsentedFlag(nonMemberOpt.get().getEmail());
            }
        }
    }
}


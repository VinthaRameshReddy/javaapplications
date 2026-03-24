package com.medgo.facescan.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.facescan.components.MedGoCustomSecurityContext;
import com.medgo.facescan.constant.FaceScanErrorMessages;
import com.medgo.facescan.domain.models.medgo.FaceScanSessionModel;
import com.medgo.facescan.domain.models.medgo.StoreFaceScanResultModel;
import com.medgo.facescan.domain.models.membership.MembershipModel;
import com.medgo.facescan.domain.request.FaceScanHistoryRequest;
import com.medgo.facescan.domain.request.FaceScanResultRequest;
import com.medgo.facescan.domain.request.FaceScanTncRequest;
import com.medgo.facescan.domain.response.FaceScanDto;
import com.medgo.facescan.domain.response.FaceScanHistoryDto;
import com.medgo.facescan.domain.response.FaceScanResultDto;
import com.medgo.facescan.repository.medgo.FaceScanMemberWhiteListedRepository;
import com.medgo.facescan.repository.medgo.FaceScanResultRepository;
import com.medgo.facescan.repository.medgo.FaceScanSessionRepository;
import com.medgo.facescan.repository.medgo.FaceScanWhiteListedRepository;
import com.medgo.facescan.repository.membership.MembershipModelRepository;
import com.medgo.facescan.service.FaceScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FaceScanServiceImpl implements FaceScanService {

    private final FaceScanSessionRepository sessionRepository;
    private final FaceScanResultRepository faceScanResultRepository;
    private final ObjectMapper objectMapper;
    private final FaceScanWhiteListedRepository faceScanWhiteListedRepository;
    private final FaceScanMemberWhiteListedRepository faceScanMemberWhiteListedRepository;
    private final MembershipModelRepository membershipRepository;
    private final MedGoCustomSecurityContext securityContext;

    @Value("${facescan.cooling.time.days:7}")
    private int coolingTimeDays = 7;

    @Value("${facescan.cooling.time.hours:0}")
    private int coolingTimeHours = 0;

    @Value("${facescan.cooling.time.mins:0}")
    private int coolingTimeMins = 0;

    @Value("${facescan.cooling.time.seconds:0}")
    private int coolingTimeSeconds =0;

    public FaceScanServiceImpl(
            FaceScanSessionRepository sessionRepository,
            FaceScanResultRepository faceScanResultRepository,
            ObjectMapper objectMapper,
            FaceScanWhiteListedRepository faceScanWhiteListedRepository,
            FaceScanMemberWhiteListedRepository faceScanMemberWhiteListedRepository,
            MembershipModelRepository membershipRepository,
            MedGoCustomSecurityContext securityContext) {
        this.sessionRepository = sessionRepository;
        this.faceScanResultRepository = faceScanResultRepository;
        this.objectMapper = objectMapper;
        this.faceScanWhiteListedRepository = faceScanWhiteListedRepository;
        this.faceScanMemberWhiteListedRepository = faceScanMemberWhiteListedRepository;
        this.membershipRepository = membershipRepository;
        this.securityContext = securityContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommonResponse checkEligibility(String memberCode) {
        log.info("Checking eligibility for member code: {}", memberCode);

        if (memberCode == null || memberCode.isEmpty()) {
            log.error(FaceScanErrorMessages.MEMBER_CODE_NULL_OR_EMPTY);
            return CommonResponse.error(
                    new ErrorResponse(400, FaceScanErrorMessages.MEMBER_CODE_NULL_OR_EMPTY),
                    400);
        }

        // Validate memberCode against authenticated user's token
        HashMap<String, Object> validationResult = validateMemCode(memberCode);
        Boolean isValid = (Boolean) validationResult.get("validate");
        if (!Boolean.TRUE.equals(isValid)) {
            log.error("SECURITY ALERT: MemberCode validation failed for memberCode: {}", memberCode);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.FORBIDDEN.value(), "MemberCode does not belong to authenticated user. Access denied."),
                    HttpStatus.FORBIDDEN.value());
        }

        try {
            Optional<MembershipModel> memberOpt = membershipRepository.findByMemberCode(memberCode);

            if (memberOpt.isEmpty()) {
                log.warn(FaceScanErrorMessages.MEMBER_CODE_NOT_FOUND + ": {}", memberCode);
                return CommonResponse.error(
                        new ErrorResponse(404, FaceScanErrorMessages.MEMBER_CODE_NOT_FOUND),
                        404);
            }

            MembershipModel member = memberOpt.get();

            log.info("Found member {} in Membership DB, AccountCode: {}", member.getMemberCode(), member.getAccountCode());

            String accountCode = member.getAccountCode();
            Character memberType = member.getMemType().charAt(0);

            Integer active = faceScanMemberWhiteListedRepository.findActiveMember(memberCode, memberType, LocalDateTime.now());
            if (active == null || active == 0) {
                Integer accountActive = faceScanWhiteListedRepository.findActiveMember(accountCode, memberType, LocalDateTime.now());
                if (accountActive == null || accountActive == 0) {
                    log.warn(FaceScanErrorMessages.NOT_CORPORATE_USER + ": {}", memberCode);
                    return CommonResponse.error(
                            new ErrorResponse(100, FaceScanErrorMessages.NOT_CORPORATE_USER),
                            100);
                }
            }

            Pageable pageable = PageRequest.of(0, 1);
            List<FaceScanSessionModel> sessions = sessionRepository.findTop1ByMemberCode(memberCode, pageable);
            FaceScanSessionModel lastSession = sessions.isEmpty() ? null : sessions.get(0);

            String fedId = null;
            Map<String, Object> lastScanData = null;
            Map<String, Object> sessionFields = new HashMap<>();
            Long sessionId = 0L;

            if (lastSession != null) {
                fedId = lastSession.getFedId();
                sessionId = lastSession.getId();
                sessionFields.put("height", lastSession.getHeight());
                sessionFields.put("weight", lastSession.getWeight());
                sessionFields.put("isSmoker", lastSession.getIsSmoker());
                sessionFields.put("bpMedication", lastSession.getBpMedication());
                sessionFields.put("isDiabetic", lastSession.getIsDiabetic());

                LocalDateTime latestSessionUpdatedOn = lastSession.getUpdatedOn();
                if (latestSessionUpdatedOn != null && !isCooldownOver(latestSessionUpdatedOn)) {
                    long timeLeft = getTimeLeftForNextScan(latestSessionUpdatedOn);
                    String formattedTime = formatDuration(timeLeft);
                    log.warn(FaceScanErrorMessages.FACE_SCAN_LIMIT_EXCEEDED + ": {}", memberCode);
                    return CommonResponse.error(
                            new ErrorResponse(102,
                                    String.format(FaceScanErrorMessages.FACE_SCAN_LIMIT_EXCEEDED, formattedTime)),
                            102);
                }
            }

            List<StoreFaceScanResultModel> lastScanResult = faceScanResultRepository.findLatestScanByMemberCode(memberCode);
            if (lastScanResult != null && !lastScanResult.isEmpty()) {
                lastScanData = objectMapper.readValue(lastScanResult.get(0).getScanData(), Map.class);
                log.debug("Found previous scan data for member {}", memberCode);
            }

            if (fedId == null) {
                fedId = generateFedId();
                log.debug("Generated new fedId: {}", fedId);
            }

            FaceScanSessionModel newSession = new FaceScanSessionModel(fedId, memberCode);
            if (sessionId > 0) {
                newSession.setId(sessionId);
                newSession.setHeight(lastSession.getHeight());
                newSession.setWeight(lastSession.getWeight());
                newSession.setBpMedication(lastSession.getBpMedication());
                newSession.setIsSmoker(lastSession.getIsSmoker());
                newSession.setIsDiabetic(lastSession.getIsDiabetic());
                log.debug("Updating existing session with ID: {}", sessionId);
            }

            sessionRepository.save(newSession);
            log.info("Successfully processed eligibility check for member {}", memberCode);

            FaceScanDto result = new FaceScanDto(
                    "User Eligible For Face Scan",
                    fedId,
                    lastScanData,
                    sessionFields
            );

            return CommonResponse.success(result);

        } catch (DataAccessException e) {
            log.error(FaceScanErrorMessages.DATABASE_ERROR + " while checking eligibility for member {}", memberCode, e);
            return CommonResponse.error(
                    new ErrorResponse(500, FaceScanErrorMessages.DATABASE_ERROR),
                    500);
        } catch (Exception e) {
            log.error(FaceScanErrorMessages.UNEXPECTED_ERROR + " while checking eligibility for member {}", memberCode, e);
            return CommonResponse.error(
                    new ErrorResponse(500, FaceScanErrorMessages.UNEXPECTED_ERROR),
                    500);
        }
    }

    @Override
    public CommonResponse acceptTncAndInitiateSession(FaceScanTncRequest request) {
        log.info("Processing TNC acceptance for fedId: {}", request.getFedId());

        if (request.getFedId() == null || request.getFedId().isEmpty()) {
            log.error(FaceScanErrorMessages.FED_ID_NULL_OR_EMPTY);
            return CommonResponse.error(
                    new ErrorResponse(400, FaceScanErrorMessages.FED_ID_NULL_OR_EMPTY),
                    400);
        }

        if (!request.isConsent()) {
            log.warn(FaceScanErrorMessages.TNC_NOT_ACCEPTED + " for fedId: {}", request.getFedId());
            return CommonResponse.error(
                    new ErrorResponse(400, FaceScanErrorMessages.TNC_NOT_ACCEPTED),
                    400);
        }

        try {
            Optional<FaceScanSessionModel> existingSessionOpt = sessionRepository.findByFedId(request.getFedId());
            if (existingSessionOpt.isEmpty()) {
                log.error(FaceScanErrorMessages.SESSION_NOT_FOUND_FOR_FED_ID + ": {}", request.getFedId());
                return CommonResponse.error(
                        new ErrorResponse(404, FaceScanErrorMessages.SESSION_NOT_FOUND_FOR_FED_ID),
                        404);
            }

            FaceScanSessionModel existingSession = existingSessionOpt.get();
            String memberCode = existingSession.getMemberCode();
            
            // Validate memberCode from session against authenticated user's token
            if (memberCode != null && !memberCode.isEmpty()) {
                HashMap<String, Object> validationResult = validateMemCode(memberCode);
                Boolean isValid = (Boolean) validationResult.get("validate");
                if (!Boolean.TRUE.equals(isValid)) {
                    log.error("SECURITY ALERT: MemberCode validation failed for session memberCode: {}", memberCode);
                    return CommonResponse.error(
                            new ErrorResponse(HttpStatus.FORBIDDEN.value(), "MemberCode does not belong to authenticated user. Access denied."),
                            HttpStatus.FORBIDDEN.value());
                }
            }
            
            Long id = existingSession.getId();
            String sessionId = UUID.randomUUID().toString();

            log.debug("Creating new session with ID: {} for member: {}", sessionId, memberCode);

            FaceScanSessionModel newSession = new FaceScanSessionModel();
            newSession.setId(id);
            newSession.setFedId(request.getFedId());
            newSession.setSessionId(sessionId);
            newSession.setStatus("STARTED");
            newSession.setCreationTime(LocalDateTime.now());
            newSession.setConsent(request.isConsent());
            newSession.setMemberCode(memberCode);

            setOptionalSessionFields(newSession, request);

            sessionRepository.save(newSession);

            log.info("Successfully initiated session {} for member {}", sessionId, memberCode);

            return CommonResponse.success(newSession);

        } catch (DataAccessException e) {
            log.error(FaceScanErrorMessages.DATABASE_ERROR + " while processing TNC for fedId: {}", request.getFedId(), e);
            return CommonResponse.error(
                    new ErrorResponse(500, FaceScanErrorMessages.DATABASE_ERROR),
                    500);
        } catch (Exception e) {
            log.error(FaceScanErrorMessages.UNEXPECTED_ERROR + " while processing TNC for fedId: {}", request.getFedId(), e);
            return CommonResponse.error(
                    new ErrorResponse(500, FaceScanErrorMessages.UNEXPECTED_ERROR),
                    500);
        }
    }

    @Transactional("medigoTransactionManager")
    @Override
    @SuppressWarnings("unchecked")
    public CommonResponse storeFaceScanResult(FaceScanResultRequest request) {
        log.info("Storing face scan result for session: {}", request.getSessionId());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            log.error(FaceScanErrorMessages.SESSION_ID_NULL_OR_EMPTY);
            return CommonResponse.error(
                    new ErrorResponse(400, FaceScanErrorMessages.SESSION_ID_NULL_OR_EMPTY),
                    400);
        }

        try {
            Optional<FaceScanSessionModel> sessionOptional = sessionRepository.findBySessionId(request.getSessionId());

            if (sessionOptional.isEmpty()) {
                log.error(FaceScanErrorMessages.FACE_SCAN_SESSION_NOT_FOUND + ": {}", request.getSessionId());
                return CommonResponse.error(
                        new ErrorResponse(404, FaceScanErrorMessages.FACE_SCAN_SESSION_NOT_FOUND),
                        404);
            }

            FaceScanSessionModel session = sessionOptional.get();
            
            // Validate memberCode from session against authenticated user's token
            String sessionMemberCode = session.getMemberCode();
            if (sessionMemberCode != null && !sessionMemberCode.isEmpty()) {
                HashMap<String, Object> validationResult = validateMemCode(sessionMemberCode);
                Boolean isValid = (Boolean) validationResult.get("validate");
                if (!Boolean.TRUE.equals(isValid)) {
                    log.error("SECURITY ALERT: MemberCode validation failed for session memberCode: {}", sessionMemberCode);
                    return CommonResponse.error(
                            new ErrorResponse(HttpStatus.FORBIDDEN.value(), "MemberCode does not belong to authenticated user. Access denied."),
                            HttpStatus.FORBIDDEN.value());
                }
            }

            LocalDateTime endTime;
            try {
                endTime = LocalDateTime.parse(request.getEndTime(), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                log.error(FaceScanErrorMessages.INVALID_END_TIME_FORMAT + ": {}", request.getEndTime());
                return CommonResponse.error(
                        new ErrorResponse(400, FaceScanErrorMessages.INVALID_END_TIME_FORMAT),
                        400);
            }

            Optional<StoreFaceScanResultModel> existingScanOpt = faceScanResultRepository.findBySessionId(request.getSessionId());

            StoreFaceScanResultModel scanResult;

            if (existingScanOpt.isPresent()) {
                scanResult = existingScanOpt.get();
                log.debug("Updating existing scan result for sessionId: {}", request.getSessionId());
            } else {
                scanResult = new StoreFaceScanResultModel();
                log.debug("Creating new scan result for sessionId: {}", request.getSessionId());
                scanResult.setSessionId(request.getSessionId());
                scanResult.setFedId(session.getFedId());
                scanResult.setMemberCode(session.getMemberCode());
            }

            scanResult.setScanResult(request.getScanResult());

            if (request.getScanData() != null && request.getScanData().get("measurementID") != null) {
                scanResult.setMeasurementID(request.getScanData().get("measurementID").toString());
            }

            try {
                scanResult.setScanData(objectMapper.writeValueAsString(request.getScanData()));
                log.debug("Successfully serialized scan data");
            } catch (JsonProcessingException e) {
                log.error(FaceScanErrorMessages.FAILED_TO_SERIALIZE_SCAN_DATA, e);
                return CommonResponse.error(
                        new ErrorResponse(500, FaceScanErrorMessages.FAILED_TO_SERIALIZE_SCAN_DATA),
                        500);
            }

            scanResult.setEndTime(endTime);

            faceScanResultRepository.save(scanResult);
            log.debug("Saved scan result to database");

            session.setSessionEndTime(endTime);
            session.setUpdatedOn(LocalDateTime.now());
            session.setStatus("COMPLETED");
            sessionRepository.save(session);
            log.info("Successfully stored face scan result for session {}", request.getSessionId());

            return CommonResponse.success("Face scan result stored successfully");

        } catch (DataAccessException e) {
            log.error(FaceScanErrorMessages.DATABASE_ERROR + " while storing scan result for session {}", request.getSessionId(), e);
            return CommonResponse.error(
                    new ErrorResponse(500, FaceScanErrorMessages.DATABASE_ERROR),
                    500);
        } catch (Exception e) {
            log.error(FaceScanErrorMessages.UNEXPECTED_ERROR + " while storing scan result for session {}", request.getSessionId(), e);
            return CommonResponse.error(
                    new ErrorResponse(500, FaceScanErrorMessages.UNEXPECTED_ERROR),
                    500);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommonResponse getFaceScanHistory(FaceScanHistoryRequest request) {
        String memberCode = request.getMemberCode();
        log.info("Fetching face scan history for member: {}", memberCode);

        if (memberCode == null || memberCode.isEmpty()) {
            log.error(FaceScanErrorMessages.MEMBER_CODE_NULL_OR_EMPTY);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), FaceScanErrorMessages.MEMBER_CODE_NULL_OR_EMPTY),
                    HttpStatus.BAD_REQUEST.value());
        }

        // Validate memberCode against authenticated user's token
        HashMap<String, Object> validationResult = validateMemCode(memberCode);
        Boolean isValid = (Boolean) validationResult.get("validate");
        if (!Boolean.TRUE.equals(isValid)) {
            log.error("SECURITY ALERT: MemberCode validation failed for memberCode: {}", memberCode);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.FORBIDDEN.value(), "MemberCode does not belong to authenticated user. Access denied."),
                    HttpStatus.FORBIDDEN.value());
        }

        try {
            List<StoreFaceScanResultModel> results =
                    faceScanResultRepository.findByMemberCodeAndScanResultOrderByEndTimeDesc(memberCode, "SUCCESS");

            if (results.isEmpty()) {
                log.warn(FaceScanErrorMessages.NO_FACE_SCAN_HISTORY_FOUND + ": {}", memberCode);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), FaceScanErrorMessages.NO_FACE_SCAN_HISTORY_FOUND),
                        HttpStatus.NOT_FOUND.value());
            }

            log.debug("Found {} scan records for member {}", results.size(), memberCode);

            List<FaceScanHistoryDto.ScanRecord> history = results.stream()
                    .map(record -> {
                        try {
                            return parseScanRecord(record);
                        } catch (Exception e) {
                            log.warn("Error parsing scan record for session {}", record.getSessionId(), e);
                            return createBasicScanRecord(record);
                        }
                    })
                    .collect(Collectors.toList());

            FaceScanHistoryDto dto = new FaceScanHistoryDto(
                    FaceScanErrorMessages.FACE_SCAN_HISTORY_RETRIEVE_SUCCESS, history);

            log.info("Successfully retrieved {} scan records for member {}", history.size(), memberCode);
            return CommonResponse.success(dto);

        } catch (DataAccessException e) {
            log.error(FaceScanErrorMessages.DATABASE_ERROR + " while fetching scan history for member {}", memberCode, e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), FaceScanErrorMessages.DATABASE_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            log.error(FaceScanErrorMessages.UNEXPECTED_ERROR + " while fetching scan history for member {}", memberCode, e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), FaceScanErrorMessages.UNEXPECTED_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommonResponse fetchFaceScanResult(String sessionId) {
        log.info("Fetching face scan result for session: {}", sessionId);

        if (sessionId == null || sessionId.isEmpty()) {
            log.error("Session ID is null or empty");
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Session ID cannot be null or empty"),
                    HttpStatus.BAD_REQUEST.value());
        }

        try {
            Optional<StoreFaceScanResultModel> resultOpt = faceScanResultRepository.findBySessionId(sessionId);

            if (resultOpt.isEmpty()) {
                log.error("Scan result not found for session {}", sessionId);
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Face scan session not found."),
                        HttpStatus.NOT_FOUND.value());
            }

            StoreFaceScanResultModel result = resultOpt.get();

            Map<String, Object> scanData = null;
            try {
                if (result.getScanData() != null) {
                    scanData = objectMapper.readValue(result.getScanData(), Map.class);
                    log.debug("Successfully parsed scan data for session {}", sessionId);
                }
            } catch (Exception e) {
                log.error("Error parsing scan data for session {}", sessionId, e);
            }

            FaceScanResultDto.ScanResult scanResult = new FaceScanResultDto.ScanResult(
                    result.getSessionId(),
                    result.getEndTime(),
                    result.getScanResult(),
                    scanData
            );

            FaceScanResultDto dto = new FaceScanResultDto(
                    "Face scan result retrieved successfully.",
                    scanResult
            );

            log.info("Successfully retrieved scan result for session {}", sessionId);
            return CommonResponse.success(dto);

        } catch (DataAccessException e) {
            log.error("Database error while fetching scan result for session {}", sessionId, e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Database error"),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            log.error("Unexpected error while fetching scan result for session {}", sessionId, e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @SuppressWarnings("unchecked")
    private FaceScanHistoryDto.ScanRecord parseScanRecord(StoreFaceScanResultModel record) throws JsonProcessingException {
        Double healthScore = null;
        Double multiplier = null;

        if (record.getScanData() != null) {
            Map<String, Object> scanDataMap = objectMapper.readValue(record.getScanData(), Map.class);
            Map<String, Object> channels = (Map<String, Object>) scanDataMap.get("channels");

            if (channels != null && channels.containsKey("HEALTH_SCORE")) {
                Map<String, Object> healthScoreData = (Map<String, Object>) channels.get("HEALTH_SCORE");
                List<Double> dataList = (List<Double>) healthScoreData.get("dataList");
                healthScore = dataList != null && !dataList.isEmpty() ? dataList.get(0) : null;
            }

            if (scanDataMap.containsKey("multiplier")) {
                multiplier = ((Number) scanDataMap.get("multiplier")).doubleValue();
            }
        }

        return new FaceScanHistoryDto.ScanRecord(
                record.getSessionId(),
                record.getEndTime(),
                record.getScanResult(),
                healthScore,
                multiplier
        );
    }

    private FaceScanHistoryDto.ScanRecord createBasicScanRecord(StoreFaceScanResultModel record) {
        return new FaceScanHistoryDto.ScanRecord(
                record.getSessionId(),
                record.getEndTime(),
                record.getScanResult(),
                null,
                null
        );
    }

    private void setOptionalSessionFields(FaceScanSessionModel session, FaceScanTncRequest request) {
        if (request.getMemberName() != null) session.setMemberName(request.getMemberName());
        if (request.getAge() != null) session.setAge(request.getAge());
        if (request.getGender() != null) session.setGender(request.getGender());
        if (request.getWeight() != null) session.setWeight(request.getWeight());
        if (request.getHeight() != null) session.setHeight(request.getHeight());
        if (request.getIsSmoker() != null) session.setIsSmoker(request.getIsSmoker());
        if (request.getIsDiabetic() != null) session.setIsDiabetic(request.getIsDiabetic());
        if (request.getBpMedication() != null) session.setBpMedication(request.getBpMedication());
    }

    private boolean isCooldownOver(LocalDateTime latestSessionEndTime) {
        if (latestSessionEndTime == null) {
            return true;
        }
        LocalDateTime pastDate = LocalDateTime.now().minusDays(coolingTimeDays).minusHours(coolingTimeHours).minusMinutes(coolingTimeMins).plusSeconds(coolingTimeSeconds);;
        return latestSessionEndTime.isBefore(pastDate);
    }

    private long getTimeLeftForNextScan(LocalDateTime latestSessionEndTime) {
        LocalDateTime nextAllowedTime = latestSessionEndTime.plusDays(coolingTimeDays).plusHours(coolingTimeHours).plusMinutes(coolingTimeMins).plusSeconds(coolingTimeSeconds);
        return Duration.between(LocalDateTime.now(), nextAllowedTime).toSeconds();
    }

    private String formatDuration(long seconds) {
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;
        long sec = seconds % 60;
        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, sec);
    }

    private String generateFedId() {
        return "FED" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Validate memberCode against authenticated user's membership data from JWT token
     * Similar to validateMemCode example - validates memberCode belongs to authenticated user
     * 
     * @param memCode Member code to validate
     * @return HashMap with validation result, memberCode, accountCode, memType, and validate flag
     */
    private HashMap<String, Object> validateMemCode(String memCode) {
        HashMap<String, Object> result = new HashMap<>();
        
        // Get authenticated username from JWT token
        String userId = securityContext.authenticatedUsername();
        
        if (userId == null || userId.isEmpty()) {
            log.error("SECURITY ALERT: No authenticated user found in JWT token");
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }
        
        // Get memberCode from JWT token
        String tokenMemberCode = securityContext.authenticatedMemberCode();
        
        if (tokenMemberCode == null || tokenMemberCode.isEmpty()) {
            log.warn("No memberCode found in JWT token for user: {}", userId);
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }
        
        // Validate requested memberCode matches token memberCode
        if (!tokenMemberCode.equalsIgnoreCase(memCode)) {
            log.error("SECURITY ALERT: MemberCode mismatch! Token memberCode={}, requested memberCode={}, userId={}", 
                    tokenMemberCode, memCode, userId);
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }
        
        // Fetch membership data from database
        Optional<MembershipModel> membershipOpt = membershipRepository.findByMemberCode(memCode);
        
        if (membershipOpt.isEmpty()) {
            log.error("MemberCode not found in membership table: {}", memCode);
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", "");
            result.put("memType", "");
            return result;
        }
        
        MembershipModel membership = membershipOpt.get();
        String memberCode = membership.getMemberCode();
        String accountCode = membership.getAccountCode();
        String memType = membership.getMemType();
        
        if (memberCode == null) {
            log.error("Member Code not found in the membership record. {}", membership);
            result.put("validate", Boolean.FALSE);
            result.put("memberCode", "");
            result.put("accountCode", accountCode != null ? accountCode : "");
            result.put("memType", memType != null ? memType : "");
            return result;
        }
        
        // Validate memberCode matches
        boolean isValid = memCode.equalsIgnoreCase(memberCode);
        
        result.put("memberCode", memberCode);
        result.put("accountCode", accountCode != null ? accountCode : "");
        result.put("memType", memType != null ? memType : "");
        result.put("validate", isValid);
        
        if (isValid) {
            log.info("MemberCode validation successful: userId={}, memberCode={}, accountCode={}, memType={}", 
                    userId, memberCode, accountCode, memType);
        } else {
            log.error("SECURITY ALERT: MemberCode validation failed! userId={}, requested={}, found={}", 
                    userId, memCode, memberCode);
        }
        
        return result;
    }
}

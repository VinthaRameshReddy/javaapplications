package com.medgo.reimburse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.reimburse.domain.dto.MemberData;
import com.medgo.reimburse.domain.dto.ReimbursementResubmitRequest;
import com.medgo.reimburse.domain.dto.ReimbursementSubmissionRequest;
import com.medgo.reimburse.domain.entity.reimb.ReimIpInfo;
import com.medgo.reimburse.domain.entity.reimb.ReimIpReceiving;
import com.medgo.reimburse.domain.entity.reimb.ReimOpInfoEntries;
import com.medgo.reimburse.domain.entity.reimb.ReimOpReceiving;
import com.medgo.reimburse.feign.MembershipServiceClient;
import com.medgo.reimburse.repository.reimb.ReimIpInfoRepository;
import com.medgo.reimburse.repository.reimb.ReimIpReceivingRepository;
import com.medgo.reimburse.repository.reimb.ReimOpInfoEntriesRepository;
import com.medgo.reimburse.repository.reimb.ReimOpReceivingRepository;
import com.medgo.reimburse.repository.reimb.ReimUtilRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReimDbService {

    private final ReimOpReceivingRepository reimOpReceivingRepository;
    private final ReimIpReceivingRepository reimIpReceivingRepository;
    private final ReimOpInfoEntriesRepository reimOpInfoEntriesRepository;
    private final ReimIpInfoRepository reimIpInfoRepository;
    private final ReimUtilRepository reimUtilRepository;
    private final MembershipServiceClient membershipServiceClient;
    private final ObjectMapper objectMapper;

    private NameParts parsePatientName(String patientName) {
        if (patientName == null || patientName.trim().isEmpty()) {
            return new NameParts("", "", "");
        }

        String trimmed = patientName.trim();
        String[] parts = trimmed.split("\\s+");

        if (parts.length == 1) {
            return new NameParts(parts[0], "", "");
        } else if (parts.length == 2) {
            return new NameParts(parts[0], "", parts[1]);
        } else {
            String firstName = parts[0];
            String lastName = parts[parts.length - 1];
            StringBuilder middleName = new StringBuilder();
            for (int i = 1; i < parts.length - 1; i++) {
                if (middleName.length() > 0) {
                    middleName.append(" ");
                }
                middleName.append(parts[i]);
            }
            return new NameParts(firstName, middleName.toString(), lastName);
        }
    }

    private String getEntryByFromPatientName(NameParts nameParts) {
        String firstName = nameParts != null && nameParts.firstName != null 
                ? nameParts.firstName.trim() 
                : null;
        
        if (firstName == null || firstName.isEmpty()) {
            return "SYSTEM";
        }
        
        if (firstName.length() > 25) {
            return firstName.substring(0, 25);
        }
        
        return firstName;
    }

    private String getEntryByFromPatientName(String patientName) {
        if (patientName == null || patientName.trim().isEmpty()) {
            return "SYSTEM";
        }
        
        NameParts nameParts = parsePatientName(patientName);
        String firstName = nameParts.firstName != null ? nameParts.firstName.trim() : null;
        
        if (firstName == null || firstName.isEmpty()) {
            return "SYSTEM";
        }
        
        if (firstName.length() > 25) {
            return firstName.substring(0, 25);
        }
        
        return firstName;
    }

    public Float calculateAge(LocalDate birthDate, LocalDate availmentDate) {
        if (birthDate == null || availmentDate == null) {
            return null;
        }
        try {
            Period period = Period.between(birthDate, availmentDate);
            float years = period.getYears() + (period.getMonths() / 12.0f) + (period.getDays() / 365.0f);
            return years;
        } catch (Exception e) {
            log.warn("Failed to calculate age from birthDate: {} and availmentDate: {}", birthDate, availmentDate, e);
            return null;
        }
    }

    public MemberData fetchMemberData(String memberCode) {
        try {
            log.debug("Fetching member data for memberCode: {}", memberCode);
            Object responseObj = membershipServiceClient.findMemberByCode(memberCode, null);

            if (!(responseObj instanceof Map)) {
                log.warn("Membership service returned unexpected response for memberCode: {}", memberCode);
                return MemberData.builder().build();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) responseObj;
            Object data = responseMap.get("data");

            if (data == null) {
                log.warn("Membership service returned null data for memberCode: {}", memberCode);
                return MemberData.builder().build();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (data instanceof Map)
                    ? (Map<String, Object>) data
                    : objectMapper.convertValue(data, Map.class);
            
            String principalCode = getStringValue(dataMap, "principalCode");
            String accountCode = getStringValue(dataMap, "accountCode");
            String accountName = getStringValue(dataMap, "accountName");
            String memTypeString = getStringValue(dataMap, "memType");
            
            Float age = null;
            Object ageObj = dataMap.get("age");
            if (ageObj != null) {
                try {
                    if (ageObj instanceof Number) {
                        age = ((Number) ageObj).floatValue();
                    } else if (ageObj instanceof String) {
                        String ageStr = ((String) ageObj).trim();
                        if (!ageStr.isEmpty()) {
                            age = Float.parseFloat(ageStr);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse age from membership response: {}", ageObj, e);
                }
            }
            
            LocalDate birthDate = null;
            Object birthDateObj = dataMap.get("birthDate");
            if (birthDateObj == null) {
                birthDateObj = dataMap.get("memBday");
            }
            if (birthDateObj == null) {
                birthDateObj = dataMap.get("mem_bday");
            }
            
            if (birthDateObj != null) {
                if (birthDateObj instanceof LocalDateTime) {
                    birthDate = ((LocalDateTime) birthDateObj).toLocalDate();
                } else if (birthDateObj instanceof String) {
                    try {
                        String birthDateStr = (String) birthDateObj;
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(birthDateStr);
                            birthDate = dateTime.toLocalDate();
                        } catch (Exception e) {
                            birthDate = LocalDate.parse(birthDateStr);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse birthDate/mem_bday: {}", birthDateObj, e);
                    }
                }
            }
            
            Short memType = convertMemTypeToShort(memTypeString, memberCode, principalCode);
            
            log.debug("Fetched member data - principalCode: {}, companyCode: {}, accountName: {}, memType: {}, age: {}", 
                    principalCode, accountCode, accountName, memType, age);
            
            return MemberData.builder()
                    .principalCode(principalCode)
                    .companyCode(accountCode)
                    .accountName(accountName)
                    .birthDate(birthDate)
                    .age(age)
                    .memType(memType)
                    .memTypeString(memTypeString)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to fetch member data for memberCode: {}", memberCode, e);
            return MemberData.builder().build();
        }
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Short convertMemTypeToShort(String memTypeString, String memberCode, String principalCode) {
        if (principalCode == null || principalCode.equals(memberCode)) {
            return 0;
        }
        
        if (memTypeString != null && memTypeString.toLowerCase().contains("extended")) {
            return 2;
        }
        
        return 1;
    }

    private LocalDateTime calculateDueDate(LocalDateTime receivedDate) {
        if (receivedDate == null) {
            return null;
        }
        return receivedDate.plusDays(15);
    }

    public Integer getParticularsCode(String natureOfClaim) {
        if (natureOfClaim == null || natureOfClaim.trim().isEmpty()) {
            return null;
        }
        try {
            Integer code = reimUtilRepository.getParticularsCode(natureOfClaim);
            // If lookup fails (table doesn't exist or no match), return null
            // The save methods should handle null by either using a default or allowing null
            if (code == null) {
                log.debug("Particulars code lookup returned null for natureOfClaim: {}. Will use null/default in save operation.", natureOfClaim);
            }
            return code;
        } catch (Exception e) {
            log.warn("Failed to get particulars_code for natureOfClaim: {}. Will use null/default.", natureOfClaim, e);
            return null;
        }
    }

    public Integer getCostplusCode(String companyCode) {
        if (companyCode == null || companyCode.isBlank()) {
            return null;
        }
        return reimUtilRepository.getCostplusCode(companyCode);
    }

    public long getMaxControlCodeSequence(String codePrefix) {
        try {
            long maxOp = reimOpReceivingRepository.findAll().stream()
                    .filter(r -> r.getControlCode() != null && r.getControlCode().startsWith(codePrefix))
                    .mapToLong(r -> {
                        try {
                            String code = r.getControlCode();
                            if (code.length() > codePrefix.length()) {
                                String seqPart = code.substring(codePrefix.length());
                                return Long.parseLong(seqPart);
                            }
                            return 0;
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            
            long maxIp = reimIpReceivingRepository.findAll().stream()
                    .filter(r -> r.getControlCode() != null && r.getControlCode().startsWith(codePrefix))
                    .mapToLong(r -> {
                        try {
                            String code = r.getControlCode();
                            if (code.length() > codePrefix.length()) {
                                String seqPart = code.substring(codePrefix.length());
                                return Long.parseLong(seqPart);
                            }
                            return 0;
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            
            return Math.max(maxOp, maxIp);
        } catch (Exception e) {
            log.warn("Failed to get max control code sequence from ReimDB, using 0", e);
            return 0;
        }
    }

    public long getMaxEntryCodeSequence(String codePrefix) {
        return 0;
    }

    public void saveOpReceiving(String controlCode, String entryCode, ReimbursementSubmissionRequest request) {
        try {
            if (request.getMemberCode() == null || request.getMemberCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Member code is required and cannot be null or empty");
            }
            
            if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()) {
                throw new IllegalArgumentException("Patient name is required and cannot be null or empty");
            }
            
            NameParts nameParts = parsePatientName(request.getPatientName());
            
            if (nameParts.lastName == null || nameParts.lastName.trim().isEmpty()) {
                log.warn("Last name is empty for patient: {}, using 'UNKNOWN' as fallback", request.getPatientName());
                nameParts.lastName = "UNKNOWN";
            }
            if (nameParts.firstName == null || nameParts.firstName.trim().isEmpty()) {
                log.warn("First name is empty for patient: {}, using 'UNKNOWN' as fallback", request.getPatientName());
                nameParts.firstName = "UNKNOWN";
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueDate = calculateDueDate(now);
            
            MemberData memberData = fetchMemberData(request.getMemberCode());
            String principalCode = memberData.getPrincipalCode();
            String companyCode = memberData.getCompanyCode();
            Float memAge = memberData.getAge();
            Short memType = memberData.getMemType();
            
            Integer particularsCode = getParticularsCode(request.getNatureOfClaim());
            String entryBy = getEntryByFromPatientName(nameParts);

            ReimOpReceiving opReceiving = ReimOpReceiving.builder()
                    .controlCode(controlCode)
                    .receivedDate(now)
                    .orgReceivedDate(now)
                    .memberCode(request.getMemberCode())
                    .memLname(nameParts.lastName.trim())
                    .memFname(nameParts.firstName.trim())
                    .memMname(nameParts.middleName != null && !nameParts.middleName.trim().isEmpty() 
                            ? nameParts.middleName.trim() 
                            : null)
                    .memAge(memAge)
                    .memType(memType)
                    .prinCode(principalCode)
                    .companyCode(companyCode)
                    .costplusCode(null)
                    .particularsCode(particularsCode)
                    .visitDate(request.getDateOfTreatment() != null 
                            ? request.getDateOfTreatment().atStartOfDay() 
                            : (request.getAvailmentDate() != null 
                                    ? request.getAvailmentDate().atStartOfDay() 
                                    : now))
                    .saAmount(request.getTotalClaimAmount() != null 
                            ? request.getTotalClaimAmount() 
                            : BigDecimal.ZERO)
                    .dueDate(dueDate)
                    .orgDueDate(dueDate)
                    .advancePayment(null)
                    .bank(request.getBankName() != null && request.getBankName().length() > 15 
                            ? request.getBankName().substring(0, 15) 
                            : request.getBankName())
                    .checkNo(null)
                    .checkDate(null)
                    .checkReleasedDate(null)
                    .complied(false)
                    .forVerify(null)
                    .verificationCode(null)
                    .processed(false)
                    .entryBy(entryBy)
                    .entryDate(now)
                    .updatedBy(null)
                    .updatedDate(null)
                    .build();

            log.debug("Attempting to save outpatient reimbursement to ReimDB - Control Code: {}, Member: {}, Patient: {}", 
                    controlCode, request.getMemberCode(), request.getPatientName());
            
            reimOpReceivingRepository.save(opReceiving);
            reimOpReceivingRepository.flush();
            
            saveOpInfoEntries(controlCode, entryCode, request, memberData, nameParts, entryBy, now);
            
            log.info("Successfully saved outpatient reimbursement to ReimDB - Control Code: {}, Entry Code: {}, Member: {}", 
                    controlCode, entryCode, request.getMemberCode());
        } catch (IllegalArgumentException e) {
            log.error("Validation error while saving outpatient reimbursement to ReimDB - Control Code: {}, Error: {}", 
                    controlCode, e.getMessage());
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Database constraint violation while saving outpatient reimbursement to ReimDB - Control Code: {}, " +
                    "Member: {}, Patient: {}. Error: {}", 
                    controlCode, request.getMemberCode(), request.getPatientName(), e.getMessage(), e);
            throw new RuntimeException("Database constraint violation: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to save outpatient reimbursement to ReimDB - Control Code: {}, Member: {}, Patient: {}. " +
                    "Error type: {}, Message: {}", 
                    controlCode, request.getMemberCode(), request.getPatientName(), 
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to save to ReimDB: " + e.getMessage(), e);
        }
    }

    public void saveIpReceiving(String controlCode, String entryCode, ReimbursementSubmissionRequest request) {
        try {
            NameParts nameParts = parsePatientName(request.getPatientName());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueDate = calculateDueDate(now);
            
            MemberData memberData = fetchMemberData(request.getMemberCode());
            String principalCode = memberData.getPrincipalCode();
            String companyCode = memberData.getCompanyCode();
            Float memAge = memberData.getAge();
            Short memType = memberData.getMemType();
            
            Integer particularsCode = getParticularsCode(request.getNatureOfClaim());

            ReimIpReceiving ipReceiving = ReimIpReceiving.builder()
                    .controlCode(controlCode)
                    .type("In-Patient")
                    .receivedDate(now)
                    .orgReceivedDate(now)
                    .memberCode(request.getMemberCode())
                    .memLname(nameParts.lastName)
                    .memFname(nameParts.firstName)
                    .memMname(nameParts.middleName.isEmpty() ? null : nameParts.middleName)
                    .memAge(memAge)
                    .memType(memType)
                    .prinCode(principalCode)
                    .companyCode(companyCode)
                    .costplusCode(null)
                    .particularsCode(particularsCode)
                    .confinedDate(request.getConfinementDate() != null 
                            ? request.getConfinementDate().atStartOfDay() 
                            : null)
                    .dischargedDate(request.getDischargeDate() != null 
                            ? request.getDischargeDate().atStartOfDay() 
                            : null)
                    .hospitalCode(request.getHospitalClinicName() != null && request.getHospitalClinicName().length() > 25
                            ? request.getHospitalClinicName().substring(0, 25)
                            : request.getHospitalClinicName())
                    .saAmount(request.getTotalClaimAmount() != null 
                            ? request.getTotalClaimAmount() 
                            : BigDecimal.ZERO)
                    .dueDate(dueDate)
                    .orgDueDate(dueDate)
                    .advancePayment(null)
                    .bank(request.getBankName() != null && request.getBankName().length() > 15 
                            ? request.getBankName().substring(0, 15) 
                            : request.getBankName())
                    .checkNo(null)
                    .checkDate(null)
                    .checkReleasedDate(null)
                    .complied(false)
                    .forVerify(null)
                    .verificationCode(null)
                    .processed(false)
                    .entryBy(getEntryByFromPatientName(nameParts))
                    .entryDate(now)
                    .updatedBy(null)
                    .updatedDate(null)
                    .build();

            reimIpReceivingRepository.save(ipReceiving);
            saveIpInfo(controlCode, request, memberData, nameParts, now);
            
            log.info("Saved inpatient reimbursement to ReimDB - Control Code: {}, Entry Code: {}", controlCode, entryCode);
        } catch (Exception e) {
            log.error("Failed to save inpatient reimbursement to ReimDB - Control Code: {}", controlCode, e);
            throw new RuntimeException("Failed to save to ReimDB", e);
        }
    }

    public void updateOpReceiving(String controlCode, String entryCode, ReimbursementResubmitRequest request) {
        try {
            ReimOpReceiving existing = reimOpReceivingRepository.findById(controlCode)
                    .orElseThrow(() -> new RuntimeException("ReimDB record not found for control code: " + controlCode));

            NameParts nameParts = parsePatientName(request.getPatientName());
            LocalDateTime now = LocalDateTime.now();

            existing.setMemberCode(request.getMemberCode());
            existing.setMemLname(nameParts.lastName);
            existing.setMemFname(nameParts.firstName);
            existing.setMemMname(nameParts.middleName.isEmpty() ? null : nameParts.middleName);
            existing.setVisitDate(request.getDateOfTreatment() != null 
                    ? request.getDateOfTreatment().atStartOfDay() 
                    : (request.getAvailmentDate() != null 
                            ? request.getAvailmentDate().atStartOfDay() 
                            : now));
            existing.setSaAmount(request.getTotalClaimAmount() != null 
                    ? request.getTotalClaimAmount() 
                    : BigDecimal.ZERO);
            existing.setBank(request.getBankName() != null && request.getBankName().length() > 15 
                    ? request.getBankName().substring(0, 15) 
                    : request.getBankName());
            existing.setCostplusCode(null);
            existing.setAdvancePayment(null);
            existing.setUpdatedBy(getEntryByFromPatientName(request.getPatientName()));
            existing.setUpdatedDate(now);

            reimOpReceivingRepository.save(existing);
            updateOpInfoEntries(controlCode, entryCode, request, nameParts, now);
            
            log.info("Updated outpatient reimbursement in ReimDB - Control Code: {}, Entry Code: {}", controlCode, entryCode);
        } catch (Exception e) {
            log.error("Failed to update outpatient reimbursement in ReimDB - Control Code: {}", controlCode, e);
            throw new RuntimeException("Failed to update ReimDB", e);
        }
    }

    public void updateIpReceiving(String controlCode, String entryCode, ReimbursementResubmitRequest request) {
        try {
            ReimIpReceiving existing = reimIpReceivingRepository.findById(controlCode)
                    .orElseThrow(() -> new RuntimeException("ReimDB record not found for control code: " + controlCode));

            NameParts nameParts = parsePatientName(request.getPatientName());
            LocalDateTime now = LocalDateTime.now();

            existing.setMemberCode(request.getMemberCode());
            existing.setMemLname(nameParts.lastName);
            existing.setMemFname(nameParts.firstName);
            existing.setMemMname(nameParts.middleName.isEmpty() ? null : nameParts.middleName);
            existing.setConfinedDate(request.getConfinementDate() != null 
                    ? request.getConfinementDate().atStartOfDay() 
                    : null);
            existing.setDischargedDate(request.getDischargeDate() != null 
                    ? request.getDischargeDate().atStartOfDay() 
                    : null);
            existing.setHospitalCode(request.getHospitalClinicName() != null && request.getHospitalClinicName().length() > 25
                    ? request.getHospitalClinicName().substring(0, 25)
                    : request.getHospitalClinicName());
            existing.setSaAmount(request.getTotalClaimAmount() != null 
                    ? request.getTotalClaimAmount() 
                    : BigDecimal.ZERO);
            existing.setBank(request.getBankName() != null && request.getBankName().length() > 15 
                    ? request.getBankName().substring(0, 15) 
                    : request.getBankName());
            existing.setCostplusCode(null);
            existing.setAdvancePayment(null);
            existing.setUpdatedBy(getEntryByFromPatientName(request.getPatientName()));
            existing.setUpdatedDate(now);

            reimIpReceivingRepository.save(existing);
            updateIpInfo(controlCode, request, nameParts, now);
            
            log.info("Updated inpatient reimbursement in ReimDB - Control Code: {}, Entry Code: {}", controlCode, entryCode);
        } catch (Exception e) {
            log.error("Failed to update inpatient reimbursement in ReimDB - Control Code: {}", controlCode, e);
            throw new RuntimeException("Failed to update ReimDB", e);
        }
    }

    private void saveOpInfoEntries(String controlCode, String entryCode, ReimbursementSubmissionRequest request,
                                   MemberData memberData, NameParts nameParts, String entryBy, LocalDateTime now) {
        try {
            if (memberData.getCompanyCode() == null || memberData.getCompanyCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Company code is required and cannot be null or empty");
            }
            
            Integer memStatus = 0;
            String companyCode = memberData.getCompanyCode() != null ? memberData.getCompanyCode() : "";
            Float memAge = memberData.getAge() != null ? memberData.getAge() : 0.0f;
            Short memType = memberData.getMemType() != null ? memberData.getMemType() : (short) 0;
            
            ReimOpInfoEntries opInfoEntries = ReimOpInfoEntries.builder()
                    .entryCode(entryCode)
                    .controlCode(controlCode)
                    .firstEntry(true)
                    .memberCode(request.getMemberCode())
                    .memLname(nameParts.lastName.trim())
                    .memFname(nameParts.firstName.trim())
                    .memMname(nameParts.middleName != null && !nameParts.middleName.trim().isEmpty() 
                            ? nameParts.middleName.trim() 
                            : null)
                    .memAge(memAge)
                    .memType(memType)
                    .memStatus(memStatus)
                    .memEffectivity(null)
                    .memValidity(null)
                    .prinCode(memberData.getPrincipalCode())
                    .companyCode(companyCode)
                    .planDesc(null)
                    .visitDate(request.getDateOfTreatment() != null 
                            ? request.getDateOfTreatment().atStartOfDay() 
                            : (request.getAvailmentDate() != null 
                                    ? request.getAvailmentDate().atStartOfDay() 
                                    : now))
                    .arsNo(null)
                    .firstLife(false)
                    .hospitalCode(request.getHospitalClinicName())
                    .isDoctor(false)
                    .docDentistCode(null)
                    .diagCode("5000")
                    .limit(null)
                    .type(null)
                    .procType(null)
                    .dxRemarks(null)
                    .specialRemarks(null)
                    .classificationCode(null)
                    .contractCoverage(null)
                    .disapprovedRemarks(null)
                    .inquiryRptId(null)
                    .extractedBy(null)
                    .extractedDate(null)
                    .entryBy(entryBy)
                    .entryDate(now)
                    .updatedBy(entryBy)
                    .updatedDate(now)
                    .build();

            reimOpInfoEntriesRepository.save(opInfoEntries);
            log.info("Successfully saved to reim_op_info_entries - Entry Code: {}, Control Code: {}", entryCode, controlCode);
        } catch (Exception e) {
            log.error("Failed to save to reim_op_info_entries - Entry Code: {}, Control Code: {}", entryCode, controlCode, e);
            throw new RuntimeException("Failed to save to reim_op_info_entries: " + e.getMessage(), e);
        }
    }

    private void saveIpInfo(String controlCode, ReimbursementSubmissionRequest request,
                           MemberData memberData, NameParts nameParts, LocalDateTime now) {
        try {
            String entryBy = getEntryByFromPatientName(nameParts);
            
            if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()) {
                throw new IllegalArgumentException("Patient name is required for reim_ip_info");
            }
            
            ReimIpInfo ipInfo = ReimIpInfo.builder()
                    .controlCode(controlCode)
                    .payeeType((short) 0)
                    .otherPayee(request.getPatientName())
                    .targetDate(request.getConfinementDate() != null 
                            ? request.getConfinementDate().atStartOfDay() 
                            : null)
                    .arsNo(null)
                    .admissionType(null)
                    .planDesc(null)
                    .pecLimit(null)
                    .remLimit(null)
                    .planAvailed(null)
                    .roomRate(null)
                    .noDays(null)
                    .planAvailed1(null)
                    .roomRate1(null)
                    .noDays1(null)
                    .planAvailed2(null)
                    .roomRate2(null)
                    .noDays2(null)
                    .planAvailed3(null)
                    .roomRate3(null)
                    .noDays3(null)
                    .diagnosis(null)
                    .type(null)
                    .limit(null)
                    .procType(null)
                    .otherDiagnosis(null)
                    .specialRemarks(null)
                    .classificationCode(null)
                    .contractCoverage(null)
                    .disapprovedRemarks(null)
                    .complied(null)
                    .compliedDate(null)
                    .compliedDueDate(null)
                    .compliedTargetDate(null)
                    .compliedRemarks(null)
                    .rmdStatus(null)
                    .rmdHoldRem(null)
                    .rmdLapseRem(null)
                    .rmdComplied(null)
                    .rmdCompliedDate(null)
                    .memoType((byte) 0)
                    .actionMemoId(null)
                    .disapprovedMemoId(null)
                    .emptyMemoId(null)
                    .whbMemoId(null)
                    .inquiryRptId(null)
                    .incidentRptId(null)
                    .processingStatusId(null)
                    .extractedBy(null)
                    .extractedDate(null)
                    .entryBy(entryBy)
                    .entryDate(now)
                    .updatedBy(null)
                    .updatedDate(null)
                    .build();

            reimIpInfoRepository.save(ipInfo);
            log.info("Successfully saved to reim_ip_info - Control Code: {}", controlCode);
        } catch (Exception e) {
            log.error("Failed to save to reim_ip_info - Control Code: {}", controlCode, e);
            throw new RuntimeException("Failed to save to reim_ip_info: " + e.getMessage(), e);
        }
    }

    private void updateOpInfoEntries(String controlCode, String entryCode, ReimbursementResubmitRequest request,
                                    NameParts nameParts, LocalDateTime now) {
        try {
            ReimOpInfoEntries existing = reimOpInfoEntriesRepository.findById(entryCode)
                    .orElse(null);
            
            if (existing == null) {
                log.warn("reim_op_info_entries record not found for entry code: {}, skipping update", entryCode);
                return;
            }

            existing.setMemberCode(request.getMemberCode());
            existing.setMemLname(nameParts.lastName.trim());
            existing.setMemFname(nameParts.firstName.trim());
            existing.setMemMname(nameParts.middleName != null && !nameParts.middleName.trim().isEmpty() 
                    ? nameParts.middleName.trim() 
                    : null);
            existing.setVisitDate(request.getDateOfTreatment() != null 
                    ? request.getDateOfTreatment().atStartOfDay() 
                    : (request.getAvailmentDate() != null 
                            ? request.getAvailmentDate().atStartOfDay() 
                            : now));
            existing.setHospitalCode(request.getHospitalClinicName());
            existing.setUpdatedBy(getEntryByFromPatientName(request.getPatientName()));
            existing.setUpdatedDate(now);

            reimOpInfoEntriesRepository.save(existing);
            log.info("Successfully updated reim_op_info_entries - Entry Code: {}, Control Code: {}", entryCode, controlCode);
        } catch (Exception e) {
            log.error("Failed to update reim_op_info_entries - Entry Code: {}, Control Code: {}", entryCode, controlCode, e);
            throw new RuntimeException("Failed to update reim_op_info_entries: " + e.getMessage(), e);
        }
    }

    private void updateIpInfo(String controlCode, ReimbursementResubmitRequest request,
                             NameParts nameParts, LocalDateTime now) {
        try {
            ReimIpInfo existing = reimIpInfoRepository.findById(controlCode)
                    .orElse(null);
            
            if (existing == null) {
                log.warn("reim_ip_info record not found for control code: {}, skipping update", controlCode);
                return;
            }

            existing.setOtherPayee(request.getPatientName());
            existing.setTargetDate(request.getConfinementDate() != null 
                    ? request.getConfinementDate().atStartOfDay() 
                    : null);
            existing.setUpdatedBy(getEntryByFromPatientName(request.getPatientName()));
            existing.setUpdatedDate(now);

            reimIpInfoRepository.save(existing);
            log.info("Successfully updated reim_ip_info - Control Code: {}", controlCode);
        } catch (Exception e) {
            log.error("Failed to update reim_ip_info - Control Code: {}", controlCode, e);
            throw new RuntimeException("Failed to update reim_ip_info: " + e.getMessage(), e);
        }
    }

    private static class NameParts {
        String firstName;
        String middleName;
        String lastName;

        NameParts(String firstName, String middleName, String lastName) {
            this.firstName = firstName != null ? firstName : "";
            this.middleName = middleName != null ? middleName : "";
            this.lastName = lastName != null ? lastName : "";
        }
    }
}

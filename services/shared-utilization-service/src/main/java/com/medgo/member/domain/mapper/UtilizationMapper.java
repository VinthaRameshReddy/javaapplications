package com.medgo.member.domain.mapper;




import com.medgo.member.domain.entity.utilization.UtilizationLegacyEntity;
import com.medgo.member.domain.response.UtilizationResponse;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;



public final class UtilizationMapper {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // For "2025-03-06T00:00:00"
            DateTimeFormatter.ISO_LOCAL_DATE,       // For "2025-03-06"
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),  // For SQL datetime strings
            DateTimeFormatter.ofPattern("MM/dd/yyyy")  // Alternative format if needed
    };
    private UtilizationMapper() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static UtilizationResponse mapToUtilizationResponse(Object[] row) {
        UtilizationResponse response = new UtilizationResponse();

        try {
            // Map each column from Object[] to UtilizationResponse
            response.setControlCode(getString(row[0]));                   // CONTROL_CODE
            response.setAvailFr(convertToLocalDate(row[1]));             // AVAIL_FR
            response.setAvailTo(convertToLocalDate(row[2]));             // AVAIL_TO (nullable)
            response.setDiagDesc(getString(row[3]));                     // DIAG_DESC
            response.setDxRem(getString(row[4]));                        // DX_REM
            response.setHospitalName(getString(row[5]));                 // HOSPITAL_NAME
            response.setDoctorName(getString(row[6]));                   // DOCTOR_NAME
            response.setApproved(toBigDecimal(row[7]));                  // APPROVED
            response.setDisapproved(toBigDecimal(row[8]));               // DISAPPROVED
            response.setAdvances(toBigDecimal(row[9]));                  // ADVANCES
            response.setErc(toBigDecimal(row[10]));                      // ERC
            response.setMemcode(getString(row[11]));                     // MEMCODE
            response.setPatient(getString(row[12]));                     // PATIENT
            response.setCompany(getString(row[13]));                     // COMPANY
            response.setPeriodFr(convertToLocalDate(row[14]));          // PERIOD_FR
            response.setPeriodTo(convertToLocalDate(row[15]));          // PERIOD_TO
            response.setPrintedBy(getString(row[16]));                   // PRINTED_BY
            response.setBillcode(getString(row[17]));                    // BILLCODE
            response.setMedicareIncentives(toBigDecimal(row[18]));      // MEDICARE_INCENTIVES
            response.setReimReason(getString(row[19]));                  // REIM_REASON
            response.setUpdatedBy(getString(row[20]));                   // UPDATED_BY
            response.setUpdatedDate(convertToLocalDateTime(row[21]));   // UPDATED_DATE
            response.setValid(convertToLocalDate(row[22]));             // VALID
            response.setEffective(convertToLocalDate(row[23]));         // EFFECTIVE
            response.setHospSoa(getString(row[24]));                    // HOSP_SOA
            response.setIcd10Code(getString(row[25]));                  // ICD10_CODE
            response.setIcd10Desc(getString(row[26]));                  // ICD10_DESC
            response.setRemarks2(getString(row[27]));                   // REMARKS2
            response.setChecknum(getString(row[28]));                   // CHECKNUM
            response.setPf(getString(row[29]));                         // PF
            response.setRcvdBy(getString(row[30]));                     // RCVD_BY
            response.setRcvdDate(convertToLocalDate(row[31]));          // RCVD_DATE
            response.setDepname(getString(row[32]));                    // DEPNAME
            response.setDepcode(getString(row[33]));                    // DEPCODE


        } catch (Exception e) {
            throw new RuntimeException("Error mapping row to UtilizationResponse at index "
                    + getErrorIndex(e) + ": " + e.getMessage(), e);
        }

        return response;
    }

    private static String getString(Object value) {
        return value != null ? value.toString() : null;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static LocalDate convertToLocalDate(Object value) {
        if (value == null) return null;

        // 🚨 Fix: Skip unexpected Character inputs
        if (value instanceof Character || (value instanceof String && ((String) value).trim().isEmpty())) {
            System.err.println("Skipping invalid date value: " + value);  // Debugging output
            return null;  // Ignore invalid Character or empty String values
        }

        // Handle Timestamp directly
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime().toLocalDate();
        }

        // Handle String dates
        if (value instanceof String) {
            String dateStr = ((String) value).trim();
            if (dateStr.isEmpty()) return null;

            // Try multiple date formats
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    if (formatter == DateTimeFormatter.ISO_LOCAL_DATE_TIME) {
                        return LocalDateTime.parse(dateStr, formatter).toLocalDate();
                    }
                    return LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException ignored) {
                    // Try next format
                }
            }
            throw new DateTimeParseException("Unparseable date: " + dateStr, dateStr, 0);
        }

        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to LocalDate");
    }

    public static LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;

        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }

        if (value instanceof String) {
            String dateStr = ((String) value).trim();
            if (dateStr.isEmpty()) return null;

            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return LocalDateTime.parse(dateStr, formatter);
                } catch (DateTimeParseException ignored) {
                    // Try next format
                }
            }
        }

        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to LocalDateTime");
    }

    private static int getErrorIndex(Exception e) {
        if (e instanceof DateTimeParseException) {
            String msg = e.getMessage();
            if (msg.contains("index")) {
                return Integer.parseInt(msg.split("index ")[1].split("\\D")[0]);
            }
        }
        return -1;
    }

    public static UtilizationResponse toDto(UtilizationLegacyEntity entity) {
        UtilizationResponse response = new UtilizationResponse();

        response.setControlCode(entity.getId().getControlCode());
        response.setAvailFr(entity.getAvailFr() != null ? entity.getAvailFr().toLocalDate() : null);
        response.setAvailTo(entity.getAvailTo() != null ? entity.getAvailTo().toLocalDate() : null);
        response.setDiagDesc(entity.getDiagDesc());
        response.setDxRem(entity.getDxRem());
        response.setHospitalName(entity.getHospitalName());
        response.setDoctorName(entity.getDoctorName());
        response.setApproved(BigDecimal.valueOf(entity.getApproved() != null ? entity.getApproved() : 0));
        response.setDisapproved(BigDecimal.valueOf(entity.getDisapproved() != null ? entity.getDisapproved() : 0));
        response.setAdvances(BigDecimal.valueOf(entity.getAdvances() != null ? entity.getAdvances() : 0));
        response.setErc(BigDecimal.valueOf(entity.getErc() != null ? entity.getErc() : 0));
        response.setMemcode(entity.getMemcode());
        response.setPatient(entity.getPatient());
        response.setCompany(entity.getCompany());
        response.setPeriodFr(entity.getPeriodFr() != null ? entity.getPeriodFr().toLocalDate() : null);
        response.setPeriodTo(entity.getPeriodTo() != null ? entity.getPeriodTo().toLocalDate() : null);
        response.setPrintedBy(entity.getPrintedBy());
        response.setBillcode(entity.getBillcode());
        response.setMedicareIncentives(BigDecimal.valueOf(entity.getMedicareIncentives() != null ? entity.
                getMedicareIncentives() : 0));
        response.setReimReason(entity.getReimReason());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getId().getUpdatedDate());
        response.setValid(entity.getValid() != null ? LocalDate.parse(entity.getValid()) : null);
        response.setEffective(entity.getEffective() != null ? LocalDate.parse(entity.getEffective()) : null);
        response.setHospSoa(entity.getHospSoa());
        response.setIcd10Code(entity.getIcd10code());
        response.setIcd10Desc(entity.getIcd10desc());
        response.setRemarks2(entity.getRemarks2());
        response.setChecknum(entity.getChecknum());
        response.setPf(entity.getPf());
        response.setRcvdBy(entity.getRcvdBy());
        response.setRcvdDate(entity.getRcvdDate() != null ? entity.getRcvdDate().toLocalDate() : null);
        response.setDepname(entity.getDepname());
        response.setDepcode(entity.getDepcode());
        return response;
    }


}
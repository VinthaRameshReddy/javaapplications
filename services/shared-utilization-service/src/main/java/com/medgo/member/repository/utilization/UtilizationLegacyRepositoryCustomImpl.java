package com.medgo.member.repository.utilization;


import com.medgo.member.domain.mapper.UtilizationMapper;
import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.domain.response.UtilizationResponse;
import com.medgo.member.repository.utilization.UtilizationLegacyRepositoryCustom;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UtilizationLegacyRepositoryCustomImpl implements UtilizationLegacyRepositoryCustom {


    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public List<UtilizationResponse> findUtilizationDataV6(UtilizationRequest request) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("CP_CLMS_REP_UTILIZATION_ALL_V6");


        query.registerStoredProcedureParameter(1, LocalDateTime.class, ParameterMode.IN); // DATE_FR
        query.registerStoredProcedureParameter(2, LocalDateTime.class, ParameterMode.IN); // DATE_TO
        query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);        // MEMCODE
        query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);        // LNAME
        query.registerStoredProcedureParameter(5, String.class, ParameterMode.IN);        // FNAME
        query.registerStoredProcedureParameter(6, String.class, ParameterMode.IN);        // MI
        query.registerStoredProcedureParameter(7, String.class, ParameterMode.IN);        // COMP
        query.registerStoredProcedureParameter(8, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(9, LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(10, LocalDateTime.class, ParameterMode.IN);



        // ✅ Debug print
        System.out.println("Stored Procedure: CP_CLMS_REP_UTILIZATION_ALL_V6");
        for (Parameter<?> param : query.getParameters()) {
            try {
                Object value = query.getParameterValue(param);
                System.out.println(param.getName() + " = " + value);
            } catch (Exception e) {
                System.out.println(param.getName() + " = (not set)");
            }
        }

        // Set parameter values (reuse your method, but it assumes registrations are done)
        setQueryParameters(query, request);

        // Execute and get results as Object[]
        query.execute();
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<UtilizationResponse> dtoList = new ArrayList<>();
        for (Object[] row : results) {
            UtilizationResponse dto = new UtilizationResponse();
//            log.debug("row: {}", row);
            dto.setControlCode((String) row[0]);
            dto.setAvailFr(UtilizationMapper.convertToLocalDate(row[1])); // Adjusted for LocalDate field
            dto.setAvailTo(UtilizationMapper.convertToLocalDate(row[2])); // Adjusted for LocalDate field
            dto.setDiagDesc((String) row[3]);
            dto.setDxRem((String) row[4]);
            dto.setHospitalName((String) row[5]);
            dto.setDoctorName((String) row[6]);
            dto.setApproved(row[7] != null ? BigDecimal.valueOf(((Number) row[7]).doubleValue()) : null);
            dto.setDisapproved(row[8] != null ? BigDecimal.valueOf(((Number) row[8]).doubleValue()) : null);
//            dto.setAdvances(row[9] != null ? BigDecimal.valueOf(((Number) row[9]).doubleValue()) : null);
//            dto.setErc(row[10] != null ? BigDecimal.valueOf(((Number) row[10]).doubleValue()) : null);
            dto.setMemcode((String) row[11]);
            dto.setPatient((String) row[12]);
            dto.setCompany((String) row[13]);
            dto.setPeriodFr(UtilizationMapper.convertToLocalDate(row[14]));
            dto.setPeriodTo(UtilizationMapper.convertToLocalDate(row[15]));
            dto.setPrintedBy((String) row[16]);
            dto.setBillcode((String) row[17]);
            dto.setMedicareIncentives(row[18] != null ? BigDecimal.valueOf(((Number) row[18]).doubleValue()) : null);
            dto.setReimReason((String) row[19]);
            dto.setUpdatedBy((String) row[20]);
            dto.setUpdatedDate(UtilizationMapper.convertToLocalDateTime(row[21]));
            dto.setValid(row[22] != null ? LocalDate.parse((String) row[22]) : null); // Parse string to LocalDate (adjust format if needed)
            dto.setEffective(row[23] != null ? LocalDate.parse((String) row[23]) : null); // Parse string to LocalDate (adjust format if needed)
//            dto.setHospSoa(row[24] != null ? row[24].toString() : null);
//            dto.setIcd10Code((String) row[25]);
//            dto.setIcd10Desc((String) row[26]);
//            dto.setRemarks2((String) row[27]);
//            dto.setChecknum((String) row[28]);
//            dto.setPf((String) row[29]);
//            dto.setRcvdBy((String) row[30]);
//            dto.setRcvdDate(UtilizationMapper.convertToLocalDate(row[31])); // Adjusted for LocalDate field
//            dto.setDepname((String) row[32]);
//            dto.setDepcode((String) row[33]);
//
            dtoList.add(dto);
        }
        return dtoList;
    }





    private void setQueryParameters(StoredProcedureQuery query, UtilizationRequest request) {
        // Set parameters by position
        query.setParameter(1, request.getDateFr());
        query.setParameter(2, request.getDateTo());
        query.setParameter(3, request.getMemcode());
        query.setParameter(4, request.getLname()==null?"":request.getLname());
        query.setParameter(5, request.getFname()==null?"":request.getFname());
        query.setParameter(6, request.getMi()==null?"":request.getMi());
        query.setParameter(7, request.getComp()==null?"":request.getComp());
        query.setParameter(8, request.getUser() == null?"": request.getUser());
        query.setParameter(9, request.getValDate()==null?"": request.getValDate());
        query.setParameter(10, request.getEffective()==null?"":request.getEffective());
    }


    private LocalDateTime convertToLocalDateTime(Object dateObject) {
        if (dateObject == null) {
            return null;
        }
        if (dateObject instanceof Timestamp) {
            return ((Timestamp) dateObject).toLocalDateTime();
        }
        if (dateObject instanceof Date) {
            return ((Date) dateObject).toLocalDate().atStartOfDay();
        }
        throw new IllegalArgumentException("Unsupported date type: " + dateObject.getClass());
    }


}







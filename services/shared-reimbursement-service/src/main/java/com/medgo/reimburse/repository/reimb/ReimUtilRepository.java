package com.medgo.reimburse.repository.reimb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
public class ReimUtilRepository {

    @PersistenceContext(unitName = "reimbPU")
    private EntityManager entityManager;

    public Integer getCostplusCode(String companyCode) {
        try {
            if (companyCode == null || companyCode.trim().isEmpty()) {
                return null;
            }
            
            Query query = entityManager.createNativeQuery(
                    "SELECT REC_ID FROM reim_util_costplus WHERE company_code = :companyCode"
            );
            query.setParameter("companyCode", companyCode);
            
            @SuppressWarnings("unchecked")
            List<Object> results = query.getResultList();
            
            if (results != null && !results.isEmpty()) {
                Object result = results.get(0);
                if (result != null) {
                    return ((Number) result).intValue();
                }
            }
            
            log.debug("No costplus_code found for company_code: {}", companyCode);
            return null;
        } catch (Exception e) {
            log.warn("Failed to get costplus_code for company_code: {}", companyCode, e);
            return null;
        }
    }

    /**
     * Get particulars code from lookup table.
     * Uses REQUIRES_NEW propagation to prevent SQLGrammarException from marking
     * the main transaction as rollback-only if the table doesn't exist.
     * This method runs in a separate transaction that won't affect the main ReimDB transaction.
     */
    @Transactional(
            transactionManager = "reimbTransactionManager",
            propagation = Propagation.REQUIRES_NEW, 
            readOnly = true, 
            noRollbackFor = {org.hibernate.exception.SQLGrammarException.class, 
                           jakarta.persistence.PersistenceException.class,
                           org.springframework.dao.DataAccessException.class})
    public Integer getParticularsCode(String particularsDesc) {
        try {
            if (particularsDesc == null || particularsDesc.trim().isEmpty()) {
                return null;
            }
            
            Query query = entityManager.createNativeQuery(
                    "SELECT particulars_code FROM reim_lib_particulars WHERE particulars_desc = :particularsDesc"
            );
            query.setParameter("particularsDesc", particularsDesc);
            
            @SuppressWarnings("unchecked")
            List<Object> results = query.getResultList();
            
            if (results != null && !results.isEmpty()) {
                Object result = results.get(0);
                if (result != null) {
                    return ((Number) result).intValue();
                }
            }
            
            log.debug("No particulars_code found for particulars_desc: {}", particularsDesc);
            return null;
        } catch (org.hibernate.exception.SQLGrammarException e) {
            // Table doesn't exist - use fallback/default
            log.warn("Table 'reim_lib_particulars' does not exist or is inaccessible. Using default particulars_code for particulars_desc: {}", particularsDesc);
            return null; // Return null, caller should handle with default
        } catch (jakarta.persistence.PersistenceException e) {
            // Handle other persistence exceptions
            Throwable cause = e.getCause();
            if (cause instanceof org.hibernate.exception.SQLGrammarException) {
                log.warn("SQL grammar error accessing reim_lib_particulars table. Using default for particulars_desc: {}", particularsDesc);
                return null;
            }
            log.warn("Failed to get particulars_code for particulars_desc: {}", particularsDesc, e);
            return null;
        } catch (Exception e) {
            log.warn("Failed to get particulars_code for particulars_desc: {}", particularsDesc, e);
            return null;
        }
    }
}

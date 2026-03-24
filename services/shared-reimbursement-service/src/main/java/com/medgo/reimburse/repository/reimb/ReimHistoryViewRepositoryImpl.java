package com.medgo.reimburse.repository.reimb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReimHistoryViewRepositoryImpl implements ReimHistoryViewRepository {

    @PersistenceContext(unitName = "reimbPU")
    private EntityManager em;




    @Override
    public List<Object[]> findByMemberCode(String memberCode) {
        return em.createNativeQuery("""
            SELECT 
                SERVICE_TYPE,
                CHECK_NO,
                CONTROL_CODE,
                ENTRY_CODE,
                MEMBER_CODE,
                MEM_LNAME,
                MEM_FNAME,
                MEM_MNAME,
                HOLD_REMARKS,
                LAPSE_PAYMENT_REMARKS,
                FOR_RMD_APPROVAL,
                REQUESTED_AMOUNT,
                APPROVED_AMOUNT,
                AVAILMENT_DATE,
                RECEIVED_DATE,
                ENTRY_DATE,
                CLAIM_NATURE,
                ACTION_MEMO_REMARKS,
                DENIED_MEMO_REMARKS,
                WHB_MEMO_REMARKS,
                ACTION_MEMO,
                DENIED_MEMO,
                WHB_MEMO,
                REIMB_STATUS
            FROM Reim_VW_MEDGO2_History_Combined
            WHERE MEMBER_CODE = :memberCode
            ORDER BY ENTRY_DATE DESC, AVAILMENT_DATE DESC
        """)
                .setParameter("memberCode", memberCode)
                .getResultList();
    }
    
    @Override
    public List<Object[]> findByControlCodeAndStatus(String controlCode, String status) {
        String trimmedStatus = (status != null) ? status.trim().toUpperCase() : null;
        
        return em.createNativeQuery("""
            SELECT 
                SERVICE_TYPE,
                CHECK_NO,
                CONTROL_CODE,
                ENTRY_CODE,
                MEMBER_CODE,
                MEM_LNAME,
                MEM_FNAME,
                MEM_MNAME,
                HOLD_REMARKS,
                LAPSE_PAYMENT_REMARKS,
                FOR_RMD_APPROVAL,
                REQUESTED_AMOUNT,
                APPROVED_AMOUNT,
                AVAILMENT_DATE,
                RECEIVED_DATE,
                ENTRY_DATE,
                CLAIM_NATURE,
                ACTION_MEMO_REMARKS,
                DENIED_MEMO_REMARKS,
                WHB_MEMO_REMARKS,
                ACTION_MEMO,
                DENIED_MEMO,
                WHB_MEMO,
                REIMB_STATUS
            FROM Reim_VW_MEDGO2_History_Combined WITH (NOLOCK)
            WHERE CONTROL_CODE = :controlCode
                AND UPPER(REIMB_STATUS) = :status
            ORDER BY ENTRY_DATE DESC, AVAILMENT_DATE DESC
        """)
                .setParameter("controlCode", controlCode)
                .setParameter("status", trimmedStatus)
                .getResultList();
    }
    
    @Override
    public List<Object[]> findByControlCodeAndStatusAndEntryCode(String controlCode, String status, String entryCode) {
        String trimmedStatus = (status != null) ? status.trim().toUpperCase() : null;
        
        return em.createNativeQuery("""
            SELECT 
                SERVICE_TYPE,
                CHECK_NO,
                CONTROL_CODE,
                ENTRY_CODE,
                MEMBER_CODE,
                MEM_LNAME,
                MEM_FNAME,
                MEM_MNAME,
                HOLD_REMARKS,
                LAPSE_PAYMENT_REMARKS,
                FOR_RMD_APPROVAL,
                REQUESTED_AMOUNT,
                APPROVED_AMOUNT,
                AVAILMENT_DATE,
                RECEIVED_DATE,
                ENTRY_DATE,
                CLAIM_NATURE,
                ACTION_MEMO_REMARKS,
                DENIED_MEMO_REMARKS,
                WHB_MEMO_REMARKS,
                ACTION_MEMO,
                DENIED_MEMO,
                WHB_MEMO,
                REIMB_STATUS
            FROM Reim_VW_MEDGO2_History_Combined WITH (NOLOCK)
            WHERE CONTROL_CODE = :controlCode
                AND ENTRY_CODE = :entryCode
                AND UPPER(REIMB_STATUS) = :status
            ORDER BY ENTRY_DATE DESC, AVAILMENT_DATE DESC
        """)
                .setParameter("controlCode", controlCode)
                .setParameter("status", trimmedStatus)
                .setParameter("entryCode", entryCode)
                .getResultList();
    }
}

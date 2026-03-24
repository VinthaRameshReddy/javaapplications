package com.medgo.reimburse.repository.reimb;

import java.util.List;

public interface ReimHistoryViewRepository {



    List<Object[]> findByMemberCode(String memberCode);
    
    List<Object[]> findByControlCodeAndStatus(String controlCode, String status);
    
    List<Object[]> findByControlCodeAndStatusAndEntryCode(String controlCode, String status, String entryCode);
}

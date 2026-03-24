package com.medgo.reimburse.repository.reimb;

import com.medgo.reimburse.domain.entity.reimb.ReimHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementHistoryRepository extends JpaRepository<ReimHistory, String> {
    List<ReimHistory> findAllByMemberCode(String memberCode);

}

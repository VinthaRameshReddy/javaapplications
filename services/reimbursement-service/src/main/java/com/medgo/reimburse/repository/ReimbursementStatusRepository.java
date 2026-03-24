package com.medgo.reimburse.repository;

import com.medgo.reimburse.domain.entity.ReimHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementStatusRepository extends JpaRepository<ReimHistory, String> {
    List<ReimHistory> findAllByMemberCode(String memberCode);

}

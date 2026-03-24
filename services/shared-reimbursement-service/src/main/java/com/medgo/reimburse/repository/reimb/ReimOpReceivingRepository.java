package com.medgo.reimburse.repository.reimb;

import com.medgo.reimburse.domain.entity.reimb.ReimOpReceiving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReimOpReceivingRepository extends JpaRepository<ReimOpReceiving, String> {
}

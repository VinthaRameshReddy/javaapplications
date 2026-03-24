package com.medgo.reimburse.repository.reimb;

import com.medgo.reimburse.domain.entity.reimb.ReimIpReceiving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReimIpReceivingRepository extends JpaRepository<ReimIpReceiving, String> {
}

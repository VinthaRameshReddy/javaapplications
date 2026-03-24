package com.medgo.facescan.repository.medgo;


import com.medgo.facescan.domain.models.medgo.StoreFaceScanResultModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaceScanResultRepository extends JpaRepository<StoreFaceScanResultModel, Long> {

    List<StoreFaceScanResultModel> findLatestScanByMemberCode(String memberCode);

    Optional<StoreFaceScanResultModel> findBySessionId(String sessionId);

    List<StoreFaceScanResultModel> findByMemberCodeAndScanResultOrderByEndTimeDesc(String memberCode, String success);
}
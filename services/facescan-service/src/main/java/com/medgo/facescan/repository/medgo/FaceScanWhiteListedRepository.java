package com.medgo.facescan.repository.medgo;



import com.medgo.facescan.domain.models.medgo.FaceScanWhiteListedModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FaceScanWhiteListedRepository extends JpaRepository<FaceScanWhiteListedModel, Long> {

    @Query("SELECT COUNT(*) FROM FaceScanWhiteListedModel w WHERE" +
            " w.accountCode = :accountCode AND (w.memberType IS NULL OR w.memberType= :memberType)" +
            " AND (w.startDate IS NULL OR w.startDate <= :currentDate )" +
            " AND (w.endDate IS NULL OR w.endDate >= :currentDate )"
    )
    Integer findActiveMember(@Param("accountCode") String accountCode,
                             @Param("memberType") Character memberType,
                             @Param("currentDate") LocalDateTime currentDate);
}
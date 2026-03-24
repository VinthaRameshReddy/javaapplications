package com.medgo.facescan.repository.medgo;



import com.medgo.facescan.domain.models.medgo.FaceScanSessionModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaceScanSessionRepository extends JpaRepository<FaceScanSessionModel, Long> {

    // Custom query example
    @Query("SELECT s FROM FaceScanSessionModel s WHERE s.memberCode = :memberCode ORDER BY s.sessionEndTime DESC")
    List<FaceScanSessionModel> findTop1ByMemberCode(@Param("memberCode") String memberCode,Pageable pageable);

    Optional<FaceScanSessionModel> findByFedId(String fedId);

    Optional<FaceScanSessionModel> findBySessionId(@NotBlank(message = "Session ID is required") String sessionId);
}


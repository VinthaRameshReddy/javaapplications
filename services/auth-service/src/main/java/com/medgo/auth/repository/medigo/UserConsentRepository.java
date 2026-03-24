package com.medgo.auth.repository.medigo;

import com.medgo.auth.domain.entity.medigo.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserConsentRepository extends JpaRepository<UserConsent, Integer> {
    
    List<UserConsent> findByUserId(String userId);
    
    Optional<UserConsent> findByUserIdAndConsentId(String userId, Integer consentId);
    
    @Query("SELECT uc.consentId FROM UserConsent uc WHERE uc.userId = :userId AND uc.agreed = 1")
    List<Integer> findAgreedConsentIdsByUserId(@Param("userId") String userId);

}


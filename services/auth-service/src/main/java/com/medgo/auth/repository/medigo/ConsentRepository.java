package com.medgo.auth.repository.medigo;

import com.medgo.auth.domain.entity.medigo.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, Integer> {
    
    @Query("SELECT c FROM Consent c WHERE c.enabled = 1 ORDER BY c.type, c.versionNo DESC")
    List<Consent> findAllEnabledConsents();
    
    @Query("SELECT c FROM Consent c WHERE c.enabled = 1 AND c.required = 1 ORDER BY c.type, c.versionNo DESC")
    List<Consent> findRequiredConsents();
}


package com.medgo.reimburse.repository.medigo;

import com.medgo.reimburse.domain.entity.medigo.ClaimNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClaimNatureRepository extends JpaRepository<ClaimNature, Integer> {
    Optional<ClaimNature> findByClassificationCode(String classificationCode);
}

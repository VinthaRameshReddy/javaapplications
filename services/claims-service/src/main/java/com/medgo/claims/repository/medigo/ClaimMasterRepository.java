package com.medgo.claims.repository.medigo;

import com.medgo.claims.domain.entity.medigo.ClaimNatureMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimMasterRepository extends JpaRepository<ClaimNatureMaster, Integer> {

    List<ClaimNatureMaster> findByServiceTypeOrderByIdAsc(String serviceType);
}
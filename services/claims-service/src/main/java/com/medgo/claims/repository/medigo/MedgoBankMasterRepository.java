package com.medgo.claims.repository.medigo;

import com.medgo.claims.domain.entity.medigo.MedgoBankMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedgoBankMasterRepository extends JpaRepository<MedgoBankMaster, Long> {

    List<MedgoBankMaster> findByEnabledTrueOrderByNameAsc();
}


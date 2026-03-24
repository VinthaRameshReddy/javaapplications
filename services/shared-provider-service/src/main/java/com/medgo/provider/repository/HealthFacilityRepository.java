package com.medgo.provider.repository;

import com.medgo.provider.domain.entity.HealthFacilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HealthFacilityRepository extends JpaRepository<HealthFacilityEntity, Long>, JpaSpecificationExecutor<HealthFacilityEntity> {
}
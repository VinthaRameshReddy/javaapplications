package com.medgo.provider.repository;


import com.medgo.provider.domain.entity.AffiliationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AffiliationViewRepository extends JpaRepository<AffiliationView, Long>, JpaSpecificationExecutor<AffiliationView> {
}
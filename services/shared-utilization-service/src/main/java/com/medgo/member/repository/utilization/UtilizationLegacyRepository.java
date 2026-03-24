package com.medgo.member.repository.utilization;



import com.medgo.member.domain.entity.utilization.UtilizationLegacyEntity;
import com.medgo.member.domain.entity.utilization.UtilizationLegacyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UtilizationLegacyRepository  extends JpaRepository<UtilizationLegacyEntity, UtilizationLegacyId>, UtilizationLegacyRepositoryCustom {




}

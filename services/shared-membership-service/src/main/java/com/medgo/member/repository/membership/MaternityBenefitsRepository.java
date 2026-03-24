package com.medgo.member.repository.membership;

import com.medgo.member.domain.entity.membership.MaternityBenefitsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MaternityBenefitsRepository extends JpaRepository<MaternityBenefitsEntity, Integer> {

    @Procedure(name = "SP.getMaternityBenefitsByModelEntity")
    List<MaternityBenefitsEntity> getMaternityBenefitsByModelEntity(@Param("ACCOUNTCODE") String accountCode);

}
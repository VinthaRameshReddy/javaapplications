package com.medgo.member.repository.membership;

import com.medgo.member.domain.entity.membership.DataPrivacyTaggingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataPrivacyTaggingRepository extends JpaRepository<DataPrivacyTaggingEntity, String> {
    boolean existsByMemberCode(String memberCode);
}
package com.medgo.auth.repository.medigo;

import com.medgo.auth.domain.entity.medigo.OtpBypassMemberCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpBypassMemberCodeRepository extends JpaRepository<OtpBypassMemberCode, Long> {
    Optional<OtpBypassMemberCode> findByMemberCodeAndIsActiveTrue(String memberCode);
}


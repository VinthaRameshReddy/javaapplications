package com.medgo.auth.repository.medigo;

import com.medgo.auth.domain.entity.medigo.InvalidBirthdateLogModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvalidBirthdateLogRepository extends JpaRepository<InvalidBirthdateLogModel, String> {

    Optional<InvalidBirthdateLogModel> findByMemberCode(String memberCode);
}

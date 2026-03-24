package com.medgo.reimburse.repository;

import com.medgo.reimburse.domain.entity.ReimVWMedgo2RequestDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReimVWMedgo2RequestDetailsRepository extends JpaRepository<ReimVWMedgo2RequestDetails, String> {

    Optional<ReimVWMedgo2RequestDetails> findByControlCodeAndEntryCodeAndMemberCode(String controlCode,
            String entryCode, String memberCode);
}

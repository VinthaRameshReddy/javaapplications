package com.medgo.reimburse.repository.reimb;

import com.medgo.reimburse.domain.entity.reimb.ReimVWMedgo2RequestDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReimRequestDetailsRepository extends JpaRepository<ReimVWMedgo2RequestDetails, String> {




    Optional<ReimVWMedgo2RequestDetails> findByMemberCodeAndControlCodeAndEntryCode(String memberCode, String controlCode, String entryCode);
    Optional<ReimVWMedgo2RequestDetails> findByMemberCodeAndControlCode(String memberCode, String controlCode);
}

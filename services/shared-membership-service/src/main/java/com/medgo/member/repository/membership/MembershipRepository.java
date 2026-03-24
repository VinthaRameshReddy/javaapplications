package com.medgo.member.repository.membership;

import com.medgo.member.domain.entity.membership.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {

    /**
     * Find a single membership record by member code.
     */
    MembershipEntity findByMemberCode(String memberCode);

    /**
     * Find all dependents linked to a principal code.
     */
    List<MembershipEntity> findByPrincipalCode(String principalCode);
}

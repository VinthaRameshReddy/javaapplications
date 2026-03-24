package com.medgo.auth.repository.membership;

import com.medgo.auth.domain.entity.membership.MembershipModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipModelRepository extends JpaRepository<MembershipModel, String> {

    Optional<MembershipModel> findByMemberCode(String memberCode);

}

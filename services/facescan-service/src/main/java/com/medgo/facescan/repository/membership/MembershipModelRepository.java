package com.medgo.facescan.repository.membership;

import com.medgo.facescan.domain.models.membership.MembershipModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MembershipModelRepository extends JpaRepository<MembershipModel, String> {

    @Query(value = "SELECT * FROM VW_MCAP_MEMBERSHIP WHERE PRIN_CODE = :memberCode", nativeQuery = true)
    Optional<MembershipModel> findByMemberCode(String memberCode);


}

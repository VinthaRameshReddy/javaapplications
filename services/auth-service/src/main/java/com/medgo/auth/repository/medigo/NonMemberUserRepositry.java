package com.medgo.auth.repository.medigo;

import com.medgo.auth.domain.entity.medigo.NonMemberUserModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NonMemberUserRepositry extends JpaRepository<NonMemberUserModel, Long> {
    Optional<NonMemberUserModel> findByEmail(@Email(message = "Invalid email format")
                                             @NotBlank(message = "Email is required") String email);

    Optional<NonMemberUserModel> findByEmailOrMobile(String emailId, String mobileNumber);

    Optional<NonMemberUserModel> findByMobile(String mobile);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM NonMemberUserModel u
            WHERE u.email = :email
              AND (:mobile IS NULL OR :mobile = '' OR u.mobile = :mobile)
            """)
    boolean existsByEmailAndOptionalMobile(String email, String mobile);

    @Query("""
            SELECT MAX(u.nonMemberCode)
            FROM NonMemberUserModel u
            WHERE u.nonMemberCode LIKE 'NM-%'
            """)
    Optional<String> findMaxNonMemberCode();

}

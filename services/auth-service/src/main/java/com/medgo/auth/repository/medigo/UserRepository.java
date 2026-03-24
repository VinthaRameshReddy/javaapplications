package com.medgo.auth.repository.medigo;

import com.medgo.auth.domain.entity.medigo.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);

    Optional<UserModel> findByEmailOrMobile(String emailId, String mobileNumber);

    Optional<UserModel> findByMemberCode(String memberCode);

    Optional<UserModel> findByMemberCodeAndBirthDate(String memberCode, LocalDate dob);

    @Query("""
    SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
    FROM UserModel u
    WHERE u.email = :email
      AND (:mobile IS NULL OR :mobile = '' OR u.mobile = :mobile)
    """)
    boolean existsByEmailAndOptionalMobile(String email, String mobile);
}

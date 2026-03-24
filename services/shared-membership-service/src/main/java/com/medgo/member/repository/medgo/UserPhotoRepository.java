package com.medgo.member.repository.medgo;

import com.medgo.member.domain.entity.medgo.UserPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;



public interface UserPhotoRepository extends JpaRepository<UserPhotoEntity, Long> {
    Optional<UserPhotoEntity> findByMemberCode(String memberCode);
}
package com.medgo.auth.repository.medigo;


import com.medgo.auth.domain.entity.medigo.TrustDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustDeviceEntity, Long> {

    Optional<TrustDeviceEntity> findByUserIdAndUserType(Long userId, String userType);
}
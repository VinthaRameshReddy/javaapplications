package com.medgo.provider.repository;

import com.medgo.provider.domain.entity.CityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CityRepository extends JpaRepository<CityEntity, Long> {

    Page<CityEntity> findByDeleted(String deleted, Pageable pageable);

    @Query("SELECT c FROM CityEntity c WHERE c.deleted = :deleted AND (lower(c.description) LIKE lower(concat('%', :q, '%')) OR lower(c.code) LIKE lower(concat('%', :q, '%')))")
    Page<CityEntity> searchActive(@Param("deleted") String deleted, @Param("q") String q, Pageable pageable);
}


























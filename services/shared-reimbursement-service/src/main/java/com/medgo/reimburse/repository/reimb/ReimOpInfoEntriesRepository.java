package com.medgo.reimburse.repository.reimb;

import com.medgo.reimburse.domain.entity.reimb.ReimOpInfoEntries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReimOpInfoEntriesRepository extends JpaRepository<ReimOpInfoEntries, String> {
}

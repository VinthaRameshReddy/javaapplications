package com.medgo.reimburse.repository.medigo;

import com.medgo.reimburse.domain.entity.medigo.ReimbursementDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementDocumentRepository extends JpaRepository<ReimbursementDocumentEntity, Long> {
    List<ReimbursementDocumentEntity> findByReimbursementRequestId(Long reimbursementRequestId);
    List<ReimbursementDocumentEntity> findByReimbursementRequestControlCode(String controlCode);
}

package com.medgo.reimburse.repository.medigo;

import com.medgo.reimburse.domain.entity.medigo.ReimbursementRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReimbursementRequestRepository extends JpaRepository<ReimbursementRequestEntity, Long> {
    Optional<ReimbursementRequestEntity> findByControlCode(String controlCode);

    /**
     * Atomically mark submission email as sent for the given control code only if not already sent.
     * NULL = email not sent yet → this UPDATE finds the row, sets timestamp, returns 1 → we send email.
     * Non-null = email already sent → no row matches (IS NULL false), returns 0 → we skip (deduplication).
     * Returns the number of rows updated (1 = send email to admin and user, 0 = skip duplicate).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ReimbursementRequestEntity r SET r.submissionEmailSentAt = :sentAt WHERE r.controlCode = :controlCode AND r.submissionEmailSentAt IS NULL")
    int markSubmissionEmailSentIfNotSent(@Param("controlCode") String controlCode, @Param("sentAt") LocalDateTime sentAt);
}

package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "MEDGO_USER_PW_HISTORY")
public class UserPassHistoryModel extends BaseEntity {
    @Column(name = "USER_ID", nullable = false)
    private Long portalUserId;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "EXPIRES_ON")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private UserModel portalUser;
}

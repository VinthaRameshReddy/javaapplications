package com.medgo.member.domain.entity.medgo;

import com.medgo.member.domain.entity.membership.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "USER_DEPENDENT")
@EqualsAndHashCode(callSuper = true)
public class UserDependentEntity extends BaseEntity {
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "DEPENDENT_CODE")
    private String dependentCode;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private UserDependentStatusEnum status;
}
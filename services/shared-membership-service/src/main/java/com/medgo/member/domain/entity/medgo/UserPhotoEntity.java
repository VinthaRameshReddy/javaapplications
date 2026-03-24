package com.medgo.member.domain.entity.medgo;

import com.medgo.member.domain.entity.membership.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "USER_PHOTO")
public class UserPhotoEntity extends BaseEntity {
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "NON_MEMBER_CODE", length = 50)
    private String nonMemberCode;

    @Column(name = "MEMBER_CODE", length = 50)
    private String memberCode;

    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Column(name = "FILE_NAME", length = 150)
    private String fileName;

    @Column(name = "FILE_SIZE_BYTE")
    private Long fileSizeByte;
}
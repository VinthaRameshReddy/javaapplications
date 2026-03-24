package com.

{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.domain.entity;

import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.Column;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AuditAwareBaseEntity extends AbstractBaseEntity {

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @LastModifiedDate
    private OffsetDateTime lastModifiedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}

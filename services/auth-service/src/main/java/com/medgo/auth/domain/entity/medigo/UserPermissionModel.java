package com.medgo.auth.domain.entity.medigo;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity(name = "MEDGO_USER_PERMISSION")
@EqualsAndHashCode(callSuper = true)
public class UserPermissionModel extends BaseEntity {

    @Column(name = "PREFIX")
    private String prefix;

    @Column(name = "CODE")
    private String code;

    @Column(name = "HIERARCHY")
    private String hierarchy;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "DESCRIPTION")
    @Size(max = 250)
    private String description;

    @Column(name = "ENABLED")
    private Boolean enabled;
}

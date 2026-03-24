package com.medgo.auth.domain.entity.medigo;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@Entity(name = "MEDGO_USER_ROLE")
@FieldNameConstants
@EqualsAndHashCode(callSuper = true, exclude = {  "permissions" })
public class UserRoleModel extends BaseEntity {
    @Column(name = "NAME")
    @Size(max = 100)
    @NotNull
    private String name;

    @Column(name = "DESCRIPTION")
    @Size(max = 250)
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ROLE_ID")
    private List<UserRolePermissionModel> permissions;
}

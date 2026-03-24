package com.medgo.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity(name = "MEDGO_USER_ROLE_PERMISSION")
@EqualsAndHashCode(callSuper = true, exclude = {"permissions"})
public class UserRolePermissionModel extends BaseEntity {
    @Column(name = "USER_ROLE_ID", nullable = false)
    private Long userRoleId;

    @Column(name = "USER_PERMISSION_ID", nullable = false)
    private Long userPermissionId;

    // NO NEED FOR MULTIPLE STATUS, FOR REMOVAL -- JJ
//    @Column(name = "STATUS")
//    @Enumerated(EnumType.STRING)
//    private MedGoModelStatusEnum status;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ID", referencedColumnName = "USER_PERMISSION_ID")
    private List<UserPermissionModel> permissions;
}

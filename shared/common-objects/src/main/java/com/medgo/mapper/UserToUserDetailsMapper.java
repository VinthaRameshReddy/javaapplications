package com.medgo.mapper;

import com.medgo.entity.UserModel;
import com.medgo.enums.MedGoUserStatusEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserToUserDetailsMapper {

    public static UserDetails map(UserModel user) {
        Collection<? extends GrantedAuthority> authorities =
                user.getRole().getPermissions().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getUserPermissionId()))
                    .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())          // maps login/username
                .password(user.getPassword())              // maps hashed password
                .authorities(authorities)                  // maps roles
                .accountExpired(false)                     // or use flags from DB
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getStatus().equals(MedGoUserStatusEnum.ACTIVE))                // maps active flag
                .build();
    }
}

package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "med_users_new")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL", unique = true)
    private String emailId;

    @Column(name = "MOBILE", unique = true)
    private String mobileNumber;

    @Column(name = "PASSWORD")
    private String password;
}

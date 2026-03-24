package com.medgo.auth.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@Table(name = "med_invalid_birthdate_log", schema = "dbo")
@Entity
public class InvalidBirthdateLogModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO, SEQUENCE, or IDENTITY depending on DB
    @Column(name = "ID")
    private Long id;
    @Column(name = "MEMBER_CODE", length = 50)
    private String memberCode;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "DATABASE_DATE")
    private LocalDate databaseDate;
    @Column(name = "Count")
    private Integer wrongCount;



}

package com.medgo.claims.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "MEDGO_BANK_MASTER", schema = "dbo")
public class MedgoBankMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

//    @Column(name = "ABBREVIATION", length = 50)
//    private String abbreviation;

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;

    @Column(name = "PATTERN", length = 255)
    private String pattern;
}

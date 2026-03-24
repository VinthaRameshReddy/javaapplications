package com.medgo.claims.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "CLAIM_NATURE_MASTER", schema = "dbo")
public class ClaimNatureMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "SERVICE_TYPE", nullable = false, length = 20)
    private String serviceType;   // INPATIENT / OUTPATIENT

    @Column(name = "CLAIM_NATURE", nullable = false, length = 100)
    private String claimNature;
}


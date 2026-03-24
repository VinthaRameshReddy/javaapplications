package com.medgo.reimburse.domain.entity.medigo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medgo_claim_nature")
@Getter
@Setter
public class ClaimNature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "classification_code")
    private String classificationCode;

    @Column(name = "type")
    private String type;

    @Column(name = "sub_type")
    private String subType;

    @Column(name = "claim_nature")
    private String claimNature;

    @Column(name = "display_claim_nature")
    private String displayClaimNature;
}

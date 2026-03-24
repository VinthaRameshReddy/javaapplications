package com.medgo.member.domain.entity.membership;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "DP_MemberTagging_Tbl")
public class DataPrivacyTaggingEntity {
    @Id
    @Column(name = "EntryID")
    private String id;

    @Column(name = "Member_Code")
    private String memberCode;

    @Column(name = "Document_Source")
    private Integer documentSource;
}
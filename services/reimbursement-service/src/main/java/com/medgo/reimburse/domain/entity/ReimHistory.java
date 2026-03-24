package com.medgo.reimburse.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Reim_VW_MEDGO2_History_Table", schema = "dbo", catalog = "MedicardSQL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimHistory {

    @Column(name = "STATUS")
    private String status;

    @Transient
    private String displayStatus;

    @Id
    @Column(name = "CONTROL_CODE")
    private String controlCode;

    @Column(name = "ENTRY_CODE")
    private String entryCode;

    @Column(name = "MEMBER_CODE")
    private String memberCode;

    @Transient
    private ReimVWMedgo2RequestDetails moreDetails;

}

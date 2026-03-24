package com.medgo.reimburse.domain.entity.reimb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

@Entity
@Immutable
@Subselect("""
    SELECT 
        ROW_NUMBER() OVER (
            ORDER BY MEMBER_CODE, CONTROL_CODE, ENTRY_CODE
        ) AS id,
        MEMBER_CODE,
        CONTROL_CODE,
        ENTRY_CODE,
        STATUS,
        SERVICE_TYPE
    FROM Reim_VW_MEDGO2_History
""")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimHistory {

    @Id
    private Long id;

    @Column(name = "MEMBER_CODE")
    private String memberCode;

    @Column(name = "CONTROL_CODE")
    private String controlCode;

    @Column(name = "ENTRY_CODE")
    private String entryCode;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "SERVICE_TYPE")
    private String serviceType;

    @Transient
    private String displayStatus;

    @Transient
    private ReimVWMedgo2RequestDetails moreDetails;
}

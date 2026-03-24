package com.medgo.reimburse.domain.entity.reimb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reim_ip_receiving", schema = "dbo", catalog = "ReimDB")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimIpReceiving {

    @Id
    @Column(name = "control_code", length = 25, nullable = false)
    private String controlCode;

    @Column(name = "type", length = 10, nullable = false)
    private String type;

    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;

    @Column(name = "org_received_date")
    private LocalDateTime orgReceivedDate;

    @Column(name = "member_code", length = 50, nullable = false)
    private String memberCode;

    @Column(name = "mem_lname", length = 50, nullable = false)
    private String memLname;

    @Column(name = "mem_fname", length = 50, nullable = false)
    private String memFname;

    @Column(name = "mem_mname", length = 50)
    private String memMname;

    @Column(name = "mem_age")
    private Float memAge;

    @Column(name = "mem_type")
    private Short memType;

    @Column(name = "prin_code", length = 50)
    private String prinCode;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "costplus_code")
    private Integer costplusCode;

    @Column(name = "particulars_code")
    private Integer particularsCode;

    @Column(name = "confined_date")
    private LocalDateTime confinedDate;

    @Column(name = "discharged_date")
    private LocalDateTime dischargedDate;

    @Column(name = "hospital_code", length = 25)
    private String hospitalCode;

    @Column(name = "sa_amount")
    private BigDecimal saAmount;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "org_due_date")
    private LocalDateTime orgDueDate;

    @Column(name = "advance_payment")
    private Boolean advancePayment;

    @Column(name = "bank", length = 15)
    private String bank;

    @Column(name = "check_no", length = 20)
    private String checkNo;

    @Column(name = "check_date")
    private LocalDate checkDate;

    @Column(name = "check_released_date")
    private LocalDateTime checkReleasedDate;

    @Column(name = "complied")
    @Builder.Default
    private Boolean complied = false;

    @Column(name = "for_verify")
    private Boolean forVerify;

    @Column(name = "verification_code")
    private Integer verificationCode;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @Column(name = "entry_by", length = 25, nullable = false)
    private String entryBy;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "updated_by", length = 25)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

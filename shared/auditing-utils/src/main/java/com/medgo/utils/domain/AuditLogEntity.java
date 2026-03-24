package com.medgo.utils.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "smartnow_audit_log")
public class AuditLogEntity extends AuditAwareBaseEntity {

    @Column(name = "txn_ref_num")
    private String txn_ref_num;

    @Column(name = "login_id")
    private String loginId;

    @Column(name = "cust_mobile_no")
    private String mobileNumber;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "request", nullable = false)
    private String request;

    @Column(name = "response", nullable = false)
    private String response;

    @Column(name = "api_url")
    private String apiUrl;

    @Column(name = "api_name")
    private String apiName;

    @Column(name = "status")
    private String status;

    @Id
    private String id; // Changed from Long to String
}
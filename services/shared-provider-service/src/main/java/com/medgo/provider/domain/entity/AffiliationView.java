package com.medgo.provider.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "vw_affiliation")
public class AffiliationView {

    // Doctor
    @Id
    @Column(name = "doctorCode")
    private String doctorCode;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "middleInitial")
    private String middleInitial;

    @Column(name = "status")
    private String status;

    @Column(name = "specializationDesc")
    private String specializationDesc;

    @Column(name = "specializationCode")
    private String specializationCode;

    @Column(name = "subSpecializationDesc")
    private String subSpecializationDesc;

    @Column(name = "subSpecializationCode")
    private String subSpecializationCode;

    @Column(name = "subSubSpecializationDesc")
    private String subSubSpecializationDesc;

    @Column(name = "subSubSpecializationCode")
    private String subSubSpecializationCode;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile")
    private String mobile;

    // Hospital
    @Column(name = "hospitalCode")
    private String hospitalCode;

    @Column(name = "hospitalName")
    private String hospitalName;

    @Column(name = "room")
    private String room;

    @Column(name = "schedule")
    private String schedule;

    @Column(name = "hospitalCoordinator")
    private String hospitalCoordinator;

    @Column(name = "hospitalPhoneNumber")
    private String hospitalPhoneNumber;

    @Column(name = "hospitalFaxNo")
    private String hospitalFaxNo;

    @Column(name = "hospitalEmail")
    private String hospitalEmail;

    @Column(name = "hospitalAddress")
    private String hospitalAddress;

    @Column(name = "hospitalCity")
    private String hospitalCity;

    @Column(name = "hospitalCityCode")
    private String hospitalCityCode;

    @Column(name = "hospitalProvince")
    private String hospitalProvince;

    @Column(name = "hospitalProvinceCode")
    private String hospitalProvinceCode;

    @Column(name = "hospitalRegion")
    private String hospitalRegion;

    @Column(name = "hospitalRegionCode")
    private String hospitalRegionCode;

    @Column(name = "coveredAccountCompanies")
    private String coveredAccountCompanies;

    // Metadata (Affiliation)
    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "createdOn")
    private String createdOn;

    @Column(name = "updatedBy")
    private String updatedBy;
}
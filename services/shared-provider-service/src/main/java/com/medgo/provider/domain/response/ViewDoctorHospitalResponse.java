package com.medgo.provider.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ViewDoctorHospitalResponse {
    private String lastName;
    private String middleInitial;
    private String firstName;
    private String doctorCode;
    private String specializationCode;
    private String specializationDesc;
    private String hospitalCode;
    private String hospitalName;
    private String room;
    private String schedule;
    private String email;
    private String mobile;
    private String createdBy;
    private String createdOn;
    private String updatedBy;
    private String fullName;
    private List<McapSpecializationResponse> specializations;
}
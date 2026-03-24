package com.medgo.provider.mapper;

import com.medgo.provider.domain.entity.AffiliationView;
import com.medgo.provider.domain.response.ViewDoctorHospitalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MedGoClaimsMapper {

    @Mappings({
            @Mapping(source = "lastName", target = "lastName"),
            @Mapping(source = "middleInitial", target = "middleInitial"),
            @Mapping(source = "firstName", target = "firstName"),
            @Mapping(source = "doctorCode", target = "doctorCode"),
            @Mapping(source = "specializationCode", target = "specializationCode"),
            @Mapping(source = "specializationDesc", target = "specializationDesc"),
            @Mapping(source = "hospitalCode", target = "hospitalCode"),
            @Mapping(source = "hospitalName", target = "hospitalName"),
            @Mapping(source = "room", target = "room"),
            @Mapping(source = "schedule", target = "schedule"),
            @Mapping(source = "email", target = "email"),
            @Mapping(source = "mobile", target = "mobile"),
            @Mapping(source = "createdBy", target = "createdBy"),
            @Mapping(source = "createdOn", target = "createdOn"),
            @Mapping(source = "updatedBy", target = "updatedBy"),
            @Mapping(target = "fullName", expression = "java(entity.getLastName() + \", \" + entity.getFirstName() + \" \" + entity.getMiddleInitial())"),
            @Mapping(target = "specializations", expression = "java(java.util.List.of(new com.medgo.provider.domain.response.McapSpecializationResponse(entity.getSpecializationCode(), entity.getSpecializationDesc(), \"Primary\")))")
    })
    ViewDoctorHospitalResponse toViewDoctorHospitalResponse(AffiliationView entity);
}
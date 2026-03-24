package com.medgo.provider.domain.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViewDoctorHospitalRequest {

    @Size(max = 50, message = "Search value for hospital should be maximum of 50 characters.")
    private String hospitalName;
    private String hospitalCode;
    @Size(max = 50, message = "Search value for doctor should be maximum of 50 characters.")
    private String doctorName;
    private String doctorCode;
    private String cityName;
    private String cityCode;
    private String provinceName;
    private String provinceCode;
    private String regionName;
    private String regionCode;
    private List<String> accreditationStatusCodes;
    private List<String> specializationCodes;
    private List<String> specializationCodeNotIn;
    private boolean maternity;
}
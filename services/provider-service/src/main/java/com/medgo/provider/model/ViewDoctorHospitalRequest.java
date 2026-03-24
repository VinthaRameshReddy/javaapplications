package com.medgo.provider.model;

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
    @Size(max = 50)
    private String hospitalName;
    private String hospitalCode;
    @Size(max = 50)
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



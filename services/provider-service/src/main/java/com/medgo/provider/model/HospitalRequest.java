package com.medgo.provider.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HospitalRequest {
    private List<String> hfCodes;
    private List<String> hfStatuses;
    private List<String> hfTypes;
    private List<String> regionCodes;
    private List<String> provinceCodes;
    private List<String> cityCodes;
    private List<String> groups;
    private List<String> accountCodes;
}



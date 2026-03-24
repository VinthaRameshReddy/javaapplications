package com.medgo.facescan.domain.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FaceScanTncRequest extends BaseDto {

    @NotBlank(message = "FED ID is required")
    private String fedId;

    @NotNull(message = "consent is required")
    private boolean consent;

    private String memberName;
    private Integer age;
    private String gender;
    private Double weight;
    private Double height;
    private String isSmoker;
    private String isDiabetic;
    private String bpMedication;
}

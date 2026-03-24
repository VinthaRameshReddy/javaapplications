package com.medgo.facescan.domain.response;


import com.medgo.facescan.domain.models.medgo.FaceScanSessionModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FaceScanTncDto {
    private int status;
    private String message;
    private String sessionId;
    private FaceScanSessionModel sessionDetails;
}


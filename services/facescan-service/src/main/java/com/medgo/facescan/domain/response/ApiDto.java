package com.medgo.facescan.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiDto {
    private int status;
    private String message;
}

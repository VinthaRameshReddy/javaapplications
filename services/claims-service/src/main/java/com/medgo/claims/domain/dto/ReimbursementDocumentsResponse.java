package com.medgo.claims.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementDocumentsResponse {
    private String controlCode;
    private String status;
    private List<DocumentResponse> documents;
}


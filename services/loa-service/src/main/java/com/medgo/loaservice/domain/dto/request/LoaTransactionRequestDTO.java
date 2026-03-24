package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoaTransactionRequestDTO {

    @Min(value = 1, message = "serviceTypeId must be greater than 0")
    private Long serviceTypeId;

    @Min(value = 1, message = "serviceSubTypeId must be greater than 0")
    private Long serviceSubTypeId;

    @Min(value = 1, message = "serviceFromId must be greater than 0")
    private Long serviceFromId;

    @Size(max = 50, message = "serviceFromRemarks must not exceed 50 characters")
    private String serviceFromRemarks;

    @Min(value = 1, message = "serviceOriginId must be greater than 0")
    private Long serviceOriginId;

    @Size(max = 50, message = "serviceOriginRemarks must not exceed 50 characters")
    private String serviceOriginRemarks;

    private Long serviceDuplicateId;

    // Optional field from RequestJson (portal user ID)
    private Long portalUserId;

    @jakarta.validation.constraints.NotBlank(message = "Requested by is required.")
    @Size(max = 100, message = "Requested by max limit of 100 characters reached.")
    @Pattern(regexp = "^[A-Za-z0-9-,_ ]*$", message = "Request By accepts alphanumeric - and , only.")
    private String requestedBy;

    @Size(max = 50, message = "fromHospitalCode must not exceed 50 characters")
    @Pattern(regexp = "^[\\w -]*$", message = "fromHospitalCode contains invalid characters")
    private String fromHospitalCode;

    @Valid
    @NotNull(message = "member is required")
    private MemberDTO member;

    @Valid
    private ConsultationDTO consultation;

    @Valid
    @JsonProperty("medGoConsultation")
    private MedGoConsultationDTO medGoConsultation;

    @Valid
    @JsonProperty("testV2")
    private TestV2DTO testV2;

    @Valid
    private InquiryDTO inquiry;

    @Valid
    @JsonProperty("inPatient")
    private InPatientDTO inPatient;

    @Valid
    private List<NoteDTO> notes;

    private List<AttachmentDTO> attachments;

    private BigDecimal costSaving;

    // Steerage
    @Valid
    @JsonProperty("requestSteerage")
    private RequestSteerageDTO requestSteerage;

    // User Bank Detail
    @JsonProperty("userBankDetail")
    private UserBankDetailDTO userBankDetail;

    @JsonProperty("userBankDetailAttachments")
    private List<UserBankDetailAttachmentDTO> userBankDetailAttachments;
}

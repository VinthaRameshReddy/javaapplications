package com.medgo.loaservice.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemedicineRequestDTO {

    @Size(max = 50, message = "memberCode must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "memberCode must be alphanumeric only")
    @NotNull(message = "memberCode is required")
    private String memberCode;

    @Email(message = "email must be a valid email address")
    @Size(max = 255, message = "email must not exceed 255 characters")
    private String email;

    @NotNull(message = "mobile is required")
    @Pattern(regexp = "^(09)\\d{9}$", message = "Mobile number format must be 09XXXXXXXXX.")
    private String mobile;

    @NotNull(message = "subject is required")
    @Size(max = 50, message = "Subject reached maxed limit.")
    @Pattern(regexp = "^[A-Za-z0-9 !@#$%&*(),._?:{}]*$", message = "Subject contains invalid characters")
    private String subject;

    @Size(max = 250, message = "description reached maxed limit.")
    @Pattern(regexp = "^[A-Za-z0-9 !@#$%&*(),._?:{}]*$", message = "Description contains invalid characters")
    private String description;

    @JsonProperty("preferredDateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime preferredDateTime;
}

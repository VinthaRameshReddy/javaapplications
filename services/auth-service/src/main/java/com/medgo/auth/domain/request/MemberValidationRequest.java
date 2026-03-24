package com.medgo.auth.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MemberValidationRequest(
        @NotBlank(message = "Member Code is required.")
        @Size(max = 50, message = "Reached max limit characters.")
        @Pattern(regexp = "^[A-Za-z0-9]*$", message = "Member code accepts alphanumeric only.")
        @JsonProperty("memberCode")
        String memberCode,

        @NotNull
        @PastOrPresent
        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonProperty("birthDate")
        LocalDate birthDate
) {

}

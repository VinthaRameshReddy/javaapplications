package com.medgo.auth.domain.request;

import com.medgo.auth.validation.ValidEmail;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record NonMemberRegistrationRequest(
        @ValidEmail
        @Size(max = 100, message = "Email exceeds maximum length of 100 characters.")
        String email,

        @Size(max = 11, message = "Phone number exceeds maximum length of 11 characters.")
        @Pattern(regexp = "^[0-9]*$", message = "Phone number accepts numeric values only.")
        String mobileNumber) {
}

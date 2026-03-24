package com.medgo.loaservice.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {

    @Size(max = 50, message = "temporaryCode must not exceed 50 characters")
    private String temporaryCode;

    @Size(max = 50, message = "memberCode must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "memberCode must be alphanumeric only")
    private String memberCode;

    @Size(max = 100, message = "lastName must not exceed 100 characters")
    private String lastName;

    @Size(max = 100, message = "firstName must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "middleName must not exceed 100 characters")
    private String middleName;

    @Size(max = 50, message = "accountCode must not exceed 50 characters")
    private String accountCode;

    @Email(message = "email must be a valid email address")
    @Size(max = 255, message = "email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^(09)\\d{9}$", message = "mobile must start with 09 and have 11 digits")
    private String mobile;
}

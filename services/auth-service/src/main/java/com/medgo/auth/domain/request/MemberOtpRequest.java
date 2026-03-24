package com.medgo.auth.domain.request;

import java.time.LocalDate;

public record MemberOtpRequest(
        String memberCode,
        LocalDate birthDate) {
}
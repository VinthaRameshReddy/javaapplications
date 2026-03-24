package com.medgo.auth.domain.response;


public record MemberValidationResponse(

        String memberCode,
        String dob,
        boolean verified) {
}

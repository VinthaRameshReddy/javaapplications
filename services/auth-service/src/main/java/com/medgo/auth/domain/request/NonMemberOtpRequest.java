package com.medgo.auth.domain.request;

public record NonMemberOtpRequest(
        String email,
        String mobile) {
}
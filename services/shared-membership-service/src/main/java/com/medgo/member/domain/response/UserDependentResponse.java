package com.medgo.member.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDependentResponse {
    private Long id;
    private String dependentCode;
    private String status;
    private MembershipResponse membership;
    private String photoUrl;
    private boolean consented;
}
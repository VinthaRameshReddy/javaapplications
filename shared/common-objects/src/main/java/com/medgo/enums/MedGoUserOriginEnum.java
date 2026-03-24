package com.medgo.enums;

import lombok.Getter;

@Getter
public enum MedGoUserOriginEnum {
    // REFERENCE: origin column in dbo.medgo_users (not MEDGO.dbo.USERS)
    URG,
    MIMS,
    MEDGO;
}

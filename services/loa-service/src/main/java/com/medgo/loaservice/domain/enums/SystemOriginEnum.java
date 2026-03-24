package com.medgo.loaservice.domain.enums;


public enum SystemOriginEnum {
    MCAP("MCAP", "MediCard Authorization Portal"),
    MACE("MACE", "MediCard Authorization Center");

    private final String code;
    private final String description;

    SystemOriginEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SystemOriginEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (SystemOriginEnum origin : values()) {
            if (origin.code.equalsIgnoreCase(code)) {
                return origin;
            }
        }
        return null;
    }
}

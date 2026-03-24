package com.medgo.reimburse.util;

import com.medgo.reimburse.constants.enums.ReimbursementStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

@Slf4j
public final class CoreUtils {

    private CoreUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ReimbursementStatusEnum getReimbursementStatusEnum(String status) {
        if (status == null) {
            return null;
        }

        return Stream.of(ReimbursementStatusEnum.values())
                .filter(i -> i.getValue().equalsIgnoreCase(status))
                .findFirst()
                .orElse(null);
    }
}

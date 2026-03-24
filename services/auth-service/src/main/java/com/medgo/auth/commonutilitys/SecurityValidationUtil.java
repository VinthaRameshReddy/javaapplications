package com.medgo.auth.commonutilitys;

import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.commons.RequestContext;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for security validation operations
 */
public class SecurityValidationUtil {

    /**
     * Validates that DeviceId header is present and not empty
     * 
     * @return ResponseEntity with error if validation fails, null if validation passes
     */
    public static ResponseEntity<CommonResponse> validateDeviceId() {
        String deviceId = RequestContext.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            return ResponseEntity.badRequest().body(CommonResponse.error(
                    new ErrorResponse(400, "DeviceId header is required"), 400));
        }
        return null;
    }
}


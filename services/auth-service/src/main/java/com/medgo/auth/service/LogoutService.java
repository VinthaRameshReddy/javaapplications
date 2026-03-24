package com.medgo.auth.service;

import com.medgo.commons.CommonResponse;

/**
 * Service interface for logout operations.
 * Implementation should be provided in an *Impl class.
 */
public interface LogoutService {
    /**
     * Invalidate the given token if it belongs to the provided userId (deviceId).
     *
     * @param token  raw JWT token (without Bearer prefix). May be null.
     * @param userId deviceId / user identifier to validate against token subject
     * @return CommonResponse indicating success or error
     */
    CommonResponse logout(String token, String userId);
}


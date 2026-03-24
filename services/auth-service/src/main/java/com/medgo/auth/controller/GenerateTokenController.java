package com.medgo.auth.controller;


import com.medgo.auth.domain.request.JwtRequest;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.RequestContext;
import com.medgo.crypto.annotation.DecryptBody;
import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.exception.GenericException;
import com.medgo.jwt.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Objects;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GenerateTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateTokenController.class);
    @Autowired
    private  AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService jwtInMemoryUserDetailsService;

    @PostMapping("/generateToken")
    @EncryptResponse
    public ResponseEntity<CommonResponse> createAuthenticationToken(@Valid @DecryptBody(JwtRequest.class) JwtRequest request)
            throws Exception {
        LOGGER.debug("jwtRequest {}", request);
        authenticate(request.userId());

        // Use deviceId from RequestContext (set from X-DEVICE-ID header) for token generation
        // This ensures consistency with login endpoints and prevents userId,deviceId concatenation issues
        String deviceId = RequestContext.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            LOGGER.error("DeviceId is missing from request header");
            throw new GenericException("DeviceId header is required");
        }

        // Sanitize deviceId - ensure it doesn't contain comma-separated values (userId,deviceId format)
        // If somehow a concatenated value got through, extract only the deviceId part
        if (deviceId.contains(",")) {
            LOGGER.warn("DeviceId contains comma - possible concatenation issue. Original: {}", deviceId);
            // If format is "userId,deviceId", take the part after the last comma
            String[] parts = deviceId.split(",");
            deviceId = parts[parts.length - 1].trim();
            LOGGER.info("Extracted deviceId after sanitization: {}", deviceId);
        }

        LOGGER.info("Generating JWT token for userId: {} with deviceId: {}", request.userId(), deviceId);

        final UserDetails userDetails = jwtInMemoryUserDetailsService
                .loadUserByUsername(deviceId);

        // Include username (email/mobile) in token for memberCode validation
        final String token = jwtTokenUtil.generateToken(userDetails, request.userId());
        
        // Add token to header instead of body
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Access-Token", token);
        LOGGER.info("Added X-Access-Token to response header");
        
        // Return empty data or success message without token
        HashMap<String, Object> responseData = new HashMap<>();
        responseData.put("message", "Token generated successfully");
        return ResponseEntity.ok()
                .headers(headers)
                .body(CommonResponse.success(responseData));
    }


    private void authenticate(String userId) throws Exception {
        Objects.requireNonNull(userId);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userId, userId));
        } catch (DisabledException e) {
            throw new GenericException("USER_DISABLED");
        } catch (BadCredentialsException e) {
            throw new GenericException("INVALID_CREDENTIALS");
        }
    }
}

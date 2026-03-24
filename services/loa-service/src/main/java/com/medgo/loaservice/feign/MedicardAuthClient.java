package com.medgo.loaservice.feign;

import com.medgo.loaservice.config.AuthFeignClientConfig;
import com.medgo.loaservice.domain.dto.response.AuthResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "medicard-auth-api",
        url = "${medicard.api.url}",
        configuration = AuthFeignClientConfig.class
)
public interface MedicardAuthClient {

    @PostMapping(value = "/mcap/mace-medgo-auth/oauth2/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AuthResponseDTO getAccessToken(@RequestBody MultiValueMap<String, String> formData);
}

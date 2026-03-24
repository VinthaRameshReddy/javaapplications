package com.medgo.provider.feign;

import com.medgo.commons.CommonResponse;
import com.medgo.provider.config.FeignClientConfig;
import com.medgo.provider.model.CityRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "shared-provider-cities",
        url = "${shared.provider.service.url}",
        configuration = FeignClientConfig.class
)
public interface CitiesClient {

    @PostMapping("/provider/v1/cities")
    CommonResponse listCities(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "search", required = false) String search,
            @Valid @RequestBody CityRequest request
    );
}



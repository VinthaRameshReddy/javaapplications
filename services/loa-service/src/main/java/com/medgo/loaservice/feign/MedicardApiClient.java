package com.medgo.loaservice.feign;

import com.medgo.loaservice.config.FeignClientConfig;
import com.medgo.loaservice.domain.dto.request.LoaRequestDTO;
import com.medgo.loaservice.domain.dto.response.LoaDownloadResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(
        name = "medicard-api",
        url = "${medicard.api.url}",
        configuration = FeignClientConfig.class
)
public interface MedicardApiClient {


    @PostMapping("/mcap/mace-medgo/service/transaction/v2")
    Object requestLoa(@RequestBody LoaRequestDTO request);


    @GetMapping("/mcap/mace-medgo/service/transaction/v2")
    Object getLoaHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam("originMemberCode") String originMemberCode,
            @RequestParam(value = "hospitalCode", required = false) String hospitalCode
    );


    @GetMapping("/mcap/mace-medgo/service/transaction/v2/{id}")
    Object getLoaHistoryDetails(
            @PathVariable("id") Long id,
            @RequestParam("systemOrigin") String systemOrigin,
            @RequestParam("originMemberCode") String originMemberCode
    );


    @GetMapping("/mcap/mace-medgo/form/loa/v2")
    LoaDownloadResponseDTO downloadLoa(
            @RequestParam("systemOrigin") String systemOrigin,
            @RequestParam("requestId") Long requestId,
            @RequestParam("originMemberCode") String originMemberCode
    );
}



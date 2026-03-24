package com.medgo.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "filemanagement-service", url = "${filemanagement.service.url}")
public interface FileServiceFeignClient {
    @GetMapping("/file/findLinksByTags")
    List<String> findLinksByTags(@RequestParam Map<String, String> tags);
}
//package com.medgo.member.feign;
//
//import com.medgo.commons.CommonResponse;
//import com.medgo.member.config.FeignClientConfig;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//import java.util.Map;
//@FeignClient(
//        name = "filemanagement-service",
//        url = "${filemanagement.service.url}")
////        configuration = FeignClientConfig.class)
//public interface FileManagmentServiceClient {
//
//
//
//    @GetMapping("/feign/findLinksByTags")
//    List<String> findLinksByTags(Map<String, String> tags);
//
////    @GetMapping("/findLinksByTags")
////    public ResponseEntity<List<String>> findLinksByTags(@RequestParam Map<String, String> tags) {
////        log.info("Fetching links for tags: {}", tags);
////        List<String> links = service.findLinksByTags(tags);
////        return ResponseEntity.ok(links);
//    }
//
//
//
//
//
//
//

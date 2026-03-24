//package com.medgo.claims.feign;
//
//import com.medgo.claims.config.FeignClientConfig;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import java.util.Map;
//
//@FeignClient(
//        name = "notification-service",
//        url = "${notification.service.url:https://medgo2o-stg.medicardphils.com}",
//        configuration = FeignClientConfig.class)
//public interface NotificationServiceClient {
//
//    @PostMapping("/notification/send")
//    ResponseEntity<Map<String, String>> sendEmail(@RequestBody Map<String, Object> emailRequest);
//}
//

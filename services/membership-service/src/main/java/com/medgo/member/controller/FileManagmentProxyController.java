//package com.medgo.member.controller;
//
//import com.medgo.commons.CommonResponse;
//import com.medgo.member.feign.FileManagmentServiceClient;
//import com.medgo.member.feign.SharedMembershipServiceClient;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Map;
//
//
//@RestController
//@RequestMapping("/api/v1/filemanagment")
//@RequiredArgsConstructor
//
//public class FileManagmentProxyController {
//
//
//    private final FileManagmentServiceClient fileManagmentServiceClient;
//
//    @GetMapping("/findLinksByTags")
//    public ResponseEntity<List<String>> findLinksByTags(@RequestParam Map<String, String> tags) {
////        log.info("Fetching links for tags: {}", tags);
//       List<String> links = fileManagmentServiceClient.findLinksByTags(tags);
//       return ResponseEntity.ok(links);
//}
//
//}

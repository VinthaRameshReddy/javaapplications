package com.medgo.filemanagement.controller;

import com.medgo.filemanagement.domain.dto.FileUploadResponse;
import com.medgo.filemanagement.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService service;

    @GetMapping("/findLinksByTags")
    public ResponseEntity<List<String>> findLinksByTags(@RequestParam Map<String, String> tags) {
        log.info("Fetching links for tags: {}", tags);
        List<String> links = service.findLinksByTags(tags);
        return ResponseEntity.ok(links);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("controlCode") String controlCode,
            @RequestParam("documentType") String documentType,
            @RequestParam("folderName") String folderName) {
        log.info("Uploading file for controlCode={}, documentType={}, folderName={}", controlCode, documentType, folderName);
        FileUploadResponse response = service.uploadFile(file, controlCode, documentType, folderName);
        return ResponseEntity.ok(response);
    }
}
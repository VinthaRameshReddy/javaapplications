package com.medgo.claims.feign;

import com.medgo.claims.config.FeignMultipartSupportConfig;
import com.medgo.claims.config.FileManagementFeignConfig;
import com.medgo.claims.domain.dto.FileUploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "filemanagement-service",
        url = "${filemanagement.service.url}",
        configuration = {FileManagementFeignConfig.class, FeignMultipartSupportConfig.class})
public interface FileManagementServiceClient {
    
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FileUploadResponse uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("controlCode") String controlCode,
            @RequestParam("documentType") String documentType,
            @RequestParam("folderName") String folderName);
}







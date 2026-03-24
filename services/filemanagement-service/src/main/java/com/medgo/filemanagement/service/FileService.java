package com.medgo.filemanagement.service;//package com.medgo.filemanagement.service;

import com.medgo.filemanagement.domain.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileService {
    List<String> findLinksByTags(Map<String, String> tags);

    FileUploadResponse uploadFile(MultipartFile file, String controlCode, String documentType, String folderName);
}
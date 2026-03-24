package com.medgo.filemanagement.service.impl;


import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.BlobClient;
import com.medgo.filemanagement.domain.dto.FileUploadResponse;
import com.medgo.filemanagement.service.FileService;
import com.medgo.filemanagement.utils.AzureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AzureUtil azureUtil;



    @Override
    public List<String> findLinksByTags(Map<String, String> tags) {
        var filePath = AzureUtil.getPathByTags(tags);
        return getLinksByPrefix(filePath);
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String controlCode, String documentType, String folderName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try {
            String sanitizedDocType = documentType != null ? documentType.replaceAll("\\s+", "_") : "Unknown";
            // Use folderName for blob path instead of controlCode to match format: controlcode_firstname_lastname_companyname
            String folderPath = folderName != null && !folderName.trim().isEmpty() ? folderName : controlCode;
            String blobName = String.format("%s/%s_%s", folderPath, sanitizedDocType, UUID.randomUUID());

            BlobClient blobClient = azureUtil.blobServiceClientBuilder().getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // metadata could be added if needed:
            // Map<String, String> metadata = Map.of("documentType", documentType, "controlCode", controlCode);
            // blobClient.setMetadata(metadata);

            String blobUrl = blobClient.getBlobUrl();

            log.info("Uploaded file to blob storage with folder: {}, blobName: {}", folderPath, blobName);
            return FileUploadResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .blobName(blobName)
                    .blobUrl(blobUrl)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .build();
        } catch (Exception e) {
            log.error("Failed to upload file for controlCode={}, documentType={}, folderName={}", controlCode, documentType, folderName, e);
            throw com.medgo.filemanagement.utils.CustomExceptionHelper.uploadingFailed(e.getMessage());
        }
    }

    private List<String> getLinksByPrefix(String prefix) {
        List<BlobItem> files = azureUtil.getBlobsByPrefix(prefix, 100);
        return files.stream()
                .map(file -> String.format("%s?%s", file.getMetadata().get("blobUri"), azureUtil.generateSas(file.getName())))
                .toList();
    }
}
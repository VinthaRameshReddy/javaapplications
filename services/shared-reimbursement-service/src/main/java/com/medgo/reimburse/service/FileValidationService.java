package com.medgo.reimburse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FileValidationService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    private static final long MAX_SECTION_SIZE = 50 * 1024 * 1024; // 50MB in bytes

    /**
     * Validates a list of files for a document section
     * 
     * @param files List of files to validate
     * @param sectionName Name of the section (for error messages)
     * @throws IllegalArgumentException if validation fails
     */
    public void validateFiles(List<MultipartFile> files, String sectionName) {
        if (files == null || files.isEmpty()) {
            return; // Empty lists are allowed (optional documents)
        }

        long totalSize = 0;
        
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue; // Skip null or empty files
            }

            // Validate file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException(
                    String.format("File in %s section has no filename", sectionName)
                );
            }

            String extension = getFileExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException(
                    String.format("File '%s' in %s section has invalid file type. Only JPG, PNG, and JPEG files are allowed.",
                        originalFilename, sectionName)
                );
            }

            // Validate individual file size
            long fileSize = file.getSize();
            if (fileSize > MAX_FILE_SIZE) {
                throw new IllegalArgumentException(
                    String.format("File '%s' in %s section exceeds maximum file size of 10MB. File size: %.2f MB",
                        originalFilename, sectionName, fileSize / (1024.0 * 1024.0))
                );
            }

            totalSize += fileSize;
        }

        // Validate total section size
        if (totalSize > MAX_SECTION_SIZE) {
            throw new IllegalArgumentException(
                String.format("Total size of files in %s section exceeds maximum of 50MB. Total size: %.2f MB",
                    sectionName, totalSize / (1024.0 * 1024.0))
            );
        }

        log.debug("Validated {} files in {} section. Total size: {} bytes", 
            files.size(), sectionName, totalSize);
    }

    public void validateSingleFileOnly(List<MultipartFile> files, String sectionName) {
        if (files == null || files.isEmpty()) {
            return; // No file provided is allowed (optional documents)
        }
        if (files.size() > 1) {
            throw new IllegalArgumentException("For Bank Documents, upload only one document.");
        }
        validateFile(files.get(0), sectionName);
    }

    /**
     * Validates a single file
     * 
     * @param file File to validate
     * @param sectionName Name of the section (for error messages)
     * @throws IllegalArgumentException if validation fails
     */
    public void validateFile(MultipartFile file, String sectionName) {
        if (file == null || file.isEmpty()) {
            return; // Null or empty files are allowed (optional documents)
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("File in %s section has no filename", sectionName)
            );
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                String.format("File '%s' in %s section has invalid file type. Only JPG, PNG, and JPEG files are allowed.",
                    originalFilename, sectionName)
            );
        }

        // Validate file size
        long fileSize = file.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File '%s' in %s section exceeds maximum file size of 10MB. File size: %.2f MB",
                    originalFilename, sectionName, fileSize / (1024.0 * 1024.0))
            );
        }

        log.debug("Validated file '{}' in {} section. Size: {} bytes", 
            originalFilename, sectionName, fileSize);
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}

package com.maxed.mediaservice.api;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing media files.
 */
public interface MediaService {

    /**
     * Uploads a file to the storage.
     *
     * @param file The file to upload.
     * @return A DTO with information about the uploaded file.
     */
    FileUploadResponse uploadFile(MultipartFile file);
}

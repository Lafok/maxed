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
     * @return A DTO containing the generated object name for the uploaded file.
     */
    FileUploadResponse uploadFile(MultipartFile file);

    /**
     * Uploads an avatar image to the storage.
     *
     * @param file The avatar file to upload.
     * @return The unique object name (key) for the uploaded avatar.
     */
    String uploadAvatar(MultipartFile file);

    /**
     * Generates a temporary, pre-signed URL to access a private object.
     *
     * @param objectName The name of the object in the storage.
     * @return A temporary URL that provides GET access to the file.
     */
    String getPresignedUrl(String objectName);
}

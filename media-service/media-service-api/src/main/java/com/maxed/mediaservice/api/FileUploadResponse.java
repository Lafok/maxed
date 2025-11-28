package com.maxed.mediaservice.api;

/**
 * DTO for the response after a successful file upload.
 *
 * @param fileUrl  The URL to access the uploaded file.
 * @param fileName The name of the file.
 */
public record FileUploadResponse(
    String fileUrl,
    String fileName
) {}

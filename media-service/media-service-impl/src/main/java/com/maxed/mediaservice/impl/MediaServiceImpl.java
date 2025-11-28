package com.maxed.mediaservice.impl;

import com.maxed.mediaservice.api.FileUploadResponse;
import com.maxed.mediaservice.api.MediaService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            // This is a placeholder. You should construct the URL based on your MinIO setup.
            String fileUrl = "http://localhost:9000/" + bucketName + "/" + fileName;
            return new FileUploadResponse(fileUrl, fileName);
        } catch (Exception e) {
            throw new RuntimeException("Error while uploading file to MinIO", e);
        }
    }
}

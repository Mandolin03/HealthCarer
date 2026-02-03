package com.evaruiz.healthcarer.service;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar el bucket de MinIO: " + e.getMessage());
        }
    }

    public String uploadImage(MultipartFile imageFile) throws Exception {
        // Asegurar que el bucket existe
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = "";
        int dotIndex = originalFileName != null ? originalFileName.lastIndexOf('.') : -1;
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        String fileName = UUID.randomUUID() + fileExtension;

        // Subir a MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(imageFile.getInputStream(), imageFile.getSize(), -1)
                        .contentType(imageFile.getContentType())
                        .build()
        );

        return fileName;
    }

    public void deleteImageFile(String fileName) throws Exception {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    // En lugar de devolver un recurso de sistema de archivos,
    // obtenemos un InputStream del objeto en MinIO
    public InputStream getImageFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }
}

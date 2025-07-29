package com.evaruiz.healthcarer.service;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {

    private String UPLOAD_DIRECTORY = "";

    public void setUploadDirectory() {
        if(System.getProperty("profile") != null && System.getProperty("profile").equals("test")) {
            this.UPLOAD_DIRECTORY = "uploads/";
        } else{
            this.UPLOAD_DIRECTORY = "/uploads/";
        }
    }

    public String uploadImage(MultipartFile imageFile) throws IOException {
        setUploadDirectory();
        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = "";
        int dotIndex = originalFileName != null ? originalFileName.lastIndexOf('.') : -1;
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String fileName = UUID.randomUUID() + fileExtension;
        Path uploadPath = Path.of(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public void deleteImageFile(String imagePath) throws IOException {
        setUploadDirectory();
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        String localFileName = imagePath.replace("/uploads/", "");
        Path filePath = Path.of(UPLOAD_DIRECTORY).resolve(localFileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new IOException("No se ha encontrado la imagen: " + imagePath);
        }
    }

    public FileSystemResource getImageFile(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        String localFileName = imagePath.replace("/uploads/", "");
        Path filePath = Paths.get(UPLOAD_DIRECTORY, localFileName);
        if (Files.exists(filePath)) {
            return new FileSystemResource(filePath);
        } else {
            return null;
        }
    }

}

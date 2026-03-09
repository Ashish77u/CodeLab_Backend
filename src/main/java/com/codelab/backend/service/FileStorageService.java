package com.codelab.backend.service;


import com.codelab.backend.exception.project.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("images"));
            Files.createDirectories(rootLocation.resolve("zips"));
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directories");
        }
    }

    public String storeImage(MultipartFile file) {
        validateImageFile(file);
        String filename = generateFilename(file.getOriginalFilename());
        Path target = rootLocation.resolve("images").resolve(filename);
        saveFile(file, target);
        return "/uploads/images/" + filename;
    }

    public String storeZip(MultipartFile file) {
        validateZipFile(file);
        String filename = generateFilename(file.getOriginalFilename());
        Path target = rootLocation.resolve("zips").resolve(filename);
        saveFile(file, target);
        return "/uploads/zips/" + filename;
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            String relativePath = filePath.replaceFirst("^/uploads/", "");
            Path file = rootLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
            throw new FileStorageException("File not found: " + filePath);
        } catch (MalformedURLException e) {
            throw new FileStorageException("Invalid file path: " + filePath);
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) return;
        try {
            String relativePath = filePath.replaceFirst("^/uploads/", "");
            Path file = rootLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) { /* log but don't throw */ }
    }

    private void saveFile(MultipartFile file, Path target) {
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }

    private String generateFilename(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename != null ? originalFilename : "file");
        return UUID.randomUUID().toString().substring(0, 8) + "_" + cleaned;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new FileStorageException("Cover image cannot be empty");
        List<String> allowed = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
        if (!allowed.contains(file.getContentType())) throw new FileStorageException("Invalid image type");
        if (file.getSize() > 5 * 1024 * 1024) throw new FileStorageException("Image must be under 5MB");
    }

    private void validateZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new FileStorageException("ZIP file cannot be empty");
        List<String> allowed = Arrays.asList("application/zip", "application/x-zip-compressed", "application/octet-stream");
        if (!allowed.contains(file.getContentType())) throw new FileStorageException("Must be a ZIP");
        if (file.getSize() > 50 * 1024 * 1024) throw new FileStorageException("ZIP must be under 50MB");
    }
}
package com.codelab.backend.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // ── Upload Image ──────────────────────────────────────
    public String uploadImage(MultipartFile file) {
        try {
            String publicId = "codelab/images/" + UUID.randomUUID();

            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "image",
                            "overwrite", true
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("Image uploaded to Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    // ── Upload ZIP File ───────────────────────────────────
    public String uploadZip(MultipartFile file) {
        try {
            String publicId = "codelab/zips/" + UUID.randomUUID();

            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "raw",  // raw = non-image files
                            "overwrite", true
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("ZIP uploaded to Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Failed to upload ZIP to Cloudinary", e);
            throw new RuntimeException("ZIP upload failed: " + e.getMessage());
        }
    }

    // ── Delete File ───────────────────────────────────────
    public void deleteFile(String url, String resourceType) {
        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/cloud/resource_type/upload/v123/public_id.ext
            if (url == null || url.isBlank()) return;

            String publicId = extractPublicId(url);
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
            log.info("Deleted from Cloudinary: {}", publicId);

        } catch (Exception e) {
            log.warn("Failed to delete from Cloudinary: {}", e.getMessage());
        }
    }

    private String extractPublicId(String url) {
        // Remove query params
        String cleanUrl = url.split("\\?")[0];
        // Get everything after /upload/
        String[] parts = cleanUrl.split("/upload/");
        if (parts.length < 2) return "";
        // Remove version prefix (v1234567/) if present
        String withVersion = parts[1];
        String publicIdWithExt = withVersion.replaceFirst("v\\d+/", "");
        // Remove file extension
        int dotIndex = publicIdWithExt.lastIndexOf('.');
        return dotIndex > 0
                ? publicIdWithExt.substring(0, dotIndex)
                : publicIdWithExt;
    }
}
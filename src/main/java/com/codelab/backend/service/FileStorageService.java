package com.codelab.backend.service;


import com.codelab.backend.exception.project.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final CloudinaryService cloudinaryService;
    private final SupabaseStorageService supabaseStorageService;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;   // 5MB
    private static final long MAX_ZIP_SIZE   = 50 * 1024 * 1024;  // 50MB

    // ── Store Image → Cloudinary ──────────────────────────

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("Image file is empty");

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType()))
            throw new RuntimeException(
                    "Invalid image type. Allowed: JPG, PNG, WEBP");

        if (file.getSize() > MAX_IMAGE_SIZE)
            throw new RuntimeException(
                    "Image too large. Max size: 5MB");

        return cloudinaryService.uploadImage(file);
    }

    // ── Store ZIP → Supabase ──────────────────────────────

    public String storeZip(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("ZIP file is empty");

        if (file.getSize() > MAX_ZIP_SIZE)
            throw new RuntimeException(
                    "ZIP too large. Max size: 50MB");

        return supabaseStorageService.uploadZip(file);
    }

    // ── Delete File ───────────────────────────────────────

    public void deleteFile(String url) {
        if (url == null || url.isBlank()) return;

        if (url.contains("supabase.co")) {
            // ZIP stored in Supabase
            supabaseStorageService.deleteZip(url);
        } else {
            // Image stored in Cloudinary
            cloudinaryService.deleteFile(url, "image");
        }
    }
}




















//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class FileStorageService {
//
////    private final CloudinaryService cloudinaryService;
////
////    // Allowed image types
////    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
////            "image/jpeg", "image/png", "image/webp"
////    );
////
////    // Max sizes
////    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;   // 5MB
////    private static final long MAX_ZIP_SIZE   = 50 * 1024 * 1024;  // 50MB
////
////    // ── Store Image ───────────────────────────────────────
////
////    public String storeImage(MultipartFile file) {
////        // Validate
////        if (file == null || file.isEmpty()) {
////            throw new RuntimeException("Image file is empty");
////        }
////        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
////            throw new RuntimeException(
////                    "Invalid image type. Allowed: JPG, PNG, WEBP");
////        }
////        if (file.getSize() > MAX_IMAGE_SIZE) {
////            throw new RuntimeException(
////                    "Image too large. Max size: 5MB");
////        }
////
////        // Upload to Cloudinary
////        return cloudinaryService.uploadImage(file);
////    }
////
////    // ── Store ZIP ─────────────────────────────────────────
////
////    public String storeZip(MultipartFile file) {
////        // Validate
////        if (file == null || file.isEmpty()) {
////            throw new RuntimeException("ZIP file is empty");
////        }
////        if (file.getSize() > MAX_ZIP_SIZE) {
////            throw new RuntimeException(
////                    "ZIP too large. Max size: 50MB");
////        }
////
////        // Upload to Cloudinary
////        return cloudinaryService.uploadZip(file);
////    }
////
////    // ── Delete File ───────────────────────────────────────
////
////    public void deleteFile(String url) {
////        if (url == null || url.isBlank()) return;
////
////        // Determine resource type from URL
////        if (url.contains("/codelab/zips/")) {
////            cloudinaryService.deleteFile(url, "raw");
////        } else {
////            cloudinaryService.deleteFile(url, "image");
////        }
////    }
//}
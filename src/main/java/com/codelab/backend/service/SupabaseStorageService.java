package com.codelab.backend.service;


import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final OkHttpClient httpClient = new OkHttpClient();

    // ── Upload ZIP to Supabase ────────────────────────────

    public String uploadZip(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_"
                    + file.getOriginalFilename();

            String uploadUrl = supabaseUrl
                    + "/storage/v1/object/"
                    + bucket + "/" + filename;

            RequestBody body = RequestBody.create(
                    file.getBytes(),
                    MediaType.parse("application/zip")
            );

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(body)
                    .addHeader("Authorization",
                            "Bearer " + serviceRoleKey)
                    .addHeader("Content-Type", "application/zip")
                    .build();

            try (Response response = httpClient
                    .newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null
                            ? response.body().string() : "unknown";
                    throw new RuntimeException(
                            "Supabase upload failed: " + errorBody);
                }

                // Build public URL
                String publicUrl = supabaseUrl
                        + "/storage/v1/object/public/"
                        + bucket + "/" + filename;

                log.info("ZIP uploaded to Supabase: {}", publicUrl);
                return publicUrl;
            }

        } catch (IOException e) {
            log.error("Supabase upload error", e);
            throw new RuntimeException(
                    "ZIP upload failed: " + e.getMessage());
        }
    }

    // ── Delete ZIP from Supabase ──────────────────────────

    public void deleteZip(String publicUrl) {
        try {
            if (publicUrl == null || publicUrl.isBlank()) return;

            // Extract filename from URL
            String filename = publicUrl
                    .substring(publicUrl.lastIndexOf("/") + 1);

            String deleteUrl = supabaseUrl
                    + "/storage/v1/object/"
                    + bucket + "/" + filename;

            Request request = new Request.Builder()
                    .url(deleteUrl)
                    .delete()
                    .addHeader("Authorization",
                            "Bearer " + serviceRoleKey)
                    .build();

            try (Response response = httpClient
                    .newCall(request).execute()) {
                log.info("ZIP deleted from Supabase: {}",
                        filename);
            }

        } catch (IOException e) {
            log.warn("Supabase delete failed: {}", e.getMessage());
        }
    }
}
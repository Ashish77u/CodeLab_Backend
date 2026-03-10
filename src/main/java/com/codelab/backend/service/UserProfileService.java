package com.codelab.backend.service;

import com.codelab.backend.dto.request.UpdateProfileRequest;
import com.codelab.backend.dto.response.OwnProfileResponse;
import com.codelab.backend.dto.response.PublicProfileResponse;
import com.codelab.backend.entity.User;
import com.codelab.backend.exception.user.UserNotFoundException;
import com.codelab.backend.repository.ProjectRepository;
import com.codelab.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;

    // ── Public Profile (anyone can view) ─────────────────────

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User '@" + username + "' not found"));

        long totalProjects = projectRepository
                .countByUploaderAndPublishedTrue(user);

        long totalDownloads = projectRepository
                .sumDownloadCountByUploader(user);

        return toPublicResponse(user, totalProjects, totalDownloads);
    }

    // ── Own Profile (authenticated user only) ────────────────

    @Transactional(readOnly = true)
    public OwnProfileResponse getOwnProfile(User currentUser) {
        long totalProjects = projectRepository
                .countByUploaderAndPublishedTrue(currentUser);

        long totalDownloads = projectRepository
                .sumDownloadCountByUploader(currentUser);

        return toOwnResponse(currentUser, totalProjects, totalDownloads);
    }

    // ── Update Profile ────────────────────────────────────────

    @Transactional
    public OwnProfileResponse updateProfile(
            UpdateProfileRequest request, User currentUser) {

        // Only update fields that are NOT null in the request
        // This allows partial updates — frontend sends only changed fields
        if (request.realName() != null)
            currentUser.setRealName(request.realName().trim());

        if (request.bio() != null)
            currentUser.setBio(request.bio().trim());

        if (request.location() != null)
            currentUser.setLocation(request.location().trim());

        if (request.education() != null)
            currentUser.setEducation(request.education().trim());

        if (request.work() != null)
            currentUser.setWork(request.work().trim());

        if (request.websiteUrl() != null)
            currentUser.setWebsiteUrl(request.websiteUrl().trim());

        User saved = userRepository.save(currentUser);
        log.info("Profile updated for user '{}'", saved.getUsername());

        long totalProjects = projectRepository
                .countByUploaderAndPublishedTrue(saved);
        long totalDownloads = projectRepository
                .sumDownloadCountByUploader(saved);

        return toOwnResponse(saved, totalProjects, totalDownloads);
    }

    // ── Upload Profile Image ──────────────────────────────────

    @Transactional
    public OwnProfileResponse uploadProfileImage(
            MultipartFile image, User currentUser) {

        // Delete old profile image from disk if it exists
        if (currentUser.getProfileImageUrl() != null
                && !currentUser.getProfileImageUrl().isBlank()) {
            fileStorageService.deleteFile(currentUser.getProfileImageUrl());
        }

        // Store the new image
        String imageUrl = fileStorageService.storeImage(image);
        currentUser.setProfileImageUrl(imageUrl);
        User saved = userRepository.save(currentUser);

        log.info("Profile image updated for user '{}'", saved.getUsername());

        long totalProjects = projectRepository
                .countByUploaderAndPublishedTrue(saved);
        long totalDownloads = projectRepository
                .sumDownloadCountByUploader(saved);

        return toOwnResponse(saved, totalProjects, totalDownloads);
    }

    // ── Private: Entity → DTO Mappers ─────────────────────────

    private PublicProfileResponse toPublicResponse(
            User user, long totalProjects, long totalDownloads) {
        return new PublicProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getBio(),
                user.getLocation(),
                user.getEducation(),
                user.getWork(),
                user.getWebsiteUrl(),
                user.getProfileImageUrl(),
                user.getCreatedAt(),
                totalProjects,
                totalDownloads
        );
    }

    private OwnProfileResponse toOwnResponse(
            User user, long totalProjects, long totalDownloads) {
        return new OwnProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRealName(),
                user.getBio(),
                user.getLocation(),
                user.getEducation(),
                user.getWork(),
                user.getWebsiteUrl(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getCreatedAt(),
                totalProjects,
                totalDownloads
        );
    }
}
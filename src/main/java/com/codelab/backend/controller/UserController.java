package com.codelab.backend.controller;

import com.codelab.backend.dto.request.UpdateProfileRequest;
import com.codelab.backend.dto.response.OwnProfileResponse;
import com.codelab.backend.dto.response.ProjectCardResponse;
import com.codelab.backend.dto.response.PublicProfileResponse;
import com.codelab.backend.entity.User;
import com.codelab.backend.service.ProjectService;
import com.codelab.backend.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final ProjectService projectService;

    // ── PUBLIC endpoints ──────────────────────────────────────

    // Anyone can view a user's public profile
    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(
            @PathVariable String username) {
        return ResponseEntity.ok(
                userProfileService.getPublicProfile(username));
    }

    // Anyone can view a user's published projects
    // Used on profile page to show project cards
    @GetMapping("/{username}/projects")
    public ResponseEntity<Page<ProjectCardResponse>> getUserProjects(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(
                projectService.getProjectsByUsername(username, page, size));
    }

    // ── AUTHENTICATED endpoints ───────────────────────────────

    // Get own full profile (includes email, role)
    @GetMapping("/me")
    public ResponseEntity<OwnProfileResponse> getMyProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                userProfileService.getOwnProfile(currentUser));
    }

    // Update own profile info
    @PutMapping("/me")
    public ResponseEntity<OwnProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                userProfileService.updateProfile(request, currentUser));
    }

    // Upload own profile image
    @PostMapping(value = "/me/profile-image",
            consumes = "multipart/form-data")
    public ResponseEntity<OwnProfileResponse> uploadProfileImage(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                userProfileService.uploadProfileImage(image, currentUser));
    }
}
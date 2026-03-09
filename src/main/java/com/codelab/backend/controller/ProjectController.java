package com.codelab.backend.controller;

// ... (full ProjectController code — see original)
// Endpoints: GET /projects, GET /projects/{id}, POST /projects,
// PUT /projects/{id}, DELETE /projects/{id}, GET /projects/{id}/download,
// GET /projects/search, GET /projects/user/{username}

 
import com.codelab.backend.dto.request.ProjectUploadRequest;
import com.codelab.backend.dto.response.ProjectCardResponse;
import com.codelab.backend.dto.response.ProjectResponse;
import com.codelab.backend.entity.User;
import com.codelab.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ── PUBLIC ────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<ProjectCardResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(projectService.getAllPublished(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<ProjectCardResponse>> getProjectsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(
                projectService.getProjectsByUsername(username, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProjectCardResponse>> searchProjects(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(projectService.searchProjects(q, page, size));
    }

    // ── AUTHENTICATED ─────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectResponse> uploadProject(
            @Valid @ModelAttribute ProjectUploadRequest request,
            @RequestPart("coverImage") MultipartFile coverImage,
            @RequestPart("zipFile") MultipartFile zipFile,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.uploadProject(request, coverImage, zipFile, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUploadRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.updateProject(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        Resource resource = projectService.downloadProject(id, currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
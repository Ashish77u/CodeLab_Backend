package com.codelab.backend.service;

// ... (full ProjectService code — see original)
// Contains: uploadProject, getAllPublished, getProjectById,
// getProjectsByUsername, searchProjects, downloadProject,
// deleteProject, updateProject, parseTags, toProjectResponse, toCardResponse


import com.codelab.backend.dto.request.ProjectUploadRequest;
import com.codelab.backend.dto.response.ProjectCardResponse;
import com.codelab.backend.dto.response.ProjectResponse;
import com.codelab.backend.entity.Project;
import com.codelab.backend.entity.Tag;
import com.codelab.backend.entity.User;
import com.codelab.backend.exception.project.AccessDeniedException;
import com.codelab.backend.exception.project.ProjectNotFoundException;
import com.codelab.backend.repository.ProjectRepository;
import com.codelab.backend.repository.TagRepository;
import com.codelab.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProjectResponse uploadProject(
            ProjectUploadRequest request,
            MultipartFile coverImage,
            MultipartFile zipFile,
            User currentUser) {

        String coverUrl = fileStorageService.storeImage(coverImage);
        String zipUrl   = fileStorageService.storeZip(zipFile);
        Set<Tag> tags   = parseTags(request.tags());

        Project project = Project.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .about(request.about().trim())
                .coverImageUrl(coverUrl)
                .zipFileUrl(zipUrl)
                .zipFileName(zipFile.getOriginalFilename())
                .zipFileSize(zipFile.getSize())
                .uploader(currentUser)
                .tags(tags)
                .published(true)
                .build();

        Project saved = projectRepository.save(project);
        log.info("Project '{}' uploaded by '{}'", saved.getTitle(), currentUser.getUsername());
        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProjectCardResponse> getAllPublished(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectRepository
                .findByPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(this::toCardResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findByIdWithUploader(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));
        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectCardResponse> getProjectsByUsername(
            String username, int page, int size) {
//        User user = userRepository.findByEmail(username)
        User user = userRepository.findByUsername(username)     // find by username
                .orElseThrow(() -> new UsernameNotFoundException("User '@" + username + "' not found"));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return projectRepository

                .findByUploaderAndPublishedTrueOrderByCreatedAtDesc(user, pageable)
                .map(this::toCardResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProjectCardResponse> searchProjects(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectRepository
                .findByTitleContainingIgnoreCaseAndPublishedTrue(query, pageable)
                .map(this::toCardResponse);
    }

//    @Transactional
//    public Resource downloadProject(Long id, User currentUser) {
//        Project project = projectRepository.findById(id)
//                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));
//        if (!project.isPublished())
//            throw new ProjectNotFoundException("Project not available");
//        project.setDownloadCount(project.getDownloadCount() + 1);
//        projectRepository.save(project);
//        log.info("Project '{}' downloaded by '{}'", project.getTitle(), currentUser.getUsername());
//        return fileStorageService.loadFileAsResource(project.getZipFileUrl());
//    }

    @Transactional
    public String getDownloadUrl(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ProjectNotFoundException("Project not found: " + id));

        if (!project.isPublished())
            throw new ProjectNotFoundException("Project not available");

        // Increment download count
        project.setDownloadCount(project.getDownloadCount() + 1);
        projectRepository.save(project);

        log.info("Project '{}' downloaded by '{}'",
                project.getTitle(), currentUser.getUsername());

        // Return Cloudinary URL directly
        return project.getZipFileUrl();
    }

    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Project not found with id: " + id));

        // Check ownership
        boolean isOwner = project.getUploader().getId()
                .equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You can only delete your own projects");
        }

        // Delete files from Cloudinary
        fileStorageService.deleteFile(project.getCoverImageUrl());
        fileStorageService.deleteFile(project.getZipFileUrl());

        projectRepository.delete(project);
        log.info("Project deleted: {} by {}", id, currentUser.getUsername());
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUploadRequest request, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));
        if (!project.getUploader().getId().equals(currentUser.getId()))
            throw new AccessDeniedException("You can only edit your own projects");
        project.setTitle(request.title().trim());
        project.setDescription(request.description().trim());
        project.setAbout(request.about().trim());
        project.setTags(parseTags(request.tags()));
        return toProjectResponse(projectRepository.save(project));
    }

    private Set<Tag> parseTags(String tagsString) {
        if (tagsString == null || tagsString.isBlank()) return Set.of();
        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(name -> !name.isBlank())
                .limit(10)
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toSet());
    }

//    private ProjectResponse toProjectResponse(Project p) {
//        Set<String> tagNames = p.getTags().stream()
//                .map(Tag::getName).collect(Collectors.toSet());
//        return new ProjectResponse(
//                p.getId(), p.getTitle(), p.getDescription(), p.getAbout(),
//                p.getCoverImageUrl(), p.getZipFileName(), p.getZipFileSize(),
//                p.getUploader().getUsername(), p.getUploader().getRealName(),
//                p.getUploader().getProfileImageUrl(), tagNames,
//                p.getDownloadCount(), p.isPublished(), p.getCreatedAt(), p.getUpdatedAt()
//        );
//    }
private ProjectResponse toProjectResponse(Project project) {
    User uploader = project.getUploader();
    return new ProjectResponse(
            project.getId(),
            project.getTitle(),
            project.getDescription(),
            project.getAbout(),
            project.getCoverImageUrl(),
            project.getZipFileName(),
            project.getZipFileUrl(),      // ← ADD THIS
            project.getDownloadCount(),
            project.getCreatedAt(),
            project.getTags().stream().map(Tag::getName).toList(),
            uploader.getUsername(),
            uploader.getRealName(),
            uploader.getBio(),
            uploader.getLocation(),
            uploader.getEducation(),
            uploader.getWork(),
            uploader.getProfileImageUrl(),
            uploader.getCreatedAt()
    );
}

    private ProjectCardResponse toCardResponse(Project p) {
        Set<String> tagNames = p.getTags().stream()
                .map(Tag::getName).collect(Collectors.toSet());
        String shortDesc = p.getDescription() != null && p.getDescription().length() > 150
                ? p.getDescription().substring(0, 150) + "..."
                : p.getDescription();
        return new ProjectCardResponse(
                p.getId(), p.getTitle(), shortDesc, p.getCoverImageUrl(),
                p.getUploader().getUsername(), p.getUploader().getProfileImageUrl(),
                tagNames, p.getDownloadCount(), p.getCreatedAt()
        );
    }
}



/*

    package com.codelab.backend.service;

// ... (full ProjectService code — see original)
// Contains: uploadProject, getAllPublished, getProjectById,
// getProjectsByUsername, searchProjects, downloadProject,
// deleteProject, updateProject, parseTags, toProjectResponse, toCardResponse


import com.codelab.backend.dto.request.ProjectUploadRequest;
import com.codelab.backend.dto.response.ProjectCardResponse;
import com.codelab.backend.dto.response.ProjectResponse;
import com.codelab.backend.entity.Project;
import com.codelab.backend.entity.Tag;
import com.codelab.backend.entity.User;
import com.codelab.backend.exception.project.AccessDeniedException;
import com.codelab.backend.exception.project.ProjectNotFoundException;
import com.codelab.backend.repository.ProjectRepository;
import com.codelab.backend.repository.TagRepository;
import com.codelab.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProjectResponse uploadProject(
            ProjectUploadRequest request,
            MultipartFile coverImage,
            MultipartFile zipFile,
            User currentUser) {

        String coverUrl = fileStorageService.storeImage(coverImage);
        String zipUrl   = fileStorageService.storeZip(zipFile);
//        String coverImageUrl = fileStorageService.storeImage(coverImage);
//        String zipFileUrl    = fileStorageService.storeZip(zipFile);
        Set<Tag> tags   = parseTags(request.tags());

        Project project = Project.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .about(request.about().trim())
                .coverImageUrl(coverUrl)
                .zipFileUrl(zipUrl)
                .zipFileName(zipFile.getOriginalFilename())
                .zipFileSize(zipFile.getSize())
                .uploader(currentUser)
                .tags(tags)
                .published(true)
                .build();

        Project saved = projectRepository.save(project);
        log.info("Project '{}' uploaded by '{}'", saved.getTitle(), currentUser.getUsername());
        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProjectCardResponse> getAllPublished(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectRepository
                .findByPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(this::toCardResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findByIdWithUploader(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));
        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectCardResponse> getProjectsByUsername(
            String username, int page, int size) {
//        User user = userRepository.findByEmail(username)
        User user = userRepository.findByUsername(username)     // find by username
                .orElseThrow(() -> new UsernameNotFoundException("User '@" + username + "' not found"));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return projectRepository

                .findByUploaderAndPublishedTrueOrderByCreatedAtDesc(user, pageable)
                .map(this::toCardResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProjectCardResponse> searchProjects(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectRepository
                .findByTitleContainingIgnoreCaseAndPublishedTrue(query, pageable)
                .map(this::toCardResponse);
    }

    @Transactional
    public String getDownloadUrl(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ProjectNotFoundException("Project not found: " + id));

        if (!project.isPublished())
            throw new ProjectNotFoundException("Project not available");

        // Increment download count
        project.setDownloadCount(project.getDownloadCount() + 1);
        projectRepository.save(project);

        log.info("Project '{}' downloaded by '{}'",
                project.getTitle(), currentUser.getUsername());

        // Return Cloudinary URL directly
        return project.getZipFileUrl();
    }

    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));

        boolean isOwner = project.getUploader().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin)
            throw new AccessDeniedException("You don't have permission to delete this project");

        fileStorageService.deleteFile(project.getCoverImageUrl());
        fileStorageService.deleteFile(project.getZipFileUrl());
        projectRepository.delete(project);
        log.info("Project '{}' deleted by '{}'", project.getTitle(), currentUser.getUsername());
    }

    @Transactional
    public ProjectResponse uploadProject(
            String title,
            String description,
            String about,
            String tags,
            MultipartFile coverImage,
            MultipartFile zipFile,
            User uploader) {

        // Upload files to Cloudinary
        String coverImageUrl = fileStorageService.storeImage(coverImage);
        String zipFileUrl    = fileStorageService.storeZip(zipFile);

        // Parse tags
//        List<Tag> tagList = parseTags(tags);
          List<Tag> tagList = (List<Tag>) parseTags(tags);

        // Build project
        Project project = Project.builder()
                .title(title.trim())
                .description(description.trim())
                .about(about != null ? about.trim() : null)
                .coverImageUrl(coverImageUrl)
                .zipFileUrl(zipFileUrl)
                .zipFileName(zipFile.getOriginalFilename())
                .zipFileSize(zipFile.getSize())
                .uploader(uploader)
                .tags(new HashSet<>(tagList))
                .published(true)
                .downloadCount(0)
                .build();

        Project saved = projectRepository.save(project);
        log.info("Project uploaded: {} by {}", saved.getTitle(),
                uploader.getUsername());

        return toProjectResponse(saved);
    }

    private Set<Tag> parseTags(String tagsString) {
        if (tagsString == null || tagsString.isBlank()) return Set.of();
        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(name -> !name.isBlank())
                .limit(10)
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toSet());
    }

//    private ProjectResponse toProjectResponse(Project p) {
//        Set<String> tagNames = p.getTags().stream()
//                .map(Tag::getName).collect(Collectors.toSet());
//        return new ProjectResponse(
//                p.getId(), p.getTitle(), p.getDescription(), p.getAbout(),
//                p.getCoverImageUrl(), p.getZipFileName(), p.getZipFileSize(),
//                p.getUploader().getUsername(), p.getUploader().getRealName(),
//                p.getUploader().getProfileImageUrl(), tagNames,
//                p.getDownloadCount(), p.isPublished(), p.getCreatedAt(), p.getUpdatedAt()
//        );
//    }
private ProjectResponse toProjectResponse(Project project) {
    User uploader = project.getUploader();
    return new ProjectResponse(
            project.getId(),
            project.getTitle(),
            project.getDescription(),
            project.getAbout(),
            project.getCoverImageUrl(),
            project.getZipFileName(),
            project.getZipFileUrl(),      // ← ADD THIS
            project.getDownloadCount(),
            project.getCreatedAt(),
            project.getTags().stream().map(Tag::getName).toList(),
            uploader.getUsername(),
            uploader.getRealName(),
            uploader.getBio(),
            uploader.getLocation(),
            uploader.getEducation(),
            uploader.getWork(),
            uploader.getProfileImageUrl(),
            uploader.getCreatedAt()
    );
}

    private ProjectCardResponse toCardResponse(Project p) {
        Set<String> tagNames = p.getTags().stream()
                .map(Tag::getName).collect(Collectors.toSet());
        String shortDesc = p.getDescription() != null && p.getDescription().length() > 150
                ? p.getDescription().substring(0, 150) + "..."
                : p.getDescription();
        return new ProjectCardResponse(
                p.getId(), p.getTitle(), shortDesc, p.getCoverImageUrl(),
                p.getUploader().getUsername(), p.getUploader().getProfileImageUrl(),
                tagNames, p.getDownloadCount(), p.getCreatedAt()
        );
    }
}


 */
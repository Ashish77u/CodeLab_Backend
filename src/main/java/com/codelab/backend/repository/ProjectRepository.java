package com.codelab.backend.repository;

import com.codelab.backend.entity.Project;
import com.codelab.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Project> findByUploaderAndPublishedTrueOrderByCreatedAtDesc(User uploader, Pageable pageable);
    Page<Project> findByTitleContainingIgnoreCaseAndPublishedTrue(String title, Pageable pageable);

    @Query("SELECT p FROM Project p JOIN p.tags t WHERE LOWER(t.name) = LOWER(:tag) AND p.published = true ORDER BY p.createdAt DESC")
    Page<Project> findByTagName(@Param("tag") String tag, Pageable pageable);

    long countByUploaderAndPublishedTrue(User uploader);

    @Query("SELECT COALESCE(SUM(p.downloadCount), 0) FROM Project p WHERE p.uploader = :uploader")
    long sumDownloadCountByUploader(@Param("uploader") User uploader);

    @Query("SELECT p FROM Project p JOIN FETCH p.uploader WHERE p.id = :id AND p.published = true")
    Optional<Project> findByIdWithUploader(@Param("id") Long id);

    Page<Project> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

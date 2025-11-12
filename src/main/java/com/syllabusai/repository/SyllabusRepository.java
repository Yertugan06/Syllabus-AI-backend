package com.syllabusai.repository;

import com.syllabusai.model.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {

    List<Syllabus> findByUserId(Long userId);

    List<Syllabus> findByUserIdOrderByUploadDateDesc(Long userId);

    @Query("SELECT s FROM Syllabus s LEFT JOIN FETCH s.topics WHERE s.id = :id")
    Optional<Syllabus> findByIdWithTopics(@Param("id") Long id);

    @Query("SELECT s FROM Syllabus s LEFT JOIN FETCH s.deadlines WHERE s.id = :id")
    Optional<Syllabus> findByIdWithDeadlines(@Param("id") Long id);

    @Query("SELECT s FROM Syllabus s LEFT JOIN FETCH s.materials WHERE s.id = :id")
    Optional<Syllabus> findByIdWithMaterials(@Param("id") Long id);

    boolean existsByUserIdAndFilename(Long userId, String filename);
}
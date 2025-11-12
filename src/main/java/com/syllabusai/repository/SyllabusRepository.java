package com.syllabusai.repository;

import com.syllabusai.model.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {

    List<Syllabus> findByUserIdOrderByUploadDateDesc(Long userId);

    long countByUserId(Long userId);

    boolean existsByUserIdAndId(Long userId, Long syllabusId);
}
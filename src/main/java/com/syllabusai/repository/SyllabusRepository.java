package com.syllabusai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.syllabusai.model.*;

import java.util.List;


public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {
    List<Syllabus> findByUserId(Long userId);

    List<Syllabus> findByFilenameContaining(String filename);
}
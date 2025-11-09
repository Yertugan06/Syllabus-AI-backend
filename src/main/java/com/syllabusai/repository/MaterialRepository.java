package com.syllabusai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.syllabusai.model.*;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findBySyllabusId(Long syllabusId);
}
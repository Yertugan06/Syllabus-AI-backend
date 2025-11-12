package com.syllabusai.repository;

import com.syllabusai.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findBySyllabusId(Long syllabusId);
    List<Material> findBySyllabusIdAndType(Long syllabusId, Material.MaterialType type);
}
package com.syllabusai.service;

import com.syllabusai.dto.MaterialDTO;
import com.syllabusai.mapper.MaterialMapper;
import com.syllabusai.model.Material;
import com.syllabusai.repository.MaterialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public Material saveMaterial(Material material) {
        return materialRepository.save(material);
    }

    public List<Material> getMaterialsBySyllabusId(Long syllabusId) {
        return materialRepository.findBySyllabusId(syllabusId);
    }

    public MaterialDTO toDTO(Material material) {
        return MaterialMapper.toDTO(material);
    }

    public List<Material> saveAll(List<Material> materials) {
        return materialRepository.saveAll(materials);
    }
}
package com.syllabusai.service;

import com.syllabusai.model.Material;
import com.syllabusai.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    public List<Material> getMaterialsBySyllabusId(Long syllabusId) {
        return materialRepository.findBySyllabusId(syllabusId);
    }
}
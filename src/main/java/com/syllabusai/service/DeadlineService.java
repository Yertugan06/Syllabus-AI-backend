package com.syllabusai.service;

import com.syllabusai.dto.DeadlineDTO;
import com.syllabusai.mapper.DeadlineMapper;
import com.syllabusai.model.Deadline;
import com.syllabusai.repository.DeadlineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeadlineService {

    private final DeadlineRepository deadlineRepository;

    public DeadlineService(DeadlineRepository deadlineRepository) {
        this.deadlineRepository = deadlineRepository;
    }

    public Deadline saveDeadline(Deadline deadline) {
        return deadlineRepository.save(deadline);
    }

    public List<Deadline> getDeadlinesBySyllabusId(Long syllabusId) {
        return deadlineRepository.findBySyllabusId(syllabusId);
    }

    public DeadlineDTO toDTO(Deadline deadline) {
        return DeadlineMapper.toDTO(deadline);
    }

    public List<Deadline> saveAll(List<Deadline> deadlines) {
        return deadlineRepository.saveAll(deadlines);
    }
}
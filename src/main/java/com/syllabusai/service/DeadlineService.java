package com.syllabusai.service;

import com.syllabusai.model.Deadline;
import com.syllabusai.repository.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeadlineService {

    private final DeadlineRepository deadlineRepository;

    public List<Deadline> getDeadlinesBySyllabusId(Long syllabusId) {
        return deadlineRepository.findBySyllabusIdOrderByDateAsc(syllabusId);
    }

    public List<Deadline> getUpcomingDeadlines(Long syllabusId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonth = now.plusMonths(1);
        return deadlineRepository.findUpcomingDeadlines(syllabusId, now, nextMonth);
    }
}
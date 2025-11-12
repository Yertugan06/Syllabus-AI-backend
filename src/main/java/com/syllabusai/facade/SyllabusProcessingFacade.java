package com.syllabusai.facade;

import com.syllabusai.decorator.AIDifficultyDecorator;
import com.syllabusai.decorator.BasicSyllabusContent;
import com.syllabusai.decorator.SyllabusContent;
import com.syllabusai.dto.SyllabusDTO;
import com.syllabusai.dto.SyllabusOverviewDTO;
import com.syllabusai.dto.TopicDTO;
import com.syllabusai.dto.DeadlineDTO;
import com.syllabusai.dto.MaterialDTO;
import com.syllabusai.mapper.TopicMapper;
import com.syllabusai.mapper.DeadlineMapper;
import com.syllabusai.mapper.MaterialMapper;
import com.syllabusai.model.*;
import com.syllabusai.observer.FileProcessingSubject;
import com.syllabusai.parser.SyllabusParser;
import com.syllabusai.parser.SyllabusParserFactory;
import com.syllabusai.service.SyllabusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyllabusProcessingFacade {

    private final SyllabusService syllabusService;
    private final SyllabusParserFactory parserFactory;
    private final AIDifficultyDecorator aiDifficultyDecorator;
    private final FileProcessingSubject progressSubject;

    public SyllabusDTO processSyllabusUpload(MultipartFile file, String userEmail) {
        log.info("Starting syllabus processing for user: {}", userEmail);

        try {
            progressSubject.notifyProgress(10, "Starting file processing");

            // Use the existing service method that works
            progressSubject.notifyProgress(50, "Uploading and parsing syllabus");
            SyllabusDTO result = syllabusService.uploadAndParse(file, userEmail);

            progressSubject.notifyProgress(100, "Syllabus processed successfully");

            log.info("Syllabus processing completed: ID {}", result.getId());
            return result;

        } catch (Exception e) {
            log.error("Syllabus processing failed for user: {}", userEmail, e);
            progressSubject.notifyError("Processing failed: " + e.getMessage());
            throw new RuntimeException("Syllabus processing failed: " + e.getMessage(), e);
        }
    }

    public SyllabusOverviewDTO getSyllabusOverview(Long syllabusId) {
        try {
            // Get data using existing service methods
            List<Topic> topics = syllabusService.getTopicsBySyllabusId(syllabusId);
            List<Deadline> deadlines = syllabusService.getDeadlinesBySyllabusId(syllabusId);
            List<Material> materials = syllabusService.getMaterialsBySyllabusId(syllabusId);

            // Get syllabus info from topics (first topic's syllabus)
            Syllabus syllabus = null;
            if (!topics.isEmpty()) {
                syllabus = topics.get(0).getSyllabus();
            } else if (!deadlines.isEmpty()) {
                syllabus = deadlines.get(0).getSyllabus();
            } else if (!materials.isEmpty()) {
                syllabus = materials.get(0).getSyllabus();
            }

            if (syllabus == null) {
                throw new RuntimeException("Syllabus not found or has no content");
            }

            // Convert to DTOs
            SyllabusDTO syllabusDTO = convertToSyllabusDTO(syllabus);
            List<TopicDTO> topicDTOs = topics.stream()
                    .map(TopicMapper::toDTO)
                    .collect(Collectors.toList());

            List<DeadlineDTO> deadlineDTOs = deadlines.stream()
                    .map(DeadlineMapper::toDTO)
                    .collect(Collectors.toList());

            List<MaterialDTO> materialDTOs = materials.stream()
                    .map(MaterialMapper::toDTO)
                    .collect(Collectors.toList());

            List<DeadlineDTO> upcomingDeadlineDTOs = getUpcomingDeadlines(deadlines).stream()
                    .map(DeadlineMapper::toDTO)
                    .collect(Collectors.toList());

            return SyllabusOverviewDTO.builder()
                    .syllabus(syllabusDTO)
                    .topics(topicDTOs)
                    .deadlines(deadlineDTOs)
                    .materials(materialDTOs)
                    .totalWeeks(calculateTotalWeeks(topics))
                    .upcomingDeadlines(upcomingDeadlineDTOs)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get syllabus overview: " + e.getMessage(), e);
        }
    }

    private SyllabusDTO convertToSyllabusDTO(Syllabus syllabus) {
        return SyllabusDTO.builder()
                .id(syllabus.getId())
                .fileName(syllabus.getFilename())
                .uploadDate(syllabus.getUploadDate())
                .build();
    }

    private int calculateTotalWeeks(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return 0;
        }
        return topics.stream()
                .mapToInt(topic -> topic.getWeek() != null ? topic.getWeek() : 0)
                .max()
                .orElse(0);
    }

    private List<Deadline> getUpcomingDeadlines(List<Deadline> deadlines) {
        if (deadlines == null || deadlines.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        return deadlines.stream()
                .filter(deadline -> deadline.getDate().isAfter(now))
                .sorted(Comparator.comparing(Deadline::getDate))
                .limit(5)
                .collect(Collectors.toList());
    }
}
package com.syllabusai.service;

import com.syllabusai.decorator.*;
import com.syllabusai.observer.FileProcessingSubject;
import com.syllabusai.adapter.AIService;
import com.syllabusai.adapter.CalendarService;
import com.syllabusai.builder.SyllabusBuilder;
import com.syllabusai.strategy.ExtractionContext;
import com.syllabusai.dto.SyllabusDTO;
import com.syllabusai.mapper.SyllabusMapper;
import com.syllabusai.model.Syllabus;
import com.syllabusai.parser.ParserFactory;
import com.syllabusai.parser.SyllabusParser;
import com.syllabusai.repository.SyllabusRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnhancedSyllabusService {

    private final ParserFactory parserFactory;
    private final SyllabusRepository syllabusRepository;
    private final TopicService topicService;
    private final DeadlineService deadlineService;
    private final MaterialService materialService;
    private final UserService userService;
    private final FileProcessingSubject progressSubject;
    private final AIService aiService;
    private final CalendarService calendarService;
    private final ExtractionContext extractionContext;

    public EnhancedSyllabusService(ParserFactory parserFactory,
                                   SyllabusRepository syllabusRepository,
                                   TopicService topicService,
                                   DeadlineService deadlineService,
                                   MaterialService materialService,
                                   UserService userService,
                                   FileProcessingSubject progressSubject,
                                   AIService aiService,
                                   CalendarService calendarService,
                                   ExtractionContext extractionContext) {
        this.parserFactory = parserFactory;
        this.syllabusRepository = syllabusRepository;
        this.topicService = topicService;
        this.deadlineService = deadlineService;
        this.materialService = materialService;
        this.userService = userService;
        this.progressSubject = progressSubject;
        this.aiService = aiService;
        this.calendarService = calendarService;
        this.extractionContext = extractionContext;
    }

    public SyllabusDTO uploadAndParseWithProgress(MultipartFile file) {
        try {
            progressSubject.notifyProgress(10, "Starting file processing...");

            // Validate file
            if (file.isEmpty()) {
                progressSubject.notifyError("File is empty");
                throw new RuntimeException("File is empty");
            }

            progressSubject.notifyProgress(30, "Detecting university type...");

            // Get appropriate parser using Factory Method
            SyllabusParser parser = parserFactory.createParser(file);

            progressSubject.notifyProgress(50, "Parsing syllabus content...");

            // Parse the syllabus
            Syllabus syllabus = parser.parse(file);

            progressSubject.notifyProgress(70, "Enhancing content with decorators...");

            // Apply Decorator Pattern for content enhancement
            SyllabusContent basicContent = new BasicSyllabusContent(syllabus, "Raw content would go here");
            SyllabusContent enhancedContent = new DateFormattedDecorator(
                    new TopicEnrichmentDecorator(
                            new DeadlineHighlightDecorator(basicContent)
                    )
            );

            // Use Builder Pattern to construct syllabus
            SyllabusBuilder builder = new SyllabusBuilder()
                    .withFilename(file.getOriginalFilename())
                    .withUser(userService.getOrCreateDefaultUser())
                    .withStatus("PARSED")
                    .withTopics(enhancedContent.getTopics())
                    .withDeadlines(syllabus.getDeadlines())
                    .withMaterials(syllabus.getMaterials());

            Syllabus builtSyllabus = builder.build();

            progressSubject.notifyProgress(80, "Saving to database...");

            // Save to database
            Syllabus savedSyllabus = syllabusRepository.save(builtSyllabus);

            // Save related entities
            if (savedSyllabus.getTopics() != null) {
                topicService.saveAll(savedSyllabus.getTopics());
            }

            if (savedSyllabus.getDeadlines() != null) {
                deadlineService.saveAll(savedSyllabus.getDeadlines());

                // Use Adapter Pattern to sync with calendar
                savedSyllabus.getDeadlines().forEach(calendarService::addDeadline);
            }

            if (savedSyllabus.getMaterials() != null) {
                materialService.saveAll(savedSyllabus.getMaterials());
            }

            progressSubject.notifyProgress(100, "Processing complete!");
            progressSubject.notifyComplete("Syllabus processed successfully");

            return SyllabusMapper.toDTO(savedSyllabus);

        } catch (Exception e) {
            progressSubject.notifyError("Failed to process syllabus: " + e.getMessage());
            throw new RuntimeException("Failed to parse syllabus: " + e.getMessage(), e);
        }
    }

    // Other methods remain the same...
    public SyllabusDTO getSyllabus(Long id) {
        Syllabus syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Syllabus not found with id: " + id));
        return SyllabusMapper.toDTO(syllabus);
    }

    public List<TopicDTO> getTopicsBySyllabusId(Long syllabusId) {
        return topicService.getTopicsBySyllabusId(syllabusId).stream()
                .map(topicService::toDTO)
                .collect(Collectors.toList());
    }

    public List<MaterialDTO> getMaterialsBySyllabusId(Long syllabusId) {
        return materialService.getMaterialsBySyllabusId(syllabusId).stream()
                .map(materialService::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeadlineDTO> getDeadlinesBySyllabusId(Long syllabusId) {
        return deadlineService.getDeadlinesBySyllabusId(syllabusId).stream()
                .map(deadlineService::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteSyllabus(Long id) {
        if (!syllabusRepository.existsById(id)) {
            throw new RuntimeException("Syllabus not found with id: " + id);
        }
        syllabusRepository.deleteById(id);
    }

    public List<SyllabusDTO> getAllSyllabi() {
        return syllabusRepository.findAll().stream()
                .map(SyllabusMapper::toDTO)
                .collect(Collectors.toList());
    }
}
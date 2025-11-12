package com.syllabusai.service;

import com.syllabusai.dto.SyllabusDTO;
import com.syllabusai.exception.SyllabusProcessingException;
import com.syllabusai.model.*;
import com.syllabusai.observer.FileProcessingSubject;
import com.syllabusai.parser.SyllabusParser;
import com.syllabusai.parser.SyllabusParserFactory;
import com.syllabusai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final TopicRepository topicRepository;
    private final MaterialRepository materialRepository;
    private final DeadlineRepository deadlineRepository;
    private final UserRepository userRepository;
    private final SyllabusParserFactory parserFactory;
    private final FileProcessingSubject progressSubject;

    public SyllabusDTO uploadAndParse(MultipartFile file, String userEmail) {
        log.info("Processing syllabus upload for user: {}, file: {}", userEmail, file.getOriginalFilename());

        try {
            progressSubject.notifyProgress(10, "Starting file processing");

            // Validate input
            validateFile(file);

            // Find user
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new SyllabusProcessingException("User not found: " + userEmail));

            progressSubject.notifyProgress(30, "User validated, starting PDF parsing");

            // Parse syllabus using Factory Method pattern
            SyllabusParser parser = parserFactory.createParser(file);
            Syllabus parsedSyllabus = parser.parse(file);

            progressSubject.notifyProgress(60, "PDF parsed successfully, saving data");

            // Set user and save syllabus FIRST
            parsedSyllabus.setUser(user);
            Syllabus savedSyllabus = syllabusRepository.save(parsedSyllabus);
            log.info("Initial syllabus saved with ID: {}", savedSyllabus.getId());

            // FIXED: Save child entities with proper syllabus relationships
            saveChildEntitiesWithRelations(savedSyllabus);

            // Return simple DTO
            SyllabusDTO result = SyllabusDTO.builder()
                    .id(savedSyllabus.getId())
                    .fileName(savedSyllabus.getFilename())
                    .uploadDate(savedSyllabus.getUploadDate())
                    .status("PROCESSED")
                    .build();

            progressSubject.notifyProgress(100, "Syllabus processed successfully");
            progressSubject.notifyComplete("Syllabus ID: " + savedSyllabus.getId());

            log.info("Syllabus processed successfully: ID {}", savedSyllabus.getId());
            return result;

        } catch (Exception e) {
            log.error("Syllabus upload failed for user: {}, file: {}", userEmail, file.getOriginalFilename(), e);
            progressSubject.notifyError("Upload failed: " + e.getMessage());
            throw new SyllabusProcessingException("Failed to upload and parse syllabus: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Completely rewritten to ensure syllabus relationships are set
     */
    private void saveChildEntitiesWithRelations(Syllabus syllabus) {
        log.info("Saving child entities for syllabus ID: {}", syllabus.getId());

        // Save topics
        if (syllabus.getTopics() != null && !syllabus.getTopics().isEmpty()) {
            log.info("Processing {} topics", syllabus.getTopics().size());
            for (Topic topic : syllabus.getTopics()) {
                topic.setSyllabus(syllabus); // Set the foreign key
                log.debug("Set syllabus for topic: {}", topic.getTitle());
            }
            List<Topic> savedTopics = topicRepository.saveAll(syllabus.getTopics());
            log.info("Successfully saved {} topics", savedTopics.size());
        }

        // Save deadlines - FIXED: This was the main issue
        if (syllabus.getDeadlines() != null && !syllabus.getDeadlines().isEmpty()) {
            log.info("Processing {} deadlines", syllabus.getDeadlines().size());
            for (Deadline deadline : syllabus.getDeadlines()) {
                deadline.setSyllabus(syllabus); // Set the foreign key
                log.debug("Set syllabus for deadline: {}", deadline.getTitle());
            }
            List<Deadline> savedDeadlines = deadlineRepository.saveAll(syllabus.getDeadlines());
            log.info("Successfully saved {} deadlines", savedDeadlines.size());
        }

        // Save materials
        if (syllabus.getMaterials() != null && !syllabus.getMaterials().isEmpty()) {
            log.info("Processing {} materials", syllabus.getMaterials().size());
            for (Material material : syllabus.getMaterials()) {
                material.setSyllabus(syllabus); // Set the foreign key
                log.debug("Set syllabus for material: {}", material.getTitle());
            }
            List<Material> savedMaterials = materialRepository.saveAll(syllabus.getMaterials());
            log.info("Successfully saved {} materials", savedMaterials.size());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SyllabusProcessingException("File is empty");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new SyllabusProcessingException("File must be a PDF. Received: " + file.getContentType());
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new SyllabusProcessingException("File size exceeds 10MB limit");
        }
    }

    @Transactional(readOnly = true)
    public List<Syllabus> getUserSyllabi(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new SyllabusProcessingException("User not found"));
        return syllabusRepository.findByUserIdOrderByUploadDateDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Topic> getTopicsBySyllabusId(Long syllabusId) {
        return topicRepository.findBySyllabusIdOrderByWeekAsc(syllabusId);
    }

    @Transactional(readOnly = true)
    public List<Material> getMaterialsBySyllabusId(Long syllabusId) {
        return materialRepository.findBySyllabusId(syllabusId);
    }

    @Transactional(readOnly = true)
    public List<Deadline> getDeadlinesBySyllabusId(Long syllabusId) {
        return deadlineRepository.findBySyllabusIdOrderByDateAsc(syllabusId);
    }

    @Transactional(readOnly = true)
    public SyllabusDTO getSyllabus(Long id) {
        log.debug("Fetching syllabus with ID: {}", id);

        Syllabus syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new SyllabusProcessingException("Syllabus not found with id: " + id));

        return SyllabusDTO.builder()
                .id(syllabus.getId())
                .fileName(syllabus.getFilename())
                .uploadDate(syllabus.getUploadDate())
                .status("PROCESSED")
                .build();
    }

    @Transactional
    public void deleteSyllabus(Long id) {
        log.info("Deleting syllabus with ID: {}", id);

        if (!syllabusRepository.existsById(id)) {
            throw new SyllabusProcessingException("Syllabus not found with id: " + id);
        }

        syllabusRepository.deleteById(id);
        log.info("Syllabus deleted successfully: {}", id);
    }
}
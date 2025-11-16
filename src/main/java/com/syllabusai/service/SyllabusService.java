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

            validateFile(file);

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new SyllabusProcessingException("User not found: " + userEmail));

            progressSubject.notifyProgress(30, "User validated, starting PDF parsing");

            SyllabusParser parser = parserFactory.createParser(file);
            Syllabus parsedSyllabus = parser.parse(file);

            progressSubject.notifyProgress(60, "PDF parsed successfully, saving data");

            parsedSyllabus.setUser(user);

            establishRelationships(parsedSyllabus);

            Syllabus savedSyllabus = syllabusRepository.save(parsedSyllabus);
            log.info("Syllabus saved successfully with ID: {}", savedSyllabus.getId());

            log.info("Saved {} topics, {} deadlines, {} materials",
                    savedSyllabus.getTopics().size(),
                    savedSyllabus.getDeadlines().size(),
                    savedSyllabus.getMaterials().size());

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

    private void establishRelationships(Syllabus syllabus) {
        log.info("Establishing bidirectional relationships for syllabus");

        if (syllabus.getTopics() != null) {
            log.info("Processing {} topics", syllabus.getTopics().size());
            for (Topic topic : syllabus.getTopics()) {
                topic.setSyllabus(syllabus);
                log.debug("Linked topic '{}' to syllabus", topic.getTitle());
            }
        }

        if (syllabus.getDeadlines() != null) {
            log.info("Processing {} deadlines", syllabus.getDeadlines().size());
            for (Deadline deadline : syllabus.getDeadlines()) {
                deadline.setSyllabus(syllabus);
                log.debug("Linked deadline '{}' to syllabus", deadline.getTitle());
            }
        }

        if (syllabus.getMaterials() != null) {
            log.info("Processing {} materials", syllabus.getMaterials().size());
            for (Material material : syllabus.getMaterials()) {
                material.setSyllabus(syllabus);
                log.debug("Linked material '{}' to syllabus", material.getTitle());
            }
        }

        log.info("Bidirectional relationships established successfully");
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
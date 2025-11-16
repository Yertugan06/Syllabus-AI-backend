package com.syllabusai.controller;

import com.syllabusai.dto.SyllabusDTO;
import com.syllabusai.dto.SyllabusOverviewDTO;
import com.syllabusai.facade.SyllabusProcessingFacade;
import com.syllabusai.model.*;
import com.syllabusai.repository.UserRepository;
import com.syllabusai.service.SyllabusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/syllabus")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusProcessingFacade syllabusFacade;
    private final SyllabusService syllabusService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadSyllabus(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userEmail") String userEmail) {

        try {
            log.info("=== UPLOAD REQUEST ===");
            log.info("File: {}, User: {}", file.getOriginalFilename(), userEmail);

            SyllabusDTO syllabus = syllabusFacade.processSyllabusUpload(file, userEmail);

            log.info("Upload successful, syllabus ID: {}", syllabus.getId());
            return ResponseEntity.ok(syllabus);

        } catch (Exception e) {
            log.error("Upload error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserSyllabi(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            log.info("=== GET USER SYLLABI REQUEST ===");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("No authorization header or invalid format");
                return ResponseEntity.status(401)
                        .body(Map.of("error", "No authorization token"));
            }

            String token = authHeader.substring(7);
            log.info("Token received: {}", token.substring(0, Math.min(20, token.length())) + "...");

            String userEmail = extractEmailFromToken(token);

            if (userEmail == null) {
                log.error("Could not extract email from token");
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid token"));
            }

            log.info("User email from token: {}", userEmail);

            List<Syllabus> syllabi = syllabusService.getUserSyllabi(userEmail);
            log.info("Found {} syllabi for user {}", syllabi.size(), userEmail);

            List<Map<String, Object>> response = syllabi.stream()
                    .map(s -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", s.getId());
                        map.put("fileName", s.getFilename());
                        map.put("uploadDate", s.getUploadDate());
                        map.put("status", s.getStatus());
                        log.debug("Mapped syllabus: ID={}, fileName={}", s.getId(), s.getFilename());
                        return map;
                    })
                    .collect(Collectors.toList());

            log.info("Returning {} syllabi", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting user syllabi: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get syllabi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSyllabus(@PathVariable Long id) {
        try {
            log.info("Getting syllabus with ID: {}", id);
            SyllabusDTO syllabus = syllabusService.getSyllabus(id);
            return ResponseEntity.ok(syllabus);
        } catch (Exception e) {
            log.error("Error getting syllabus {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/topics")
    public ResponseEntity<?> getSyllabusTopics(@PathVariable Long id) {
        try {
            log.info("Getting topics for syllabus ID: {}", id);
            List<Topic> topics = syllabusService.getTopicsBySyllabusId(id);
            log.info("Found {} topics", topics.size());

            List<Map<String, Object>> response = topics.stream()
                    .map(t -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", t.getId());
                        map.put("title", t.getTitle());
                        map.put("description", t.getDescription());
                        map.put("week", t.getWeek());
                        map.put("difficultyLevel", t.getDifficultyLevel().toString());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting topics for syllabus {}: {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get topics: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/materials")
    public ResponseEntity<?> getSyllabusMaterials(@PathVariable Long id) {
        try {
            log.info("Getting materials for syllabus ID: {}", id);
            List<Material> materials = syllabusService.getMaterialsBySyllabusId(id);
            log.info("Found {} materials", materials.size());

            List<Map<String, Object>> response = materials.stream()
                    .map(m -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", m.getId());
                        map.put("title", m.getTitle());
                        map.put("type", m.getType().toString());
                        map.put("url", m.getLink() != null ? m.getLink() : "");
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting materials for syllabus {}: {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get materials: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/deadlines")
    public ResponseEntity<?> getSyllabusDeadlines(@PathVariable Long id) {
        try {
            log.info("Getting deadlines for syllabus ID: {}", id);
            List<Deadline> deadlines = syllabusService.getDeadlinesBySyllabusId(id);
            log.info("Found {} deadlines", deadlines.size());

            List<Map<String, Object>> response = deadlines.stream()
                    .map(d -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", d.getId());
                        map.put("title", d.getTitle());
                        map.put("dueDate", d.getDate());
                        map.put("type", d.getType().toString());
                        map.put("description", d.getDescription() != null ? d.getDescription() : "");
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting deadlines for syllabus {}: {}", id, e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get deadlines: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<?> getSyllabusOverview(@PathVariable Long id) {
        try {
            log.info("Getting overview for syllabus ID: {}", id);
            SyllabusOverviewDTO overview = syllabusFacade.getSyllabusOverview(id);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("Error getting overview for syllabus {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSyllabus(@PathVariable Long id) {
        try {
            log.info("Deleting syllabus ID: {}", id);
            syllabusService.deleteSyllabus(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting syllabus {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private String extractEmailFromToken(String token) {
        try {
            if (token == null || !token.startsWith("demo-token-")) {
                log.error("Invalid token format: {}", token);
                return null;
            }

            String[] parts = token.split("-");
            if (parts.length < 3) {
                log.error("Token has insufficient parts: {}", parts.length);
                return null;
            }

            Long userId = Long.parseLong(parts[2]);
            log.info("Extracted userId from token: {}", userId);

            return userRepository.findById(userId)
                    .map(user -> {
                        log.info("Found user: {}", user.getEmail());
                        return user.getEmail();
                    })
                    .orElseGet(() -> {
                        log.error("User not found with ID: {}", userId);
                        return null;
                    });

        } catch (NumberFormatException e) {
            log.error("Invalid userId in token: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error parsing token: {}", e.getMessage(), e);
            return null;
        }
    }
}
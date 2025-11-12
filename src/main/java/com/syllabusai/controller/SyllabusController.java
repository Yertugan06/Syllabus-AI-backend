package com.syllabusai.controller;


import com.syllabusai.dto.SyllabusDTO;
import com.syllabusai.dto.SyllabusOverviewDTO;
import com.syllabusai.facade.SyllabusProcessingFacade;
import com.syllabusai.model.*;
import com.syllabusai.service.SyllabusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/syllabus")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusProcessingFacade syllabusFacade;
    private final SyllabusService syllabusService;

    // POST /api/syllabus/upload - Upload syllabus PDF (uses Facade)
    @PostMapping("/upload")
    public ResponseEntity<SyllabusDTO> uploadSyllabus(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userEmail") String userEmail) {

        try {
            System.out.println("=== UPLOAD REQUEST ===");
            SyllabusDTO syllabus = syllabusFacade.processSyllabusUpload(file, userEmail);
            return ResponseEntity.ok(syllabus);

        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /api/syllabus/{id} - Retrieve parsed syllabus data
    @GetMapping("/{id}")
    public ResponseEntity<SyllabusDTO> getSyllabus(@PathVariable Long id) {
        try {
            SyllabusDTO syllabus = syllabusService.getSyllabus(id);
            return ResponseEntity.ok(syllabus);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/syllabus/{id}/topics - Get list of topics
    @GetMapping("/{id}/topics")
    public ResponseEntity<List<Topic>> getSyllabusTopics(@PathVariable Long id) {
        try {
            List<Topic> topics = syllabusService.getTopicsBySyllabusId(id);
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/syllabus/{id}/materials - Get list of materials
    @GetMapping("/{id}/materials")
    public ResponseEntity<List<Material>> getSyllabusMaterials(@PathVariable Long id) {
        try {
            List<Material> materials = syllabusService.getMaterialsBySyllabusId(id);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/syllabus/{id}/deadlines - Get deadlines and exams
    @GetMapping("/{id}/deadlines")
    public ResponseEntity<List<Deadline>> getSyllabusDeadlines(@PathVariable Long id) {
        try {
            List<Deadline> deadlines = syllabusService.getDeadlinesBySyllabusId(id);
            return ResponseEntity.ok(deadlines);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/syllabus/{id}/overview - Get complete overview (uses Facade)
    @GetMapping("/{id}/overview")
    public ResponseEntity<SyllabusOverviewDTO> getSyllabusOverview(@PathVariable Long id) {
        try {
            SyllabusOverviewDTO overview = syllabusFacade.getSyllabusOverview(id);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/syllabus/{id} - Delete syllabus
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSyllabus(@PathVariable Long id) {
        try {
            syllabusService.deleteSyllabus(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
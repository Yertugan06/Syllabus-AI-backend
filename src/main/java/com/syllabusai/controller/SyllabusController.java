package com.syllabusai.controller;

import com.syllabusai.dto.SyllabusDTO;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/syllabus")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService syllabusService;

    @PostMapping("/upload")
    public ResponseEntity<SyllabusDTO> uploadSyllabus(@RequestParam("file") MultipartFile file) {
        SyllabusDTO syllabus = syllabusService.uploadAndParse(file);
        return ResponseEntity.ok(syllabus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SyllabusDTO> getSyllabus(@PathVariable Long id) {
        return ResponseEntity.ok(syllabusService.getSyllabus(id));
    }

    @GetMapping("/{id}/topics")
    public ResponseEntity<?> getTopics(@PathVariable Long id) {
        return ResponseEntity.ok(syllabusService.getTopicsBySyllabusId(id));
    }

    @GetMapping("/{id}/materials")
    public ResponseEntity<?> getMaterials(@PathVariable Long id) {
        return ResponseEntity.ok(syllabusService.getMaterialsBySyllabusId(id));
    }

    @GetMapping("/{id}/deadlines")
    public ResponseEntity<?> getDeadlines(@PathVariable Long id) {
        return ResponseEntity.ok(syllabusService.getDeadlinesBySyllabusId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSyllabus(@PathVariable Long id) {
        syllabusService.deleteSyllabus(id);
        return ResponseEntity.noContent().build();
    }
}☺
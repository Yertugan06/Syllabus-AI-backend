package com.syllabusai.parser;

import com.syllabusai.adapter.AIService;
import com.syllabusai.model.*;
import com.syllabusai.strategy.ExtractionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenericSyllabusParser implements SyllabusParser {

    private final AIService aiService;
    private final ExtractionContext extractionContext;

    @Override
    public boolean supports(MultipartFile file) {
        return file.getContentType() != null &&
                file.getContentType().equals("application/pdf");
    }

    @Override
    public Syllabus parse(MultipartFile file) throws Exception {
        log.info("Parsing syllabus file: {}", file.getOriginalFilename());

        String textContent = extractTextFromPDF(file);
        log.debug("Extracted {} characters from PDF", textContent.length());

        Syllabus syllabus = Syllabus.builder()
                .filename(file.getOriginalFilename())
                .uploadDate(LocalDateTime.now())
                .status("PARSED")
                .topics(new ArrayList<>())
                .materials(new ArrayList<>())
                .deadlines(new ArrayList<>())
                .build();

        try {
            extractWithStrategies(syllabus, textContent);

        } catch (Exception e) {
            log.error("Parsing failed: {}", e.getMessage(), e);
            syllabus.setStatus("ERROR");
            throw e;
        }

        log.info("Parsing completed: {} topics, {} materials, {} deadlines",
                syllabus.getTopics().size(),
                syllabus.getMaterials().size(),
                syllabus.getDeadlines().size());

        return syllabus;
    }

    private void extractWithStrategies(Syllabus syllabus, String textContent) throws InterruptedException {
        log.debug("Starting sequential strategy-based extraction");

        log.info("=== Extracting TOPICS ===");
        List<Topic> topics = extractionContext.extractTopics(textContent);
        syllabus.getTopics().addAll(topics);
        log.info("Extracted {} topics", topics.size());

        Thread.sleep(1000);

        log.info("=== Extracting DEADLINES ===");
        List<Deadline> deadlines = extractionContext.extractDeadlines(textContent);
        syllabus.getDeadlines().addAll(deadlines);
        log.info("Extracted {} deadlines", deadlines.size());

        Thread.sleep(1000);

        log.info("=== Extracting MATERIALS ===");
        List<Material> materials = extractionContext.extractMaterials(textContent);
        syllabus.getMaterials().addAll(materials);
        log.info("Extracted {} materials", materials.size());

        log.info("Sequential extraction completed: {} topics, {} deadlines, {} materials",
                topics.size(), deadlines.size(), materials.size());

        if (!topics.isEmpty() || !deadlines.isEmpty() || !materials.isEmpty()) {
            log.info("Extracted entities will have relationships established by SyllabusService");
        }
    }

    private String extractTextFromPDF(MultipartFile file) throws Exception {
        log.debug("Extracting text from PDF: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(inputStream))) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setWordSeparator(" ");

            String text = stripper.getText(document);
            log.debug("PDF text extraction completed, {} characters", text.length());

            return text.trim();

        } catch (Exception e) {
            log.error("PDF text extraction failed for file: {}", file.getOriginalFilename(), e);
            throw new Exception("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }
}
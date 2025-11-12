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
            // Use strategy context for extraction
            extractWithStrategies(syllabus, textContent);

        } catch (Exception e) {
            log.error("Parsing failed: {}", e.getMessage());
            syllabus.setStatus("ERROR");
            throw e;
        }

        log.info("Parsing completed: {} topics, {} materials, {} deadlines",
                syllabus.getTopics().size(),
                syllabus.getMaterials().size(),
                syllabus.getDeadlines().size());

        return syllabus;
    }

    /**
     * Extract content using strategy pattern
     */
    private void extractWithStrategies(Syllabus syllabus, String textContent) {
        log.debug("Starting strategy-based extraction");

        // Extract using context that selects best strategy
        List<Topic> topics = extractionContext.extractTopics(textContent);
        List<Deadline> deadlines = extractionContext.extractDeadlines(textContent);
        List<Material> materials = extractionContext.extractMaterials(textContent);

        syllabus.getTopics().addAll(topics);
        syllabus.getDeadlines().addAll(deadlines);
        syllabus.getMaterials().addAll(materials);

        log.debug("Strategy extraction completed: {} topics, {} deadlines, {} materials",
                topics.size(), deadlines.size(), materials.size());
    }

    /**
     * Extract text from PDF file using PDFBox
     */
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
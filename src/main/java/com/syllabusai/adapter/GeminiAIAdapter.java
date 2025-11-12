package com.syllabusai.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAIAdapter implements AIService {

    private final WebClient webClient;

    @Value("${gemini.api-key:demo-key-placeholder}")
    private String apiKey;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final int MAX_CONTENT_LENGTH = 25000;
    private static final Duration API_TIMEOUT = Duration.ofSeconds(45);

    // Default semester start date (September 1st of current year)
    private static final LocalDate DEFAULT_SEMESTER_START = LocalDate.now().withMonth(9).withDayOfMonth(1);

    @Override
    public String extractTopics(String content) {
        if (isDemoMode()) {
            log.warn("API key not configured, returning empty");
            return "[]";
        }

        String prompt = createSmartTopicExtractionPrompt(content);
        log.debug("Extracting topics with AI, content length: {}", content.length());
        return callGeminiAPI(prompt);
    }

    @Override
    public String extractDeadlines(String content) {
        if (isDemoMode()) {
            log.warn("API key not configured, returning empty");
            return "[]";
        }

        String prompt = createSmartDeadlineExtractionPrompt(content);
        log.debug("Extracting deadlines with AI, content length: {}", content.length());
        return callGeminiAPI(prompt);
    }

    @Override
    public String extractMaterials(String content) {
        if (isDemoMode()) {
            log.warn("API key not configured, returning empty");
            return "[]";
        }

        String prompt = createSmartMaterialExtractionPrompt(content);
        log.debug("Extracting materials with AI, content length: {}", content.length());
        return callGeminiAPI(prompt);
    }

    @Override
    public String analyzeSyllabusStructure(String content) {
        if (isDemoMode()) return "{}";
        String prompt = createStructureAnalysisPrompt(content);
        return callGeminiAPI(prompt);
    }

    @Override
    public String generateText(String prompt) {
        if (isDemoMode()) return "MEDIUM";
        return callGeminiAPI(prompt);
    }

    @Override
    public String analyzeDocument(byte[] documentBytes, String mimeType, String prompt) {
        return "{}";
    }

    private boolean isDemoMode() {
        return "demo-key-placeholder".equals(apiKey) || apiKey == null || apiKey.trim().isEmpty();
    }

    /**
     * Smart topic extraction that reads from course plan table
     */
    private String createSmartTopicExtractionPrompt(String content) {
        String semesterStart = DEFAULT_SEMESTER_START.format(DateTimeFormatter.ISO_DATE);

        return """
            You are analyzing a university syllabus document. Find the "Course Plan" or "Course Topics" table.
            
            Extract EACH WEEK'S topic as a SEPARATE entry. For each topic, rate its difficulty comparatively:
            - EASY: Introductory concepts, simple patterns (like Builder, Factory)
            - MEDIUM: More complex patterns requiring understanding of abstractions (like Adapter, Decorator, Strategy)
            - HARD: Advanced patterns with complex relationships (like Bridge, Visitor, Abstract Factory)
            
            Return ONLY this JSON array:
            [
              {
                "week": 1,
                "title": "Builder",
                "description": "Brief summary of what's covered in this week",
                "difficulty": "EASY"
              },
              {
                "week": 2,
                "title": "Factory Method",
                "description": "Brief summary",
                "difficulty": "MEDIUM"
              }
            ]
            
            IMPORTANT:
            - Extract ALL weeks (typically 1-10 or 1-15)
            - Each week = separate JSON object
            - Title should be concise (under 100 chars)
            - Rate difficulty based on pattern complexity
            - If no topics found, return []
            
            Syllabus content:
            """ + truncateContent(content);
    }

    /**
     * Smart deadline extraction with date calculation
     */
    private String createSmartDeadlineExtractionPrompt(String content) {
        String semesterStart = DEFAULT_SEMESTER_START.format(DateTimeFormatter.ISO_DATE);

        return """
            You are analyzing a university syllabus. Extract ALL deadlines, assignments, and exams.
            
            CALCULATE DATES:
            - Assume semester starts on: %s
            - Week 1 starts on semester start date
            - Assignment due dates: End of the week they're assigned (e.g., Week 2 assignment due on Week 2 Sunday)
            - Midterm: Usually around Week 4-5
            - Final/Endterm: Usually Week 10
            
            Return ONLY this JSON array:
            [
              {
                "week": 2,
                "title": "Assignment 1: Builder Pattern",
                "date": "2025-09-14",
                "type": "ASSIGNMENT",
                "description": "Implement Car.Builder in Java"
              },
              {
                "week": 5,
                "title": "Midterm Examination",
                "date": "2025-10-05",
                "type": "EXAM",
                "description": "Covers patterns from weeks 1-5"
              },
              {
                "week": 10,
                "title": "Final Examination",
                "date": "2025-11-09",
                "type": "EXAM",
                "description": "Comprehensive final exam"
              }
            ]
            
            IMPORTANT:
            - Calculate actual dates based on semester start
            - Week N ends 7*(N) days after start
            - Each deadline = separate JSON object
            - Type must be: ASSIGNMENT, EXAM, QUIZ, or PROJECT
            - If document mentions "Midterm week" extract it
            - If document mentions "Endterm week" extract it
            
            Syllabus content:
            """.formatted(semesterStart) + truncateContent(content);
    }

    /**
     * Smart material extraction - per topic
     */
    private String createSmartMaterialExtractionPrompt(String content) {
        return """
            You are analyzing a university syllabus. Extract ALL learning materials/resources mentioned.
            
            For each TOPIC/WEEK, find its associated materials. If materials are listed in a table or
            "Detailed Course Plan" section, extract them per week.
            
            Return ONLY this JSON array:
            [
              {
                "week": 1,
                "title": "Head First Design Patterns - Builder Chapter",
                "type": "TEXTBOOK",
                "link": "",
                "topicReference": "Builder Pattern"
              },
              {
                "week": 1,
                "title": "Refactoring.Guru - Builder Tutorial",
                "type": "WEBSITE",
                "link": "https://refactoring.guru/design-patterns/builder",
                "topicReference": "Builder Pattern"
              },
              {
                "week": 2,
                "title": "Clean Code - Chapter 6",
                "type": "READING",
                "link": "",
                "topicReference": "Factory Pattern"
              }
            ]
            
            IMPORTANT:
            - Extract materials mentioned in "Resources:", "Reading:", or "Supporting reading:" sections
            - Each material = separate JSON object
            - Include week number to link material to topic
            - Type must be: TEXTBOOK, READING, VIDEO, WEBSITE, or EXERCISE
            - If NO materials section exists, return []
            - Do NOT make up materials - only extract what's actually mentioned
            
            Syllabus content:
            """ + truncateContent(content);
    }

    private String createStructureAnalysisPrompt(String content) {
        return """
            Extract basic course information:
            {
              "courseTitle": "Course name",
              "courseCode": "Code if mentioned",
              "instructor": "Instructor name",
              "semester": "Fall 2025 or similar",
              "totalWeeks": 10
            }
            
            Syllabus:
            """ + truncateContent(content);
    }

    /**
     * Call Gemini API with robust error handling
     */
    private String callGeminiAPI(String prompt) {
        try {
            log.debug("Calling Gemini API with prompt length: {}", prompt.length());

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            requestBody.put("generationConfig", Map.of(
                    "temperature", 0.1,
                    "topK", 40,
                    "topP", 0.8,
                    "maxOutputTokens", 8192, // Increased even more
                    "responseMimeType", "application/json"
            ));

            String url = BASE_URL + "?key=" + apiKey;

            JsonNode rootNode = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.USER_AGENT, "SyllabusAI/1.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(API_TIMEOUT)
                    .block();

            if (rootNode == null) {
                log.error("Gemini returned null");
                return "[]";
            }

            JsonNode candidatesNode = rootNode.path("candidates");
            if (candidatesNode.isMissingNode() || candidatesNode.isEmpty()) {
                log.error("No candidates in response");
                return "[]";
            }

            JsonNode firstCandidate = candidatesNode.get(0);
            if (firstCandidate == null) {
                log.error("First candidate is null");
                return "[]";
            }

            String finishReason = firstCandidate.path("finishReason").asText("");
            if ("MAX_TOKENS".equals(finishReason)) {
                log.error("Response truncated due to MAX_TOKENS!");
                // Try to parse partial response
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");
                if (!partsNode.isEmpty()) {
                    String partial = partsNode.get(0).path("text").asText("");
                    log.warn("Got partial response, length: {}", partial.length());
                    return partial.isEmpty() ? "[]" : partial;
                }
                return "[]";
            }
            if ("SAFETY".equals(finishReason)) {
                log.error("Response blocked by safety filters");
                return "[]";
            }

            JsonNode contentNode = firstCandidate.path("content");
            JsonNode partsNode = contentNode.path("parts");
            if (partsNode.isMissingNode() || partsNode.isEmpty()) {
                log.error("No parts in content");
                return "[]";
            }

            JsonNode firstPart = partsNode.get(0);
            if (firstPart == null) {
                log.error("First part is null");
                return "[]";
            }

            String result = firstPart.path("text").asText("");

            if (result.isEmpty()) {
                log.warn("Empty text in response");
                return "[]";
            }

            log.info("Gemini API success, response length: {}", result.length());
            return result;

        } catch (WebClientResponseException e) {
            log.error("Gemini HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "[]";
        } catch (Exception e) {
            log.error("Gemini API failed: {}", e.getMessage(), e);
            return "[]";
        }
    }

    private String truncateContent(String content) {
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }

        log.warn("Content truncated from {} to {} chars", content.length(), MAX_CONTENT_LENGTH);
        int halfLimit = MAX_CONTENT_LENGTH / 2;
        String beginning = content.substring(0, halfLimit);
        String end = content.substring(content.length() - halfLimit);

        return beginning + "\n\n...[TRUNCATED]...\n\n" + end;
    }
}
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
    private static final int MAX_CONTENT_LENGTH = 30000;
    private static final Duration API_TIMEOUT = Duration.ofSeconds(30);

    @Override
    public String extractTopics(String content) {
        if (isDemoMode()) {
            log.info("Using demo mode for topic extraction");
            return getDemoTopics();
        }

        String prompt = createTopicExtractionPrompt(content);
        log.debug("Extracting topics with AI, content length: {}", content.length());
        return callGeminiAPI(prompt);
    }

    @Override
    public String extractDeadlines(String content) {
        if (isDemoMode()) {
            log.info("Using demo mode for deadline extraction");
            return getDemoDeadlines();
        }

        String prompt = createDeadlineExtractionPrompt(content);
        log.debug("Extracting deadlines with AI, content length: {}", content.length());
        return callGeminiAPI(prompt);
    }

    @Override
    public String extractMaterials(String content) {
        if (isDemoMode()) {
            log.info("Using demo mode for material extraction");
            return getDemoMaterials();
        }

        String prompt = createMaterialExtractionPrompt(content);
        log.debug("Extracting materials with AI, content length: {}", content.length());
        return callGeminiAPI(prompt);
    }

    @Override
    public String analyzeSyllabusStructure(String content) {
        if (isDemoMode()) {
            log.info("Using demo mode for syllabus structure analysis");
            return getDemoSyllabusStructure();
        }

        String prompt = createStructureAnalysisPrompt(content);
        return callGeminiAPI(prompt);
    }

    @Override
    public String generateText(String prompt) {
        if (isDemoMode()) {
            log.info("Using demo mode for text generation");
            return "MEDIUM"; // Default response for difficulty analysis
        }
        return callGeminiAPI(prompt);
    }

    @Override
    public String analyzeDocument(byte[] documentBytes, String mimeType, String prompt) {
        log.warn("Direct document analysis not implemented, using demo mode");
        if (isDemoMode()) {
            return "Demo analysis result for document processing";
        }
        return callGeminiAPI(prompt + "\n[Document content would be processed here]");
    }

    private boolean isDemoMode() {
        return "demo-key-placeholder".equals(apiKey) || apiKey == null || apiKey.trim().isEmpty();
    }

    private String createTopicExtractionPrompt(String content) {
        return """
            Analyze the following university syllabus content and extract ALL topics, lectures, modules, and weekly content.
            Return ONLY a valid JSON array with this exact structure. Do not include any other text:
            [
              {
                "title": "Specific topic title (e.g., 'Introduction to Java Programming')",
                "week": week_number (integer 1-16),
                "description": "Brief description of what this topic covers",
                "difficultyLevel": "EASY|MEDIUM|HARD"
              }
            ]
            
            Rules:
            - If no topics found, return empty array []
            - Estimate week numbers based on progression
            - Determine difficulty based on topic complexity
            - Extract ALL topics mentioned, even if not explicitly numbered
            
            Syllabus Content:
            """ + truncateContent(content);
    }

    private String createDeadlineExtractionPrompt(String content) {
        return """
            Analyze the following syllabus content and extract ALL deadlines, assignments, exams, quizzes, projects, and important dates.
            Return ONLY a valid JSON array with this exact structure. Do not include any other text:
            [
              {
                "title": "Specific assignment/exam name (e.g., 'Midterm Exam', 'Project Proposal')",
                "dueDate": "YYYY-MM-DD" (estimate if only relative dates given),
                "type": "ASSIGNMENT|EXAM|QUIZ|PROJECT|PRESENTATION|PAPER",
                "description": "Brief description or requirements"
              }
            ]
            
            Rules:
            - If no deadlines found, return empty array []
            - Convert relative dates (e.g., 'Week 5', 'March 15th') to YYYY-MM-DD format
            - Estimate dates based on academic calendar if needed
            - Include all assessment items mentioned
            
            Syllabus Content:
            """ + truncateContent(content);
    }

    private String createMaterialExtractionPrompt(String content) {
        return """
            Analyze the following syllabus content and extract ALL learning materials, textbooks, readings, resources, and references.
            Return ONLY a valid JSON array with this exact structure. Do not include any other text:
            [
              {
                "title": "Material title (e.g., 'Introduction to Algorithms textbook')",
                "type": "TEXTBOOK|READING|VIDEO|WEBSITE|SLIDES|EXERCISE",
                "url": "URL or reference if provided, otherwise empty string"
              }
            ]
            
            Rules:
            - If no materials found, return empty array []
            - Include required and recommended materials
            - Categorize materials by type
            - Extract ISBNs or author names if no clear title
            
            Syllabus Content:
            """ + truncateContent(content);
    }

    private String createStructureAnalysisPrompt(String content) {
        return """
            Analyze this syllabus document structure and provide metadata.
            Return a JSON object with:
            {
              "courseTitle": "Extracted course title",
              "courseCode": "Extracted course code if available",
              "instructor": "Instructor name if available",
              "credits": "Number of credits",
              "semester": "Semester/term information",
              "learningObjectives": ["array", "of", "objectives"],
              "gradingBreakdown": {"Component": "Percentage"}
            }
            
            Syllabus Content:
            """ + truncateContent(content);
    }

    /**
     * Make actual API call to Gemini AI using WebClient
     */
    private String callGeminiAPI(String prompt) {
        try {
            log.debug("Calling Gemini API via WebClient with prompt length: {}", prompt.length());

            // Prepare request payload
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            // Configure safety settings
            requestBody.put("safetySettings", new Object[]{
                    Map.of(
                            "category", "HARM_CATEGORY_HARASSMENT",
                            "threshold", "BLOCK_MEDIUM_AND_ABOVE"
                    )
            });

            requestBody.put("generationConfig", Map.of(
                    "temperature", 0.2,
                    "topK", 40,
                    "topP", 0.8,
                    "maxOutputTokens", 2048
            ));

            String url = BASE_URL + "?key=" + apiKey;

            log.debug("Sending request to Gemini API");

            // Use WebClient for the POST request
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
                throw new RuntimeException("Gemini API returned null body");
            }

            // Parse response
            String result = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            if (result.isEmpty()) {
                if ("SAFETY".equals(rootNode.path("candidates").get(0).path("finishReason").asText())) {
                    log.error("Gemini API call blocked for safety reasons.");
                    throw new RuntimeException("API call blocked due to safety settings.");
                }
                log.warn("Gemini API returned an empty text result.");
            }

            log.debug("Gemini API response received, length: {}", result.length());
            return result;

        } catch (WebClientResponseException e) {
            log.error("Gemini API call failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
        }

        // Return demo data if API call fails
        log.warn("Gemini API call failed, returning demo data.");
        return getDemoFallback(prompt);
    }

    private String getDemoFallback(String prompt) {
        if (prompt.contains("topics")) return getDemoTopics();
        if (prompt.contains("deadlines")) return getDemoDeadlines();
        if (prompt.contains("materials")) return getDemoMaterials();
        if (prompt.contains("structure")) return getDemoSyllabusStructure();
        return "{}";
    }

    private String getDemoTopics() {
        return """
            [
              {
                "id": 1,
                "title": "Introduction to Web Development",
                "week": 1,
                "description": "Overview of web technologies, HTML basics, and development tools",
                "difficultyLevel": "EASY"
              },
              {
                "id": 2,
                "title": "CSS and Styling",
                "week": 2,
                "description": "CSS fundamentals, selectors, box model, and responsive design",
                "difficultyLevel": "EASY"
              },
              {
                "id": 3,
                "title": "JavaScript Fundamentals",
                "week": 3,
                "description": "Variables, functions, DOM manipulation, and event handling",
                "difficultyLevel": "MEDIUM"
              },
              {
                "id": 4,
                "title": "Advanced JavaScript and APIs",
                "week": 4,
                "description": "Async programming, REST APIs, and modern JavaScript features",
                "difficultyLevel": "HARD"
              }
            ]
            """;
    }

    private String getDemoDeadlines() {
        return """
            [
              {
                "id": 1,
                "title": "HTML/CSS Project",
                "dueDate": "2025-11-20",
                "type": "PROJECT",
                "description": "Build a responsive portfolio website using HTML and CSS"
              },
              {
                "id": 2,
                "title": "Midterm Exam",
                "dueDate": "2025-12-01",
                "type": "EXAM",
                "description": "Covers HTML, CSS, and basic JavaScript concepts"
              },
              {
                "id": 3,
                "title": "JavaScript Application",
                "dueDate": "2025-12-15",
                "type": "ASSIGNMENT",
                "description": "Create an interactive web application using JavaScript"
              }
            ]
            """;
    }

    private String getDemoMaterials() {
        return """
            [
              {
                "id": 1,
                "title": "MDN Web Docs - HTML Guide",
                "type": "WEBSITE",
                "url": "https://developer.mozilla.org/en-US/docs/Web/HTML"
              },
              {
                "id": 2,
                "title": "CSS: The Definitive Guide, 4th Edition",
                "type": "TEXTBOOK",
                "url": ""
              },
              {
                "id": 3,
                "title": "JavaScript: The Good Parts",
                "type": "READING",
                "url": ""
              }
            ]
            """;
    }

    private String getDemoSyllabusStructure() {
        return """
            {
              "courseTitle": "Introduction to Web Development",
              "courseCode": "CS101",
              "instructor": "Dr. Jane Smith",
              "credits": 3,
              "semester": "Fall 2025",
              "learningObjectives": [
                "Understand web technologies",
                "Build responsive websites",
                "Implement interactive features",
                "Work with web APIs"
              ],
              "gradingBreakdown": {
                "Projects": 40,
                "Exams": 30,
                "Assignments": 20,
                "Participation": 10
              }
            }
            """;
    }

    private String truncateContent(String content) {
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }

        log.warn("Content too long ({} chars), truncating to {}", content.length(), MAX_CONTENT_LENGTH);
        int halfLimit = MAX_CONTENT_LENGTH / 2;
        String beginning = content.substring(0, halfLimit);
        String end = content.substring(content.length() - halfLimit);

        return beginning + "\n\n...[CONTENT TRUNCATED FOR LENGTH]...\n\n" + end;
    }
}
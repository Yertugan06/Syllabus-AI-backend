package com.syllabusai.strategy;

import com.syllabusai.adapter.AIService;
import com.syllabusai.adapter.GeminiAIAdapter;
import com.syllabusai.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIExtractionStrategy implements ExtractionStrategy {

    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:demo-key-placeholder}")
    private String apiKey;

    @Override
    public List<Topic> extractTopics(String content) {
        if (isDemoMode()) {
            log.warn("AI API key not configured, skipping AI extraction for topics");
            return new ArrayList<>();
        }

        try {
            log.debug("Using AI strategy to extract topics");
            String aiResponse = aiService.extractTopics(content);
            return parseAIResponse(aiResponse, "topics");
        } catch (Exception e) {
            log.warn("AI topic extraction failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Deadline> extractDeadlines(String content) {
        if (isDemoMode()) {
            log.warn("AI API key not configured, skipping AI extraction for deadlines");
            return new ArrayList<>();
        }

        try {
            log.debug("Using AI strategy to extract deadlines");
            String aiResponse = aiService.extractDeadlines(content);
            return parseAIResponse(aiResponse, "deadlines");
        } catch (Exception e) {
            log.warn("AI deadline extraction failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Material> extractMaterials(String content) {
        if (isDemoMode()) {
            log.warn("AI API key not configured, skipping AI extraction for materials");
            return new ArrayList<>();
        }

        try {
            log.debug("Using AI strategy to extract materials");
            String aiResponse = aiService.extractMaterials(content);
            return parseAIResponse(aiResponse, "materials");
        } catch (Exception e) {
            log.warn("AI material extraction failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean supports(String content) {
        boolean hasKey = !isDemoMode();
        boolean hasContent = content != null && content.length() > 200;

        if (!hasKey) {
            log.warn("AI Strategy disabled - API key not configured. Current key: {}",
                    apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null");
        }

        return hasKey && hasContent;
    }

    @Override
    public int getPriority() {
        return isDemoMode() ? 999 : 1;
    }

    @Override
    public int getConfidence(String content) {
        if (isDemoMode()) {
            return 0;
        }
        return 85;
    }

    @Override
    public String getName() {
        return "AI_EXTRACTION_STRATEGY";
    }

    private boolean isDemoMode() {
        return "demo-key-placeholder".equals(apiKey) ||
                apiKey == null ||
                apiKey.trim().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> parseAIResponse(String jsonResponse, String type) {
        List<T> results = new ArrayList<>();

        try {
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            log.debug("Parsing {} response, cleaned JSON length: {}", type, cleanJson.length());

            JsonNode rootNode = objectMapper.readTree(cleanJson);

            if (rootNode.isArray()) {
                log.debug("Found {} items in AI response array", rootNode.size());
                for (JsonNode node : rootNode) {
                    try {
                        T entity = parseEntityFromAI(node, type);
                        if (entity != null) {
                            results.add(entity);
                            log.debug("Successfully parsed {} entity: {}", type, entity);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse {} entity from AI response: {}", type, e.getMessage());
                        log.debug("Problematic node: {}", node.toString());
                    }
                }
            } else {
                log.warn("AI response is not a JSON array for type: {}", type);
            }

        } catch (Exception e) {
            log.error("Failed to parse AI {} response: {}", type, e.getMessage());
            log.error("Raw response was: {}", jsonResponse);
        }

        log.info("Parsed {} {} entities from AI response", results.size(), type);
        return results;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseEntityFromAI(JsonNode node, String type) {
        try {
            switch (type) {
                case "topics":
                    int week = node.path("week").asInt(1);
                    String title = node.path("title").asText("Unnamed Topic");
                    String description = node.path("description").asText("");
                    String difficulty = node.path("difficulty").asText("MEDIUM");

                    log.debug("Parsing topic: Week {} - {}", week, title);

                    Topic topic = Topic.builder()
                            .week(week)
                            .title(truncate(title, 500))
                            .description(description)
                            .difficultyLevel(parseDifficulty(difficulty))
                            .build();
                    return (T) topic;

                case "deadlines":
                    String dateStr = node.path("date").asText();
                    if (dateStr.isEmpty()) {
                        dateStr = node.path("dueDate").asText();
                    }
                    LocalDateTime date = parseDate(dateStr);

                    Deadline deadline = Deadline.builder()
                            .title(truncate(node.path("title").asText("Unnamed Deadline"), 500))
                            .type(parseDeadlineType(node.path("type").asText("ASSIGNMENT")))
                            .date(date != null ? date : LocalDateTime.now().plusWeeks(2))
                            .description(node.path("description").asText(""))
                            .build();
                    return (T) deadline;

                case "materials":
                    String link = node.path("link").asText("");
                    if (link.isEmpty()) {
                        link = node.path("url").asText("");
                    }

                    String materialTitle = node.path("title").asText("Unnamed Material");

                    if (materialTitle.length() > 900) {
                        log.warn("Material title too long ({}), truncating: {}",
                                materialTitle.length(), materialTitle.substring(0, 50) + "...");
                        materialTitle = truncate(materialTitle, 900);
                    }

                    Material material = Material.builder()
                            .title(materialTitle)
                            .type(parseMaterialType(node.path("type").asText("READING")))
                            .link(link)
                            .build();
                    return (T) material;

                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Failed to parse {} entity from AI node", type, e);
            return null;
        }
    }

    private Topic.DifficultyLevel parseDifficulty(String difficulty) {
        try {
            return Topic.DifficultyLevel.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Topic.DifficultyLevel.MEDIUM;
        }
    }

    private Deadline.DeadlineType parseDeadlineType(String type) {
        try {
            return Deadline.DeadlineType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Deadline.DeadlineType.ASSIGNMENT;
        }
    }

    private Material.MaterialType parseMaterialType(String type) {
        try {
            return Material.MaterialType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.MaterialType.READING;
        }
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            if (dateStr.length() == 10) {
                return LocalDateTime.parse(dateStr + "T23:59:59");
            }
            return LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            log.debug("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        log.warn("Truncating text from {} to {} characters", text.length(), maxLength);
        return text.substring(0, maxLength - 3) + "...";
    }
}
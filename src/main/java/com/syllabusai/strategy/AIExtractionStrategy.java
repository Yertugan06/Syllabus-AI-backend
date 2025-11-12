package com.syllabusai.strategy;

import com.syllabusai.adapter.AIService;
import com.syllabusai.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public List<Topic> extractTopics(String content) {
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
        return content != null && content.length() > 200;
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority
    }

    @Override
    public String getName() {
        return "AI_EXTRACTION_STRATEGY";
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> parseAIResponse(String jsonResponse, String type) {
        List<T> results = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    try {
                        T entity = parseEntityFromAI(node, type);
                        if (entity != null) {
                            results.add(entity);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse {} entity from AI response: {}", type, e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse AI {} response: {}", type, e.getMessage());
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private <T> T parseEntityFromAI(JsonNode node, String type) {
        try {
            switch (type) {
                case "topics":
                    Topic topic = Topic.builder()
                            .title(node.path("title").asText("Unnamed Topic"))
                            .week(node.path("week").asInt(1))
                            .description(node.path("description").asText(""))
                            .difficultyLevel(parseDifficulty(node.path("difficulty").asText("MEDIUM")))
                            .build();
                    return (T) topic;

                case "deadlines":
                    String dateStr = node.path("date").asText();
                    LocalDateTime date = parseDate(dateStr);

                    Deadline deadline = Deadline.builder()
                            .title(node.path("title").asText("Unnamed Deadline"))
                            .type(parseDeadlineType(node.path("type").asText("ASSIGNMENT")))
                            .date(date != null ? date : LocalDateTime.now().plusWeeks(2))
                            .description(node.path("description").asText(""))
                            .build();
                    return (T) deadline;

                case "materials":
                    Material material = Material.builder()
                            .title(node.path("title").asText("Unnamed Material"))
                            .type(parseMaterialType(node.path("type").asText("READING")))
                            .link(node.path("link").asText(""))
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
        try {
            return LocalDateTime.parse(dateStr + "T23:59:59");
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(dateStr);
            } catch (Exception e2) {
                log.debug("Failed to parse date: {}", dateStr);
                return null;
            }
        }
    }
}
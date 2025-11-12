package com.syllabusai.decorator;

import com.syllabusai.adapter.AIService;
import com.syllabusai.model.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AIDifficultyDecorator extends SyllabusDecorator {

    private final AIService aiService;

    // Primary constructor for Spring DI
    @Autowired
    public AIDifficultyDecorator(AIService aiService) {
        super();
        this.aiService = aiService;
    }

    // Alternative constructor for manual wrapping
    public AIDifficultyDecorator(SyllabusContent content, AIService aiService) {
        super(content);
        this.aiService = aiService;
    }

    @Override
    public SyllabusContent wrap(SyllabusContent content) {
        this.wrappedContent = content;
        return this;
    }

    @Override
    public List<Topic> getTopics() {
        if (wrappedContent == null) {
            log.warn("No wrapped content available");
            return new ArrayList<>();
        }
        List<Topic> originalTopics = wrappedContent.getTopics();
        return calculateAIDifficulty(originalTopics);
    }

    @Override
    public List<String> getEnhancedContent() {
        if (wrappedContent == null) {
            return List.of("AI Difficulty Analysis: No content available");
        }

        List<String> content = new ArrayList<>(wrappedContent.getEnhancedContent());
        List<Topic> topics = getTopics();
        content.add("AI Difficulty Analysis: Topics analyzed with Gemini AI");
        content.add("Enhanced Topics Count: " + topics.size());

        // Add difficulty summary
        long easyCount = topics.stream().filter(t -> t.getDifficultyLevel() == Topic.DifficultyLevel.EASY).count();
        long mediumCount = topics.stream().filter(t -> t.getDifficultyLevel() == Topic.DifficultyLevel.MEDIUM).count();
        long hardCount = topics.stream().filter(t -> t.getDifficultyLevel() == Topic.DifficultyLevel.HARD).count();

        content.add(String.format("Difficulty Distribution - Easy: %d, Medium: %d, Hard: %d",
                easyCount, mediumCount, hardCount));

        return content;
    }

    private List<Topic> calculateAIDifficulty(List<Topic> topics) {
        if (topics == null || topics.isEmpty() || aiService == null) {
            log.warn("No topics to enhance or AI service unavailable");
            return topics != null ? topics : new ArrayList<>();
        }

        log.info("Calculating AI difficulty for {} topics", topics.size());
        List<Topic> enhancedTopics = new ArrayList<>();

        for (Topic topic : topics) {
            try {
                Topic enhancedTopic = enhanceTopicWithAIDifficulty(topic);
                enhancedTopics.add(enhancedTopic);
            } catch (Exception e) {
                log.warn("AI difficulty analysis failed for topic '{}': {}", topic.getTitle(), e.getMessage());
                enhancedTopics.add(applyFallbackDifficulty(topic));
            }
        }

        log.debug("AI difficulty calculation completed for {} topics", enhancedTopics.size());
        return enhancedTopics;
    }

    private Topic enhanceTopicWithAIDifficulty(Topic topic) {
        String analysisPrompt = createDifficultyAnalysisPrompt(topic);
        String aiResponse = aiService.generateText(analysisPrompt);

        Topic.DifficultyLevel difficulty = parseAIDifficultyResponse(aiResponse, topic);

        return createEnhancedTopic(topic, difficulty);
    }

    private String createDifficultyAnalysisPrompt(Topic topic) {
        return String.format("""
            Analyze the academic difficulty level of this course topic for university students.
            
            TOPIC: %s
            DESCRIPTION: %s
            WEEK: %d
            
            Consider these factors:
            1. Complexity of concepts (basic, intermediate, advanced)
            2. Required background knowledge
            3. Technical vs theoretical nature
            4. Typical student workload
            5. Prerequisite requirements
            
            Based on your analysis, classify the difficulty as:
            - EASY: Introductory, basic concepts, minimal prerequisites
            - MEDIUM: Intermediate, some prerequisites, moderate complexity
            - HARD: Advanced, significant prerequisites, complex concepts
            
            Respond ONLY with one of these three words: EASY, MEDIUM, or HARD.
            Do not include any explanations or additional text.
            """,
                topic.getTitle(),
                topic.getDescription() != null ? topic.getDescription() : "No description provided",
                topic.getWeek() != null ? topic.getWeek() : 1
        );
    }

    private Topic.DifficultyLevel parseAIDifficultyResponse(String aiResponse, Topic topic) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            log.debug("Empty AI response for topic: {}", topic.getTitle());
            return determineFallbackDifficulty(topic);
        }

        String cleanResponse = aiResponse.trim().toUpperCase();

        try {
            return Topic.DifficultyLevel.valueOf(cleanResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid AI difficulty response '{}' for topic: {}", cleanResponse, topic.getTitle());
            return extractDifficultyFromText(cleanResponse, topic);
        }
    }

    private Topic.DifficultyLevel extractDifficultyFromText(String response, Topic topic) {
        String lowerResponse = response.toLowerCase();

        if (lowerResponse.contains("easy") || lowerResponse.contains("basic") ||
                lowerResponse.contains("introductory") || lowerResponse.contains("fundamental")) {
            return Topic.DifficultyLevel.EASY;
        } else if (lowerResponse.contains("hard") || lowerResponse.contains("advanced") ||
                lowerResponse.contains("complex") || lowerResponse.contains("expert")) {
            return Topic.DifficultyLevel.HARD;
        } else {
            return Topic.DifficultyLevel.MEDIUM;
        }
    }

    private Topic.DifficultyLevel determineFallbackDifficulty(Topic topic) {
        if (topic == null || topic.getTitle() == null) {
            return Topic.DifficultyLevel.MEDIUM;
        }

        String text = (topic.getTitle() + " " +
                (topic.getDescription() != null ? topic.getDescription() : "")).toLowerCase();

        // Simple fallback analysis
        if (text.contains("introduction") || text.contains("overview") || text.contains("basic")) {
            return Topic.DifficultyLevel.EASY;
        } else if (text.contains("advanced") || text.contains("complex") || text.contains("research")) {
            return Topic.DifficultyLevel.HARD;
        } else {
            return Topic.DifficultyLevel.MEDIUM;
        }
    }

    private Topic createEnhancedTopic(Topic original, Topic.DifficultyLevel difficulty) {
        return Topic.builder()
                .id(original.getId())
                .title(original.getTitle())
                .description(enhanceDescription(original.getDescription(), difficulty))
                .week(original.getWeek())
                .difficultyLevel(difficulty)
                .syllabus(original.getSyllabus())
                .build();
    }

    private Topic applyFallbackDifficulty(Topic topic) {
        Topic.DifficultyLevel fallbackDifficulty = determineFallbackDifficulty(topic);
        return createEnhancedTopic(topic, fallbackDifficulty);
    }

    private String enhanceDescription(String originalDescription, Topic.DifficultyLevel difficulty) {
        String baseDescription = originalDescription != null ? originalDescription : "Topic content";
        return String.format("%s [AI Difficulty: %s]", baseDescription, difficulty);
    }
}
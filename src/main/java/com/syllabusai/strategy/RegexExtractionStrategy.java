package com.syllabusai.strategy;

import com.syllabusai.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RegexExtractionStrategy implements ExtractionStrategy {

    private static final Pattern TOPIC_PATTERN = Pattern.compile(
            "(?i)(?:week|lecture|topic|chapter|module|session|lesson)\\s*(\\d+)[:\\-\\.]?\\s*([^\\n]{5,200})",
            Pattern.MULTILINE
    );

    private static final Pattern DEADLINE_PATTERN = Pattern.compile(
            "(?i)(assignment|homework|exam|test|quiz|project|midterm|final|endterm).*?(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})",
            Pattern.MULTILINE
    );

    private static final Pattern MATERIAL_PATTERN = Pattern.compile(
            "(?i)(textbook|book|reading|resource|material|reference)[:\\-]\\s*([^\\n]{5,200})",
            Pattern.MULTILINE
    );

    @Override
    public List<Topic> extractTopics(String content) {
        List<Topic> topics = new ArrayList<>();
        Matcher matcher = TOPIC_PATTERN.matcher(content);

        int count = 0;
        while (matcher.find() && count < 25) {
            try {
                int week = Integer.parseInt(matcher.group(1));
                String title = matcher.group(2).trim();

                if (isValidTopic(title)) {
                    Topic topic = Topic.builder()
                            .title(title)
                            .week(week)
                            .description("Extracted from syllabus structure")
                            .difficultyLevel(Topic.DifficultyLevel.MEDIUM)
                            .build();
                    topics.add(topic);
                    count++;
                    log.debug("Extracted topic: Week {} - {}", week, title);
                }
            } catch (NumberFormatException e) {
                log.debug("Invalid week number: {}", matcher.group(1));
            }
        }

        log.info("Regex strategy extracted {} topics", topics.size());
        return topics;
    }

    @Override
    public List<Deadline> extractDeadlines(String content) {
        List<Deadline> deadlines = new ArrayList<>();
        Matcher matcher = DEADLINE_PATTERN.matcher(content);

        int count = 0;
        while (matcher.find() && count < 15) {
            try {
                String type = matcher.group(1);
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                int year = Integer.parseInt(matcher.group(4));

                if (year < 100) year += 2000;

                LocalDateTime date = LocalDateTime.of(year, month, day, 23, 59);

                Deadline deadline = Deadline.builder()
                        .title(type + " " + (count + 1))
                        .type(mapDeadlineType(type))
                        .date(date)
                        .description("Automatically extracted deadline")
                        .build();
                deadlines.add(deadline);
                count++;
                log.debug("Extracted deadline: {} on {}", type, date);

            } catch (Exception e) {
                log.debug("Invalid deadline format", e);
            }
        }

        if (deadlines.isEmpty()) {
            log.info("No explicit deadlines found, creating standard 10-week program deadlines");
            deadlines.addAll(createStandardDeadlines());
        }

        log.info("Regex strategy extracted {} deadlines", deadlines.size());
        return deadlines;
    }

    @Override
    public List<Material> extractMaterials(String content) {
        List<Material> materials = new ArrayList<>();
        Matcher matcher = MATERIAL_PATTERN.matcher(content);

        while (matcher.find()) {
            String type = matcher.group(1);
            String title = matcher.group(2).trim();

            if (title.length() < 10 || isGenericMaterial(title)) {
                continue;
            }

            Material material = Material.builder()
                    .title(title)
                    .type(mapMaterialType(type))
                    .link("")
                    .build();
            materials.add(material);
            log.debug("Extracted material: {}", title);
        }

        log.info("Regex strategy extracted {} materials", materials.size());
        return materials;
    }

    @Override
    public boolean supports(String content) {
        return content != null && content.length() > 50;
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public int getConfidence(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int confidence = 30; // Base confidence

        if (TOPIC_PATTERN.matcher(content).find()) confidence += 20;
        if (DEADLINE_PATTERN.matcher(content).find()) confidence += 20;
        if (MATERIAL_PATTERN.matcher(content).find()) confidence += 15;

        if (content.toLowerCase().contains("week") && content.toLowerCase().contains("topic")) {
            confidence += 15;
        }

        return Math.min(confidence, 100);
    }

    @Override
    public String getName() {
        return "REGEX_EXTRACTION_STRATEGY";
    }

    private boolean isValidTopic(String title) {
        return title.length() >= 5 &&
                !title.toLowerCase().contains("syllabus") &&
                !title.toLowerCase().contains("introduction to course") &&
                !title.toLowerCase().contains("table of contents");
    }

    private boolean isGenericMaterial(String title) {
        String lower = title.toLowerCase();
        return lower.contains("see above") ||
                lower.contains("as needed") ||
                lower.contains("various") ||
                lower.length() < 10;
    }

    private Deadline.DeadlineType mapDeadlineType(String type) {
        String lower = type.toLowerCase();
        if (lower.contains("exam") || lower.contains("test") ||
                lower.contains("midterm") || lower.contains("final") ||
                lower.contains("endterm")) {
            return Deadline.DeadlineType.EXAM;
        }
        if (lower.contains("quiz")) return Deadline.DeadlineType.QUIZ;
        if (lower.contains("project")) return Deadline.DeadlineType.PROJECT;
        return Deadline.DeadlineType.ASSIGNMENT;
    }

    private Material.MaterialType mapMaterialType(String type) {
        String lower = type.toLowerCase();
        if (lower.contains("textbook") || lower.contains("book")) {
            return Material.MaterialType.TEXTBOOK;
        }
        if (lower.contains("video")) return Material.MaterialType.VIDEO;
        if (lower.contains("website") || lower.contains("resource")) {
            return Material.MaterialType.WEBSITE;
        }
        return Material.MaterialType.READING;
    }

    private List<Deadline> createStandardDeadlines() {
        List<Deadline> defaults = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        defaults.add(Deadline.builder()
                .title("Midterm Examination")
                .type(Deadline.DeadlineType.EXAM)
                .date(now.plusWeeks(5))
                .description("Midterm exam at week 5")
                .build());

        defaults.add(Deadline.builder()
                .title("Final Examination")
                .type(Deadline.DeadlineType.EXAM)
                .date(now.plusWeeks(10))
                .description("Final exam at week 10")
                .build());

        log.info("Created standard 10-week program deadlines");
        return defaults;
    }
}
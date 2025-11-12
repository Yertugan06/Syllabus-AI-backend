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
            "(?i)(?:week|lecture|topic|chapter|module)\\s*(\\d+)[:\\-]?\\s*([^\\n]{5,100})",
            Pattern.MULTILINE
    );

    private static final Pattern DEADLINE_PATTERN = Pattern.compile(
            "(?i)(assignment|homework|exam|test|quiz|project).*?(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})",
            Pattern.MULTILINE
    );

    private static final Pattern MATERIAL_PATTERN = Pattern.compile(
            "(?i)(textbook|reading|book|material|resource)[:\\-]\\s*\"?([^\"]+)\"?",
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
                }
            } catch (NumberFormatException e) {
                log.debug("Invalid week number: {}", matcher.group(1));
            }
        }

        log.debug("Regex strategy extracted {} topics", topics.size());
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

            } catch (Exception e) {
                log.debug("Invalid deadline format", e);
            }
        }

        if (deadlines.isEmpty()) {
            deadlines.addAll(createDefaultDeadlines());
        }

        log.debug("Regex strategy extracted {} deadlines", deadlines.size());
        return deadlines;
    }

    @Override
    public List<Material> extractMaterials(String content) {
        List<Material> materials = new ArrayList<>();
        Matcher matcher = MATERIAL_PATTERN.matcher(content);

        while (matcher.find()) {
            String type = matcher.group(1);
            String title = matcher.group(2).trim();

            Material material = Material.builder()
                    .title(title)
                    .type(mapMaterialType(type))
                    .link("")
                    .build();
            materials.add(material);
        }

        if (materials.isEmpty()) {
            materials.add(createDefaultMaterial());
        }

        log.debug("Regex strategy extracted {} materials", materials.size());
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
    public String getName() {
        return "REGEX_EXTRACTION_STRATEGY";
    }

    private boolean isValidTopic(String title) {
        return title.length() >= 5 &&
                !title.toLowerCase().contains("syllabus") &&
                !title.toLowerCase().contains("introduction to course");
    }

    private Deadline.DeadlineType mapDeadlineType(String type) {
        switch (type.toLowerCase()) {
            case "exam": case "test": return Deadline.DeadlineType.EXAM;
            case "quiz": return Deadline.DeadlineType.QUIZ;
            case "project": return Deadline.DeadlineType.PROJECT;
            default: return Deadline.DeadlineType.ASSIGNMENT;
        }
    }

    private Material.MaterialType mapMaterialType(String type) {
        switch (type.toLowerCase()) {
            case "textbook": case "book": return Material.MaterialType.TEXTBOOK;
            case "video": return Material.MaterialType.VIDEO;
            case "website": case "resource": return Material.MaterialType.WEBSITE;
            default: return Material.MaterialType.READING;
        }
    }

    private List<Deadline> createDefaultDeadlines() {
        List<Deadline> defaults = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        defaults.add(Deadline.builder()
                .title("Assignment 1")
                .type(Deadline.DeadlineType.ASSIGNMENT)
                .date(now.plusWeeks(2))
                .description("First course assignment")
                .build());

        defaults.add(Deadline.builder()
                .title("Midterm Examination")
                .type(Deadline.DeadlineType.EXAM)
                .date(now.plusWeeks(6))
                .description("Midterm exam covering first half of course")
                .build());

        return defaults;
    }

    private Material createDefaultMaterial() {
        return Material.builder()
                .title("Course Textbook and Materials")
                .type(Material.MaterialType.TEXTBOOK)
                .link("")
                .build();
    }
}
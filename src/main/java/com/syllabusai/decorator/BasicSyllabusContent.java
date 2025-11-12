package com.syllabusai.decorator;

import com.syllabusai.model.Syllabus;
import com.syllabusai.model.Topic;
import java.util.ArrayList;
import java.util.List;

public class BasicSyllabusContent implements SyllabusContent {
    private final Syllabus syllabus;
    private final String rawContent;

    public BasicSyllabusContent(Syllabus syllabus, String rawContent) {
        this.syllabus = syllabus;
        this.rawContent = rawContent;
    }

    @Override
    public String getContent() {
        return rawContent;
    }

    @Override
    public List<Topic> getTopics() {
        return syllabus.getTopics() != null ? syllabus.getTopics() : new ArrayList<>();
    }

    @Override
    public List<String> getEnhancedContent() {
        List<String> content = new ArrayList<>();
        content.add("Original Content: " + rawContent);
        return content;
    }
}
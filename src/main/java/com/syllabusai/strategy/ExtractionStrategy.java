package com.syllabusai.strategy;

import com.syllabusai.model.Topic;
import com.syllabusai.model.Deadline;
import com.syllabusai.model.Material;

import java.util.List;

public interface ExtractionStrategy {
    List<Topic> extractTopics(String content);
    List<Deadline> extractDeadlines(String content);
    List<Material> extractMaterials(String content);
    boolean supports(String content);
    int getPriority();
    String getName();

    default int getConfidence(String content) {
        return supports(content) ? 80 : 0;
    }
}
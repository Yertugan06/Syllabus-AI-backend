package com.syllabusai.decorator;

import com.syllabusai.model.Topic;
import java.util.List;

public interface SyllabusContent {
    String getContent();
    List<Topic> getTopics();
    List<String> getEnhancedContent();
}
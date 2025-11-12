package com.syllabusai.decorator;

import com.syllabusai.model.Topic;

import java.util.ArrayList;
import java.util.List;

public abstract class SyllabusDecorator implements SyllabusContent {
    protected SyllabusContent wrappedContent;

    // Default constructor for Lombok/Spring
    public SyllabusDecorator() {
    }

    public SyllabusDecorator(SyllabusContent content) {
        this.wrappedContent = content;
    }

    public void setWrappedContent(SyllabusContent wrappedContent) {
        this.wrappedContent = wrappedContent;
    }

    @Override
    public String getContent() {
        return wrappedContent != null ? wrappedContent.getContent() : "";
    }

    @Override
    public List<Topic> getTopics() {
        return wrappedContent != null ? wrappedContent.getTopics() : new ArrayList<>();
    }

    @Override
    public List<String> getEnhancedContent() {
        return wrappedContent != null ? wrappedContent.getEnhancedContent() : new ArrayList<>();
    }

    public SyllabusContent wrap(SyllabusContent content) {
        this.wrappedContent = content;
        return this;
    }
}
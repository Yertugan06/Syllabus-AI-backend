package com.syllabusai.adapter;


public interface AIService {

    String extractTopics(String content);

    String extractDeadlines(String content);

    String extractMaterials(String content);

    String analyzeSyllabusStructure(String content);

    String generateText(String prompt);

    String analyzeDocument(byte[] documentBytes, String mimeType, String prompt);
}
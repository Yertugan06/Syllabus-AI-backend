package com.syllabusai.strategy;

import com.syllabusai.model.Topic;
import com.syllabusai.model.Deadline;
import com.syllabusai.model.Material;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractionContext {

    private final List<ExtractionStrategy> strategies;

    /**
     * Extract topics using the best available strategy
     */
    public List<Topic> extractTopics(String content) {
        ExtractionStrategy strategy = selectBestStrategy(content);
        log.debug("Selected strategy for topic extraction: {} (confidence: {}%)",
                strategy.getName(), strategy.getConfidence(content));

        return strategy.extractTopics(content);
    }

    /**
     * Extract deadlines using the best available strategy
     */
    public List<Deadline> extractDeadlines(String content) {
        ExtractionStrategy strategy = selectBestStrategy(content);
        log.debug("Selected strategy for deadline extraction: {} (confidence: {}%)",
                strategy.getName(), strategy.getConfidence(content));

        return strategy.extractDeadlines(content);
    }

    /**
     * Extract materials using the best available strategy
     */
    public List<Material> extractMaterials(String content) {
        ExtractionStrategy strategy = selectBestStrategy(content);
        log.debug("Selected strategy for material extraction: {} (confidence: {}%)",
                strategy.getName(), strategy.getConfidence(content));

        return strategy.extractMaterials(content);
    }

    /**
     * Select the best strategy based on priority and confidence
     */
    public ExtractionStrategy selectBestStrategy(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        // First, try strategies by priority
        Optional<ExtractionStrategy> priorityStrategy = strategies.stream()
                .filter(strategy -> strategy.supports(content))
                .min(Comparator.comparingInt(ExtractionStrategy::getPriority));

        if (priorityStrategy.isPresent()) {
            ExtractionStrategy strategy = priorityStrategy.get();
            int confidence = strategy.getConfidence(content);

            // Only use if confidence is reasonable
            if (confidence >= 50) {
                return strategy;
            }

            log.debug("Strategy {} has low confidence ({}%), trying alternatives",
                    strategy.getName(), confidence);
        }

        // Fallback: use strategy with highest confidence
        Optional<ExtractionStrategy> confidenceStrategy = strategies.stream()
                .filter(strategy -> strategy.supports(content))
                .max(Comparator.comparingInt(strategy -> strategy.getConfidence(content)));

        if (confidenceStrategy.isPresent()) {
            ExtractionStrategy strategy = confidenceStrategy.get();
            log.warn("Using fallback strategy: {} with {}% confidence",
                    strategy.getName(), strategy.getConfidence(content));
            return strategy;
        }

        // Last resort: use any strategy that can handle the content
        Optional<ExtractionStrategy> anyStrategy = strategies.stream()
                .filter(strategy -> strategy.supports(content))
                .findFirst();

        if (anyStrategy.isPresent()) {
            log.error("Using last-resort strategy: {}", anyStrategy.get().getName());
            return anyStrategy.get();
        }

        throw new IllegalStateException("No suitable extraction strategy found for content");
    }

    /**
     * Get strategy analysis for debugging and monitoring
     */
    public StrategyAnalysis analyzeStrategies(String content) {
        List<StrategyAnalysis.StrategyInfo> strategyInfos = strategies.stream()
                .map(strategy -> new StrategyAnalysis.StrategyInfo(
                        strategy.getName(),
                        strategy.getPriority(),
                        strategy.supports(content),
                        strategy.getConfidence(content)
                ))
                .sorted(Comparator.comparingInt(StrategyAnalysis.StrategyInfo::getPriority))
                .toList();

        return new StrategyAnalysis(content.length(), strategyInfos);
    }

    /**
     * Analysis result for strategy selection
     */
    public static class StrategyAnalysis {
        private final int contentLength;
        private final List<StrategyInfo> strategies;

        public StrategyAnalysis(int contentLength, List<StrategyInfo> strategies) {
            this.contentLength = contentLength;
            this.strategies = strategies;
        }

        public static class StrategyInfo {
            private final String name;
            private final int priority;
            private final boolean supported;
            private final int confidence;

            public StrategyInfo(String name, int priority, boolean supported, int confidence) {
                this.name = name;
                this.priority = priority;
                this.supported = supported;
                this.confidence = confidence;
            }

            // Getters
            public String getName() { return name; }
            public int getPriority() { return priority; }
            public boolean isSupported() { return supported; }
            public int getConfidence() { return confidence; }
        }

        // Getters
        public int getContentLength() { return contentLength; }
        public List<StrategyInfo> getStrategies() { return strategies; }
    }
}
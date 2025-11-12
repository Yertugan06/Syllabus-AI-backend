package com.syllabusai.service;

import com.syllabusai.model.Topic;
import com.syllabusai.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public List<Topic> getTopicsBySyllabusId(Long syllabusId) {
        return topicRepository.findBySyllabusIdOrderByWeekAsc(syllabusId);
    }

    public List<Topic> getTopicsByDifficulty(Long syllabusId, Topic.DifficultyLevel difficulty) {
        return topicRepository.findBySyllabusIdAndDifficulty(syllabusId, difficulty);
    }
}
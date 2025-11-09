package com.syllabusai.service;

import com.syllabusai.dto.TopicDTO;
import com.syllabusai.mapper.TopicMapper;
import com.syllabusai.model.Topic;
import com.syllabusai.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public Topic saveTopic(Topic topic) {
        return topicRepository.save(topic);
    }

    public List<Topic> getTopicsBySyllabusId(Long syllabusId) {
        return topicRepository.findBySyllabusId(syllabusId);
    }

    public TopicDTO toDTO(Topic topic) {
        return TopicMapper.toDTO(topic);
    }

    public List<Topic> saveAll(List<Topic> topics) {
        return topicRepository.saveAll(topics);
    }
}
package com.syllabusai.mapper;

import com.syllabusai.dto.TopicDTO;
import com.syllabusai.model.Topic;

public class TopicMapper {

    public static TopicDTO toDTO(Topic topic){
        return TopicDTO.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .week(topic.getWeek())
                .difficultyLevel(topic.getDifficultyLevel())
                .build();
    }
}

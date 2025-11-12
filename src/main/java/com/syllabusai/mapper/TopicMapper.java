package com.syllabusai.mapper;

import com.syllabusai.dto.TopicDTO;
import com.syllabusai.model.Topic;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

    public static TopicDTO toDTO(Topic topic) {
        if (topic == null) {
            return null;
        }

        return TopicDTO.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .week(topic.getWeek())
                .difficultyLevel(topic.getDifficultyLevel())
                .build();
    }

    public static Topic toEntity(TopicDTO dto) {
        if (dto == null) {
            return null;
        }

        return Topic.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .week(dto.getWeek())
                .difficultyLevel(dto.getDifficultyLevel())
                .build();
    }
}
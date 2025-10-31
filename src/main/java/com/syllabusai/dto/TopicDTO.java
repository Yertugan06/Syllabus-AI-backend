package com.syllabusai.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TopicDTO {
    private Long id;
    private String title;
    private String description;
    private Integer week;
    private String difficultyLevel;
}

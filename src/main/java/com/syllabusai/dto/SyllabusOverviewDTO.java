package com.syllabusai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusOverviewDTO {
    private SyllabusDTO syllabus;
    private List<TopicDTO> topics;
    private List<DeadlineDTO> deadlines;
    private List<MaterialDTO> materials;
    private Integer totalWeeks;
    private List<DeadlineDTO> upcomingDeadlines;
}
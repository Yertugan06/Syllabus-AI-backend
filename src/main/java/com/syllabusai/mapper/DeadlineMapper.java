package com.syllabusai.mapper;

import com.syllabusai.dto.DeadlineDTO;
import com.syllabusai.model.Deadline;

public class DeadlineMapper {
    public static DeadlineDTO toDTO(Deadline deadline) {

        return DeadlineDTO.builder()
                .id(deadline.getId())
                .title(deadline.getTitle())
                .date(deadline.getDate())
                .build();
    }
}
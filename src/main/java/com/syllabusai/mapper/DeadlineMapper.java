package com.syllabusai.mapper;

import com.syllabusai.dto.DeadlineDTO;
import com.syllabusai.model.Deadline;
import org.springframework.stereotype.Component;

@Component
public class DeadlineMapper {

    public static DeadlineDTO toDTO(Deadline deadline) {
        if (deadline == null) {
            return null;
        }

        return DeadlineDTO.builder()
                .id(deadline.getId())
                .title(deadline.getTitle())
                .date(deadline.getDate())
                .type(deadline.getType())
                .description(deadline.getDescription())
                .build();
    }

    public static Deadline toEntity(DeadlineDTO dto) {
        if (dto == null) {
            return null;
        }

        return Deadline.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .date(dto.getDate())
                .type(dto.getType())
                .description(dto.getDescription())
                .build();
    }
}
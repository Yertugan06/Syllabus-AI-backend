package com.syllabusai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.syllabusai.model.Deadline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineDTO {
    private Long id;
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;  // Changed from 'dueDate' to match entity field 'date'

    private Deadline.DeadlineType type;  // Use enum from entity
    private String description;
}
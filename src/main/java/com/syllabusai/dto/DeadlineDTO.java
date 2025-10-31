package com.syllabusai.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadlineDTO {
    private Long id;
    private String title;
    private LocalDateTime date;
    private String type;
}

package com.syllabusai.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusDTO {
    private Long id;
    private String filename;
    private String status;
    private LocalDateTime uploadDate;
    private List<String> topics;
}

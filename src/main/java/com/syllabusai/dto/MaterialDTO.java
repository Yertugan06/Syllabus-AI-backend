package com.syllabusai.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDTO {
    private long id;
    private String title;
    private String type;
    private String link;
}

package com.syllabusai.dto;

import com.syllabusai.model.Material;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {
    private Long id;
    private String title;
    private Material.MaterialType type;  // Use enum from entity
    private String url;
}
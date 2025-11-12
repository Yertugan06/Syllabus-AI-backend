package com.syllabusai.mapper;

import com.syllabusai.dto.MaterialDTO;
import com.syllabusai.model.Material;
import org.springframework.stereotype.Component;

@Component
public class MaterialMapper {

    public static MaterialDTO toDTO(Material material) {
        if (material == null) {
            return null;
        }

        return MaterialDTO.builder()
                .id(material.getId())
                .title(material.getTitle())
                .type(material.getType())
                .url(material.getLink())
                .build();
    }

    public static Material toEntity(MaterialDTO dto) {
        if (dto == null) {
            return null;
        }

        return Material.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .type(dto.getType())
                .link(dto.getUrl())
                .build();
    }
}
package com.syllabusai.mapper;


import com.syllabusai.dto.MaterialDTO;
import com.syllabusai.model.Material;

public class MaterialMapper {
    public static MaterialDTO toDTO (Material material){
        return MaterialDTO.builder()
                .id(material.getId())
                .title(material.getTitle())
                .type(material.getType())
                .link(material.getLink())
                .build();
    }

}

package com.syllabusai.mapper;

import com.syllabusai.dto.SyllabusDTO;
import com.syllabusai.model.Syllabus;
import org.springframework.stereotype.Component;

@Component
public class SyllabusMapper {

    public static SyllabusDTO toDTO(Syllabus syllabus) {
        if (syllabus == null) {
            return null;
        }

        return SyllabusDTO.builder()
                .id(syllabus.getId())
                .fileName(syllabus.getFilename())
                .uploadDate(syllabus.getUploadDate())
                .status("PROCESSED")
                .build();
    }

    public static Syllabus toEntity(SyllabusDTO dto) {
        if (dto == null) {
            return null;
        }

        return Syllabus.builder()
                .id(dto.getId())
                .filename(dto.getFileName())
                .uploadDate(dto.getUploadDate())
                .build();
    }
}
package com.syllabusai.mapper;

import com.syllabusai.dto.SyllabusDTO;
import com.syllabusai.model.Syllabus;

import java.util.stream.Collectors;

public class SyllabusMapper{

    public static SyllabusDTO toDTO(Syllabus syllabus) {
        return SyllabusDTO.builder()
                .id(syllabus.getId())
                .filename(syllabus.getFilename())
                .status(syllabus.getStatus())
                .uploadDate(syllabus.getUploadDate())
                .topics(
                        syllabus.getTopics() != null
                                ? syllabus.getTopics().stream()
                                .map(t -> t.getTitle())
                                .collect(Collectors.toList())
                                : null
                )
                .build();
    }
}
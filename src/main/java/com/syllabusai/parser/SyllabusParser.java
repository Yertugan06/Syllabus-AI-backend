package com.syllabusai.parser;

import com.syllabusai.model.Syllabus;
import org.springframework.web.multipart.MultipartFile;

public interface SyllabusParser {
    boolean supports(MultipartFile file);
    Syllabus parse(MultipartFile file) throws Exception;
}
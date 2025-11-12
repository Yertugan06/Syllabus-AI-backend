package com.syllabusai.parser;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class SyllabusParserFactory {

    private final GenericSyllabusParser genericParser;

    public SyllabusParserFactory(GenericSyllabusParser genericParser) {
        this.genericParser = genericParser;
    }

    public SyllabusParser createParser(MultipartFile file) {
        return genericParser;
    }
}
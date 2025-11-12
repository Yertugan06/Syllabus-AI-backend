package com.syllabusai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SyllabusAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyllabusAiApplication.class, args);
        log.info("Syllabus AI Application started successfully");
    }
}
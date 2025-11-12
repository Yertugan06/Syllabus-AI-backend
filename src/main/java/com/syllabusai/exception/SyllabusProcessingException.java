package com.syllabusai.exception;

public class SyllabusProcessingException extends RuntimeException {

    public SyllabusProcessingException(String message) {
        super(message);
    }

    public SyllabusProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyllabusProcessingException(Throwable cause) {
        super(cause);
    }
}
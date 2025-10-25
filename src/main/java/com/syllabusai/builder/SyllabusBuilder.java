package com.syllabusai.builder;

import com.syllabusai.model.Syllabus;
import com.syllabusai.model.User;
import java.time.LocalDateTime;

public class SyllabusBuilder {
    private User user;
    private String filename;
    private String status;
    private LocalDateTime uploadDate;

    public SyllabusBuilder user(User user) {
        this.user = user;
        return this;
    }

    public SyllabusBuilder filename(String filename) {
        this.filename = filename;
        return this;
    }

    public SyllabusBuilder uploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
        return this;
    }

    public SyllabusBuilder status(String status) {
        this.status = status;
        return this;
    }
    public Syllabus build() {
        Syllabus syllabus = new Syllabus();
        syllabus.setUser(user);
        syllabus.setFilename(filename);
        syllabus.setUploadDate(uploadDate != null ? uploadDate : LocalDateTime.now());
        syllabus.setStatus(status);
        return syllabus;
    }
}

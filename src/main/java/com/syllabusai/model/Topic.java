package com.syllabusai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "topics")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Integer week;
    private String difficultyLevel;

    @ManyToOne
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;
}

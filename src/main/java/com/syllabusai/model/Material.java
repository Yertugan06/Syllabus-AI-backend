package com.syllabusai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "materials")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    private String link;

    @ManyToOne
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;
}

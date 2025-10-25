package com.syllabusai.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Integer week;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @ManyToOne
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;


}

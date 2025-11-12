package com.syllabusai.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topics", indexes = {
        @Index(name = "idx_topic_syllabus_week", columnList = "syllabus_id, week")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer week;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    @JsonIgnore
    private Syllabus syllabus;

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
}
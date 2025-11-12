package com.syllabusai.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;


@Entity
@Table(name = "deadlines", indexes = {
        @Index(name = "idx_deadline_date", columnList = "date"),
        @Index(name = "idx_deadline_syllabus", columnList = "syllabus_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Check(constraints = "date > CURRENT_TIMESTAMP")
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeadlineType type = DeadlineType.ASSIGNMENT;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id", nullable = false)
    @JsonIgnore
    private Syllabus syllabus;

    public enum DeadlineType {
        ASSIGNMENT, EXAM, QUIZ, PROJECT
    }
}
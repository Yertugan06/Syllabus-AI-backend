package com.syllabusai.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deadlines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deadline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime date;
    private String type;

    @ManyToOne
    @JoinColumn(name = "syllabus_id", nullable = false)
    private Syllabus syllabus;
}

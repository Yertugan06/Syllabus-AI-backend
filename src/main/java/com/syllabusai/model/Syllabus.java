package com.syllabusai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "syllabi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Syllabus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String filename;
    private String status;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Material> materials;

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Topic> topics;

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deadline> deadlines;

}

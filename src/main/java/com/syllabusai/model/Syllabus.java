package com.syllabusai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "syllabi")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Syllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String filename;

    @Column(nullable = false)
    private String status; // PARSED, PROCESSING, ERROR

    @CreationTimestamp
    @Column(name = "upload_date", updatable = false)
    private LocalDateTime uploadDate;

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Topic> topics = new ArrayList<>();

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Material> materials = new ArrayList<>();

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Deadline> deadlines = new ArrayList<>();

    public void addTopic(Topic topic) {
        topics.add(topic);
        topic.setSyllabus(this);
    }

    public void addMaterial(Material material) {
        materials.add(material);
        material.setSyllabus(this);
    }

    public void addDeadline(Deadline deadline) {
        deadlines.add(deadline);
        deadline.setSyllabus(this);
    }
}
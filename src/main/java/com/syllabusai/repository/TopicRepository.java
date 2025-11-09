package com.syllabusai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.syllabusai.model.*;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findBySyllabusId(Long syllabusId);
    List<Topic> findBySyllabusIdAndWeek(Long syllabusId, Integer week);
}
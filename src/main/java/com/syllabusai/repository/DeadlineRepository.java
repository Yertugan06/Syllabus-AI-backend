package com.syllabusai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.syllabusai.model.*;

import java.util.List;

public interface DeadlineRepository extends JpaRepository<Deadline, Long> {
    List<Deadline> findBySyllabusId(Long syllabusId);

    List<Deadline> findBySyllabusIdAndType(Long syllabusId, String type);
}
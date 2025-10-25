package com.syllabusai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.syllabusai.model.*;

public interface DeadlineRepository extends JpaRepository<Deadline, Long> {}
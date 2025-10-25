package com.syllabusai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.syllabusai.model.*;

public interface UserRepository extends JpaRepository<User, Long> {}
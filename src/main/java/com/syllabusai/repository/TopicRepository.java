package com.syllabusai.repository;

import com.syllabusai.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findBySyllabusId(Long syllabusId);

    List<Topic> findBySyllabusIdOrderByWeekAsc(Long syllabusId);

    List<Topic> findBySyllabusIdAndWeek(Long syllabusId, Integer week);

    @Query("SELECT t FROM Topic t WHERE t.syllabus.id = :syllabusId AND t.difficultyLevel = :difficulty")
    List<Topic> findBySyllabusIdAndDifficulty(@Param("syllabusId") Long syllabusId,
                                              @Param("difficulty") Topic.DifficultyLevel difficulty);
}
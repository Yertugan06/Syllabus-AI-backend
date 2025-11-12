package com.syllabusai.repository;

import com.syllabusai.model.Deadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {

    List<Deadline> findBySyllabusId(Long syllabusId);

    List<Deadline> findBySyllabusIdOrderByDateAsc(Long syllabusId);

    List<Deadline> findBySyllabusIdAndType(Long syllabusId, Deadline.DeadlineType type);

    @Query("SELECT d FROM Deadline d WHERE d.syllabus.id = :syllabusId AND d.date BETWEEN :start AND :end")
    List<Deadline> findUpcomingDeadlines(@Param("syllabusId") Long syllabusId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT d FROM Deadline d WHERE d.syllabus.user.id = :userId AND d.date > :now ORDER BY d.date ASC")
    List<Deadline> findUpcomingDeadlinesByUser(@Param("userId") Long userId,
                                               @Param("now") LocalDateTime now);
}
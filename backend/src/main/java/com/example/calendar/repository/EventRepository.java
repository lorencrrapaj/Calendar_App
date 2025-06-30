package com.example.calendar.repository;

import com.example.calendar.model.Event;
import com.example.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUser(User user);
    List<Event> findByUserOrderByStartDateTimeAsc(User user);
    List<Event> findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(User user, LocalDateTime start, LocalDateTime end);
    List<Event> findByUserEmailAndStartDateTimeBetween(String email, LocalDateTime start, LocalDateTime end);
    void deleteByParentEvent(Event parentEvent);

    @Query("SELECT DISTINCT e FROM Event e JOIN e.tags t WHERE e.user = :user AND e.startDateTime BETWEEN :start AND :end AND t.id = :tagId ORDER BY e.startDateTime ASC")
    List<Event> findByUserAndStartDateTimeBetweenAndTagIdOrderByStartDateTimeAsc(
        @Param("user") User user, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end, 
        @Param("tagId") Long tagId
    );
}

package com.example.calendar.repository;

import com.example.calendar.model.Event;
import com.example.calendar.model.Tag;
import com.example.calendar.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        otherUser = User.builder()
                .email("other@example.com")
                .passwordHash("hashedpassword")
                .build();
        otherUser = entityManager.persistAndFlush(otherUser);
    }

    @Test
    void findByUser_ReturnsEventsForSpecificUser() {
        // Given
        Event event1 = Event.builder()
                .title("Event 1")
                .description("Description 1")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .user(testUser)
                .build();

        Event event2 = Event.builder()
                .title("Event 2")
                .description("Description 2")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .user(testUser)
                .build();

        Event otherEvent = Event.builder()
                .title("Other Event")
                .description("Other Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .user(otherUser)
                .build();

        entityManager.persistAndFlush(event1);
        entityManager.persistAndFlush(event2);
        entityManager.persistAndFlush(otherEvent);

        // When
        List<Event> events = eventRepository.findByUser(testUser);

        // Then
        assertThat(events).hasSize(2);
        assertThat(events).extracting(Event::getTitle).containsExactlyInAnyOrder("Event 1", "Event 2");
        assertThat(events).allMatch(event -> event.getUser().equals(testUser));
    }

    @Test
    void findByUserOrderByStartDateTimeAsc_ReturnsEventsInChronologicalOrder() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Event laterEvent = Event.builder()
                .title("Later Event")
                .description("Description")
                .startDateTime(now.plusDays(2))
                .endDateTime(now.plusDays(2).plusHours(1))
                .user(testUser)
                .build();

        Event earlierEvent = Event.builder()
                .title("Earlier Event")
                .description("Description")
                .startDateTime(now.plusDays(1))
                .endDateTime(now.plusDays(1).plusHours(1))
                .user(testUser)
                .build();

        Event middleEvent = Event.builder()
                .title("Middle Event")
                .description("Description")
                .startDateTime(now.plusDays(1).plusHours(12))
                .endDateTime(now.plusDays(1).plusHours(13))
                .user(testUser)
                .build();

        entityManager.persistAndFlush(laterEvent);
        entityManager.persistAndFlush(earlierEvent);
        entityManager.persistAndFlush(middleEvent);

        // When
        List<Event> events = eventRepository.findByUserOrderByStartDateTimeAsc(testUser);

        // Then
        assertThat(events).hasSize(3);
        assertThat(events).extracting(Event::getTitle)
                .containsExactly("Earlier Event", "Middle Event", "Later Event");
    }

    @Test
    void findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc_ReturnsEventsInRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeStart = now.plusDays(1);
        LocalDateTime rangeEnd = now.plusDays(3);

        Event beforeRange = Event.builder()
                .title("Before Range")
                .description("Description")
                .startDateTime(now)
                .endDateTime(now.plusHours(1))
                .user(testUser)
                .build();

        Event inRange1 = Event.builder()
                .title("In Range 1")
                .description("Description")
                .startDateTime(now.plusDays(1).plusHours(6))
                .endDateTime(now.plusDays(1).plusHours(7))
                .user(testUser)
                .build();

        Event inRange2 = Event.builder()
                .title("In Range 2")
                .description("Description")
                .startDateTime(now.plusDays(2))
                .endDateTime(now.plusDays(2).plusHours(1))
                .user(testUser)
                .build();

        Event afterRange = Event.builder()
                .title("After Range")
                .description("Description")
                .startDateTime(now.plusDays(4))
                .endDateTime(now.plusDays(4).plusHours(1))
                .user(testUser)
                .build();

        entityManager.persistAndFlush(beforeRange);
        entityManager.persistAndFlush(inRange1);
        entityManager.persistAndFlush(inRange2);
        entityManager.persistAndFlush(afterRange);

        // When
        List<Event> events = eventRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(
                testUser, rangeStart, rangeEnd);

        // Then
        assertThat(events).hasSize(2);
        assertThat(events).extracting(Event::getTitle)
                .containsExactly("In Range 1", "In Range 2");
    }

    @Test
    void findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc_EmptyRange_ReturnsEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeStart = now.plusDays(10);
        LocalDateTime rangeEnd = now.plusDays(11);

        Event event = Event.builder()
                .title("Event")
                .description("Description")
                .startDateTime(now)
                .endDateTime(now.plusHours(1))
                .user(testUser)
                .build();

        entityManager.persistAndFlush(event);

        // When
        List<Event> events = eventRepository.findByUserAndStartDateTimeBetweenOrderByStartDateTimeAsc(
                testUser, rangeStart, rangeEnd);

        // Then
        assertThat(events).isEmpty();
    }

    @Test
    void findByUser_NoEvents_ReturnsEmptyList() {
        // When
        List<Event> events = eventRepository.findByUser(testUser);

        // Then
        assertThat(events).isEmpty();
    }

    @Test
    void findByUserEmailAndStartDateTimeBetween_ReturnsEventsInRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeStart = now.plusDays(1);
        LocalDateTime rangeEnd = now.plusDays(3);

        Event inRange = Event.builder()
                .title("In Range Event")
                .description("Description")
                .startDateTime(now.plusDays(2))
                .endDateTime(now.plusDays(2).plusHours(1))
                .user(testUser)
                .build();

        Event outOfRange = Event.builder()
                .title("Out of Range Event")
                .description("Description")
                .startDateTime(now.plusDays(5))
                .endDateTime(now.plusDays(5).plusHours(1))
                .user(testUser)
                .build();

        entityManager.persistAndFlush(inRange);
        entityManager.persistAndFlush(outOfRange);

        // When
        List<Event> events = eventRepository.findByUserEmailAndStartDateTimeBetween(
                testUser.getEmail(), rangeStart, rangeEnd);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("In Range Event");
    }

    @Test
    void deleteByParentEvent_DeletesChildEvents() {
        // Given
        Event parentEvent = Event.builder()
                .title("Parent Event")
                .description("Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .user(testUser)
                .build();

        entityManager.persistAndFlush(parentEvent);

        Event childEvent1 = Event.builder()
                .title("Child Event 1")
                .description("Description")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .user(testUser)
                .parentEvent(parentEvent)
                .build();

        Event childEvent2 = Event.builder()
                .title("Child Event 2")
                .description("Description")
                .startDateTime(LocalDateTime.now().plusDays(2))
                .endDateTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .user(testUser)
                .parentEvent(parentEvent)
                .build();

        entityManager.persistAndFlush(childEvent1);
        entityManager.persistAndFlush(childEvent2);

        // When
        eventRepository.deleteByParentEvent(parentEvent);
        entityManager.flush();

        // Then
        List<Event> remainingEvents = eventRepository.findAll();
        assertThat(remainingEvents).hasSize(1); // Only parent should remain
        assertThat(remainingEvents.get(0).getTitle()).isEqualTo("Parent Event");
    }

    @Test
    void findByUserAndStartDateTimeBetweenAndTagIdOrderByStartDateTimeAsc_ReturnsEventsWithSpecificTag() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeStart = now.plusDays(1);
        LocalDateTime rangeEnd = now.plusDays(3);

        Tag workTag = Tag.builder()
                .name("Work")
                .user(testUser)
                .build();

        Tag personalTag = Tag.builder()
                .name("Personal")
                .user(testUser)
                .build();

        entityManager.persistAndFlush(workTag);
        entityManager.persistAndFlush(personalTag);

        Event workEvent = Event.builder()
                .title("Work Event")
                .description("Description")
                .startDateTime(now.plusDays(2))
                .endDateTime(now.plusDays(2).plusHours(1))
                .user(testUser)
                .build();

        Event personalEvent = Event.builder()
                .title("Personal Event")
                .description("Description")
                .startDateTime(now.plusDays(2).plusHours(6))
                .endDateTime(now.plusDays(2).plusHours(7))
                .user(testUser)
                .build();

        Event eventWithoutTag = Event.builder()
                .title("Event Without Tag")
                .description("Description")
                .startDateTime(now.plusDays(2).plusHours(12))
                .endDateTime(now.plusDays(2).plusHours(13))
                .user(testUser)
                .build();

        entityManager.persistAndFlush(workEvent);
        entityManager.persistAndFlush(personalEvent);
        entityManager.persistAndFlush(eventWithoutTag);

        // Associate events with tags
        Set<Tag> workTags = new HashSet<>();
        workTags.add(workTag);
        workEvent.setTags(workTags);

        Set<Tag> personalTags = new HashSet<>();
        personalTags.add(personalTag);
        personalEvent.setTags(personalTags);

        entityManager.persistAndFlush(workEvent);
        entityManager.persistAndFlush(personalEvent);

        // When
        List<Event> workEvents = eventRepository.findByUserAndStartDateTimeBetweenAndTagIdOrderByStartDateTimeAsc(
                testUser, rangeStart, rangeEnd, workTag.getId());

        // Then
        assertThat(workEvents).hasSize(1);
        assertThat(workEvents.get(0).getTitle()).isEqualTo("Work Event");
        assertThat(workEvents.get(0).getTags()).contains(workTag);
    }
}

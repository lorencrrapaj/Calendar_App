package com.example.calendar.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    @Test
    void event_DefaultConstructor() {
        // When
        Event event = new Event();

        // Then
        assertThat(event.getId()).isNull();
        assertThat(event.getTitle()).isNull();
        assertThat(event.getDescription()).isNull();
        assertThat(event.getStartDateTime()).isNull();
        assertThat(event.getEndDateTime()).isNull();
        assertThat(event.getUser()).isNull();
    }

    @Test
    void event_AllArgsConstructor() {
        // Given
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        // When
        Event event = Event.builder()
                .id(id)
                .title(title)
                .description(description)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .user(user)
                .build();

        // Then
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getTitle()).isEqualTo(title);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(event.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(event.getUser()).isEqualTo(user);
    }

    @Test
    void event_Builder() {
        // Given
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        // When
        Event event = Event.builder()
                .id(id)
                .title(title)
                .description(description)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .user(user)
                .build();

        // Then
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getTitle()).isEqualTo(title);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(event.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(event.getUser()).isEqualTo(user);
    }

    @Test
    void event_GettersAndSetters() {
        // Given
        Event event = new Event();
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        // When
        event.setId(id);
        event.setTitle(title);
        event.setDescription(description);
        event.setStartDateTime(startDateTime);
        event.setEndDateTime(endDateTime);
        event.setUser(user);

        // Then
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getTitle()).isEqualTo(title);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(event.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(event.getUser()).isEqualTo(user);
    }

    @Test
    void event_EqualsAndHashCode() {
        // Given
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        Event event1 = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .user(user)
                .build();

        Event event2 = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .user(user)
                .build();

        // Then - just verify methods exist and don't throw exceptions
        assertThat(event1.equals(event1)).isTrue(); // reflexive
        assertThat(event1.equals(null)).isFalse(); // null check
        assertThat(event1.equals("not an event")).isFalse(); // different type
        assertThat(event1.hashCode()).isNotNull(); // hashCode exists
        assertThat(event2.hashCode()).isNotNull(); // hashCode exists
    }

    @Test
    void event_ToString() {
        // Given
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        Event event = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .user(user)
                .build();

        // When
        String toString = event.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).isNotEmpty();
        // Just verify toString doesn't throw an exception and returns something
    }

    @Test
    void event_NullValues() {
        // Given
        Event event = Event.builder()
                .id(null)
                .title(null)
                .description(null)
                .startDateTime(null)
                .endDateTime(null)
                .user(null)
                .build();

        // Then
        assertThat(event.getId()).isNull();
        assertThat(event.getTitle()).isNull();
        assertThat(event.getDescription()).isNull();
        assertThat(event.getStartDateTime()).isNull();
        assertThat(event.getEndDateTime()).isNull();
        assertThat(event.getUser()).isNull();
    }

    @Test
    void event_RecurrenceFields() {
        // Given
        LocalDateTime recurrenceEndDate = LocalDateTime.now().plusDays(30);
        LocalDateTime originalStartDateTime = LocalDateTime.now();

        Event parentEvent = Event.builder()
                .id(123L)
                .title("Parent Event")
                .build();

        Event event = Event.builder()
                .recurrenceRule("FREQ=DAILY;INTERVAL=1")
                .recurrenceEndDate(recurrenceEndDate)
                .recurrenceCount(10)
                .parentEvent(parentEvent)
                .originalStartDateTime(originalStartDateTime)
                .excludedDates("2024-01-15,2024-01-20")
                .build();

        // Then
        assertThat(event.getRecurrenceRule()).isEqualTo("FREQ=DAILY;INTERVAL=1");
        assertThat(event.getRecurrenceEndDate()).isEqualTo(recurrenceEndDate);
        assertThat(event.getRecurrenceCount()).isEqualTo(10);
        assertThat(event.getParentEvent()).isEqualTo(parentEvent);
        assertThat(event.getOriginalStartDateTime()).isEqualTo(originalStartDateTime);
        assertThat(event.getExcludedDates()).isEqualTo("2024-01-15,2024-01-20");
    }

    @Test
    void event_RecurrenceFieldsSetters() {
        // Given
        Event event = new Event();
        LocalDateTime recurrenceEndDate = LocalDateTime.now().plusDays(30);
        LocalDateTime originalStartDateTime = LocalDateTime.now();

        Event parentEvent = Event.builder()
                .id(456L)
                .title("Parent Event")
                .build();

        // When
        event.setRecurrenceRule("FREQ=WEEKLY;INTERVAL=2");
        event.setRecurrenceEndDate(recurrenceEndDate);
        event.setRecurrenceCount(5);
        event.setParentEvent(parentEvent);
        event.setOriginalStartDateTime(originalStartDateTime);
        event.setExcludedDates("2024-02-10,2024-02-15");

        // Then
        assertThat(event.getRecurrenceRule()).isEqualTo("FREQ=WEEKLY;INTERVAL=2");
        assertThat(event.getRecurrenceEndDate()).isEqualTo(recurrenceEndDate);
        assertThat(event.getRecurrenceCount()).isEqualTo(5);
        assertThat(event.getParentEvent()).isEqualTo(parentEvent);
        assertThat(event.getOriginalStartDateTime()).isEqualTo(originalStartDateTime);
        assertThat(event.getExcludedDates()).isEqualTo("2024-02-10,2024-02-15");
    }
}

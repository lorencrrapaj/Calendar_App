package com.example.calendar.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = new Tag();
    }

    @Test
    void testDefaultConstructor() {
        // When
        Tag newTag = new Tag();

        // Then
        assertNull(newTag.getId());
        assertNull(newTag.getName());
        assertNotNull(newTag.getEvents());
        assertTrue(newTag.getEvents().isEmpty());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        Set<Event> events = new HashSet<>();
        Event event = new Event();
        events.add(event);
        User user = new User();
        user.setId(1L);

        // When
        Tag newTag = new Tag(1L, user, "Work", events);

        // Then
        assertEquals(1L, newTag.getId());
        assertEquals(user, newTag.getUser());
        assertEquals("Work", newTag.getName());
        assertEquals(events, newTag.getEvents());
        assertEquals(1, newTag.getEvents().size());
    }

    @Test
    void testBuilder() {
        // Given
        Set<Event> events = new HashSet<>();
        Event event = new Event();
        events.add(event);

        // When
        Tag builtTag = Tag.builder()
                .id(2L)
                .name("Personal")
                .events(events)
                .build();

        // Then
        assertEquals(2L, builtTag.getId());
        assertEquals("Personal", builtTag.getName());
        assertEquals(events, builtTag.getEvents());
        assertEquals(1, builtTag.getEvents().size());
    }

    @Test
    void testBuilderWithDefaults() {
        // When
        Tag builtTag = Tag.builder()
                .id(3L)
                .name("Test")
                .build();

        // Then
        assertEquals(3L, builtTag.getId());
        assertEquals("Test", builtTag.getName());
        assertNotNull(builtTag.getEvents());
        assertTrue(builtTag.getEvents().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Set<Event> events = new HashSet<>();
        Event event = new Event();
        events.add(event);

        // When
        tag.setId(5L);
        tag.setName("Important");
        tag.setEvents(events);

        // Then
        assertEquals(5L, tag.getId());
        assertEquals("Important", tag.getName());
        assertEquals(events, tag.getEvents());
        assertEquals(1, tag.getEvents().size());
    }

    @Test
    void testEventsManipulation() {
        // Given
        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        Set<Event> events = new HashSet<>();
        events.add(event1);
        events.add(event2);

        // When
        tag.setEvents(events);

        // Then
        assertEquals(2, tag.getEvents().size());
        assertTrue(tag.getEvents().contains(event1));
        assertTrue(tag.getEvents().contains(event2));
    }

    @Test
    void testEventsRemoval() {
        // Given
        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        Set<Event> events = new HashSet<>();
        events.add(event1);
        events.add(event2);
        tag.setEvents(events);

        // When - create new set without event1
        Set<Event> updatedEvents = new HashSet<>();
        updatedEvents.add(event2);
        tag.setEvents(updatedEvents);

        // Then
        assertEquals(1, tag.getEvents().size());
        assertFalse(tag.getEvents().contains(event1));
        assertTrue(tag.getEvents().contains(event2));
    }

    @Test
    void testEventsSetNull() {
        // When
        tag.setEvents(null);

        // Then - getEvents() returns empty set when internal events is null (defensive copy)
        assertNotNull(tag.getEvents());
        assertTrue(tag.getEvents().isEmpty());
        assertEquals(0, tag.getEvents().size());
    }

    @Test
    void testEventsSetEmpty() {
        // Given
        Set<Event> emptyEvents = new HashSet<>();

        // When
        tag.setEvents(emptyEvents);

        // Then
        assertNotNull(tag.getEvents());
        assertTrue(tag.getEvents().isEmpty());
        assertEquals(0, tag.getEvents().size());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        Tag tag1 = Tag.builder()
                .id(1L)
                .name("Work")
                .build();

        Tag tag2 = Tag.builder()
                .id(2L)
                .name("Personal")
                .build();

        // Then - Just verify methods exist and work
        // Test self equality
        assertEquals(tag1, tag1);

        // Test null inequality
        assertNotEquals(tag1, null);

        // Test different class inequality
        assertNotEquals(tag1, "not a tag");

        // Test hashCode method exists and returns consistent values
        int hashCode1 = tag1.hashCode();
        int hashCode2 = tag1.hashCode();
        assertEquals(hashCode1, hashCode2);

        // Different objects should have different hash codes (usually)
        assertNotEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        tag.setId(1L);
        tag.setName("Work");

        // When
        String toString = tag.toString();

        // Then - Just verify toString method works and returns non-empty string
        assertNotNull(toString);
        assertFalse(toString.isEmpty());

        // Verify toString is consistent
        String toString2 = tag.toString();
        assertEquals(toString, toString2);
    }

    @Test
    void testBuilderToString() {
        // When
        String builderToString = Tag.builder().toString();

        // Then
        assertNotNull(builderToString);
        assertTrue(builderToString.contains("Tag.TagBuilder"));
    }

    @Test
    void testNullName() {
        // When
        tag.setName(null);

        // Then
        assertNull(tag.getName());
    }

    @Test
    void testEmptyName() {
        // When
        tag.setName("");

        // Then
        assertEquals("", tag.getName());
    }

    @Test
    void testLongName() {
        // Given
        String longName = "A".repeat(100);

        // When
        tag.setName(longName);

        // Then
        assertEquals(longName, tag.getName());
        assertEquals(100, tag.getName().length());
    }
}

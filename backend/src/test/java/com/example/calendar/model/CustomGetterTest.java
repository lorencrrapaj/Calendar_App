package com.example.calendar.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CustomGetterTest {

    @Test
    void event_getTags_WithNullTags_ReturnsEmptySet() {
        Event event = new Event();
        // tags field is null by default
        
        Set<Tag> result = event.getTags();
        
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void event_getTags_WithNonNullTags_ReturnsDefensiveCopy() {
        Event event = new Event();
        Set<Tag> originalTags = new HashSet<>();
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("TestTag");
        tag.setUser(user);
        originalTags.add(tag);
        
        event.setTags(originalTags);
        
        Set<Tag> result = event.getTags();
        
        assertThat(result).isNotSameAs(originalTags);
        assertThat(result).hasSize(1);
        assertThat(result).contains(tag);
        
        // Modifying the returned set should not affect the original
        result.clear();
        assertThat(event.getTags()).hasSize(1);
    }

    @Test
    void tag_getEvents_WithNullEvents_ReturnsEmptySet() {
        Tag tag = new Tag();
        // events field is null by default
        
        Set<Event> result = tag.getEvents();
        
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void tag_getEvents_WithNonNullEvents_ReturnsDefensiveCopy() {
        Tag tag = new Tag();
        Set<Event> originalEvents = new HashSet<>();
        
        Event event = new Event();
        event.setId(1L);
        event.setTitle("TestEvent");
        event.setDescription("Description");
        event.setStartDateTime(LocalDateTime.now());
        event.setEndDateTime(LocalDateTime.now().plusHours(1));
        originalEvents.add(event);
        
        tag.setEvents(originalEvents);
        
        Set<Event> result = tag.getEvents();
        
        assertThat(result).isNotSameAs(originalEvents);
        assertThat(result).hasSize(1);
        assertThat(result).contains(event);
        
        // Modifying the returned set should not affect the original
        result.clear();
        assertThat(tag.getEvents()).hasSize(1);
    }

    @Test
    void createEventDTO_getTagIds_WithNullTagIds_ReturnsEmptyList() {
        com.example.calendar.dto.CreateEventDTO dto = new com.example.calendar.dto.CreateEventDTO();
        // tagIds field is null by default
        
        var result = dto.getTagIds();
        
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void createEventDTO_getTagIds_WithNonNullTagIds_ReturnsDefensiveCopy() {
        com.example.calendar.dto.CreateEventDTO dto = new com.example.calendar.dto.CreateEventDTO();
        java.util.List<Long> originalTagIds = java.util.Arrays.asList(1L, 2L, 3L);
        dto.setTagIds(originalTagIds);
        
        var result = dto.getTagIds();
        
        assertThat(result).isNotSameAs(originalTagIds);
        assertThat(result).containsExactly(1L, 2L, 3L);
        
        // Modifying the returned list should not affect the original
        result.clear();
        assertThat(dto.getTagIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void event_builderDefault_InitializesTagsAsEmptySet() {
        Event event = Event.builder()
                .id(1L)
                .title("Test")
                .description("Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .build();
        
        Set<Tag> tags = event.getTags();
        assertThat(tags).isNotNull();
        assertThat(tags).isEmpty();
    }

    @Test
    void tag_builderDefault_InitializesEventsAsEmptySet() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        
        Tag tag = Tag.builder()
                .id(1L)
                .name("TestTag")
                .user(user)
                .build();
        
        Set<Event> events = tag.getEvents();
        assertThat(events).isNotNull();
        assertThat(events).isEmpty();
    }
}
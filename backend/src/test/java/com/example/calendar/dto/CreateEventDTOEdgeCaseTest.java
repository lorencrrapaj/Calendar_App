package com.example.calendar.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateEventDTOEdgeCaseTest {

    @Test
    void getTagIds_WhenTagIdsIsNull_ReturnsEmptyList() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTagIds(null);

        List<Long> result = dto.getTagIds();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getTagIds_WhenTagIdsIsNotNull_ReturnsDefensiveCopy() {
        CreateEventDTO dto = new CreateEventDTO();
        List<Long> originalList = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        dto.setTagIds(originalList);

        List<Long> result = dto.getTagIds();

        assertThat(result).isNotSameAs(originalList);
        assertThat(result).containsExactly(1L, 2L, 3L);
        
        // Modifying the returned list should not affect the original
        result.add(4L);
        assertThat(dto.getTagIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void getTagIds_WhenTagIdsIsEmpty_ReturnsEmptyDefensiveCopy() {
        CreateEventDTO dto = new CreateEventDTO();
        List<Long> originalList = new ArrayList<>();
        dto.setTagIds(originalList);

        List<Long> result = dto.getTagIds();

        assertThat(result).isNotSameAs(originalList);
        assertThat(result).isEmpty();
    }

    @Test
    void createEventDTO_AllArgsConstructor_SetsAllFields() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        LocalDateTime recurrenceEnd = start.plusDays(30);
        List<Long> tagIds = Arrays.asList(1L, 2L);

        CreateEventDTO dto = new CreateEventDTO(
            "Test Title",
            "Test Description", 
            start,
            end,
            "FREQ=DAILY",
            recurrenceEnd,
            10,
            tagIds
        );

        assertThat(dto.getTitle()).isEqualTo("Test Title");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getStartDateTime()).isEqualTo(start);
        assertThat(dto.getEndDateTime()).isEqualTo(end);
        assertThat(dto.getRecurrenceRule()).isEqualTo("FREQ=DAILY");
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(recurrenceEnd);
        assertThat(dto.getRecurrenceCount()).isEqualTo(10);
        assertThat(dto.getTagIds()).containsExactly(1L, 2L);
    }

    @Test
    void createEventDTO_NoArgsConstructor_InitializesWithNullValues() {
        CreateEventDTO dto = new CreateEventDTO();

        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getStartDateTime()).isNull();
        assertThat(dto.getEndDateTime()).isNull();
        assertThat(dto.getRecurrenceRule()).isNull();
        assertThat(dto.getRecurrenceEndDate()).isNull();
        assertThat(dto.getRecurrenceCount()).isNull();
        assertThat(dto.getTagIds()).isEmpty(); // Custom getter returns empty list for null
    }

    @Test
    void createEventDTO_EqualsAndHashCode_WorksCorrectly() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        List<Long> tagIds = Arrays.asList(1L, 2L);

        CreateEventDTO dto1 = new CreateEventDTO();
        dto1.setTitle("Test");
        dto1.setDescription("Description");
        dto1.setStartDateTime(start);
        dto1.setEndDateTime(end);
        dto1.setTagIds(tagIds);

        CreateEventDTO dto2 = new CreateEventDTO();
        dto2.setTitle("Test");
        dto2.setDescription("Description");
        dto2.setStartDateTime(start);
        dto2.setEndDateTime(end);
        dto2.setTagIds(new ArrayList<>(tagIds));

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void createEventDTO_ToString_ContainsAllFields() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test Title");
        dto.setDescription("Test Description");
        dto.setStartDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        dto.setRecurrenceRule("FREQ=DAILY");
        dto.setRecurrenceCount(5);
        dto.setTagIds(Arrays.asList(1L, 2L));

        String toString = dto.toString();

        assertThat(toString).contains("Test Title");
        assertThat(toString).contains("Test Description");
        assertThat(toString).contains("2024-01-01T10:00");
        assertThat(toString).contains("2024-01-01T11:00");
        assertThat(toString).contains("FREQ=DAILY");
        assertThat(toString).contains("5");
        assertThat(toString).contains("[1, 2]");
    }

    @Test
    void createEventDTO_WithRecurrenceFields_AllFieldsSet() {
        CreateEventDTO dto = new CreateEventDTO();
        LocalDateTime recurrenceEnd = LocalDateTime.now().plusDays(30);
        
        dto.setRecurrenceRule("FREQ=WEEKLY;BYDAY=MO,WE,FR");
        dto.setRecurrenceEndDate(recurrenceEnd);
        dto.setRecurrenceCount(20);

        assertThat(dto.getRecurrenceRule()).isEqualTo("FREQ=WEEKLY;BYDAY=MO,WE,FR");
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(recurrenceEnd);
        assertThat(dto.getRecurrenceCount()).isEqualTo(20);
    }
}
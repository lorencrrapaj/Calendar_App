package com.example.calendar.dto;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateEventDTOTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void getTagIds_WhenTagIdsIsNull_ReturnsEmptyList() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTagIds(null);

        // When
        List<Long> result = dto.getTagIds();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getTagIds_WhenTagIdsIsNotNull_ReturnsDefensiveCopy() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        List<Long> originalTagIds = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        dto.setTagIds(originalTagIds);

        // When
        List<Long> result = dto.getTagIds();

        // Then
        assertThat(result).isNotSameAs(originalTagIds);
        assertThat(result).containsExactly(1L, 2L, 3L);
        
        // Verify it's a defensive copy by modifying the original
        originalTagIds.add(4L);
        assertThat(result).hasSize(3);
    }

    @Test
    void getTagIds_WhenTagIdsIsEmpty_ReturnsEmptyList() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTagIds(new ArrayList<>());

        // When
        List<Long> result = dto.getTagIds();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void validation_WhenTitleIsBlank_ReturnsViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("");
        dto.setDescription("Test description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void validation_WhenTitleIsNull_ReturnsViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle(null);
        dto.setDescription("Test description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void validation_WhenDescriptionIsBlank_ReturnsViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test title");
        dto.setDescription("");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description is required");
    }

    @Test
    void validation_WhenDescriptionIsNull_ReturnsViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test title");
        dto.setDescription(null);
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description is required");
    }

    @Test
    void validation_WhenStartDateTimeIsNull_ReturnsViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test title");
        dto.setDescription("Test description");
        dto.setStartDateTime(null);
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Start date and time is required");
    }

    @Test
    void validation_WhenEndDateTimeIsNull_ReturnsViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test title");
        dto.setDescription("Test description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(null);

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("End date and time is required");
    }

    @Test
    void validation_WhenAllFieldsAreValid_ReturnsNoViolations() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test title");
        dto.setDescription("Test description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void equalsAndHashCode_WorksCorrectly() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        
        CreateEventDTO dto1 = new CreateEventDTO();
        dto1.setTitle("Test");
        dto1.setDescription("Description");
        dto1.setStartDateTime(start);
        dto1.setEndDateTime(end);
        dto1.setTagIds(Arrays.asList(1L, 2L));

        CreateEventDTO dto2 = new CreateEventDTO();
        dto2.setTitle("Test");
        dto2.setDescription("Description");
        dto2.setStartDateTime(start);
        dto2.setEndDateTime(end);
        dto2.setTagIds(Arrays.asList(1L, 2L));

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toString_ContainsAllFields() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test title");
        dto.setDescription("Test description");
        dto.setStartDateTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2023, 1, 1, 11, 0));
        dto.setRecurrenceRule("FREQ=DAILY");
        dto.setTagIds(Arrays.asList(1L, 2L));

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains("Test title");
        assertThat(result).contains("Test description");
        assertThat(result).contains("2023-01-01T10:00");
        assertThat(result).contains("2023-01-01T11:00");
        assertThat(result).contains("FREQ=DAILY");
    }
}
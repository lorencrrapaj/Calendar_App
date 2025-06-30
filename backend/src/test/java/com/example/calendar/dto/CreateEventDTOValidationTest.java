package com.example.calendar.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateEventDTOValidationTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createEventDTO_ValidData_NoViolations() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void createEventDTO_NullTitle_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle(null);
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
        assertThat(violation.getMessage()).isEqualTo("Title is required");
    }

    @Test
    void createEventDTO_EmptyTitle_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
        assertThat(violation.getMessage()).isEqualTo("Title is required");
    }

    @Test
    void createEventDTO_BlankTitle_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("   ");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
        assertThat(violation.getMessage()).isEqualTo("Title is required");
    }

    @Test
    void createEventDTO_NullDescription_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription(null);
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isEqualTo("Description is required");
    }

    @Test
    void createEventDTO_EmptyDescription_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isEqualTo("Description is required");
    }

    @Test
    void createEventDTO_BlankDescription_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("   ");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isEqualTo("Description is required");
    }

    @Test
    void createEventDTO_NullStartDateTime_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(null);
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("startDateTime");
        assertThat(violation.getMessage()).isEqualTo("Start date and time is required");
    }

    @Test
    void createEventDTO_NullEndDateTime_HasViolation() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(null);

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateEventDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("endDateTime");
        assertThat(violation.getMessage()).isEqualTo("End date and time is required");
    }

    @Test
    void createEventDTO_MultipleViolations() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle(null);
        dto.setDescription("");
        dto.setStartDateTime(null);
        dto.setEndDateTime(null);

        // When
        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(4);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("title", "description", "startDateTime", "endDateTime");
    }

    @Test
    void createEventDTO_GettersAndSetters() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        String title = "Test Title";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);

        // When
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setStartDateTime(startDateTime);
        dto.setEndDateTime(endDateTime);

        // Then
        assertThat(dto.getTitle()).isEqualTo(title);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dto.getEndDateTime()).isEqualTo(endDateTime);
    }

    @Test
    void createEventDTO_EqualsAndHashCode() {
        // Given
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);

        CreateEventDTO dto1 = new CreateEventDTO();
        dto1.setTitle("Test Title");
        dto1.setDescription("Test Description");
        dto1.setStartDateTime(startDateTime);
        dto1.setEndDateTime(endDateTime);

        CreateEventDTO dto2 = new CreateEventDTO();
        dto2.setTitle("Test Title");
        dto2.setDescription("Test Description");
        dto2.setStartDateTime(startDateTime);
        dto2.setEndDateTime(endDateTime);

        CreateEventDTO dto3 = new CreateEventDTO();
        dto3.setTitle("Different Title");
        dto3.setDescription("Different Description");
        dto3.setStartDateTime(startDateTime);
        dto3.setEndDateTime(endDateTime);

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void createEventDTO_ToString() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test Title");
        dto.setDescription("Test Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("CreateEventDTO");
        assertThat(toString).contains("title=Test Title");
        assertThat(toString).contains("description=Test Description");
    }

    @Test
    void createEventDTO_JsonSerialization() throws Exception {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test Event");
        dto.setDescription("Test Description");
        dto.setStartDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        dto.setRecurrenceRule("DAILY");
        dto.setRecurrenceEndDate(LocalDateTime.of(2024, 1, 31, 23, 59));
        dto.setRecurrenceCount(30);

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertThat(json).contains("\"title\":\"Test Event\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
        assertThat(json).contains("\"recurrenceRule\":\"DAILY\"");
        assertThat(json).contains("\"recurrenceCount\":30");
    }

    @Test
    void createEventDTO_JsonDeserialization() throws Exception {
        // Given
        String json = "{\"title\":\"Test Event\",\"description\":\"Test Description\"," +
                "\"startDateTime\":\"2024-01-01T10:00:00\",\"endDateTime\":\"2024-01-01T11:00:00\"," +
                "\"recurrenceRule\":\"DAILY\",\"recurrenceEndDate\":\"2024-01-31T23:59:00\"," +
                "\"recurrenceCount\":30}";

        // When
        CreateEventDTO dto = objectMapper.readValue(json, CreateEventDTO.class);

        // Then
        assertThat(dto.getTitle()).isEqualTo("Test Event");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getStartDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getEndDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 11, 0));
        assertThat(dto.getRecurrenceRule()).isEqualTo("DAILY");
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(LocalDateTime.of(2024, 1, 31, 23, 59));
        assertThat(dto.getRecurrenceCount()).isEqualTo(30);
    }

    @Test
    void createEventDTO_JsonRoundTrip() throws Exception {
        // Given
        CreateEventDTO original = new CreateEventDTO();
        original.setTitle("Test Event");
        original.setDescription("Test Description");
        original.setStartDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        original.setEndDateTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        original.setRecurrenceRule("WEEKLY");
        original.setRecurrenceEndDate(LocalDateTime.of(2024, 12, 31, 23, 59));
        original.setRecurrenceCount(52);

        // When
        String json = objectMapper.writeValueAsString(original);
        CreateEventDTO deserialized = objectMapper.readValue(json, CreateEventDTO.class);

        // Then
        assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
        assertThat(deserialized.getDescription()).isEqualTo(original.getDescription());
        assertThat(deserialized.getStartDateTime()).isEqualTo(original.getStartDateTime());
        assertThat(deserialized.getEndDateTime()).isEqualTo(original.getEndDateTime());
        assertThat(deserialized.getRecurrenceRule()).isEqualTo(original.getRecurrenceRule());
        assertThat(deserialized.getRecurrenceEndDate()).isEqualTo(original.getRecurrenceEndDate());
        assertThat(deserialized.getRecurrenceCount()).isEqualTo(original.getRecurrenceCount());
    }

    @Test
    void createEventDTO_JsonWithNullOptionalFields() throws Exception {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test Event");
        dto.setDescription("Test Description");
        dto.setStartDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        dto.setRecurrenceRule(null);
        dto.setRecurrenceEndDate(null);
        dto.setRecurrenceCount(null);

        // When
        String json = objectMapper.writeValueAsString(dto);
        CreateEventDTO deserialized = objectMapper.readValue(json, CreateEventDTO.class);

        // Then
        assertThat(deserialized.getTitle()).isEqualTo("Test Event");
        assertThat(deserialized.getDescription()).isEqualTo("Test Description");
        assertThat(deserialized.getStartDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(deserialized.getEndDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 11, 0));
        assertThat(deserialized.getRecurrenceRule()).isNull();
        assertThat(deserialized.getRecurrenceEndDate()).isNull();
        assertThat(deserialized.getRecurrenceCount()).isNull();
    }

    @Test
    void createEventDTO_JsonWithMinimalData() throws Exception {
        // Given
        String json = "{\"title\":\"Minimal Event\",\"description\":\"Minimal Description\"," +
                "\"startDateTime\":\"2024-01-01T10:00:00\",\"endDateTime\":\"2024-01-01T11:00:00\"}";

        // When
        CreateEventDTO dto = objectMapper.readValue(json, CreateEventDTO.class);

        // Then
        assertThat(dto.getTitle()).isEqualTo("Minimal Event");
        assertThat(dto.getDescription()).isEqualTo("Minimal Description");
        assertThat(dto.getStartDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getEndDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 11, 0));
        assertThat(dto.getRecurrenceRule()).isNull();
        assertThat(dto.getRecurrenceEndDate()).isNull();
        assertThat(dto.getRecurrenceCount()).isNull();
    }

    @Test
    void createEventDTO_JsonWithSpecialCharacters() throws Exception {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Event with \"quotes\" & special chars");
        dto.setDescription("Description with\nnewlines and\ttabs");
        dto.setStartDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2024, 1, 1, 11, 0));

        // When
        String json = objectMapper.writeValueAsString(dto);
        CreateEventDTO deserialized = objectMapper.readValue(json, CreateEventDTO.class);

        // Then
        assertThat(deserialized.getTitle()).isEqualTo("Event with \"quotes\" & special chars");
        assertThat(deserialized.getDescription()).isEqualTo("Description with\nnewlines and\ttabs");
    }
}

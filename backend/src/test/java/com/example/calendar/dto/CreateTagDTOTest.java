package com.example.calendar.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class CreateTagDTOTest {

    private Validator validator;
    private CreateTagDTO createTagDTO;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        createTagDTO = new CreateTagDTO();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testValidTagName() {
        // Given
        createTagDTO.setName("Work");

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("Work", createTagDTO.getName());
    }

    @Test
    void testBlankTagName_ReturnsValidationError() {
        // Given
        createTagDTO.setName("");

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTagDTO> violation = violations.iterator().next();
        assertEquals("Tag name is required", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testNullTagName_ReturnsValidationError() {
        // Given
        createTagDTO.setName(null);

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTagDTO> violation = violations.iterator().next();
        assertEquals("Tag name is required", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testWhitespaceOnlyTagName_ReturnsValidationError() {
        // Given
        createTagDTO.setName("   ");

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTagDTO> violation = violations.iterator().next();
        assertEquals("Tag name is required", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testTagNameTooLong_ReturnsValidationError() {
        // Given
        String longName = "A".repeat(51); // 51 characters, exceeds max of 50
        createTagDTO.setName(longName);

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<CreateTagDTO> violation = violations.iterator().next();
        assertEquals("Tag name must not exceed 50 characters", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testTagNameExactlyMaxLength_IsValid() {
        // Given
        String maxLengthName = "A".repeat(50); // Exactly 50 characters
        createTagDTO.setName(maxLengthName);

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals(maxLengthName, createTagDTO.getName());
        assertEquals(50, createTagDTO.getName().length());
    }

    @Test
    void testTagNameWithSpecialCharacters_IsValid() {
        // Given
        createTagDTO.setName("Work-2024!");

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("Work-2024!", createTagDTO.getName());
    }

    @Test
    void testTagNameWithNumbers_IsValid() {
        // Given
        createTagDTO.setName("Project123");

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("Project123", createTagDTO.getName());
    }

    @Test
    void testTagNameWithSpaces_IsValid() {
        // Given
        createTagDTO.setName("Work Project");

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("Work Project", createTagDTO.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        CreateTagDTO dto1 = new CreateTagDTO();
        dto1.setName("Work");

        CreateTagDTO dto2 = new CreateTagDTO();
        dto2.setName("Work");

        CreateTagDTO dto3 = new CreateTagDTO();
        dto3.setName("Personal");

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        createTagDTO.setName("Work");

        // When
        String toString = createTagDTO.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Work"));
        assertTrue(toString.contains("CreateTagDTO"));
    }

    @Test
    void testDefaultConstructor() {
        CreateTagDTO dto = new CreateTagDTO();

        assertThat(dto.getName()).isNull();
    }

    @Test
    void testJsonSerialization() throws Exception {
        // Given
        createTagDTO.setName("Work");

        // When
        String json = objectMapper.writeValueAsString(createTagDTO);

        // Then
        assertThat(json).contains("\"name\":\"Work\"");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"name\":\"Work\"}";

        // When
        CreateTagDTO dto = objectMapper.readValue(json, CreateTagDTO.class);

        // Then
        assertThat(dto.getName()).isEqualTo("Work");
    }

    @Test
    void testJsonRoundTrip() throws Exception {
        // Given
        CreateTagDTO original = new CreateTagDTO();
        original.setName("Work");

        // When
        String json = objectMapper.writeValueAsString(original);
        CreateTagDTO deserialized = objectMapper.readValue(json, CreateTagDTO.class);

        // Then
        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getName()).isEqualTo(original.getName());
    }

    @Test
    void testJsonWithNullName() throws Exception {
        // Given
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName(null);

        // When
        String json = objectMapper.writeValueAsString(dto);
        CreateTagDTO deserialized = objectMapper.readValue(json, CreateTagDTO.class);

        // Then
        assertThat(deserialized.getName()).isNull();
    }

    @Test
    void testEqualsWithSameObject() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("Work");

        assertThat(dto.equals(dto)).isTrue();
    }

    @Test
    void testEqualsWithNull() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("Work");

        assertThat(dto.equals(null)).isFalse();
    }

    @Test
    void testEqualsWithDifferentClass() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("Work");

        assertThat(dto.equals("not a CreateTagDTO")).isFalse();
    }

    @Test
    void testEqualsWithNullFields() {
        CreateTagDTO dto1 = new CreateTagDTO();
        dto1.setName(null);

        CreateTagDTO dto2 = new CreateTagDTO();
        dto2.setName(null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void testHashCodeConsistency() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("Work");

        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void testToStringWithNullName() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName(null);

        String toString = dto.toString();

        assertThat(toString).isNotNull();
        assertThat(toString).contains("CreateTagDTO");
    }

    @Test
    void testTagNameWithUnicodeCharacters() {
        // Given
        createTagDTO.setName("工作");

        // When
        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(createTagDTO);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("工作", createTagDTO.getName());
    }
}

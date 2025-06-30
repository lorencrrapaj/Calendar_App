package com.example.calendar.validation;

import com.example.calendar.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createEventDTO_ValidData_NoViolations() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void createEventDTO_BlankTitle_HasViolation() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void createEventDTO_NullTitle_HasViolation() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle(null);
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void createEventDTO_BlankDescription_HasViolation() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description is required");
    }

    @Test
    void createEventDTO_NullDescription_HasViolation() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription(null);
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Description is required");
    }

    @Test
    void createEventDTO_NullStartDateTime_HasViolation() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(null);
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Start date and time is required");
    }

    @Test
    void createEventDTO_NullEndDateTime_HasViolation() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(null);

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("End date and time is required");
    }

    @Test
    void createEventDTO_MultipleViolations_HasAllViolations() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("");
        dto.setDescription("");
        dto.setStartDateTime(null);
        dto.setEndDateTime(null);

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(4);
    }

    @Test
    void registerDTO_ValidData_NoViolations() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("validPassword123");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void loginDTO_ValidData_NoViolations() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("validPassword123");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void changePasswordDTO_ValidData_NoViolations() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword123");

        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void createTagDTO_ValidData_NoViolations() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("Valid Tag Name");

        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void createTagDTO_BlankName_HasViolation() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("");

        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Tag name is required");
    }

    @Test
    void createTagDTO_NullName_HasViolation() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName(null);

        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Tag name is required");
    }

    @Test
    void createTagDTO_NameTooLong_HasViolation() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("This is a very long tag name that exceeds the maximum allowed length of 50 characters");

        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Tag name must not exceed 50 characters");
    }

    @Test
    void createTagDTO_NameExactly50Characters_NoViolations() {
        CreateTagDTO dto = new CreateTagDTO();
        dto.setName("12345678901234567890123456789012345678901234567890"); // exactly 50 chars

        Set<ConstraintViolation<CreateTagDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}

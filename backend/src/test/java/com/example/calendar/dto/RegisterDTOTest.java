package com.example.calendar.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterDTOTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerDTO_ValidData_NoViolations() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerDTO_GettersAndSetters() {
        RegisterDTO dto = new RegisterDTO();
        String email = "test@example.com";
        String password = "password123";

        dto.setEmail(email);
        dto.setPassword(password);

        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getPassword()).isEqualTo(password);
    }

    @Test
    void registerDTO_EqualsAndHashCode() {
        RegisterDTO dto1 = new RegisterDTO();
        dto1.setEmail("test@example.com");
        dto1.setPassword("password123");

        RegisterDTO dto2 = new RegisterDTO();
        dto2.setEmail("test@example.com");
        dto2.setPassword("password123");

        RegisterDTO dto3 = new RegisterDTO();
        dto3.setEmail("different@example.com");
        dto3.setPassword("password123");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void registerDTO_ToString() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        String toString = dto.toString();

        assertThat(toString).contains("RegisterDTO");
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("password123");
    }

    @Test
    void registerDTO_NullValues() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void registerDTO_EmptyValues() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("");
        dto.setPassword("");

        assertThat(dto.getEmail()).isEmpty();
        assertThat(dto.getPassword()).isEmpty();
    }

    @Test
    void registerDTO_JsonSerialization() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"email\":\"test@example.com\"");
        assertThat(json).contains("\"password\":\"password123\"");
    }

    @Test
    void registerDTO_JsonDeserialization() throws Exception {
        String json = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";

        RegisterDTO dto = objectMapper.readValue(json, RegisterDTO.class);

        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getPassword()).isEqualTo("password123");
    }

    @Test
    void registerDTO_JsonRoundTrip() throws Exception {
        RegisterDTO original = new RegisterDTO();
        original.setEmail("test@example.com");
        original.setPassword("password123");

        String json = objectMapper.writeValueAsString(original);
        RegisterDTO deserialized = objectMapper.readValue(json, RegisterDTO.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getEmail()).isEqualTo(original.getEmail());
        assertThat(deserialized.getPassword()).isEqualTo(original.getPassword());
    }

    @Test
    void registerDTO_JsonWithNullValues() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        String json = objectMapper.writeValueAsString(dto);
        RegisterDTO deserialized = objectMapper.readValue(json, RegisterDTO.class);

        assertThat(deserialized.getEmail()).isNull();
        assertThat(deserialized.getPassword()).isNull();
    }

    @Test
    void registerDTO_JsonWithSpecialCharacters() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test+special@example.com");
        dto.setPassword("pass@word#123");

        String json = objectMapper.writeValueAsString(dto);
        RegisterDTO deserialized = objectMapper.readValue(json, RegisterDTO.class);

        assertThat(deserialized.getEmail()).isEqualTo("test+special@example.com");
        assertThat(deserialized.getPassword()).isEqualTo("pass@word#123");
    }

    @Test
    void registerDTO_DefaultConstructor() {
        RegisterDTO dto = new RegisterDTO();

        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void registerDTO_EqualsWithSameObject() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        assertThat(dto.equals(dto)).isTrue();
    }

    @Test
    void registerDTO_EqualsWithNull() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        assertThat(dto.equals(null)).isFalse();
    }

    @Test
    void registerDTO_EqualsWithDifferentClass() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        assertThat(dto.equals("not a RegisterDTO")).isFalse();
    }

    @Test
    void registerDTO_EqualsWithNullFields() {
        RegisterDTO dto1 = new RegisterDTO();
        dto1.setEmail(null);
        dto1.setPassword(null);

        RegisterDTO dto2 = new RegisterDTO();
        dto2.setEmail(null);
        dto2.setPassword(null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void registerDTO_EqualsWithMixedNullFields() {
        RegisterDTO dto1 = new RegisterDTO();
        dto1.setEmail("test@example.com");
        dto1.setPassword(null);

        RegisterDTO dto2 = new RegisterDTO();
        dto2.setEmail(null);
        dto2.setPassword("password123");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void registerDTO_HashCodeConsistency() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void registerDTO_ToStringWithNullFields() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        String toString = dto.toString();

        assertThat(toString).isNotNull();
        assertThat(toString).contains("RegisterDTO");
    }

    @Test
    void registerDTO_JsonWithLongValues() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        String longEmail = "very.long.email.address.that.might.cause.issues@example.com";
        String longPassword = "this.is.a.very.long.password.that.might.cause.serialization.issues";

        dto.setEmail(longEmail);
        dto.setPassword(longPassword);

        String json = objectMapper.writeValueAsString(dto);
        RegisterDTO deserialized = objectMapper.readValue(json, RegisterDTO.class);

        assertThat(deserialized.getEmail()).isEqualTo(longEmail);
        assertThat(deserialized.getPassword()).isEqualTo(longPassword);
    }

    @Test
    void registerDTO_JsonWithMalformedEmail() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("not-an-email");
        dto.setPassword("password123");

        String json = objectMapper.writeValueAsString(dto);
        RegisterDTO deserialized = objectMapper.readValue(json, RegisterDTO.class);

        assertThat(deserialized.getEmail()).isEqualTo("not-an-email");
        assertThat(deserialized.getPassword()).isEqualTo("password123");
    }
}

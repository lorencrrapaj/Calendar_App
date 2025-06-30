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

class LoginDTOTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginDTO_ValidData_NoViolations() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void loginDTO_GettersAndSetters() {
        LoginDTO dto = new LoginDTO();
        String email = "test@example.com";
        String password = "password123";

        dto.setEmail(email);
        dto.setPassword(password);

        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getPassword()).isEqualTo(password);
    }

    @Test
    void loginDTO_EqualsAndHashCode() {
        LoginDTO dto1 = new LoginDTO();
        dto1.setEmail("test@example.com");
        dto1.setPassword("password123");

        LoginDTO dto2 = new LoginDTO();
        dto2.setEmail("test@example.com");
        dto2.setPassword("password123");

        LoginDTO dto3 = new LoginDTO();
        dto3.setEmail("different@example.com");
        dto3.setPassword("password123");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void loginDTO_ToString() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        String toString = dto.toString();

        assertThat(toString).contains("LoginDTO");
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("password123");
    }

    @Test
    void loginDTO_NullValues() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void loginDTO_EmptyValues() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("");
        dto.setPassword("");

        assertThat(dto.getEmail()).isEmpty();
        assertThat(dto.getPassword()).isEmpty();
    }

    @Test
    void loginDTO_JsonSerialization() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"email\":\"test@example.com\"");
        assertThat(json).contains("\"password\":\"password123\"");
    }

    @Test
    void loginDTO_JsonDeserialization() throws Exception {
        String json = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";

        LoginDTO dto = objectMapper.readValue(json, LoginDTO.class);

        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getPassword()).isEqualTo("password123");
    }

    @Test
    void loginDTO_JsonRoundTrip() throws Exception {
        LoginDTO original = new LoginDTO();
        original.setEmail("test@example.com");
        original.setPassword("password123");

        String json = objectMapper.writeValueAsString(original);
        LoginDTO deserialized = objectMapper.readValue(json, LoginDTO.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getEmail()).isEqualTo(original.getEmail());
        assertThat(deserialized.getPassword()).isEqualTo(original.getPassword());
    }

    @Test
    void loginDTO_JsonWithNullValues() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        String json = objectMapper.writeValueAsString(dto);
        LoginDTO deserialized = objectMapper.readValue(json, LoginDTO.class);

        assertThat(deserialized.getEmail()).isNull();
        assertThat(deserialized.getPassword()).isNull();
    }

    @Test
    void loginDTO_JsonWithSpecialCharacters() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test+special@example.com");
        dto.setPassword("pass@word#123");

        String json = objectMapper.writeValueAsString(dto);
        LoginDTO deserialized = objectMapper.readValue(json, LoginDTO.class);

        assertThat(deserialized.getEmail()).isEqualTo("test+special@example.com");
        assertThat(deserialized.getPassword()).isEqualTo("pass@word#123");
    }

    @Test
    void loginDTO_JsonWithMalformedEmail() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("not-an-email");
        dto.setPassword("password123");

        String json = objectMapper.writeValueAsString(dto);
        LoginDTO deserialized = objectMapper.readValue(json, LoginDTO.class);

        assertThat(deserialized.getEmail()).isEqualTo("not-an-email");
        assertThat(deserialized.getPassword()).isEqualTo("password123");
    }

    @Test
    void loginDTO_JsonWithLongValues() throws Exception {
        LoginDTO dto = new LoginDTO();
        String longEmail = "very.long.email.address.that.might.cause.issues@example.com";
        String longPassword = "this.is.a.very.long.password.that.might.cause.serialization.issues";

        dto.setEmail(longEmail);
        dto.setPassword(longPassword);

        String json = objectMapper.writeValueAsString(dto);
        LoginDTO deserialized = objectMapper.readValue(json, LoginDTO.class);

        assertThat(deserialized.getEmail()).isEqualTo(longEmail);
        assertThat(deserialized.getPassword()).isEqualTo(longPassword);
    }

    @Test
    void loginDTO_DefaultConstructor() {
        LoginDTO dto = new LoginDTO();

        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void loginDTO_EqualsWithSameObject() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        assertThat(dto.equals(dto)).isTrue();
    }

    @Test
    void loginDTO_EqualsWithNull() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        assertThat(dto.equals(null)).isFalse();
    }

    @Test
    void loginDTO_EqualsWithDifferentClass() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        assertThat(dto.equals("not a LoginDTO")).isFalse();
    }

    @Test
    void loginDTO_EqualsWithNullFields() {
        LoginDTO dto1 = new LoginDTO();
        dto1.setEmail(null);
        dto1.setPassword(null);

        LoginDTO dto2 = new LoginDTO();
        dto2.setEmail(null);
        dto2.setPassword(null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void loginDTO_EqualsWithMixedNullFields() {
        LoginDTO dto1 = new LoginDTO();
        dto1.setEmail("test@example.com");
        dto1.setPassword(null);

        LoginDTO dto2 = new LoginDTO();
        dto2.setEmail(null);
        dto2.setPassword("password123");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void loginDTO_HashCodeConsistency() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void loginDTO_ToStringWithNullFields() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        String toString = dto.toString();

        assertThat(toString).isNotNull();
        assertThat(toString).contains("LoginDTO");
    }
}

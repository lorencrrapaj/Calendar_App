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

class LoginResponseTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginResponse_ValidData_NoViolations() {
        LoginResponse dto = new LoginResponse("valid-jwt-token-123");

        Set<ConstraintViolation<LoginResponse>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void loginResponse_AllArgsConstructor() {
        String token = "jwt-token-12345";

        LoginResponse dto = new LoginResponse(token);

        assertThat(dto.getToken()).isEqualTo(token);
    }

    @Test
    void loginResponse_GettersAndSetters() {
        LoginResponse dto = new LoginResponse("initial-token");
        String newToken = "new-jwt-token-67890";

        dto.setToken(newToken);

        assertThat(dto.getToken()).isEqualTo(newToken);
    }

    @Test
    void loginResponse_EqualsAndHashCode() {
        LoginResponse dto1 = new LoginResponse("same-token-123");
        LoginResponse dto2 = new LoginResponse("same-token-123");
        LoginResponse dto3 = new LoginResponse("different-token-456");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void loginResponse_ToString() {
        LoginResponse dto = new LoginResponse("test-token-789");

        String toString = dto.toString();

        assertThat(toString).contains("LoginResponse");
        assertThat(toString).contains("test-token-789");
    }

    @Test
    void loginResponse_NullToken() {
        LoginResponse dto = new LoginResponse(null);

        assertThat(dto.getToken()).isNull();
    }

    @Test
    void loginResponse_EmptyToken() {
        LoginResponse dto = new LoginResponse("");

        assertThat(dto.getToken()).isEmpty();
    }

    @Test
    void loginResponse_JsonSerialization() throws Exception {
        LoginResponse dto = new LoginResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\"");
    }

    @Test
    void loginResponse_JsonDeserialization() throws Exception {
        String json = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\"}";

        LoginResponse dto = objectMapper.readValue(json, LoginResponse.class);

        assertThat(dto.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
    }

    @Test
    void loginResponse_JsonRoundTrip() throws Exception {
        LoginResponse original = new LoginResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");

        String json = objectMapper.writeValueAsString(original);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getToken()).isEqualTo(original.getToken());
    }

    @Test
    void loginResponse_JsonWithNullToken() throws Exception {
        LoginResponse dto = new LoginResponse(null);

        String json = objectMapper.writeValueAsString(dto);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        assertThat(deserialized.getToken()).isNull();
    }

    @Test
    void loginResponse_JsonWithSpecialCharacters() throws Exception {
        LoginResponse dto = new LoginResponse("token-with-special-chars!@#$%^&*()");

        String json = objectMapper.writeValueAsString(dto);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        assertThat(deserialized.getToken()).isEqualTo("token-with-special-chars!@#$%^&*()");
    }

    @Test
    void loginResponse_JsonWithLongToken() throws Exception {
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyNDI2MjIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlVTRVIiLCJBRE1JTiJdLCJwZXJtaXNzaW9ucyI6WyJSRUFEIiwiV1JJVEUiLCJERUxFVEUiXX0.very-long-signature-that-might-cause-serialization-issues";
        LoginResponse dto = new LoginResponse(longToken);

        String json = objectMapper.writeValueAsString(dto);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        assertThat(deserialized.getToken()).isEqualTo(longToken);
    }

    @Test
    void loginResponse_JsonWithUnicodeCharacters() throws Exception {
        LoginResponse dto = new LoginResponse("токен-с-юникодом-символами-密码");

        String json = objectMapper.writeValueAsString(dto);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        assertThat(deserialized.getToken()).isEqualTo("токен-с-юникодом-символами-密码");
    }

    @Test
    void loginResponse_JsonWithEmptyToken() throws Exception {
        LoginResponse dto = new LoginResponse("");

        String json = objectMapper.writeValueAsString(dto);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        assertThat(deserialized.getToken()).isEmpty();
    }

    @Test
    void loginResponse_DefaultConstructor() {
        LoginResponse dto = new LoginResponse();

        assertThat(dto.getToken()).isNull();
    }

    @Test
    void loginResponse_EqualsWithSameObject() {
        LoginResponse dto = new LoginResponse("test-token");

        assertThat(dto.equals(dto)).isTrue();
    }

    @Test
    void loginResponse_EqualsWithNull() {
        LoginResponse dto = new LoginResponse("test-token");

        assertThat(dto.equals(null)).isFalse();
    }

    @Test
    void loginResponse_EqualsWithDifferentClass() {
        LoginResponse dto = new LoginResponse("test-token");

        assertThat(dto.equals("not a LoginResponse")).isFalse();
    }

    @Test
    void loginResponse_EqualsWithNullFields() {
        LoginResponse dto1 = new LoginResponse(null);
        LoginResponse dto2 = new LoginResponse(null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void loginResponse_EqualsWithMixedNullFields() {
        LoginResponse dto1 = new LoginResponse("test-token");
        LoginResponse dto2 = new LoginResponse(null);

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void loginResponse_HashCodeConsistency() {
        LoginResponse dto = new LoginResponse("test-token");

        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void loginResponse_ToStringWithNullToken() {
        LoginResponse dto = new LoginResponse(null);

        String toString = dto.toString();

        assertThat(toString).isNotNull();
        assertThat(toString).contains("LoginResponse");
    }

    @Test
    void loginResponse_SetterWithNullToken() {
        LoginResponse dto = new LoginResponse("initial-token");
        dto.setToken(null);

        assertThat(dto.getToken()).isNull();
    }

    @Test
    void loginResponse_SetterWithEmptyToken() {
        LoginResponse dto = new LoginResponse("initial-token");
        dto.setToken("");

        assertThat(dto.getToken()).isEmpty();
    }
}

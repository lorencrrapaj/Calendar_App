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

class ChangePasswordDTOTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void changePasswordDTO_ValidData_NoViolations() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword456");

        Set<ConstraintViolation<ChangePasswordDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void changePasswordDTO_GettersAndSetters() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword456";

        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);

        assertThat(dto.getOldPassword()).isEqualTo(oldPassword);
        assertThat(dto.getNewPassword()).isEqualTo(newPassword);
    }

    @Test
    void changePasswordDTO_EqualsAndHashCode() {
        ChangePasswordDTO dto1 = new ChangePasswordDTO();
        dto1.setOldPassword("oldPassword123");
        dto1.setNewPassword("newPassword456");

        ChangePasswordDTO dto2 = new ChangePasswordDTO();
        dto2.setOldPassword("oldPassword123");
        dto2.setNewPassword("newPassword456");

        ChangePasswordDTO dto3 = new ChangePasswordDTO();
        dto3.setOldPassword("differentOldPassword");
        dto3.setNewPassword("newPassword456");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void changePasswordDTO_ToString() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword456");

        String toString = dto.toString();

        assertThat(toString).contains("ChangePasswordDTO");
        assertThat(toString).contains("oldPassword123");
        assertThat(toString).contains("newPassword456");
    }

    @Test
    void changePasswordDTO_NullValues() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(null);
        dto.setNewPassword(null);

        assertThat(dto.getOldPassword()).isNull();
        assertThat(dto.getNewPassword()).isNull();
    }

    @Test
    void changePasswordDTO_EmptyValues() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("");
        dto.setNewPassword("");

        assertThat(dto.getOldPassword()).isEmpty();
        assertThat(dto.getNewPassword()).isEmpty();
    }

    @Test
    void changePasswordDTO_JsonSerialization() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword456");

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"oldPassword\":\"oldPassword123\"");
        assertThat(json).contains("\"newPassword\":\"newPassword456\"");
    }

    @Test
    void changePasswordDTO_JsonDeserialization() throws Exception {
        String json = "{\"oldPassword\":\"oldPassword123\",\"newPassword\":\"newPassword456\"}";

        ChangePasswordDTO dto = objectMapper.readValue(json, ChangePasswordDTO.class);

        assertThat(dto.getOldPassword()).isEqualTo("oldPassword123");
        assertThat(dto.getNewPassword()).isEqualTo("newPassword456");
    }

    @Test
    void changePasswordDTO_JsonRoundTrip() throws Exception {
        ChangePasswordDTO original = new ChangePasswordDTO();
        original.setOldPassword("oldPassword123");
        original.setNewPassword("newPassword456");

        String json = objectMapper.writeValueAsString(original);
        ChangePasswordDTO deserialized = objectMapper.readValue(json, ChangePasswordDTO.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getOldPassword()).isEqualTo(original.getOldPassword());
        assertThat(deserialized.getNewPassword()).isEqualTo(original.getNewPassword());
    }

    @Test
    void changePasswordDTO_JsonWithNullValues() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(null);
        dto.setNewPassword(null);

        String json = objectMapper.writeValueAsString(dto);
        ChangePasswordDTO deserialized = objectMapper.readValue(json, ChangePasswordDTO.class);

        assertThat(deserialized.getOldPassword()).isNull();
        assertThat(deserialized.getNewPassword()).isNull();
    }

    @Test
    void changePasswordDTO_JsonWithSpecialCharacters() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("old@pass#word$123");
        dto.setNewPassword("new&pass*word%456");

        String json = objectMapper.writeValueAsString(dto);
        ChangePasswordDTO deserialized = objectMapper.readValue(json, ChangePasswordDTO.class);

        assertThat(deserialized.getOldPassword()).isEqualTo("old@pass#word$123");
        assertThat(deserialized.getNewPassword()).isEqualTo("new&pass*word%456");
    }

    @Test
    void changePasswordDTO_JsonWithSamePasswords() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("samePassword123");
        dto.setNewPassword("samePassword123");

        String json = objectMapper.writeValueAsString(dto);
        ChangePasswordDTO deserialized = objectMapper.readValue(json, ChangePasswordDTO.class);

        assertThat(deserialized.getOldPassword()).isEqualTo("samePassword123");
        assertThat(deserialized.getNewPassword()).isEqualTo("samePassword123");
        assertThat(deserialized.getOldPassword()).isEqualTo(deserialized.getNewPassword());
    }

    @Test
    void changePasswordDTO_JsonWithLongPasswords() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        String longOldPassword = "this.is.a.very.long.old.password.that.might.cause.serialization.issues.123";
        String longNewPassword = "this.is.a.very.long.new.password.that.might.cause.serialization.issues.456";

        dto.setOldPassword(longOldPassword);
        dto.setNewPassword(longNewPassword);

        String json = objectMapper.writeValueAsString(dto);
        ChangePasswordDTO deserialized = objectMapper.readValue(json, ChangePasswordDTO.class);

        assertThat(deserialized.getOldPassword()).isEqualTo(longOldPassword);
        assertThat(deserialized.getNewPassword()).isEqualTo(longNewPassword);
    }

    @Test
    void changePasswordDTO_JsonWithUnicodeCharacters() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("пароль123");
        dto.setNewPassword("密码456");

        String json = objectMapper.writeValueAsString(dto);
        ChangePasswordDTO deserialized = objectMapper.readValue(json, ChangePasswordDTO.class);

        assertThat(deserialized.getOldPassword()).isEqualTo("пароль123");
        assertThat(deserialized.getNewPassword()).isEqualTo("密码456");
    }

    @Test
    void changePasswordDTO_DefaultConstructor() {
        ChangePasswordDTO dto = new ChangePasswordDTO();

        assertThat(dto.getOldPassword()).isNull();
        assertThat(dto.getNewPassword()).isNull();
    }

    @Test
    void changePasswordDTO_EqualsWithSameObject() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword456");

        assertThat(dto.equals(dto)).isTrue();
    }

    @Test
    void changePasswordDTO_EqualsWithNull() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword456");

        assertThat(dto.equals(null)).isFalse();
    }

    @Test
    void changePasswordDTO_EqualsWithDifferentClass() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword456");

        assertThat(dto.equals("not a ChangePasswordDTO")).isFalse();
    }

    @Test
    void changePasswordDTO_EqualsWithNullFields() {
        ChangePasswordDTO dto1 = new ChangePasswordDTO();
        dto1.setOldPassword(null);
        dto1.setNewPassword(null);

        ChangePasswordDTO dto2 = new ChangePasswordDTO();
        dto2.setOldPassword(null);
        dto2.setNewPassword(null);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void changePasswordDTO_EqualsWithMixedNullFields() {
        ChangePasswordDTO dto1 = new ChangePasswordDTO();
        dto1.setOldPassword("oldPassword123");
        dto1.setNewPassword(null);

        ChangePasswordDTO dto2 = new ChangePasswordDTO();
        dto2.setOldPassword(null);
        dto2.setNewPassword("newPassword456");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void changePasswordDTO_HashCodeConsistency() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword123");
        dto.setNewPassword("newPassword456");

        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void changePasswordDTO_ToStringWithNullFields() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(null);
        dto.setNewPassword(null);

        String toString = dto.toString();

        assertThat(toString).isNotNull();
        assertThat(toString).contains("ChangePasswordDTO");
    }
}

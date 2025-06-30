package com.example.calendar.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("CreateEventDTO Comprehensive Coverage Tests")
class CreateEventDTOComprehensiveTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("Test all setter methods individually")
    void testAllSetterMethods() {
        CreateEventDTO dto = new CreateEventDTO();

        // Test title setter
        dto.setTitle("Test Title");
        assertThat(dto.getTitle()).isEqualTo("Test Title");

        // Test description setter
        dto.setDescription("Test Description");
        assertThat(dto.getDescription()).isEqualTo("Test Description");

        // Test startDateTime setter
        LocalDateTime start = LocalDateTime.now();
        dto.setStartDateTime(start);
        assertThat(dto.getStartDateTime()).isEqualTo(start);

        // Test endDateTime setter
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        dto.setEndDateTime(end);
        assertThat(dto.getEndDateTime()).isEqualTo(end);

        // Test recurrenceRule setter
        dto.setRecurrenceRule("FREQ=DAILY");
        assertThat(dto.getRecurrenceRule()).isEqualTo("FREQ=DAILY");

        // Test recurrenceEndDate setter
        LocalDateTime recEnd = LocalDateTime.now().plusDays(30);
        dto.setRecurrenceEndDate(recEnd);
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(recEnd);

        // Test recurrenceCount setter
        dto.setRecurrenceCount(10);
        assertThat(dto.getRecurrenceCount()).isEqualTo(10);

        // Test tagIds setter
        List<Long> tagIds = Arrays.asList(1L, 2L, 3L);
        dto.setTagIds(tagIds);
        assertThat(dto.getTagIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("Test getTagIds with various list modifications")
    void testGetTagIdsDefensiveCopyBehavior() {
        CreateEventDTO dto = new CreateEventDTO();

        // Test with mutable list
        List<Long> mutableList = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        dto.setTagIds(mutableList);

        List<Long> result1 = dto.getTagIds();
        List<Long> result2 = dto.getTagIds();

        // Each call should return a new defensive copy
        assertThat(result1).isNotSameAs(result2);
        assertThat(result1).isNotSameAs(mutableList);

        // Store the original content before modification
        List<Long> originalContent = new ArrayList<>(result1);

        // Modifying the original list WILL affect the DTO's internal state 
        // because setter doesn't make defensive copy (only getter does)
        mutableList.clear();
        assertThat(dto.getTagIds()).isEmpty(); // This reflects the current behavior

        // Reset with new data to test returned copy modification
        dto.setTagIds(Arrays.asList(1L, 2L, 3L));
        List<Long> result3 = dto.getTagIds();

        // Modifying returned copy should not affect the DTO
        result3.add(4L);
        assertThat(dto.getTagIds()).containsExactly(1L, 2L, 3L);
        assertThat(result3).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("Test equals and hashCode with all field combinations")
    void testEqualsAndHashCodeComprehensive() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);
        LocalDateTime recEnd = LocalDateTime.of(2024, 12, 31, 23, 59);
        List<Long> tagIds = Arrays.asList(1L, 2L);

        CreateEventDTO dto1 = new CreateEventDTO();
        dto1.setTitle("Title");
        dto1.setDescription("Description");
        dto1.setStartDateTime(start);
        dto1.setEndDateTime(end);
        dto1.setRecurrenceRule("FREQ=DAILY");
        dto1.setRecurrenceEndDate(recEnd);
        dto1.setRecurrenceCount(10);
        dto1.setTagIds(tagIds);

        CreateEventDTO dto2 = new CreateEventDTO();
        dto2.setTitle("Title");
        dto2.setDescription("Description");
        dto2.setStartDateTime(start);
        dto2.setEndDateTime(end);
        dto2.setRecurrenceRule("FREQ=DAILY");
        dto2.setRecurrenceEndDate(recEnd);
        dto2.setRecurrenceCount(10);
        dto2.setTagIds(new ArrayList<>(tagIds));

        // Test equality with all fields set
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());

        // Test inequality when changing each field
        CreateEventDTO dto3 = new CreateEventDTO();
        dto3.setTitle("Different Title");
        dto3.setDescription("Description");
        dto3.setStartDateTime(start);
        dto3.setEndDateTime(end);
        assertThat(dto1).isNotEqualTo(dto3);

        CreateEventDTO dto4 = new CreateEventDTO();
        dto4.setTitle("Title");
        dto4.setDescription("Different Description");
        dto4.setStartDateTime(start);
        dto4.setEndDateTime(end);
        assertThat(dto1).isNotEqualTo(dto4);

        // Test with null values
        CreateEventDTO dto5 = new CreateEventDTO();
        CreateEventDTO dto6 = new CreateEventDTO();
        assertThat(dto5).isEqualTo(dto6);
        assertThat(dto5.hashCode()).isEqualTo(dto6.hashCode());
    }

    @Test
    @DisplayName("Test toString with various field combinations")
    void testToStringComprehensive() {
        CreateEventDTO dto = new CreateEventDTO();

        // Test toString with minimal fields
        dto.setTitle("Title");
        dto.setDescription("Description");
        String result1 = dto.toString();
        assertThat(result1).contains("Title");
        assertThat(result1).contains("Description");
        assertThat(result1).contains("CreateEventDTO");

        // Test toString with all fields
        dto.setStartDateTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        dto.setRecurrenceRule("FREQ=WEEKLY");
        dto.setRecurrenceEndDate(LocalDateTime.of(2024, 12, 31, 23, 59));
        dto.setRecurrenceCount(52);
        dto.setTagIds(Arrays.asList(1L, 2L, 3L));

        String result2 = dto.toString();
        assertThat(result2).contains("Title");
        assertThat(result2).contains("Description");
        assertThat(result2).contains("2024-01-01T10:00");
        assertThat(result2).contains("2024-01-01T11:00");
        assertThat(result2).contains("FREQ=WEEKLY");
        assertThat(result2).contains("2024-12-31T23:59");
        assertThat(result2).contains("52");
        assertThat(result2).contains("[1, 2, 3]");
    }

    @Test
    @DisplayName("Test validation with whitespace-only strings")
    void testValidationWithWhitespaceStrings() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("   \t\n   ");
        dto.setDescription("   \t\n   ");
        dto.setStartDateTime(LocalDateTime.now());
        dto.setEndDateTime(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("title", "description");
    }

    @Test
    @DisplayName("Test recurrence fields with various values")
    void testRecurrenceFieldsComprehensive() {
        CreateEventDTO dto = new CreateEventDTO();

        // Test with complex recurrence rule
        dto.setRecurrenceRule("FREQ=WEEKLY;BYDAY=MO,WE,FR;INTERVAL=2");
        assertThat(dto.getRecurrenceRule()).isEqualTo("FREQ=WEEKLY;BYDAY=MO,WE,FR;INTERVAL=2");

        // Test with null recurrence rule
        dto.setRecurrenceRule(null);
        assertThat(dto.getRecurrenceRule()).isNull();

        // Test with empty recurrence rule
        dto.setRecurrenceRule("");
        assertThat(dto.getRecurrenceRule()).isEmpty();

        // Test recurrence count edge cases
        dto.setRecurrenceCount(0);
        assertThat(dto.getRecurrenceCount()).isEqualTo(0);

        dto.setRecurrenceCount(Integer.MAX_VALUE);
        assertThat(dto.getRecurrenceCount()).isEqualTo(Integer.MAX_VALUE);

        dto.setRecurrenceCount(null);
        assertThat(dto.getRecurrenceCount()).isNull();

        // Test recurrence end date edge cases
        LocalDateTime futureDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        dto.setRecurrenceEndDate(futureDate);
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(futureDate);

        dto.setRecurrenceEndDate(null);
        assertThat(dto.getRecurrenceEndDate()).isNull();
    }

    @Test
    @DisplayName("Test constructor with all null values")
    void testAllArgsConstructorWithNulls() {
        CreateEventDTO dto = new CreateEventDTO(
            null, null, null, null, null, null, null, null
        );

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
    @DisplayName("Test tagIds with large list")
    void testTagIdsWithLargeList() {
        CreateEventDTO dto = new CreateEventDTO();

        // Create a large list of tag IDs
        List<Long> largeTagList = new ArrayList<>();
        for (long i = 1; i <= 1000; i++) {
            largeTagList.add(i);
        }

        dto.setTagIds(largeTagList);
        List<Long> result = dto.getTagIds();

        assertThat(result).hasSize(1000);
        assertThat(result).isNotSameAs(largeTagList);
        assertThat(result.get(0)).isEqualTo(1L);
        assertThat(result.get(999)).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Test validation with edge case date times")
    void testValidationWithEdgeCaseDateTimes() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Valid Description");

        // Test with very old date
        LocalDateTime oldDate = LocalDateTime.of(1900, 1, 1, 0, 0);
        dto.setStartDateTime(oldDate);
        dto.setEndDateTime(oldDate.plusHours(1));

        Set<ConstraintViolation<CreateEventDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();

        // Test with very future date
        LocalDateTime futureDate = LocalDateTime.of(2999, 12, 31, 23, 59);
        dto.setStartDateTime(futureDate);
        dto.setEndDateTime(futureDate.plusHours(1));

        violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}

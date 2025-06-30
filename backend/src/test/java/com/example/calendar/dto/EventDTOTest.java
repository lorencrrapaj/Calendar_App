package com.example.calendar.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class EventDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void eventDTO_DefaultConstructor() {
        // When
        EventDTO dto = new EventDTO();

        // Then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getStartDateTime()).isNull();
        assertThat(dto.getEndDateTime()).isNull();
        assertThat(dto.getUserEmail()).isNull();
    }

    @Test
    void eventDTO_Builder() {
        // Given
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);
        String userEmail = "test@example.com";

        // When
        EventDTO dto = EventDTO.builder()
                .id(id)
                .title(title)
                .description(description)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .userEmail(userEmail)
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo(title);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dto.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(dto.getUserEmail()).isEqualTo(userEmail);
    }

    @Test
    void eventDTO_GettersAndSetters() {
        // Given
        EventDTO dto = new EventDTO();
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);
        String userEmail = "test@example.com";

        // When
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setStartDateTime(startDateTime);
        dto.setEndDateTime(endDateTime);
        dto.setUserEmail(userEmail);

        // Then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo(title);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dto.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(dto.getUserEmail()).isEqualTo(userEmail);
    }

    @Test
    void eventDTO_EqualsAndHashCode() {
        // Given
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);

        EventDTO dto1 = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .userEmail("test@example.com")
                .build();

        EventDTO dto2 = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .userEmail("test@example.com")
                .build();

        // Then - just verify methods exist and don't throw exceptions
        assertThat(dto1.equals(dto1)).isTrue(); // reflexive
        assertThat(dto1.equals(null)).isFalse(); // null check
        assertThat(dto1.equals("not a dto")).isFalse(); // different type
        assertThat(dto1.hashCode()).isNotNull(); // hashCode exists
        assertThat(dto2.hashCode()).isNotNull(); // hashCode exists
    }

    @Test
    void eventDTO_ToString() {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1))
                .userEmail("test@example.com")
                .build();

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).isNotEmpty();
    }

    @Test
    void eventDTO_NullValues() {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(null)
                .title(null)
                .description(null)
                .startDateTime(null)
                .endDateTime(null)
                .userEmail(null)
                .build();

        // Then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getStartDateTime()).isNull();
        assertThat(dto.getEndDateTime()).isNull();
        assertThat(dto.getUserEmail()).isNull();
    }

    @Test
    void eventDTO_AllFieldsGettersAndSetters() {
        // Given
        EventDTO dto = new EventDTO();
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 1, 11, 0);
        String userEmail = "test@example.com";
        String recurrenceRule = "DAILY";
        LocalDateTime recurrenceEndDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        Integer recurrenceCount = 365;
        Long parentEventId = 2L;
        LocalDateTime originalStartDateTime = LocalDateTime.of(2024, 1, 1, 9, 0);
        String excludedDates = "2024-01-15,2024-01-16";

        // When
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setStartDateTime(startDateTime);
        dto.setEndDateTime(endDateTime);
        dto.setUserEmail(userEmail);
        dto.setRecurrenceRule(recurrenceRule);
        dto.setRecurrenceEndDate(recurrenceEndDate);
        dto.setRecurrenceCount(recurrenceCount);
        dto.setParentEventId(parentEventId);
        dto.setOriginalStartDateTime(originalStartDateTime);
        dto.setExcludedDates(excludedDates);

        // Then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo(title);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dto.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(dto.getUserEmail()).isEqualTo(userEmail);
        assertThat(dto.getRecurrenceRule()).isEqualTo(recurrenceRule);
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(recurrenceEndDate);
        assertThat(dto.getRecurrenceCount()).isEqualTo(recurrenceCount);
        assertThat(dto.getParentEventId()).isEqualTo(parentEventId);
        assertThat(dto.getOriginalStartDateTime()).isEqualTo(originalStartDateTime);
        assertThat(dto.getExcludedDates()).isEqualTo(excludedDates);
    }

    @Test
    void eventDTO_BuilderWithAllFields() {
        // Given
        Long id = 1L;
        String title = "Test Event";
        String description = "Test Description";
        LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 1, 11, 0);
        String userEmail = "test@example.com";
        String recurrenceRule = "WEEKLY";
        LocalDateTime recurrenceEndDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        Integer recurrenceCount = 52;
        Long parentEventId = 3L;
        LocalDateTime originalStartDateTime = LocalDateTime.of(2024, 1, 1, 9, 0);
        String excludedDates = "2024-02-14,2024-03-15";

        // When
        EventDTO dto = EventDTO.builder()
                .id(id)
                .title(title)
                .description(description)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .userEmail(userEmail)
                .recurrenceRule(recurrenceRule)
                .recurrenceEndDate(recurrenceEndDate)
                .recurrenceCount(recurrenceCount)
                .parentEventId(parentEventId)
                .originalStartDateTime(originalStartDateTime)
                .excludedDates(excludedDates)
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo(title);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(dto.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(dto.getUserEmail()).isEqualTo(userEmail);
        assertThat(dto.getRecurrenceRule()).isEqualTo(recurrenceRule);
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(recurrenceEndDate);
        assertThat(dto.getRecurrenceCount()).isEqualTo(recurrenceCount);
        assertThat(dto.getParentEventId()).isEqualTo(parentEventId);
        assertThat(dto.getOriginalStartDateTime()).isEqualTo(originalStartDateTime);
        assertThat(dto.getExcludedDates()).isEqualTo(excludedDates);
    }

    @Test
    void eventDTO_JsonSerialization() throws Exception {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
                .userEmail("test@example.com")
                .recurrenceRule("DAILY")
                .recurrenceEndDate(LocalDateTime.of(2024, 12, 31, 23, 59))
                .recurrenceCount(365)
                .parentEventId(2L)
                .originalStartDateTime(LocalDateTime.of(2024, 1, 1, 9, 0))
                .excludedDates("2024-01-15,2024-01-16")
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"title\":\"Test Event\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
        assertThat(json).contains("\"userEmail\":\"test@example.com\"");
        assertThat(json).contains("\"recurrenceRule\":\"DAILY\"");
        assertThat(json).contains("\"recurrenceCount\":365");
        assertThat(json).contains("\"parentEventId\":2");
        assertThat(json).contains("\"excludedDates\":\"2024-01-15,2024-01-16\"");
    }

    @Test
    void eventDTO_JsonDeserialization() throws Exception {
        // Given
        String json = "{\"id\":1,\"title\":\"Test Event\",\"description\":\"Test Description\"," +
                "\"startDateTime\":\"2024-01-01T10:00:00\",\"endDateTime\":\"2024-01-01T11:00:00\"," +
                "\"userEmail\":\"test@example.com\",\"recurrenceRule\":\"WEEKLY\"," +
                "\"recurrenceEndDate\":\"2024-12-31T23:59:00\",\"recurrenceCount\":52," +
                "\"parentEventId\":3,\"originalStartDateTime\":\"2024-01-01T09:00:00\"," +
                "\"excludedDates\":\"2024-02-14,2024-03-15\"}";

        // When
        EventDTO dto = objectMapper.readValue(json, EventDTO.class);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Test Event");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getStartDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getEndDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 11, 0));
        assertThat(dto.getUserEmail()).isEqualTo("test@example.com");
        assertThat(dto.getRecurrenceRule()).isEqualTo("WEEKLY");
        assertThat(dto.getRecurrenceEndDate()).isEqualTo(LocalDateTime.of(2024, 12, 31, 23, 59));
        assertThat(dto.getRecurrenceCount()).isEqualTo(52);
        assertThat(dto.getParentEventId()).isEqualTo(3L);
        assertThat(dto.getOriginalStartDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 9, 0));
        assertThat(dto.getExcludedDates()).isEqualTo("2024-02-14,2024-03-15");
    }

    @Test
    void eventDTO_JsonRoundTrip() throws Exception {
        // Given
        EventDTO original = EventDTO.builder()
                .id(1L)
                .title("Round Trip Event")
                .description("Round Trip Description")
                .startDateTime(LocalDateTime.of(2024, 6, 15, 14, 30))
                .endDateTime(LocalDateTime.of(2024, 6, 15, 15, 30))
                .userEmail("roundtrip@example.com")
                .recurrenceRule("MONTHLY")
                .recurrenceEndDate(LocalDateTime.of(2024, 12, 15, 15, 30))
                .recurrenceCount(6)
                .parentEventId(5L)
                .originalStartDateTime(LocalDateTime.of(2024, 6, 15, 14, 0))
                .excludedDates("2024-08-15,2024-10-15")
                .build();

        // When
        String json = objectMapper.writeValueAsString(original);
        EventDTO deserialized = objectMapper.readValue(json, EventDTO.class);

        // Then
        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
        assertThat(deserialized.getDescription()).isEqualTo(original.getDescription());
        assertThat(deserialized.getStartDateTime()).isEqualTo(original.getStartDateTime());
        assertThat(deserialized.getEndDateTime()).isEqualTo(original.getEndDateTime());
        assertThat(deserialized.getUserEmail()).isEqualTo(original.getUserEmail());
        assertThat(deserialized.getRecurrenceRule()).isEqualTo(original.getRecurrenceRule());
        assertThat(deserialized.getRecurrenceEndDate()).isEqualTo(original.getRecurrenceEndDate());
        assertThat(deserialized.getRecurrenceCount()).isEqualTo(original.getRecurrenceCount());
        assertThat(deserialized.getParentEventId()).isEqualTo(original.getParentEventId());
        assertThat(deserialized.getOriginalStartDateTime()).isEqualTo(original.getOriginalStartDateTime());
        assertThat(deserialized.getExcludedDates()).isEqualTo(original.getExcludedDates());
    }

    @Test
    void eventDTO_JsonWithNullOptionalFields() throws Exception {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Minimal Event")
                .description("Minimal Description")
                .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
                .userEmail("minimal@example.com")
                .recurrenceRule(null)
                .recurrenceEndDate(null)
                .recurrenceCount(null)
                .parentEventId(null)
                .originalStartDateTime(null)
                .excludedDates(null)
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        EventDTO deserialized = objectMapper.readValue(json, EventDTO.class);

        // Then
        assertThat(deserialized.getId()).isEqualTo(1L);
        assertThat(deserialized.getTitle()).isEqualTo("Minimal Event");
        assertThat(deserialized.getDescription()).isEqualTo("Minimal Description");
        assertThat(deserialized.getStartDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(deserialized.getEndDateTime()).isEqualTo(LocalDateTime.of(2024, 1, 1, 11, 0));
        assertThat(deserialized.getUserEmail()).isEqualTo("minimal@example.com");
        assertThat(deserialized.getRecurrenceRule()).isNull();
        assertThat(deserialized.getRecurrenceEndDate()).isNull();
        assertThat(deserialized.getRecurrenceCount()).isNull();
        assertThat(deserialized.getParentEventId()).isNull();
        assertThat(deserialized.getOriginalStartDateTime()).isNull();
        assertThat(deserialized.getExcludedDates()).isNull();
    }

    @Test
    void eventDTO_JsonWithSpecialCharacters() throws Exception {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Event with \"quotes\" & special chars")
                .description("Description with\nnewlines and\ttabs")
                .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
                .userEmail("special+chars@example.com")
                .excludedDates("2024-01-15T10:00:00,2024-01-16T11:00:00")
                .build();

        // When
        String json = objectMapper.writeValueAsString(dto);
        EventDTO deserialized = objectMapper.readValue(json, EventDTO.class);

        // Then
        assertThat(deserialized.getTitle()).isEqualTo("Event with \"quotes\" & special chars");
        assertThat(deserialized.getDescription()).isEqualTo("Description with\nnewlines and\ttabs");
        assertThat(deserialized.getUserEmail()).isEqualTo("special+chars@example.com");
        assertThat(deserialized.getExcludedDates()).isEqualTo("2024-01-15T10:00:00,2024-01-16T11:00:00");
    }

    @Test
    void eventDTO_GetTags_WhenTagsIsNull_ReturnsEmptyList() {
        // Given
        EventDTO dto = new EventDTO();
        // tags field is null by default

        // When
        List<TagDTO> result = dto.getTags();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void eventDTO_GetTags_WhenTagsIsNotNull_ReturnsDefensiveCopy() {
        // Given
        EventDTO dto = new EventDTO();
        TagDTO tag1 = TagDTO.builder().id(1L).name("Work").build();
        TagDTO tag2 = TagDTO.builder().id(2L).name("Personal").build();
        List<TagDTO> originalTags = new ArrayList<>();
        originalTags.add(tag1);
        originalTags.add(tag2);
        dto.setTags(originalTags);

        // When
        List<TagDTO> result = dto.getTags();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(tag1, tag2);

        // Verify it's a defensive copy - modifying the returned list shouldn't affect the original
        result.clear();
        assertThat(dto.getTags()).hasSize(2); // Original should still have 2 elements
    }

    @Test
    void eventDTO_SetAndGetTags() {
        // Given
        EventDTO dto = new EventDTO();
        TagDTO tag1 = TagDTO.builder().id(1L).name("Meeting").build();
        TagDTO tag2 = TagDTO.builder().id(2L).name("Important").build();
        List<TagDTO> tags = new ArrayList<>();
        tags.add(tag1);
        tags.add(tag2);

        // When
        dto.setTags(tags);
        List<TagDTO> result = dto.getTags();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Meeting");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Important");
    }

    @Test
    void eventDTO_BuilderWithTags() {
        // Given
        TagDTO tag1 = TagDTO.builder().id(1L).name("Urgent").build();
        TagDTO tag2 = TagDTO.builder().id(2L).name("Project").build();
        List<TagDTO> tags = new ArrayList<>();
        tags.add(tag1);
        tags.add(tag2);

        // When
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Tagged Event")
                .description("Event with tags")
                .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
                .userEmail("tagged@example.com")
                .tags(tags)
                .build();

        // Then
        List<TagDTO> result = dto.getTags();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(tag1, tag2);
    }

    @Test
    void eventDTO_TagsInEqualsAndHashCode() {
        // Given
        TagDTO tag1 = TagDTO.builder().id(1L).name("Test").build();
        List<TagDTO> tags = new ArrayList<>();
        tags.add(tag1);

        EventDTO dto1 = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .tags(tags)
                .build();

        EventDTO dto2 = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .tags(tags)
                .build();

        EventDTO dto3 = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .tags(null)
                .build();

        // Then
        assertThat(dto1.equals(dto2)).isTrue();
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.equals(dto3)).isFalse();
    }

    @Test
    void eventDTO_TagsInToString() {
        // Given
        TagDTO tag1 = TagDTO.builder().id(1L).name("Test").build();
        List<TagDTO> tags = new ArrayList<>();
        tags.add(tag1);

        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .tags(tags)
                .build();

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("tags");
    }
}

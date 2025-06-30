package com.example.calendar.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagDTOTest {

    private TagDTO tagDTO;

    @BeforeEach
    void setUp() {
        tagDTO = new TagDTO();
    }

    @Test
    void testDefaultConstructor() {
        // When
        TagDTO newTagDTO = new TagDTO();

        // Then
        assertNull(newTagDTO.getId());
        assertNull(newTagDTO.getName());
    }

    @Test
    void testAllArgsConstructor() {
        // When
        TagDTO newTagDTO = new TagDTO(1L, "Work");

        // Then
        assertEquals(1L, newTagDTO.getId());
        assertEquals("Work", newTagDTO.getName());
    }

    @Test
    void testBuilder() {
        // When
        TagDTO builtTagDTO = TagDTO.builder()
                .id(2L)
                .name("Personal")
                .build();

        // Then
        assertEquals(2L, builtTagDTO.getId());
        assertEquals("Personal", builtTagDTO.getName());
    }

    @Test
    void testBuilderWithPartialData() {
        // When
        TagDTO builtTagDTO = TagDTO.builder()
                .name("Important")
                .build();

        // Then
        assertNull(builtTagDTO.getId());
        assertEquals("Important", builtTagDTO.getName());
    }

    @Test
    void testSettersAndGetters() {
        // When
        tagDTO.setId(5L);
        tagDTO.setName("Urgent");

        // Then
        assertEquals(5L, tagDTO.getId());
        assertEquals("Urgent", tagDTO.getName());
    }

    @Test
    void testSetNullValues() {
        // Given
        tagDTO.setId(1L);
        tagDTO.setName("Test");

        // When
        tagDTO.setId(null);
        tagDTO.setName(null);

        // Then
        assertNull(tagDTO.getId());
        assertNull(tagDTO.getName());
    }

    @Test
    void testSetEmptyName() {
        // When
        tagDTO.setName("");

        // Then
        assertEquals("", tagDTO.getName());
    }

    @Test
    void testSetLongName() {
        // Given
        String longName = "A".repeat(100);

        // When
        tagDTO.setName(longName);

        // Then
        assertEquals(longName, tagDTO.getName());
        assertEquals(100, tagDTO.getName().length());
    }

    @Test
    void testSetSpecialCharactersInName() {
        // Given
        String specialName = "Work-2024! @#$%";

        // When
        tagDTO.setName(specialName);

        // Then
        assertEquals(specialName, tagDTO.getName());
    }

    @Test
    void testSetNegativeId() {
        // When
        tagDTO.setId(-1L);

        // Then
        assertEquals(-1L, tagDTO.getId());
    }

    @Test
    void testSetZeroId() {
        // When
        tagDTO.setId(0L);

        // Then
        assertEquals(0L, tagDTO.getId());
    }

    @Test
    void testSetLargeId() {
        // Given
        Long largeId = Long.MAX_VALUE;

        // When
        tagDTO.setId(largeId);

        // Then
        assertEquals(largeId, tagDTO.getId());
    }

    @Test
    void testObjectIdentity() {
        // Given
        TagDTO dto1 = TagDTO.builder()
                .id(1L)
                .name("Work")
                .build();

        TagDTO dto2 = TagDTO.builder()
                .id(1L)
                .name("Work")
                .build();

        // Then - Test object identity (not equality since TagDTO doesn't override equals)
        assertEquals(dto1, dto1); // Same reference
        assertNotEquals(dto1, dto2); // Different objects
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, "not a TagDTO");
    }

    @Test
    void testHashCodeConsistency() {
        // Given
        tagDTO.setId(1L);
        tagDTO.setName("Work");

        // When
        int hashCode1 = tagDTO.hashCode();
        int hashCode2 = tagDTO.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testToString() {
        // Given
        tagDTO.setId(1L);
        tagDTO.setName("Work");

        // When
        String toString = tagDTO.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Work"));
        assertTrue(toString.contains("TagDTO"));
    }

    @Test
    void testToStringWithNullValues() {
        // When
        String toString = tagDTO.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("TagDTO"));
    }

    @Test
    void testBuilderToString() {
        // When
        String builderToString = TagDTO.builder().toString();

        // Then
        assertNotNull(builderToString);
        assertTrue(builderToString.contains("TagDTO.TagDTOBuilder"));
    }

    @Test
    void testChainedBuilderCalls() {
        // When
        TagDTO chainedDTO = TagDTO.builder()
                .id(3L)
                .name("Chained")
                .id(4L) // Override previous id
                .build();

        // Then
        assertEquals(4L, chainedDTO.getId());
        assertEquals("Chained", chainedDTO.getName());
    }
}

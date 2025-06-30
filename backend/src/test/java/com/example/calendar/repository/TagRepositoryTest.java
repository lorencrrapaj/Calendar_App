package com.example.calendar.repository;

import com.example.calendar.model.Tag;
import com.example.calendar.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

    private Tag workTag;
    private Tag personalTag;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .build();

        workTag = Tag.builder()
                .name("Work")
                .user(testUser)
                .build();

        personalTag = Tag.builder()
                .name("Personal")
                .user(testUser)
                .build();
    }

    @Test
    void findByUserAndName_ExistingTag_ReturnsTag() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());
        entityManager.persistAndFlush(workTag);

        // When
        Optional<Tag> found = tagRepository.findByUserAndName(user, "Work");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Work", found.get().getName());
        assertEquals(workTag.getId(), found.get().getId());
    }

    @Test
    void findByUserAndName_NonExistingTag_ReturnsEmpty() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());

        // When
        Optional<Tag> found = tagRepository.findByUserAndName(user, "NonExistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByUserAndName_NullName_ReturnsEmpty() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());

        // When
        Optional<Tag> found = tagRepository.findByUserAndName(user, null);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByUserAndName_EmptyName_ReturnsEmpty() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());

        // When
        Optional<Tag> found = tagRepository.findByUserAndName(user, "");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByUserAndName_CaseSensitive_ReturnsEmpty() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());
        entityManager.persistAndFlush(workTag);

        // When
        Optional<Tag> found = tagRepository.findByUserAndName(user, "work"); // lowercase

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void existsByUserAndName_ExistingTag_ReturnsTrue() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());
        entityManager.persistAndFlush(workTag);

        // When
        boolean exists = tagRepository.existsByUserAndName(user, "Work");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByUserAndName_NonExistingTag_ReturnsFalse() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());

        // When
        boolean exists = tagRepository.existsByUserAndName(user, "NonExistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByUserAndName_NullName_ReturnsFalse() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());

        // When
        boolean exists = tagRepository.existsByUserAndName(user, null);

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByUserAndName_EmptyName_ReturnsFalse() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());

        // When
        boolean exists = tagRepository.existsByUserAndName(user, "");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByUserAndName_CaseSensitive_ReturnsFalse() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());
        entityManager.persistAndFlush(workTag);

        // When
        boolean exists = tagRepository.existsByUserAndName(user, "work"); // lowercase

        // Then
        assertFalse(exists);
    }

    @Test
    void save_ValidTag_SavesSuccessfully() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());

        // When
        Tag saved = tagRepository.save(workTag);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Work", saved.getName());

        // Verify it's actually saved
        Optional<Tag> found = tagRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Work", found.get().getName());
    }

    @Test
    void findAll_MultipleTags_ReturnsAllTags() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());
        entityManager.persistAndFlush(workTag);
        entityManager.persistAndFlush(personalTag);

        // When
        var allTags = tagRepository.findAll();

        // Then
        assertEquals(2, allTags.size());
        assertTrue(allTags.stream().anyMatch(tag -> "Work".equals(tag.getName())));
        assertTrue(allTags.stream().anyMatch(tag -> "Personal".equals(tag.getName())));
    }

    @Test
    void deleteById_ExistingTag_DeletesSuccessfully() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());
        Tag saved = entityManager.persistAndFlush(workTag);
        Long tagId = saved.getId();

        // When
        tagRepository.deleteById(tagId);
        entityManager.flush();

        // Then
        Optional<Tag> found = tagRepository.findById(tagId);
        assertFalse(found.isPresent());
    }

    @Test
    void findById_ExistingTag_ReturnsTag() {
        // Given
        User user = entityManager.persistAndFlush(workTag.getUser());
        Tag saved = entityManager.persistAndFlush(workTag);

        // When
        Optional<Tag> found = tagRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Work", found.get().getName());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_NonExistingTag_ReturnsEmpty() {
        // When
        Optional<Tag> found = tagRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }
}

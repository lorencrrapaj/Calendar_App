package com.example.calendar.service;

import com.example.calendar.dto.CreateTagDTO;
import com.example.calendar.dto.TagDTO;
import com.example.calendar.model.Tag;
import com.example.calendar.model.User;
import com.example.calendar.repository.TagRepository;
import com.example.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TagService tagService;

    private Tag testTag;
    private User testUser;
    private CreateTagDTO createTagDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .build();

        testTag = Tag.builder()
                .id(1L)
                .name("Work")
                .user(testUser)
                .build();

        createTagDTO = new CreateTagDTO();
        createTagDTO.setName("Work");
    }

    @Test
    void getAllTags_ShouldReturnAllTags() {
        // Given
        List<Tag> tags = Arrays.asList(testTag);
        when(tagRepository.findByUserEmail("test@example.com")).thenReturn(tags);

        // When
        List<TagDTO> result = tagService.getAllTags("test@example.com");

        // Then
        assertEquals(1, result.size());
        assertEquals("Work", result.get(0).getName());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void createTag_ShouldCreateNewTag() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tagRepository.existsByUserAndName(testUser, "Work")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        // When
        TagDTO result = tagService.createTag(createTagDTO, "test@example.com");

        // Then
        assertEquals("Work", result.getName());
        assertEquals(1L, result.getId());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void createTag_ShouldThrowException_WhenTagNameExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tagRepository.existsByUserAndName(testUser, "Work")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.createTag(createTagDTO, "test@example.com")
        );
        assertEquals("Tag with name 'Work' already exists", exception.getMessage());
    }

    @Test
    void updateTag_ShouldUpdateExistingTag() {
        // Given
        CreateTagDTO updateDTO = new CreateTagDTO();
        updateDTO.setName("Personal");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(tagRepository.existsByUserAndName(testUser, "Personal")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        // When
        TagDTO result = tagService.updateTag(1L, updateDTO, "test@example.com");

        // Then
        assertNotNull(result);
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void deleteTag_ShouldDeleteExistingTag() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        // When
        tagService.deleteTag(1L, "test@example.com");

        // Then
        verify(tagRepository).delete(testTag);
    }

    @Test
    void deleteTag_ShouldThrowException_WhenTagNotFound() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.deleteTag(1L, "test@example.com")
        );
        assertEquals("Tag not found with id: 1", exception.getMessage());
    }

    @Test
    void getTagById_ShouldReturnTag() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        // When
        TagDTO result = tagService.getTagById(1L, "test@example.com");

        // Then
        assertEquals("Work", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void getTagById_ShouldThrowException_WhenTagNotFound() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.getTagById(1L, "test@example.com")
        );
        assertEquals("Tag not found with id: 1", exception.getMessage());
    }

    @Test
    void createTag_ShouldTrimWhitespace() {
        // Given
        CreateTagDTO dtoWithWhitespace = new CreateTagDTO();
        dtoWithWhitespace.setName("  Work  ");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tagRepository.existsByUserAndName(testUser, "Work")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        // When
        TagDTO result = tagService.createTag(dtoWithWhitespace, "test@example.com");

        // Then
        assertEquals("Work", result.getName());
        verify(tagRepository).existsByUserAndName(testUser, "Work");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void updateTag_ShouldTrimWhitespace() {
        // Given
        CreateTagDTO updateDTO = new CreateTagDTO();
        updateDTO.setName("  Personal  ");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(tagRepository.existsByUserAndName(testUser, "Personal")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        // When
        TagDTO result = tagService.updateTag(1L, updateDTO, "test@example.com");

        // Then
        assertNotNull(result);
        verify(tagRepository).existsByUserAndName(testUser, "Personal");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void updateTag_ShouldThrowException_WhenTagNotFound() {
        // Given
        CreateTagDTO updateDTO = new CreateTagDTO();
        updateDTO.setName("Personal");
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.updateTag(999L, updateDTO, "test@example.com")
        );
        assertEquals("Tag not found with id: 999", exception.getMessage());
    }

    @Test
    void updateTag_ShouldThrowException_WhenNewNameAlreadyExists() {
        // Given
        CreateTagDTO updateDTO = new CreateTagDTO();
        updateDTO.setName("Personal");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(tagRepository.existsByUserAndName(testUser, "Personal")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.updateTag(1L, updateDTO, "test@example.com")
        );
        assertEquals("Tag with name 'Personal' already exists", exception.getMessage());
    }

    @Test
    void updateTag_ShouldAllowSameName() {
        // Given
        CreateTagDTO updateDTO = new CreateTagDTO();
        updateDTO.setName("Work"); // Same name as current

        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        // When
        TagDTO result = tagService.updateTag(1L, updateDTO, "test@example.com");

        // Then
        assertNotNull(result);
        verify(tagRepository, never()).existsByUserAndName(any(User.class), anyString());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void getAllTags_ShouldReturnEmptyList_WhenNoTags() {
        // Given
        when(tagRepository.findByUserEmail("test@example.com")).thenReturn(Arrays.asList());

        // When
        List<TagDTO> result = tagService.getAllTags("test@example.com");

        // Then
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void createTag_ShouldThrowException_WhenUserNotFound() {
        // Given
        CreateTagDTO createTagDTO = new CreateTagDTO();
        createTagDTO.setName("Work");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.createTag(createTagDTO, "nonexistent@example.com")
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateTag_ShouldThrowException_WhenUserDoesNotOwnTag() {
        // Given
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .passwordHash("password")
                .build();

        Tag otherUserTag = Tag.builder()
                .id(1L)
                .name("Work")
                .user(otherUser)
                .build();

        CreateTagDTO updateDTO = new CreateTagDTO();
        updateDTO.setName("Personal");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(otherUserTag));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.updateTag(1L, updateDTO, "test@example.com")
        );
        assertEquals("Access denied: You can only update your own tags", exception.getMessage());
    }

    @Test
    void deleteTag_ShouldThrowException_WhenUserDoesNotOwnTag() {
        // Given
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .passwordHash("password")
                .build();

        Tag otherUserTag = Tag.builder()
                .id(1L)
                .name("Work")
                .user(otherUser)
                .build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(otherUserTag));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.deleteTag(1L, "test@example.com")
        );
        assertEquals("Access denied: You can only delete your own tags", exception.getMessage());
    }

    @Test
    void getTagById_ShouldThrowException_WhenUserDoesNotOwnTag() {
        // Given
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .passwordHash("password")
                .build();

        Tag otherUserTag = Tag.builder()
                .id(1L)
                .name("Work")
                .user(otherUser)
                .build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(otherUserTag));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tagService.getTagById(1L, "test@example.com")
        );
        assertEquals("Access denied: You can only access your own tags", exception.getMessage());
    }

    @Test
    void createTag_ShouldHandleNullTagName() {
        // Given
        CreateTagDTO createTagDTO = new CreateTagDTO();
        createTagDTO.setName(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(
                NullPointerException.class,
                () -> tagService.createTag(createTagDTO, "test@example.com")
        );
    }

    @Test
    void updateTag_ShouldHandleNullTagName() {
        // Given
        CreateTagDTO updateDTO = new CreateTagDTO();
        updateDTO.setName(null);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        // When & Then
        assertThrows(
                NullPointerException.class,
                () -> tagService.updateTag(1L, updateDTO, "test@example.com")
        );
    }
}

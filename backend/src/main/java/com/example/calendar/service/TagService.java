package com.example.calendar.service;

import com.example.calendar.dto.CreateTagDTO;
import com.example.calendar.dto.TagDTO;
import com.example.calendar.model.Tag;
import com.example.calendar.model.User;
import com.example.calendar.repository.TagRepository;
import com.example.calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    /**
     * Get all tags for a user
     */
    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags(String userEmail) {
        return tagRepository.findByUserEmail(userEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new tag
     */
    public TagDTO createTag(CreateTagDTO createTagDTO, String userEmail) {
        // Find the user
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        // Check if tag with this name already exists for this user
        if (tagRepository.existsByUserAndName(user, createTagDTO.getName().trim())) {
            throw new IllegalArgumentException("Tag with name '" + createTagDTO.getName() + "' already exists");
        }

        Tag tag = Tag.builder()
                .name(createTagDTO.getName().trim())
                .user(user)
                .build();

        Tag savedTag = tagRepository.save(tag);
        return convertToDTO(savedTag);
    }

    /**
     * Update an existing tag
     */
    public TagDTO updateTag(Long id, CreateTagDTO createTagDTO, String userEmail) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + id));

        // Verify ownership
        if (!tag.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Access denied: You can only update your own tags");
        }

        String newName = createTagDTO.getName().trim();

        // Check if another tag with this name already exists for this user
        if (!tag.getName().equals(newName) && tagRepository.existsByUserAndName(tag.getUser(), newName)) {
            throw new IllegalArgumentException("Tag with name '" + newName + "' already exists");
        }

        tag.setName(newName);
        Tag savedTag = tagRepository.save(tag);
        return convertToDTO(savedTag);
    }

    /**
     * Delete a tag
     */
    public void deleteTag(Long id, String userEmail) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + id));

        // Verify ownership
        if (!tag.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Access denied: You can only delete your own tags");
        }

        tagRepository.delete(tag);
    }

    /**
     * Get tag by ID
     */
    @Transactional(readOnly = true)
    public TagDTO getTagById(Long id, String userEmail) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + id));

        // Verify ownership
        if (!tag.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Access denied: You can only access your own tags");
        }

        return convertToDTO(tag);
    }

    /**
     * Convert Tag entity to TagDTO
     */
    private TagDTO convertToDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}

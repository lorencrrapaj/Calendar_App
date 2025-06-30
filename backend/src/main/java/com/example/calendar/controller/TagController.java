package com.example.calendar.controller;

import com.example.calendar.dto.CreateTagDTO;
import com.example.calendar.dto.TagDTO;
import com.example.calendar.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;

    /**
     * Get all tags
     */
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<TagDTO> tags = tagService.getAllTags(principal.getName());
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new tag
     */
    @PostMapping
    public ResponseEntity<?> createTag(@Valid @RequestBody CreateTagDTO createTagDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            TagDTO createdTag = tagService.createTag(createTagDTO, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the tag");
        }
    }

    /**
     * Update an existing tag
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody CreateTagDTO createTagDTO,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            TagDTO updatedTag = tagService.updateTag(id, createTagDTO, principal.getName());
            return ResponseEntity.ok(updatedTag);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("Tag not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the tag");
        }
    }

    /**
     * Delete a tag
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            tagService.deleteTag(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("Tag not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the tag");
        }
    }

    /**
     * Get a tag by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTagById(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            TagDTO tag = tagService.getTagById(id, principal.getName());
            return ResponseEntity.ok(tag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching the tag");
        }
    }
}

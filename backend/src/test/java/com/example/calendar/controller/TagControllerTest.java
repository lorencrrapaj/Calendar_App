package com.example.calendar.controller;

import com.example.calendar.dto.CreateTagDTO;
import com.example.calendar.dto.TagDTO;
import com.example.calendar.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TagController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    @Autowired
    private ObjectMapper objectMapper;

    private TagDTO workTagDTO;
    private TagDTO personalTagDTO;
    private CreateTagDTO validCreateTagDTO;

    @BeforeEach
    void setUp() {
        workTagDTO = TagDTO.builder()
                .id(1L)
                .name("Work")
                .build();

        personalTagDTO = TagDTO.builder()
                .id(2L)
                .name("Personal")
                .build();

        validCreateTagDTO = new CreateTagDTO();
        validCreateTagDTO.setName("Work");
    }

    // GET /api/tags tests
    @Test
    void getAllTags_Success_ReturnsAllTags() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        List<TagDTO> tags = Arrays.asList(workTagDTO, personalTagDTO);
        when(tagService.getAllTags(anyString())).thenReturn(tags);

        // When & Then
        mockMvc.perform(get("/api/tags")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Work"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Personal"));
    }

    @Test
    void getAllTags_Success_EmptyList_ReturnsEmptyArray() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.getAllTags(anyString())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/tags")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllTags_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.getAllTags(anyString())).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/tags")
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllTags_Unauthorized_NullPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isUnauthorized());
    }

    // POST /api/tags tests
    @Test
    void createTag_Success_ValidDTO_ReturnsCreatedTag() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.createTag(any(CreateTagDTO.class), anyString())).thenReturn(workTagDTO);

        // When & Then
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Work"));
    }

    @Test
    void createTag_BadRequest_DuplicateName_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.createTag(any(CreateTagDTO.class), anyString()))
                .thenThrow(new IllegalArgumentException("Tag with name 'Work' already exists"));

        // When & Then
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tag with name 'Work' already exists"));
    }

    @Test
    void createTag_BadRequest_EmptyName_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        CreateTagDTO invalidDTO = new CreateTagDTO();
        invalidDTO.setName("");

        // When & Then - Spring validation handles this before reaching the service
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTag_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.createTag(any(CreateTagDTO.class), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while creating the tag"));
    }

    @Test
    void createTag_Unauthorized_NullPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO)))
                .andExpect(status().isUnauthorized());
    }

    // PUT /api/tags/{id} tests
    @Test
    void updateTag_Success_ValidDTO_ReturnsUpdatedTag() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        TagDTO updatedTag = TagDTO.builder()
                .id(1L)
                .name("Updated Work")
                .build();
        when(tagService.updateTag(eq(1L), any(CreateTagDTO.class), anyString())).thenReturn(updatedTag);

        // When & Then
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Work"));
    }

    @Test
    void updateTag_NotFound_InvalidID_Returns404() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.updateTag(eq(999L), any(CreateTagDTO.class), anyString()))
                .thenThrow(new IllegalArgumentException("Tag not found"));

        // When & Then
        mockMvc.perform(put("/api/tags/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Tag not found"));
    }

    @Test
    void updateTag_BadRequest_DuplicateName_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.updateTag(eq(1L), any(CreateTagDTO.class), anyString()))
                .thenThrow(new IllegalArgumentException("Tag with name 'Personal' already exists"));

        // When & Then
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tag with name 'Personal' already exists"));
    }

    @Test
    void updateTag_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.updateTag(eq(1L), any(CreateTagDTO.class), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while updating the tag"));
    }

    @Test
    void updateTag_Unauthorized_NullPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateTagDTO)))
                .andExpect(status().isUnauthorized());
    }

    // DELETE /api/tags/{id} tests
    @Test
    void deleteTag_Success_ValidID_Returns204() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doNothing().when(tagService).deleteTag(1L, "test@example.com");

        // When & Then
        mockMvc.perform(delete("/api/tags/1")
                        .principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTag_NotFound_InvalidID_Returns404() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doThrow(new IllegalArgumentException("Tag not found"))
                .when(tagService).deleteTag(999L, "test@example.com");

        // When & Then
        mockMvc.perform(delete("/api/tags/999")
                        .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Tag not found"));
    }

    @Test
    void deleteTag_BadRequest_TagInUse_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doThrow(new IllegalArgumentException("Cannot delete tag that is in use by events"))
                .when(tagService).deleteTag(1L, "test@example.com");

        // When & Then
        mockMvc.perform(delete("/api/tags/1")
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot delete tag that is in use by events"));
    }

    @Test
    void deleteTag_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doThrow(new RuntimeException("Database connection failed"))
                .when(tagService).deleteTag(1L, "test@example.com");

        // When & Then
        mockMvc.perform(delete("/api/tags/1")
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while deleting the tag"));
    }

    @Test
    void deleteTag_Unauthorized_NullPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isUnauthorized());
    }

    // GET /api/tags/{id} tests
    @Test
    void getTagById_Success_ValidID_ReturnsTag() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.getTagById(1L, "test@example.com")).thenReturn(workTagDTO);

        // When & Then
        mockMvc.perform(get("/api/tags/1")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Work"));
    }

    @Test
    void getTagById_NotFound_InvalidID_Returns404() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.getTagById(999L, "test@example.com"))
                .thenThrow(new IllegalArgumentException("Tag not found"));

        // When & Then
        mockMvc.perform(get("/api/tags/999")
                        .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Tag not found"));
    }

    @Test
    void getTagById_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(tagService.getTagById(1L, "test@example.com"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/tags/1")
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while fetching the tag"));
    }

    @Test
    void getTagById_Unauthorized_NullPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isUnauthorized());
    }
}

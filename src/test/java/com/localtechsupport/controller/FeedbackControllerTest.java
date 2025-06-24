package com.localtechsupport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localtechsupport.dto.request.CreateFeedbackRequest;
import com.localtechsupport.dto.request.UpdateFeedbackRequest;
import com.localtechsupport.entity.FeedbackEntry;
import com.localtechsupport.entity.Ticket;
import com.localtechsupport.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedbackController.class)
@DisplayName("FeedbackController Tests")
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedbackService feedbackService;

    @Autowired
    private ObjectMapper objectMapper;

    private FeedbackEntry feedbackEntry;
    private Ticket ticket;
    private CreateFeedbackRequest createRequest;
    private UpdateFeedbackRequest updateRequest;

    @BeforeEach
    void setUp() {
        setupTestEntities();
    }

    private void setupTestEntities() {
        ticket = createTestTicket(1L, "Test ticket description");
        feedbackEntry = createTestFeedback(1L, ticket, 5, "Excellent service", "customer@example.com");
        
        createRequest = new CreateFeedbackRequest(1L, 5, "Excellent service", "customer@example.com");
        updateRequest = new UpdateFeedbackRequest(4, "Good service");
    }

    private Ticket createTestTicket(Long id, String description) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setDescription(description);
        return ticket;
    }

    private FeedbackEntry createTestFeedback(Long id, Ticket ticket, int rating, String comment, String createdBy) {
        FeedbackEntry feedback = new FeedbackEntry();
        feedback.setId(id);
        feedback.setTicket(ticket);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setCreatedBy(createdBy);
        feedback.setSubmittedAt(Instant.now());
        return feedback;
    }

    @Nested
    @DisplayName("Core CRUD Operation Tests")
    class CoreCrudOperationTests {

        @Test
        @DisplayName("Should create feedback successfully")
        void shouldCreateFeedbackSuccessfully() throws Exception {
            // Given
            when(feedbackService.createFeedback(1L, 5, "Excellent service", "customer@example.com"))
                .thenReturn(feedbackEntry);

            // When & Then
            mockMvc.perform(post("/api/feedback")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").value("Excellent service"))
                    .andExpect(jsonPath("$.createdBy").value("customer@example.com"))
                    .andExpect(jsonPath("$.ticketId").value(1));

            verify(feedbackService).createFeedback(1L, 5, "Excellent service", "customer@example.com");
        }

        @Test
        @DisplayName("Should get feedback by ID successfully")
        void shouldGetFeedbackByIdSuccessfully() throws Exception {
            // Given
            when(feedbackService.findById(1L)).thenReturn(Optional.of(feedbackEntry));

            // When & Then
            mockMvc.perform(get("/api/feedback/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").value("Excellent service"));

            verify(feedbackService).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when feedback not found")
        void shouldReturn404WhenFeedbackNotFound() throws Exception {
            // Given
            when(feedbackService.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/feedback/999"))
                    .andExpect(status().isNotFound());

            verify(feedbackService).findById(999L);
        }

        @Test
        @DisplayName("Should update feedback successfully")
        void shouldUpdateFeedbackSuccessfully() throws Exception {
            // Given
            FeedbackEntry updatedFeedback = createTestFeedback(1L, ticket, 4, "Good service", "customer@example.com");
            when(feedbackService.updateFeedback(1L, 4, "Good service")).thenReturn(updatedFeedback);

            // When & Then
            mockMvc.perform(put("/api/feedback/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.rating").value(4))
                    .andExpect(jsonPath("$.comment").value("Good service"));

            verify(feedbackService).updateFeedback(1L, 4, "Good service");
        }

        @Test
        @DisplayName("Should delete feedback successfully")
        void shouldDeleteFeedbackSuccessfully() throws Exception {
            // Given
            doNothing().when(feedbackService).deleteFeedback(1L);

            // When & Then
            mockMvc.perform(delete("/api/feedback/1"))
                    .andExpect(status().isNoContent());

            verify(feedbackService).deleteFeedback(1L);
        }

        @Test
        @DisplayName("Should get all feedback with pagination")
        void shouldGetAllFeedbackWithPagination() throws Exception {
            // Given
            List<FeedbackEntry> feedbackList = Arrays.asList(feedbackEntry);
            Page<FeedbackEntry> feedbackPage = new PageImpl<>(feedbackList, PageRequest.of(0, 20), 1);
            
            when(feedbackService.findAllFeedback(ArgumentMatchers.any(Pageable.class))).thenReturn(feedbackPage);

            // When & Then
            mockMvc.perform(get("/api/feedback"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(1));

            verify(feedbackService).findAllFeedback(ArgumentMatchers.any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get customer satisfaction metrics")
        void shouldGetCustomerSatisfactionMetrics() throws Exception {
            // Given
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("overallAverageRating", 4.2);
            metrics.put("totalFeedbackCount", 100L);
            metrics.put("highSatisfactionCount", 75L);
            metrics.put("lowSatisfactionCount", 10L);
            metrics.put("highSatisfactionPercentage", 75.0);
            metrics.put("lowSatisfactionPercentage", 10.0);
            metrics.put("ratingDistribution", Map.of(1, 5L, 2, 5L, 3, 15L, 4, 30L, 5, 45L));
            
            when(feedbackService.getCustomerSatisfactionMetrics()).thenReturn(metrics);

            // When & Then
            mockMvc.perform(get("/api/feedback/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.overallAverageRating").value(4.2))
                    .andExpect(jsonPath("$.totalFeedbackCount").value(100))
                    .andExpect(jsonPath("$.highSatisfactionPercentage").value(75.0));

            verify(feedbackService).getCustomerSatisfactionMetrics();
        }

        @Test
        @DisplayName("Should get overall average rating")
        void shouldGetOverallAverageRating() throws Exception {
            // Given
            when(feedbackService.getAverageRating()).thenReturn(4.2);
            when(feedbackService.countAllFeedback()).thenReturn(100L);

            // When & Then
            mockMvc.perform(get("/api/feedback/average-rating"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.averageRating").value(4.2))
                    .andExpect(jsonPath("$.totalFeedbackCount").value(100));

            verify(feedbackService).getAverageRating();
            verify(feedbackService).countAllFeedback();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid path variables")
        void shouldHandleInvalidPathVariables() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/feedback/invalid"))
                    .andExpect(status().isInternalServerError());
        }
    }
} 
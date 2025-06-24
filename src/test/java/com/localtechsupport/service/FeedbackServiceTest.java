package com.localtechsupport.service;

import com.localtechsupport.entity.FeedbackEntry;
import com.localtechsupport.entity.Ticket;
import com.localtechsupport.repository.FeedbackEntryRepository;
import com.localtechsupport.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackService Tests")
class FeedbackServiceTest {

    @Mock
    private FeedbackEntryRepository feedbackRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackEntry feedbackEntry;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        setupTestEntities();
    }

    private void setupTestEntities() {
        ticket = createTestTicket(1L, "Test ticket description");
        feedbackEntry = createTestFeedback(1L, ticket, 5, "Excellent service", "customer@example.com");
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
    @DisplayName("Feedback Creation Tests")
    class FeedbackCreationTests {

        @Test
        @DisplayName("Should create feedback successfully with valid inputs")
        void shouldCreateFeedbackSuccessfullyWithValidInputs() {
            // Given
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(feedbackRepository.save(any(FeedbackEntry.class))).thenReturn(feedbackEntry);

            // When
            FeedbackEntry result = feedbackService.createFeedback(1L, 5, "Excellent service", "customer@example.com");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRating()).isEqualTo(5);
            assertThat(result.getComment()).isEqualTo("Excellent service");
            assertThat(result.getCreatedBy()).isEqualTo("customer@example.com");
            verify(ticketRepository).findById(1L);
            verify(feedbackRepository).save(any(FeedbackEntry.class));
        }

        @Test
        @DisplayName("Should throw exception when ticket not found")
        void shouldThrowExceptionWhenTicketNotFound() {
            // Given
            when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.createFeedback(999L, 5, "Great service", "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ticket not found with ID: 999");
        }

        @Test
        @DisplayName("Should throw exception when rating is invalid")
        void shouldThrowExceptionWhenRatingIsInvalid() {
            // Given
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.createFeedback(1L, 6, "Great service", "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

            assertThatThrownBy(() -> 
                feedbackService.createFeedback(1L, 0, "Great service", "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
        }

        @Test
        @DisplayName("Should throw exception when comment is null or empty")
        void shouldThrowExceptionWhenCommentIsNullOrEmpty() {
            // Given
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.createFeedback(1L, 5, null, "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment cannot be null or empty");

            assertThatThrownBy(() -> 
                feedbackService.createFeedback(1L, 5, "", "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment cannot be null or empty");

            assertThatThrownBy(() -> 
                feedbackService.createFeedback(1L, 5, "   ", "customer@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception when createdBy is null or empty")
        void shouldThrowExceptionWhenCreatedByIsNullOrEmpty() {
            // Given
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.createFeedback(1L, 5, "Great service", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreatedBy cannot be null or empty");

            assertThatThrownBy(() -> 
                feedbackService.createFeedback(1L, 5, "Great service", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreatedBy cannot be null or empty");
        }

        @Test
        @DisplayName("Should trim whitespace from comment and createdBy")
        void shouldTrimWhitespaceFromCommentAndCreatedBy() {
            // Given
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(feedbackRepository.save(any(FeedbackEntry.class))).thenReturn(feedbackEntry);

            // When
            feedbackService.createFeedback(1L, 5, "  Great service  ", "  customer@example.com  ");

            // Then
            verify(feedbackRepository).save(argThat(feedback -> 
                feedback.getComment().equals("Great service") &&
                feedback.getCreatedBy().equals("customer@example.com")
            ));
        }
    }

    @Nested
    @DisplayName("Feedback Update Tests")
    class FeedbackUpdateTests {

        @Test
        @DisplayName("Should update feedback successfully with valid inputs")
        void shouldUpdateFeedbackSuccessfullyWithValidInputs() {
            // Given
            when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedbackEntry));
            when(feedbackRepository.save(any(FeedbackEntry.class))).thenReturn(feedbackEntry);

            // When
            FeedbackEntry result = feedbackService.updateFeedback(1L, 4, "Good service");

            // Then
            assertThat(result).isNotNull();
            verify(feedbackRepository).findById(1L);
            verify(feedbackRepository).save(feedbackEntry);
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedbackEntry));
            when(feedbackRepository.save(any(FeedbackEntry.class))).thenReturn(feedbackEntry);

            String originalComment = feedbackEntry.getComment();

            // When
            feedbackService.updateFeedback(1L, 4, null);

            // Then
            assertThat(feedbackEntry.getRating()).isEqualTo(4);
            assertThat(feedbackEntry.getComment()).isEqualTo(originalComment); // Should remain unchanged
        }

        @Test
        @DisplayName("Should throw exception when feedback not found")
        void shouldThrowExceptionWhenFeedbackNotFound() {
            // Given
            when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.updateFeedback(999L, 4, "Good service"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Feedback not found with ID: 999");
        }

        @Test
        @DisplayName("Should validate rating when updating")
        void shouldValidateRatingWhenUpdating() {
            // Given
            when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedbackEntry));

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.updateFeedback(1L, 6, "Good service"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
        }
    }

    @Nested
    @DisplayName("Feedback Deletion Tests")
    class FeedbackDeletionTests {

        @Test
        @DisplayName("Should delete feedback successfully")
        void shouldDeleteFeedbackSuccessfully() {
            // Given
            when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedbackEntry));
            doNothing().when(feedbackRepository).deleteById(1L);

            // When
            feedbackService.deleteFeedback(1L);

            // Then
            verify(feedbackRepository).findById(1L);
            verify(feedbackRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when feedback not found for deletion")
        void shouldThrowExceptionWhenFeedbackNotFoundForDeletion() {
            // Given
            when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.deleteFeedback(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Feedback not found with ID: 999");
        }
    }

    @Nested
    @DisplayName("Search and Retrieval Tests")
    class SearchAndRetrievalTests {

        @Test
        @DisplayName("Should find feedback by ID successfully")
        void shouldFindFeedbackByIdSuccessfully() {
            // Given
            when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedbackEntry));

            // When
            Optional<FeedbackEntry> result = feedbackService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(feedbackEntry);
            verify(feedbackRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when feedback not found by ID")
        void shouldReturnEmptyWhenFeedbackNotFoundById() {
            // Given
            when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<FeedbackEntry> result = feedbackService.findById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(feedbackRepository).findById(999L);
        }

        @Test
        @DisplayName("Should find all feedback with pagination")
        void shouldFindAllFeedbackWithPagination() {
            // Given
            List<FeedbackEntry> feedbackList = Arrays.asList(feedbackEntry);
            Page<FeedbackEntry> feedbackPage = new PageImpl<>(feedbackList);
            Pageable pageable = PageRequest.of(0, 10);
            
            when(feedbackRepository.findAll(pageable)).thenReturn(feedbackPage);

            // When
            Page<FeedbackEntry> result = feedbackService.findAllFeedback(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(feedbackEntry);
            verify(feedbackRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should find feedback by ticket")
        void shouldFindFeedbackByTicket() {
            // Given
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            List<FeedbackEntry> feedbackList = Arrays.asList(feedbackEntry);
            Page<FeedbackEntry> feedbackPage = new PageImpl<>(feedbackList);
            Pageable pageable = PageRequest.of(0, 10);
            
            when(feedbackRepository.findByTicket(ticket, pageable)).thenReturn(feedbackPage);

            // When
            Page<FeedbackEntry> result = feedbackService.findFeedbackByTicket(1L, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(ticketRepository).findById(1L);
            verify(feedbackRepository).findByTicket(ticket, pageable);
        }

        @Test
        @DisplayName("Should find feedback by rating")
        void shouldFindFeedbackByRating() {
            // Given
            List<FeedbackEntry> feedbackList = Arrays.asList(feedbackEntry);
            Page<FeedbackEntry> feedbackPage = new PageImpl<>(feedbackList);
            Pageable pageable = PageRequest.of(0, 10);
            
            when(feedbackRepository.findByRating(5, pageable)).thenReturn(feedbackPage);

            // When
            Page<FeedbackEntry> result = feedbackService.findFeedbackByRating(5, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(feedbackRepository).findByRating(5, pageable);
        }
    }

    @Nested
    @DisplayName("Statistics and Analytics Tests")
    class StatisticsAndAnalyticsTests {

        @Test
        @DisplayName("Should get average rating")
        void shouldGetAverageRating() {
            // Given
            when(feedbackRepository.getAverageRating()).thenReturn(4.5);

            // When
            Double result = feedbackService.getAverageRating();

            // Then
            assertThat(result).isEqualTo(4.5);
            verify(feedbackRepository).getAverageRating();
        }

        @Test
        @DisplayName("Should get rating distribution")
        void shouldGetRatingDistribution() {
            // Given
            List<Object[]> mockResults = Arrays.asList(
                new Object[]{1, 5L},
                new Object[]{2, 10L},
                new Object[]{3, 15L},
                new Object[]{4, 25L},
                new Object[]{5, 45L}
            );
            when(feedbackRepository.getRatingDistribution()).thenReturn(mockResults);

            // When
            Map<Integer, Long> result = feedbackService.getRatingDistribution();

            // Then
            assertThat(result).hasSize(5);
            assertThat(result.get(1)).isEqualTo(5L);
            assertThat(result.get(2)).isEqualTo(10L);
            assertThat(result.get(3)).isEqualTo(15L);
            assertThat(result.get(4)).isEqualTo(25L);
            assertThat(result.get(5)).isEqualTo(45L);
            verify(feedbackRepository).getRatingDistribution();
        }

        @Test
        @DisplayName("Should get customer satisfaction metrics")
        void shouldGetCustomerSatisfactionMetrics() {
            // Given
            when(feedbackRepository.getAverageRating()).thenReturn(4.2);
            when(feedbackRepository.count()).thenReturn(100L);
            when(feedbackRepository.countByRatingGreaterThanEqual(4)).thenReturn(75L);
            when(feedbackRepository.countByRatingLessThanEqual(2)).thenReturn(10L);
            
            List<Object[]> mockDistribution = Arrays.asList(
                new Object[]{1, 5L},
                new Object[]{2, 5L},
                new Object[]{3, 15L},
                new Object[]{4, 30L},
                new Object[]{5, 45L}
            );
            when(feedbackRepository.getRatingDistribution()).thenReturn(mockDistribution);

            // When
            Map<String, Object> result = feedbackService.getCustomerSatisfactionMetrics();

            // Then
            assertThat(result).containsKey("overallAverageRating");
            assertThat(result).containsKey("totalFeedbackCount");
            assertThat(result).containsKey("highSatisfactionCount");
            assertThat(result).containsKey("lowSatisfactionCount");
            assertThat(result).containsKey("highSatisfactionPercentage");
            assertThat(result).containsKey("lowSatisfactionPercentage");
            
            assertThat(result.get("overallAverageRating")).isEqualTo(4.2);
            assertThat(result.get("totalFeedbackCount")).isEqualTo(100L);
            assertThat(result.get("highSatisfactionPercentage")).isEqualTo(75.0);
            assertThat(result.get("lowSatisfactionPercentage")).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Should get feedback trends")
        void shouldGetFeedbackTrends() {
            // Given
            int days = 30;
            Instant sinceTime = Instant.now().minus(days, ChronoUnit.DAYS);
            
            when(feedbackRepository.countBySubmittedAtAfter(any(Instant.class))).thenReturn(50L);
            when(feedbackRepository.getAverageRatingSince(any(Instant.class))).thenReturn(4.3);
            when(feedbackRepository.countByRatingGreaterThanEqual(4)).thenReturn(40L);
            when(feedbackRepository.countByRatingLessThanEqual(2)).thenReturn(5L);
            when(feedbackRepository.getDailyAverageRatings(any(Instant.class))).thenReturn(Arrays.asList());

            // When
            Map<String, Object> result = feedbackService.getFeedbackTrends(days);

            // Then
            assertThat(result).containsKey("totalFeedback");
            assertThat(result).containsKey("averageRating");
            assertThat(result).containsKey("highSatisfaction");
            assertThat(result).containsKey("lowSatisfaction");
            assertThat(result).containsKey("dailyAverages");
            
            assertThat(result.get("totalFeedback")).isEqualTo(50L);
            assertThat(result.get("averageRating")).isEqualTo(4.3);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate rating range")
        void shouldValidateRatingRange() {
            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.findFeedbackByRatingRange(4, 2, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minimum rating cannot be greater than maximum rating");
        }

        @Test
        @DisplayName("Should validate date range")
        void shouldValidateDateRange() {
            // Given
            Instant now = Instant.now();
            Instant past = now.minus(1, ChronoUnit.DAYS);

            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.findFeedbackByDateRange(now, past, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date cannot be after end date");
        }

        @Test
        @DisplayName("Should validate createdBy parameter")
        void shouldValidateCreatedByParameter() {
            // When & Then
            assertThatThrownBy(() -> 
                feedbackService.findFeedbackByCreatedBy(null, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreatedBy cannot be null or empty");

            assertThatThrownBy(() -> 
                feedbackService.findFeedbackByCreatedBy("", PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreatedBy cannot be null or empty");
        }
    }
} 
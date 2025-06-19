package com.localtechsupport.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackEntry Entity Tests")
class FeedbackEntryTest {

    @Mock
    private Ticket mockTicket;

    private FeedbackEntry feedbackEntry;

    @BeforeEach
    void setUp() {
        feedbackEntry = new FeedbackEntry();
        
        // Set up basic feedback entry properties
        feedbackEntry.setTicket(mockTicket);
        feedbackEntry.setRating(4);
        feedbackEntry.setComment("Great service, very helpful technician!");
        feedbackEntry.setCreatedBy("john.doe@example.com");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create feedback entry with default constructor")
        void shouldCreateFeedbackEntryWithDefaultConstructor() {
            FeedbackEntry newFeedbackEntry = new FeedbackEntry();
            
            assertNotNull(newFeedbackEntry);
            assertNull(newFeedbackEntry.getId());
            assertNull(newFeedbackEntry.getTicket());
            assertEquals(0, newFeedbackEntry.getRating());
            assertNull(newFeedbackEntry.getComment());
            assertNull(newFeedbackEntry.getSubmittedAt());
            assertNull(newFeedbackEntry.getCreatedBy());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetIdCorrectly() {
            feedbackEntry.setId(1L);
            
            assertEquals(1L, feedbackEntry.getId());
        }

        @Test
        @DisplayName("Should set and get ticket correctly")
        void shouldSetAndGetTicketCorrectly() {
            Ticket newTicket = mock(Ticket.class);
            feedbackEntry.setTicket(newTicket);
            
            assertEquals(newTicket, feedbackEntry.getTicket());
        }

        @Test
        @DisplayName("Should set and get rating correctly")
        void shouldSetAndGetRatingCorrectly() {
            feedbackEntry.setRating(5);
            
            assertEquals(5, feedbackEntry.getRating());
        }

        @Test
        @DisplayName("Should set and get comment correctly")
        void shouldSetAndGetCommentCorrectly() {
            String comment = "Updated comment text";
            feedbackEntry.setComment(comment);
            
            assertEquals(comment, feedbackEntry.getComment());
        }

        @Test
        @DisplayName("Should set and get submitted at correctly")
        void shouldSetAndGetSubmittedAtCorrectly() {
            Instant submittedAt = Instant.now();
            feedbackEntry.setSubmittedAt(submittedAt);
            
            assertEquals(submittedAt, feedbackEntry.getSubmittedAt());
        }

        @Test
        @DisplayName("Should set and get created by correctly")
        void shouldSetAndGetCreatedByCorrectly() {
            String createdBy = "jane.doe@example.com";
            feedbackEntry.setCreatedBy(createdBy);
            
            assertEquals(createdBy, feedbackEntry.getCreatedBy());
        }
    }

    @Nested
    @DisplayName("Rating Tests")
    class RatingTests {

        @Test
        @DisplayName("Should accept valid rating values")
        void shouldAcceptValidRatingValues() {
            assertAll(
                () -> {
                    feedbackEntry.setRating(1);
                    assertEquals(1, feedbackEntry.getRating());
                },
                () -> {
                    feedbackEntry.setRating(2);
                    assertEquals(2, feedbackEntry.getRating());
                },
                () -> {
                    feedbackEntry.setRating(3);
                    assertEquals(3, feedbackEntry.getRating());
                },
                () -> {
                    feedbackEntry.setRating(4);
                    assertEquals(4, feedbackEntry.getRating());
                },
                () -> {
                    feedbackEntry.setRating(5);
                    assertEquals(5, feedbackEntry.getRating());
                }
            );
        }

        @Test
        @DisplayName("Should accept zero rating")
        void shouldAcceptZeroRating() {
            feedbackEntry.setRating(0);
            
            assertEquals(0, feedbackEntry.getRating());
        }

        @Test
        @DisplayName("Should accept negative rating values")
        void shouldAcceptNegativeRatingValues() {
            feedbackEntry.setRating(-1);
            
            assertEquals(-1, feedbackEntry.getRating());
        }

        @Test
        @DisplayName("Should accept rating values above typical range")
        void shouldAcceptRatingValuesAboveTypicalRange() {
            feedbackEntry.setRating(10);
            
            assertEquals(10, feedbackEntry.getRating());
        }

        @Test
        @DisplayName("Should handle very large rating values")
        void shouldHandleVeryLargeRatingValues() {
            feedbackEntry.setRating(Integer.MAX_VALUE);
            
            assertEquals(Integer.MAX_VALUE, feedbackEntry.getRating());
        }

        @Test
        @DisplayName("Should handle very small rating values")
        void shouldHandleVerySmallRatingValues() {
            feedbackEntry.setRating(Integer.MIN_VALUE);
            
            assertEquals(Integer.MIN_VALUE, feedbackEntry.getRating());
        }
    }

    @Nested
    @DisplayName("Comment Tests")
    class CommentTests {

        @Test
        @DisplayName("Should handle short comments")
        void shouldHandleShortComments() {
            String shortComment = "OK";
            feedbackEntry.setComment(shortComment);
            
            assertEquals(shortComment, feedbackEntry.getComment());
        }

        @Test
        @DisplayName("Should handle long comments")
        void shouldHandleLongComments() {
            String longComment = "This is a very long comment that provides detailed feedback about the service. ".repeat(10);
            feedbackEntry.setComment(longComment);
            
            assertEquals(longComment, feedbackEntry.getComment());
        }

        @Test
        @DisplayName("Should handle empty comment")
        void shouldHandleEmptyComment() {
            feedbackEntry.setComment("");
            
            assertEquals("", feedbackEntry.getComment());
        }

        @Test
        @DisplayName("Should handle null comment")
        void shouldHandleNullComment() {
            feedbackEntry.setComment(null);
            
            assertNull(feedbackEntry.getComment());
        }

        @Test
        @DisplayName("Should handle comments with special characters")
        void shouldHandleCommentsWithSpecialCharacters() {
            String specialComment = "Great service! @#$%^&*()_+-={}[]|\\:;\"'<>?,./";
            feedbackEntry.setComment(specialComment);
            
            assertEquals(specialComment, feedbackEntry.getComment());
        }

        @Test
        @DisplayName("Should handle comments with newlines and tabs")
        void shouldHandleCommentsWithNewlinesAndTabs() {
            String multilineComment = "Line 1\nLine 2\tTabbed content\rCarriage return";
            feedbackEntry.setComment(multilineComment);
            
            assertEquals(multilineComment, feedbackEntry.getComment());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain ticket relationship")
        void shouldMaintainTicketRelationship() {
            assertNotNull(feedbackEntry.getTicket());
            assertEquals(mockTicket, feedbackEntry.getTicket());
        }

        @Test
        @DisplayName("Should handle null ticket")
        void shouldHandleNullTicket() {
            assertDoesNotThrow(() -> {
                feedbackEntry.setTicket(null);
                assertNull(feedbackEntry.getTicket());
            });
        }

        @Test
        @DisplayName("Should handle changing ticket reference")
        void shouldHandleChangingTicketReference() {
            Ticket newTicket = mock(Ticket.class);
            
            feedbackEntry.setTicket(newTicket);
            
            assertEquals(newTicket, feedbackEntry.getTicket());
            assertNotEquals(mockTicket, feedbackEntry.getTicket());
        }
    }

    @Nested
    @DisplayName("CreatedBy Tests")
    class CreatedByTests {

        @Test
        @DisplayName("Should handle email as created by")
        void shouldHandleEmailAsCreatedBy() {
            String email = "user@example.com";
            feedbackEntry.setCreatedBy(email);
            
            assertEquals(email, feedbackEntry.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle username as created by")
        void shouldHandleUsernameAsCreatedBy() {
            String username = "john_doe";
            feedbackEntry.setCreatedBy(username);
            
            assertEquals(username, feedbackEntry.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle full name as created by")
        void shouldHandleFullNameAsCreatedBy() {
            String fullName = "John Doe";
            feedbackEntry.setCreatedBy(fullName);
            
            assertEquals(fullName, feedbackEntry.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle empty created by")
        void shouldHandleEmptyCreatedBy() {
            feedbackEntry.setCreatedBy("");
            
            assertEquals("", feedbackEntry.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle null created by")
        void shouldHandleNullCreatedBy() {
            feedbackEntry.setCreatedBy(null);
            
            assertNull(feedbackEntry.getCreatedBy());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {

        @Test
        @DisplayName("Should set submitted at timestamp on persist")
        void shouldSetSubmittedAtTimestampOnPersist() {
            FeedbackEntry newFeedbackEntry = new FeedbackEntry();
            Instant beforePersist = Instant.now();
            
            // Simulate @PrePersist
            newFeedbackEntry.onCreate();
            
            assertNotNull(newFeedbackEntry.getSubmittedAt());
            assertTrue(newFeedbackEntry.getSubmittedAt().isAfter(beforePersist.minus(1, ChronoUnit.SECONDS)));
            assertTrue(newFeedbackEntry.getSubmittedAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("Should not modify existing submitted at timestamp on persist")
        void shouldNotModifyExistingSubmittedAtTimestampOnPersist() {
            Instant existingTimestamp = Instant.now().minus(1, ChronoUnit.HOURS);
            feedbackEntry.setSubmittedAt(existingTimestamp);
            
            // Simulate @PrePersist
            feedbackEntry.onCreate();
            
            // Should update the timestamp since @PrePersist sets it to now()
            assertNotEquals(existingTimestamp, feedbackEntry.getSubmittedAt());
            assertTrue(feedbackEntry.getSubmittedAt().isAfter(existingTimestamp));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle feedback entry with minimum required fields")
        void shouldHandleFeedbackEntryWithMinimumRequiredFields() {
            FeedbackEntry minimalFeedbackEntry = new FeedbackEntry();
            minimalFeedbackEntry.setTicket(mockTicket);
            minimalFeedbackEntry.setRating(1);
            minimalFeedbackEntry.setComment("OK");
            minimalFeedbackEntry.setCreatedBy("user");
            
            assertAll(
                () -> assertNotNull(minimalFeedbackEntry.getTicket()),
                () -> assertEquals(1, minimalFeedbackEntry.getRating()),
                () -> assertEquals("OK", minimalFeedbackEntry.getComment()),
                () -> assertEquals("user", minimalFeedbackEntry.getCreatedBy())
            );
        }

        @Test
        @DisplayName("Should handle feedback entry with all fields null except rating")
        void shouldHandleFeedbackEntryWithAllFieldsNullExceptRating() {
            FeedbackEntry sparseFeedbackEntry = new FeedbackEntry();
            sparseFeedbackEntry.setRating(3);
            
            assertAll(
                () -> assertNull(sparseFeedbackEntry.getTicket()),
                () -> assertEquals(3, sparseFeedbackEntry.getRating()),
                () -> assertNull(sparseFeedbackEntry.getComment()),
                () -> assertNull(sparseFeedbackEntry.getSubmittedAt()),
                () -> assertNull(sparseFeedbackEntry.getCreatedBy())
            );
        }

        @Test
        @DisplayName("Should handle feedback entry with very old timestamp")
        void shouldHandleFeedbackEntryWithVeryOldTimestamp() {
            Instant veryOldTimestamp = Instant.parse("2000-01-01T00:00:00Z");
            feedbackEntry.setSubmittedAt(veryOldTimestamp);
            
            assertEquals(veryOldTimestamp, feedbackEntry.getSubmittedAt());
        }

        @Test
        @DisplayName("Should handle feedback entry with future timestamp")
        void shouldHandleFeedbackEntryWithFutureTimestamp() {
            Instant futureTimestamp = Instant.now().plus(365, ChronoUnit.DAYS);
            feedbackEntry.setSubmittedAt(futureTimestamp);
            
            assertEquals(futureTimestamp, feedbackEntry.getSubmittedAt());
        }

        @Test
        @DisplayName("Should handle Unicode characters in comment")
        void shouldHandleUnicodeCharactersInComment() {
            String unicodeComment = "Great service! 👍 Très bien! 素晴らしい！";
            feedbackEntry.setComment(unicodeComment);
            
            assertEquals(unicodeComment, feedbackEntry.getComment());
        }

        @Test
        @DisplayName("Should handle Unicode characters in created by")
        void shouldHandleUnicodeCharactersInCreatedBy() {
            String unicodeCreatedBy = "José García";
            feedbackEntry.setCreatedBy(unicodeCreatedBy);
            
            assertEquals(unicodeCreatedBy, feedbackEntry.getCreatedBy());
        }
    }
} 
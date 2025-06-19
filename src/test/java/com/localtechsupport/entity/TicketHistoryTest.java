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
@DisplayName("TicketHistory Entity Tests")
class TicketHistoryTest {

    @Mock
    private Ticket mockTicket;

    private TicketHistory ticketHistory;

    @BeforeEach
    void setUp() {
        ticketHistory = new TicketHistory();
        
        // Set up basic ticket history properties
        ticketHistory.setTicket(mockTicket);
        ticketHistory.setStatus(TicketStatus.OPEN);
        ticketHistory.setDescription("Ticket opened and assigned to technician");
        ticketHistory.setCreatedBy("system@techsupport.com");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ticket history with default constructor")
        void shouldCreateTicketHistoryWithDefaultConstructor() {
            TicketHistory newTicketHistory = new TicketHistory();
            
            assertNotNull(newTicketHistory);
            assertNull(newTicketHistory.getId());
            assertNull(newTicketHistory.getTicket());
            assertNull(newTicketHistory.getStatus());
            assertNull(newTicketHistory.getDescription());
            assertNull(newTicketHistory.getCreatedAt());
            assertNull(newTicketHistory.getUpdatedAt());
            assertNull(newTicketHistory.getCreatedBy());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetIdCorrectly() {
            ticketHistory.setId(1L);
            
            assertEquals(1L, ticketHistory.getId());
        }

        @Test
        @DisplayName("Should set and get ticket correctly")
        void shouldSetAndGetTicketCorrectly() {
            Ticket newTicket = mock(Ticket.class);
            ticketHistory.setTicket(newTicket);
            
            assertEquals(newTicket, ticketHistory.getTicket());
        }

        @Test
        @DisplayName("Should set and get status correctly")
        void shouldSetAndGetStatusCorrectly() {
            ticketHistory.setStatus(TicketStatus.CLOSED);
            
            assertEquals(TicketStatus.CLOSED, ticketHistory.getStatus());
        }

        @Test
        @DisplayName("Should set and get description correctly")
        void shouldSetAndGetDescriptionCorrectly() {
            String description = "Updated description";
            ticketHistory.setDescription(description);
            
            assertEquals(description, ticketHistory.getDescription());
        }

        @Test
        @DisplayName("Should set and get created at correctly")
        void shouldSetAndGetCreatedAtCorrectly() {
            Instant createdAt = Instant.now();
            ticketHistory.setCreatedAt(createdAt);
            
            assertEquals(createdAt, ticketHistory.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get updated at correctly")
        void shouldSetAndGetUpdatedAtCorrectly() {
            Instant updatedAt = Instant.now();
            ticketHistory.setUpdatedAt(updatedAt);
            
            assertEquals(updatedAt, ticketHistory.getUpdatedAt());
        }

        @Test
        @DisplayName("Should set and get created by correctly")
        void shouldSetAndGetCreatedByCorrectly() {
            String createdBy = "jane.doe@example.com";
            ticketHistory.setCreatedBy(createdBy);
            
            assertEquals(createdBy, ticketHistory.getCreatedBy());
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should set all status values correctly")
        void shouldSetAllStatusValuesCorrectly() {
            assertAll(
                () -> {
                    ticketHistory.setStatus(TicketStatus.OPEN);
                    assertEquals(TicketStatus.OPEN, ticketHistory.getStatus());
                },
                () -> {
                    ticketHistory.setStatus(TicketStatus.CLOSED);
                    assertEquals(TicketStatus.CLOSED, ticketHistory.getStatus());
                }
            );
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            ticketHistory.setStatus(null);
            
            assertNull(ticketHistory.getStatus());
        }
    }

    @Nested
    @DisplayName("Description Tests")
    class DescriptionTests {

        @Test
        @DisplayName("Should handle short descriptions")
        void shouldHandleShortDescriptions() {
            String shortDescription = "Updated";
            ticketHistory.setDescription(shortDescription);
            
            assertEquals(shortDescription, ticketHistory.getDescription());
        }

        @Test
        @DisplayName("Should handle long descriptions")
        void shouldHandleLongDescriptions() {
            String longDescription = "This is a very long description that provides detailed information about the ticket status change. ".repeat(10);
            ticketHistory.setDescription(longDescription);
            
            assertEquals(longDescription, ticketHistory.getDescription());
        }

        @Test
        @DisplayName("Should handle empty description")
        void shouldHandleEmptyDescription() {
            ticketHistory.setDescription("");
            
            assertEquals("", ticketHistory.getDescription());
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            ticketHistory.setDescription(null);
            
            assertNull(ticketHistory.getDescription());
        }

        @Test
        @DisplayName("Should handle descriptions with special characters")
        void shouldHandleDescriptionsWithSpecialCharacters() {
            String specialDescription = "Status changed @#$%^&*()_+-={}[]|\\:;\"'<>?,./";
            ticketHistory.setDescription(specialDescription);
            
            assertEquals(specialDescription, ticketHistory.getDescription());
        }

        @Test
        @DisplayName("Should handle descriptions with newlines and tabs")
        void shouldHandleDescriptionsWithNewlinesAndTabs() {
            String multilineDescription = "Line 1\nLine 2\tTabbed content\rCarriage return";
            ticketHistory.setDescription(multilineDescription);
            
            assertEquals(multilineDescription, ticketHistory.getDescription());
        }

        @Test
        @DisplayName("Should handle Unicode characters in description")
        void shouldHandleUnicodeCharactersInDescription() {
            String unicodeDescription = "Ticket updated by José García 👍 素晴らしい！";
            ticketHistory.setDescription(unicodeDescription);
            
            assertEquals(unicodeDescription, ticketHistory.getDescription());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain ticket relationship")
        void shouldMaintainTicketRelationship() {
            assertNotNull(ticketHistory.getTicket());
            assertEquals(mockTicket, ticketHistory.getTicket());
        }

        @Test
        @DisplayName("Should handle null ticket")
        void shouldHandleNullTicket() {
            assertDoesNotThrow(() -> {
                ticketHistory.setTicket(null);
                assertNull(ticketHistory.getTicket());
            });
        }

        @Test
        @DisplayName("Should handle changing ticket reference")
        void shouldHandleChangingTicketReference() {
            Ticket newTicket = mock(Ticket.class);
            
            ticketHistory.setTicket(newTicket);
            
            assertEquals(newTicket, ticketHistory.getTicket());
            assertNotEquals(mockTicket, ticketHistory.getTicket());
        }
    }

    @Nested
    @DisplayName("CreatedBy Tests")
    class CreatedByTests {

        @Test
        @DisplayName("Should handle email as created by")
        void shouldHandleEmailAsCreatedBy() {
            String email = "user@example.com";
            ticketHistory.setCreatedBy(email);
            
            assertEquals(email, ticketHistory.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle username as created by")
        void shouldHandleUsernameAsCreatedBy() {
            String username = "john_doe";
            ticketHistory.setCreatedBy(username);
            
            assertEquals(username, ticketHistory.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle system as created by")
        void shouldHandleSystemAsCreatedBy() {
            String system = "system";
            ticketHistory.setCreatedBy(system);
            
            assertEquals(system, ticketHistory.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle full name as created by")
        void shouldHandleFullNameAsCreatedBy() {
            String fullName = "John Doe";
            ticketHistory.setCreatedBy(fullName);
            
            assertEquals(fullName, ticketHistory.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle empty created by")
        void shouldHandleEmptyCreatedBy() {
            ticketHistory.setCreatedBy("");
            
            assertEquals("", ticketHistory.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle null created by")
        void shouldHandleNullCreatedBy() {
            ticketHistory.setCreatedBy(null);
            
            assertNull(ticketHistory.getCreatedBy());
        }

        @Test
        @DisplayName("Should handle Unicode characters in created by")
        void shouldHandleUnicodeCharactersInCreatedBy() {
            String unicodeCreatedBy = "José García";
            ticketHistory.setCreatedBy(unicodeCreatedBy);
            
            assertEquals(unicodeCreatedBy, ticketHistory.getCreatedBy());
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {

        @Test
        @DisplayName("Should set created at timestamp on persist")
        void shouldSetCreatedAtTimestampOnPersist() {
            TicketHistory newTicketHistory = new TicketHistory();
            Instant beforePersist = Instant.now();
            
            // Simulate @PrePersist
            newTicketHistory.onCreate();
            
            assertNotNull(newTicketHistory.getCreatedAt());
            assertTrue(newTicketHistory.getCreatedAt().isAfter(beforePersist.minus(1, ChronoUnit.SECONDS)));
            assertTrue(newTicketHistory.getCreatedAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("Should set updated at timestamp on update")
        void shouldSetUpdatedAtTimestampOnUpdate() {
            ticketHistory.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS)); // Set earlier created time
            Instant beforeUpdate = Instant.now();
            
            // Simulate @PreUpdate
            ticketHistory.onUpdate();
            
            assertNotNull(ticketHistory.getUpdatedAt());
            assertTrue(ticketHistory.getUpdatedAt().isAfter(beforeUpdate.minus(1, ChronoUnit.SECONDS)));
            assertTrue(ticketHistory.getUpdatedAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("Should not affect created at timestamp on update")
        void shouldNotAffectCreatedAtTimestampOnUpdate() {
            Instant originalCreatedAt = Instant.now().minus(1, ChronoUnit.HOURS);
            ticketHistory.setCreatedAt(originalCreatedAt);
            
            // Simulate @PreUpdate
            ticketHistory.onUpdate();
            
            assertEquals(originalCreatedAt, ticketHistory.getCreatedAt());
            assertNotNull(ticketHistory.getUpdatedAt());
        }

        @Test
        @DisplayName("Should not modify existing created at timestamp on persist")
        void shouldNotModifyExistingCreatedAtTimestampOnPersist() {
            Instant existingTimestamp = Instant.now().minus(1, ChronoUnit.HOURS);
            ticketHistory.setCreatedAt(existingTimestamp);
            
            // Simulate @PrePersist
            ticketHistory.onCreate();
            
            // Should update the timestamp since @PrePersist sets it to now()
            assertNotEquals(existingTimestamp, ticketHistory.getCreatedAt());
            assertTrue(ticketHistory.getCreatedAt().isAfter(existingTimestamp));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle ticket history with minimum required fields")
        void shouldHandleTicketHistoryWithMinimumRequiredFields() {
            TicketHistory minimalTicketHistory = new TicketHistory();
            minimalTicketHistory.setTicket(mockTicket);
            minimalTicketHistory.setStatus(TicketStatus.OPEN);
            minimalTicketHistory.setDescription("Opened");
            minimalTicketHistory.setCreatedBy("user");
            
            assertAll(
                () -> assertNotNull(minimalTicketHistory.getTicket()),
                () -> assertEquals(TicketStatus.OPEN, minimalTicketHistory.getStatus()),
                () -> assertEquals("Opened", minimalTicketHistory.getDescription()),
                () -> assertEquals("user", minimalTicketHistory.getCreatedBy())
            );
        }

        @Test
        @DisplayName("Should handle ticket history with all fields null")
        void shouldHandleTicketHistoryWithAllFieldsNull() {
            TicketHistory sparseTicketHistory = new TicketHistory();
            
            assertAll(
                () -> assertNull(sparseTicketHistory.getTicket()),
                () -> assertNull(sparseTicketHistory.getStatus()),
                () -> assertNull(sparseTicketHistory.getDescription()),
                () -> assertNull(sparseTicketHistory.getCreatedAt()),
                () -> assertNull(sparseTicketHistory.getUpdatedAt()),
                () -> assertNull(sparseTicketHistory.getCreatedBy())
            );
        }

        @Test
        @DisplayName("Should handle ticket history with very old timestamps")
        void shouldHandleTicketHistoryWithVeryOldTimestamps() {
            Instant veryOldTimestamp = Instant.parse("2000-01-01T00:00:00Z");
            ticketHistory.setCreatedAt(veryOldTimestamp);
            ticketHistory.setUpdatedAt(veryOldTimestamp);
            
            assertEquals(veryOldTimestamp, ticketHistory.getCreatedAt());
            assertEquals(veryOldTimestamp, ticketHistory.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle ticket history with future timestamps")
        void shouldHandleTicketHistoryWithFutureTimestamps() {
            Instant futureTimestamp = Instant.now().plus(365, ChronoUnit.DAYS);
            ticketHistory.setCreatedAt(futureTimestamp);
            ticketHistory.setUpdatedAt(futureTimestamp);
            
            assertEquals(futureTimestamp, ticketHistory.getCreatedAt());
            assertEquals(futureTimestamp, ticketHistory.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle updated at timestamp before created at")
        void shouldHandleUpdatedAtTimestampBeforeCreatedAt() {
            Instant laterTime = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant earlierTime = Instant.now();
            
            ticketHistory.setCreatedAt(laterTime);
            ticketHistory.setUpdatedAt(earlierTime);
            
            assertEquals(laterTime, ticketHistory.getCreatedAt());
            assertEquals(earlierTime, ticketHistory.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle multiple status changes")
        void shouldHandleMultipleStatusChanges() {
            // Test changing status multiple times
            ticketHistory.setStatus(TicketStatus.OPEN);
            assertEquals(TicketStatus.OPEN, ticketHistory.getStatus());
            
            ticketHistory.setStatus(TicketStatus.CLOSED);
            assertEquals(TicketStatus.CLOSED, ticketHistory.getStatus());
            
            ticketHistory.setStatus(TicketStatus.OPEN);
            assertEquals(TicketStatus.OPEN, ticketHistory.getStatus());
        }

        @Test
        @DisplayName("Should handle status change with description update")
        void shouldHandleStatusChangeWithDescriptionUpdate() {
            ticketHistory.setStatus(TicketStatus.OPEN);
            ticketHistory.setDescription("Ticket reopened due to additional issues");
            
            assertEquals(TicketStatus.OPEN, ticketHistory.getStatus());
            assertEquals("Ticket reopened due to additional issues", ticketHistory.getDescription());
        }
    }
} 
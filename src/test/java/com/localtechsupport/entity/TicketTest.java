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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ticket Entity Tests")
class TicketTest {

    @Mock
    private Client mockClient;
    
    @Mock
    private ServiceType mockServiceType;
    
    @Mock
    private Technician mockTechnician;

    private Ticket ticket;
    private Instant futureDate;
    private Instant pastDate;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        
        // Set up basic ticket properties
        ticket.setClient(mockClient);
        ticket.setServiceType(mockServiceType);
        ticket.setDescription("Test ticket description");
        ticket.setDueAt(futureDate);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create ticket with default constructor")
        void shouldCreateTicketWithDefaultConstructor() {
            Ticket newTicket = new Ticket();
            
            assertNotNull(newTicket);
            assertNull(newTicket.getId());
            assertEquals(TicketStatus.OPEN, newTicket.getStatus());
            assertNotNull(newTicket.getHistory());
            assertNotNull(newTicket.getFeedbackEntries());
            assertTrue(newTicket.getHistory().isEmpty());
            assertTrue(newTicket.getFeedbackEntries().isEmpty());
        }

        @Test
        @DisplayName("Should create ticket with parameterized constructor")
        void shouldCreateTicketWithParameterizedConstructor() {
            String description = "Hardware issue with printer";
            
            Ticket newTicket = new Ticket(mockClient, mockServiceType, description, futureDate);
            
            assertNotNull(newTicket);
            assertEquals(mockClient, newTicket.getClient());
            assertEquals(mockServiceType, newTicket.getServiceType());
            assertEquals(description, newTicket.getDescription());
            assertEquals(futureDate, newTicket.getDueAt());
            assertEquals(TicketStatus.OPEN, newTicket.getStatus());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterAndSetterTests {

        @Test
        @DisplayName("Should set and get id correctly")
        void shouldSetAndGetIdCorrectly() {
            ticket.setId(1L);
            
            assertEquals(1L, ticket.getId());
        }

        @Test
        @DisplayName("Should set and get client correctly")
        void shouldSetAndGetClientCorrectly() {
            Client newClient = mock(Client.class);
            ticket.setClient(newClient);
            
            assertEquals(newClient, ticket.getClient());
        }

        @Test
        @DisplayName("Should set and get service type correctly")
        void shouldSetAndGetServiceTypeCorrectly() {
            ServiceType newServiceType = mock(ServiceType.class);
            ticket.setServiceType(newServiceType);
            
            assertEquals(newServiceType, ticket.getServiceType());
        }

        @Test
        @DisplayName("Should set and get description correctly")
        void shouldSetAndGetDescriptionCorrectly() {
            String description = "Updated description";
            ticket.setDescription(description);
            
            assertEquals(description, ticket.getDescription());
        }

        @Test
        @DisplayName("Should set and get created at correctly")
        void shouldSetAndGetCreatedAtCorrectly() {
            Instant createdAt = Instant.now();
            ticket.setCreatedAt(createdAt);
            
            assertEquals(createdAt, ticket.getCreatedAt());
        }

        @Test
        @DisplayName("Should set and get due at correctly")
        void shouldSetAndGetDueAtCorrectly() {
            Instant dueAt = Instant.now().plus(2, ChronoUnit.DAYS);
            ticket.setDueAt(dueAt);
            
            assertEquals(dueAt, ticket.getDueAt());
        }

        @Test
        @DisplayName("Should set and get status correctly")
        void shouldSetAndGetStatusCorrectly() {
            ticket.setStatus(TicketStatus.CLOSED);
            
            assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        }

        @Test
        @DisplayName("Should set and get assigned technician correctly")
        void shouldSetAndGetAssignedTechnicianCorrectly() {
            ticket.setAssignedTechnician(mockTechnician);
            
            assertEquals(mockTechnician, ticket.getAssignedTechnician());
        }

        @Test
        @DisplayName("Should set and get history correctly")
        void shouldSetAndGetHistoryCorrectly() {
            List<TicketHistory> history = new ArrayList<>();
            TicketHistory historyEntry = mock(TicketHistory.class);
            history.add(historyEntry);
            
            ticket.setHistory(history);
            
            assertEquals(history, ticket.getHistory());
            assertEquals(1, ticket.getHistory().size());
        }

        @Test
        @DisplayName("Should set and get feedback entries correctly")
        void shouldSetAndGetFeedbackEntriesCorrectly() {
            List<FeedbackEntry> feedbackEntries = new ArrayList<>();
            FeedbackEntry feedbackEntry = mock(FeedbackEntry.class);
            feedbackEntries.add(feedbackEntry);
            
            ticket.setFeedbackEntries(feedbackEntries);
            
            assertEquals(feedbackEntries, ticket.getFeedbackEntries());
            assertEquals(1, ticket.getFeedbackEntries().size());
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should have OPEN as default status")
        void shouldHaveOpenAsDefaultStatus() {
            Ticket newTicket = new Ticket();
            
            assertEquals(TicketStatus.OPEN, newTicket.getStatus());
        }

        @Test
        @DisplayName("Should set all status values correctly")
        void shouldSetAllStatusValuesCorrectly() {
            assertAll(
                () -> {
                    ticket.setStatus(TicketStatus.OPEN);
                    assertEquals(TicketStatus.OPEN, ticket.getStatus());
                },
                () -> {
                    ticket.setStatus(TicketStatus.CLOSED);
                    assertEquals(TicketStatus.CLOSED, ticket.getStatus());
                }
            );
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should return true when ticket is open")
        void shouldReturnTrueWhenTicketIsOpen() {
            ticket.setStatus(TicketStatus.OPEN);
            
            assertTrue(ticket.isOpen());
        }

        @Test
        @DisplayName("Should return false when ticket is closed")
        void shouldReturnFalseWhenTicketIsClosed() {
            ticket.setStatus(TicketStatus.CLOSED);
            
            assertFalse(ticket.isOpen());
        }

        @Test
        @DisplayName("Should return true when ticket is overdue")
        void shouldReturnTrueWhenTicketIsOverdue() {
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setDueAt(pastDate);
            
            assertTrue(ticket.isOverdue());
        }

        @Test
        @DisplayName("Should return false when ticket is not overdue")
        void shouldReturnFalseWhenTicketIsNotOverdue() {
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setDueAt(futureDate);
            
            assertFalse(ticket.isOverdue());
        }

        @Test
        @DisplayName("Should return false when closed ticket is past due date")
        void shouldReturnFalseWhenClosedTicketIsPastDueDate() {
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setDueAt(pastDate);
            
            assertFalse(ticket.isOverdue());
        }

        @Test
        @DisplayName("Should add history entry correctly")
        void shouldAddHistoryEntryCorrectly() {
            TicketHistory historyEntry = mock(TicketHistory.class);
            
            ticket.addHistory(historyEntry);
            
            verify(historyEntry).setTicket(ticket);
            assertTrue(ticket.getHistory().contains(historyEntry));
            assertEquals(1, ticket.getHistory().size());
        }

        @Test
        @DisplayName("Should add multiple history entries correctly")
        void shouldAddMultipleHistoryEntriesCorrectly() {
            TicketHistory historyEntry1 = mock(TicketHistory.class);
            TicketHistory historyEntry2 = mock(TicketHistory.class);
            
            ticket.addHistory(historyEntry1);
            ticket.addHistory(historyEntry2);
            
            verify(historyEntry1).setTicket(ticket);
            verify(historyEntry2).setTicket(ticket);
            assertEquals(2, ticket.getHistory().size());
            assertTrue(ticket.getHistory().contains(historyEntry1));
            assertTrue(ticket.getHistory().contains(historyEntry2));
        }

        @Test
        @DisplayName("Should return latest feedback when feedback entries exist")
        void shouldReturnLatestFeedbackWhenFeedbackEntriesExist() {
            FeedbackEntry olderFeedback = mock(FeedbackEntry.class);
            FeedbackEntry newerFeedback = mock(FeedbackEntry.class);
            
            Instant olderTime = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant newerTime = Instant.now();
            
            when(olderFeedback.getSubmittedAt()).thenReturn(olderTime);
            when(newerFeedback.getSubmittedAt()).thenReturn(newerTime);
            
            ticket.getFeedbackEntries().add(olderFeedback);
            ticket.getFeedbackEntries().add(newerFeedback);
            
            Optional<FeedbackEntry> latest = ticket.latestFeedback();
            
            assertTrue(latest.isPresent());
            assertEquals(newerFeedback, latest.get());
        }

        @Test
        @DisplayName("Should return empty optional when no feedback entries exist")
        void shouldReturnEmptyOptionalWhenNoFeedbackEntriesExist() {
            Optional<FeedbackEntry> latest = ticket.latestFeedback();
            
            assertFalse(latest.isPresent());
        }

        @Test
        @DisplayName("Should return single feedback entry when only one exists")
        void shouldReturnSingleFeedbackEntryWhenOnlyOneExists() {
            FeedbackEntry singleFeedback = mock(FeedbackEntry.class);
            lenient().when(singleFeedback.getSubmittedAt()).thenReturn(Instant.now());
            
            ticket.getFeedbackEntries().add(singleFeedback);
            
            Optional<FeedbackEntry> latest = ticket.latestFeedback();
            
            assertTrue(latest.isPresent());
            assertEquals(singleFeedback, latest.get());
        }
    }

    @Nested
    @DisplayName("Collection Initialization Tests")
    class CollectionInitializationTests {

        @Test
        @DisplayName("Should initialize history collection")
        void shouldInitializeHistoryCollection() {
            Ticket newTicket = new Ticket();
            
            assertNotNull(newTicket.getHistory());
            assertTrue(newTicket.getHistory().isEmpty());
            assertTrue(newTicket.getHistory() instanceof ArrayList);
        }

        @Test
        @DisplayName("Should initialize feedback entries collection")
        void shouldInitializeFeedbackEntriesCollection() {
            Ticket newTicket = new Ticket();
            
            assertNotNull(newTicket.getFeedbackEntries());
            assertTrue(newTicket.getFeedbackEntries().isEmpty());
            assertTrue(newTicket.getFeedbackEntries() instanceof ArrayList);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null history collection gracefully")
        void shouldHandleNullHistoryCollectionGracefully() {
            ticket.setHistory(null);
            
            assertDoesNotThrow(() -> {
                TicketHistory historyEntry = mock(TicketHistory.class);
                ticket.addHistory(historyEntry);
            });
        }

        @Test
        @DisplayName("Should handle null feedback entries collection gracefully")
        void shouldHandleNullFeedbackEntriesCollectionGracefully() {
            ticket.setFeedbackEntries(null);
            
            assertDoesNotThrow(() -> {
                Optional<FeedbackEntry> latest = ticket.latestFeedback();
                assertFalse(latest.isPresent());
            });
        }

        @Test
        @DisplayName("Should handle null due date in overdue check")
        void shouldHandleNullDueDateInOverdueCheck() {
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setDueAt(null);
            
            assertDoesNotThrow(() -> {
                boolean isOverdue = ticket.isOverdue();
                assertFalse(isOverdue);
            });
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            assertDoesNotThrow(() -> {
                ticket.setDescription(null);
                assertNull(ticket.getDescription());
            });
        }
    }

    @Nested
    @DisplayName("Lifecycle Hook Tests")
    class LifecycleHookTests {

        @Test
        @DisplayName("Should set created at timestamp on persist")
        void shouldSetCreatedAtTimestampOnPersist() {
            Ticket newTicket = new Ticket();
            Instant beforePersist = Instant.now();
            
            // Simulate @PrePersist
            newTicket.onCreate(); // This method should be protected, but we're testing the behavior
            
            assertNotNull(newTicket.getCreatedAt());
            assertTrue(newTicket.getCreatedAt().isAfter(beforePersist.minus(1, ChronoUnit.SECONDS)));
            assertTrue(newTicket.getCreatedAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
        }
    }
}

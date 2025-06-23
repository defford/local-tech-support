package com.localtechsupport.repository;

import com.localtechsupport.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("TicketHistoryRepository Tests")
class TicketHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    private Ticket testTicket1;
    private Ticket testTicket2;

    @BeforeEach
    void setUp() {
        ticketHistoryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Client createTestClient(String email) {
        Client client = new Client();
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setEmail(email);
        client.setStatus(Client.ClientStatus.ACTIVE);
        client.setPhone("555-0123");
        client.setAddress("123 Test St");
        client.setNotes("Test notes");
        return client;
    }

    private Technician createTestTechnician(String email) {
        Technician technician = new Technician();
        technician.setFullName("Test Technician");
        technician.setEmail(email);
        technician.setStatus(TechnicianStatus.ACTIVE);
        return technician;
    }

    private Ticket createTestTicket(String description, Client client, Technician technician) {
        Ticket ticket = new Ticket();
        ticket.setDescription(description);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setServiceType(ServiceType.HARDWARE);
        ticket.setClient(client);
        ticket.setAssignedTechnician(technician);
        ticket.setDueAt(Instant.now().plus(1, ChronoUnit.DAYS));
        return ticket;
    }

    private TicketHistory createTestTicketHistory(Ticket ticket, TicketStatus status, String description, String createdBy) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus(status);
        history.setDescription(description);
        history.setCreatedBy(createdBy);
        history.setCreatedAt(Instant.now());
        history.setUpdatedAt(Instant.now());
        return history;
    }

    private void setupBasicTestData() {
        Client client1 = createTestClient("client1@example.com");
        Technician tech1 = createTestTechnician("tech1@example.com");
        entityManager.persistAndFlush(client1);
        entityManager.persistAndFlush(tech1);

        testTicket1 = createTestTicket("Test Ticket 1", client1, tech1);
        testTicket2 = createTestTicket("Test Ticket 2", client1, tech1);
        entityManager.persistAndFlush(testTicket1);
        entityManager.persistAndFlush(testTicket2);

        TicketHistory history1 = createTestTicketHistory(testTicket1, TicketStatus.OPEN, "Ticket created", "admin");
        TicketHistory history2 = createTestTicketHistory(testTicket1, TicketStatus.CLOSED, "Ticket resolved", "tech1@example.com");
        TicketHistory history3 = createTestTicketHistory(testTicket2, TicketStatus.OPEN, "Second ticket created", "admin");

        entityManager.persistAndFlush(history1);
        entityManager.persistAndFlush(history2);
        entityManager.persistAndFlush(history3);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Standard CRUD Operations")
    class StandardCrudTests {

        @Test
        @DisplayName("Should save new ticket history successfully")
        void shouldSaveNewTicketHistorySuccessfully() {
            setupBasicTestData();
            TicketHistory history = createTestTicketHistory(testTicket1, TicketStatus.OPEN, "Test description", "test@example.com");

            TicketHistory savedHistory = ticketHistoryRepository.save(history);

            assertThat(savedHistory).isNotNull();
            assertThat(savedHistory.getId()).isNotNull();
            assertThat(savedHistory.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("Should find all ticket histories")
        void shouldFindAllTicketHistories() {
            setupBasicTestData();

            List<TicketHistory> histories = ticketHistoryRepository.findAll();

            assertThat(histories).hasSize(3);
        }

        @Test
        @DisplayName("Should count all ticket histories correctly")
        void shouldCountAllTicketHistoriesCorrectly() {
            setupBasicTestData();

            long count = ticketHistoryRepository.count();

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Ticket-Based Query Tests")
    class TicketBasedTests {

        @Test
        @DisplayName("Should find history by ticket")
        void shouldFindHistoryByTicket() {
            setupBasicTestData();

            List<TicketHistory> result = ticketHistoryRepository.findByTicket(testTicket1);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find latest history entry for ticket")
        void shouldFindLatestHistoryEntryForTicket() {
            setupBasicTestData();

            Optional<TicketHistory> result = ticketHistoryRepository.findTopByTicketOrderByCreatedAtDesc(testTicket1);

            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).isEqualTo("Ticket resolved");
        }

        @Test
        @DisplayName("Should count history entries for ticket")
        void shouldCountHistoryEntriesForTicket() {
            setupBasicTestData();

            long count = ticketHistoryRepository.countByTicket(testTicket1);

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Status-Based Query Tests")
    class StatusBasedTests {

        @Test
        @DisplayName("Should find history by OPEN status")
        void shouldFindHistoryByOpenStatus() {
            setupBasicTestData();

            List<TicketHistory> result = ticketHistoryRepository.findByStatus(TicketStatus.OPEN);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find history by CLOSED status")
        void shouldFindHistoryByClosedStatus() {
            setupBasicTestData();

            List<TicketHistory> result = ticketHistoryRepository.findByStatus(TicketStatus.CLOSED);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should count history entries by status")
        void shouldCountHistoryEntriesByStatus() {
            setupBasicTestData();

            long openCount = ticketHistoryRepository.countByStatus(TicketStatus.OPEN);
            long closedCount = ticketHistoryRepository.countByStatus(TicketStatus.CLOSED);

            assertThat(openCount).isEqualTo(2);
            assertThat(closedCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("User Activity Tests")
    class UserActivityTests {

        @Test
        @DisplayName("Should find history by created by user")
        void shouldFindHistoryByCreatedByUser() {
            setupBasicTestData();

            List<TicketHistory> result = ticketHistoryRepository.findByCreatedBy("admin");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should count history entries by created by user")
        void shouldCountHistoryEntriesByCreatedByUser() {
            setupBasicTestData();

            long adminCount = ticketHistoryRepository.countByCreatedBy("admin");
            long techCount = ticketHistoryRepository.countByCreatedBy("tech1@example.com");

            assertThat(adminCount).isEqualTo(2);
            assertThat(techCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Time-Based Query Tests")
    class TimeBasedTests {

        @Test
        @DisplayName("Should find history after specific date")
        void shouldFindHistoryAfterSpecificDate() {
            setupBasicTestData();
            Instant cutoffTime = Instant.now().minus(1, ChronoUnit.HOURS);

            List<TicketHistory> result = ticketHistoryRepository.findByCreatedAtAfter(cutoffTime);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should count history entries after specific date")
        void shouldCountHistoryEntriesAfterSpecificDate() {
            setupBasicTestData();
            Instant cutoffTime = Instant.now().minus(1, ChronoUnit.HOURS);

            long count = ticketHistoryRepository.countByCreatedAtAfter(cutoffTime);

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Description Search Tests")
    class DescriptionSearchTests {

        @Test
        @DisplayName("Should search by description term")
        void shouldSearchByDescriptionTerm() {
            setupBasicTestData();

            List<TicketHistory> result = ticketHistoryRepository.searchByDescription("created");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should perform case insensitive search")
        void shouldPerformCaseInsensitiveSearch() {
            setupBasicTestData();

            List<TicketHistory> result = ticketHistoryRepository.searchByDescription("TICKET");

            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Combined Filtering Tests")
    class CombinedFilteringTests {

        @Test
        @DisplayName("Should find history by ticket and created by")
        void shouldFindHistoryByTicketAndCreatedBy() {
            setupBasicTestData();
            Pageable pageable = PageRequest.of(0, 10);

            Page<TicketHistory> result = ticketHistoryRepository.findByTicketAndCreatedBy(testTicket1, "admin", pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should count history by ticket and status")
        void shouldCountHistoryByTicketAndStatus() {
            setupBasicTestData();

            long count = ticketHistoryRepository.countByTicketAndStatus(testTicket1, TicketStatus.OPEN);

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Workflow Analysis Tests")
    class WorkflowAnalysisTests {

        @Test
        @DisplayName("Should get status change frequency")
        void shouldGetStatusChangeFrequency() {
            setupBasicTestData();
            Instant sinceTime = Instant.now().minus(2, ChronoUnit.HOURS);

            List<Object[]> result = ticketHistoryRepository.getStatusChangeFrequency(sinceTime);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should find recent activity for ticket")
        void shouldFindRecentActivityForTicket() {
            setupBasicTestData();
            Instant sinceTime = Instant.now().minus(2, ChronoUnit.HOURS);

            List<TicketHistory> result = ticketHistoryRepository.findRecentActivityForTicket(testTicket1, sinceTime);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should not allow null ticket")
        void shouldNotAllowNullTicket() {
            TicketHistory history = new TicketHistory();
            history.setStatus(TicketStatus.OPEN);
            history.setDescription("Test description");
            history.setCreatedBy("admin");

            assertThatThrownBy(() -> {
                ticketHistoryRepository.save(history);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should not allow null status")
        void shouldNotAllowNullStatus() {
            setupBasicTestData();
            TicketHistory history = new TicketHistory();
            history.setTicket(testTicket1);
            history.setDescription("Test description");
            history.setCreatedBy("admin");

            assertThatThrownBy(() -> {
                ticketHistoryRepository.save(history);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should not allow null description")
        void shouldNotAllowNullDescription() {
            setupBasicTestData();
            TicketHistory history = new TicketHistory();
            history.setTicket(testTicket1);
            history.setStatus(TicketStatus.OPEN);
            history.setCreatedBy("admin");

            assertThatThrownBy(() -> {
                ticketHistoryRepository.save(history);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }
    }
}

package com.localtechsupport.repository;

import com.localtechsupport.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Transactional
@Rollback
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("TicketRepository Tests")
class TicketRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    private Client testClient1;
    private Client testClient2;
    private Technician testTechnician1;
    private Technician testTechnician2;
    private Ticket testTicket1;
    private Ticket testTicket2;
    private Ticket testTicket3;

    @BeforeEach
    void setUp() {
        // Manual cleanup to ensure test isolation
        entityManager.flush();
        
        // Get the underlying EntityManager for native queries
        EntityManager em = entityManager.getEntityManager();
        
        // Clear all ticket-related data in proper order to avoid FK constraints
        em.createNativeQuery("DELETE FROM ticket_history").executeUpdate();
        em.createNativeQuery("DELETE FROM feedback_entries").executeUpdate();
        em.createNativeQuery("DELETE FROM appointments").executeUpdate();
        em.createNativeQuery("DELETE FROM tickets").executeUpdate();
        em.createNativeQuery("DELETE FROM technician_skills").executeUpdate();
        em.createNativeQuery("DELETE FROM technicians").executeUpdate();
        em.createNativeQuery("DELETE FROM clients").executeUpdate();
        
        // Reset identity sequences
        em.createNativeQuery("ALTER TABLE ticket_history ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE feedback_entries ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE appointments ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE tickets ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE technician_skills ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE technicians ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE clients ALTER COLUMN id RESTART WITH 1").executeUpdate();
        
        entityManager.flush();
        entityManager.clear();
    }

    // Test data helper methods
    private Client createTestClient(String firstName, String lastName, String email) {
        Client client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setStatus(Client.ClientStatus.ACTIVE);
        client.setPhone("555-0123");
        client.setAddress("123 Test St");
        client.setNotes("Test notes");
        return client;
    }

    private Technician createTestTechnician(String fullName, String email, TechnicianStatus status) {
        Technician technician = new Technician();
        technician.setFullName(fullName);
        technician.setEmail(email);
        technician.setStatus(status);
        return technician;
    }

    private Ticket createTestTicket(Client client, ServiceType serviceType, String description, 
                                   TicketStatus status, Instant dueAt, Technician assignedTechnician) {
        Ticket ticket = new Ticket(client, serviceType, description, dueAt);
        ticket.setStatus(status);
        ticket.setAssignedTechnician(assignedTechnician);
        return ticket;
    }

    private Ticket createTestTicket(Client client, ServiceType serviceType, String description) {
        return createTestTicket(client, serviceType, description, TicketStatus.OPEN, 
                               Instant.now().plus(7, ChronoUnit.DAYS), null);
    }

    private void setupTestData() {
        // Create clients
        testClient1 = createTestClient("John", "Doe", "john.doe@example.com");
        testClient2 = createTestClient("Jane", "Smith", "jane.smith@example.com");
        entityManager.persistAndFlush(testClient1);
        entityManager.persistAndFlush(testClient2);

        // Create technicians
        testTechnician1 = createTestTechnician("Tech One", "tech1@company.com", TechnicianStatus.ACTIVE);
        testTechnician2 = createTestTechnician("Tech Two", "tech2@company.com", TechnicianStatus.ACTIVE);
        entityManager.persistAndFlush(testTechnician1);
        entityManager.persistAndFlush(testTechnician2);

        // Create tickets
        testTicket1 = createTestTicket(testClient1, ServiceType.HARDWARE, "Hardware issue", 
                                      TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), testTechnician1);
        testTicket2 = createTestTicket(testClient2, ServiceType.SOFTWARE, "Software problem", 
                                      TicketStatus.CLOSED, Instant.now().plus(3, ChronoUnit.DAYS), testTechnician2);
        testTicket3 = createTestTicket(testClient1, ServiceType.HARDWARE, "Another hardware issue", 
                                      TicketStatus.OPEN, Instant.now().plus(5, ChronoUnit.DAYS), null);

        entityManager.persistAndFlush(testTicket1);
        entityManager.persistAndFlush(testTicket2);
        entityManager.persistAndFlush(testTicket3);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Standard CRUD Operations")
    class StandardCrudTests {

        @Test
        @DisplayName("Should save new ticket successfully")
        void shouldSaveNewTicketSuccessfully() {
            // Given
            Client client = createTestClient("Test", "User", "test@example.com");
            entityManager.persistAndFlush(client);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Test ticket");

            // When
            Ticket savedTicket = ticketRepository.save(ticket);

            // Then
            assertThat(savedTicket).isNotNull();
            assertThat(savedTicket.getId()).isNotNull();
            assertThat(savedTicket.getDescription()).isEqualTo("Test ticket");
            assertThat(savedTicket.getCreatedAt()).isNotNull();
            assertThat(savedTicket.getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should update existing ticket successfully")
        void shouldUpdateExistingTicketSuccessfully() {
            // Given
            setupTestData();
            
            // When
            testTicket1.setDescription("Updated description");
            testTicket1.setStatus(TicketStatus.CLOSED);
            Ticket updatedTicket = ticketRepository.save(testTicket1);

            // Then
            assertThat(updatedTicket.getDescription()).isEqualTo("Updated description");
            assertThat(updatedTicket.getStatus()).isEqualTo(TicketStatus.CLOSED);
        }

        @Test
        @DisplayName("Should find ticket by existing ID")
        void shouldFindTicketByExistingId() {
            // Given
            setupTestData();

            // When
            Optional<Ticket> result = ticketRepository.findById(testTicket1.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).isEqualTo("Hardware issue");
        }

        @Test
        @DisplayName("Should return empty when finding by non-existing ID")
        void shouldReturnEmptyWhenFindingByNonExistingId() {
            // When
            Optional<Ticket> result = ticketRepository.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all tickets")
        void shouldFindAllTickets() {
            // Given
            setupTestData();

            // When
            List<Ticket> tickets = ticketRepository.findAll();

            // Then
            assertThat(tickets).hasSize(3);
            assertThat(tickets).extracting(Ticket::getDescription)
                .containsExactlyInAnyOrder("Hardware issue", "Software problem", "Another hardware issue");
        }

        @Test
        @DisplayName("Should return empty list when no tickets exist")
        void shouldReturnEmptyListWhenNoTicketsExist() {
            // When
            List<Ticket> tickets = ticketRepository.findAll();

            // Then
            assertThat(tickets).isEmpty();
        }

        @Test
        @DisplayName("Should delete ticket by ID")
        void shouldDeleteTicketById() {
            // Given
            setupTestData();

            // When
            ticketRepository.deleteById(testTicket1.getId());

            // Then
            Optional<Ticket> result = ticketRepository.findById(testTicket1.getId());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count all tickets correctly")
        void shouldCountAllTicketsCorrectly() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Status-Based Query Tests")
    class StatusBasedTests {

        @Test
        @DisplayName("Should find tickets by OPEN status")
        void shouldFindTicketsByOpenStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(Ticket::getStatus)
                .containsOnly(TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should find tickets by CLOSED status")
        void shouldFindTicketsByClosedStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.CLOSED, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).extracting(Ticket::getStatus)
                .containsOnly(TicketStatus.CLOSED);
        }

        @Test
        @DisplayName("Should find tickets by status without pagination")
        void shouldFindTicketsByStatusWithoutPagination() {
            // Given
            setupTestData();

            // When
            List<Ticket> openTickets = ticketRepository.findByStatus(TicketStatus.OPEN);
            List<Ticket> closedTickets = ticketRepository.findByStatus(TicketStatus.CLOSED);

            // Then
            assertThat(openTickets).hasSize(2);
            assertThat(closedTickets).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty page when no tickets match status")
        void shouldReturnEmptyPageWhenNoTicketsMatchStatus() {
            // Given - no test data setup
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should count tickets by OPEN status")
        void shouldCountTicketsByOpenStatus() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.countByStatus(TicketStatus.OPEN);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count tickets by CLOSED status")
        void shouldCountTicketsByClosedStatus() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.countByStatus(TicketStatus.CLOSED);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return zero count for status with no tickets")
        void shouldReturnZeroCountForStatusWithNoTickets() {
            // Given - no test data

            // When
            long count = ticketRepository.countByStatus(TicketStatus.OPEN);

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Client Association Tests")
    class ClientAssociationTests {

        @Test
        @DisplayName("Should find tickets by client")
        void shouldFindTicketsByClient() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByClient(testClient1, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(t -> t.getClient().getEmail())
                .containsOnly("john.doe@example.com");
        }

        @Test
        @DisplayName("Should find tickets by client without pagination")
        void shouldFindTicketsByClientWithoutPagination() {
            // Given
            setupTestData();

            // When
            List<Ticket> result = ticketRepository.findByClient(testClient1);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(t -> t.getClient().getEmail())
                .containsOnly("john.doe@example.com");
        }

        @Test
        @DisplayName("Should find tickets by client and status")
        void shouldFindTicketsByClientAndStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByClientAndStatus(testClient1, TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(t -> 
                t.getClient().getEmail().equals("john.doe@example.com") && 
                t.getStatus() == TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should return empty when client has no tickets")
        void shouldReturnEmptyWhenClientHasNoTickets() {
            // Given
            Client clientWithNoTickets = createTestClient("No", "Tickets", "no.tickets@example.com");
            entityManager.persistAndFlush(clientWithNoTickets);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByClient(clientWithNoTickets, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count tickets by client")
        void shouldCountTicketsByClient() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.countByClient(testClient1);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find tickets by client and service type")
        void shouldFindTicketsByClientAndServiceType() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByClientAndServiceType(testClient1, ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(t -> 
                t.getClient().equals(testClient1) && 
                t.getServiceType() == ServiceType.HARDWARE);
        }
    }

    @Nested
    @DisplayName("Technician Assignment Tests")
    class TechnicianAssignmentTests {

        @Test
        @DisplayName("Should find tickets by assigned technician")
        void shouldFindTicketsByAssignedTechnician() {
            // Given
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("Test", "Client", "test.client." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);
            
            Technician technician = createTestTechnician("Test Tech", "test.tech." + uniqueId + "@company.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(technician);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Test ticket", 
                                           TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), technician);
            entityManager.persistAndFlush(ticket);
            entityManager.clear();
            
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByAssignedTechnician(technician, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAssignedTechnician().getId()).isEqualTo(technician.getId());
        }

        @Test
        @DisplayName("Should find tickets by assigned technician without pagination")
        void shouldFindTicketsByAssignedTechnicianWithoutPagination() {
            // Given
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("Test", "Client", "test.client." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);
            
            Technician technician = createTestTechnician("Test Tech", "test.tech." + uniqueId + "@company.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(technician);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Test ticket", 
                                           TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), technician);
            entityManager.persistAndFlush(ticket);
            entityManager.clear();

            // When
            List<Ticket> result = ticketRepository.findByAssignedTechnician(technician);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAssignedTechnician().getId()).isEqualTo(technician.getId());
        }

        @Test
        @DisplayName("Should find tickets by assigned technician and status")
        void shouldFindTicketsByAssignedTechnicianAndStatus() {
            // Given
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("Test", "Client", "test.client." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);
            
            Technician technician = createTestTechnician("Test Tech", "test.tech." + uniqueId + "@company.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(technician);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Test ticket", 
                                           TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), technician);
            entityManager.persistAndFlush(ticket);
            entityManager.clear();
            
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByAssignedTechnicianAndStatus(technician, TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAssignedTechnician().getId()).isEqualTo(technician.getId());
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should find unassigned tickets")
        void shouldFindUnassignedTickets() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByAssignedTechnicianIsNull(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAssignedTechnician()).isNull();
        }

        @Test
        @DisplayName("Should find unassigned tickets without pagination")
        void shouldFindUnassignedTicketsWithoutPagination() {
            // Given
            setupTestData();

            // When
            List<Ticket> result = ticketRepository.findByAssignedTechnicianIsNull();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAssignedTechnician()).isNull();
        }

        @Test
        @DisplayName("Should find unassigned tickets by status")
        void shouldFindUnassignedTicketsByStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByAssignedTechnicianIsNullAndStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAssignedTechnician()).isNull();
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should count tickets by assigned technician")
        void shouldCountTicketsByAssignedTechnician() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.countByAssignedTechnician(testTechnician1);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count tickets by assigned technician and status")
        void shouldCountTicketsByAssignedTechnicianAndStatus() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.countByAssignedTechnicianAndStatus(testTechnician1, TicketStatus.OPEN);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count unassigned tickets")
        void shouldCountUnassignedTickets() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.countByAssignedTechnicianIsNull();

            // Then
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Service Type Tests")
    class ServiceTypeTests {

        @Test
        @DisplayName("Should find tickets by HARDWARE service type")
        void shouldFindTicketsByHardwareServiceType() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByServiceType(ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(Ticket::getServiceType)
                .containsOnly(ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should find tickets by SOFTWARE service type")
        void shouldFindTicketsBySoftwareServiceType() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByServiceType(ServiceType.SOFTWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).extracting(Ticket::getServiceType)
                .containsOnly(ServiceType.SOFTWARE);
        }

        @Test
        @DisplayName("Should find tickets by service type without pagination")
        void shouldFindTicketsByServiceTypeWithoutPagination() {
            // Given
            setupTestData();

            // When
            List<Ticket> hardwareTickets = ticketRepository.findByServiceType(ServiceType.HARDWARE);
            List<Ticket> softwareTickets = ticketRepository.findByServiceType(ServiceType.SOFTWARE);

            // Then
            assertThat(hardwareTickets).hasSize(2);
            assertThat(softwareTickets).hasSize(1);
        }

        @Test
        @DisplayName("Should find tickets by service type and status")
        void shouldFindTicketsByServiceTypeAndStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByServiceTypeAndStatus(ServiceType.HARDWARE, TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(t -> 
                t.getServiceType() == ServiceType.HARDWARE && 
                t.getStatus() == TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should find unassigned tickets by service type")
        void shouldFindUnassignedTicketsByServiceType() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByServiceTypeAndAssignedTechnicianIsNull(ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getServiceType()).isEqualTo(ServiceType.HARDWARE);
            assertThat(result.getContent().get(0).getAssignedTechnician()).isNull();
        }

        @Test
        @DisplayName("Should find unassigned tickets by service type without pagination")
        void shouldFindUnassignedTicketsByServiceTypeWithoutPagination() {
            // Given
            setupTestData();

            // When
            List<Ticket> result = ticketRepository.findByServiceTypeAndAssignedTechnicianIsNull(ServiceType.HARDWARE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getServiceType()).isEqualTo(ServiceType.HARDWARE);
            assertThat(result.get(0).getAssignedTechnician()).isNull();
        }

        @Test
        @DisplayName("Should count tickets by service type")
        void shouldCountTicketsByServiceType() {
            // Given
            setupTestData();

            // When
            long hardwareCount = ticketRepository.countByServiceType(ServiceType.HARDWARE);
            long softwareCount = ticketRepository.countByServiceType(ServiceType.SOFTWARE);

            // Then
            assertThat(hardwareCount).isEqualTo(2);
            assertThat(softwareCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count tickets by status and service type")
        void shouldCountTicketsByStatusAndServiceType() {
            // Given
            setupTestData();

            // When
            long count = ticketRepository.countByStatusAndServiceType(TicketStatus.OPEN, ServiceType.HARDWARE);

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Time-Based Query Tests")
    class TimeBasedTests {

        private Instant baseTime;
        private Instant pastTime;
        private Instant futureTime;
        private Instant farFuture;

        @BeforeEach
        void setupTimeData() {
            baseTime = Instant.now();
            pastTime = baseTime.minus(2, ChronoUnit.HOURS);
            futureTime = baseTime.plus(2, ChronoUnit.HOURS);
            farFuture = baseTime.plus(7, ChronoUnit.DAYS);

            // Create clients and technicians with unique emails
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("Time", "Test", "time.test." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);

            // Create tickets with different time stamps
            Ticket pastTicket = createTestTicket(client, ServiceType.HARDWARE, "Past ticket", 
                                               TicketStatus.CLOSED, pastTime, null);
            pastTicket.setCreatedAt(pastTime.minus(1, ChronoUnit.HOURS));
            
            Ticket currentTicket = createTestTicket(client, ServiceType.SOFTWARE, "Current ticket", 
                                                  TicketStatus.OPEN, futureTime, null);
            
            Ticket futureTicket = createTestTicket(client, ServiceType.HARDWARE, "Future ticket", 
                                                 TicketStatus.OPEN, farFuture, null);

            entityManager.persistAndFlush(pastTicket);
            entityManager.persistAndFlush(currentTicket);
            entityManager.persistAndFlush(futureTicket);
            entityManager.clear();
        }

        @Test
        @DisplayName("Should find tickets created after specific date")
        void shouldFindTicketsCreatedAfterSpecificDate() {
            // Given
            setupTimeData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByCreatedAtAfter(baseTime.minus(30, ChronoUnit.MINUTES), pageable);

            // Then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should find tickets created between dates")
        void shouldFindTicketsCreatedBetweenDates() {
            // Given
            setupTimeData();
            Pageable pageable = PageRequest.of(0, 10);
            Instant startDate = pastTime.minus(2, ChronoUnit.HOURS);
            Instant endDate = baseTime.plus(1, ChronoUnit.HOURS);

            // When
            Page<Ticket> result = ticketRepository.findByCreatedAtBetween(startDate, endDate, pageable);

            // Then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should find tickets due before specific date")
        void shouldFindTicketsDueBeforeSpecificDate() {
            // Given
            setupTimeData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByDueAtBefore(futureTime.plus(1, ChronoUnit.HOURS), pageable);

            // Then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should find tickets due between dates")
        void shouldFindTicketsDueBetweenDates() {
            // Given
            setupTimeData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByDueAtBetween(baseTime, farFuture.plus(1, ChronoUnit.HOURS), pageable);

            // Then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should find overdue tickets")
        void shouldFindOverdueTickets() {
            // Given
            setupTimeData();
            Pageable pageable = PageRequest.of(0, 10);
            Instant currentTime = baseTime.plus(3, ChronoUnit.HOURS);

            // When
            Page<Ticket> result = ticketRepository.findOverdueTickets(currentTime, pageable);

            // Then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should find overdue tickets without pagination")
        void shouldFindOverdueTicketsWithoutPagination() {
            // Given
            setupTimeData();
            Instant currentTime = baseTime.plus(3, ChronoUnit.HOURS);

            // When
            List<Ticket> result = ticketRepository.findOverdueTickets(currentTime);

            // Then
            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should find tickets due soon")
        void shouldFindTicketsDueSoon() {
            // Given
            setupTimeData();
            Pageable pageable = PageRequest.of(0, 10);
            Instant currentTime = baseTime;
            Instant thresholdTime = baseTime.plus(4, ChronoUnit.HOURS);

            // When
            Page<Ticket> result = ticketRepository.findTicketsDueSoon(currentTime, thresholdTime, pageable);

            // Then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should find tickets due soon without pagination")
        void shouldFindTicketsDueSoonWithoutPagination() {
            // Given
            setupTimeData();
            Instant currentTime = baseTime;
            Instant thresholdTime = baseTime.plus(4, ChronoUnit.HOURS);

            // When
            List<Ticket> result = ticketRepository.findTicketsDueSoon(currentTime, thresholdTime);

            // Then
            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should count tickets created after date")
        void shouldCountTicketsCreatedAfterDate() {
            // Given
            setupTimeData();

            // When
            long count = ticketRepository.countByCreatedAtAfter(baseTime.minus(30, ChronoUnit.MINUTES));

            // Then
            assertThat(count).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should count overdue tickets")
        void shouldCountOverdueTickets() {
            // Given
            setupTimeData();
            Instant currentTime = baseTime.plus(3, ChronoUnit.HOURS);

            // When
            long count = ticketRepository.countOverdueTickets(currentTime);

            // Then
            assertThat(count).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Search Tests")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    class SearchTests {

        private void setupSearchData() {
            // Create test data for search functionality with unique emails
            String uniqueId = String.valueOf(System.nanoTime());
            
            Client client1 = createTestClient("Hardware", "Expert", "hardware.expert." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client1);
            
            Client client2 = createTestClient("Software", "Developer", "software.dev." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client2);
            
            Client client3 = createTestClient("Network", "Admin", "network.admin." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client3);
            
            Ticket ticket1 = createTestTicket(client1, ServiceType.HARDWARE, "Printer won't print documents", 
                                            TicketStatus.OPEN, Instant.now().plus(7, ChronoUnit.DAYS), null);
            entityManager.persistAndFlush(ticket1);
            
            Ticket ticket2 = createTestTicket(client2, ServiceType.SOFTWARE, "Email application crashes frequently", 
                                            TicketStatus.OPEN, Instant.now().plus(7, ChronoUnit.DAYS), null);
            entityManager.persistAndFlush(ticket2);
            
            Ticket ticket3 = createTestTicket(client3, ServiceType.HARDWARE, "Network connectivity issues", 
                                            TicketStatus.OPEN, Instant.now().plus(7, ChronoUnit.DAYS), null);
            entityManager.persistAndFlush(ticket3);
        }

        @Test
        @DisplayName("Should search tickets by description")
        void shouldSearchTicketsByDescription() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("print", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDescription()).containsIgnoringCase("print");
        }

        @Test
        @DisplayName("Should search tickets by client first name")
        void shouldSearchTicketsByClientFirstName() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("Hardware", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClient().getFirstName()).isEqualTo("Hardware");
        }

        @Test
        @DisplayName("Should search tickets by client last name")
        void shouldSearchTicketsByClientLastName() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("Developer", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClient().getLastName()).isEqualTo("Developer");
        }

        @Test
        @DisplayName("Should search tickets by client email")
        void shouldSearchTicketsByClientEmail() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("network.admin", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClient().getEmail()).containsIgnoringCase("network.admin");
        }

        @Test
        @DisplayName("Should perform case insensitive search")
        void shouldPerformCaseInsensitiveSearch() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("EMAIL", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDescription()).containsIgnoringCase("email");
        }

        @Test
        @DisplayName("Should handle partial matches")
        void shouldHandlePartialMatches() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("crashes", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDescription()).containsIgnoringCase("crashes");
        }

        @Test
        @DisplayName("Should handle empty search term")
        void shouldHandleEmptySearchTerm() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("", pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty result for no matches")
        void shouldReturnEmptyResultForNoMatches() {
            // Given
            setupSearchData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.searchTickets("nonexistent", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Combined Filtering Tests")
    class CombinedFilteringTests {

        @Test
        @DisplayName("Should find tickets by status and service type")
        void shouldFindTicketsByStatusAndServiceType() {
            // Given
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("John", "Doe", "john.doe." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);

            Ticket ticket1 = createTestTicket(client, ServiceType.HARDWARE, "Hardware issue", 
                                            TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), null);
            Ticket ticket2 = createTestTicket(client, ServiceType.SOFTWARE, "Software issue", 
                                            TicketStatus.CLOSED, Instant.now().plus(2, ChronoUnit.DAYS), null);
            
            entityManager.persistAndFlush(ticket1);
            entityManager.persistAndFlush(ticket2);
            entityManager.clear();
            
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByStatusAndServiceType(TicketStatus.OPEN, ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(result.getContent().get(0).getServiceType()).isEqualTo(ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should find tickets by status and created after date")
        void shouldFindTicketsByStatusAndCreatedAfterDate() {
            // Given
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("Jane", "Smith", "jane.smith." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);
            
            Instant hourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Recent ticket", 
                                           TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), null);
            entityManager.persistAndFlush(ticket);
            entityManager.clear();
            
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByStatusAndCreatedAtAfter(TicketStatus.OPEN, hourAgo, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should return empty when no tickets match combined criteria")
        void shouldReturnEmptyWhenNoTicketsMatchCombinedCriteria() {
            // Given
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("Test", "User", "test.user." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);

            Ticket ticket = createTestTicket(client, ServiceType.SOFTWARE, "Software issue", 
                                           TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), null);
            entityManager.persistAndFlush(ticket);
            entityManager.clear();
            
            Pageable pageable = PageRequest.of(0, 10);

            // When - Looking for CLOSED HARDWARE tickets, but we only have OPEN SOFTWARE
            Page<Ticket> result = ticketRepository.findByStatusAndServiceType(TicketStatus.CLOSED, ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple service types in combined query")
        void shouldHandleMultipleServiceTypesInCombinedQuery() {
            // Given
            String uniqueId = String.valueOf(System.nanoTime());
            Client client = createTestClient("Multi", "Service", "multi.service." + uniqueId + "@example.com");
            entityManager.persistAndFlush(client);

            Ticket hardwareTicket = createTestTicket(client, ServiceType.HARDWARE, "Hardware issue", 
                                                   TicketStatus.OPEN, Instant.now().plus(1, ChronoUnit.DAYS), null);
            Ticket softwareTicket = createTestTicket(client, ServiceType.SOFTWARE, "Software issue", 
                                                   TicketStatus.OPEN, Instant.now().plus(2, ChronoUnit.DAYS), null);
            
            entityManager.persistAndFlush(hardwareTicket);
            entityManager.persistAndFlush(softwareTicket);
            entityManager.clear();
            
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> hardwareResults = ticketRepository.findByStatusAndServiceType(TicketStatus.OPEN, ServiceType.HARDWARE, pageable);
            Page<Ticket> softwareResults = ticketRepository.findByStatusAndServiceType(TicketStatus.OPEN, ServiceType.SOFTWARE, pageable);

            // Then
            assertThat(hardwareResults.getContent()).hasSize(1);
            assertThat(softwareResults.getContent()).hasSize(1);
            assertThat(hardwareResults.getContent().get(0).getServiceType()).isEqualTo(ServiceType.HARDWARE);
            assertThat(softwareResults.getContent().get(0).getServiceType()).isEqualTo(ServiceType.SOFTWARE);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    class PaginationTests {

        private void setupPaginationData() {
            // Create test data for pagination
            for (int i = 1; i <= 10; i++) {
                String uniqueId = String.valueOf(System.nanoTime() + i);
                Client client = createTestClient("Client" + i, "User" + i, "client" + i + "." + uniqueId + "@example.com");
                entityManager.persistAndFlush(client);
                
                Ticket ticket = createTestTicket(client, i % 2 == 0 ? ServiceType.SOFTWARE : ServiceType.HARDWARE, 
                                               "Ticket " + i + " description", TicketStatus.OPEN, 
                                               Instant.now().plus(7, ChronoUnit.DAYS), null);
                entityManager.persistAndFlush(ticket);
            }
        }

        @Test
        @DisplayName("Should handle first page correctly")
        void shouldHandleFirstPageCorrectly() {
            // Given
            setupPaginationData();
            Pageable pageable = PageRequest.of(0, 3);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(4);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isFalse();
        }

        @Test
        @DisplayName("Should handle middle page correctly")
        void shouldHandleMiddlePageCorrectly() {
            // Given
            setupPaginationData();
            Pageable pageable = PageRequest.of(1, 3);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isFalse();
        }

        @Test
        @DisplayName("Should handle last page correctly")
        void shouldHandleLastPageCorrectly() {
            // Given
            setupPaginationData();
            Pageable pageable = PageRequest.of(3, 3);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should handle page beyond available data")
        void shouldHandlePageBeyondAvailableData() {
            // Given
            setupPaginationData();
            Pageable pageable = PageRequest.of(10, 3);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should handle sorting by description ascending")
        void shouldHandleSortingByDescriptionAscending() {
            // Given
            setupPaginationData();
            Pageable pageable = PageRequest.of(0, 5, Sort.by("description").ascending());

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            String firstDescription = result.getContent().get(0).getDescription();
            String lastDescription = result.getContent().get(4).getDescription();
            assertThat(firstDescription.compareTo(lastDescription)).isLessThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should handle sorting by due date descending")
        void shouldHandleSortingByDueDateDescending() {
            // Given
            setupPaginationData();
            Pageable pageable = PageRequest.of(0, 5, Sort.by("dueAt").descending());

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            Instant firstDueDate = result.getContent().get(0).getDueAt();
            Instant lastDueDate = result.getContent().get(4).getDueAt();
            assertThat(firstDueDate.isAfter(lastDueDate) || firstDueDate.equals(lastDueDate)).isTrue();
        }

        @Test
        @DisplayName("Should handle empty results with pagination")
        void shouldHandleEmptyResultsWithPagination() {
            // Given - no test data
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Ticket> result = ticketRepository.findByStatus(TicketStatus.OPEN, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should handle null values properly")
        void shouldHandleNullValuesProperly() {
            // Given
            Client client = createTestClient("Null", "Test", "null.test@example.com");
            entityManager.persistAndFlush(client);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Test ticket");
            ticket.setAssignedTechnician(null); // Explicitly set to null

            // When/Then
            assertDoesNotThrow(() -> {
                Ticket savedTicket = ticketRepository.save(ticket);
                assertThat(savedTicket.getAssignedTechnician()).isNull();
            });
        }

        @Test
        @DisplayName("Should not allow null required fields")
        void shouldNotAllowNullRequiredFields() {
            // Given
            Ticket ticket = new Ticket();
            ticket.setServiceType(ServiceType.HARDWARE);
            ticket.setDescription("Test");
            ticket.setDueAt(Instant.now().plus(1, ChronoUnit.DAYS));
            // client is null - required field

            // When/Then
            assertThatThrownBy(() -> {
                ticketRepository.save(ticket);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should enforce service type constraint")
        void shouldEnforceServiceTypeConstraint() {
            // Given
            Client client = createTestClient("Service", "Type", "service.type@example.com");
            entityManager.persistAndFlush(client);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Test ticket");

            // When/Then
            assertDoesNotThrow(() -> {
                ticket.setServiceType(ServiceType.SOFTWARE);
                Ticket savedTicket = ticketRepository.save(ticket);
                assertThat(savedTicket.getServiceType()).isEqualTo(ServiceType.SOFTWARE);
            });
        }

        @Test
        @DisplayName("Should handle cascade operations properly")
        void shouldHandleCascadeOperationsProperly() {
            // Given
            Client client = createTestClient("Cascade", "Test", "cascade.test@example.com");
            entityManager.persistAndFlush(client);
            
            Ticket ticket = createTestTicket(client, ServiceType.HARDWARE, "Test ticket");
            Ticket savedTicket = ticketRepository.save(ticket);

            // When
            ticketRepository.delete(savedTicket);
            entityManager.flush();

            // Then
            Optional<Ticket> result = ticketRepository.findById(savedTicket.getId());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should maintain referential integrity")
        void shouldMaintainReferentialIntegrity() {
            // Given
            setupTestData();

            // When
            List<Ticket> clientTickets = ticketRepository.findByClient(testClient1);

            // Then
            assertThat(clientTickets).hasSize(2);
            assertThat(clientTickets).allMatch(t -> t.getClient().getId().equals(testClient1.getId()));
        }
    }
} 
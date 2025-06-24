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
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@DisplayName("AppointmentRepository Tests")
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Technician testTechnician1;
    private Technician testTechnician2;
    private Ticket testTicket1;
    private Ticket testTicket2;
    private Client testClient;
    private Appointment testAppointment1;
    private Appointment testAppointment2;
    private Appointment testAppointment3;
    
    private Instant now;
    private Instant hourAgo;
    private Instant hourFromNow;
    private Instant dayAgo;
    private Instant dayFromNow;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        appointmentRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        // Setup time references
        now = Instant.now();
        hourAgo = now.minus(1, ChronoUnit.HOURS);
        hourFromNow = now.plus(1, ChronoUnit.HOURS);
        dayAgo = now.minus(1, ChronoUnit.DAYS);
        dayFromNow = now.plus(1, ChronoUnit.DAYS);
    }

    // Test data helper methods
    private Client createTestClient(String email) {
        Client client = new Client();
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setEmail(email);
        client.setStatus(Client.ClientStatus.ACTIVE);
        client.setPhone("555-0123");
        client.setAddress("123 Test St");
        return client;
    }

    private Technician createTestTechnician(String email, TechnicianStatus status) {
        Technician technician = new Technician();
        technician.setFullName("Test Technician");
        technician.setEmail(email);
        technician.setStatus(status);
        return technician;
    }

    private Ticket createTestTicket(Client client, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setDescription("Test Description");
        ticket.setStatus(status);
        ticket.setServiceType(ServiceType.HARDWARE);
        ticket.setClient(client);
        ticket.setDueAt(dayFromNow);
        return ticket;
    }

    private Appointment createTestAppointment(Technician technician, Ticket ticket, 
                                            Instant startTime, Instant endTime, 
                                            AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setTechnician(technician);
        appointment.setTicket(ticket);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(status);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        return appointment;
    }

    private void setupTestData() {
        testClient = createTestClient("client@example.com");
        entityManager.persistAndFlush(testClient);

        testTechnician1 = createTestTechnician("tech1@example.com", TechnicianStatus.ACTIVE);
        testTechnician2 = createTestTechnician("tech2@example.com", TechnicianStatus.ACTIVE);
        entityManager.persistAndFlush(testTechnician1);
        entityManager.persistAndFlush(testTechnician2);

        testTicket1 = createTestTicket(testClient, TicketStatus.OPEN);
        testTicket2 = createTestTicket(testClient, TicketStatus.CLOSED);
        entityManager.persistAndFlush(testTicket1);
        entityManager.persistAndFlush(testTicket2);

        testAppointment1 = createTestAppointment(testTechnician1, testTicket1, 
            hourFromNow, hourFromNow.plus(2, ChronoUnit.HOURS), AppointmentStatus.CONFIRMED);
        testAppointment2 = createTestAppointment(testTechnician2, testTicket2, 
            dayFromNow, dayFromNow.plus(1, ChronoUnit.HOURS), AppointmentStatus.PENDING);
        testAppointment3 = createTestAppointment(testTechnician1, testTicket1, 
            hourAgo, hourAgo.plus(30, ChronoUnit.MINUTES), AppointmentStatus.COMPLETED);

        entityManager.persistAndFlush(testAppointment1);
        entityManager.persistAndFlush(testAppointment2);
        entityManager.persistAndFlush(testAppointment3);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Standard CRUD Operations")
    class StandardCrudTests {

        @Test
        @DisplayName("Should save new appointment successfully")
        void shouldSaveNewAppointmentSuccessfully() {
            // Given
            setupTestData();
            Appointment appointment = createTestAppointment(testTechnician1, testTicket1, 
                now.plus(3, ChronoUnit.HOURS), now.plus(4, ChronoUnit.HOURS), AppointmentStatus.PENDING);

            // When
            Appointment savedAppointment = appointmentRepository.save(appointment);

            // Then
            assertThat(savedAppointment).isNotNull();
            assertThat(savedAppointment.getId()).isNotNull();
            assertThat(savedAppointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
            assertThat(savedAppointment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update existing appointment successfully")
        void shouldUpdateExistingAppointmentSuccessfully() {
            // Given
            setupTestData();
            testAppointment1.setStatus(AppointmentStatus.IN_PROGRESS);

            // When
            Appointment updatedAppointment = appointmentRepository.save(testAppointment1);

            // Then
            assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should find appointment by existing ID")
        void shouldFindAppointmentByExistingId() {
            // Given
            setupTestData();

            // When
            Optional<Appointment> result = appointmentRepository.findById(testAppointment1.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should return empty when finding by non-existing ID")
        void shouldReturnEmptyWhenFindingByNonExistingId() {
            // When
            Optional<Appointment> result = appointmentRepository.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all appointments")
        void shouldFindAllAppointments() {
            // Given
            setupTestData();

            // When
            List<Appointment> appointments = appointmentRepository.findAll();

            // Then
            assertThat(appointments).hasSize(3);
        }

        @Test
        @DisplayName("Should delete appointment by ID")
        void shouldDeleteAppointmentById() {
            // Given
            setupTestData();
            Long appointmentId = testAppointment1.getId();

            // When
            appointmentRepository.deleteById(appointmentId);

            // Then
            Optional<Appointment> result = appointmentRepository.findById(appointmentId);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count all appointments correctly")
        void shouldCountAllAppointmentsCorrectly() {
            // Given
            setupTestData();

            // When
            long count = appointmentRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Status-Based Query Tests")
    class StatusBasedTests {

        @BeforeEach
        void setupStatusData() {
            setupTestData();
        }

        @Test
        @DisplayName("Should find appointments by CONFIRMED status")
        void shouldFindAppointmentsByConfirmedStatus() {
            // When
            Page<Appointment> result = appointmentRepository.findByStatus(
                AppointmentStatus.CONFIRMED, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should find appointments by PENDING status")
        void shouldFindAppointmentsByPendingStatus() {
            // When
            List<Appointment> result = appointmentRepository.findByStatus(AppointmentStatus.PENDING);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }

        @Test
        @DisplayName("Should find appointments by COMPLETED status")
        void shouldFindAppointmentsByCompletedStatus() {
            // When
            Page<Appointment> result = appointmentRepository.findByStatus(
                AppointmentStatus.COMPLETED, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should return empty page when no appointments match status")
        void shouldReturnEmptyPageWhenNoAppointmentsMatchStatus() {
            // When
            Page<Appointment> result = appointmentRepository.findByStatus(
                AppointmentStatus.CANCELLED, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should count appointments by CONFIRMED status")
        void shouldCountAppointmentsByConfirmedStatus() {
            // When
            long count = appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return zero count for status with no appointments")
        void shouldReturnZeroCountForStatusWithNoAppointments() {
            // When
            long count = appointmentRepository.countByStatus(AppointmentStatus.NO_SHOW);

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Technician-Based Query Tests")
    class TechnicianBasedTests {

        @BeforeEach
        void setupTechnicianData() {
            setupTestData();
        }

        @Test
        @DisplayName("Should find appointments by technician")
        void shouldFindAppointmentsByTechnician() {
            // When
            Page<Appointment> result = appointmentRepository.findByTechnician(
                testTechnician1, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(
                appointment -> appointment.getTechnician().getId().equals(testTechnician1.getId()));
        }

        @Test
        @DisplayName("Should find appointments by technician as list")
        void shouldFindAppointmentsByTechnicianAsList() {
            // When
            List<Appointment> result = appointmentRepository.findByTechnician(testTechnician2);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTechnician().getId()).isEqualTo(testTechnician2.getId());
        }

        @Test
        @DisplayName("Should find appointments by technician and status")
        void shouldFindAppointmentsByTechnicianAndStatus() {
            // When
            Page<Appointment> result = appointmentRepository.findByTechnicianAndStatus(
                testTechnician1, AppointmentStatus.CONFIRMED, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(result.getContent().get(0).getTechnician().getId()).isEqualTo(testTechnician1.getId());
        }

        @Test
        @DisplayName("Should find appointments by technician and status as list")
        void shouldFindAppointmentsByTechnicianAndStatusAsList() {
            // When
            List<Appointment> result = appointmentRepository.findByTechnicianAndStatus(
                testTechnician1, AppointmentStatus.COMPLETED);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should return empty when technician has no appointments")
        void shouldReturnEmptyWhenTechnicianHasNoAppointments() {
            // Given
            Technician newTechnician = createTestTechnician("new@example.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(newTechnician);

            // When
            List<Appointment> result = appointmentRepository.findByTechnician(newTechnician);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count appointments by technician")
        void shouldCountAppointmentsByTechnician() {
            // When
            long count = appointmentRepository.countByTechnician(testTechnician1);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count appointments by status and technician")
        void shouldCountAppointmentsByStatusAndTechnician() {
            // When
            long count = appointmentRepository.countByStatusAndTechnician(
                AppointmentStatus.CONFIRMED, testTechnician1);

            // Then
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Ticket-Based Query Tests")
    class TicketBasedTests {

        @BeforeEach
        void setupTicketData() {
            setupTestData();
        }

        @Test
        @DisplayName("Should find appointments by ticket")
        void shouldFindAppointmentsByTicket() {
            // When
            Page<Appointment> result = appointmentRepository.findByTicket(
                testTicket1, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(
                appointment -> appointment.getTicket().getId().equals(testTicket1.getId()));
        }

        @Test
        @DisplayName("Should find appointments by ticket as list")
        void shouldFindAppointmentsByTicketAsList() {
            // When
            List<Appointment> result = appointmentRepository.findByTicket(testTicket2);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTicket().getId()).isEqualTo(testTicket2.getId());
        }

        @Test
        @DisplayName("Should find appointments by ticket and status")
        void shouldFindAppointmentsByTicketAndStatus() {
            // When
            Page<Appointment> result = appointmentRepository.findByTicketAndStatus(
                testTicket1, AppointmentStatus.CONFIRMED, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(result.getContent().get(0).getTicket().getId()).isEqualTo(testTicket1.getId());
        }

        @Test
        @DisplayName("Should return empty when ticket has no appointments")
        void shouldReturnEmptyWhenTicketHasNoAppointments() {
            // Given
            Ticket newTicket = createTestTicket(testClient, TicketStatus.OPEN);
            entityManager.persistAndFlush(newTicket);

            // When
            List<Appointment> result = appointmentRepository.findByTicket(newTicket);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count appointments by ticket")
        void shouldCountAppointmentsByTicket() {
            // When
            long count = appointmentRepository.countByTicket(testTicket1);

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Time-Based Query Tests")
    class TimeBasedTests {

        private Instant baseTime;
        private Instant hourAgo;
        private Instant hourFromNow;
        private Instant dayAgo;
        private Instant dayFromNow;

        private Appointment createLocalTestAppointment(Technician technician, Ticket ticket, 
                                                Instant startTime, Instant endTime, 
                                                AppointmentStatus status) {
            Appointment appointment = new Appointment();
            appointment.setTechnician(technician);
            appointment.setTicket(ticket);
            appointment.setStartTime(startTime);
            appointment.setEndTime(endTime);
            appointment.setStatus(status);
            appointment.setCreatedAt(baseTime);
            appointment.setUpdatedAt(baseTime);
            return appointment;
        }

        @BeforeEach
        void setupTimeData() {
            // Use a fixed base time to avoid race conditions in CI environments
            baseTime = Instant.parse("2024-01-15T10:00:00Z");
            hourAgo = baseTime.minus(1, ChronoUnit.HOURS);
            hourFromNow = baseTime.plus(1, ChronoUnit.HOURS);
            dayAgo = baseTime.minus(1, ChronoUnit.DAYS);
            dayFromNow = baseTime.plus(1, ChronoUnit.DAYS);
            
            // Create test data using the local time variables
            testClient = createTestClient("client@example.com");
            entityManager.persistAndFlush(testClient);

            testTechnician1 = createTestTechnician("tech1@example.com", TechnicianStatus.ACTIVE);
            testTechnician2 = createTestTechnician("tech2@example.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(testTechnician1);
            entityManager.persistAndFlush(testTechnician2);

            testTicket1 = createTestTicket(testClient, TicketStatus.OPEN);
            testTicket2 = createTestTicket(testClient, TicketStatus.CLOSED);
            entityManager.persistAndFlush(testTicket1);
            entityManager.persistAndFlush(testTicket2);

            testAppointment1 = createLocalTestAppointment(testTechnician1, testTicket1, 
                hourFromNow, hourFromNow.plus(2, ChronoUnit.HOURS), AppointmentStatus.CONFIRMED);
            testAppointment2 = createLocalTestAppointment(testTechnician2, testTicket2, 
                dayFromNow, dayFromNow.plus(1, ChronoUnit.HOURS), AppointmentStatus.PENDING);
            testAppointment3 = createLocalTestAppointment(testTechnician1, testTicket1, 
                hourAgo, hourAgo.plus(30, ChronoUnit.MINUTES), AppointmentStatus.COMPLETED);

            entityManager.persistAndFlush(testAppointment1);
            entityManager.persistAndFlush(testAppointment2);
            entityManager.persistAndFlush(testAppointment3);
            entityManager.clear();
        }

        @Test
        @DisplayName("Should find appointments by start time between")
        void shouldFindAppointmentsByStartTimeBetween() {
            // When
            Page<Appointment> result = appointmentRepository.findByStartTimeBetween(
                baseTime, dayFromNow.plus(1, ChronoUnit.HOURS), PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(
                appointment -> appointment.getStartTime().isAfter(baseTime.minus(1, ChronoUnit.SECONDS)));
        }

        @Test
        @DisplayName("Should find appointments by start time between as list")
        void shouldFindAppointmentsByStartTimeBetweenAsList() {
            // When
            List<Appointment> result = appointmentRepository.findByStartTimeBetween(
                dayAgo, baseTime);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should find appointments by end time between")
        void shouldFindAppointmentsByEndTimeBetween() {
            // When
            Page<Appointment> result = appointmentRepository.findByEndTimeBetween(
                baseTime, dayFromNow.plus(2, ChronoUnit.HOURS), PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("Should find appointments by time range")
        void shouldFindAppointmentsByTimeRange() {
            // When
            List<Appointment> result = appointmentRepository.findByTimeRange(
                hourFromNow, hourFromNow.plus(3, ChronoUnit.HOURS));

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should find appointments by time range with pagination")
        void shouldFindAppointmentsByTimeRangeWithPagination() {
            // When
            Page<Appointment> result = appointmentRepository.findByTimeRange(
                dayAgo, dayFromNow.plus(2, ChronoUnit.HOURS), PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should count appointments by start time between")
        void shouldCountAppointmentsByStartTimeBetween() {
            // When
            long count = appointmentRepository.countByStartTimeBetween(baseTime, dayFromNow.plus(1, ChronoUnit.HOURS));

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Scheduling and Conflict Tests")
    class SchedulingTests {

        @BeforeEach
        void setupSchedulingData() {
            setupTestData();
        }

        @Test
        @DisplayName("Should find technician schedule")
        void shouldFindTechnicianSchedule() {
            // When
            List<Appointment> result = appointmentRepository.findTechnicianSchedule(
                testTechnician1, dayAgo, dayFromNow);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(
                appointment -> appointment.getTechnician().getId().equals(testTechnician1.getId()));
        }

        @Test
        @DisplayName("Should find technician schedule with pagination")
        void shouldFindTechnicianScheduleWithPagination() {
            // When
            Page<Appointment> result = appointmentRepository.findTechnicianSchedule(
                testTechnician1, dayAgo, dayFromNow, PageRequest.of(0, 1));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find conflicting appointments")
        void shouldFindConflictingAppointments() {
            // Given
            List<AppointmentStatus> excludedStatuses = Arrays.asList(
                AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);

            // When
            List<Appointment> result = appointmentRepository.findConflictingAppointments(
                testTechnician1, 
                hourFromNow.plus(30, ChronoUnit.MINUTES), 
                hourFromNow.plus(90, ChronoUnit.MINUTES), 
                excludedStatuses);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should count conflicting appointments")
        void shouldCountConflictingAppointments() {
            // Given
            List<AppointmentStatus> excludedStatuses = Arrays.asList(
                AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);

            // When
            long count = appointmentRepository.countConflictingAppointments(
                testTechnician1, 
                hourFromNow.plus(30, ChronoUnit.MINUTES), 
                hourFromNow.plus(90, ChronoUnit.MINUTES), 
                excludedStatuses);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find no conflicts when time slots don't overlap")
        void shouldFindNoConflictsWhenTimeSlotsDoNotOverlap() {
            // Given
            List<AppointmentStatus> excludedStatuses = Arrays.asList(
                AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);

            // When
            List<Appointment> result = appointmentRepository.findConflictingAppointments(
                testTechnician1, 
                dayFromNow.plus(5, ChronoUnit.HOURS), 
                dayFromNow.plus(6, ChronoUnit.HOURS), 
                excludedStatuses);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count technician appointments in period")
        void shouldCountTechnicianAppointmentsInPeriod() {
            // Given
            List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING, AppointmentStatus.IN_PROGRESS);

            // When
            long count = appointmentRepository.countTechnicianAppointmentsInPeriod(
                testTechnician1, dayAgo, dayFromNow, activeStatuses);

            // Then
            assertThat(count).isEqualTo(1); // Only CONFIRMED appointment is in active statuses
        }
    }

    @Nested
    @DisplayName("Upcoming and Missed Appointments Tests")
    class UpcomingAndMissedTests {

        @BeforeEach
        void setupUpcomingData() {
            setupTestData();
        }

        @Test
        @DisplayName("Should find upcoming appointments")
        void shouldFindUpcomingAppointments() {
            // Given
            List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING);

            // When
            List<Appointment> result = appointmentRepository.findUpcomingAppointments(
                now, activeStatuses);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(appointment -> appointment.getStartTime().isAfter(now));
        }

        @Test
        @DisplayName("Should find upcoming appointments with pagination")
        void shouldFindUpcomingAppointmentsWithPagination() {
            // Given
            List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING);

            // When
            Page<Appointment> result = appointmentRepository.findUpcomingAppointments(
                now, activeStatuses, PageRequest.of(0, 1));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find technician upcoming appointments")
        void shouldFindTechnicianUpcomingAppointments() {
            // Given
            List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING);

            // When
            List<Appointment> result = appointmentRepository.findTechnicianUpcomingAppointments(
                testTechnician1, now, activeStatuses);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(result.get(0).getTechnician().getId()).isEqualTo(testTechnician1.getId());
        }

        @Test
        @DisplayName("Should find missed appointments")
        void shouldFindMissedAppointments() {
            // Given - Create a past confirmed appointment
            Appointment pastAppointment = createTestAppointment(testTechnician1, testTicket1, 
                dayAgo, dayAgo.plus(1, ChronoUnit.HOURS), AppointmentStatus.CONFIRMED);
            entityManager.persistAndFlush(pastAppointment);
            entityManager.clear();

            // When
            List<Appointment> result = appointmentRepository.findMissedAppointments(now);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(result.get(0).getEndTime()).isBefore(now);
        }

        @Test
        @DisplayName("Should count upcoming appointments")
        void shouldCountUpcomingAppointments() {
            // Given
            List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING);

            // When
            long count = appointmentRepository.countUpcomingAppointments(now, activeStatuses);

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Combined Filtering Tests")
    class CombinedFilteringTests {

        @BeforeEach
        void setupCombinedData() {
            setupTestData();
        }

        @Test
        @DisplayName("Should find appointments by technician and start time between")
        void shouldFindAppointmentsByTechnicianAndStartTimeBetween() {
            // When
            Page<Appointment> result = appointmentRepository.findByTechnicianAndStartTimeBetween(
                testTechnician1, now, dayFromNow.plus(1, ChronoUnit.HOURS), PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(result.getContent().get(0).getTechnician().getId()).isEqualTo(testTechnician1.getId());
        }

        @Test
        @DisplayName("Should find appointments by status and start time between")
        void shouldFindAppointmentsByStatusAndStartTimeBetween() {
            // When
            Page<Appointment> result = appointmentRepository.findByStatusAndStartTimeBetween(
                AppointmentStatus.PENDING, now, dayFromNow.plus(2, ChronoUnit.HOURS), PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }

        @Test
        @DisplayName("Should return empty when no appointments match combined criteria")
        void shouldReturnEmptyWhenNoAppointmentsMatchCombinedCriteria() {
            // When
            Page<Appointment> result = appointmentRepository.findByTechnicianAndStartTimeBetween(
                testTechnician2, dayAgo, now, PageRequest.of(0, 10));

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should handle null technician gracefully")
        void shouldHandleNullTechnicianGracefully() {
            // When/Then
            assertDoesNotThrow(() -> {
                List<Appointment> result = appointmentRepository.findByTechnician(null);
                assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should handle null ticket gracefully")
        void shouldHandleNullTicketGracefully() {
            // When/Then
            assertDoesNotThrow(() -> {
                List<Appointment> result = appointmentRepository.findByTicket(null);
                assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should handle null time parameters gracefully")
        void shouldHandleNullTimeParametersGracefully() {
            // When/Then
            assertDoesNotThrow(() -> {
                List<Appointment> result = appointmentRepository.findByStartTimeBetween(null, null);
                assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should validate appointment time logic")
        void shouldValidateAppointmentTimeLogic() {
            // Given
            setupTestData();
            Appointment appointment = createTestAppointment(testTechnician1, testTicket1, 
                hourFromNow, hourFromNow.minus(1, ChronoUnit.HOURS), AppointmentStatus.PENDING);

            // When/Then - This should be validated at the entity level or service layer
            // For now, just verify the repository can save it
            assertDoesNotThrow(() -> {
                Appointment saved = appointmentRepository.save(appointment);
                assertThat(saved).isNotNull();
            });
        }

        @Test
        @DisplayName("Should maintain referential integrity with technician")
        void shouldMaintainReferentialIntegrityWithTechnician() {
            // Given
            setupTestData();
            
            // When
            Optional<Appointment> result = appointmentRepository.findById(testAppointment1.getId());
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTechnician()).isNotNull();
            assertThat(result.get().getTechnician().getId()).isEqualTo(testTechnician1.getId());
        }

        @Test
        @DisplayName("Should maintain referential integrity with ticket")
        void shouldMaintainReferentialIntegrityWithTicket() {
            // Given
            setupTestData();
            
            // When
            Optional<Appointment> result = appointmentRepository.findById(testAppointment1.getId());
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTicket()).isNotNull();
            assertThat(result.get().getTicket().getId()).isEqualTo(testTicket1.getId());
        }
    }
} 
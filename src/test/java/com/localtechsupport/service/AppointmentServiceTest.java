package com.localtechsupport.service;

import com.localtechsupport.entity.*;
import com.localtechsupport.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private TechnicianRepository technicianRepository;
    
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    // Test data - timestamps
    private Instant now;
    private Instant startTime;
    private Instant endTime;
    private Instant pastTime;
    private Instant futureTime;

    // Test data - entities
    private Technician activeTechnician;
    private Technician inactiveTechnician;
    private Ticket openTicket;
    private Ticket closedTicket;
    private Client activeClient;
    private Appointment pendingAppointment;
    private Appointment confirmedAppointment;
    private Appointment completedAppointment;

    @BeforeEach
    void setUp() {
        // Initialize timestamps
        now = Instant.now();
        startTime = now.plus(2, ChronoUnit.HOURS);
        endTime = startTime.plus(1, ChronoUnit.HOURS);
        pastTime = now.minus(1, ChronoUnit.HOURS);
        futureTime = now.plus(1, ChronoUnit.DAYS);

        // Setup test entities
        setupTestEntities();
    }

    private void setupTestEntities() {
        // Create active client
        activeClient = createTestClient("john.doe@example.com", Client.ClientStatus.ACTIVE);

        // Create technicians
        activeTechnician = createTestTechnician(1L, "John Smith", "john.smith@tech.com", TechnicianStatus.ACTIVE);
        inactiveTechnician = createTestTechnician(2L, "Jane Doe", "jane.doe@tech.com", TechnicianStatus.INACTIVE);

        // Create tickets
        openTicket = createTestTicket(1L, activeClient, TicketStatus.OPEN);
        closedTicket = createTestTicket(2L, activeClient, TicketStatus.CLOSED);

        // Create appointments
        pendingAppointment = createTestAppointment(1L, activeTechnician, openTicket, startTime, endTime, AppointmentStatus.PENDING);
        confirmedAppointment = createTestAppointment(2L, activeTechnician, openTicket, startTime, endTime, AppointmentStatus.CONFIRMED);
        completedAppointment = createTestAppointment(3L, activeTechnician, openTicket, pastTime, pastTime.plus(1, ChronoUnit.HOURS), AppointmentStatus.COMPLETED);
    }

    // === TEST HELPER METHODS ===

    private Client createTestClient(String email, Client.ClientStatus status) {
        Client client = new Client();
        client.setId(1L);
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setEmail(email);
        client.setStatus(status);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return client;
    }

    private Technician createTestTechnician(Long id, String fullName, String email, TechnicianStatus status) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setFullName(fullName);
        technician.setEmail(email);
        technician.setStatus(status);
        return technician;
    }

    private Ticket createTestTicket(Long id, Client client, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setClient(client);
        ticket.setDescription("Test ticket description");
        ticket.setServiceType(ServiceType.HARDWARE);
        ticket.setStatus(status);
        ticket.setCreatedAt(now);
        ticket.setDueAt(futureTime);
        return ticket;
    }

    private Appointment createTestAppointment(Long id, Technician technician, Ticket ticket, 
                                           Instant startTime, Instant endTime, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setTechnician(technician);
        appointment.setTicket(ticket);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(status);
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        return appointment;
    }

    // === CORE APPOINTMENT CREATION TESTS ===

    @Nested
    @DisplayName("Appointment Creation Tests")
    class AppointmentCreationTests {

        @Test
        @DisplayName("Should create appointment successfully with valid inputs")
        void shouldCreateAppointmentSuccessfullyWithValidInputs() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findConflictingAppointments(
                eq(activeTechnician), eq(startTime), eq(endTime), any())).thenReturn(new ArrayList<>());
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(pendingAppointment);

            // When
            Appointment result = appointmentService.createAppointment(1L, 1L, startTime, endTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTechnician()).isEqualTo(activeTechnician);
            assertThat(result.getTicket()).isEqualTo(openTicket);
            assertThat(result.getStartTime()).isEqualTo(startTime);
            assertThat(result.getEndTime()).isEqualTo(endTime);
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);

            // Verify interactions
            verify(technicianRepository).findById(1L);
            verify(ticketRepository).findById(1L);
            verify(appointmentRepository).findConflictingAppointments(
                eq(activeTechnician), eq(startTime), eq(endTime), any());
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should verify appointment object passed to save method")
        void shouldVerifyAppointmentObjectPassedToSaveMethod() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(new ArrayList<>());
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(pendingAppointment);

            // When
            appointmentService.createAppointment(1L, 1L, startTime, endTime);

            // Then
            ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(appointmentCaptor.capture());

            Appointment capturedAppointment = appointmentCaptor.getValue();
            assertThat(capturedAppointment.getTechnician()).isEqualTo(activeTechnician);
            assertThat(capturedAppointment.getTicket()).isEqualTo(openTicket);
            assertThat(capturedAppointment.getStartTime()).isEqualTo(startTime);
            assertThat(capturedAppointment.getEndTime()).isEqualTo(endTime);
            assertThat(capturedAppointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw exception when technician not found")
        void shouldThrowExceptionWhenTechnicianNotFound() {
            // Given
            when(technicianRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(999L, 1L, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician not found with ID: 999");

            // Verify no other repository calls were made
            verify(ticketRepository, never()).findById(any());
            verify(appointmentRepository, never()).findConflictingAppointments(any(), any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when ticket not found")
        void shouldThrowExceptionWhenTicketNotFound() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 999L, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ticket not found with ID: 999");

            // Verify no appointment creation calls were made
            verify(appointmentRepository, never()).findConflictingAppointments(any(), any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when technician is inactive")
        void shouldThrowExceptionWhenTechnicianIsInactive() {
            // Given
            when(technicianRepository.findById(2L)).thenReturn(Optional.of(inactiveTechnician));

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(2L, 1L, startTime, endTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot schedule appointments with inactive technician: " + 
                    inactiveTechnician.getFullName());

            // Verify no further processing occurred
            verify(ticketRepository, never()).findById(any());
            verify(appointmentRepository, never()).findConflictingAppointments(any(), any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when ticket is closed")
        void shouldThrowExceptionWhenTicketIsClosed() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(2L)).thenReturn(Optional.of(closedTicket));

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 2L, startTime, endTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot schedule appointments for closed tickets");

            // Verify no appointment creation calls were made
            verify(appointmentRepository, never()).findConflictingAppointments(any(), any(), any(), any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when technician has conflicts")
        void shouldThrowExceptionWhenTechnicianHasConflicts() {
            // Given
            List<Appointment> conflicts = Arrays.asList(confirmedAppointment);
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findConflictingAppointments(
                eq(activeTechnician), eq(startTime), eq(endTime), any())).thenReturn(conflicts);

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, endTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Technician " + activeTechnician.getFullName() + 
                    " has conflicting appointments during the requested time slot");

            // Verify no save occurred
            verify(appointmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Time Validation Tests")  
    class TimeValidationTests {

        @Test
        @DisplayName("Should throw exception when start time is null")
        void shouldThrowExceptionWhenStartTimeIsNull() {
            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, null, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time and end time cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when end time is null")
        void shouldThrowExceptionWhenEndTimeIsNull() {
            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time and end time cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when end time is not after start time")
        void shouldThrowExceptionWhenEndTimeIsNotAfterStartTime() {
            // Given
            Instant invalidEndTime = startTime.minus(1, ChronoUnit.HOURS);

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, invalidEndTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
        }

        @Test
        @DisplayName("Should throw exception when start time is in the past")
        void shouldThrowExceptionWhenStartTimeIsInThePast() {
            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, pastTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot schedule appointments in the past");
        }

        @Test
        @DisplayName("Should throw exception when appointment is too short")
        void shouldThrowExceptionWhenAppointmentIsTooShort() {
            // Given
            Instant shortEndTime = startTime.plus(15, ChronoUnit.MINUTES);

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, shortEndTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointments must be at least 30 minutes long");
        }

        @Test
        @DisplayName("Should throw exception when appointment is too long")
        void shouldThrowExceptionWhenAppointmentIsTooLong() {
            // Given
            Instant longEndTime = startTime.plus(9, ChronoUnit.HOURS);

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, longEndTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointments cannot be longer than 8 hours");
        }

        @Test
        @DisplayName("Should accept appointment with exactly 30 minutes duration")
        void shouldAcceptAppointmentWithExactly30MinutesDuration() {
            // Given
            Instant exactEndTime = startTime.plus(30, ChronoUnit.MINUTES);
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(new ArrayList<>());
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(pendingAppointment);

            // When & Then
            assertThatCode(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, exactEndTime))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept appointment with exactly 8 hours duration")
        void shouldAcceptAppointmentWithExactly8HoursDuration() {
            // Given
            Instant exactEndTime = startTime.plus(8, ChronoUnit.HOURS);
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(new ArrayList<>());
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(pendingAppointment);

            // When & Then
            assertThatCode(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, exactEndTime))
                .doesNotThrowAnyException();
        }
    }

    // === STATUS MANAGEMENT TESTS ===

    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {

        @Test
        @DisplayName("Should update appointment status successfully with valid transition")
        void shouldUpdateAppointmentStatusSuccessfullyWithValidTransition() {
            // Given
            Appointment updatedAppointment = createTestAppointment(1L, activeTechnician, openTicket, 
                startTime, endTime, AppointmentStatus.CONFIRMED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(pendingAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(updatedAppointment);

            // When
            Appointment result = appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

            // Verify the appointment was updated and saved
            ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(appointmentCaptor.capture());
            assertThat(appointmentCaptor.getValue().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should throw exception when appointment not found for status update")
        void shouldThrowExceptionWhenAppointmentNotFoundForStatusUpdate() {
            // Given
            when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.updateAppointmentStatus(999L, AppointmentStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointment not found with ID: 999");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for invalid status transition")
        void shouldThrowExceptionForInvalidStatusTransition() {
            // Given
            when(appointmentRepository.findById(3L)).thenReturn(Optional.of(completedAppointment));

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.updateAppointmentStatus(3L, AppointmentStatus.PENDING))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition from " + 
                    AppointmentStatus.COMPLETED + " to " + AppointmentStatus.PENDING);

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow transition from PENDING to CONFIRMED")
        void shouldAllowTransitionFromPendingToConfirmed() {
            // Given
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(pendingAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(confirmedAppointment);

            // When & Then
            assertThatCode(() -> 
                appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow transition from PENDING to CANCELLED")
        void shouldAllowTransitionFromPendingToCancelled() {
            // Given
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(pendingAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(pendingAppointment);

            // When & Then
            assertThatCode(() -> 
                appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CANCELLED))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow transition from CONFIRMED to IN_PROGRESS")
        void shouldAllowTransitionFromConfirmedToInProgress() {
            // Given
            when(appointmentRepository.findById(2L)).thenReturn(Optional.of(confirmedAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(confirmedAppointment);

            // When & Then
            assertThatCode(() -> 
                appointmentService.updateAppointmentStatus(2L, AppointmentStatus.IN_PROGRESS))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow transition from CONFIRMED to NO_SHOW")
        void shouldAllowTransitionFromConfirmedToNoShow() {
            // Given
            when(appointmentRepository.findById(2L)).thenReturn(Optional.of(confirmedAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(confirmedAppointment);

            // When & Then
            assertThatCode(() -> 
                appointmentService.updateAppointmentStatus(2L, AppointmentStatus.NO_SHOW))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Cancellation Tests")
    class CancellationTests {

        @Test
        @DisplayName("Should cancel pending appointment successfully")
        void shouldCancelPendingAppointmentSuccessfully() {
            // Given
            Appointment cancelledAppointment = createTestAppointment(1L, activeTechnician, openTicket, 
                startTime, endTime, AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(pendingAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(cancelledAppointment);

            // When
            Appointment result = appointmentService.cancelAppointment(1L, "Client requested cancellation");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);

            // Verify the appointment was updated to cancelled
            ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(appointmentCaptor.capture());
            assertThat(appointmentCaptor.getValue().getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should cancel confirmed appointment successfully")
        void shouldCancelConfirmedAppointmentSuccessfully() {
            // Given
            Appointment cancelledAppointment = createTestAppointment(2L, activeTechnician, openTicket, 
                startTime, endTime, AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(2L)).thenReturn(Optional.of(confirmedAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(cancelledAppointment);

            // When
            Appointment result = appointmentService.cancelAppointment(2L, "Emergency cancellation");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw exception when trying to cancel completed appointment")
        void shouldThrowExceptionWhenTryingToCancelCompletedAppointment() {
            // Given
            when(appointmentRepository.findById(3L)).thenReturn(Optional.of(completedAppointment));

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.cancelAppointment(3L, "Late cancellation"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel a completed appointment");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when trying to cancel already cancelled appointment")
        void shouldThrowExceptionWhenTryingToCancelAlreadyCancelledAppointment() {
            // Given
            Appointment cancelledAppointment = createTestAppointment(4L, activeTechnician, openTicket, 
                startTime, endTime, AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(4L)).thenReturn(Optional.of(cancelledAppointment));

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.cancelAppointment(4L, "Double cancellation"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Appointment is already cancelled");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when appointment not found for cancellation")
        void shouldThrowExceptionWhenAppointmentNotFoundForCancellation() {
            // Given
            when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.cancelAppointment(999L, "Cancellation reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointment not found with ID: 999");

            verify(appointmentRepository, never()).save(any());
        }
    }

    // === SEARCH AND RETRIEVAL TESTS ===

    @Nested
    @DisplayName("Search and Retrieval Tests")
    class SearchAndRetrievalTests {

        @Test
        @DisplayName("Should find appointment by ID successfully")
        void shouldFindAppointmentByIdSuccessfully() {
            // Given
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(pendingAppointment));

            // When
            Optional<Appointment> result = appointmentService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(pendingAppointment);
            verify(appointmentRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty optional when appointment not found by ID")
        void shouldReturnEmptyOptionalWhenAppointmentNotFoundById() {
            // Given
            when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Appointment> result = appointmentService.findById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(appointmentRepository).findById(999L);
        }

        @Test
        @DisplayName("Should get appointment by ID successfully")
        void shouldGetAppointmentByIdSuccessfully() {
            // Given
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(pendingAppointment));

            // When
            Appointment result = appointmentService.getAppointmentById(1L);

            // Then
            assertThat(result).isEqualTo(pendingAppointment);
            verify(appointmentRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when getting appointment by non-existent ID")
        void shouldThrowExceptionWhenGettingAppointmentByNonExistentId() {
            // Given
            when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> appointmentService.getAppointmentById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointment not found with ID: 999");
        }

        @Test
        @DisplayName("Should find all appointments with pagination")
        void shouldFindAllAppointmentsWithPagination() {
            // Given
            List<Appointment> appointments = Arrays.asList(pendingAppointment, confirmedAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(appointments);
            Pageable pageable = mock(Pageable.class);
            when(appointmentRepository.findAll(pageable)).thenReturn(appointmentPage);

            // When
            Page<Appointment> result = appointmentService.findAllAppointments(pageable);

            // Then
            assertThat(result).isEqualTo(appointmentPage);
            assertThat(result.getContent()).hasSize(2);
            verify(appointmentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should find appointments by status with pagination")
        void shouldFindAppointmentsByStatusWithPagination() {
            // Given
            List<Appointment> pendingAppointments = Arrays.asList(pendingAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(pendingAppointments);
            Pageable pageable = mock(Pageable.class);
            when(appointmentRepository.findByStatus(AppointmentStatus.PENDING, pageable)).thenReturn(appointmentPage);

            // When
            Page<Appointment> result = appointmentService.findAppointmentsByStatus(AppointmentStatus.PENDING, pageable);

            // Then
            assertThat(result).isEqualTo(appointmentPage);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.PENDING);
            verify(appointmentRepository).findByStatus(AppointmentStatus.PENDING, pageable);
        }

        @Test
        @DisplayName("Should find appointments by technician with pagination")
        void shouldFindAppointmentsByTechnicianWithPagination() {
            // Given
            List<Appointment> technicianAppointments = Arrays.asList(pendingAppointment, confirmedAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(technicianAppointments);
            Pageable pageable = mock(Pageable.class);
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(appointmentRepository.findByTechnician(activeTechnician, pageable)).thenReturn(appointmentPage);

            // When
            Page<Appointment> result = appointmentService.findAppointmentsByTechnician(1L, pageable);

            // Then
            assertThat(result).isEqualTo(appointmentPage);
            assertThat(result.getContent()).hasSize(2);
            verify(technicianRepository).findById(1L);
            verify(appointmentRepository).findByTechnician(activeTechnician, pageable);
        }

        @Test
        @DisplayName("Should find appointments by ticket with pagination")
        void shouldFindAppointmentsByTicketWithPagination() {
            // Given
            List<Appointment> ticketAppointments = Arrays.asList(pendingAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(ticketAppointments);
            Pageable pageable = mock(Pageable.class);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findByTicket(openTicket, pageable)).thenReturn(appointmentPage);

            // When
            Page<Appointment> result = appointmentService.findAppointmentsByTicket(1L, pageable);

            // Then
            assertThat(result).isEqualTo(appointmentPage);
            assertThat(result.getContent()).hasSize(1);
            verify(ticketRepository).findById(1L);
            verify(appointmentRepository).findByTicket(openTicket, pageable);
        }

        @Test
        @DisplayName("Should find technician schedule")
        void shouldFindTechnicianSchedule() {
            // Given
            List<Appointment> schedule = Arrays.asList(pendingAppointment, confirmedAppointment);
            Instant startDate = now.truncatedTo(ChronoUnit.DAYS);
            Instant endDate = startDate.plus(1, ChronoUnit.DAYS);
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(appointmentRepository.findTechnicianSchedule(activeTechnician, startDate, endDate)).thenReturn(schedule);

            // When
            List<Appointment> result = appointmentService.findTechnicianSchedule(1L, startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(pendingAppointment, confirmedAppointment);
            verify(technicianRepository).findById(1L);
            verify(appointmentRepository).findTechnicianSchedule(activeTechnician, startDate, endDate);
        }

        @Test
        @DisplayName("Should find upcoming appointments")
        void shouldFindUpcomingAppointments() {
            // Given
            List<Appointment> upcomingAppointments = Arrays.asList(pendingAppointment, confirmedAppointment);
            when(appointmentRepository.findUpcomingAppointments(any(Instant.class), any())).thenReturn(upcomingAppointments);

            // When
            List<Appointment> result = appointmentService.findUpcomingAppointments();

            // Then
            assertThat(result).hasSize(2);
            verify(appointmentRepository).findUpcomingAppointments(any(Instant.class), 
                eq(Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS)));
        }
    }

    // === AVAILABILITY AND CONFLICT CHECKING TESTS ===

    @Nested
    @DisplayName("Availability and Conflict Checking Tests")
    class AvailabilityAndConflictCheckingTests {

        @Test
        @DisplayName("Should return true when technician is available")
        void shouldReturnTrueWhenTechnicianIsAvailable() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(appointmentRepository.countConflictingAppointments(
                eq(activeTechnician), eq(startTime), eq(endTime), any())).thenReturn(0L);

            // When
            boolean result = appointmentService.isTechnicianAvailable(1L, startTime, endTime);

            // Then
            assertThat(result).isTrue();
            verify(technicianRepository).findById(1L);
            verify(appointmentRepository).countConflictingAppointments(
                eq(activeTechnician), eq(startTime), eq(endTime), 
                eq(Arrays.asList(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)));
        }

        @Test
        @DisplayName("Should return false when technician has conflicts")
        void shouldReturnFalseWhenTechnicianHasConflicts() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(appointmentRepository.countConflictingAppointments(
                eq(activeTechnician), eq(startTime), eq(endTime), any())).thenReturn(2L);

            // When
            boolean result = appointmentService.isTechnicianAvailable(1L, startTime, endTime);

            // Then
            assertThat(result).isFalse();
            verify(technicianRepository).findById(1L);
            verify(appointmentRepository).countConflictingAppointments(
                eq(activeTechnician), eq(startTime), eq(endTime), any());
        }

        @Test
        @DisplayName("Should throw exception when checking availability for non-existent technician")
        void shouldThrowExceptionWhenCheckingAvailabilityForNonExistentTechnician() {
            // Given
            when(technicianRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.isTechnicianAvailable(999L, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician not found with ID: 999");
        }

        @Test
        @DisplayName("Should find available time slots")
        void shouldFindAvailableTimeSlots() {
            // Given
            Instant date = now.truncatedTo(ChronoUnit.DAYS);
            int durationMinutes = 60;

            // When
            List<Instant> result = appointmentService.findAvailableTimeSlots(1L, date, durationMinutes);

            // Then
            // This method currently returns empty list as noted in the service implementation
            assertThat(result).isEmpty();
        }
    }

    // === STATISTICS AND ANALYTICS TESTS ===

    @Nested
    @DisplayName("Statistics and Analytics Tests")
    class StatisticsAndAnalyticsTests {

        @Test
        @DisplayName("Should count appointments by status")
        void shouldCountAppointmentsByStatus() {
            // Given
            when(appointmentRepository.countByStatus(AppointmentStatus.PENDING)).thenReturn(5L);

            // When
            long result = appointmentService.countAppointmentsByStatus(AppointmentStatus.PENDING);

            // Then
            assertThat(result).isEqualTo(5L);
            verify(appointmentRepository).countByStatus(AppointmentStatus.PENDING);
        }

        @Test
        @DisplayName("Should count upcoming appointments")
        void shouldCountUpcomingAppointments() {
            // Given
            when(appointmentRepository.countUpcomingAppointments(any(Instant.class), any())).thenReturn(3L);

            // When
            long result = appointmentService.countUpcomingAppointments();

            // Then
            assertThat(result).isEqualTo(3L);
            verify(appointmentRepository).countUpcomingAppointments(any(Instant.class), 
                eq(Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS)));
        }

        @Test
        @DisplayName("Should get appointment counts by technician")
        void shouldGetAppointmentCountsByTechnician() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician, inactiveTechnician);
            when(technicianRepository.findAll()).thenReturn(technicians);
            when(appointmentRepository.countByTechnician(activeTechnician)).thenReturn(5L);
            when(appointmentRepository.countByTechnician(inactiveTechnician)).thenReturn(2L);

            // When
            Map<Long, Long> result = appointmentService.getAppointmentCountsByTechnician();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(1L)).isEqualTo(5L);
            assertThat(result.get(2L)).isEqualTo(2L);
            verify(technicianRepository).findAll();
            verify(appointmentRepository).countByTechnician(activeTechnician);
            verify(appointmentRepository).countByTechnician(inactiveTechnician);
        }
    }

    // === ERROR HANDLING TESTS ===

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(appointmentRepository.findById(1L)).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> appointmentService.getAppointmentById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");
        }

    }

    // === EDGE CASES TESTS ===

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty conflict list")
        void shouldHandleEmptyConflictList() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(new ArrayList<>());
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(pendingAppointment);

            // When & Then
            assertThatCode(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, endTime))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle multiple conflicts correctly")
        void shouldHandleMultipleConflictsCorrectly() {
            // Given
            List<Appointment> multipleConflicts = Arrays.asList(pendingAppointment, confirmedAppointment);
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(multipleConflicts);

            // When & Then
            assertThatThrownBy(() -> 
                appointmentService.createAppointment(1L, 1L, startTime, endTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has conflicting appointments during the requested time slot");
        }

        @Test
        @DisplayName("Should handle zero count for availability check")
        void shouldHandleZeroCountForAvailabilityCheck() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(appointmentRepository.countConflictingAppointments(any(), any(), any(), any())).thenReturn(0L);

            // When
            boolean result = appointmentService.isTechnicianAvailable(1L, startTime, endTime);

            // Then
            assertThat(result).isTrue();
        }
    }
}
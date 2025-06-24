package com.localtechsupport.controller;

import com.localtechsupport.dto.request.CreateAppointmentRequest;
import com.localtechsupport.dto.request.UpdateAppointmentRequest;
import com.localtechsupport.dto.response.AppointmentResponse;
import com.localtechsupport.dto.response.TechnicianSummaryResponse;
import com.localtechsupport.entity.*;
import com.localtechsupport.service.AppointmentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController Tests")
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    // Test data
    private Appointment testAppointment;
    private Technician testTechnician;
    private Ticket testTicket;
    private Client testClient;
    private CreateAppointmentRequest createRequest;
    private UpdateAppointmentRequest updateRequest;
    private Instant startTime;
    private Instant endTime;

    @BeforeEach
    void setUp() {
        // Setup test data
        startTime = Instant.now().plus(1, ChronoUnit.HOURS);
        endTime = startTime.plus(2, ChronoUnit.HOURS);

        testClient = createTestClient(1L, "John", "Doe", "john@example.com");
        testTechnician = createTestTechnician(1L, "Jane", "Smith", "jane@example.com");
        testTicket = createTestTicket(1L, testClient, "Computer won't start");
        testAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);

        createRequest = new CreateAppointmentRequest();
        createRequest.setTechnicianId(1L);
        createRequest.setTicketId(1L);
        createRequest.setStartTime(startTime);
        createRequest.setEndTime(endTime);

        updateRequest = new UpdateAppointmentRequest();
        updateRequest.setStatus(AppointmentStatus.CONFIRMED);
    }

    @Nested
    @DisplayName("Core CRUD Operations")
    class CoreCrudOperationTests {

        @Test
        @DisplayName("Should create appointment successfully")
        void shouldCreateAppointmentSuccessfully() {
            // Given
            when(appointmentService.createAppointment(1L, 1L, startTime, endTime))
                .thenReturn(testAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.createAppointment(createRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getStartTime()).isEqualTo(startTime);
            assertThat(response.getBody().getEndTime()).isEqualTo(endTime);
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.PENDING);

            verify(appointmentService).createAppointment(1L, 1L, startTime, endTime);
        }

        @Test
        @DisplayName("Should throw exception when creating appointment with invalid data")
        void shouldThrowExceptionWhenCreatingAppointmentWithInvalidData() {
            // Given
            when(appointmentService.createAppointment(1L, 1L, startTime, endTime))
                .thenThrow(new IllegalArgumentException("Invalid appointment data"));

            // When & Then
            assertThatThrownBy(() -> appointmentController.createAppointment(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid appointment data");

            verify(appointmentService).createAppointment(1L, 1L, startTime, endTime);
        }

        @Test
        @DisplayName("Should get appointment by ID successfully")
        void shouldGetAppointmentByIdSuccessfully() {
            // Given
            when(appointmentService.findById(1L)).thenReturn(Optional.of(testAppointment));

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.getAppointment(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getTechnician().getId()).isEqualTo(1L);
            assertThat(response.getBody().getTicket().getId()).isEqualTo(1L);

            verify(appointmentService).findById(1L);
        }

        @Test
        @DisplayName("Should return not found when appointment doesn't exist")
        void shouldReturnNotFoundWhenAppointmentDoesNotExist() {
            // Given
            when(appointmentService.findById(999L)).thenReturn(Optional.empty());

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.getAppointment(999L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();

            verify(appointmentService).findById(999L);
        }

        @Test
        @DisplayName("Should get all appointments with default pagination")
        void shouldGetAllAppointmentsWithDefaultPagination() {
            // Given
            List<Appointment> appointments = Arrays.asList(testAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(appointments);
            when(appointmentService.findAllAppointments(any(Pageable.class)))
                .thenReturn(appointmentPage);

            // When
            ResponseEntity<Page<AppointmentResponse>> response = 
                appointmentController.getAllAppointments(0, 20, "startTime", "asc", null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(1L);

            verify(appointmentService).findAllAppointments(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Status Management Operations")
    class StatusManagementTests {

        @Test
        @DisplayName("Should update appointment status successfully")
        void shouldUpdateAppointmentStatusSuccessfully() {
            // Given
            Appointment confirmedAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);
            confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
            when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED))
                .thenReturn(confirmedAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = 
                appointmentController.updateStatus(1L, updateRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

            verify(appointmentService).updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should confirm appointment successfully")
        void shouldConfirmAppointmentSuccessfully() {
            // Given
            Appointment confirmedAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);
            confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
            when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED))
                .thenReturn(confirmedAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.confirmAppointment(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

            verify(appointmentService).updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should start appointment successfully")
        void shouldStartAppointmentSuccessfully() {
            // Given
            Appointment inProgressAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);
            inProgressAppointment.setStatus(AppointmentStatus.IN_PROGRESS);
            when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.IN_PROGRESS))
                .thenReturn(inProgressAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.startAppointment(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);

            verify(appointmentService).updateAppointmentStatus(1L, AppointmentStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should complete appointment successfully")
        void shouldCompleteAppointmentSuccessfully() {
            // Given
            Appointment completedAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);
            completedAppointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.COMPLETED))
                .thenReturn(completedAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.completeAppointment(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.COMPLETED);

            verify(appointmentService).updateAppointmentStatus(1L, AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should cancel appointment successfully")
        void shouldCancelAppointmentSuccessfully() {
            // Given
            Appointment cancelledAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);
            cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
            when(appointmentService.cancelAppointment(1L, "Client request"))
                .thenReturn(cancelledAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = 
                appointmentController.cancelAppointment(1L, "Client request");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.CANCELLED);

            verify(appointmentService).cancelAppointment(1L, "Client request");
        }

        @Test
        @DisplayName("Should cancel appointment without reason")
        void shouldCancelAppointmentWithoutReason() {
            // Given
            Appointment cancelledAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);
            cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
            when(appointmentService.cancelAppointment(1L, null))
                .thenReturn(cancelledAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.cancelAppointment(1L, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.CANCELLED);

            verify(appointmentService).cancelAppointment(1L, null);
        }

        @Test
        @DisplayName("Should mark appointment as no-show successfully")
        void shouldMarkAppointmentAsNoShowSuccessfully() {
            // Given
            Appointment noShowAppointment = createTestAppointment(1L, testTechnician, testTicket, startTime, endTime);
            noShowAppointment.setStatus(AppointmentStatus.NO_SHOW);
            when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.NO_SHOW))
                .thenReturn(noShowAppointment);

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.markNoShow(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);

            verify(appointmentService).updateAppointmentStatus(1L, AppointmentStatus.NO_SHOW);
        }

        @Test
        @DisplayName("Should throw exception when updating status for non-existent appointment")
        void shouldThrowExceptionWhenUpdatingStatusForNonExistentAppointment() {
            // Given
            when(appointmentService.updateAppointmentStatus(999L, AppointmentStatus.CONFIRMED))
                .thenThrow(new IllegalArgumentException("Appointment not found with ID: 999"));

            // When & Then
            assertThatThrownBy(() -> appointmentController.updateStatus(999L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointment not found with ID: 999");

            verify(appointmentService).updateAppointmentStatus(999L, AppointmentStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("Calendar and Scheduling Operations")
    class CalendarAndSchedulingTests {

        @Test
        @DisplayName("Should get upcoming appointments successfully")
        void shouldGetUpcomingAppointmentsSuccessfully() {
            // Given
            List<Appointment> upcomingAppointments = Arrays.asList(testAppointment);
            when(appointmentService.findUpcomingAppointments()).thenReturn(upcomingAppointments);

            // When
            ResponseEntity<List<AppointmentResponse>> response = 
                appointmentController.getUpcomingAppointments();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getId()).isEqualTo(1L);

            verify(appointmentService).findUpcomingAppointments();
        }

        @Test
        @DisplayName("Should get appointments by status successfully")
        void shouldGetAppointmentsByStatusSuccessfully() {
            // Given
            List<Appointment> confirmedAppointments = Arrays.asList(testAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(confirmedAppointments);
            when(appointmentService.findAppointmentsByStatus(eq(AppointmentStatus.CONFIRMED), any(Pageable.class)))
                .thenReturn(appointmentPage);

            // When
            ResponseEntity<Page<AppointmentResponse>> response = 
                appointmentController.getAppointmentsByStatus(
                    AppointmentStatus.CONFIRMED, 0, 20, "startTime", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);

            verify(appointmentService).findAppointmentsByStatus(eq(AppointmentStatus.CONFIRMED), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get technician schedule successfully")
        void shouldGetTechnicianScheduleSuccessfully() {
            // Given
            List<Appointment> technicianAppointments = Arrays.asList(testAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(technicianAppointments);
            when(appointmentService.findAppointmentsByTechnician(eq(1L), any(Pageable.class)))
                .thenReturn(appointmentPage);

            // When
            ResponseEntity<Page<AppointmentResponse>> response = 
                appointmentController.getTechnicianSchedule(1L, 0, 20, "startTime", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getTechnician().getId()).isEqualTo(1L);

            verify(appointmentService).findAppointmentsByTechnician(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty list when no upcoming appointments")
        void shouldReturnEmptyListWhenNoUpcomingAppointments() {
            // Given
            when(appointmentService.findUpcomingAppointments()).thenReturn(Arrays.asList());

            // When
            ResponseEntity<List<AppointmentResponse>> response = 
                appointmentController.getUpcomingAppointments();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();

            verify(appointmentService).findUpcomingAppointments();
        }
    }

    @Nested
    @DisplayName("Availability Checking Operations")
    class AvailabilityCheckingTests {

        @Test
        @DisplayName("Should return true when technician is available")
        void shouldReturnTrueWhenTechnicianIsAvailable() {
            // Given
            when(appointmentService.isTechnicianAvailable(1L, startTime, endTime)).thenReturn(true);

            // When
            ResponseEntity<Boolean> response = 
                appointmentController.checkAvailability(1L, startTime, endTime);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();

            verify(appointmentService).isTechnicianAvailable(1L, startTime, endTime);
        }

        @Test
        @DisplayName("Should return false when technician is not available")
        void shouldReturnFalseWhenTechnicianIsNotAvailable() {
            // Given
            when(appointmentService.isTechnicianAvailable(1L, startTime, endTime)).thenReturn(false);

            // When
            ResponseEntity<Boolean> response = 
                appointmentController.checkAvailability(1L, startTime, endTime);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();

            verify(appointmentService).isTechnicianAvailable(1L, startTime, endTime);
        }

        @Test
        @DisplayName("Should throw exception when checking availability with invalid technician ID")
        void shouldThrowExceptionWhenCheckingAvailabilityWithInvalidTechnicianId() {
            // Given
            when(appointmentService.isTechnicianAvailable(999L, startTime, endTime))
                .thenThrow(new IllegalArgumentException("Technician not found with ID: 999"));

            // When & Then
            assertThatThrownBy(() -> appointmentController.checkAvailability(999L, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician not found with ID: 999");

            verify(appointmentService).isTechnicianAvailable(999L, startTime, endTime);
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting Operations")
    class PaginationAndSortingTests {

        @Test
        @DisplayName("Should handle custom pagination parameters")
        void shouldHandleCustomPaginationParameters() {
            // Given
            List<Appointment> appointments = Arrays.asList(testAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(appointments, PageRequest.of(1, 5), 10);
            when(appointmentService.findAllAppointments(any(Pageable.class)))
                .thenReturn(appointmentPage);

            // When
            ResponseEntity<Page<AppointmentResponse>> response = 
                appointmentController.getAllAppointments(1, 5, "endTime", "desc", null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNumber()).isEqualTo(1);
            assertThat(response.getBody().getSize()).isEqualTo(5);

            verify(appointmentService).findAllAppointments(any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter appointments by status when provided")
        void shouldFilterAppointmentsByStatusWhenProvided() {
            // Given
            List<Appointment> confirmedAppointments = Arrays.asList(testAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(confirmedAppointments);
            when(appointmentService.findAppointmentsByStatus(eq(AppointmentStatus.CONFIRMED), any(Pageable.class)))
                .thenReturn(appointmentPage);

            // When
            ResponseEntity<Page<AppointmentResponse>> response = 
                appointmentController.getAllAppointments(0, 20, "startTime", "asc", AppointmentStatus.CONFIRMED, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);

            verify(appointmentService).findAppointmentsByStatus(eq(AppointmentStatus.CONFIRMED), any(Pageable.class));
            verify(appointmentService, never()).findAllAppointments(any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter appointments by technician when provided")
        void shouldFilterAppointmentsByTechnicianWhenProvided() {
            // Given
            List<Appointment> technicianAppointments = Arrays.asList(testAppointment);
            Page<Appointment> appointmentPage = new PageImpl<>(technicianAppointments);
            when(appointmentService.findAppointmentsByTechnician(eq(1L), any(Pageable.class)))
                .thenReturn(appointmentPage);

            // When
            ResponseEntity<Page<AppointmentResponse>> response = 
                appointmentController.getAllAppointments(0, 20, "startTime", "asc", null, 1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);

            verify(appointmentService).findAppointmentsByTechnician(eq(1L), any(Pageable.class));
            verify(appointmentService, never()).findAllAppointments(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Response Mapping Operations")
    class ResponseMappingTests {

        @Test
        @DisplayName("Should map appointment to response correctly with all fields")
        void shouldMapAppointmentToResponseCorrectlyWithAllFields() {
            // Given
            when(appointmentService.findById(1L)).thenReturn(Optional.of(testAppointment));

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.getAppointment(1L);

            // Then
            assertThat(response.getBody()).isNotNull();
            AppointmentResponse appointmentResponse = response.getBody();
            
            assertThat(appointmentResponse.getId()).isEqualTo(1L);
            assertThat(appointmentResponse.getStartTime()).isEqualTo(startTime);
            assertThat(appointmentResponse.getEndTime()).isEqualTo(endTime);
            assertThat(appointmentResponse.getStatus()).isEqualTo(AppointmentStatus.PENDING);
            
            // Technician mapping
            assertThat(appointmentResponse.getTechnician()).isNotNull();
            assertThat(appointmentResponse.getTechnician().getId()).isEqualTo(1L);
            assertThat(appointmentResponse.getTechnician().getFullName()).isEqualTo("Jane Smith");
            assertThat(appointmentResponse.getTechnician().getEmail()).isEqualTo("jane@example.com");
            
            // Ticket mapping
            assertThat(appointmentResponse.getTicket()).isNotNull();
            assertThat(appointmentResponse.getTicket().getId()).isEqualTo(1L);
            assertThat(appointmentResponse.getTicket().getDescription()).isEqualTo("Computer won't start");
            
            verify(appointmentService).findById(1L);
        }

        @Test
        @DisplayName("Should handle appointment with null technician")
        void shouldHandleAppointmentWithNullTechnician() {
            // Given
            Appointment appointmentWithoutTechnician = createTestAppointment(1L, null, testTicket, startTime, endTime);
            when(appointmentService.findById(1L)).thenReturn(Optional.of(appointmentWithoutTechnician));

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.getAppointment(1L);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTechnician()).isNull();
            assertThat(response.getBody().getTicket()).isNotNull();

            verify(appointmentService).findById(1L);
        }

        @Test
        @DisplayName("Should handle appointment with null ticket")
        void shouldHandleAppointmentWithNullTicket() {
            // Given
            Appointment appointmentWithoutTicket = createTestAppointment(1L, testTechnician, null, startTime, endTime);
            when(appointmentService.findById(1L)).thenReturn(Optional.of(appointmentWithoutTicket));

            // When
            ResponseEntity<AppointmentResponse> response = appointmentController.getAppointment(1L);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTechnician()).isNotNull();
            assertThat(response.getBody().getTicket()).isNull();

            verify(appointmentService).findById(1L);
        }
    }

    @Nested
    @DisplayName("Error Handling Operations")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle service layer exceptions properly")
        void shouldHandleServiceLayerExceptionsProperly() {
            // Given
            when(appointmentService.createAppointment(anyLong(), anyLong(), any(Instant.class), any(Instant.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> appointmentController.createAppointment(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");

            verify(appointmentService).createAppointment(anyLong(), anyLong(), any(Instant.class), any(Instant.class));
        }

        @Test
        @DisplayName("Should handle null pointer exceptions gracefully")
        void shouldHandleNullPointerExceptionsGracefully() {
            // Given
            when(appointmentService.findById(1L))
                .thenThrow(new NullPointerException("Unexpected null value"));

            // When & Then
            assertThatThrownBy(() -> appointmentController.getAppointment(1L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Unexpected null value");

            verify(appointmentService).findById(1L);
        }

        @Test
        @DisplayName("Should propagate illegal argument exceptions")
        void shouldPropagateIllegalArgumentExceptions() {
            // Given
            when(appointmentService.isTechnicianAvailable(anyLong(), any(Instant.class), any(Instant.class)))
                .thenThrow(new IllegalArgumentException("Start time must be before end time"));

            // When & Then
            assertThatThrownBy(() -> appointmentController.checkAvailability(1L, endTime, startTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time must be before end time");

            verify(appointmentService).isTechnicianAvailable(anyLong(), any(Instant.class), any(Instant.class));
        }
    }

    // === HELPER METHODS ===

    private Client createTestClient(Long id, String firstName, String lastName, String email) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setStatus(Client.ClientStatus.ACTIVE);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return client;
    }

    private Technician createTestTechnician(Long id, String firstName, String lastName, String email) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setFullName(firstName + " " + lastName);
        technician.setEmail(email);
        technician.setStatus(TechnicianStatus.ACTIVE);
        return technician;
    }

    private Ticket createTestTicket(Long id, Client client, String description) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setClient(client);
        ticket.setDescription(description);
        ticket.setServiceType(ServiceType.HARDWARE);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(Instant.now());
        ticket.setDueAt(Instant.now().plus(24, ChronoUnit.HOURS));
        return ticket;
    }

    private Appointment createTestAppointment(Long id, Technician technician, Ticket ticket, 
                                            Instant startTime, Instant endTime) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setTechnician(technician);
        appointment.setTicket(ticket);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(Instant.now());
        appointment.setUpdatedAt(Instant.now());
        return appointment;
    }
}
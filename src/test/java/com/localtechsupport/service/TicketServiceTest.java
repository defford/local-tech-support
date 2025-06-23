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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit test suite for TicketService.
 * 
 * Tests all business logic, validation, error handling, and repository interactions
 * using mocked dependencies to ensure isolated unit testing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private ClientRepository clientRepository;
    
    @Mock
    private TechnicianRepository technicianRepository;
    
    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @InjectMocks
    private TicketService ticketService;

    // Test data
    private Client activeClient;
    private Client inactiveClient;
    private Technician activeTechnician;
    private Technician inactiveTechnician;
    private Ticket openTicket;
    private Ticket closedTicket;

    @BeforeEach
    void setUp() {
        // Create test entities
        activeClient = createActiveClient(1L, "active@example.com");
        inactiveClient = createInactiveClient(2L, "inactive@example.com");
        activeTechnician = createActiveTechnician(1L, "John Doe");
        inactiveTechnician = createInactiveTechnician(2L, "Jane Smith");
        openTicket = createOpenTicket(1L, activeClient);
        closedTicket = createClosedTicket(2L, activeClient);
    }

    @Nested
    @DisplayName("Create Ticket")
    class CreateTicketTests {

        @Test
        @DisplayName("Should create ticket successfully for active client")
        void shouldCreateTicketSuccessfully() {
            // Arrange
            Long clientId = 1L;
            ServiceType serviceType = ServiceType.HARDWARE;
            String description = "Computer won't start";
            
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(activeClient));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act
            Ticket result = ticketService.createTicket(clientId, serviceType, description);

            // Assert
            assertThat(result).isNotNull();
            
            // Verify repository interactions
            verify(clientRepository).findById(clientId);
            
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(ticketCaptor.capture());
            
            Ticket capturedTicket = ticketCaptor.getValue();
            assertThat(capturedTicket.getClient()).isEqualTo(activeClient);
            assertThat(capturedTicket.getServiceType()).isEqualTo(serviceType);
            assertThat(capturedTicket.getDescription()).isEqualTo(description);
            assertThat(capturedTicket.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(capturedTicket.getDueAt()).isNotNull();

            // Verify history entry creation
            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            
            TicketHistory history = historyCaptor.getValue();
            assertThat(history.getTicket()).isEqualTo(openTicket);
            assertThat(history.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(history.getDescription()).contains("Ticket created");
            assertThat(history.getCreatedBy()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("Should calculate due date correctly for hardware tickets (24 hours)")
        void shouldCalculateDueDateForHardware() {
            // Arrange
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act
            ticketService.createTicket(1L, ServiceType.HARDWARE, "Hardware issue");

            // Assert
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(ticketCaptor.capture());
            
            Ticket ticket = ticketCaptor.getValue();
            Instant expectedDueDate = Instant.now().plus(24, ChronoUnit.HOURS);
            
            // Allow 1 second tolerance for test execution time
            assertThat(ticket.getDueAt()).isBetween(
                expectedDueDate.minus(1, ChronoUnit.SECONDS),
                expectedDueDate.plus(1, ChronoUnit.SECONDS)
            );
        }

        @Test
        @DisplayName("Should calculate due date correctly for software tickets (48 hours)")
        void shouldCalculateDueDateForSoftware() {
            // Arrange
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act
            ticketService.createTicket(1L, ServiceType.SOFTWARE, "Software issue");

            // Assert
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(ticketCaptor.capture());
            
            Ticket ticket = ticketCaptor.getValue();
            Instant expectedDueDate = Instant.now().plus(48, ChronoUnit.HOURS);
            
            // Allow 1 second tolerance for test execution time
            assertThat(ticket.getDueAt()).isBetween(
                expectedDueDate.minus(1, ChronoUnit.SECONDS),
                expectedDueDate.plus(1, ChronoUnit.SECONDS)
            );
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void shouldThrowExceptionWhenClientNotFound() {
            // Arrange
            Long nonExistentClientId = 999L;
            when(clientRepository.findById(nonExistentClientId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.createTicket(nonExistentClientId, ServiceType.HARDWARE, "Test")
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Client not found with ID: " + nonExistentClientId);
            
            verify(ticketRepository, never()).save(any());
            verify(ticketHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when client is inactive")
        void shouldThrowExceptionWhenClientIsInactive() {
            // Arrange
            when(clientRepository.findById(2L)).thenReturn(Optional.of(inactiveClient));

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.createTicket(2L, ServiceType.HARDWARE, "Test")
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("Cannot create ticket for inactive client");
            
            verify(ticketRepository, never()).save(any());
            verify(ticketHistoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Assign Technician")
    class AssignTechnicianTests {

        @Test
        @DisplayName("Should assign technician successfully to open ticket")
        void shouldAssignTechnicianSuccessfully() {
            // Arrange
            Long ticketId = 1L;
            Long technicianId = 1L;
            
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(openTicket));
            when(technicianRepository.findById(technicianId)).thenReturn(Optional.of(activeTechnician));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act
            Ticket result = ticketService.assignTechnician(ticketId, technicianId);

            // Assert
            assertThat(result).isNotNull();
            
            verify(ticketRepository).findById(ticketId);
            verify(technicianRepository).findById(technicianId);
            
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(ticketCaptor.capture());
            
            assertThat(ticketCaptor.getValue().getAssignedTechnician()).isEqualTo(activeTechnician);

            // Verify history entry
            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            
            TicketHistory history = historyCaptor.getValue();
            assertThat(history.getDescription()).contains("Assigned to technician: " + activeTechnician.getFullName());
        }

        @Test
        @DisplayName("Should throw exception when ticket not found")
        void shouldThrowExceptionWhenTicketNotFound() {
            // Arrange
            Long nonExistentTicketId = 999L;
            when(ticketRepository.findById(nonExistentTicketId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.assignTechnician(nonExistentTicketId, 1L)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Ticket not found with ID: " + nonExistentTicketId);
        }

        @Test
        @DisplayName("Should throw exception when trying to assign to closed ticket")
        void shouldThrowExceptionWhenTicketIsClosed() {
            // Arrange
            when(ticketRepository.findById(2L)).thenReturn(Optional.of(closedTicket));

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.assignTechnician(2L, 1L)
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("Cannot assign technician to closed ticket");
        }

        @Test
        @DisplayName("Should throw exception when technician not found")
        void shouldThrowExceptionWhenTechnicianNotFound() {
            // Arrange
            Long nonExistentTechnicianId = 999L;
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(technicianRepository.findById(nonExistentTechnicianId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.assignTechnician(1L, nonExistentTechnicianId)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Technician not found with ID: " + nonExistentTechnicianId);
        }

        @Test
        @DisplayName("Should throw exception when technician is inactive")
        void shouldThrowExceptionWhenTechnicianIsInactive() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(technicianRepository.findById(2L)).thenReturn(Optional.of(inactiveTechnician));

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.assignTechnician(1L, 2L)
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("Cannot assign inactive technician");
        }
    }

    @Nested
    @DisplayName("Close Ticket")
    class CloseTicketTests {

        @Test
        @DisplayName("Should close ticket with resolution notes successfully")
        void shouldCloseTicketWithResolutionNotes() {
            // Arrange
            Long ticketId = 1L;
            String resolutionNotes = "Replaced faulty RAM module";
            String closedBy = "tech@example.com";
            
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(openTicket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act
            Ticket result = ticketService.closeTicket(ticketId, resolutionNotes, closedBy);

            // Assert
            assertThat(result).isNotNull();
            
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(ticketCaptor.capture());
            
            assertThat(ticketCaptor.getValue().getStatus()).isEqualTo(TicketStatus.CLOSED);

            // Verify history entry
            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            
            TicketHistory history = historyCaptor.getValue();
            assertThat(history.getDescription()).contains("Ticket closed");
            assertThat(history.getDescription()).contains("Resolution: " + resolutionNotes);
            assertThat(history.getCreatedBy()).isEqualTo(closedBy);
        }

        @Test
        @DisplayName("Should throw exception when trying to close already closed ticket")
        void shouldThrowExceptionWhenTicketAlreadyClosed() {
            // Arrange
            when(ticketRepository.findById(2L)).thenReturn(Optional.of(closedTicket));

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.closeTicket(2L, "Resolution", "user")
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("Cannot close ticket that is not open");
        }
    }

    @Nested
    @DisplayName("Update Status")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update status from OPEN to CLOSED successfully")
        void shouldUpdateStatusSuccessfully() {
            // Arrange
            Long ticketId = 1L;
            TicketStatus newStatus = TicketStatus.CLOSED;
            String reason = "Issue resolved";
            String updatedBy = "tech@example.com";
            
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(openTicket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act
            Ticket result = ticketService.updateStatus(ticketId, newStatus, reason, updatedBy);

            // Assert
            assertThat(result).isNotNull();
            
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(ticketCaptor.capture());
            
            assertThat(ticketCaptor.getValue().getStatus()).isEqualTo(newStatus);

            // Verify history entry
            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            
            TicketHistory history = historyCaptor.getValue();
            assertThat(history.getDescription()).contains("Status changed from OPEN to CLOSED");
            assertThat(history.getDescription()).contains(reason);
            assertThat(history.getCreatedBy()).isEqualTo(updatedBy);
        }

        @Test
        @DisplayName("Should update status without reason successfully")
        void shouldUpdateStatusWithoutReason() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act
            ticketService.updateStatus(1L, TicketStatus.CLOSED, null, "user");

            // Assert
            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            
            TicketHistory history = historyCaptor.getValue();
            assertThat(history.getDescription()).isEqualTo("Status changed from OPEN to CLOSED");
        }

        @Test
        @DisplayName("Should allow same status transition")
        void shouldAllowSameStatusTransition() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

            // Act & Assert
            assertThatCode(() -> 
                ticketService.updateStatus(1L, TicketStatus.OPEN, "Update notes", "user")
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Unassign Technician")
    class UnassignTechnicianTests {

        @Test
        @DisplayName("Should unassign technician with reason successfully")
        void shouldUnassignTechnicianWithReason() {
            // Arrange
            Long ticketId = 1L;
            String reason = "Technician on vacation";
            String updatedBy = "manager@example.com";
            
            Ticket assignedTicket = createOpenTicket(1L, activeClient);
            assignedTicket.setAssignedTechnician(activeTechnician);
            
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(assignedTicket));
            when(ticketRepository.save(any(Ticket.class))).thenReturn(assignedTicket);

            // Act
            Ticket result = ticketService.unassignTechnician(ticketId, reason, updatedBy);

            // Assert
            assertThat(result).isNotNull();
            
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(ticketRepository).save(ticketCaptor.capture());
            
            assertThat(ticketCaptor.getValue().getAssignedTechnician()).isNull();

            // Verify history entry
            ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
            verify(ticketHistoryRepository).save(historyCaptor.capture());
            
            TicketHistory history = historyCaptor.getValue();
            assertThat(history.getDescription()).contains("Unassigned from technician: " + activeTechnician.getFullName());
            assertThat(history.getDescription()).contains(reason);
            assertThat(history.getCreatedBy()).isEqualTo(updatedBy);
        }

        @Test
        @DisplayName("Should throw exception when ticket is not assigned")
        void shouldThrowExceptionWhenTicketNotAssigned() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket)); // openTicket has no assigned technician

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.unassignTechnician(1L, "reason", "user")
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("Ticket is not currently assigned to any technician");
        }
    }

    @Nested
    @DisplayName("Search and Retrieval")
    class SearchAndRetrievalTests {

        @Test
        @DisplayName("Should find ticket by ID successfully")
        void shouldFindTicketById() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));

            // Act
            Optional<Ticket> result = ticketService.findById(1L);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(openTicket);
            verify(ticketRepository).findById(1L);
        }

        @Test
        @DisplayName("Should get ticket by ID successfully")
        void shouldGetTicketById() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));

            // Act
            Ticket result = ticketService.getTicketById(1L);

            // Assert
            assertThat(result).isEqualTo(openTicket);
            verify(ticketRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when getting non-existent ticket")
        void shouldThrowExceptionWhenGettingNonExistentTicket() {
            // Arrange
            when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> 
                ticketService.getTicketById(999L)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Ticket not found with ID: 999");
        }

        @Test
        @DisplayName("Should find all tickets with pagination")
        void shouldFindAllTickets() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Ticket> ticketPage = new PageImpl<>(Arrays.asList(openTicket, closedTicket));
            when(ticketRepository.findAll(pageable)).thenReturn(ticketPage);

            // Act
            Page<Ticket> result = ticketService.findAllTickets(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(ticketRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should find tickets by client")
        void shouldFindTicketsByClient() {
            // Arrange
            Long clientId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Ticket> ticketPage = new PageImpl<>(Arrays.asList(openTicket));
            
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(activeClient));
            when(ticketRepository.findByClient(activeClient, pageable)).thenReturn(ticketPage);

            // Act
            Page<Ticket> result = ticketService.findTicketsByClient(clientId, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(clientRepository).findById(clientId);
            verify(ticketRepository).findByClient(activeClient, pageable);
        }
    }

    @Nested
    @DisplayName("Assignment Optimization")
    class AssignmentOptimizationTests {

        @Test
        @DisplayName("Should find best technician for ticket with single technician")
        void shouldFindBestTechnicianForTicketWithSingleTechnician() {
            // Arrange
            Long ticketId = 1L;
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(openTicket));
            when(technicianRepository.findByStatus(TechnicianStatus.ACTIVE))
                .thenReturn(Arrays.asList(activeTechnician));

            // Act
            Optional<Technician> result = ticketService.findBestTechnicianForTicket(ticketId);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(activeTechnician);
            verify(ticketRepository).findById(ticketId);
            verify(technicianRepository).findByStatus(TechnicianStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should find technician with lowest workload")
        void shouldFindTechnicianWithLowestWorkload() {
            // Arrange
            Technician tech1 = createActiveTechnician(1L, "Tech One");
            Technician tech2 = createActiveTechnician(2L, "Tech Two");
            
            when(technicianRepository.findByStatus(TechnicianStatus.ACTIVE))
                .thenReturn(Arrays.asList(tech1, tech2));
            when(ticketRepository.countByAssignedTechnicianAndStatus(tech1, TicketStatus.OPEN))
                .thenReturn(5L);
            when(ticketRepository.countByAssignedTechnicianAndStatus(tech2, TicketStatus.OPEN))
                .thenReturn(2L);

            // Act
            Optional<Technician> result = ticketService.findBestTechnicianForServiceType(ServiceType.HARDWARE);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(tech2); // tech2 has lower workload
        }

        @Test
        @DisplayName("Should return empty when no active technicians available")
        void shouldReturnEmptyWhenNoActiveTechnicians() {
            // Arrange
            when(technicianRepository.findByStatus(TechnicianStatus.ACTIVE))
                .thenReturn(Arrays.asList());

            // Act
            Optional<Technician> result = ticketService.findBestTechnicianForServiceType(ServiceType.HARDWARE);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("Should count tickets by status")
        void shouldCountTicketsByStatus() {
            // Arrange
            when(ticketRepository.countByStatus(TicketStatus.OPEN)).thenReturn(5L);

            // Act
            long result = ticketService.countTicketsByStatus(TicketStatus.OPEN);

            // Assert
            assertThat(result).isEqualTo(5L);
            verify(ticketRepository).countByStatus(TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should count overdue tickets")
        void shouldCountOverdueTickets() {
            // Arrange
            when(ticketRepository.countOverdueTickets(any(Instant.class))).thenReturn(3L);

            // Act
            long result = ticketService.countOverdueTickets();

            // Assert
            assertThat(result).isEqualTo(3L);
            verify(ticketRepository).countOverdueTickets(any(Instant.class));
        }

        @Test
        @DisplayName("Should count unassigned tickets")
        void shouldCountUnassignedTickets() {
            // Arrange
            when(ticketRepository.countByAssignedTechnicianIsNull()).thenReturn(7L);

            // Act
            long result = ticketService.countUnassignedTickets();

            // Assert
            assertThat(result).isEqualTo(7L);
            verify(ticketRepository).countByAssignedTechnicianIsNull();
        }

        @Test
        @DisplayName("Should count tickets by service type")
        void shouldCountTicketsByServiceType() {
            // Arrange
            when(ticketRepository.countByServiceType(ServiceType.HARDWARE)).thenReturn(4L);

            // Act
            long result = ticketService.countTicketsByServiceType(ServiceType.HARDWARE);

            // Assert
            assertThat(result).isEqualTo(4L);
            verify(ticketRepository).countByServiceType(ServiceType.HARDWARE);
        }
    }

    // === HELPER METHODS FOR TEST DATA CREATION ===

    private Client createActiveClient(Long id, String email) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setEmail(email);
        client.setStatus(Client.ClientStatus.ACTIVE);
        return client;
    }

    private Client createInactiveClient(Long id, String email) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName("Jane");
        client.setLastName("Smith");
        client.setEmail(email);
        client.setStatus(Client.ClientStatus.INACTIVE);
        return client;
    }

    private Technician createActiveTechnician(Long id, String fullName) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setFullName(fullName);
        technician.setEmail(fullName.toLowerCase().replace(" ", ".") + "@company.com");
        technician.setStatus(TechnicianStatus.ACTIVE);
        return technician;
    }

    private Technician createInactiveTechnician(Long id, String fullName) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setFullName(fullName);
        technician.setEmail(fullName.toLowerCase().replace(" ", ".") + "@company.com");
        technician.setStatus(TechnicianStatus.INACTIVE);
        return technician;
    }

    private Ticket createOpenTicket(Long id, Client client) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setClient(client);
        ticket.setServiceType(ServiceType.HARDWARE);
        ticket.setDescription("Test ticket description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setDueAt(Instant.now().plus(24, ChronoUnit.HOURS));
        return ticket;
    }

    private Ticket createClosedTicket(Long id, Client client) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setClient(client);
        ticket.setServiceType(ServiceType.SOFTWARE);
        ticket.setDescription("Closed ticket description");
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setDueAt(Instant.now().plus(48, ChronoUnit.HOURS));
        return ticket;
    }
} 
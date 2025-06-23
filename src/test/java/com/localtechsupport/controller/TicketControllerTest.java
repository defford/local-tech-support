package com.localtechsupport.controller;

import com.localtechsupport.dto.request.*;
import com.localtechsupport.dto.response.*;
import com.localtechsupport.entity.*;
import com.localtechsupport.service.TicketService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for TicketController.
 * 
 * Tests all REST endpoints, error scenarios, and response mappings.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketController Tests")
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    // Test data
    private Client testClient;
    private Technician testTechnician;
    private Ticket testTicket;
    private CreateTicketRequest createRequest;
    private AssignTechnicianRequest assignRequest;
    private UpdateStatusRequest updateStatusRequest;
    private CloseTicketRequest closeRequest;
    private UnassignTechnicianRequest unassignRequest;

    @BeforeEach
    void setUp() {
        // Create test client
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setEmail("john.doe@example.com");

        // Create test technician
        testTechnician = new Technician();
        testTechnician.setId(1L);
        testTechnician.setFullName("Jane Smith");
        testTechnician.setEmail("jane.smith@techsupport.com");

        // Create test ticket
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setClient(testClient);
        testTicket.setAssignedTechnician(testTechnician);
        testTicket.setServiceType(ServiceType.HARDWARE);
        testTicket.setDescription("Computer won't start");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setDueAt(Instant.now().plus(24, ChronoUnit.HOURS));
        testTicket.setCreatedAt(Instant.now());

        // Create test requests
        createRequest = new CreateTicketRequest(1L, ServiceType.HARDWARE, "Computer won't start");
        assignRequest = new AssignTechnicianRequest(1L);
        updateStatusRequest = new UpdateStatusRequest(TicketStatus.CLOSED, "Fixed", "admin");
        closeRequest = new CloseTicketRequest("Issue resolved", "admin");
        unassignRequest = new UnassignTechnicianRequest("Reassigning", "admin");
    }

    @Nested
    @DisplayName("Core CRUD Operations")
    class CoreCrudOperationTests {

        @Test
        @DisplayName("Should create ticket successfully")
        void createTicket_Success() {
            // Given
            when(ticketService.createTicket(1L, ServiceType.HARDWARE, "Computer won't start"))
                .thenReturn(testTicket);

            // When
            ResponseEntity<TicketResponse> response = ticketController.createTicket(createRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getDescription()).isEqualTo("Computer won't start");
            assertThat(response.getBody().getServiceType()).isEqualTo(ServiceType.HARDWARE);
            
            verify(ticketService).createTicket(1L, ServiceType.HARDWARE, "Computer won't start");
        }

        @Test
        @DisplayName("Should get ticket by ID when found")
        void getTicket_Found() {
            // Given
            when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));

            // When
            ResponseEntity<TicketResponse> response = ticketController.getTicket(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getClient().getFullName()).isEqualTo("John Doe");
            assertThat(response.getBody().getAssignedTechnician().getFullName()).isEqualTo("Jane Smith");
            
            verify(ticketService).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when ticket not found")
        void getTicket_NotFound() {
            // Given
            when(ticketService.findById(1L)).thenReturn(Optional.empty());

            // When
            ResponseEntity<TicketResponse> response = ticketController.getTicket(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            
            verify(ticketService).findById(1L);
        }

        @Test
        @DisplayName("Should get all tickets with pagination")
        void getAllTickets_WithPagination() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(0, 20), 1);
            when(ticketService.findAllTickets(any(Pageable.class))).thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.getAllTickets(
                0, 20, "createdAt", "desc", null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(1L);
            
            verify(ticketService).findAllTickets(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle service exception during creation")
        void createTicket_ServiceThrowsException() {
            // Given
            when(ticketService.createTicket(anyLong(), any(ServiceType.class), anyString()))
                .thenThrow(new IllegalArgumentException("Client not found"));

            // When & Then
            assertThatThrownBy(() -> ticketController.createTicket(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Client not found");
            
            verify(ticketService).createTicket(1L, ServiceType.HARDWARE, "Computer won't start");
        }
    }

    @Nested
    @DisplayName("Assignment Operations")
    class AssignmentOperationTests {

        @Test
        @DisplayName("Should assign technician successfully")
        void assignTechnician_Success() {
            // Given
            when(ticketService.assignTechnician(1L, 1L)).thenReturn(testTicket);

            // When
            ResponseEntity<TicketResponse> response = ticketController.assignTechnician(1L, assignRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAssignedTechnician()).isNotNull();
            assertThat(response.getBody().getAssignedTechnician().getId()).isEqualTo(1L);
            
            verify(ticketService).assignTechnician(1L, 1L);
        }

        @Test
        @DisplayName("Should auto-assign technician successfully")
        void autoAssignTechnician_Success() {
            // Given
            when(ticketService.findBestTechnicianForTicket(1L)).thenReturn(Optional.of(testTechnician));
            when(ticketService.assignTechnician(1L, 1L)).thenReturn(testTicket);

            // When
            ResponseEntity<TicketResponse> response = ticketController.autoAssignTechnician(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAssignedTechnician()).isNotNull();
            
            verify(ticketService).findBestTechnicianForTicket(1L);
            verify(ticketService).assignTechnician(1L, 1L);
        }

        @Test
        @DisplayName("Should return bad request when no technician available for auto-assignment")
        void autoAssignTechnician_NoTechnicianAvailable() {
            // Given
            when(ticketService.findBestTechnicianForTicket(1L)).thenReturn(Optional.empty());

            // When
            ResponseEntity<TicketResponse> response = ticketController.autoAssignTechnician(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNull();
            
            verify(ticketService).findBestTechnicianForTicket(1L);
            verify(ticketService, never()).assignTechnician(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should unassign technician successfully")
        void unassignTechnician_Success() {
            // Given
            when(ticketService.unassignTechnician(1L, "Reassigning", "admin")).thenReturn(testTicket);

            // When
            ResponseEntity<TicketResponse> response = ticketController.unassignTechnician(1L, unassignRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            verify(ticketService).unassignTechnician(1L, "Reassigning", "admin");
        }
    }

    @Nested
    @DisplayName("Status Management")
    class StatusManagementTests {

        @Test
        @DisplayName("Should update ticket status successfully")
        void updateStatus_Success() {
            // Given
            when(ticketService.updateStatus(1L, TicketStatus.CLOSED, "Fixed", "admin"))
                .thenReturn(testTicket);

            // When
            ResponseEntity<TicketResponse> response = ticketController.updateStatus(1L, updateStatusRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            verify(ticketService).updateStatus(1L, TicketStatus.CLOSED, "Fixed", "admin");
        }

        @Test
        @DisplayName("Should close ticket successfully")
        void closeTicket_Success() {
            // Given
            when(ticketService.closeTicket(1L, "Issue resolved", "admin")).thenReturn(testTicket);

            // When
            ResponseEntity<TicketResponse> response = ticketController.closeTicket(1L, closeRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            verify(ticketService).closeTicket(1L, "Issue resolved", "admin");
        }
    }

    @Nested
    @DisplayName("Search and Filtering")
    class SearchAndFilteringTests {

        @Test
        @DisplayName("Should filter tickets by status")
        void getAllTickets_FilterByStatus() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(0, 20), 1);
            when(ticketService.findTicketsByStatus(eq(TicketStatus.OPEN), any(Pageable.class)))
                .thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.getAllTickets(
                0, 20, "createdAt", "desc", TicketStatus.OPEN, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(ticketService).findTicketsByStatus(eq(TicketStatus.OPEN), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter tickets by client")
        void getAllTickets_FilterByClient() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(0, 20), 1);
            when(ticketService.findTicketsByClient(eq(1L), any(Pageable.class))).thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.getAllTickets(
                0, 20, "createdAt", "desc", null, 1L, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(ticketService).findTicketsByClient(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search tickets by query")
        void searchTickets_Success() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(0, 20), 1);
            when(ticketService.searchTickets(eq("computer"), any(Pageable.class))).thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.searchTickets(
                "computer", 0, 20, "createdAt", "desc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(ticketService).searchTickets(eq("computer"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get unassigned tickets")
        void getUnassignedTickets_Success() {
            // Given
            testTicket.setAssignedTechnician(null);
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(0, 20), 1);
            when(ticketService.findUnassignedTickets(any(Pageable.class))).thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.getUnassignedTickets(
                0, 20, "createdAt", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getAssignedTechnician()).isNull();
            
            verify(ticketService).findUnassignedTickets(any(Pageable.class));
        }

        @Test
        @DisplayName("Should get overdue tickets")
        void getOverdueTickets_Success() {
            // Given
            testTicket.setDueAt(Instant.now().minus(1, ChronoUnit.HOURS));
            List<Ticket> tickets = Arrays.asList(testTicket);
            when(ticketService.findOverdueTickets()).thenReturn(tickets);

            // When
            ResponseEntity<List<TicketResponse>> response = ticketController.getOverdueTickets();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            
            verify(ticketService).findOverdueTickets();
        }

        @Test
        @DisplayName("Should get tickets due soon")
        void getTicketsDueSoon_Success() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            when(ticketService.findTicketsDueSoon(24)).thenReturn(tickets);

            // When
            ResponseEntity<List<TicketResponse>> response = ticketController.getTicketsDueSoon(24);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            
            verify(ticketService).findTicketsDueSoon(24);
        }
    }

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("Should get statistics successfully")
        void getStatistics_Success() {
            // Given
            when(ticketService.countTicketsByStatus(TicketStatus.OPEN)).thenReturn(5L);
            when(ticketService.countTicketsByStatus(TicketStatus.CLOSED)).thenReturn(3L);
            when(ticketService.countOverdueTickets()).thenReturn(2L);
            when(ticketService.countUnassignedTickets()).thenReturn(1L);
            when(ticketService.countTicketsByServiceType(ServiceType.HARDWARE)).thenReturn(4L);
            when(ticketService.countTicketsByServiceType(ServiceType.SOFTWARE)).thenReturn(4L);

            // When
            ResponseEntity<TicketStatisticsResponse> response = ticketController.getStatistics();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalTickets()).isEqualTo(8L);
            assertThat(response.getBody().getOpenTickets()).isEqualTo(5L);
            assertThat(response.getBody().getClosedTickets()).isEqualTo(3L);
            assertThat(response.getBody().getOverdueTickets()).isEqualTo(2L);
            assertThat(response.getBody().getUnassignedTickets()).isEqualTo(1L);
            assertThat(response.getBody().getTicketsByServiceType()).containsEntry("HARDWARE", 4L);
            assertThat(response.getBody().getTicketsByServiceType()).containsEntry("SOFTWARE", 4L);
            
            verify(ticketService, times(4)).countTicketsByStatus(any(TicketStatus.class));
            verify(ticketService).countOverdueTickets();
            verify(ticketService).countUnassignedTickets();
            verify(ticketService, times(2)).countTicketsByServiceType(any(ServiceType.class));
        }

        @Test
        @DisplayName("Should handle empty statistics")
        void getStatistics_EmptyData() {
            // Given
            when(ticketService.countTicketsByStatus(any(TicketStatus.class))).thenReturn(0L);
            when(ticketService.countOverdueTickets()).thenReturn(0L);
            when(ticketService.countUnassignedTickets()).thenReturn(0L);
            when(ticketService.countTicketsByServiceType(any(ServiceType.class))).thenReturn(0L);

            // When
            ResponseEntity<TicketStatisticsResponse> response = ticketController.getStatistics();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalTickets()).isEqualTo(0L);
            assertThat(response.getBody().getClosureRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Response Mapping")
    class ResponseMappingTests {

        @Test
        @DisplayName("Should map ticket with assigned technician correctly")
        void mapToTicketResponse_WithAssignedTechnician() {
            // Given
            when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));

            // When
            ResponseEntity<TicketResponse> response = ticketController.getTicket(1L);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getClient()).isNotNull();
            assertThat(response.getBody().getClient().getId()).isEqualTo(1L);
            assertThat(response.getBody().getClient().getFullName()).isEqualTo("John Doe");
            assertThat(response.getBody().getAssignedTechnician()).isNotNull();
            assertThat(response.getBody().getAssignedTechnician().getId()).isEqualTo(1L);
            assertThat(response.getBody().getAssignedTechnician().getFullName()).isEqualTo("Jane Smith");
            assertThat(response.getBody().getServiceType()).isEqualTo(ServiceType.HARDWARE);
            assertThat(response.getBody().getStatus()).isEqualTo(TicketStatus.OPEN);
        }

        @Test
        @DisplayName("Should map ticket without assigned technician correctly")
        void mapToTicketResponse_WithoutAssignedTechnician() {
            // Given
            testTicket.setAssignedTechnician(null);
            when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));

            // When
            ResponseEntity<TicketResponse> response = ticketController.getTicket(1L);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAssignedTechnician()).isNull();
            assertThat(response.getBody().isAssigned()).isFalse();
        }

        @Test
        @DisplayName("Should handle null client gracefully")
        void mapToTicketResponse_NullClient() {
            // Given
            testTicket.setClient(null);
            when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));

            // When
            ResponseEntity<TicketResponse> response = ticketController.getTicket(1L);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getClient()).isNull();
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting")
    class PaginationAndSortingTests {

        @Test
        @DisplayName("Should handle custom sorting")
        void getAllTickets_CustomSorting() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(0, 10), 1);
            when(ticketService.findAllTickets(any(Pageable.class))).thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.getAllTickets(
                0, 10, "dueAt", "asc", null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            // Verify the pageable was created with correct sorting
            verify(ticketService).findAllTickets(argThat(pageable -> 
                pageable.getSort().getOrderFor("dueAt") != null &&
                pageable.getSort().getOrderFor("dueAt").getDirection() == Sort.Direction.ASC
            ));
        }

        @Test
        @DisplayName("Should handle custom pagination parameters")
        void getAllTickets_CustomPagination() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(2, 5), 15);
            when(ticketService.findAllTickets(any(Pageable.class))).thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.getAllTickets(
                2, 5, "createdAt", "desc", null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            // Verify the pageable was created with correct page and size
            verify(ticketService).findAllTickets(argThat(pageable -> 
                pageable.getPageNumber() == 2 && pageable.getPageSize() == 5
            ));
        }

        @Test
        @DisplayName("Should handle search with sorting and pagination")
        void searchTickets_WithSortingAndPagination() {
            // Given
            List<Ticket> tickets = Arrays.asList(testTicket);
            Page<Ticket> ticketPage = new PageImpl<>(tickets, PageRequest.of(1, 10), 20);
            when(ticketService.searchTickets(eq("test"), any(Pageable.class))).thenReturn(ticketPage);

            // When
            ResponseEntity<Page<TicketResponse>> response = ticketController.searchTickets(
                "test", 1, 10, "description", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            verify(ticketService).searchTickets(eq("test"), argThat(pageable -> 
                pageable.getPageNumber() == 1 && 
                pageable.getPageSize() == 10 &&
                pageable.getSort().getOrderFor("description") != null &&
                pageable.getSort().getOrderFor("description").getDirection() == Sort.Direction.ASC
            ));
        }
    }
} 
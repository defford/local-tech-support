package com.localtechsupport.controller;

import com.localtechsupport.dto.request.*;
import com.localtechsupport.dto.response.*;
import com.localtechsupport.entity.Client;
import com.localtechsupport.entity.Client.ClientStatus;
import com.localtechsupport.service.ClientService;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ClientController.
 * 
 * Tests all REST endpoints, error scenarios, and response mappings.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientController Tests")
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    // Test data
    private Client activeClient;
    private Client inactiveClient;
    private Client suspendedClient;
    private CreateClientRequest createRequest;
    private UpdateClientRequest updateRequest;
    private UpdateClientStatusRequest statusRequest;
    private ClientStatisticsResponse statisticsResponse;

    @BeforeEach
    void setUp() {
        // Create test clients
        activeClient = createTestClient(1L, "John", "Doe", "john.doe@example.com", 
            "+1234567890", "123 Main St", "Active client", ClientStatus.ACTIVE);
        
        inactiveClient = createTestClient(2L, "Jane", "Smith", "jane.smith@example.com", 
            "+0987654321", "456 Oak Ave", "Inactive client", ClientStatus.INACTIVE);
        
        suspendedClient = createTestClient(3L, "Bob", "Johnson", "bob.johnson@example.com", 
            null, null, "Suspended client", ClientStatus.SUSPENDED);

        // Create test requests
        createRequest = new CreateClientRequest("Alice", "Williams", "alice.williams@example.com", 
            "+1122334455", "789 Pine Rd", "New client");
        
        updateRequest = new UpdateClientRequest("Updated", "Name", "updated@example.com", 
            "+9999999999", "Updated Address", "Updated notes");
        
        statusRequest = new UpdateClientStatusRequest(ClientStatus.INACTIVE, 
            "Client requested deactivation", "admin");

        // Create test statistics response
        statisticsResponse = new ClientStatisticsResponse(10L, 7L, 2L, 1L, 3L, 5L, 
            Map.of("ACTIVE", 7L, "INACTIVE", 2L, "SUSPENDED", 1L), 
            LocalDateTime.now().minusDays(30), LocalDateTime.now());
    }

    private Client createTestClient(Long id, String firstName, String lastName, String email,
                                   String phone, String address, String notes, ClientStatus status) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setNotes(notes);
        client.setStatus(status);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return client;
    }

    @Nested
    @DisplayName("Core CRUD Operations")
    class CoreCrudOperationTests {

        @Test
        @DisplayName("Should create client successfully")
        void createClient_Success() {
            // Given
            when(clientService.createClient("Alice", "Williams", "alice.williams@example.com", 
                "+1122334455", "789 Pine Rd", "New client")).thenReturn(activeClient);

            // When
            ResponseEntity<ClientResponse> response = clientController.createClient(createRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getFirstName()).isEqualTo("John");
            assertThat(response.getBody().getLastName()).isEqualTo("Doe");
            assertThat(response.getBody().getEmail()).isEqualTo("john.doe@example.com");
            assertThat(response.getBody().getStatus()).isEqualTo(ClientStatus.ACTIVE);
            
            verify(clientService).createClient("Alice", "Williams", "alice.williams@example.com", 
                "+1122334455", "789 Pine Rd", "New client");
        }

        @Test
        @DisplayName("Should get client by ID when found")
        void getClient_Found() {
            // Given
            when(clientService.findById(1L)).thenReturn(Optional.of(activeClient));

            // When
            ResponseEntity<ClientResponse> response = clientController.getClient(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getFullName()).isEqualTo("John Doe");
            assertThat(response.getBody().getEmail()).isEqualTo("john.doe@example.com");
            assertThat(response.getBody().isActive()).isTrue();
            
            verify(clientService).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when client not found")
        void getClient_NotFound() {
            // Given
            when(clientService.findById(999L)).thenReturn(Optional.empty());

            // When
            ResponseEntity<ClientResponse> response = clientController.getClient(999L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            
            verify(clientService).findById(999L);
        }

        @Test
        @DisplayName("Should update client successfully")
        void updateClient_Success() {
            // Given
            when(clientService.updateClient(1L, "Updated", "Name", "updated@example.com", 
                "+9999999999", "Updated Address", "Updated notes")).thenReturn(activeClient);

            // When
            ResponseEntity<ClientResponse> response = clientController.updateClient(1L, updateRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            
            verify(clientService).updateClient(1L, "Updated", "Name", "updated@example.com", 
                "+9999999999", "Updated Address", "Updated notes");
        }

        @Test
        @DisplayName("Should delete client successfully")
        void deleteClient_Success() {
            // Given
            doNothing().when(clientService).deleteClient(2L);

            // When
            ResponseEntity<Void> response = clientController.deleteClient(2L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
            
            verify(clientService).deleteClient(2L);
        }

        @Test
        @DisplayName("Should get all clients with pagination")
        void getAllClients_WithPagination() {
            // Given
            List<Client> clients = Arrays.asList(activeClient, inactiveClient);
            Page<Client> clientPage = new PageImpl<>(clients, PageRequest.of(0, 20), 2);
            when(clientService.findAllClients(any(Pageable.class))).thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getAllClients(
                0, 20, "createdAt", "desc", null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(2);
            assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(1L);
            assertThat(response.getBody().getContent().get(1).getId()).isEqualTo(2L);
            
            verify(clientService).findAllClients(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle service exception during creation")
        void createClient_ServiceThrowsException() {
            // Given
            when(clientService.createClient(anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Email already exists"));

            // When & Then
            assertThatThrownBy(() -> clientController.createClient(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
            
            verify(clientService).createClient("Alice", "Williams", "alice.williams@example.com", 
                "+1122334455", "789 Pine Rd", "New client");
        }
    }

    @Nested
    @DisplayName("Status Management")
    class StatusManagementTests {

        @Test
        @DisplayName("Should update client status successfully")
        void updateStatus_Success() {
            // Given
            when(clientService.updateClientStatus(1L, ClientStatus.INACTIVE, 
                "Client requested deactivation")).thenReturn(inactiveClient);

            // When
            ResponseEntity<ClientResponse> response = clientController.updateStatus(1L, statusRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(2L);
            assertThat(response.getBody().getStatus()).isEqualTo(ClientStatus.INACTIVE);
            
            verify(clientService).updateClientStatus(1L, ClientStatus.INACTIVE, 
                "Client requested deactivation");
        }

        @Test
        @DisplayName("Should activate client successfully")
        void activateClient_Success() {
            // Given
            when(clientService.activateClient(2L, "Reactivation requested")).thenReturn(activeClient);

            // When
            ResponseEntity<ClientResponse> response = clientController.activateClient(2L, 
                "Reactivation requested");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isActive()).isTrue();
            
            verify(clientService).activateClient(2L, "Reactivation requested");
        }

        @Test
        @DisplayName("Should deactivate client successfully")
        void deactivateClient_Success() {
            // Given
            when(clientService.deactivateClient(1L, "Client requested")).thenReturn(inactiveClient);

            // When
            ResponseEntity<ClientResponse> response = clientController.deactivateClient(1L, 
                "Client requested");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(2L);
            
            verify(clientService).deactivateClient(1L, "Client requested");
        }

        @Test
        @DisplayName("Should suspend client successfully")
        void suspendClient_Success() {
            // Given
            when(clientService.suspendClient(1L, "Non-payment")).thenReturn(suspendedClient);

            // When
            ResponseEntity<ClientResponse> response = clientController.suspendClient(1L, "Non-payment");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(ClientStatus.SUSPENDED);
            
            verify(clientService).suspendClient(1L, "Non-payment");
        }

        @Test
        @DisplayName("Should handle null reason for status operations")
        void statusOperations_NullReason() {
            // Given
            when(clientService.activateClient(1L, null)).thenReturn(activeClient);

            // When
            ResponseEntity<ClientResponse> response = clientController.activateClient(1L, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(clientService).activateClient(1L, null);
        }
    }

    @Nested
    @DisplayName("Search and Filtering")
    class SearchAndFilteringTests {

        @Test
        @DisplayName("Should filter clients by status")
        void getAllClients_FilterByStatus() {
            // Given
            List<Client> activeClients = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(activeClients, PageRequest.of(0, 20), 1);
            when(clientService.findClientsByStatus(eq(ClientStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getAllClients(
                0, 20, "createdAt", "desc", ClientStatus.ACTIVE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(ClientStatus.ACTIVE);
            
            verify(clientService).findClientsByStatus(eq(ClientStatus.ACTIVE), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search clients by query")
        void searchClients_Success() {
            // Given
            List<Client> searchResults = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(searchResults, PageRequest.of(0, 20), 1);
            when(clientService.searchClients(eq("John"), any(Pageable.class))).thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.searchClients(
                "John", 0, 20, "createdAt", "desc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getFirstName()).isEqualTo("John");
            
            verify(clientService).searchClients(eq("John"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get clients by specific status")
        void getClientsByStatus_Success() {
            // Given
            List<Client> activeClients = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(activeClients, PageRequest.of(0, 20), 1);
            when(clientService.findClientsByStatus(eq(ClientStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getClientsByStatus(
                ClientStatus.ACTIVE, 0, 20, "createdAt", "desc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(clientService).findClientsByStatus(eq(ClientStatus.ACTIVE), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get active clients")
        void getActiveClients_Success() {
            // Given
            List<Client> activeClients = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(activeClients, PageRequest.of(0, 20), 1);
            when(clientService.findActiveClients(any(Pageable.class))).thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getActiveClients(
                0, 20, "createdAt", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).isActive()).isTrue();
            
            verify(clientService).findActiveClients(any(Pageable.class));
        }

        @Test
        @DisplayName("Should get inactive clients")
        void getInactiveClients_Success() {
            // Given
            List<Client> inactiveClients = Arrays.asList(inactiveClient);
            Page<Client> clientPage = new PageImpl<>(inactiveClients, PageRequest.of(0, 20), 1);
            when(clientService.findInactiveClients(any(Pageable.class))).thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getInactiveClients(
                0, 20, "createdAt", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(ClientStatus.INACTIVE);
            
            verify(clientService).findInactiveClients(any(Pageable.class));
        }

        @Test
        @DisplayName("Should get recent clients")
        void getRecentClients_Success() {
            // Given
            List<Client> recentClients = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(recentClients, PageRequest.of(0, 20), 1);
            when(clientService.findRecentClients(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getRecentClients(
                7, 0, 20, "createdAt", "desc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(clientService).findRecentClients(any(LocalDateTime.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Statistics and Analytics")
    class StatisticsTests {

        @Test
        @DisplayName("Should get comprehensive statistics successfully")
        void getStatistics_Success() {
            // Given
            when(clientService.countAllClients()).thenReturn(10L);
            when(clientService.countActiveClients()).thenReturn(7L);
            when(clientService.countInactiveClients()).thenReturn(2L);
            when(clientService.countSuspendedClients()).thenReturn(1L);
            when(clientService.countRecentClients(any(LocalDateTime.class))).thenReturn(3L, 5L, 8L);

            // When
            ResponseEntity<ClientStatisticsResponse> response = clientController.getStatistics();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalClients()).isEqualTo(10L);
            assertThat(response.getBody().getActiveClients()).isEqualTo(7L);
            assertThat(response.getBody().getInactiveClients()).isEqualTo(2L);
            assertThat(response.getBody().getActivePercentage()).isEqualTo(70.0);
            assertThat(response.getBody().getClientsByStatus()).containsKeys("ACTIVE", "INACTIVE", "SUSPENDED");
            
            verify(clientService).countAllClients();
            verify(clientService).countActiveClients();
            verify(clientService).countInactiveClients();
            verify(clientService).countSuspendedClients();
            verify(clientService, times(3)).countRecentClients(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should get total client count")
        void getClientCount_Success() {
            // Given
            when(clientService.countAllClients()).thenReturn(15L);

            // When
            ResponseEntity<Map<String, Long>> response = clientController.getClientCount();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("total", 15L);
            
            verify(clientService).countAllClients();
        }

        @Test
        @DisplayName("Should get client count by specific status")
        void getClientCountByStatus_Success() {
            // Given
            when(clientService.countClientsByStatus(ClientStatus.ACTIVE)).thenReturn(8L);

            // When
            ResponseEntity<Map<String, Long>> response = clientController.getClientCountByStatus(
                ClientStatus.ACTIVE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("active", 8L);
            
            verify(clientService).countClientsByStatus(ClientStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should get client counts by all statuses")
        void getClientCountsByStatus_Success() {
            // Given
            Map<ClientStatus, Long> statusCounts = Map.of(
                ClientStatus.ACTIVE, 7L,
                ClientStatus.INACTIVE, 2L,
                ClientStatus.SUSPENDED, 1L
            );
            when(clientService.getClientCountsByStatus()).thenReturn(statusCounts);

            // When
            ResponseEntity<Map<ClientStatus, Long>> response = clientController.getClientCountsByStatus();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3);
            assertThat(response.getBody()).containsEntry(ClientStatus.ACTIVE, 7L);
            assertThat(response.getBody()).containsEntry(ClientStatus.INACTIVE, 2L);
            assertThat(response.getBody()).containsEntry(ClientStatus.SUSPENDED, 1L);
            
            verify(clientService).getClientCountsByStatus();
        }

        @Test
        @DisplayName("Should handle empty statistics")
        void getStatistics_EmptyData() {
            // Given
            when(clientService.countAllClients()).thenReturn(0L);
            when(clientService.countActiveClients()).thenReturn(0L);
            when(clientService.countInactiveClients()).thenReturn(0L);
            when(clientService.countSuspendedClients()).thenReturn(0L);
            when(clientService.countRecentClients(any(LocalDateTime.class))).thenReturn(0L);

            // When
            ResponseEntity<ClientStatisticsResponse> response = clientController.getStatistics();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalClients()).isEqualTo(0L);
            assertThat(response.getBody().getActivePercentage()).isEqualTo(0.0);
            assertThat(response.getBody().getInactivePercentage()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Email Availability")
    class EmailAvailabilityTests {

        @Test
        @DisplayName("Should check email availability successfully")
        void checkEmailAvailability_Available() {
            // Given
            when(clientService.isEmailAvailable("available@example.com")).thenReturn(true);

            // When
            ResponseEntity<Map<String, Boolean>> response = clientController.checkEmailAvailability(
                "available@example.com");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("available", true);
            
            verify(clientService).isEmailAvailable("available@example.com");
        }

        @Test
        @DisplayName("Should return false when email is taken")
        void checkEmailAvailability_Taken() {
            // Given
            when(clientService.isEmailAvailable("taken@example.com")).thenReturn(false);

            // When
            ResponseEntity<Map<String, Boolean>> response = clientController.checkEmailAvailability(
                "taken@example.com");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsEntry("available", false);
            
            verify(clientService).isEmailAvailable("taken@example.com");
        }
    }

    @Nested
    @DisplayName("Response Mapping")
    class ResponseMappingTests {

        @Test
        @DisplayName("Should map client with all fields correctly")
        void mapToClientResponse_WithAllFields() {
            // Given
            when(clientService.findById(1L)).thenReturn(Optional.of(activeClient));

            // When
            ResponseEntity<ClientResponse> response = clientController.getClient(1L);

            // Then
            assertThat(response.getBody()).isNotNull();
            ClientResponse clientResponse = response.getBody();
            assertThat(clientResponse.getId()).isEqualTo(1L);
            assertThat(clientResponse.getFirstName()).isEqualTo("John");
            assertThat(clientResponse.getLastName()).isEqualTo("Doe");
            assertThat(clientResponse.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(clientResponse.getPhone()).isEqualTo("+1234567890");
            assertThat(clientResponse.getAddress()).isEqualTo("123 Main St");
            assertThat(clientResponse.getNotes()).isEqualTo("Active client");
            assertThat(clientResponse.getStatus()).isEqualTo(ClientStatus.ACTIVE);
            assertThat(clientResponse.getFullName()).isEqualTo("John Doe");
            assertThat(clientResponse.isActive()).isTrue();
            assertThat(clientResponse.hasPhone()).isTrue();
            assertThat(clientResponse.hasAddress()).isTrue();
            assertThat(clientResponse.hasNotes()).isTrue();
        }

        @Test
        @DisplayName("Should map client with minimal fields correctly")
        void mapToClientResponse_WithMinimalFields() {
            // Given
            Client minimalClient = createTestClient(4L, "Min", "Client", "min@example.com", 
                null, null, null, ClientStatus.ACTIVE);
            when(clientService.findById(4L)).thenReturn(Optional.of(minimalClient));

            // When
            ResponseEntity<ClientResponse> response = clientController.getClient(4L);

            // Then
            assertThat(response.getBody()).isNotNull();
            ClientResponse clientResponse = response.getBody();
            assertThat(clientResponse.getId()).isEqualTo(4L);
            assertThat(clientResponse.getFirstName()).isEqualTo("Min");
            assertThat(clientResponse.getLastName()).isEqualTo("Client");
            assertThat(clientResponse.getEmail()).isEqualTo("min@example.com");
            assertThat(clientResponse.getPhone()).isNull();
            assertThat(clientResponse.getAddress()).isNull();
            assertThat(clientResponse.getNotes()).isNull();
            assertThat(clientResponse.hasPhone()).isFalse();
            assertThat(clientResponse.hasAddress()).isFalse();
            assertThat(clientResponse.hasNotes()).isFalse();
        }

        @Test
        @DisplayName("Should handle suspended client status correctly")
        void mapToClientResponse_SuspendedStatus() {
            // Given
            when(clientService.findById(3L)).thenReturn(Optional.of(suspendedClient));

            // When
            ResponseEntity<ClientResponse> response = clientController.getClient(3L);

            // Then
            assertThat(response.getBody()).isNotNull();
            ClientResponse clientResponse = response.getBody();
            assertThat(clientResponse.getStatus()).isEqualTo(ClientStatus.SUSPENDED);
            assertThat(clientResponse.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting")
    class PaginationAndSortingTests {

        @Test
        @DisplayName("Should handle custom sorting")
        void getAllClients_CustomSorting() {
            // Given
            List<Client> clients = Arrays.asList(activeClient, inactiveClient);
            Page<Client> clientPage = new PageImpl<>(clients, PageRequest.of(0, 20), 2);
            when(clientService.findAllClients(any(Pageable.class))).thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getAllClients(
                0, 20, "lastName", "asc", null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(2);
            
            verify(clientService).findAllClients(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination parameters")
        void getAllClients_CustomPagination() {
            // Given
            List<Client> clients = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(clients, PageRequest.of(1, 5), 10);
            when(clientService.findAllClients(any(Pageable.class))).thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.getAllClients(
                1, 5, "createdAt", "desc", null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNumber()).isEqualTo(1);
            assertThat(response.getBody().getSize()).isEqualTo(5);
            
            verify(clientService).findAllClients(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle search with sorting and pagination")
        void searchClients_WithSortingAndPagination() {
            // Given
            List<Client> searchResults = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(searchResults, PageRequest.of(0, 10), 1);
            when(clientService.searchClients(eq("John"), any(Pageable.class))).thenReturn(clientPage);

            // When
            ResponseEntity<Page<ClientResponse>> response = clientController.searchClients(
                "John", 0, 10, "firstName", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getSize()).isEqualTo(10);
            
            verify(clientService).searchClients(eq("John"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle service exceptions during update")
        void updateClient_ServiceThrowsException() {
            // Given
            when(clientService.updateClient(anyLong(), anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Client not found"));

            // When & Then
            assertThatThrownBy(() -> clientController.updateClient(999L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Client not found");
        }

        @Test
        @DisplayName("Should handle service exceptions during status update")
        void updateStatus_ServiceThrowsException() {
            // Given
            when(clientService.updateClientStatus(anyLong(), any(ClientStatus.class), anyString()))
                .thenThrow(new IllegalStateException("Invalid status transition"));

            // When & Then
            assertThatThrownBy(() -> clientController.updateStatus(1L, statusRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid status transition");
        }

        @Test
        @DisplayName("Should handle service exceptions during deletion")
        void deleteClient_ServiceThrowsException() {
            // Given
            doThrow(new IllegalStateException("Cannot delete active client"))
                .when(clientService).deleteClient(1L);

            // When & Then
            assertThatThrownBy(() -> clientController.deleteClient(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete active client");
        }
    }
} 
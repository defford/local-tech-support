package com.localtechsupport.service;

import com.localtechsupport.entity.Client;
import com.localtechsupport.entity.Client.ClientStatus;
import com.localtechsupport.entity.Ticket;
import com.localtechsupport.entity.TicketStatus;
import com.localtechsupport.repository.ClientRepository;
import com.localtechsupport.repository.TicketRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Tests")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ClientService clientService;

    // Test data - timestamps
    private LocalDateTime now;
    private LocalDateTime yesterday;
    private LocalDateTime lastWeek;
    private LocalDateTime lastMonth;

    // Test data - entities
    private Client activeClient;
    private Client inactiveClient;
    private Client suspendedClient;
    private Client newClient;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Initialize timestamps
        now = LocalDateTime.now();
        yesterday = now.minusDays(1);
        lastWeek = now.minusDays(7);
        lastMonth = now.minusDays(30);

        // Setup test entities
        setupTestEntities();
    }

    private void setupTestEntities() {
        // Create test clients
        activeClient = createTestClient(1L, "John", "Doe", "john.doe@example.com", 
            "+1234567890", "123 Main St", "Active client notes", ClientStatus.ACTIVE, yesterday);
        
        inactiveClient = createTestClient(2L, "Jane", "Smith", "jane.smith@example.com", 
            "+0987654321", "456 Oak Ave", "Inactive client notes", ClientStatus.INACTIVE, lastWeek);
        
        suspendedClient = createTestClient(3L, "Bob", "Johnson", "bob.johnson@example.com", 
            null, null, "Suspended for non-payment", ClientStatus.SUSPENDED, lastMonth);
        
        newClient = createTestClient(null, "Alice", "Williams", "alice.williams@example.com", 
            "+1122334455", "789 Pine Rd", "New client", ClientStatus.ACTIVE, now);

        testTicket = new Ticket();
        testTicket.setClient(activeClient);
    }

    // === TEST HELPER METHODS ===

    private Client createTestClient(Long id, String firstName, String lastName, String email,
                                   String phone, String address, String notes, ClientStatus status,
                                   LocalDateTime createdAt) {
        Client client = new Client();
        client.setId(id);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setNotes(notes);
        client.setStatus(status);
        client.setCreatedAt(createdAt);
        client.setUpdatedAt(createdAt);
        return client;
    }

    // === CORE CLIENT CREATION TESTS ===

    @Nested
    @DisplayName("Client Creation Tests")
    class ClientCreationTests {

        @Test
        @DisplayName("Should create client successfully with valid inputs")
        void shouldCreateClientSuccessfullyWithValidInputs() {
            // Given
            when(clientRepository.existsByEmail("new.client@example.com")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(newClient);

            // When
            Client result = clientService.createClient(
                "Alice", "Williams", "new.client@example.com", 
                "+1122334455", "789 Pine Rd", "New client"
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Alice");
            assertThat(result.getLastName()).isEqualTo("Williams");
            assertThat(result.getEmail()).isEqualTo("alice.williams@example.com");
            assertThat(result.getStatus()).isEqualTo(ClientStatus.ACTIVE);

            // Verify interactions
            verify(clientRepository).existsByEmail("new.client@example.com");
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should verify client object passed to save method")
        void shouldVerifyClientObjectPassedToSaveMethod() {
            // Given
            when(clientRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            clientService.createClient("John", "Doe", "test@example.com", 
                "+1234567890", "123 Main St", "Test notes");

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(clientCaptor.capture());

            Client capturedClient = clientCaptor.getValue();
            assertThat(capturedClient.getFirstName()).isEqualTo("John");
            assertThat(capturedClient.getLastName()).isEqualTo("Doe");
            assertThat(capturedClient.getEmail()).isEqualTo("test@example.com");
            assertThat(capturedClient.getPhone()).isEqualTo("+1234567890");
            assertThat(capturedClient.getAddress()).isEqualTo("123 Main St");
            assertThat(capturedClient.getNotes()).isEqualTo("Test notes");
            assertThat(capturedClient.getStatus()).isEqualTo(ClientStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            when(clientRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> 
                clientService.createClient("John", "Doe", "existing@example.com", 
                    null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client with email existing@example.com already exists");

            // Verify no save occurred
            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when first name is null")
        void shouldThrowExceptionWhenFirstNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> 
                clientService.createClient(null, "Doe", "test@example.com", 
                    null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First name is required");
        }

        @Test
        @DisplayName("Should throw exception when first name is empty")
        void shouldThrowExceptionWhenFirstNameIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> 
                clientService.createClient("", "Doe", "test@example.com", 
                    null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First name is required");
        }

        @Test
        @DisplayName("Should throw exception when last name is null")
        void shouldThrowExceptionWhenLastNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> 
                clientService.createClient("John", null, "test@example.com", 
                    null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Last name is required");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            assertThatThrownBy(() -> 
                clientService.createClient("John", "Doe", null, 
                    null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");
        }

        @Test
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // When & Then
            assertThatThrownBy(() -> 
                clientService.createClient("John", "Doe", "invalid-email", 
                    null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email format is invalid");
        }

        @Test
        @DisplayName("Should normalize email to lowercase")
        void shouldNormalizeEmailToLowercase() {
            // Given
            // The service checks email existence with original case, then normalizes for storage
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            clientService.createClient("John", "Doe", "Test@Example.Com", 
                null, null, null);

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getEmail()).isEqualTo("test@example.com");
            verify(clientRepository).existsByEmail("Test@Example.Com"); // Verify original email was checked
        }

        @Test
        @DisplayName("Should trim whitespace from all fields")
        void shouldTrimWhitespaceFromAllFields() {
            // Given
            // The service checks email existence with original whitespace, then trims/normalizes for storage
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            clientService.createClient("  John  ", "  Doe  ", "  test@example.com  ", 
                "  +1234567890  ", "  123 Main St  ", "  Test notes  ");

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(clientCaptor.capture());
            
            Client capturedClient = clientCaptor.getValue();
            assertThat(capturedClient.getFirstName()).isEqualTo("John");
            assertThat(capturedClient.getLastName()).isEqualTo("Doe");
            assertThat(capturedClient.getEmail()).isEqualTo("test@example.com");
            assertThat(capturedClient.getPhone()).isEqualTo("+1234567890");
            assertThat(capturedClient.getAddress()).isEqualTo("123 Main St");
            assertThat(capturedClient.getNotes()).isEqualTo("Test notes");
            verify(clientRepository).existsByEmail("  test@example.com  "); // Verify original email was checked
        }
    }

    // === CLIENT UPDATE TESTS ===

    @Nested
    @DisplayName("Client Update Tests")
    class ClientUpdateTests {

        @Test
        @DisplayName("Should update client successfully with valid inputs")
        void shouldUpdateClientSuccessfullyWithValidInputs() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            Client result = clientService.updateClient(1L, "Updated", "Name", 
                null, "+9999999999", "Updated Address", "Updated notes");

            // Then
            assertThat(result).isNotNull();
            verify(clientRepository).findById(1L);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            clientService.updateClient(1L, "Updated", null, null, null, null, null);

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(clientCaptor.capture());
            
            Client capturedClient = clientCaptor.getValue();
            assertThat(capturedClient.getFirstName()).isEqualTo("Updated");
            assertThat(capturedClient.getLastName()).isEqualTo("Doe"); // Original value
        }

        @Test
        @DisplayName("Should check email uniqueness when email is changed")
        void shouldCheckEmailUniquenessWhenEmailIsChanged() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            clientService.updateClient(1L, null, null, "new@example.com", 
                null, null, null);

            // Then
            verify(clientRepository).existsByEmail("new@example.com");
        }

        @Test
        @DisplayName("Should throw exception when updating to existing email")
        void shouldThrowExceptionWhenUpdatingToExistingEmail() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> 
                clientService.updateClient(1L, null, null, "existing@example.com", 
                    null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client with email existing@example.com already exists");
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void shouldThrowExceptionWhenClientNotFound() {
            // Given
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                clientService.updateClient(999L, "John", "Doe", 
                    "test@example.com", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client not found with ID: 999");
        }

        @Test
        @DisplayName("Should handle empty strings as null values")
        void shouldHandleEmptyStringsAsNullValues() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            clientService.updateClient(1L, null, null, null, "", "", "");

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(clientCaptor.capture());
            
            Client capturedClient = clientCaptor.getValue();
            assertThat(capturedClient.getPhone()).isNull();
            assertThat(capturedClient.getAddress()).isNull();
            assertThat(capturedClient.getNotes()).isNull();
        }
    }

    // === CLIENT DELETION TESTS ===

    @Nested
    @DisplayName("Client Deletion Tests")
    class ClientDeletionTests {

        @Test
        @DisplayName("Should delete inactive client successfully")
        void shouldDeleteInactiveClientSuccessfully() {
            // Given
            when(clientRepository.findById(2L)).thenReturn(Optional.of(inactiveClient));
            when(ticketRepository.findByClient(inactiveClient)).thenReturn(new ArrayList<>());

            // When
            clientService.deleteClient(2L);

            // Then
            verify(clientRepository).findById(2L);
            verify(ticketRepository).findByClient(inactiveClient);
            verify(clientRepository).deleteById(2L);
        }

        @Test
        @DisplayName("Should delete inactive client and unassign tickets successfully")
        void shouldDeleteInactiveClientAndUnassignTicketsSuccessfully() {
            // Given
            List<Ticket> clientTickets = Arrays.asList(testTicket);
            when(clientRepository.findById(2L)).thenReturn(Optional.of(inactiveClient));
            when(ticketRepository.findByClient(inactiveClient)).thenReturn(clientTickets);

            // When
            clientService.deleteClient(2L);

            // Then
            verify(clientRepository).findById(2L);
            verify(ticketRepository).findByClient(inactiveClient);
            verify(ticketRepository).save(testTicket);
            assertThat(testTicket.getClient()).isNull();
            verify(clientRepository).deleteById(2L);
        }

        @Test
        @DisplayName("Should throw exception when trying to delete active client")
        void shouldThrowExceptionWhenTryingToDeleteActiveClient() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));

            // When & Then
            assertThatThrownBy(() -> clientService.deleteClient(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete active client. Please deactivate first.");

            verify(clientRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw exception when client not found for deletion")
        void shouldThrowExceptionWhenClientNotFoundForDeletion() {
            // Given
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> clientService.deleteClient(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client not found with ID: 999");

            verify(clientRepository, never()).deleteById(any());
        }


    }

    // === STATUS MANAGEMENT TESTS ===

    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {

        @Test
        @DisplayName("Should update client status successfully with valid transition")
        void shouldUpdateClientStatusSuccessfullyWithValidTransition() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            Client result = clientService.updateClientStatus(1L, ClientStatus.INACTIVE, "Requested by client");

            // Then
            assertThat(result).isNotNull();
            verify(clientRepository).findById(1L);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should activate client successfully")
        void shouldActivateClientSuccessfully() {
            // Given
            when(clientRepository.findById(2L)).thenReturn(Optional.of(inactiveClient));
            when(clientRepository.save(any(Client.class))).thenReturn(inactiveClient);

            // When
            Client result = clientService.activateClient(2L, "Reactivation requested");

            // Then
            assertThat(result).isNotNull();
            verify(clientRepository).findById(2L);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should deactivate client successfully")
        void shouldDeactivateClientSuccessfully() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            Client result = clientService.deactivateClient(1L, "Client requested deactivation");

            // Then
            assertThat(result).isNotNull();
            verify(clientRepository).findById(1L);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should suspend client successfully")
        void shouldSuspendClientSuccessfully() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When
            Client result = clientService.suspendClient(1L, "Non-payment");

            // Then
            assertThat(result).isNotNull();
            verify(clientRepository).findById(1L);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should allow all valid status transitions")
        void shouldAllowAllValidStatusTransitions() {
            // Given - all transitions are valid based on the service implementation
            when(clientRepository.findById(3L)).thenReturn(Optional.of(suspendedClient));
            when(clientRepository.save(any(Client.class))).thenReturn(suspendedClient);

            // When - suspended to active should be allowed
            Client result = clientService.updateClientStatus(3L, ClientStatus.ACTIVE, "Direct activation");

            // Then
            assertThat(result).isNotNull();
            verify(clientRepository).findById(3L);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should throw exception when client not found for status update")
        void shouldThrowExceptionWhenClientNotFoundForStatusUpdate() {
            // Given
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                clientService.updateClientStatus(999L, ClientStatus.INACTIVE, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client not found with ID: 999");
        }
    }

    // === SEARCH AND RETRIEVAL TESTS ===

    @Nested
    @DisplayName("Search and Retrieval Tests")
    class SearchAndRetrievalTests {

        @Test
        @DisplayName("Should find client by ID successfully")
        void shouldFindClientByIdSuccessfully() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));

            // When
            Optional<Client> result = clientService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(activeClient);
            verify(clientRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty optional when client not found by ID")
        void shouldReturnEmptyOptionalWhenClientNotFoundById() {
            // Given
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Client> result = clientService.findById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(clientRepository).findById(999L);
        }

        @Test
        @DisplayName("Should get client by ID successfully")
        void shouldGetClientByIdSuccessfully() {
            // Given
            when(clientRepository.findById(1L)).thenReturn(Optional.of(activeClient));

            // When
            Client result = clientService.getClientById(1L);

            // Then
            assertThat(result).isEqualTo(activeClient);
            verify(clientRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when getting client by non-existent ID")
        void shouldThrowExceptionWhenGettingClientByNonExistentId() {
            // Given
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> clientService.getClientById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client not found with ID: 999");
        }

        @Test
        @DisplayName("Should find client by email successfully")
        void shouldFindClientByEmailSuccessfully() {
            // Given
            when(clientRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(activeClient));

            // When
            Optional<Client> result = clientService.findByEmail("john.doe@example.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(activeClient);
            verify(clientRepository).findByEmail("john.doe@example.com");
        }

        @Test
        @DisplayName("Should return empty optional when email is null or empty")
        void shouldReturnEmptyOptionalWhenEmailIsNullOrEmpty() {
            // When & Then
            assertThat(clientService.findByEmail(null)).isEmpty();
            assertThat(clientService.findByEmail("")).isEmpty();
            assertThat(clientService.findByEmail("   ")).isEmpty();

            verify(clientRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("Should find all clients with pagination")
        void shouldFindAllClientsWithPagination() {
            // Given
            List<Client> clients = Arrays.asList(activeClient, inactiveClient);
            Page<Client> clientPage = new PageImpl<>(clients);
            Pageable pageable = mock(Pageable.class);
            when(clientRepository.findAll(pageable)).thenReturn(clientPage);

            // When
            Page<Client> result = clientService.findAllClients(pageable);

            // Then
            assertThat(result).isEqualTo(clientPage);
            assertThat(result.getContent()).hasSize(2);
            verify(clientRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should find clients by status with pagination")
        void shouldFindClientsByStatusWithPagination() {
            // Given
            List<Client> activeClients = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(activeClients);
            Pageable pageable = mock(Pageable.class);
            when(clientRepository.findByStatus(ClientStatus.ACTIVE, pageable)).thenReturn(clientPage);

            // When
            Page<Client> result = clientService.findClientsByStatus(ClientStatus.ACTIVE, pageable);

            // Then
            assertThat(result).isEqualTo(clientPage);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(ClientStatus.ACTIVE);
            verify(clientRepository).findByStatus(ClientStatus.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Should search clients with search term")
        void shouldSearchClientsWithSearchTerm() {
            // Given
            List<Client> searchResults = Arrays.asList(activeClient);
            Page<Client> clientPage = new PageImpl<>(searchResults);
            Pageable pageable = mock(Pageable.class);
            when(clientRepository.searchClients("John", pageable)).thenReturn(clientPage);

            // When
            Page<Client> result = clientService.searchClients("John", pageable);

            // Then
            assertThat(result).isEqualTo(clientPage);
            verify(clientRepository).searchClients("John", pageable);
        }

        @Test
        @DisplayName("Should return all clients when search term is empty")
        void shouldReturnAllClientsWhenSearchTermIsEmpty() {
            // Given
            List<Client> allClients = Arrays.asList(activeClient, inactiveClient);
            Page<Client> clientPage = new PageImpl<>(allClients);
            Pageable pageable = mock(Pageable.class);
            when(clientRepository.findAll(pageable)).thenReturn(clientPage);

            // When
            Page<Client> result = clientService.searchClients("", pageable);

            // Then
            assertThat(result).isEqualTo(clientPage);
            verify(clientRepository).findAll(pageable);
            verify(clientRepository, never()).searchClients(any(), any());
        }
    }

    // === STATISTICS AND ANALYTICS TESTS ===

    @Nested
    @DisplayName("Statistics and Analytics Tests")
    class StatisticsAndAnalyticsTests {

        @Test
        @DisplayName("Should count all clients")
        void shouldCountAllClients() {
            // Given
            when(clientRepository.count()).thenReturn(10L);

            // When
            long result = clientService.countAllClients();

            // Then
            assertThat(result).isEqualTo(10L);
            verify(clientRepository).count();
        }

        @Test
        @DisplayName("Should count clients by status")
        void shouldCountClientsByStatus() {
            // Given
            when(clientRepository.countByStatus(ClientStatus.ACTIVE)).thenReturn(5L);

            // When
            long result = clientService.countClientsByStatus(ClientStatus.ACTIVE);

            // Then
            assertThat(result).isEqualTo(5L);
            verify(clientRepository).countByStatus(ClientStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should count active clients")
        void shouldCountActiveClients() {
            // Given
            when(clientRepository.countByStatus(ClientStatus.ACTIVE)).thenReturn(7L);

            // When
            long result = clientService.countActiveClients();

            // Then
            assertThat(result).isEqualTo(7L);
            verify(clientRepository).countByStatus(ClientStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should count inactive clients")
        void shouldCountInactiveClients() {
            // Given
            when(clientRepository.countByStatus(ClientStatus.INACTIVE)).thenReturn(3L);

            // When
            long result = clientService.countInactiveClients();

            // Then
            assertThat(result).isEqualTo(3L);
            verify(clientRepository).countByStatus(ClientStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should count suspended clients")
        void shouldCountSuspendedClients() {
            // Given
            when(clientRepository.countByStatus(ClientStatus.SUSPENDED)).thenReturn(2L);

            // When
            long result = clientService.countSuspendedClients();

            // Then
            assertThat(result).isEqualTo(2L);
            verify(clientRepository).countByStatus(ClientStatus.SUSPENDED);
        }

        @Test
        @DisplayName("Should count recent clients")
        void shouldCountRecentClients() {
            // Given
            LocalDateTime since = now.minusDays(7);
            when(clientRepository.countByCreatedAtAfter(since)).thenReturn(4L);

            // When
            long result = clientService.countRecentClients(since);

            // Then
            assertThat(result).isEqualTo(4L);
            verify(clientRepository).countByCreatedAtAfter(since);
        }

        @Test
        @DisplayName("Should get client counts by status")
        void shouldGetClientCountsByStatus() {
            // Given
            when(clientRepository.countByStatus(ClientStatus.ACTIVE)).thenReturn(5L);
            when(clientRepository.countByStatus(ClientStatus.INACTIVE)).thenReturn(3L);
            when(clientRepository.countByStatus(ClientStatus.SUSPENDED)).thenReturn(2L);

            // When
            Map<ClientStatus, Long> result = clientService.getClientCountsByStatus();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(ClientStatus.ACTIVE)).isEqualTo(5L);
            assertThat(result.get(ClientStatus.INACTIVE)).isEqualTo(3L);
            assertThat(result.get(ClientStatus.SUSPENDED)).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should get comprehensive client statistics")
        void shouldGetComprehensiveClientStatistics() {
            // Given
            when(clientRepository.count()).thenReturn(10L);
            when(clientRepository.countByStatus(ClientStatus.ACTIVE)).thenReturn(6L);
            when(clientRepository.countByStatus(ClientStatus.INACTIVE)).thenReturn(3L);
            when(clientRepository.countByStatus(ClientStatus.SUSPENDED)).thenReturn(1L);
            when(clientRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(2L);

            // When
            Map<String, Object> result = clientService.getClientStatistics();

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).containsKey("totalClients");
            assertThat(result).containsKey("activeClients");
            assertThat(result).containsKey("inactiveClients");
            assertThat(result).containsKey("suspendedClients");
        }
    }

    // === EMAIL VALIDATION TESTS ===

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should return true when email is available")
        void shouldReturnTrueWhenEmailIsAvailable() {
            // Given
            when(clientRepository.existsByEmail("available@example.com")).thenReturn(false);

            // When
            boolean result = clientService.isEmailAvailable("available@example.com");

            // Then
            assertThat(result).isTrue();
            verify(clientRepository).existsByEmail("available@example.com");
        }

        @Test
        @DisplayName("Should return false when email is taken")
        void shouldReturnFalseWhenEmailIsTaken() {
            // Given
            when(clientRepository.existsByEmail("taken@example.com")).thenReturn(true);

            // When
            boolean result = clientService.isEmailAvailable("taken@example.com");

            // Then
            assertThat(result).isFalse();
            verify(clientRepository).existsByEmail("taken@example.com");
        }

        @Test
        @DisplayName("Should return true for isEmailTaken when email exists")
        void shouldReturnTrueForIsEmailTakenWhenEmailExists() {
            // Given
            when(clientRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When
            boolean result = clientService.isEmailTaken("existing@example.com");

            // Then
            assertThat(result).isTrue();
            verify(clientRepository).existsByEmail("existing@example.com");
        }

        @Test
        @DisplayName("Should normalize email case for availability check")
        void shouldNormalizeEmailCaseForAvailabilityCheck() {
            // Given
            when(clientRepository.existsByEmail("test@example.com")).thenReturn(false);

            // When
            clientService.isEmailAvailable("Test@Example.Com");

            // Then
            verify(clientRepository).existsByEmail("test@example.com");
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
            when(clientRepository.findById(1L)).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> clientService.getClientById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");
        }

        @Test
        @DisplayName("Should handle null pageable gracefully")
        void shouldHandleNullPageableGracefully() {
            // Given
            when(clientRepository.findAll((Pageable) null)).thenReturn(Page.empty());

            // When & Then
            assertThatCode(() -> clientService.findAllClients(null))
                .doesNotThrowAnyException();
        }
    }

    // === EDGE CASES TESTS ===

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty repository results")
        void shouldHandleEmptyRepositoryResults() {
            // Given
            when(clientRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            // When
            Page<Client> result = clientService.findAllClients(mock(Pageable.class));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long client names")
        void shouldHandleVeryLongClientNames() {
            // Given
            String longName = "A".repeat(100);
            when(clientRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When & Then
            assertThatCode(() -> 
                clientService.createClient(longName, longName, "test@example.com", 
                    null, null, null))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void shouldHandleSpecialCharactersInNames() {
            // Given
            when(clientRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(activeClient);

            // When & Then
            assertThatCode(() -> 
                clientService.createClient("José", "O'Connor-Smith", "test@example.com", 
                    null, null, null))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle zero count results")
        void shouldHandleZeroCountResults() {
            // Given
            when(clientRepository.countByStatus(ClientStatus.SUSPENDED)).thenReturn(0L);

            // When
            long result = clientService.countSuspendedClients();

            // Then
            assertThat(result).isEqualTo(0L);
        }
    }
} 
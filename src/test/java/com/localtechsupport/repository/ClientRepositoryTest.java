package com.localtechsupport.repository;

import com.localtechsupport.entity.Client;
import com.localtechsupport.entity.Client.ClientStatus;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@DisplayName("ClientRepository Tests")
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Client testClient1;
    private Client testClient2;
    private Client testClient3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        clientRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    // Test data helper methods
    private Client createTestClient(String firstName, String lastName, String email, ClientStatus status) {
        Client client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setStatus(status);
        client.setPhone("555-0123");
        client.setAddress("123 Test St");
        client.setNotes("Test notes");
        return client;
    }

    private Client createTestClient(String email) {
        return createTestClient("John", "Doe", email, ClientStatus.ACTIVE);
    }

    private void setupTestData() {
        testClient1 = createTestClient("John", "Doe", "john.doe@example.com", ClientStatus.ACTIVE);
        testClient2 = createTestClient("Jane", "Smith", "jane.smith@example.com", ClientStatus.INACTIVE);
        testClient3 = createTestClient("Bob", "Johnson", "bob.johnson@example.com", ClientStatus.SUSPENDED);
        
        entityManager.persistAndFlush(testClient1);
        entityManager.persistAndFlush(testClient2);
        entityManager.persistAndFlush(testClient3);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Standard CRUD Operations")
    class StandardCrudTests {

        @Test
        @DisplayName("Should save new client successfully")
        void shouldSaveNewClientSuccessfully() {
            // Given
            Client client = createTestClient("test@example.com");

            // When
            Client savedClient = clientRepository.save(client);

            // Then
            assertThat(savedClient).isNotNull();
            assertThat(savedClient.getId()).isNotNull();
            assertThat(savedClient.getEmail()).isEqualTo("test@example.com");
            assertThat(savedClient.getCreatedAt()).isNotNull();
            assertThat(savedClient.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update existing client successfully")
        void shouldUpdateExistingClientSuccessfully() {
            // Given
            Client client = createTestClient("original@example.com");
            Client savedClient = entityManager.persistAndFlush(client);
            entityManager.clear();

            // When
            savedClient.setEmail("updated@example.com");
            savedClient.setFirstName("Updated");
            Client updatedClient = clientRepository.save(savedClient);

            // Then
            assertThat(updatedClient.getEmail()).isEqualTo("updated@example.com");
            assertThat(updatedClient.getFirstName()).isEqualTo("Updated");
            assertThat(updatedClient.getUpdatedAt()).isAfter(updatedClient.getCreatedAt());
        }

        @Test
        @DisplayName("Should find client by existing ID")
        void shouldFindClientByExistingId() {
            // Given
            Client client = createTestClient("find@example.com");
            Client savedClient = entityManager.persistAndFlush(client);
            entityManager.clear();

            // When
            Optional<Client> result = clientRepository.findById(savedClient.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("find@example.com");
        }

        @Test
        @DisplayName("Should return empty when finding by non-existing ID")
        void shouldReturnEmptyWhenFindingByNonExistingId() {
            // When
            Optional<Client> result = clientRepository.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all clients")
        void shouldFindAllClients() {
            // Given
            setupTestData();

            // When
            List<Client> clients = clientRepository.findAll();

            // Then
            assertThat(clients).hasSize(3);
            assertThat(clients).extracting(Client::getEmail)
                .containsExactlyInAnyOrder(
                    "john.doe@example.com",
                    "jane.smith@example.com",
                    "bob.johnson@example.com"
                );
        }

        @Test
        @DisplayName("Should return empty list when no clients exist")
        void shouldReturnEmptyListWhenNoClientsExist() {
            // When
            List<Client> clients = clientRepository.findAll();

            // Then
            assertThat(clients).isEmpty();
        }

        @Test
        @DisplayName("Should delete client by ID")
        void shouldDeleteClientById() {
            // Given
            Client client = createTestClient("delete@example.com");
            Client savedClient = entityManager.persistAndFlush(client);
            entityManager.clear();

            // When
            clientRepository.deleteById(savedClient.getId());

            // Then
            Optional<Client> result = clientRepository.findById(savedClient.getId());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count all clients correctly")
        void shouldCountAllClientsCorrectly() {
            // Given
            setupTestData();

            // When
            long count = clientRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Email-Based Lookup Tests")
    class EmailLookupTests {

        @Test
        @DisplayName("Should find client by exact email match")
        void shouldFindClientByExactEmailMatch() {
            // Given
            Client client = createTestClient("exact@example.com");
            entityManager.persistAndFlush(client);

            // When
            Optional<Client> result = clientRepository.findByEmail("exact@example.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("exact@example.com");
        }

        @Test
        @DisplayName("Should return empty for non-existing email")
        void shouldReturnEmptyForNonExistingEmail() {
            // When
            Optional<Client> result = clientRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should be case sensitive for email search")
        void shouldBeCaseSensitiveForEmailSearch() {
            // Given
            Client client = createTestClient("CaseSensitive@Example.com");
            entityManager.persistAndFlush(client);

            // When
            Optional<Client> result1 = clientRepository.findByEmail("CaseSensitive@Example.com");
            Optional<Client> result2 = clientRepository.findByEmail("casesensitive@example.com");

            // Then
            assertThat(result1).isPresent();
            assertThat(result2).isEmpty();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // When
            boolean exists = clientRepository.existsByEmail("nonexistent@example.com");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            Client client = createTestClient("exists@example.com");
            entityManager.persistAndFlush(client);

            // When
            boolean exists = clientRepository.existsByEmail("exists@example.com");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should handle null email gracefully in findByEmail")
        void shouldHandleNullEmailGracefullyInFindByEmail() {
            // When
            Optional<Client> result = clientRepository.findByEmail(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty email in findByEmail")
        void shouldHandleEmptyEmailInFindByEmail() {
            // When
            Optional<Client> result = clientRepository.findByEmail("");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Status-Based Query Tests")
    class StatusBasedTests {

        @Test
        @DisplayName("Should find clients by ACTIVE status")
        void shouldFindClientsByActiveStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatus(ClientStatus.ACTIVE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(ClientStatus.ACTIVE);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should find clients by INACTIVE status")
        void shouldFindClientsByInactiveStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatus(ClientStatus.INACTIVE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(ClientStatus.INACTIVE);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("jane.smith@example.com");
        }

        @Test
        @DisplayName("Should find clients by SUSPENDED status")
        void shouldFindClientsBySuspendedStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatus(ClientStatus.SUSPENDED, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(ClientStatus.SUSPENDED);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("bob.johnson@example.com");
        }

        @Test
        @DisplayName("Should return empty page when no clients match status")
        void shouldReturnEmptyPageWhenNoClientsMatchStatus() {
            // Given - no clients with SUSPENDED status
            Client client = createTestClient("John", "Doe", "active@example.com", ClientStatus.ACTIVE);
            entityManager.persistAndFlush(client);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatus(ClientStatus.SUSPENDED, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should count clients by ACTIVE status")
        void shouldCountClientsByActiveStatus() {
            // Given
            setupTestData();

            // When
            long count = clientRepository.countByStatus(ClientStatus.ACTIVE);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count clients by INACTIVE status")
        void shouldCountClientsByInactiveStatus() {
            // Given
            setupTestData();

            // When
            long count = clientRepository.countByStatus(ClientStatus.INACTIVE);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return zero count for status with no clients")
        void shouldReturnZeroCountForStatusWithNoClients() {
            // Given - only ACTIVE clients
            Client client = createTestClient("John", "Doe", "active@example.com", ClientStatus.ACTIVE);
            entityManager.persistAndFlush(client);

            // When
            long count = clientRepository.countByStatus(ClientStatus.SUSPENDED);

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Search Functionality Tests")
    class SearchTests {

        @BeforeEach
        void setupSearchData() {
            Client client1 = createTestClient("John", "Doe", "john.doe@example.com", ClientStatus.ACTIVE);
            client1.setPhone("555-1234");
            
            Client client2 = createTestClient("Jane", "Smith", "jane.smith@example.com", ClientStatus.ACTIVE);
            client2.setPhone("555-5678");
            
            Client client3 = createTestClient("Johnny", "Johnson", "johnny.johnson@example.com", ClientStatus.ACTIVE);
            client3.setPhone("555-9999");

            entityManager.persistAndFlush(client1);
            entityManager.persistAndFlush(client2);
            entityManager.persistAndFlush(client3);
        }

        @Test
        @DisplayName("Should search clients by first name")
        void shouldSearchClientsByFirstName() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.searchClients("John", pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // John and Johnny
            assertThat(result.getContent()).extracting(Client::getFirstName)
                .containsExactlyInAnyOrder("John", "Johnny");
        }

        @Test
        @DisplayName("Should search clients by last name")
        void shouldSearchClientsByLastName() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.searchClients("Smith", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should search clients by email")
        void shouldSearchClientsByEmail() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.searchClients("jane.smith", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).contains("jane.smith");
        }

        @Test
        @DisplayName("Should search clients by phone")
        void shouldSearchClientsByPhone() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.searchClients("555-1234", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getPhone()).isEqualTo("555-1234");
        }

        @Test
        @DisplayName("Should perform case insensitive search")
        void shouldPerformCaseInsensitiveSearch() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result1 = clientRepository.searchClients("JOHN", pageable);
            Page<Client> result2 = clientRepository.searchClients("john", pageable);
            Page<Client> result3 = clientRepository.searchClients("John", pageable);

            // Then
            assertThat(result1.getContent()).hasSize(2);
            assertThat(result2.getContent()).hasSize(2);
            assertThat(result3.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("Should handle partial matches")
        void shouldHandlePartialMatches() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - search for "John" which should match "John" and "Johnny" first names
            Page<Client> result = clientRepository.searchClients("John", pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // John, Johnny
        }

        @Test
        @DisplayName("Should return empty result for no matches")
        void shouldReturnEmptyResultForNoMatches() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.searchClients("NonExistent", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should handle empty search term")
        void shouldHandleEmptySearchTerm() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.searchClients("", pageable);

            // Then
            assertThat(result.getContent()).hasSize(3); // Should match all due to empty string
        }

        @Test
        @DisplayName("Should handle null search term")
        void shouldHandleNullSearchTerm() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.searchClients(null, pageable);

            // Then
            // Null search term returns empty results (no matches for null in LIKE query)
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Time-Based Query Tests")
    class TimeBasedTests {

        private LocalDateTime baseTime;
        private LocalDateTime hourAgo;
        private LocalDateTime hourFromNow;

        @BeforeEach
        void setupTimeData() {
            baseTime = LocalDateTime.now();
            hourAgo = baseTime.minusHours(1);
            hourFromNow = baseTime.plusHours(1);

            // Create clients with different creation times
            // Note: We persist first, then use native SQL to update timestamps
            // because @CreationTimestamp overrides our setCreatedAt calls
            Client oldClient = createTestClient("old@example.com");
            Client recentClient = createTestClient("recent@example.com");
            Client futureClient = createTestClient("future@example.com");

            entityManager.persistAndFlush(oldClient);
            entityManager.persistAndFlush(recentClient);
            entityManager.persistAndFlush(futureClient);
            
            // Update timestamps using native SQL to bypass Hibernate annotations
            entityManager.getEntityManager().createNativeQuery(
                "UPDATE clients SET created_at = ? WHERE email = ?")
                .setParameter(1, hourAgo.minusHours(1))
                .setParameter(2, "old@example.com")
                .executeUpdate();
                
            entityManager.getEntityManager().createNativeQuery(
                "UPDATE clients SET created_at = ? WHERE email = ?")
                .setParameter(1, hourAgo.plusMinutes(30))
                .setParameter(2, "recent@example.com")
                .executeUpdate();
                
            entityManager.getEntityManager().createNativeQuery(
                "UPDATE clients SET created_at = ? WHERE email = ?")
                .setParameter(1, hourFromNow)
                .setParameter(2, "future@example.com")
                .executeUpdate();
                
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("Should find clients created after specific date")
        void shouldFindClientsCreatedAfterSpecificDate() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByCreatedAtAfter(hourAgo, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // recent and future clients
            assertThat(result.getContent()).extracting(Client::getEmail)
                .containsExactlyInAnyOrder("recent@example.com", "future@example.com");
        }

        @Test
        @DisplayName("Should return empty when no clients created after date")
        void shouldReturnEmptyWhenNoClientsCreatedAfterDate() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime futureDate = baseTime.plusDays(1);

            // When
            Page<Client> result = clientRepository.findByCreatedAtAfter(futureDate, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should find clients created between dates")
        void shouldFindClientsCreatedBetweenDates() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime startDate = hourAgo.minusMinutes(30);
            LocalDateTime endDate = hourAgo.plusHours(1);

            // When
            Page<Client> result = clientRepository.findByCreatedAtBetween(startDate, endDate, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1); // only recent client
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("recent@example.com");
        }

        @Test
        @DisplayName("Should handle inclusive date range")
        void shouldHandleInclusiveDateRange() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            // Use the same base time calculation as setup to avoid race conditions
            LocalDateTime exactTime = baseTime.minusHours(1).plusMinutes(30);

            // When
            Page<Client> result = clientRepository.findByCreatedAtBetween(exactTime, exactTime, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("recent@example.com");
        }

        @Test
        @DisplayName("Should count clients created after date")
        void shouldCountClientsCreatedAfterDate() {
            // When
            long count = clientRepository.countByCreatedAtAfter(hourAgo);

            // Then
            assertThat(count).isEqualTo(2); // recent and future clients
        }

        @Test
        @DisplayName("Should return zero count when no clients created after date")
        void shouldReturnZeroCountWhenNoClientsCreatedAfterDate() {
            // Given
            LocalDateTime futureDate = baseTime.plusDays(1);

            // When
            long count = clientRepository.countByCreatedAtAfter(futureDate);

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Combined Filtering Tests")
    class CombinedFilteringTests {
        
        private LocalDateTime baseTime;
        private LocalDateTime hourAgo;

        @BeforeEach
        void setupCombinedData() {
            baseTime = LocalDateTime.now();
            hourAgo = baseTime.minusHours(1);
            
            // Active client created recently
            Client activeRecent = createTestClient("John", "Doe", "active.recent@example.com", ClientStatus.ACTIVE);
            
            // Active client created long ago
            Client activeOld = createTestClient("Jane", "Smith", "active.old@example.com", ClientStatus.ACTIVE);
            
            // Inactive client created recently
            Client inactiveRecent = createTestClient("Bob", "Johnson", "inactive.recent@example.com", ClientStatus.INACTIVE);

            entityManager.persistAndFlush(activeRecent);
            entityManager.persistAndFlush(activeOld);
            entityManager.persistAndFlush(inactiveRecent);
            
            // Update timestamps using native SQL
            entityManager.getEntityManager().createNativeQuery(
                "UPDATE clients SET created_at = ? WHERE email = ?")
                .setParameter(1, hourAgo.plusMinutes(30))
                .setParameter(2, "active.recent@example.com")
                .executeUpdate();
                
            entityManager.getEntityManager().createNativeQuery(
                "UPDATE clients SET created_at = ? WHERE email = ?")
                .setParameter(1, hourAgo.minusHours(2))
                .setParameter(2, "active.old@example.com")
                .executeUpdate();
                
            entityManager.getEntityManager().createNativeQuery(
                "UPDATE clients SET created_at = ? WHERE email = ?")
                .setParameter(1, hourAgo.plusMinutes(15))
                .setParameter(2, "inactive.recent@example.com")
                .executeUpdate();
                
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("Should find active clients created after specific date")
        void shouldFindActiveClientsCreatedAfterSpecificDate() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatusAndCreatedAtAfter(
                ClientStatus.ACTIVE, hourAgo, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("active.recent@example.com");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(ClientStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should find inactive clients created after specific date")
        void shouldFindInactiveClientsCreatedAfterSpecificDate() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatusAndCreatedAtAfter(
                ClientStatus.INACTIVE, hourAgo, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("inactive.recent@example.com");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(ClientStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should return empty when no clients match combined criteria")
        void shouldReturnEmptyWhenNoClientsMatchCombinedCriteria() {
            // Given
            LocalDateTime futureDate = baseTime.plusDays(1);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatusAndCreatedAtAfter(
                ClientStatus.ACTIVE, futureDate, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle suspended status in combined query")
        void shouldHandleSuspendedStatusInCombinedQuery() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatusAndCreatedAtAfter(
                ClientStatus.SUSPENDED, hourAgo, pageable);

            // Then
            assertThat(result.getContent()).isEmpty(); // No suspended clients in test data
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @BeforeEach
        void setupPaginationData() {
            // Create 15 clients for pagination testing
            for (int i = 1; i <= 15; i++) {
                Client client = createTestClient(
                    "User" + i,
                    "Test" + i,
                    "user" + i + "@example.com",
                    i % 3 == 0 ? ClientStatus.INACTIVE : ClientStatus.ACTIVE
                );
                entityManager.persist(client);
            }
            entityManager.flush();
        }

        @Test
        @DisplayName("Should handle first page correctly")
        void shouldHandleFirstPageCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 5);

            // When
            Page<Client> result = clientRepository.findByStatus(ClientStatus.ACTIVE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getNumber()).isZero(); // First page
            assertThat(result.getTotalPages()).isEqualTo(2); // 10 active clients / 5 per page
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("Should handle middle page correctly")
        void shouldHandleMiddlePageCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(1, 3);

            // When
            Page<Client> result = clientRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(5); // 15 clients / 3 per page
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should handle last page correctly")
        void shouldHandleLastPageCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(4, 3); // Last page

            // When
            Page<Client> result = clientRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(3); // 15 % 3 = 0, so full page
            assertThat(result.getNumber()).isEqualTo(4);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should handle page beyond available data")
        void shouldHandlePageBeyondAvailableData() {
            // Given
            Pageable pageable = PageRequest.of(10, 5); // Way beyond available data

            // When
            Page<Client> result = clientRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getNumber()).isEqualTo(10);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should handle sorting by first name ascending")
        void shouldHandleSortingByFirstNameAscending() {
            // Given
            Pageable pageable = PageRequest.of(0, 5, Sort.by("firstName").ascending());

            // When
            Page<Client> result = clientRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            List<String> firstNames = result.getContent().stream()
                .map(Client::getFirstName)
                .toList();
            assertThat(firstNames).isSorted();
        }

        @Test
        @DisplayName("Should handle sorting by email descending")
        void shouldHandleSortingByEmailDescending() {
            // Given
            Pageable pageable = PageRequest.of(0, 5, Sort.by("email").descending());

            // When
            Page<Client> result = clientRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            List<String> emails = result.getContent().stream()
                .map(Client::getEmail)
                .toList();
            
            // Verify descending order
            for (int i = 0; i < emails.size() - 1; i++) {
                assertThat(emails.get(i)).isGreaterThanOrEqualTo(emails.get(i + 1));
            }
        }

        @Test
        @DisplayName("Should handle empty results with pagination")
        void shouldHandleEmptyResultsWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Client> result = clientRepository.findByStatus(ClientStatus.SUSPENDED, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isFalse();
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should enforce unique email constraint")
        void shouldEnforceUniqueEmailConstraint() {
            // Given
            Client client1 = createTestClient("duplicate@example.com");
            Client client2 = createTestClient("duplicate@example.com");
            
            entityManager.persistAndFlush(client1);

            // When & Then
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(client2);
            }).isInstanceOf(Exception.class); // Should throw constraint violation
        }

        @Test
        @DisplayName("Should handle null values properly")
        void shouldHandleNullValuesProperly() {
            // Given
            Client client = createTestClient("null.test@example.com");
            client.setPhone(null);
            client.setAddress(null);
            client.setNotes(null);

            // When & Then
            assertDoesNotThrow(() -> {
                Client saved = entityManager.persistAndFlush(client);
                assertThat(saved.getPhone()).isNull();
                assertThat(saved.getAddress()).isNull();
                assertThat(saved.getNotes()).isNull();
            });
        }

        @Test
        @DisplayName("Should not allow null required fields")
        void shouldNotAllowNullRequiredFields() {
            // Given
            Client client = new Client();
            client.setEmail("required.test@example.com");
            // firstName, lastName are required but not set

            // When & Then
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(client);
            }).isInstanceOf(Exception.class); // Should throw validation exception
        }

        @Test
        @DisplayName("Should validate email format")
        void shouldValidateEmailFormat() {
            // Given
            Client client = createTestClient("John", "Doe", "invalid-email", ClientStatus.ACTIVE);

            // When & Then
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(client);
            }).isInstanceOf(Exception.class); // Should throw validation exception
        }
    }
}
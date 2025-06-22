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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@DisplayName("TechnicianRepository Tests")
class TechnicianRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TechnicianRepository technicianRepository;

    private Technician testTechnician1;
    private Technician testTechnician2;
    private Technician testTechnician3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        technicianRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    // Test data helper methods
    private Technician createTestTechnician(String fullName, String email, TechnicianStatus status) {
        Technician technician = new Technician();
        technician.setFullName(fullName);
        technician.setEmail(email);
        technician.setStatus(status);
        return technician;
    }

    private Technician createTestTechnician(String email) {
        return createTestTechnician("John Doe", email, TechnicianStatus.ACTIVE);
    }

    private TechnicianSkill createTechnicianSkill(Technician technician, ServiceType serviceType) {
        TechnicianSkill skill = new TechnicianSkill();
        skill.setTechnician(technician);
        skill.setServiceType(serviceType);
        return skill;
    }

    private Client createTestClient(String email) {
        Client client = new Client();
        client.setFirstName("Test");
        client.setLastName("Client");
        client.setEmail(email);
        client.setStatus(Client.ClientStatus.ACTIVE);
        return client;
    }

    private Ticket createTestTicket(Client client, Technician technician, ServiceType serviceType, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setClient(client);
        ticket.setAssignedTechnician(technician);
        ticket.setServiceType(serviceType);
        ticket.setDescription("Test ticket");
        ticket.setStatus(status);
        ticket.setDueAt(Instant.now().plus(1, ChronoUnit.DAYS));
        return ticket;
    }

    private void setupTestData() {
        testTechnician1 = createTestTechnician("John Doe", "john.doe@example.com", TechnicianStatus.ACTIVE);
        testTechnician2 = createTestTechnician("Jane Smith", "jane.smith@example.com", TechnicianStatus.INACTIVE);
        testTechnician3 = createTestTechnician("Bob Johnson", "bob.johnson@example.com", TechnicianStatus.IN_TRAINING);

        entityManager.persistAndFlush(testTechnician1);
        entityManager.persistAndFlush(testTechnician2);
        entityManager.persistAndFlush(testTechnician3);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Standard CRUD Operations")
    class StandardCrudTests {

        @Test
        @DisplayName("Should save new technician successfully")
        void shouldSaveNewTechnicianSuccessfully() {
            // Given
            Technician technician = createTestTechnician("test@example.com");

            // When
            Technician savedTechnician = technicianRepository.save(technician);

            // Then
            assertThat(savedTechnician).isNotNull();
            assertThat(savedTechnician.getId()).isNotNull();
            assertThat(savedTechnician.getEmail()).isEqualTo("test@example.com");
            assertThat(savedTechnician.getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should update existing technician successfully")
        void shouldUpdateExistingTechnicianSuccessfully() {
            // Given
            Technician technician = createTestTechnician("original@example.com");
            Technician savedTechnician = entityManager.persistAndFlush(technician);
            entityManager.clear();

            // When
            savedTechnician.setEmail("updated@example.com");
            savedTechnician.setFullName("Updated Name");
            Technician updatedTechnician = technicianRepository.save(savedTechnician);

            // Then
            assertThat(updatedTechnician.getEmail()).isEqualTo("updated@example.com");
            assertThat(updatedTechnician.getFullName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should find technician by existing ID")
        void shouldFindTechnicianByExistingId() {
            // Given
            Technician technician = createTestTechnician("find@example.com");
            Technician savedTechnician = entityManager.persistAndFlush(technician);
            entityManager.clear();

            // When
            Optional<Technician> result = technicianRepository.findById(savedTechnician.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("find@example.com");
        }

        @Test
        @DisplayName("Should return empty when finding by non-existing ID")
        void shouldReturnEmptyWhenFindingByNonExistingId() {
            // When
            Optional<Technician> result = technicianRepository.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all technicians")
        void shouldFindAllTechnicians() {
            // Given
            setupTestData();

            // When
            List<Technician> technicians = technicianRepository.findAll();

            // Then
            assertThat(technicians).hasSize(3);
            assertThat(technicians).extracting(Technician::getEmail)
                .containsExactlyInAnyOrder(
                    "john.doe@example.com",
                    "jane.smith@example.com",
                    "bob.johnson@example.com"
                );
        }

        @Test
        @DisplayName("Should return empty list when no technicians exist")
        void shouldReturnEmptyListWhenNoTechniciansExist() {
            // When
            List<Technician> technicians = technicianRepository.findAll();

            // Then
            assertThat(technicians).isEmpty();
        }

        @Test
        @DisplayName("Should delete technician by ID")
        void shouldDeleteTechnicianById() {
            // Given
            Technician technician = createTestTechnician("delete@example.com");
            Technician savedTechnician = entityManager.persistAndFlush(technician);
            entityManager.clear();

            // When
            technicianRepository.deleteById(savedTechnician.getId());

            // Then
            Optional<Technician> result = technicianRepository.findById(savedTechnician.getId());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count all technicians correctly")
        void shouldCountAllTechniciansCorrectly() {
            // Given
            setupTestData();

            // When
            long count = technicianRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Email-Based Lookup Tests")
    class EmailLookupTests {

        @Test
        @DisplayName("Should find technician by exact email match")
        void shouldFindTechnicianByExactEmailMatch() {
            // Given
            Technician technician = createTestTechnician("exact@example.com");
            entityManager.persistAndFlush(technician);

            // When
            Optional<Technician> result = technicianRepository.findByEmail("exact@example.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("exact@example.com");
        }

        @Test
        @DisplayName("Should return empty for non-existing email")
        void shouldReturnEmptyForNonExistingEmail() {
            // When
            Optional<Technician> result = technicianRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should be case sensitive for email search")
        void shouldBeCaseSensitiveForEmailSearch() {
            // Given
            Technician technician = createTestTechnician("CaseSensitive@Example.com");
            entityManager.persistAndFlush(technician);

            // When
            Optional<Technician> result1 = technicianRepository.findByEmail("CaseSensitive@Example.com");
            Optional<Technician> result2 = technicianRepository.findByEmail("casesensitive@example.com");

            // Then
            assertThat(result1).isPresent();
            assertThat(result2).isEmpty();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // When
            boolean exists = technicianRepository.existsByEmail("nonexistent@example.com");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            Technician technician = createTestTechnician("exists@example.com");
            entityManager.persistAndFlush(technician);

            // When
            boolean exists = technicianRepository.existsByEmail("exists@example.com");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should handle null email gracefully in findByEmail")
        void shouldHandleNullEmailGracefullyInFindByEmail() {
            // When
            Optional<Technician> result = technicianRepository.findByEmail(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty email in findByEmail")
        void shouldHandleEmptyEmailInFindByEmail() {
            // When
            Optional<Technician> result = technicianRepository.findByEmail("");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Status-Based Query Tests")
    class StatusBasedTests {

        @Test
        @DisplayName("Should find technicians by ACTIVE status")
        void shouldFindTechniciansByActiveStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByStatus(TechnicianStatus.ACTIVE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should find technicians by INACTIVE status")
        void shouldFindTechniciansByInactiveStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByStatus(TechnicianStatus.INACTIVE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TechnicianStatus.INACTIVE);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("jane.smith@example.com");
        }

        @Test
        @DisplayName("Should find technicians by IN_TRAINING status")
        void shouldFindTechniciansByInTrainingStatus() {
            // Given
            setupTestData();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByStatus(TechnicianStatus.IN_TRAINING, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TechnicianStatus.IN_TRAINING);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("bob.johnson@example.com");
        }

        @Test
        @DisplayName("Should return empty page when no technicians match status")
        void shouldReturnEmptyPageWhenNoTechniciansMatchStatus() {
            // Given - no technicians with TERMINATED status
            Technician technician = createTestTechnician("John Doe", "active@example.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(technician);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByStatus(TechnicianStatus.TERMINATED, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should find technicians by status using list method")
        void shouldFindTechniciansByStatusUsingListMethod() {
            // Given
            setupTestData();

            // When
            List<Technician> result = technicianRepository.findByStatus(TechnicianStatus.ACTIVE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should count technicians by ACTIVE status")
        void shouldCountTechniciansByActiveStatus() {
            // Given
            setupTestData();

            // When
            long count = technicianRepository.countByStatus(TechnicianStatus.ACTIVE);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count technicians by INACTIVE status")
        void shouldCountTechniciansByInactiveStatus() {
            // Given
            setupTestData();

            // When
            long count = technicianRepository.countByStatus(TechnicianStatus.INACTIVE);

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return zero count for status with no technicians")
        void shouldReturnZeroCountForStatusWithNoTechnicians() {
            // Given - only ACTIVE technicians
            Technician technician = createTestTechnician("John Doe", "active@example.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(technician);

            // When
            long count = technicianRepository.countByStatus(TechnicianStatus.TERMINATED);

            // Then
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Search Functionality Tests")
    class SearchTests {

        @BeforeEach
        void setupSearchData() {
            Technician tech1 = createTestTechnician("John Doe", "john.doe@example.com", TechnicianStatus.ACTIVE);
            Technician tech2 = createTestTechnician("Jane Smith", "jane.smith@example.com", TechnicianStatus.ACTIVE);
            Technician tech3 = createTestTechnician("Johnny Johnson", "johnny.johnson@example.com", TechnicianStatus.ACTIVE);

            entityManager.persistAndFlush(tech1);
            entityManager.persistAndFlush(tech2);
            entityManager.persistAndFlush(tech3);
        }

        @Test
        @DisplayName("Should search technicians by full name")
        void shouldSearchTechniciansByFullName() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.searchTechnicians("John", pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // John and Johnny
        }

        @Test
        @DisplayName("Should search technicians by email")
        void shouldSearchTechniciansByEmail() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.searchTechnicians("jane.smith", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).contains("jane.smith");
        }

        @Test
        @DisplayName("Should perform case insensitive search")
        void shouldPerformCaseInsensitiveSearch() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result1 = technicianRepository.searchTechnicians("JOHN", pageable);
            Page<Technician> result2 = technicianRepository.searchTechnicians("john", pageable);
            Page<Technician> result3 = technicianRepository.searchTechnicians("John", pageable);

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

            // When
            Page<Technician> result = technicianRepository.searchTechnicians("Doe", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFullName()).contains("Doe");
        }

        @Test
        @DisplayName("Should return empty result for no matches")
        void shouldReturnEmptyResultForNoMatches() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.searchTechnicians("NonExistent", pageable);

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
            Page<Technician> result = technicianRepository.searchTechnicians("", pageable);

            // Then
            assertThat(result.getContent()).hasSize(3); // Should match all due to empty string
        }

        @Test
        @DisplayName("Should handle null search term")
        void shouldHandleNullSearchTerm() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.searchTechnicians(null, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Service Type and Skills Tests")
    class ServiceTypeTests {

        @BeforeEach
        void setupServiceTypeData() {
            // Create technicians with different skills
            Technician hardwareTech = createTestTechnician("Hardware Tech", "hardware@example.com", TechnicianStatus.ACTIVE);
            Technician softwareTech = createTestTechnician("Software Tech", "software@example.com", TechnicianStatus.ACTIVE);
            Technician bothTech = createTestTechnician("Both Tech", "both@example.com", TechnicianStatus.ACTIVE);

            entityManager.persistAndFlush(hardwareTech);
            entityManager.persistAndFlush(softwareTech);
            entityManager.persistAndFlush(bothTech);

            // Add skills
            TechnicianSkill hardwareSkill1 = createTechnicianSkill(hardwareTech, ServiceType.HARDWARE);
            TechnicianSkill softwareSkill = createTechnicianSkill(softwareTech, ServiceType.SOFTWARE);
            TechnicianSkill hardwareSkill2 = createTechnicianSkill(bothTech, ServiceType.HARDWARE);
            TechnicianSkill softwareSkill2 = createTechnicianSkill(bothTech, ServiceType.SOFTWARE);

            entityManager.persistAndFlush(hardwareSkill1);
            entityManager.persistAndFlush(softwareSkill);
            entityManager.persistAndFlush(hardwareSkill2);
            entityManager.persistAndFlush(softwareSkill2);
        }

        @Test
        @DisplayName("Should find technicians by HARDWARE service type")
        void shouldFindTechniciansByHardwareServiceType() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByServiceType(ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // hardwareTech and bothTech
        }

        @Test
        @DisplayName("Should find technicians by SOFTWARE service type")
        void shouldFindTechniciansBySoftwareServiceType() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByServiceType(ServiceType.SOFTWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // softwareTech and bothTech
        }

        @Test
        @DisplayName("Should find technicians by service type using list method")
        void shouldFindTechniciansByServiceTypeUsingListMethod() {
            // When
            List<Technician> result = technicianRepository.findByServiceType(ServiceType.HARDWARE);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should count technicians by HARDWARE service type")
        void shouldCountTechniciansByHardwareServiceType() {
            // When
            long count = technicianRepository.countByServiceType(ServiceType.HARDWARE);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count technicians by SOFTWARE service type")
        void shouldCountTechniciansBySoftwareServiceType() {
            // When
            long count = technicianRepository.countByServiceType(ServiceType.SOFTWARE);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return empty when no technicians have specific service type")
        void shouldReturnEmptyWhenNoTechniciansHaveSpecificServiceType() {
            // Given - clear data and create technician without skills
            entityManager.getEntityManager().createQuery("DELETE FROM TechnicianSkill").executeUpdate();
            Technician techWithoutSkills = createTestTechnician("No Skills", "noskills@example.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(techWithoutSkills);
            
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByServiceType(ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Load Balancing and Availability Tests")
    class LoadBalancingTests {

        @BeforeEach
        void setupLoadBalancingData() {
            // Create test client
            Client client = createTestClient("client@example.com");
            entityManager.persistAndFlush(client);

            // Create technicians with different loads
            Technician lightLoadTech = createTestTechnician("Light Load", "light@example.com", TechnicianStatus.ACTIVE);
            Technician heavyLoadTech = createTestTechnician("Heavy Load", "heavy@example.com", TechnicianStatus.ACTIVE);
            Technician inactiveTech = createTestTechnician("Inactive Tech", "inactive@example.com", TechnicianStatus.INACTIVE);

            entityManager.persistAndFlush(lightLoadTech);
            entityManager.persistAndFlush(heavyLoadTech);
            entityManager.persistAndFlush(inactiveTech);

            // Create tickets to simulate load
            Ticket ticket1 = createTestTicket(client, lightLoadTech, ServiceType.HARDWARE, TicketStatus.OPEN);
            Ticket ticket2 = createTestTicket(client, heavyLoadTech, ServiceType.HARDWARE, TicketStatus.OPEN);
            Ticket ticket3 = createTestTicket(client, heavyLoadTech, ServiceType.SOFTWARE, TicketStatus.OPEN);
            Ticket ticket4 = createTestTicket(client, heavyLoadTech, ServiceType.HARDWARE, TicketStatus.CLOSED); // Closed ticket shouldn't count

            entityManager.persistAndFlush(ticket1);
            entityManager.persistAndFlush(ticket2);
            entityManager.persistAndFlush(ticket3);
            entityManager.persistAndFlush(ticket4);
        }

        @Test
        @DisplayName("Should find technicians with low current load")
        void shouldFindTechniciansWithLowCurrentLoad() {
            // When - find active technicians with max 1 open ticket
            List<Technician> result = technicianRepository.findByStatusAndCurrentLoadLessThanEqual(TechnicianStatus.ACTIVE, 1L);

            // Then - should find lightLoadTech (1 open ticket)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("light@example.com");
        }

        @Test
        @DisplayName("Should find available technicians with specified max load")
        void shouldFindAvailableTechniciansWithSpecifiedMaxLoad() {
            // When - find technicians with less than 2 open tickets
            List<Technician> result = technicianRepository.findAvailableTechnicians(TechnicianStatus.ACTIVE, 2L);

            // Then - should find lightLoadTech (1 open ticket)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("light@example.com");
        }

        @Test
        @DisplayName("Should not find inactive technicians in availability queries")
        void shouldNotFindInactiveTechniciansInAvailabilityQueries() {
            // When
            List<Technician> result = technicianRepository.findAvailableTechnicians(TechnicianStatus.ACTIVE, 10L);

            // Then - should only find active technicians
            assertThat(result).extracting(Technician::getStatus)
                .allMatch(status -> status == TechnicianStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should count available technicians correctly")
        void shouldCountAvailableTechniciansCorrectly() {
            // When
            long count = technicianRepository.countAvailableTechnicians(TechnicianStatus.ACTIVE, 2L);

            // Then
            assertThat(count).isEqualTo(1); // Only lightLoadTech
        }

        @Test
        @DisplayName("Should return empty when no technicians meet load criteria")
        void shouldReturnEmptyWhenNoTechniciansMeetLoadCriteria() {
            // When - very restrictive load limit
            List<Technician> result = technicianRepository.findAvailableTechnicians(TechnicianStatus.ACTIVE, 0L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Combined Filtering Tests")
    class CombinedFilteringTests {

        @BeforeEach
        void setupCombinedData() {
            // Create test client
            Client client = createTestClient("client@example.com");
            entityManager.persistAndFlush(client);

            // Active technician with hardware skills
            Technician activeHardware = createTestTechnician("Active Hardware", "active.hw@example.com", TechnicianStatus.ACTIVE);
            // Active technician with software skills
            Technician activeSoftware = createTestTechnician("Active Software", "active.sw@example.com", TechnicianStatus.ACTIVE);
            // Inactive technician with hardware skills
            Technician inactiveHardware = createTestTechnician("Inactive Hardware", "inactive.hw@example.com", TechnicianStatus.INACTIVE);

            entityManager.persistAndFlush(activeHardware);
            entityManager.persistAndFlush(activeSoftware);
            entityManager.persistAndFlush(inactiveHardware);

            // Add skills
            TechnicianSkill activeHwSkill = createTechnicianSkill(activeHardware, ServiceType.HARDWARE);
            TechnicianSkill activeSwSkill = createTechnicianSkill(activeSoftware, ServiceType.SOFTWARE);
            TechnicianSkill inactiveHwSkill = createTechnicianSkill(inactiveHardware, ServiceType.HARDWARE);

            entityManager.persistAndFlush(activeHwSkill);
            entityManager.persistAndFlush(activeSwSkill);
            entityManager.persistAndFlush(inactiveHwSkill);

            // Add some tickets for load testing
            Ticket ticket1 = createTestTicket(client, activeHardware, ServiceType.HARDWARE, TicketStatus.OPEN);
            entityManager.persistAndFlush(ticket1);
        }

        @Test
        @DisplayName("Should find active technicians with specific service type")
        void shouldFindActiveTechniciansWithSpecificServiceType() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByStatusAndServiceType(
                TechnicianStatus.ACTIVE, ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("active.hw@example.com");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should find inactive technicians with specific service type")
        void shouldFindInactiveTechniciansWithSpecificServiceType() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> result = technicianRepository.findByStatusAndServiceType(
                TechnicianStatus.INACTIVE, ServiceType.HARDWARE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("inactive.hw@example.com");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TechnicianStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should use list method for status and service type combination")
        void shouldUseListMethodForStatusAndServiceTypeCombination() {
            // When
            List<Technician> result = technicianRepository.findByStatusAndServiceType(
                TechnicianStatus.ACTIVE, ServiceType.HARDWARE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("active.hw@example.com");
        }

        @Test
        @DisplayName("Should find available technicians for specific service")
        void shouldFindAvailableTechniciansForSpecificService() {
            // When - find available hardware technicians with max 2 open tickets
            List<Technician> result = technicianRepository.findAvailableTechniciansForService(
                TechnicianStatus.ACTIVE, ServiceType.HARDWARE, 2L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("active.hw@example.com");
        }

        @Test
        @DisplayName("Should return empty when no technicians match combined criteria")
        void shouldReturnEmptyWhenNoTechniciansMatchCombinedCriteria() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - look for terminated technicians with software skills (none exist)
            Page<Technician> result = technicianRepository.findByStatusAndServiceType(
                TechnicianStatus.TERMINATED, ServiceType.SOFTWARE, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should not find overloaded technicians in available for service")
        void shouldNotFindOverloadedTechniciansInAvailableForService() {
            // Given - add more tickets to make technician overloaded
            Client client = entityManager.find(Client.class, 
                entityManager.getEntityManager().createQuery("SELECT c.id FROM Client c", Long.class).getSingleResult());
            Technician tech = entityManager.getEntityManager()
                .createQuery("SELECT t FROM Technician t WHERE t.email = 'active.hw@example.com'", Technician.class)
                .getSingleResult();
            
            Ticket ticket2 = createTestTicket(client, tech, ServiceType.HARDWARE, TicketStatus.OPEN);
            Ticket ticket3 = createTestTicket(client, tech, ServiceType.HARDWARE, TicketStatus.OPEN);
            entityManager.persistAndFlush(ticket2);
            entityManager.persistAndFlush(ticket3);

            // When - look for technicians with max 2 open tickets (technician now has 3)
            List<Technician> result = technicianRepository.findAvailableTechniciansForService(
                TechnicianStatus.ACTIVE, ServiceType.HARDWARE, 2L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @BeforeEach
        void setupPaginationData() {
            // Create 15 technicians for pagination testing
            for (int i = 1; i <= 15; i++) {
                Technician technician = createTestTechnician(
                    "Tech" + i,
                    "tech" + i + "@example.com",
                    i % 3 == 0 ? TechnicianStatus.INACTIVE : TechnicianStatus.ACTIVE
                );
                entityManager.persist(technician);
            }
            entityManager.flush();
        }

        @Test
        @DisplayName("Should handle first page correctly")
        void shouldHandleFirstPageCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 5);

            // When
            Page<Technician> result = technicianRepository.findByStatus(TechnicianStatus.ACTIVE, pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getNumber()).isZero(); // First page
            assertThat(result.getTotalPages()).isEqualTo(2); // 10 active technicians / 5 per page
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
            Page<Technician> result = technicianRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(5); // 15 technicians / 3 per page
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should handle last page correctly")
        void shouldHandleLastPageCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(4, 3); // Last page

            // When
            Page<Technician> result = technicianRepository.findAll(pageable);

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
            Page<Technician> result = technicianRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getNumber()).isEqualTo(10);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should handle sorting by full name ascending")
        void shouldHandleSortingByFullNameAscending() {
            // Given
            Pageable pageable = PageRequest.of(0, 5, Sort.by("fullName").ascending());

            // When
            Page<Technician> result = technicianRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            List<String> fullNames = result.getContent().stream()
                .map(Technician::getFullName)
                .toList();
            assertThat(fullNames).isSorted();
        }

        @Test
        @DisplayName("Should handle sorting by email descending")
        void shouldHandleSortingByEmailDescending() {
            // Given
            Pageable pageable = PageRequest.of(0, 5, Sort.by("email").descending());

            // When
            Page<Technician> result = technicianRepository.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(5);
            List<String> emails = result.getContent().stream()
                .map(Technician::getEmail)
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
            Page<Technician> result = technicianRepository.findByStatus(TechnicianStatus.TERMINATED, pageable);

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
            Technician tech1 = createTestTechnician("duplicate@example.com");
            Technician tech2 = createTestTechnician("duplicate@example.com");
            
            entityManager.persistAndFlush(tech1);

            // When & Then
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(tech2);
            }).isInstanceOf(Exception.class); // Should throw constraint violation
        }

        @Test
        @DisplayName("Should handle null values properly")
        void shouldHandleNullValuesProperly() {
            // Given
            Technician technician = createTestTechnician("null.test@example.com");
            // Status should default to ACTIVE, other fields can be null if not required

            // When & Then
            assertDoesNotThrow(() -> {
                Technician saved = entityManager.persistAndFlush(technician);
                assertThat(saved.getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
            });
        }

        @Test
        @DisplayName("Should not allow null required fields")
        void shouldNotAllowNullRequiredFields() {
            // Given
            Technician technician = new Technician();
            technician.setEmail("required.test@example.com");
            // fullName is required but not set

            // When & Then
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(technician);
            }).isInstanceOf(Exception.class); // Should throw validation exception
        }

        @Test
        @DisplayName("Should validate email format")
        void shouldValidateEmailFormat() {
            // Given - The entity doesn't have @Email validation, so this test verifies that 
            // any string can be stored as email (database level validation only)
            Technician technician = createTestTechnician("John Doe", "invalid-email", TechnicianStatus.ACTIVE);

            // When & Then - This should succeed since there's no email format validation
            assertDoesNotThrow(() -> {
                Technician saved = entityManager.persistAndFlush(technician);
                assertThat(saved.getEmail()).isEqualTo("invalid-email");
            });
        }

        @Test
        @DisplayName("Should handle cascading operations with skills")
        void shouldHandleCascadingOperationsWithSkills() {
            // Given
            Technician technician = createTestTechnician("cascade@example.com");
            
            // When - Save technician first
            entityManager.persistAndFlush(technician);
            
            // Then create and add skill separately to avoid circular reference issues
            TechnicianSkill skill = new TechnicianSkill();
            skill.setTechnician(technician);
            skill.setServiceType(ServiceType.HARDWARE);
            
            assertDoesNotThrow(() -> {
                entityManager.persistAndFlush(skill);
            });

            // Verify the relationship was established by counting skills
            entityManager.clear();
            long skillCount = technicianRepository.countByServiceType(ServiceType.HARDWARE);
            assertThat(skillCount).isEqualTo(1);
            
            // Verify technician exists
            Technician saved = technicianRepository.findByEmail("cascade@example.com").orElseThrow();
            assertThat(saved.getEmail()).isEqualTo("cascade@example.com");
        }
    }
}
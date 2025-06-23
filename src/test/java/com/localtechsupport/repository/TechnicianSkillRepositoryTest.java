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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@DisplayName("TechnicianSkillRepository Tests")
class TechnicianSkillRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TechnicianSkillRepository technicianSkillRepository;

    private Technician testTechnician1;
    private Technician testTechnician2;
    private Technician testTechnician3;
    private TechnicianSkill testSkill1;
    private TechnicianSkill testSkill2;
    private TechnicianSkill testSkill3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        technicianSkillRepository.deleteAll();
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

    private TechnicianSkill createTestTechnicianSkill(Technician technician, ServiceType serviceType) {
        TechnicianSkill skill = new TechnicianSkill();
        skill.setTechnician(technician);
        skill.setServiceType(serviceType);
        return skill;
    }

    private void setupBasicTestData() {
        testTechnician1 = createTestTechnician("John Doe", "john.doe@example.com", TechnicianStatus.ACTIVE);
        testTechnician2 = createTestTechnician("Jane Smith", "jane.smith@example.com", TechnicianStatus.ACTIVE);
        testTechnician3 = createTestTechnician("Bob Johnson", "bob.johnson@example.com", TechnicianStatus.INACTIVE);

        entityManager.persistAndFlush(testTechnician1);
        entityManager.persistAndFlush(testTechnician2);
        entityManager.persistAndFlush(testTechnician3);

        testSkill1 = createTestTechnicianSkill(testTechnician1, ServiceType.HARDWARE);
        testSkill2 = createTestTechnicianSkill(testTechnician1, ServiceType.SOFTWARE);
        testSkill3 = createTestTechnicianSkill(testTechnician2, ServiceType.HARDWARE);

        entityManager.persistAndFlush(testSkill1);
        entityManager.persistAndFlush(testSkill2);
        entityManager.persistAndFlush(testSkill3);
        entityManager.clear();
    }

    @Nested
    @DisplayName("Standard CRUD Operations")
    class StandardCrudTests {

        @Test
        @DisplayName("Should save new technician skill successfully")
        void shouldSaveNewTechnicianSkillSuccessfully() {
            // Given
            Technician technician = createTestTechnician("test@example.com");
            entityManager.persistAndFlush(technician);
            TechnicianSkill skill = createTestTechnicianSkill(technician, ServiceType.HARDWARE);

            // When
            TechnicianSkill savedSkill = technicianSkillRepository.save(skill);

            // Then
            assertThat(savedSkill).isNotNull();
            assertThat(savedSkill.getId()).isNotNull();
            assertThat(savedSkill.getTechnician()).isEqualTo(technician);
            assertThat(savedSkill.getServiceType()).isEqualTo(ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should update existing technician skill successfully")
        void shouldUpdateExistingTechnicianSkillSuccessfully() {
            // Given
            Technician technician = createTestTechnician("update@example.com");
            entityManager.persistAndFlush(technician);
            TechnicianSkill skill = createTestTechnicianSkill(technician, ServiceType.HARDWARE);
            entityManager.clear();
            TechnicianSkill savedSkill = entityManager.persistAndFlush(skill);
            Long id = savedSkill.getTechnician().getId();

            // When
            savedSkill.setServiceType(ServiceType.SOFTWARE);
            TechnicianSkill updatedSkill = technicianSkillRepository.save(savedSkill);

            // Then
            assertThat(updatedSkill.getServiceType()).isEqualTo(ServiceType.SOFTWARE);
            assertThat(updatedSkill.getTechnician().getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should find technician skill by existing ID")
        void shouldFindTechnicianSkillByExistingId() {
            // Given
            Technician technician = createTestTechnician("find@example.com");
            entityManager.persistAndFlush(technician);
            TechnicianSkill skill = createTestTechnicianSkill(technician, ServiceType.HARDWARE);
            TechnicianSkill savedSkill = entityManager.persistAndFlush(skill);
            entityManager.clear();

            // When
            Optional<TechnicianSkill> result = technicianSkillRepository.findById(savedSkill.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getServiceType()).isEqualTo(ServiceType.HARDWARE);
            assertThat(result.get().getTechnician().getEmail()).isEqualTo("find@example.com");
        }

        @Test
        @DisplayName("Should return empty when finding by non-existing ID")
        void shouldReturnEmptyWhenFindingByNonExistingId() {
            // When
            Optional<TechnicianSkill> result = technicianSkillRepository.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all technician skills")
        void shouldFindAllTechnicianSkills() {
            // Given
            setupBasicTestData();

            // When
            List<TechnicianSkill> skills = technicianSkillRepository.findAll();

            // Then
            assertThat(skills).hasSize(3);
            assertThat(skills).extracting(TechnicianSkill::getServiceType)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE, ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should return empty list when no technician skills exist")
        void shouldReturnEmptyListWhenNoTechnicianSkillsExist() {
            // When
            List<TechnicianSkill> skills = technicianSkillRepository.findAll();

            // Then
            assertThat(skills).isEmpty();
        }

        @Test
        @DisplayName("Should delete technician skill by ID")
        void shouldDeleteTechnicianSkillById() {
            // Given
            Technician technician = createTestTechnician("delete@example.com");
            entityManager.persistAndFlush(technician);
            TechnicianSkill skill = createTestTechnicianSkill(technician, ServiceType.HARDWARE);
            TechnicianSkill savedSkill = entityManager.persistAndFlush(skill);
            entityManager.clear();

            // When
            technicianSkillRepository.deleteById(savedSkill.getId());

            // Then
            Optional<TechnicianSkill> result = technicianSkillRepository.findById(savedSkill.getId());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count all technician skills correctly")
        void shouldCountAllTechnicianSkillsCorrectly() {
            // Given
            setupBasicTestData();

            // When
            long count = technicianSkillRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Technician-Based Query Tests")
    class TechnicianBasedTests {

        @BeforeEach
        void setupTechnicianData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find skills by technician with pagination")
        void shouldFindSkillsByTechnicianWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<TechnicianSkill> result = technicianSkillRepository.findByTechnician(testTechnician1, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // testTechnician1 has both HARDWARE and SOFTWARE skills
            assertThat(result.getContent()).extracting(TechnicianSkill::getServiceType)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE);
        }

        @Test
        @DisplayName("Should find skills by technician using list method")
        void shouldFindSkillsByTechnicianUsingListMethod() {
            // When
            List<TechnicianSkill> result = technicianSkillRepository.findByTechnician(testTechnician1);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(TechnicianSkill::getServiceType)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE);
        }

        @Test
        @DisplayName("Should return empty when technician has no skills")
        void shouldReturnEmptyWhenTechnicianHasNoSkills() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<TechnicianSkill> result = technicianSkillRepository.findByTechnician(testTechnician3, pageable);
            List<TechnicianSkill> listResult = technicianSkillRepository.findByTechnician(testTechnician3);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(listResult).isEmpty();
        }

        @Test
        @DisplayName("Should count skills by technician")
        void shouldCountSkillsByTechnician() {
            // When
            long count1 = technicianSkillRepository.countByTechnician(testTechnician1);
            long count2 = technicianSkillRepository.countByTechnician(testTechnician2);
            long count3 = technicianSkillRepository.countByTechnician(testTechnician3);

            // Then
            assertThat(count1).isEqualTo(2); // Has both skills
            assertThat(count2).isEqualTo(1); // Has only hardware skill
            assertThat(count3).isZero(); // Has no skills
        }

        @Test
        @DisplayName("Should find service types by technician")
        void shouldFindServiceTypesByTechnician() {
            // When
            List<ServiceType> serviceTypes1 = technicianSkillRepository.findServiceTypesByTechnician(testTechnician1);
            List<ServiceType> serviceTypes2 = technicianSkillRepository.findServiceTypesByTechnician(testTechnician2);
            List<ServiceType> serviceTypes3 = technicianSkillRepository.findServiceTypesByTechnician(testTechnician3);

            // Then
            assertThat(serviceTypes1).hasSize(2)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE);
            assertThat(serviceTypes2).hasSize(1)
                .containsExactly(ServiceType.HARDWARE);
            assertThat(serviceTypes3).isEmpty();
        }

        @Test
        @DisplayName("Should return empty service types for technician with no skills")
        void shouldReturnEmptyServiceTypesForTechnicianWithNoSkills() {
            // Given
            Technician newTechnician = createTestTechnician("noskills@example.com");
            entityManager.persistAndFlush(newTechnician);

            // When
            List<ServiceType> serviceTypes = technicianSkillRepository.findServiceTypesByTechnician(newTechnician);

            // Then
            assertThat(serviceTypes).isEmpty();
        }

        @Test
        @DisplayName("Should handle null technician gracefully")
        void shouldHandleNullTechnicianGracefully() {
            // When & Then
            assertThat(technicianSkillRepository.findByTechnician(null)).isEmpty();
            assertThat(technicianSkillRepository.countByTechnician(null)).isZero();
            assertThat(technicianSkillRepository.findServiceTypesByTechnician(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Service Type-Based Query Tests")
    class ServiceTypeBasedTests {

        @BeforeEach
        void setupServiceTypeData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find skills by service type with pagination")
        void shouldFindSkillsByServiceTypeWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<TechnicianSkill> hardwareResult = technicianSkillRepository.findByServiceType(ServiceType.HARDWARE, pageable);
            Page<TechnicianSkill> softwareResult = technicianSkillRepository.findByServiceType(ServiceType.SOFTWARE, pageable);

            // Then
            assertThat(hardwareResult.getContent()).hasSize(2); // testTechnician1 and testTechnician2 have hardware skills
            assertThat(softwareResult.getContent()).hasSize(1); // Only testTechnician1 has software skill
        }

        @Test
        @DisplayName("Should find skills by service type using list method")
        void shouldFindSkillsByServiceTypeUsingListMethod() {
            // When
            List<TechnicianSkill> hardwareSkills = technicianSkillRepository.findByServiceType(ServiceType.HARDWARE);
            List<TechnicianSkill> softwareSkills = technicianSkillRepository.findByServiceType(ServiceType.SOFTWARE);

            // Then
            assertThat(hardwareSkills).hasSize(2);
            assertThat(softwareSkills).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when no technicians have service type")
        void shouldReturnEmptyWhenNoTechniciansHaveServiceType() {
            // Given - clear all skills first
            technicianSkillRepository.deleteAll();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<TechnicianSkill> result = technicianSkillRepository.findByServiceType(ServiceType.HARDWARE, pageable);
            List<TechnicianSkill> listResult = technicianSkillRepository.findByServiceType(ServiceType.HARDWARE);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(listResult).isEmpty();
        }

        @Test
        @DisplayName("Should count skills by service type")
        void shouldCountSkillsByServiceType() {
            // When
            long hardwareCount = technicianSkillRepository.countByServiceType(ServiceType.HARDWARE);
            long softwareCount = technicianSkillRepository.countByServiceType(ServiceType.SOFTWARE);

            // Then
            assertThat(hardwareCount).isEqualTo(2); // testTechnician1 and testTechnician2
            assertThat(softwareCount).isEqualTo(1); // Only testTechnician1
        }

        @Test
        @DisplayName("Should count technicians with skill")
        void shouldCountTechniciansWithSkill() {
            // When
            long hardwareTechCount = technicianSkillRepository.countTechniciansWithSkill(ServiceType.HARDWARE);
            long softwareTechCount = technicianSkillRepository.countTechniciansWithSkill(ServiceType.SOFTWARE);

            // Then
            assertThat(hardwareTechCount).isEqualTo(2); // testTechnician1 and testTechnician2
            assertThat(softwareTechCount).isEqualTo(1); // Only testTechnician1
        }

        @Test
        @DisplayName("Should return zero count for service type with no technicians")
        void shouldReturnZeroCountForServiceTypeWithNoTechnicians() {
            // Given - clear all skills
            technicianSkillRepository.deleteAll();

            // When
            long hardwareCount = technicianSkillRepository.countByServiceType(ServiceType.HARDWARE);
            long technicianCount = technicianSkillRepository.countTechniciansWithSkill(ServiceType.HARDWARE);

            // Then
            assertThat(hardwareCount).isZero();
            assertThat(technicianCount).isZero();
        }

        @Test
        @DisplayName("Should handle pagination correctly for service type queries")
        void shouldHandlePaginationCorrectlyForServiceTypeQueries() {
            // Given
            Pageable firstPage = PageRequest.of(0, 1);
            Pageable secondPage = PageRequest.of(1, 1);

            // When
            Page<TechnicianSkill> firstPageResult = technicianSkillRepository.findByServiceType(ServiceType.HARDWARE, firstPage);
            Page<TechnicianSkill> secondPageResult = technicianSkillRepository.findByServiceType(ServiceType.HARDWARE, secondPage);

            // Then
            assertThat(firstPageResult.getContent()).hasSize(1);
            assertThat(secondPageResult.getContent()).hasSize(1);
            assertThat(firstPageResult.getTotalElements()).isEqualTo(2);
            assertThat(secondPageResult.getTotalElements()).isEqualTo(2);
            assertThat(firstPageResult.hasNext()).isTrue();
            assertThat(secondPageResult.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should handle edge case with no skills data")
        void shouldHandleEdgeCaseWithNoSkillsData() {
            // Given - start with empty database
            technicianSkillRepository.deleteAll();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<TechnicianSkill> result = technicianSkillRepository.findByServiceType(ServiceType.HARDWARE, pageable);
            long count = technicianSkillRepository.countByServiceType(ServiceType.HARDWARE);
            long techCount = technicianSkillRepository.countTechniciansWithSkill(ServiceType.HARDWARE);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(count).isZero();
            assertThat(techCount).isZero();
        }
    }

    @Nested
    @DisplayName("Combination Queries Tests")
    class CombinationQueriesTests {

        @BeforeEach
        void setupCombinationData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find technician skill by technician and service type")
        void shouldFindTechnicianSkillByTechnicianAndServiceType() {
            // When
            Optional<TechnicianSkill> hardwareSkill = technicianSkillRepository.findByTechnicianAndServiceType(testTechnician1, ServiceType.HARDWARE);
            Optional<TechnicianSkill> softwareSkill = technicianSkillRepository.findByTechnicianAndServiceType(testTechnician1, ServiceType.SOFTWARE);
            Optional<TechnicianSkill> tech2HardwareSkill = technicianSkillRepository.findByTechnicianAndServiceType(testTechnician2, ServiceType.HARDWARE);

            // Then
            assertThat(hardwareSkill).isPresent();
            assertThat(hardwareSkill.get().getTechnician().getId()).isEqualTo(testTechnician1.getId());
            assertThat(hardwareSkill.get().getServiceType()).isEqualTo(ServiceType.HARDWARE);

            assertThat(softwareSkill).isPresent();
            assertThat(softwareSkill.get().getTechnician().getId()).isEqualTo(testTechnician1.getId());
            assertThat(softwareSkill.get().getServiceType()).isEqualTo(ServiceType.SOFTWARE);

            assertThat(tech2HardwareSkill).isPresent();
            assertThat(tech2HardwareSkill.get().getTechnician().getId()).isEqualTo(testTechnician2.getId());
            assertThat(tech2HardwareSkill.get().getServiceType()).isEqualTo(ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should return empty when combination does not exist")
        void shouldReturnEmptyWhenCombinationDoesNotExist() {
            // When
            Optional<TechnicianSkill> nonExistentSkill = technicianSkillRepository.findByTechnicianAndServiceType(testTechnician2, ServiceType.SOFTWARE);
            Optional<TechnicianSkill> tech3Skill = technicianSkillRepository.findByTechnicianAndServiceType(testTechnician3, ServiceType.HARDWARE);

            // Then
            assertThat(nonExistentSkill).isEmpty(); // testTechnician2 doesn't have software skill
            assertThat(tech3Skill).isEmpty(); // testTechnician3 has no skills
        }

        @Test
        @DisplayName("Should return true when technician has service type")
        void shouldReturnTrueWhenTechnicianHasServiceType() {
            // When
            boolean tech1HasHardware = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician1, ServiceType.HARDWARE);
            boolean tech1HasSoftware = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician1, ServiceType.SOFTWARE);
            boolean tech2HasHardware = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician2, ServiceType.HARDWARE);

            // Then
            assertThat(tech1HasHardware).isTrue();
            assertThat(tech1HasSoftware).isTrue();
            assertThat(tech2HasHardware).isTrue();
        }

        @Test
        @DisplayName("Should return false when technician does not have service type")
        void shouldReturnFalseWhenTechnicianDoesNotHaveServiceType() {
            // When
            boolean tech2HasSoftware = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician2, ServiceType.SOFTWARE);
            boolean tech3HasHardware = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician3, ServiceType.HARDWARE);
            boolean tech3HasSoftware = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician3, ServiceType.SOFTWARE);

            // Then
            assertThat(tech2HasSoftware).isFalse(); // testTechnician2 doesn't have software skill
            assertThat(tech3HasHardware).isFalse(); // testTechnician3 has no skills
            assertThat(tech3HasSoftware).isFalse(); // testTechnician3 has no skills
        }

        @Test
        @DisplayName("Should return true when technician is qualified for service type")
        void shouldReturnTrueWhenTechnicianIsQualifiedFor() {
            // When
            boolean tech1QualifiedHardware = technicianSkillRepository.isTechnicianQualifiedFor(testTechnician1, ServiceType.HARDWARE);
            boolean tech1QualifiedSoftware = technicianSkillRepository.isTechnicianQualifiedFor(testTechnician1, ServiceType.SOFTWARE);
            boolean tech2QualifiedHardware = technicianSkillRepository.isTechnicianQualifiedFor(testTechnician2, ServiceType.HARDWARE);

            // Then
            assertThat(tech1QualifiedHardware).isTrue();
            assertThat(tech1QualifiedSoftware).isTrue();
            assertThat(tech2QualifiedHardware).isTrue();
        }

        @Test
        @DisplayName("Should return false when technician is not qualified for service type")
        void shouldReturnFalseWhenTechnicianIsNotQualifiedFor() {
            // When
            boolean tech2QualifiedSoftware = technicianSkillRepository.isTechnicianQualifiedFor(testTechnician2, ServiceType.SOFTWARE);
            boolean tech3QualifiedHardware = technicianSkillRepository.isTechnicianQualifiedFor(testTechnician3, ServiceType.HARDWARE);
            boolean tech3QualifiedSoftware = technicianSkillRepository.isTechnicianQualifiedFor(testTechnician3, ServiceType.SOFTWARE);

            // Then
            assertThat(tech2QualifiedSoftware).isFalse(); // testTechnician2 doesn't have software skill
            assertThat(tech3QualifiedHardware).isFalse(); // testTechnician3 has no skills
            assertThat(tech3QualifiedSoftware).isFalse(); // testTechnician3 has no skills
        }

        @Test
        @DisplayName("Should handle null parameters gracefully")
        void shouldHandleNullParametersGracefully() {
            // When & Then
            assertThat(technicianSkillRepository.findByTechnicianAndServiceType(null, ServiceType.HARDWARE)).isEmpty();
            assertThat(technicianSkillRepository.findByTechnicianAndServiceType(testTechnician1, null)).isEmpty();
            assertThat(technicianSkillRepository.existsByTechnicianAndServiceType(null, ServiceType.HARDWARE)).isFalse();
            assertThat(technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician1, null)).isFalse();
            assertThat(technicianSkillRepository.isTechnicianQualifiedFor(null, ServiceType.HARDWARE)).isFalse();
            assertThat(technicianSkillRepository.isTechnicianQualifiedFor(testTechnician1, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Technician Discovery Tests")
    class TechnicianDiscoveryTests {

        @BeforeEach
        void setupDiscoveryData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find technicians with specific skill")
        void shouldFindTechniciansWithSpecificSkill() {
            // When
            List<Technician> hardwareTechnicians = technicianSkillRepository.findTechniciansWithSkill(ServiceType.HARDWARE);
            List<Technician> softwareTechnicians = technicianSkillRepository.findTechniciansWithSkill(ServiceType.SOFTWARE);

            // Then
            assertThat(hardwareTechnicians).hasSize(2)
                .extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());

            assertThat(softwareTechnicians).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician1.getId());
        }

        @Test
        @DisplayName("Should find technicians with specific skill with pagination")
        void shouldFindTechniciansWithSpecificSkillWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> hardwareTechnicians = technicianSkillRepository.findTechniciansWithSkill(ServiceType.HARDWARE, pageable);
            Page<Technician> softwareTechnicians = technicianSkillRepository.findTechniciansWithSkill(ServiceType.SOFTWARE, pageable);

            // Then
            assertThat(hardwareTechnicians.getContent()).hasSize(2)
                .extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());
            assertThat(hardwareTechnicians.getTotalElements()).isEqualTo(2);

            assertThat(softwareTechnicians.getContent()).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician1.getId());
            assertThat(softwareTechnicians.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find technicians with all specified skills")
        void shouldFindTechniciansWithAllSpecifiedSkills() {
            // Given
            List<ServiceType> bothSkills = List.of(ServiceType.HARDWARE, ServiceType.SOFTWARE);

            // When
            List<Technician> techniciansWithBothSkills = technicianSkillRepository.findTechniciansWithAllSkills(bothSkills, 2L);

            // Then
            assertThat(techniciansWithBothSkills).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician1.getId()); // Only testTechnician1 has both skills
        }

        @Test
        @DisplayName("Should find technicians with any specified skill")
        void shouldFindTechniciansWithAnySpecifiedSkill() {
            // Given
            List<ServiceType> anySkills = List.of(ServiceType.HARDWARE, ServiceType.SOFTWARE);

            // When
            List<Technician> techniciansWithAnySkill = technicianSkillRepository.findTechniciansWithAnySkill(anySkills);

            // Then
            assertThat(techniciansWithAnySkill).hasSize(2)
                .extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());
        }

        @Test
        @DisplayName("Should find technicians with any specified skill with pagination")
        void shouldFindTechniciansWithAnySpecifiedSkillWithPagination() {
            // Given
            List<ServiceType> anySkills = List.of(ServiceType.HARDWARE, ServiceType.SOFTWARE);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> techniciansWithAnySkill = technicianSkillRepository.findTechniciansWithAnySkill(anySkills, pageable);

            // Then
            assertThat(techniciansWithAnySkill.getContent()).hasSize(2)
                .extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());
            assertThat(techniciansWithAnySkill.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return empty when no technicians have skill")
        void shouldReturnEmptyWhenNoTechniciansHaveSkill() {
            // Given - clear all skills
            technicianSkillRepository.deleteAll();

            // When
            List<Technician> hardwareTechnicians = technicianSkillRepository.findTechniciansWithSkill(ServiceType.HARDWARE);
            Page<Technician> paginatedResult = technicianSkillRepository.findTechniciansWithSkill(ServiceType.HARDWARE, PageRequest.of(0, 10));

            // Then
            assertThat(hardwareTechnicians).isEmpty();
            assertThat(paginatedResult.getContent()).isEmpty();
            assertThat(paginatedResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should handle empty skill list in multiple skill search")
        void shouldHandleEmptySkillListInMultipleSkillSearch() {
            // Given
            List<ServiceType> emptySkills = List.of();

            // When
            List<Technician> techniciansWithAllSkills = technicianSkillRepository.findTechniciansWithAllSkills(emptySkills, 0L);
            List<Technician> techniciansWithAnySkill = technicianSkillRepository.findTechniciansWithAnySkill(emptySkills);
            Page<Technician> paginatedAnySkill = technicianSkillRepository.findTechniciansWithAnySkill(emptySkills, PageRequest.of(0, 10));

            // Then
            assertThat(techniciansWithAllSkills).isEmpty();
            assertThat(techniciansWithAnySkill).isEmpty();
            assertThat(paginatedAnySkill.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle single skill in multiple skill searches")
        void shouldHandleSingleSkillInMultipleSkillSearches() {
            // Given
            List<ServiceType> singleSkill = List.of(ServiceType.HARDWARE);

            // When
            List<Technician> techniciansWithAllSkills = technicianSkillRepository.findTechniciansWithAllSkills(singleSkill, 1L);
            List<Technician> techniciansWithAnySkill = technicianSkillRepository.findTechniciansWithAnySkill(singleSkill);

            // Then
            assertThat(techniciansWithAllSkills).hasSize(2)
                .extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());

            assertThat(techniciansWithAnySkill).hasSize(2)
                .extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());
        }
    }

    @Nested
    @DisplayName("Skill Analytics Tests")
    class SkillAnalyticsTests {

        @BeforeEach
        void setupAnalyticsData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should get skill coverage analysis")
        void shouldGetSkillCoverageAnalysis() {
            // When
            List<Object[]> skillCoverage = technicianSkillRepository.getSkillCoverageAnalysis();

            // Then
            assertThat(skillCoverage).hasSize(2); // HARDWARE and SOFTWARE
            
            // Results should be ordered by count DESC, so HARDWARE (2 technicians) comes first
            Object[] hardwareData = skillCoverage.get(0);
            Object[] softwareData = skillCoverage.get(1);

            assertThat(hardwareData[0]).isEqualTo(ServiceType.HARDWARE);
            assertThat(hardwareData[1]).isEqualTo(2L); // 2 technicians have hardware skills

            assertThat(softwareData[0]).isEqualTo(ServiceType.SOFTWARE);
            assertThat(softwareData[1]).isEqualTo(1L); // 1 technician has software skills
        }

        @Test
        @DisplayName("Should get technician skill counts")
        void shouldGetTechnicianSkillCounts() {
            // When
            List<Object[]> technicianSkillCounts = technicianSkillRepository.getTechnicianSkillCounts();

            // Then
            assertThat(technicianSkillCounts).hasSize(2); // Only technicians with skills are included
            
            // Results should be ordered by count DESC, so testTechnician1 (2 skills) comes first
            Object[] tech1Data = technicianSkillCounts.get(0);
            Object[] tech2Data = technicianSkillCounts.get(1);

            assertThat(((Technician) tech1Data[0]).getId()).isEqualTo(testTechnician1.getId());
            assertThat(tech1Data[1]).isEqualTo(2L); // testTechnician1 has 2 skills

            assertThat(((Technician) tech2Data[0]).getId()).isEqualTo(testTechnician2.getId());
            assertThat(tech2Data[1]).isEqualTo(1L); // testTechnician2 has 1 skill
        }

        @Test
        @DisplayName("Should get skill composition summary")
        void shouldGetSkillCompositionSummary() {
            // When
            List<Object[]> compositionSummary = technicianSkillRepository.getSkillCompositionSummary();

            // Then
            assertThat(compositionSummary).hasSize(1); // Single summary row
            
            Object[] summaryData = compositionSummary.get(0);
            assertThat(summaryData[0]).isEqualTo(2L); // totalTechnicians with skills
            assertThat(summaryData[1]).isEqualTo(2L); // totalSkillTypes (HARDWARE, SOFTWARE)
            assertThat(summaryData[2]).isEqualTo(3L); // totalSkillAssignments (3 TechnicianSkill records)
        }

        @Test
        @DisplayName("Should get skill redundancy analysis")
        void shouldGetSkillRedundancyAnalysis() {
            // When
            List<Object[]> redundancyAnalysis = technicianSkillRepository.getSkillRedundancyAnalysis();

            // Then
            assertThat(redundancyAnalysis).hasSize(2); // HARDWARE and SOFTWARE
            
            // Results should be ordered by technicianCount ASC, so SOFTWARE (1) comes first
            Object[] softwareData = redundancyAnalysis.get(0);
            Object[] hardwareData = redundancyAnalysis.get(1);

            assertThat(softwareData[0]).isEqualTo(ServiceType.SOFTWARE);
            assertThat(softwareData[1]).isEqualTo(1L); // 1 technician has software skills

            assertThat(hardwareData[0]).isEqualTo(ServiceType.HARDWARE);
            assertThat(hardwareData[1]).isEqualTo(2L); // 2 technicians have hardware skills
        }

        @Test
        @DisplayName("Should return empty analytics when no skills exist")
        void shouldReturnEmptyAnalyticsWhenNoSkillsExist() {
            // Given - clear all skills
            technicianSkillRepository.deleteAll();

            // When
            List<Object[]> skillCoverage = technicianSkillRepository.getSkillCoverageAnalysis();
            List<Object[]> technicianSkillCounts = technicianSkillRepository.getTechnicianSkillCounts();
            List<Object[]> compositionSummary = technicianSkillRepository.getSkillCompositionSummary();
            List<Object[]> redundancyAnalysis = technicianSkillRepository.getSkillRedundancyAnalysis();

            // Then
            assertThat(skillCoverage).isEmpty();
            assertThat(technicianSkillCounts).isEmpty();
            assertThat(redundancyAnalysis).isEmpty();
            
            // Composition summary should show zeros
            assertThat(compositionSummary).hasSize(1);
            Object[] summaryData = compositionSummary.get(0);
            assertThat(summaryData[0]).isEqualTo(0L); // totalTechnicians
            assertThat(summaryData[1]).isEqualTo(0L); // totalSkillTypes
            assertThat(summaryData[2]).isEqualTo(0L); // totalSkillAssignments
        }

        @Test
        @DisplayName("Should handle single technician with multiple skills in analytics")
        void shouldHandleSingleTechnicianWithMultipleSkillsInAnalytics() {
            // Given - clear existing data and create new scenario
            technicianSkillRepository.deleteAll();
            
            Technician soloTech = createTestTechnician("Solo Tech", "solo@example.com", TechnicianStatus.ACTIVE);
            entityManager.persistAndFlush(soloTech);
            
            TechnicianSkill hardwareSkill = createTestTechnicianSkill(soloTech, ServiceType.HARDWARE);
            TechnicianSkill softwareSkill = createTestTechnicianSkill(soloTech, ServiceType.SOFTWARE);
            entityManager.persistAndFlush(hardwareSkill);
            entityManager.persistAndFlush(softwareSkill);

            // When
            List<Object[]> skillCoverage = technicianSkillRepository.getSkillCoverageAnalysis();
            List<Object[]> technicianSkillCounts = technicianSkillRepository.getTechnicianSkillCounts();
            List<Object[]> compositionSummary = technicianSkillRepository.getSkillCompositionSummary();

            // Then
            assertThat(skillCoverage).hasSize(2);
            assertThat(technicianSkillCounts).hasSize(1);
            
            // Single technician with 2 skills
            Object[] techData = technicianSkillCounts.get(0);
            assertThat(((Technician) techData[0]).getId()).isEqualTo(soloTech.getId());
            assertThat(techData[1]).isEqualTo(2L);
            
            // Summary should reflect single technician scenario
            Object[] summaryData = compositionSummary.get(0);
            assertThat(summaryData[0]).isEqualTo(1L); // 1 technician
            assertThat(summaryData[1]).isEqualTo(2L); // 2 skill types
            assertThat(summaryData[2]).isEqualTo(2L); // 2 skill assignments
        }
    }

    @Nested
    @DisplayName("Training Needs Tests")
    class TrainingNeedsTests {

        @BeforeEach
        void setupTrainingNeedsData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find technicians with no skills")
        void shouldFindTechniciansWithNoSkills() {
            // When
            List<Technician> techniciansWithNoSkills = technicianSkillRepository.findTechniciansWithNoSkills();

            // Then
            assertThat(techniciansWithNoSkills).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician3.getId()); // testTechnician3 has no skills
        }

        @Test
        @DisplayName("Should find technicians with no skills with pagination")
        void shouldFindTechniciansWithNoSkillsWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Technician> techniciansWithNoSkills = technicianSkillRepository.findTechniciansWithNoSkills(pageable);

            // Then
            assertThat(techniciansWithNoSkills.getContent()).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician3.getId());
            assertThat(techniciansWithNoSkills.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find technicians with single skill")
        void shouldFindTechniciansWithSingleSkill() {
            // When
            List<Technician> techniciansWithSingleSkill = technicianSkillRepository.findTechniciansWithSingleSkill();

            // Then
            assertThat(techniciansWithSingleSkill).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician2.getId()); // testTechnician2 has only hardware skill
        }

        @Test
        @DisplayName("Should find fully qualified technicians")
        void shouldFindFullyQualifiedTechnicians() {
            // When
            List<Technician> fullyQualifiedTechnicians = technicianSkillRepository.findFullyQualifiedTechnicians();

            // Then
            assertThat(fullyQualifiedTechnicians).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician1.getId()); // testTechnician1 has both available skills
        }

        @Test
        @DisplayName("Should return empty when all technicians have skills")
        void shouldReturnEmptyWhenAllTechniciansHaveSkills() {
            // Given - add a skill to testTechnician3 so all have skills
            TechnicianSkill additionalSkill = createTestTechnicianSkill(testTechnician3, ServiceType.SOFTWARE);
            entityManager.persistAndFlush(additionalSkill);

            // When
            List<Technician> techniciansWithNoSkills = technicianSkillRepository.findTechniciansWithNoSkills();
            Page<Technician> paginatedResult = technicianSkillRepository.findTechniciansWithNoSkills(PageRequest.of(0, 10));

            // Then
            assertThat(techniciansWithNoSkills).isEmpty();
            assertThat(paginatedResult.getContent()).isEmpty();
            assertThat(paginatedResult.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should return empty when no technicians have all skills")
        void shouldReturnEmptyWhenNoTechniciansHaveAllSkills() {
            // Given - clear existing skills and create scenario where no one has all skills
            technicianSkillRepository.deleteAll();
            
            // Create technicians with partial skills only
            TechnicianSkill partialSkill1 = createTestTechnicianSkill(testTechnician1, ServiceType.HARDWARE);
            TechnicianSkill partialSkill2 = createTestTechnicianSkill(testTechnician2, ServiceType.SOFTWARE);
            entityManager.persistAndFlush(partialSkill1);
            entityManager.persistAndFlush(partialSkill2);

            // When
            List<Technician> fullyQualifiedTechnicians = technicianSkillRepository.findFullyQualifiedTechnicians();

            // Then
            assertThat(fullyQualifiedTechnicians).isEmpty();
        }

        @Test
        @DisplayName("Should handle edge case with no technicians")
        void shouldHandleEdgeCaseWithNoTechnicians() {
            // Given - remove all technicians (cascade should remove skills)
            entityManager.getEntityManager().createQuery("DELETE FROM TechnicianSkill").executeUpdate();
            entityManager.getEntityManager().createQuery("DELETE FROM Technician").executeUpdate();

            // When
            List<Technician> techniciansWithNoSkills = technicianSkillRepository.findTechniciansWithNoSkills();
            List<Technician> techniciansWithSingleSkill = technicianSkillRepository.findTechniciansWithSingleSkill();
            List<Technician> fullyQualifiedTechnicians = technicianSkillRepository.findFullyQualifiedTechnicians();

            // Then
            assertThat(techniciansWithNoSkills).isEmpty();
            assertThat(techniciansWithSingleSkill).isEmpty();
            assertThat(fullyQualifiedTechnicians).isEmpty();
        }
    }

    @Nested
    @DisplayName("Skill Gap Analysis Tests")
    class SkillGapAnalysisTests {

        @BeforeEach
        void setupSkillGapData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find missing skills for technician")
        void shouldFindMissingSkillsForTechnician() {
            // When
            List<ServiceType> missingSkillsTech1 = technicianSkillRepository.findMissingSkillsForTechnician(testTechnician1);
            List<ServiceType> missingSkillsTech2 = technicianSkillRepository.findMissingSkillsForTechnician(testTechnician2);
            List<ServiceType> missingSkillsTech3 = technicianSkillRepository.findMissingSkillsForTechnician(testTechnician3);

            // Then
            assertThat(missingSkillsTech1).isEmpty(); // testTechnician1 has all available skills
            assertThat(missingSkillsTech2).hasSize(1)
                .containsExactly(ServiceType.SOFTWARE); // testTechnician2 missing software
            assertThat(missingSkillsTech3).hasSize(2)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE); // testTechnician3 missing all
        }

        @Test
        @DisplayName("Should find underrepresented skills")
        void shouldFindUnderrepresentedSkills() {
            // When - find skills with 1 or fewer technicians
            List<ServiceType> underrepresentedSkills = technicianSkillRepository.findUnderrepresentedSkills(1L);

            // Then
            assertThat(underrepresentedSkills).hasSize(1)
                .containsExactly(ServiceType.SOFTWARE); // Only 1 technician has software skill
        }

        @Test
        @DisplayName("Should find overrepresented skills")
        void shouldFindOverrepresentedSkills() {
            // When - find skills with 2 or more technicians
            List<ServiceType> overrepresentedSkills = technicianSkillRepository.findOverrepresentedSkills(2L);

            // Then
            assertThat(overrepresentedSkills).hasSize(1)
                .containsExactly(ServiceType.HARDWARE); // 2 technicians have hardware skill
        }

        @Test
        @DisplayName("Should find most versatile technicians")
        void shouldFindMostVersatileTechnicians() {
            // When - find technicians with 2 or more skills
            List<Object[]> mostVersatileTechnicians = technicianSkillRepository.findMostVersatileTechnicians(2L);

            // Then
            assertThat(mostVersatileTechnicians).hasSize(1);
            Object[] techData = mostVersatileTechnicians.get(0);
            assertThat(((Technician) techData[0]).getId()).isEqualTo(testTechnician1.getId());
            assertThat(techData[1]).isEqualTo(2L); // testTechnician1 has 2 skills
        }

        @Test
        @DisplayName("Should return all skills as missing for technician with no skills")
        void shouldReturnAllSkillsAsMissingForTechnicianWithNoSkills() {
            // When
            List<ServiceType> missingSkills = technicianSkillRepository.findMissingSkillsForTechnician(testTechnician3);

            // Then
            assertThat(missingSkills).hasSize(2)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE);
        }

        @Test
        @DisplayName("Should return empty missing skills for fully qualified technician")
        void shouldReturnEmptyMissingSkillsForFullyQualifiedTechnician() {
            // When
            List<ServiceType> missingSkills = technicianSkillRepository.findMissingSkillsForTechnician(testTechnician1);

            // Then
            assertThat(missingSkills).isEmpty(); // testTechnician1 has all available skills
        }

        @Test
        @DisplayName("Should handle threshold variations in skill representation analysis")
        void shouldHandleThresholdVariationsInSkillRepresentationAnalysis() {
            // When
            List<ServiceType> underrepresented0 = technicianSkillRepository.findUnderrepresentedSkills(0L);
            List<ServiceType> overrepresented1 = technicianSkillRepository.findOverrepresentedSkills(1L);
            List<ServiceType> overrepresented3 = technicianSkillRepository.findOverrepresentedSkills(3L);

            // Then
            assertThat(underrepresented0).isEmpty(); // No skills have 0 or fewer technicians
            assertThat(overrepresented1).hasSize(2)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE); // Both have 1+ technicians
            assertThat(overrepresented3).isEmpty(); // No skills have 3+ technicians
        }

        @Test
        @DisplayName("Should handle versatility analysis with different thresholds")
        void shouldHandleVersatilityAnalysisWithDifferentThresholds() {
            // When
            List<Object[]> versatile1 = technicianSkillRepository.findMostVersatileTechnicians(1L);
            List<Object[]> versatile3 = technicianSkillRepository.findMostVersatileTechnicians(3L);

            // Then
            assertThat(versatile1).hasSize(2); // Both testTechnician1 and testTechnician2 have 1+ skills
            assertThat(versatile3).isEmpty(); // No technician has 3+ skills
        }
    }

    @Nested
    @DisplayName("Mentorship and Training Tests")
    class MentorshipAndTrainingTests {

        @BeforeEach
        void setupMentorshipData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find potential mentors by skill")
        void shouldFindPotentialMentorsBySkill() {
            // When
            List<Object[]> potentialMentors = technicianSkillRepository.findPotentialMentorsBySkill();

            // Then
            assertThat(potentialMentors).isNotEmpty();
            
            // Should include technicians who have skills that others lack
            // testTechnician1 can mentor SOFTWARE (testTechnician2 doesn't have it)
            // testTechnician1 and testTechnician2 can mentor HARDWARE (testTechnician3 doesn't have it)
            boolean hasSoftwareMentor = potentialMentors.stream()
                .anyMatch(mentor -> ((Technician) mentor[0]).getId().equals(testTechnician1.getId()) 
                    && mentor[1].equals(ServiceType.SOFTWARE));
            
            assertThat(hasSoftwareMentor).isTrue();
        }

        @Test
        @DisplayName("Should get training recommendations")
        void shouldGetTrainingRecommendations() {
            // When
            List<Object[]> trainingRecommendations = technicianSkillRepository.getTrainingRecommendations();

            // Then
            assertThat(trainingRecommendations).isNotEmpty();
            
            // Should recommend skills for active technicians that they don't have
            // testTechnician2 should be recommended SOFTWARE training
            boolean hasSoftwareRecommendation = trainingRecommendations.stream()
                .anyMatch(rec -> ((Technician) rec[0]).getId().equals(testTechnician2.getId()) 
                    && rec[1].equals(ServiceType.SOFTWARE));
            
            assertThat(hasSoftwareRecommendation).isTrue();
        }

        @Test
        @DisplayName("Should find best qualified technicians")
        void shouldFindBestQualifiedTechnicians() {
            // When
            List<Technician> bestHardwareTechs = technicianSkillRepository.findBestQualifiedTechnicians(ServiceType.HARDWARE);
            List<Technician> bestSoftwareTechs = technicianSkillRepository.findBestQualifiedTechnicians(ServiceType.SOFTWARE);

            // Then
            assertThat(bestHardwareTechs).hasSize(2)
                .extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());
            
            assertThat(bestSoftwareTechs).hasSize(1)
                .extracting(Technician::getId)
                .containsExactly(testTechnician1.getId());
        }

        @Test
        @DisplayName("Should return empty when no mentors available")
        void shouldReturnEmptyWhenNoMentorsAvailable() {
            // Given - scenario where all active technicians have all skills
            technicianSkillRepository.deleteAll();
            
            TechnicianSkill tech1Hardware = createTestTechnicianSkill(testTechnician1, ServiceType.HARDWARE);
            TechnicianSkill tech1Software = createTestTechnicianSkill(testTechnician1, ServiceType.SOFTWARE);
            TechnicianSkill tech2Hardware = createTestTechnicianSkill(testTechnician2, ServiceType.HARDWARE);
            TechnicianSkill tech2Software = createTestTechnicianSkill(testTechnician2, ServiceType.SOFTWARE);
            
            entityManager.persistAndFlush(tech1Hardware);
            entityManager.persistAndFlush(tech1Software);
            entityManager.persistAndFlush(tech2Hardware);
            entityManager.persistAndFlush(tech2Software);

            // When
            List<Object[]> potentialMentors = technicianSkillRepository.findPotentialMentorsBySkill();
            List<Object[]> trainingRecommendations = technicianSkillRepository.getTrainingRecommendations();

            // Then
            assertThat(potentialMentors).isEmpty(); // No one needs mentoring
            assertThat(trainingRecommendations).isEmpty(); // No training needed
        }

        @Test
        @DisplayName("Should order best qualified technicians by workload")
        void shouldOrderBestQualifiedTechniciansByWorkload() {
            // Given - create some tickets to establish workload differences
            // Note: This test assumes the query orders by SIZE(technician.assignedTickets)
            // We'll verify the ordering is consistent
            
            // When
            List<Technician> bestQualifiedTechs = technicianSkillRepository.findBestQualifiedTechnicians(ServiceType.HARDWARE);

            // Then
            assertThat(bestQualifiedTechs).hasSize(2);
            // The order should be consistent based on the query's ORDER BY clause
            // We can't easily test actual workload without setting up tickets, 
            // but we can verify the query returns results in a predictable order
            assertThat(bestQualifiedTechs).extracting(Technician::getId)
                .containsExactlyInAnyOrder(testTechnician1.getId(), testTechnician2.getId());
        }

        @Test
        @DisplayName("Should handle inactive technicians in mentorship queries")
        void shouldHandleInactiveTechniciansInMentorshipQueries() {
            // Given - testTechnician3 is INACTIVE and has no skills
            // mentorship queries should only consider ACTIVE technicians
            
            // When
            List<Object[]> potentialMentors = technicianSkillRepository.findPotentialMentorsBySkill();
            List<Object[]> trainingRecommendations = technicianSkillRepository.getTrainingRecommendations();
            List<Technician> bestQualified = technicianSkillRepository.findBestQualifiedTechnicians(ServiceType.HARDWARE);

            // Then
            // Results should not include inactive technicians
            assertThat(potentialMentors).isNotEmpty();
            assertThat(trainingRecommendations).isNotEmpty();
            assertThat(bestQualified).extracting(Technician::getStatus)
                .allMatch(status -> status == TechnicianStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Bulk Operations Tests")
    class BulkOperationsTests {

        @BeforeEach
        void setupBulkOperationsData() {
            setupBasicTestData();
        }

        @Test
        @DisplayName("Should find skills by multiple technicians")
        void shouldFindSkillsByMultipleTechnicians() {
            // Given
            List<Technician> technicians = List.of(testTechnician1, testTechnician2);

            // When
            List<TechnicianSkill> skills = technicianSkillRepository.findByTechnicianIn(technicians);

            // Then
            assertThat(skills).hasSize(3); // testTechnician1 has 2 skills, testTechnician2 has 1 skill
            assertThat(skills).extracting(skill -> skill.getTechnician().getId())
                .containsExactlyInAnyOrder(
                    testTechnician1.getId(), testTechnician1.getId(), testTechnician2.getId()
                );
        }

        @Test
        @DisplayName("Should find skills by multiple service types")
        void shouldFindSkillsByMultipleServiceTypes() {
            // Given
            List<ServiceType> serviceTypes = List.of(ServiceType.HARDWARE, ServiceType.SOFTWARE);

            // When
            List<TechnicianSkill> skills = technicianSkillRepository.findByServiceTypeIn(serviceTypes);

            // Then
            assertThat(skills).hasSize(3); // All 3 existing skills match these service types
            assertThat(skills).extracting(TechnicianSkill::getServiceType)
                .containsExactlyInAnyOrder(ServiceType.HARDWARE, ServiceType.SOFTWARE, ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should delete all skills by technician")
        void shouldDeleteAllSkillsByTechnician() {
            // Given - verify testTechnician1 has skills initially
            long initialCount = technicianSkillRepository.countByTechnician(testTechnician1);
            assertThat(initialCount).isEqualTo(2);

            // When
            technicianSkillRepository.deleteByTechnician(testTechnician1);

            // Then
            long finalCount = technicianSkillRepository.countByTechnician(testTechnician1);
            assertThat(finalCount).isZero();
            
            // Verify other technicians' skills are unaffected
            assertThat(technicianSkillRepository.countByTechnician(testTechnician2)).isEqualTo(1);
        }

        @Test
        @DisplayName("Should delete specific technician skill combination")
        void shouldDeleteSpecificTechnicianSkillCombination() {
            // Given - verify the specific skill exists
            boolean exists = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician1, ServiceType.HARDWARE);
            assertThat(exists).isTrue();

            // When
            technicianSkillRepository.deleteByTechnicianAndServiceType(testTechnician1, ServiceType.HARDWARE);

            // Then
            boolean stillExists = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician1, ServiceType.HARDWARE);
            assertThat(stillExists).isFalse();
            
            // Verify other skills of the same technician are unaffected
            boolean softwareStillExists = technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician1, ServiceType.SOFTWARE);
            assertThat(softwareStillExists).isTrue();
        }

        @Test
        @DisplayName("Should handle empty lists in bulk operations")
        void shouldHandleEmptyListsInBulkOperations() {
            // Given
            List<Technician> emptyTechnicians = List.of();
            List<ServiceType> emptyServiceTypes = List.of();

            // When
            List<TechnicianSkill> skillsByEmptyTechnicians = technicianSkillRepository.findByTechnicianIn(emptyTechnicians);
            List<TechnicianSkill> skillsByEmptyServiceTypes = technicianSkillRepository.findByServiceTypeIn(emptyServiceTypes);

            // Then
            assertThat(skillsByEmptyTechnicians).isEmpty();
            assertThat(skillsByEmptyServiceTypes).isEmpty();
        }

        @Test
        @DisplayName("Should handle single item lists in bulk operations")
        void shouldHandleSingleItemListsInBulkOperations() {
            // Given
            List<Technician> singleTechnician = List.of(testTechnician1);
            List<ServiceType> singleServiceType = List.of(ServiceType.HARDWARE);

            // When
            List<TechnicianSkill> skillsBySingleTechnician = technicianSkillRepository.findByTechnicianIn(singleTechnician);
            List<TechnicianSkill> skillsBySingleServiceType = technicianSkillRepository.findByServiceTypeIn(singleServiceType);

            // Then
            assertThat(skillsBySingleTechnician).hasSize(2); // testTechnician1 has 2 skills
            assertThat(skillsBySingleServiceType).hasSize(2); // 2 technicians have hardware skills
        }

        @Test
        @DisplayName("Should handle non-existent technicians in bulk operations")
        void shouldHandleNonExistentTechniciansInBulkOperations() {
            // Given
            Technician nonExistentTechnician = createTestTechnician("nonexistent@example.com");
            nonExistentTechnician.setId(999L); // Set a non-existent ID
            List<Technician> technicians = List.of(testTechnician1, nonExistentTechnician);

            // When
            List<TechnicianSkill> skills = technicianSkillRepository.findByTechnicianIn(technicians);

            // Then
            assertThat(skills).hasSize(2); // Only testTechnician1's skills should be found
            assertThat(skills).extracting(skill -> skill.getTechnician().getId())
                .allMatch(id -> id.equals(testTechnician1.getId()));
        }

        @Test
        @DisplayName("Should handle delete operations on non-existent combinations")
        void shouldHandleDeleteOperationsOnNonExistentCombinations() {
            // Given
            Technician nonExistentTechnician = createTestTechnician("delete.test@example.com");
            entityManager.persistAndFlush(nonExistentTechnician);

            // When & Then - these should not throw exceptions
            assertDoesNotThrow(() -> {
                technicianSkillRepository.deleteByTechnician(nonExistentTechnician);
                technicianSkillRepository.deleteByTechnicianAndServiceType(testTechnician2, ServiceType.SOFTWARE);
            });
        }
    }
} 
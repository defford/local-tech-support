package com.localtechsupport.service;

import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.TechnicianStatus;
import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.entity.TechnicianSkill;
import com.localtechsupport.repository.TechnicianRepository;
import com.localtechsupport.repository.TechnicianSkillRepository;
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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TechnicianService Tests")
class TechnicianServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private TechnicianSkillRepository technicianSkillRepository;

    @InjectMocks
    private TechnicianService technicianService;

    // Test data - entities
    private Technician activeTechnician;
    private Technician inactiveTechnician;
    private Technician inTrainingTechnician;
    private Technician terminatedTechnician;
    private TechnicianSkill hardwareSkill;
    private TechnicianSkill softwareSkill;

    @BeforeEach
    void setUp() {
        setupTestEntities();
    }

    private void setupTestEntities() {
        // Create test technicians
        activeTechnician = createTestTechnician(1L, "John Smith", "john.smith@techsupport.com", 
            TechnicianStatus.ACTIVE);
        
        inactiveTechnician = createTestTechnician(2L, "Jane Doe", "jane.doe@techsupport.com", 
            TechnicianStatus.INACTIVE);
        
        inTrainingTechnician = createTestTechnician(3L, "Bob Johnson", "bob.johnson@techsupport.com", 
            TechnicianStatus.IN_TRAINING);
        
        terminatedTechnician = createTestTechnician(4L, "Alice Williams", "alice.williams@techsupport.com", 
            TechnicianStatus.TERMINATED);

        // Create test skills
        hardwareSkill = createTestSkill(1L, activeTechnician, ServiceType.HARDWARE);
        softwareSkill = createTestSkill(2L, activeTechnician, ServiceType.SOFTWARE);
        
        // Set up skills for active technician
        activeTechnician.getSkills().add(hardwareSkill);
        activeTechnician.getSkills().add(softwareSkill);
    }

    // === TEST HELPER METHODS ===

    private Technician createTestTechnician(Long id, String fullName, String email, TechnicianStatus status) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setFullName(fullName);
        technician.setEmail(email);
        technician.setStatus(status);
        return technician;
    }

    private TechnicianSkill createTestSkill(Long id, Technician technician, ServiceType serviceType) {
        TechnicianSkill skill = new TechnicianSkill();
        skill.setId(id);
        skill.setTechnician(technician);
        skill.setServiceType(serviceType);
        return skill;
    }

    // === CORE TECHNICIAN CREATION TESTS ===

    @Nested
    @DisplayName("Technician Creation Tests")
    class TechnicianCreationTests {

        @Test
        @DisplayName("Should create technician successfully with valid inputs")
        void shouldCreateTechnicianSuccessfullyWithValidInputs() {
            // Given
            when(technicianRepository.existsByEmail("new.tech@example.com")).thenReturn(false);
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            Technician result = technicianService.createTechnician(
                "John Smith", "new.tech@example.com", Set.of(ServiceType.HARDWARE), "Test notes"
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("John Smith");
            assertThat(result.getEmail()).isEqualTo("john.smith@techsupport.com");
            assertThat(result.getStatus()).isEqualTo(TechnicianStatus.ACTIVE);

            // Verify interactions
            verify(technicianRepository).existsByEmail("new.tech@example.com");
            verify(technicianRepository).save(any(Technician.class));
        }

        @Test
        @DisplayName("Should verify technician object passed to save method")
        void shouldVerifyTechnicianObjectPassedToSaveMethod() {
            // Given
            when(technicianRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            technicianService.createTechnician("John Smith", "test@example.com", null, "Test notes");

            // Then
            ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
            verify(technicianRepository).save(technicianCaptor.capture());

            Technician capturedTechnician = technicianCaptor.getValue();
            assertThat(capturedTechnician.getFullName()).isEqualTo("John Smith");
            assertThat(capturedTechnician.getEmail()).isEqualTo("test@example.com");
            assertThat(capturedTechnician.getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            when(technicianRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> 
                technicianService.createTechnician("John Smith", "existing@example.com", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician with email existing@example.com already exists");

            // Verify no save occurred
            verify(technicianRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when full name is null")
        void shouldThrowExceptionWhenFullNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> 
                technicianService.createTechnician(null, "test@example.com", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full name is required");
        }

        @Test
        @DisplayName("Should throw exception when full name is empty")
        void shouldThrowExceptionWhenFullNameIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> 
                technicianService.createTechnician("", "test@example.com", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Full name is required");
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            assertThatThrownBy(() -> 
                technicianService.createTechnician("John Smith", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");
        }

        @Test
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // When & Then
            assertThatThrownBy(() -> 
                technicianService.createTechnician("John Smith", "invalid-email", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("Should normalize email to lowercase")
        void shouldNormalizeEmailToLowercase() {
            // Given
            // The service normalizes email first, so we need to stub the normalized version
            when(technicianRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            technicianService.createTechnician("John Smith", "TEST@EXAMPLE.COM", null, null);

            // Then
            ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
            verify(technicianRepository).save(technicianCaptor.capture());

            Technician capturedTechnician = technicianCaptor.getValue();
            assertThat(capturedTechnician.getEmail()).isEqualTo("test@example.com");
            
            // Verify the normalized email was used in the existence check
            verify(technicianRepository).existsByEmail("test@example.com");
        }

        @Test
        @DisplayName("Should trim whitespace from name and email")
        void shouldTrimWhitespaceFromNameAndEmail() {
            // Given
            // The service normalizes email first, so we need to stub the normalized version
            when(technicianRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            technicianService.createTechnician("  John Smith  ", "  test@example.com  ", null, null);

            // Then
            ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
            verify(technicianRepository).save(technicianCaptor.capture());

            Technician capturedTechnician = technicianCaptor.getValue();
            assertThat(capturedTechnician.getFullName()).isEqualTo("John Smith");
            assertThat(capturedTechnician.getEmail()).isEqualTo("test@example.com");
            
            // Verify the normalized email was used in the existence check
            verify(technicianRepository).existsByEmail("test@example.com");
        }

        @Test
        @DisplayName("Should add skills when provided during creation")
        void shouldAddSkillsWhenProvidedDuringCreation() {
            // Given
            when(technicianRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);
            when(technicianSkillRepository.findByTechnicianAndServiceType(any(), any())).thenReturn(Optional.empty());

            Set<ServiceType> skills = Set.of(ServiceType.HARDWARE, ServiceType.SOFTWARE);

            // When
            technicianService.createTechnician("John Smith", "test@example.com", skills, null);

            // Then
            verify(technicianSkillRepository, times(2)).save(any(TechnicianSkill.class));
        }
    }

    @Nested
    @DisplayName("Technician Update Tests")
    class TechnicianUpdateTests {

        @Test
        @DisplayName("Should update technician successfully with valid inputs")
        void shouldUpdateTechnicianSuccessfullyWithValidInputs() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            Technician result = technicianService.updateTechnician(1L, "Updated Name", "updated@example.com", "Updated notes");

            // Then
            assertThat(result).isNotNull();
            verify(technicianRepository).findById(1L);
            verify(technicianRepository).save(activeTechnician);
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            String originalEmail = activeTechnician.getEmail();

            // When
            technicianService.updateTechnician(1L, "Updated Name", null, null);

            // Then
            assertThat(activeTechnician.getFullName()).isEqualTo("Updated Name");
            assertThat(activeTechnician.getEmail()).isEqualTo(originalEmail); // Should remain unchanged
        }

        @Test
        @DisplayName("Should check email uniqueness when email is changed")
        void shouldCheckEmailUniquenessWhenEmailIsChanged() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.existsByEmail("newemail@example.com")).thenReturn(false);
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            technicianService.updateTechnician(1L, null, "newemail@example.com", null);

            // Then
            verify(technicianRepository).existsByEmail("newemail@example.com");
        }

        @Test
        @DisplayName("Should throw exception when updating to existing email")
        void shouldThrowExceptionWhenUpdatingToExistingEmail() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> 
                technicianService.updateTechnician(1L, null, "existing@example.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician with email existing@example.com already exists");
        }

        @Test
        @DisplayName("Should throw exception when technician not found")
        void shouldThrowExceptionWhenTechnicianNotFound() {
            // Given
            when(technicianRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                technicianService.updateTechnician(999L, "Updated Name", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician not found with ID: 999");
        }
    }

    @Nested
    @DisplayName("Technician Deletion Tests")
    class TechnicianDeletionTests {

        @Test
        @DisplayName("Should delete terminated technician successfully")
        void shouldDeleteTerminatedTechnicianSuccessfully() {
            // Given
            terminatedTechnician.getAssignedTickets().clear(); // No active tickets
            when(technicianRepository.findById(4L)).thenReturn(Optional.of(terminatedTechnician));
            doNothing().when(technicianRepository).deleteById(4L);

            // When
            technicianService.deleteTechnician(4L);

            // Then
            verify(technicianRepository).findById(4L);
            verify(technicianRepository).deleteById(4L);
        }

        @Test
        @DisplayName("Should throw exception when trying to delete non-terminated technician")
        void shouldThrowExceptionWhenTryingToDeleteNonTerminatedTechnician() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));

            // When & Then
            assertThatThrownBy(() -> 
                technicianService.deleteTechnician(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete technician. Please terminate first.");
        }

        @Test
        @DisplayName("Should throw exception when technician not found for deletion")
        void shouldThrowExceptionWhenTechnicianNotFoundForDeletion() {
            // Given
            when(technicianRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> 
                technicianService.deleteTechnician(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician not found with ID: 999");
        }
    }

    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {

        @Test
        @DisplayName("Should update technician status successfully with valid transition")
        void shouldUpdateTechnicianStatusSuccessfullyWithValidTransition() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            Technician result = technicianService.updateStatus(1L, TechnicianStatus.INACTIVE, "Taking break", "admin");

            // Then
            assertThat(result).isNotNull();
            verify(technicianRepository).findById(1L);
            verify(technicianRepository).save(activeTechnician);
        }

        @Test
        @DisplayName("Should activate technician successfully")
        void shouldActivateTechnicianSuccessfully() {
            // Given
            when(technicianRepository.findById(2L)).thenReturn(Optional.of(inactiveTechnician));
            when(technicianRepository.save(any(Technician.class))).thenReturn(inactiveTechnician);

            // When
            Technician result = technicianService.activateTechnician(2L, "admin");

            // Then
            assertThat(result).isNotNull();
            verify(technicianRepository).save(inactiveTechnician);
        }

        @Test
        @DisplayName("Should deactivate technician successfully")
        void shouldDeactivateTechnicianSuccessfully() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            Technician result = technicianService.deactivateTechnician(1L, "Personal leave", "admin");

            // Then
            assertThat(result).isNotNull();
            verify(technicianRepository).save(activeTechnician);
        }

        @Test
        @DisplayName("Should terminate technician successfully")
        void shouldTerminateTechnicianSuccessfully() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            Technician result = technicianService.terminateTechnician(1L, "Contract ended", "admin");

            // Then
            assertThat(result).isNotNull();
            verify(technicianRepository).save(activeTechnician);
        }

        @Test
        @DisplayName("Should throw exception for invalid status transition")
        void shouldThrowExceptionForInvalidStatusTransition() {
            // Given
            when(technicianRepository.findById(4L)).thenReturn(Optional.of(terminatedTechnician));

            // When & Then - Cannot transition from TERMINATED to ACTIVE
            assertThatThrownBy(() -> 
                technicianService.updateStatus(4L, TechnicianStatus.ACTIVE, "Rehire", "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition from TERMINATED to ACTIVE");
        }

        @Test
        @DisplayName("Should allow same status transition")
        void shouldAllowSameStatusTransition() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianRepository.save(any(Technician.class))).thenReturn(activeTechnician);

            // When
            Technician result = technicianService.updateStatus(1L, TechnicianStatus.ACTIVE, "Status confirmed", "admin");

            // Then
            assertThat(result).isNotNull();
            verify(technicianRepository).save(activeTechnician);
        }
    }

    @Nested
    @DisplayName("Search and Retrieval Tests")
    class SearchAndRetrievalTests {

        @Test
        @DisplayName("Should find technician by ID successfully")
        void shouldFindTechnicianByIdSuccessfully() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));

            // When
            Optional<Technician> result = technicianService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(activeTechnician);
            verify(technicianRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty optional when technician not found by ID")
        void shouldReturnEmptyOptionalWhenTechnicianNotFoundById() {
            // Given
            when(technicianRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Technician> result = technicianService.findById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(technicianRepository).findById(999L);
        }

        @Test
        @DisplayName("Should find all technicians with pagination")
        void shouldFindAllTechniciansWithPagination() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician, inactiveTechnician);
            Page<Technician> page = new PageImpl<>(technicians);
            when(technicianRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            Page<Technician> result = technicianService.findAllTechnicians(Pageable.unpaged());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(activeTechnician, inactiveTechnician);
            verify(technicianRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should search technicians with search term")
        void shouldSearchTechniciansWithSearchTerm() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician);
            Page<Technician> page = new PageImpl<>(technicians);
            when(technicianRepository.searchTechnicians("John", Pageable.unpaged())).thenReturn(page);

            // When
            Page<Technician> result = technicianService.searchTechnicians("John", Pageable.unpaged());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).containsExactly(activeTechnician);
            verify(technicianRepository).searchTechnicians("John", Pageable.unpaged());
        }
    }

    @Nested
    @DisplayName("Skill Management Tests")
    class SkillManagementTests {

        @Test
        @DisplayName("Should add skill to technician successfully")
        void shouldAddSkillToTechnicianSuccessfully() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));
            when(technicianSkillRepository.findByTechnicianAndServiceType(activeTechnician, ServiceType.HARDWARE))
                .thenReturn(Optional.empty());
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));

            // When
            Technician result = technicianService.addSkill(1L, ServiceType.HARDWARE);

            // Then
            assertThat(result).isNotNull();
            verify(technicianSkillRepository).save(any(TechnicianSkill.class));
        }

        @Test
        @DisplayName("Should get technician skills successfully")
        void shouldGetTechnicianSkillsSuccessfully() {
            // Given
            when(technicianRepository.findById(1L)).thenReturn(Optional.of(activeTechnician));

            // When
            Set<ServiceType> result = technicianService.getTechnicianSkills(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains(ServiceType.HARDWARE, ServiceType.SOFTWARE);
        }
    }

    @Nested
    @DisplayName("Statistics and Analytics Tests")
    class StatisticsAndAnalyticsTests {

        @Test
        @DisplayName("Should get comprehensive technician statistics")
        void shouldGetComprehensiveTechnicianStatistics() {
            // Given
            when(technicianRepository.count()).thenReturn(10L);
            when(technicianRepository.countByStatus(TechnicianStatus.ACTIVE)).thenReturn(7L);
            when(technicianRepository.countByStatus(TechnicianStatus.INACTIVE)).thenReturn(2L);
            when(technicianRepository.countByStatus(TechnicianStatus.IN_TRAINING)).thenReturn(1L);
            when(technicianRepository.countByStatus(TechnicianStatus.ON_VACATION)).thenReturn(0L);
            when(technicianRepository.countByStatus(TechnicianStatus.TERMINATED)).thenReturn(0L);
            when(technicianRepository.countAvailableTechnicians(TechnicianStatus.ACTIVE, 10L)).thenReturn(5L);

            // When
            Map<String, Object> result = technicianService.getTechnicianStatistics();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.get("totalTechnicians")).isEqualTo(10L);
            assertThat(result.get("activeTechnicians")).isEqualTo(7L);
            assertThat(result.get("inactiveTechnicians")).isEqualTo(2L);
            assertThat(result.get("inTrainingTechnicians")).isEqualTo(1L);
            assertThat(result.get("availableTechnicians")).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should return true when email is available")
        void shouldReturnTrueWhenEmailIsAvailable() {
            // Given
            when(technicianRepository.existsByEmail("available@example.com")).thenReturn(false);

            // When
            boolean result = technicianService.isEmailAvailable("available@example.com");

            // Then
            assertThat(result).isTrue();
            verify(technicianRepository).existsByEmail("available@example.com");
        }

        @Test
        @DisplayName("Should return false when email is taken")
        void shouldReturnFalseWhenEmailIsTaken() {
            // Given
            when(technicianRepository.existsByEmail("taken@example.com")).thenReturn(true);

            // When
            boolean result = technicianService.isEmailAvailable("taken@example.com");

            // Then
            assertThat(result).isFalse();
            verify(technicianRepository).existsByEmail("taken@example.com");
        }

        @Test
        @DisplayName("Should normalize email correctly")
        void shouldNormalizeEmailCorrectly() {
            // When & Then
            assertThat(technicianService.normalizeEmail("TEST@EXAMPLE.COM")).isEqualTo("test@example.com");
            assertThat(technicianService.normalizeEmail("  test@example.com  ")).isEqualTo("test@example.com");
            assertThat(technicianService.normalizeEmail(null)).isNull();
        }
    }
} 
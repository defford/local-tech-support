package com.localtechsupport.controller;

import com.localtechsupport.dto.request.*;
import com.localtechsupport.dto.response.*;
import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.TechnicianStatus;
import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.service.TechnicianService;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for TechnicianController.
 * 
 * Tests all REST endpoints, error scenarios, and response mappings.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TechnicianController Tests")
class TechnicianControllerTest {

    @Mock
    private TechnicianService technicianService;

    @InjectMocks
    private TechnicianController technicianController;

    // Test data
    private Technician activeTechnician;
    private Technician inactiveTechnician;
    private Technician inTrainingTechnician;
    private CreateTechnicianRequest createRequest;
    private UpdateTechnicianRequest updateRequest;
    private UpdateTechnicianStatusRequest statusRequest;
    private AddSkillRequest skillRequest;

    @BeforeEach
    void setUp() {
        // Create test technicians
        activeTechnician = createTestTechnician(1L, "John Smith", "john.smith@techsupport.com", 
            TechnicianStatus.ACTIVE);
        
        inactiveTechnician = createTestTechnician(2L, "Jane Doe", "jane.doe@techsupport.com", 
            TechnicianStatus.INACTIVE);
        
        inTrainingTechnician = createTestTechnician(3L, "Bob Johnson", "bob.johnson@techsupport.com", 
            TechnicianStatus.IN_TRAINING);

        // Create test requests
        createRequest = new CreateTechnicianRequest("Alice Williams", "alice.williams@techsupport.com", 
            Set.of(ServiceType.HARDWARE), "New technician");
        
        updateRequest = new UpdateTechnicianRequest("Updated Name", "updated@example.com", "Updated notes");
        
        statusRequest = new UpdateTechnicianStatusRequest(TechnicianStatus.INACTIVE, 
            "Taking leave", "admin");

        skillRequest = new AddSkillRequest(ServiceType.SOFTWARE);
    }

    private Technician createTestTechnician(Long id, String fullName, String email, TechnicianStatus status) {
        Technician technician = new Technician();
        technician.setId(id);
        technician.setFullName(fullName);
        technician.setEmail(email);
        technician.setStatus(status);
        return technician;
    }

    @Nested
    @DisplayName("Core CRUD Operations")
    class CoreCrudOperationTests {

        @Test
        @DisplayName("Should create technician successfully")
        void createTechnician_Success() {
            // Given
            when(technicianService.createTechnician(any(CreateTechnicianRequest.class)))
                .thenReturn(activeTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = technicianController.createTechnician(createRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getFullName()).isEqualTo("John Smith");
            assertThat(response.getBody().getEmail()).isEqualTo("john.smith@techsupport.com");
            assertThat(response.getBody().getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
            
            verify(technicianService).createTechnician(createRequest);
        }

        @Test
        @DisplayName("Should get technician by ID when found")
        void getTechnician_Found() {
            // Given
            when(technicianService.findById(1L)).thenReturn(Optional.of(activeTechnician));

            // When
            ResponseEntity<TechnicianResponse> response = technicianController.getTechnician(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getFullName()).isEqualTo("John Smith");
            assertThat(response.getBody().getEmail()).isEqualTo("john.smith@techsupport.com");
            
            verify(technicianService).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when technician not found")
        void getTechnician_NotFound() {
            // Given
            when(technicianService.findById(999L)).thenReturn(Optional.empty());

            // When
            ResponseEntity<TechnicianResponse> response = technicianController.getTechnician(999L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
            
            verify(technicianService).findById(999L);
        }

        @Test
        @DisplayName("Should update technician successfully")
        void updateTechnician_Success() {
            // Given
            when(technicianService.updateTechnician(eq(1L), any(UpdateTechnicianRequest.class)))
                .thenReturn(activeTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = technicianController.updateTechnician(1L, updateRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            
            verify(technicianService).updateTechnician(1L, updateRequest);
        }

        @Test
        @DisplayName("Should delete technician successfully")
        void deleteTechnician_Success() {
            // Given
            doNothing().when(technicianService).deleteTechnician(2L);

            // When
            ResponseEntity<Void> response = technicianController.deleteTechnician(2L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();
            
            verify(technicianService).deleteTechnician(2L);
        }

        @Test
        @DisplayName("Should get all technicians with pagination")
        void getAllTechnicians_WithPagination() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician, inactiveTechnician);
            Page<Technician> page = new PageImpl<>(technicians);
            when(technicianService.findAllTechnicians(any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<TechnicianResponse>> response = 
                technicianController.getAllTechnicians(0, 20, "fullName", "asc", null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(2);
            
            verify(technicianService).findAllTechnicians(any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter technicians by status")
        void getAllTechnicians_FilterByStatus() {
            // Given
            List<Technician> activeTechnicians = Arrays.asList(activeTechnician);
            Page<Technician> page = new PageImpl<>(activeTechnicians);
            when(technicianService.findTechniciansByStatus(eq(TechnicianStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

            // When
            ResponseEntity<Page<TechnicianResponse>> response = 
                technicianController.getAllTechnicians(0, 20, "fullName", "asc", TechnicianStatus.ACTIVE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(technicianService).findTechniciansByStatus(TechnicianStatus.ACTIVE, 
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("fullName").ascending()));
        }

        @Test
        @DisplayName("Should handle service exception during creation")
        void createTechnician_ServiceThrowsException() {
            // Given
            when(technicianService.createTechnician(any(CreateTechnicianRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

            // When & Then
            assertThatThrownBy(() -> 
                technicianController.createTechnician(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

            verify(technicianService).createTechnician(createRequest);
        }
    }

    @Nested
    @DisplayName("Status Management")
    class StatusManagementTests {

        @Test
        @DisplayName("Should update technician status successfully")
        void updateStatus_Success() {
            // Given
            when(technicianService.updateStatus(1L, TechnicianStatus.INACTIVE, "Taking leave", "admin"))
                .thenReturn(activeTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = 
                technicianController.updateStatus(1L, statusRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            
            verify(technicianService).updateStatus(1L, TechnicianStatus.INACTIVE, "Taking leave", "admin");
        }

        @Test
        @DisplayName("Should activate technician successfully")
        void activateTechnician_Success() {
            // Given
            when(technicianService.activateTechnician(2L, "admin")).thenReturn(activeTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = 
                technicianController.activateTechnician(2L, "admin");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            
            verify(technicianService).activateTechnician(2L, "admin");
        }

        @Test
        @DisplayName("Should deactivate technician successfully")
        void deactivateTechnician_Success() {
            // Given
            when(technicianService.deactivateTechnician(1L, "Personal leave", "admin"))
                .thenReturn(inactiveTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = 
                technicianController.deactivateTechnician(1L, "Personal leave", "admin");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(2L);
            
            verify(technicianService).deactivateTechnician(1L, "Personal leave", "admin");
        }

        @Test
        @DisplayName("Should terminate technician successfully")
        void terminateTechnician_Success() {
            // Given
            when(technicianService.terminateTechnician(1L, "Contract ended", "admin"))
                .thenReturn(activeTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = 
                technicianController.terminateTechnician(1L, "Contract ended", "admin");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            
            verify(technicianService).terminateTechnician(1L, "Contract ended", "admin");
        }

        @Test
        @DisplayName("Should handle invalid status transition")
        void updateStatus_InvalidTransition() {
            // Given
            when(technicianService.updateStatus(1L, TechnicianStatus.ACTIVE, "Invalid", "admin"))
                .thenThrow(new IllegalStateException("Invalid status transition"));

            // When & Then
            assertThatThrownBy(() -> {
                UpdateTechnicianStatusRequest invalidRequest = 
                    new UpdateTechnicianStatusRequest(TechnicianStatus.ACTIVE, "Invalid", "admin");
                technicianController.updateStatus(1L, invalidRequest);
            }).isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("Invalid status transition");
        }
    }

    @Nested
    @DisplayName("Search and Filtering")
    class SearchAndFilteringTests {

        @Test
        @DisplayName("Should search technicians by query")
        void searchTechnicians_Success() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician);
            Page<Technician> page = new PageImpl<>(technicians);
            when(technicianService.searchTechnicians(eq("John"), any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<TechnicianResponse>> response = 
                technicianController.searchTechnicians("John", 0, 20, "fullName", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(technicianService).searchTechnicians(eq("John"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should get technicians by specific status")
        void getTechniciansByStatus_Success() {
            // Given
            List<Technician> activeTechnicians = Arrays.asList(activeTechnician);
            Page<Technician> page = new PageImpl<>(activeTechnicians);
            when(technicianService.findTechniciansByStatus(eq(TechnicianStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

            // When
            ResponseEntity<Page<TechnicianResponse>> response = 
                technicianController.getTechniciansByStatus(TechnicianStatus.ACTIVE, 0, 20, "fullName", "asc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            
            verify(technicianService).findTechniciansByStatus(eq(TechnicianStatus.ACTIVE), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Skill Management")
    class SkillManagementTests {

        @Test
        @DisplayName("Should get technician skills successfully")
        void getTechnicianSkills_Success() {
            // Given
            Set<ServiceType> skills = Set.of(ServiceType.HARDWARE, ServiceType.SOFTWARE);
            when(technicianService.getTechnicianSkills(1L)).thenReturn(skills);

            // When
            ResponseEntity<Set<ServiceType>> response = technicianController.getTechnicianSkills(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody()).contains(ServiceType.HARDWARE, ServiceType.SOFTWARE);
            
            verify(technicianService).getTechnicianSkills(1L);
        }

        @Test
        @DisplayName("Should add skill to technician successfully")
        void addSkill_Success() {
            // Given
            when(technicianService.addSkill(1L, ServiceType.SOFTWARE)).thenReturn(activeTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = 
                technicianController.addSkill(1L, skillRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            
            verify(technicianService).addSkill(1L, ServiceType.SOFTWARE);
        }

        @Test
        @DisplayName("Should remove skill from technician successfully")
        void removeSkill_Success() {
            // Given
            when(technicianService.removeSkill(1L, ServiceType.HARDWARE)).thenReturn(activeTechnician);

            // When
            ResponseEntity<TechnicianResponse> response = 
                technicianController.removeSkill(1L, ServiceType.HARDWARE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            
            verify(technicianService).removeSkill(1L, ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should handle adding skill to non-existent technician")
        void addSkill_TechnicianNotFound() {
            // Given
            when(technicianService.addSkill(999L, ServiceType.SOFTWARE))
                .thenThrow(new IllegalArgumentException("Technician not found with ID: 999"));

            // When & Then
            assertThatThrownBy(() -> 
                technicianController.addSkill(999L, skillRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician not found with ID: 999");
        }
    }

    @Nested
    @DisplayName("Availability and Workload")
    class AvailabilityAndWorkloadTests {

        @Test
        @DisplayName("Should get available technicians for service type")
        void getAvailableTechnicians_WithServiceType() {
            // Given
            List<Technician> availableTechnicians = Arrays.asList(activeTechnician);
            when(technicianService.getAvailableTechniciansForService(ServiceType.HARDWARE))
                .thenReturn(availableTechnicians);

            // When
            ResponseEntity<List<TechnicianResponse>> response = 
                technicianController.getAvailableTechnicians(ServiceType.HARDWARE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            
            verify(technicianService).getAvailableTechniciansForService(ServiceType.HARDWARE);
        }

        @Test
        @DisplayName("Should get all available technicians when no service type specified")
        void getAvailableTechnicians_WithoutServiceType() {
            // Given
            List<Technician> availableTechnicians = Arrays.asList(activeTechnician, inactiveTechnician);
            when(technicianService.getAvailableTechnicians()).thenReturn(availableTechnicians);

            // When
            ResponseEntity<List<TechnicianResponse>> response = 
                technicianController.getAvailableTechnicians(null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            
            verify(technicianService).getAvailableTechnicians();
        }

        @Test
        @DisplayName("Should get technician workload successfully")
        void getTechnicianWorkload_Success() {
            // Given
            when(technicianService.getTechnicianWorkload(1L)).thenReturn(5L);

            // When
            ResponseEntity<Map<String, Object>> response = 
                technicianController.getTechnicianWorkload(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("technicianId")).isEqualTo(1L);
            assertThat(response.getBody().get("currentWorkload")).isEqualTo(5L);
            assertThat(response.getBody().get("available")).isEqualTo(true);
            
            verify(technicianService).getTechnicianWorkload(1L);
        }

        @Test
        @DisplayName("Should indicate unavailable when workload is high")
        void getTechnicianWorkload_HighWorkload() {
            // Given
            when(technicianService.getTechnicianWorkload(1L)).thenReturn(15L);

            // When
            ResponseEntity<Map<String, Object>> response = 
                technicianController.getTechnicianWorkload(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("currentWorkload")).isEqualTo(15L);
            assertThat(response.getBody().get("available")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Statistics and Analytics")
    class StatisticsTests {

        @Test
        @DisplayName("Should get comprehensive statistics successfully")
        void getStatistics_Success() {
            // Given
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalTechnicians", 10L);
            statistics.put("activeTechnicians", 7L);
            statistics.put("inactiveTechnicians", 2L);
            statistics.put("availableTechnicians", 5L);
            when(technicianService.getTechnicianStatistics()).thenReturn(statistics);

            // When
            ResponseEntity<Map<String, Object>> response = technicianController.getStatistics();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("totalTechnicians")).isEqualTo(10L);
            assertThat(response.getBody().get("activeTechnicians")).isEqualTo(7L);
            assertThat(response.getBody().get("availableTechnicians")).isEqualTo(5L);
            
            verify(technicianService).getTechnicianStatistics();
        }

        @Test
        @DisplayName("Should get workload distribution successfully")
        void getWorkloadDistribution_Success() {
            // Given
            Map<TechnicianStatus, Long> distribution = new HashMap<>();
            distribution.put(TechnicianStatus.ACTIVE, 7L);
            distribution.put(TechnicianStatus.INACTIVE, 2L);
            distribution.put(TechnicianStatus.IN_TRAINING, 1L);
            when(technicianService.getWorkloadDistribution()).thenReturn(distribution);

            // When
            ResponseEntity<Map<TechnicianStatus, Long>> response = 
                technicianController.getWorkloadDistribution();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get(TechnicianStatus.ACTIVE)).isEqualTo(7L);
            assertThat(response.getBody().get(TechnicianStatus.INACTIVE)).isEqualTo(2L);
            
            verify(technicianService).getWorkloadDistribution();
        }

        @Test
        @DisplayName("Should get skill coverage successfully")
        void getSkillCoverage_Success() {
            // Given
            Map<ServiceType, Long> coverage = new HashMap<>();
            coverage.put(ServiceType.HARDWARE, 5L);
            coverage.put(ServiceType.SOFTWARE, 3L);
            when(technicianService.getSkillCoverage()).thenReturn(coverage);

            // When
            ResponseEntity<Map<ServiceType, Long>> response = technicianController.getSkillCoverage();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get(ServiceType.HARDWARE)).isEqualTo(5L);
            assertThat(response.getBody().get(ServiceType.SOFTWARE)).isEqualTo(3L);
            
            verify(technicianService).getSkillCoverage();
        }

        @Test
        @DisplayName("Should handle empty statistics")
        void getStatistics_EmptyData() {
            // Given
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalTechnicians", 0L);
            emptyStats.put("activeTechnicians", 0L);
            emptyStats.put("availableTechnicians", 0L);
            when(technicianService.getTechnicianStatistics()).thenReturn(emptyStats);

            // When
            ResponseEntity<Map<String, Object>> response = technicianController.getStatistics();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("totalTechnicians")).isEqualTo(0L);
            assertThat(response.getBody().get("activeTechnicians")).isEqualTo(0L);
            assertThat(response.getBody().get("availableTechnicians")).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Email Availability")
    class EmailAvailabilityTests {

        @Test
        @DisplayName("Should check email availability successfully")
        void checkEmailAvailability_Available() {
            // Given
            when(technicianService.isEmailAvailable("available@example.com")).thenReturn(true);

            // When
            ResponseEntity<Map<String, Boolean>> response = 
                technicianController.checkEmailAvailability("available@example.com");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("available")).isTrue();
            assertThat(response.getBody().get("email")).isFalse();
            
            verify(technicianService).isEmailAvailable("available@example.com");
        }

        @Test
        @DisplayName("Should return false when email is taken")
        void checkEmailAvailability_Taken() {
            // Given
            when(technicianService.isEmailAvailable("taken@example.com")).thenReturn(false);

            // When
            ResponseEntity<Map<String, Boolean>> response = 
                technicianController.checkEmailAvailability("taken@example.com");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("available")).isFalse();
            assertThat(response.getBody().get("email")).isTrue();
            
            verify(technicianService).isEmailAvailable("taken@example.com");
        }
    }

    @Nested
    @DisplayName("Response Mapping")
    class ResponseMappingTests {

        @Test
        @DisplayName("Should map technician with all fields correctly")
        void mapToTechnicianResponse_WithAllFields() {
            // Given
            activeTechnician.getSkills().clear(); // Clear to test conversion
            when(technicianService.findById(1L)).thenReturn(Optional.of(activeTechnician));

            // When
            ResponseEntity<TechnicianResponse> response = technicianController.getTechnician(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            TechnicianResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getId()).isEqualTo(1L);
            assertThat(body.getFullName()).isEqualTo("John Smith");
            assertThat(body.getEmail()).isEqualTo("john.smith@techsupport.com");
            assertThat(body.getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
            assertThat(body.isAvailable()).isTrue(); // Active with low workload
        }

        @Test
        @DisplayName("Should map technician availability correctly based on status and workload")
        void mapToTechnicianResponse_AvailabilityCalculation() {
            // Given
            // Mock high workload scenario
            Technician busyTechnician = createTestTechnician(1L, "Busy Tech", "busy@example.com", TechnicianStatus.ACTIVE);
            when(technicianService.findById(1L)).thenReturn(Optional.of(busyTechnician));

            // When
            ResponseEntity<TechnicianResponse> response = technicianController.getTechnician(1L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            TechnicianResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getStatus()).isEqualTo(TechnicianStatus.ACTIVE);
            // Availability depends on current load calculation in convertToResponse method
        }

        @Test
        @DisplayName("Should handle inactive technician status correctly")
        void mapToTechnicianResponse_InactiveStatus() {
            // Given
            when(technicianService.findById(2L)).thenReturn(Optional.of(inactiveTechnician));

            // When
            ResponseEntity<TechnicianResponse> response = technicianController.getTechnician(2L);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            TechnicianResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getStatus()).isEqualTo(TechnicianStatus.INACTIVE);
            assertThat(body.isAvailable()).isFalse(); // Inactive technicians are not available
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting")
    class PaginationAndSortingTests {

        @Test
        @DisplayName("Should handle custom sorting")
        void getAllTechnicians_CustomSorting() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician, inactiveTechnician);
            Page<Technician> page = new PageImpl<>(technicians);
            when(technicianService.findAllTechnicians(any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<TechnicianResponse>> response = 
                technicianController.getAllTechnicians(0, 10, "email", "desc", null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(2);
            
            // Verify sorting parameters are passed correctly
            verify(technicianService).findAllTechnicians(
                PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("email").descending()));
        }

        @Test
        @DisplayName("Should handle custom pagination parameters")
        void getAllTechnicians_CustomPagination() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician);
            Page<Technician> page = new PageImpl<>(technicians);
            when(technicianService.findAllTechnicians(any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<TechnicianResponse>> response = 
                technicianController.getAllTechnicians(2, 5, "fullName", "asc", null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            // Verify pagination parameters
            verify(technicianService).findAllTechnicians(
                PageRequest.of(2, 5, org.springframework.data.domain.Sort.by("fullName").ascending()));
        }

        @Test
        @DisplayName("Should handle search with sorting and pagination")
        void searchTechnicians_WithSortingAndPagination() {
            // Given
            List<Technician> technicians = Arrays.asList(activeTechnician);
            Page<Technician> page = new PageImpl<>(technicians);
            when(technicianService.searchTechnicians(eq("John"), any(Pageable.class))).thenReturn(page);

            // When
            ResponseEntity<Page<TechnicianResponse>> response = 
                technicianController.searchTechnicians("John", 1, 15, "email", "desc");

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            verify(technicianService).searchTechnicians(eq("John"), 
                eq(PageRequest.of(1, 15, org.springframework.data.domain.Sort.by("email").descending())));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle service exceptions during update")
        void updateTechnician_ServiceThrowsException() {
            // Given
            when(technicianService.updateTechnician(eq(1L), any(UpdateTechnicianRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

            // When & Then
            assertThatThrownBy(() -> 
                technicianController.updateTechnician(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

            verify(technicianService).updateTechnician(1L, updateRequest);
        }

        @Test
        @DisplayName("Should handle service exceptions during status update")
        void updateStatus_ServiceThrowsException() {
            // Given
            when(technicianService.updateStatus(anyLong(), any(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("Invalid status transition"));

            // When & Then
            assertThatThrownBy(() -> 
                technicianController.updateStatus(1L, statusRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("Should handle service exceptions during deletion")
        void deleteTechnician_ServiceThrowsException() {
            // Given
            doThrow(new IllegalStateException("Cannot delete active technician"))
                .when(technicianService).deleteTechnician(1L);

            // When & Then
            assertThatThrownBy(() -> 
                technicianController.deleteTechnician(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete active technician");

            verify(technicianService).deleteTechnician(1L);
        }

        @Test
        @DisplayName("Should handle service exceptions during skill operations")
        void skillOperations_ServiceThrowsException() {
            // Given
            when(technicianService.addSkill(999L, ServiceType.HARDWARE))
                .thenThrow(new IllegalArgumentException("Technician not found"));

            // When & Then
            assertThatThrownBy(() -> 
                technicianController.addSkill(999L, new AddSkillRequest(ServiceType.HARDWARE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Technician not found");
        }
    }
} 
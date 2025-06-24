package com.localtechsupport.service;

import com.localtechsupport.entity.*;
import com.localtechsupport.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simplified unit tests for TechnicianSkillService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TechnicianSkillService Simple Tests")
class TechnicianSkillSimpleServiceTest {

    @Mock
    private TechnicianSkillRepository technicianSkillRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private TechnicianSkillService technicianSkillService;

    private Technician testTechnician;
    private TechnicianSkill testSkill;
    private ServiceType testServiceType;

    @BeforeEach
    void setUp() {
        testServiceType = ServiceType.HARDWARE;
        
        testTechnician = new Technician();
        testTechnician.setId(1L);
        testTechnician.setFullName("John Doe");
        testTechnician.setEmail("john.doe@example.com");
        testTechnician.setStatus(TechnicianStatus.ACTIVE);

        testSkill = new TechnicianSkill();
        testSkill.setId(1L);
        testSkill.setTechnician(testTechnician);
        testSkill.setServiceType(testServiceType);
    }

    @Test
    @DisplayName("Should add skill to technician successfully")
    void shouldAddSkillToTechnicianSuccessfully() {
        // Given
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(technicianSkillRepository.existsByTechnicianAndServiceType(testTechnician, testServiceType)).thenReturn(false);
        when(technicianSkillRepository.save(any(TechnicianSkill.class))).thenReturn(testSkill);

        // When
        TechnicianSkill result = technicianSkillService.addSkill(1L, testServiceType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTechnician()).isEqualTo(testTechnician);
        assertThat(result.getServiceType()).isEqualTo(testServiceType);
        verify(technicianSkillRepository).save(any(TechnicianSkill.class));
    }

    @Test
    @DisplayName("Should throw exception when technician not found")
    void shouldThrowExceptionWhenTechnicianNotFound() {
        // Given
        when(technicianRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> technicianSkillService.addSkill(999L, testServiceType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Technician not found with ID: 999");
    }

    @Test
    @DisplayName("Should throw exception when technician is inactive")
    void shouldThrowExceptionWhenTechnicianInactive() {
        // Given
        testTechnician.setStatus(TechnicianStatus.INACTIVE);
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));

        // When & Then
        assertThatThrownBy(() -> technicianSkillService.addSkill(1L, testServiceType))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot assign skills to inactive technician");
    }

    @Test
    @DisplayName("Should remove skill from technician successfully")
    void shouldRemoveSkillFromTechnicianSuccessfully() {
        // Given
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(technicianSkillRepository.findByTechnicianAndServiceType(testTechnician, testServiceType))
            .thenReturn(Optional.of(testSkill));

        // When
        technicianSkillService.removeSkill(1L, testServiceType);

        // Then
        verify(technicianSkillRepository).delete(testSkill);
    }

    @Test
    @DisplayName("Should find skill by ID")
    void shouldFindSkillById() {
        // Given
        when(technicianSkillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        // When
        Optional<TechnicianSkill> result = technicianSkillService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testSkill);
    }

    @Test
    @DisplayName("Should find all skills with pagination")
    void shouldFindAllSkillsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TechnicianSkill> expectedPage = new PageImpl<>(Collections.singletonList(testSkill));
        when(technicianSkillRepository.findAll(pageable)).thenReturn(expectedPage);

        // When
        Page<TechnicianSkill> result = technicianSkillService.findAllSkills(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testSkill);
    }

    @Test
    @DisplayName("Should find skills by technician")
    void shouldFindSkillsByTechnician() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TechnicianSkill> expectedPage = new PageImpl<>(Collections.singletonList(testSkill));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(technicianSkillRepository.findByTechnician(testTechnician, pageable)).thenReturn(expectedPage);

        // When
        Page<TechnicianSkill> result = technicianSkillService.findSkillsByTechnician(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should count skills by technician")
    void shouldCountSkillsByTechnician() {
        // Given
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(technicianSkillRepository.countByTechnician(testTechnician)).thenReturn(3L);

        // When
        long result = technicianSkillService.countSkillsByTechnician(1L);

        // Then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should check if technician is qualified")
    void shouldCheckIfTechnicianIsQualified() {
        // Given
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(technicianSkillRepository.isTechnicianQualifiedFor(testTechnician, testServiceType)).thenReturn(true);

        // When
        boolean result = technicianSkillService.isTechnicianQualified(1L, testServiceType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should find technicians by service type")
    void shouldFindTechniciansByServiceType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Technician> expectedPage = new PageImpl<>(Collections.singletonList(testTechnician));
        when(technicianSkillRepository.findTechniciansWithSkill(testServiceType, pageable)).thenReturn(expectedPage);

        // When
        Page<Technician> result = technicianSkillService.findTechniciansByServiceType(testServiceType, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testTechnician);
    }

    @Test
    @DisplayName("Should add multiple skills successfully")
    void shouldAddMultipleSkillsSuccessfully() {
        // Given
        List<ServiceType> serviceTypes = Arrays.asList(ServiceType.HARDWARE, ServiceType.SOFTWARE);
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(technicianSkillRepository.existsByTechnicianAndServiceType(eq(testTechnician), any(ServiceType.class))).thenReturn(false);
        when(technicianSkillRepository.save(any(TechnicianSkill.class))).thenReturn(testSkill);

        // When
        List<TechnicianSkill> result = technicianSkillService.addMultipleSkills(1L, serviceTypes);

        // Then
        assertThat(result).hasSize(2);
        verify(technicianSkillRepository, times(2)).save(any(TechnicianSkill.class));
    }

    @Test
    @DisplayName("Should remove all skills from technician")
    void shouldRemoveAllSkillsFromTechnician() {
        // Given
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));

        // When
        technicianSkillService.removeAllSkills(1L);

        // Then
        verify(technicianSkillRepository).deleteByTechnician(testTechnician);
    }

    @Test
    @DisplayName("Should validate duplicate skill assignment")
    void shouldValidateDuplicateSkillAssignment() {
        // Given
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(technicianSkillRepository.countDuplicateSkills(testTechnician, testServiceType)).thenReturn(1L);

        // When
        boolean result = technicianSkillService.wouldCreateDuplicate(1L, testServiceType);

        // Then
        assertThat(result).isTrue();
    }
} 
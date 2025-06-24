package com.localtechsupport.controller;

import com.localtechsupport.dto.request.AddSkillRequest;
import com.localtechsupport.dto.response.SkillResponse;
import com.localtechsupport.dto.response.SkillCoverageResponse;
import com.localtechsupport.entity.*;
import com.localtechsupport.service.TechnicianSkillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TechnicianSkillController.
 */
@WebMvcTest(TechnicianSkillController.class)
@DisplayName("TechnicianSkillController Tests")
class TechnicianSkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TechnicianSkillService technicianSkillService;

    @Autowired
    private ObjectMapper objectMapper;

    private TechnicianSkill testSkill;
    private SkillResponse testSkillResponse;
    private AddSkillRequest testAddSkillRequest;

    @BeforeEach
    void setUp() {
        Technician testTechnician = new Technician();
        testTechnician.setId(1L);
        testTechnician.setFullName("John Doe");
        testTechnician.setEmail("john.doe@example.com");
        testTechnician.setStatus(TechnicianStatus.ACTIVE);

        testSkill = new TechnicianSkill();
        testSkill.setId(1L);
        testSkill.setTechnician(testTechnician);
        testSkill.setServiceType(ServiceType.HARDWARE);

        testSkillResponse = SkillResponse.fromEntity(testSkill);

        testAddSkillRequest = new AddSkillRequest();
        testAddSkillRequest.setTechnicianId(1L);
        testAddSkillRequest.setServiceType(ServiceType.HARDWARE);
    }

    @Test
    @DisplayName("Should add skill successfully")
    void shouldAddSkillSuccessfully() throws Exception {
        // Given
        when(technicianSkillService.addSkill(1L, ServiceType.HARDWARE)).thenReturn(testSkill);

        // When & Then
        mockMvc.perform(post("/api/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAddSkillRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skillId").value(1L))
                .andExpect(jsonPath("$.technicianId").value(1L))
                .andExpect(jsonPath("$.serviceType").value("HARDWARE"));

        verify(technicianSkillService).addSkill(1L, ServiceType.HARDWARE);
    }

    @Test
    @DisplayName("Should return bad request for invalid add skill request")
    void shouldReturnBadRequestForInvalidAddSkillRequest() throws Exception {
        // Given
        when(technicianSkillService.addSkill(1L, ServiceType.HARDWARE))
            .thenThrow(new IllegalArgumentException("Invalid request"));

        // When & Then
        mockMvc.perform(post("/api/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAddSkillRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should remove skill successfully")
    void shouldRemoveSkillSuccessfully() throws Exception {
        // Given
        doNothing().when(technicianSkillService).removeSkill(1L, ServiceType.HARDWARE);

        // When & Then
        mockMvc.perform(delete("/api/skills/technician/1/service-type/HARDWARE"))
                .andExpect(status().isNoContent());

        verify(technicianSkillService).removeSkill(1L, ServiceType.HARDWARE);
    }

    @Test
    @DisplayName("Should return not found when removing non-existent skill")
    void shouldReturnNotFoundWhenRemovingNonExistentSkill() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Skill not found"))
            .when(technicianSkillService).removeSkill(1L, ServiceType.HARDWARE);

        // When & Then
        mockMvc.perform(delete("/api/skills/technician/1/service-type/HARDWARE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get skill by ID successfully")
    void shouldGetSkillByIdSuccessfully() throws Exception {
        // Given
        when(technicianSkillService.findById(1L)).thenReturn(Optional.of(testSkill));

        // When & Then
        mockMvc.perform(get("/api/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillId").value(1L))
                .andExpect(jsonPath("$.technicianName").value("John Doe"));

        verify(technicianSkillService).findById(1L);
    }

    @Test
    @DisplayName("Should return not found for non-existent skill ID")
    void shouldReturnNotFoundForNonExistentSkillId() throws Exception {
        // Given
        when(technicianSkillService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/skills/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get all skills with pagination")
    void shouldGetAllSkillsWithPagination() throws Exception {
        // Given
        Page<TechnicianSkill> skillsPage = new PageImpl<>(Collections.singletonList(testSkill));
        when(technicianSkillService.findAllSkills(any())).thenReturn(skillsPage);

        // When & Then
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].skillId").value(1L));

        verify(technicianSkillService).findAllSkills(any());
    }

    @Test
    @DisplayName("Should get skills by technician")
    void shouldGetSkillsByTechnician() throws Exception {
        // Given
        Page<TechnicianSkill> skillsPage = new PageImpl<>(Collections.singletonList(testSkill));
        when(technicianSkillService.findSkillsByTechnician(eq(1L), any())).thenReturn(skillsPage);

        // When & Then
        mockMvc.perform(get("/api/skills/technician/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].skillId").value(1L));

        verify(technicianSkillService).findSkillsByTechnician(eq(1L), any());
    }

    @Test
    @DisplayName("Should get technician service types")
    void shouldGetTechnicianServiceTypes() throws Exception {
        // Given
        List<ServiceType> serviceTypes = Arrays.asList(ServiceType.HARDWARE, ServiceType.SOFTWARE);
        when(technicianSkillService.getTechnicianServiceTypes(1L)).thenReturn(serviceTypes);

        // When & Then
        mockMvc.perform(get("/api/skills/technician/1/service-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("HARDWARE"))
                .andExpect(jsonPath("$[1]").value("SOFTWARE"));

        verify(technicianSkillService).getTechnicianServiceTypes(1L);
    }

    @Test
    @DisplayName("Should count skills by technician")
    void shouldCountSkillsByTechnician() throws Exception {
        // Given
        when(technicianSkillService.countSkillsByTechnician(1L)).thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/skills/technician/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(technicianSkillService).countSkillsByTechnician(1L);
    }

    @Test
    @DisplayName("Should check if technician is qualified")  
    void shouldCheckIfTechnicianIsQualified() throws Exception {
        // Given
        when(technicianSkillService.isTechnicianQualified(1L, ServiceType.HARDWARE)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/skills/technician/1/qualified/HARDWARE"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(technicianSkillService).isTechnicianQualified(1L, ServiceType.HARDWARE);
    }

    @Test
    @DisplayName("Should get skill coverage")
    void shouldGetSkillCoverage() throws Exception {
        // Given
        Map<String, Object> coverage = new HashMap<>();
        coverage.put("totalSkillTypes", 2);
        Map<String, Object> composition = new HashMap<>();
        composition.put("totalTechnicians", 5L);
        
        when(technicianSkillService.getSkillCoverage()).thenReturn(coverage);
        when(technicianSkillService.getTeamSkillComposition()).thenReturn(composition);

        // When & Then
        mockMvc.perform(get("/api/skills/coverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSkillTypes").value(2))
                .andExpect(jsonPath("$.totalTechnicians").value(5));

        verify(technicianSkillService).getSkillCoverage();
        verify(technicianSkillService).getTeamSkillComposition();
    }

    @Test
    @DisplayName("Should add multiple skills successfully")
    void shouldAddMultipleSkillsSuccessfully() throws Exception {
        // Given
        List<ServiceType> serviceTypes = Arrays.asList(ServiceType.HARDWARE, ServiceType.SOFTWARE);
        List<TechnicianSkill> skills = Arrays.asList(testSkill, testSkill);
        when(technicianSkillService.addMultipleSkills(1L, serviceTypes)).thenReturn(skills);

        // When & Then
        mockMvc.perform(post("/api/skills/technician/1/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceTypes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].skillId").value(1L));

        verify(technicianSkillService).addMultipleSkills(1L, serviceTypes);
    }

    @Test
    @DisplayName("Should remove all skills from technician")
    void shouldRemoveAllSkillsFromTechnician() throws Exception {
        // Given
        doNothing().when(technicianSkillService).removeAllSkills(1L);

        // When & Then
        mockMvc.perform(delete("/api/skills/technician/1/all"))
                .andExpect(status().isNoContent());

        verify(technicianSkillService).removeAllSkills(1L);
    }

    @Test
    @DisplayName("Should validate skill assignment")
    void shouldValidateSkillAssignment() throws Exception {
        // Given
        when(technicianSkillService.wouldCreateDuplicate(1L, ServiceType.HARDWARE)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/skills/validate/technician/1/service-type/HARDWARE"))
                .andExpect(status().isOk())
                .andExpect(content().string("true")); // Returns true if assignment is valid

        verify(technicianSkillService).wouldCreateDuplicate(1L, ServiceType.HARDWARE);
    }
} 
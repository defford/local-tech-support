package com.localtechsupport.controller;

import com.localtechsupport.dto.request.*;
import com.localtechsupport.dto.response.*;
import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.TechnicianStatus;
import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.service.TechnicianService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for Technician management operations.
 * 
 * Provides comprehensive API endpoints for:
 * - Technician CRUD operations
 * - Status management
 * - Skill management
 * - Search and filtering
 * - Statistics and reporting
 * - Workload analysis
 */
@RestController
@RequestMapping("/api/technicians")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TechnicianController {

    private final TechnicianService technicianService;

    @Autowired
    public TechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Create a new technician.
     */
    @PostMapping
    public ResponseEntity<TechnicianResponse> createTechnician(@Valid @RequestBody CreateTechnicianRequest request) {
        Technician technician = technicianService.createTechnician(request);
        TechnicianResponse response = convertToResponse(technician);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a technician by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponse> getTechnician(@PathVariable Long id) {
        Optional<Technician> technician = technicianService.findById(id);
        
        if (technician.isPresent()) {
            TechnicianResponse response = convertToResponse(technician.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update an existing technician.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TechnicianResponse> updateTechnician(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTechnicianRequest request) {
        
        Technician technician = technicianService.updateTechnician(id, request);
        TechnicianResponse response = convertToResponse(technician);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a technician by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Long id) {
        technicianService.deleteTechnician(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all technicians with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<TechnicianResponse>> getAllTechnicians(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) TechnicianStatus status) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Technician> technicians;
        
        if (status != null) {
            technicians = technicianService.findTechniciansByStatus(status, pageable);
        } else {
            technicians = technicianService.findAllTechnicians(pageable);
        }
        
        Page<TechnicianResponse> response = technicians.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    // === STATUS MANAGEMENT ===

    /**
     * Update technician status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TechnicianResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTechnicianStatusRequest request) {
        
        Technician technician = technicianService.updateStatus(
            id, 
            request.getStatus(), 
            request.getReason(),
            request.getUpdatedBy()
        );
        
        TechnicianResponse response = convertToResponse(technician);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a technician.
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<TechnicianResponse> activateTechnician(
            @PathVariable Long id,
            @RequestParam String updatedBy) {
        
        Technician technician = technicianService.activateTechnician(id, updatedBy);
        TechnicianResponse response = convertToResponse(technician);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a technician.
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<TechnicianResponse> deactivateTechnician(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam String updatedBy) {
        
        Technician technician = technicianService.deactivateTechnician(id, reason, updatedBy);
        TechnicianResponse response = convertToResponse(technician);
        return ResponseEntity.ok(response);
    }

    /**
     * Terminate a technician.
     */
    @PostMapping("/{id}/terminate")
    public ResponseEntity<TechnicianResponse> terminateTechnician(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam String updatedBy) {
        
        Technician technician = technicianService.terminateTechnician(id, reason, updatedBy);
        TechnicianResponse response = convertToResponse(technician);
        return ResponseEntity.ok(response);
    }

    // === SEARCH AND FILTERING ===

    /**
     * Search technicians by name or email.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TechnicianResponse>> searchTechnicians(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Technician> technicians = technicianService.searchTechnicians(query, pageable);
        Page<TechnicianResponse> response = technicians.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Get technicians by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TechnicianResponse>> getTechniciansByStatus(
            @PathVariable TechnicianStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Technician> technicians = technicianService.findTechniciansByStatus(status, pageable);
        Page<TechnicianResponse> response = technicians.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    // === SKILL MANAGEMENT ===

    /**
     * Get technician skills.
     */
    @GetMapping("/{id}/skills")
    public ResponseEntity<Set<ServiceType>> getTechnicianSkills(@PathVariable Long id) {
        Set<ServiceType> skills = technicianService.getTechnicianSkills(id);
        return ResponseEntity.ok(skills);
    }

    /**
     * Add a skill to a technician.
     */
    @PostMapping("/{id}/skills")
    public ResponseEntity<TechnicianResponse> addSkill(
            @PathVariable Long id,
            @Valid @RequestBody AddSkillRequest request) {
        
        Technician technician = technicianService.addSkill(id, request.getServiceType());
        TechnicianResponse response = convertToResponse(technician);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a skill from a technician.
     */
    @DeleteMapping("/{id}/skills/{serviceType}")
    public ResponseEntity<TechnicianResponse> removeSkill(
            @PathVariable Long id,
            @PathVariable ServiceType serviceType) {
        
        Technician technician = technicianService.removeSkill(id, serviceType);
        TechnicianResponse response = convertToResponse(technician);
        return ResponseEntity.ok(response);
    }

    // === AVAILABILITY AND WORKLOAD ===

    /**
     * Get available technicians for a service type.
     */
    @GetMapping("/available")
    public ResponseEntity<List<TechnicianResponse>> getAvailableTechnicians(
            @RequestParam(required = false) ServiceType serviceType) {
        
        List<Technician> technicians;
        if (serviceType != null) {
            technicians = technicianService.getAvailableTechniciansForService(serviceType);
        } else {
            technicians = technicianService.getAvailableTechnicians();
        }
        
        List<TechnicianResponse> response = technicians.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get technician workload.
     */
    @GetMapping("/{id}/workload")
    public ResponseEntity<Map<String, Object>> getTechnicianWorkload(@PathVariable Long id) {
        long workload = technicianService.getTechnicianWorkload(id);
        Map<String, Object> response = new HashMap<>();
        response.put("technicianId", id);
        response.put("currentWorkload", workload);
        response.put("available", workload < 10); // Using default max workload
        return ResponseEntity.ok(response);
    }

    // === STATISTICS ===

    /**
     * Get technician statistics.
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = technicianService.getTechnicianStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get workload distribution.
     */
    @GetMapping("/workload-distribution")
    public ResponseEntity<Map<TechnicianStatus, Long>> getWorkloadDistribution() {
        Map<TechnicianStatus, Long> distribution = technicianService.getWorkloadDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get skill coverage.
     */
    @GetMapping("/skill-coverage")
    public ResponseEntity<Map<ServiceType, Long>> getSkillCoverage() {
        Map<ServiceType, Long> coverage = technicianService.getSkillCoverage();
        return ResponseEntity.ok(coverage);
    }

    // === EMAIL VALIDATION ===

    /**
     * Check email availability.
     */
    @GetMapping("/email/available")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean available = technicianService.isEmailAvailable(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        response.put("email", !available);
        return ResponseEntity.ok(response);
    }

    // === PRIVATE HELPER METHODS ===

    private TechnicianResponse convertToResponse(Technician technician) {
        Set<ServiceType> skills = technician.getSkills().stream()
            .map(skill -> skill.getServiceType())
            .collect(Collectors.toSet());
        
        TechnicianResponse response = new TechnicianResponse();
        response.setId(technician.getId());
        response.setFullName(technician.getFullName());
        response.setEmail(technician.getEmail());
        response.setStatus(technician.getStatus());
        response.setSkills(skills);
        response.setCurrentWorkload(technician.getCurrentLoad());
        response.setAvailable(technician.getStatus() == TechnicianStatus.ACTIVE && technician.getCurrentLoad() < 10);
        
        return response;
    }
} 
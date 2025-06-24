package com.localtechsupport.service;

import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.TechnicianStatus;
import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.entity.TechnicianSkill;
import com.localtechsupport.repository.TechnicianRepository;
import com.localtechsupport.repository.TechnicianSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for Technician management operations.
 * 
 * Provides business logic for:
 * - Technician CRUD operations with validation
 * - Status management and lifecycle tracking
 * - Search and filtering capabilities
 * - Skill management and competency tracking
 * - Workload analysis and assignment optimization
 * - Statistics and reporting
 * - Email uniqueness validation
 */
@Service
@Transactional
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final TechnicianSkillRepository technicianSkillRepository;

    // Constants for business logic
    private static final long DEFAULT_MAX_WORKLOAD = 10L;

    @Autowired
    public TechnicianService(TechnicianRepository technicianRepository, 
                           TechnicianSkillRepository technicianSkillRepository) {
        this.technicianRepository = technicianRepository;
        this.technicianSkillRepository = technicianSkillRepository;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Creates a new technician with validation and email uniqueness check.
     */
    public Technician createTechnician(String fullName, String email, Set<ServiceType> skills, String notes) {
        // Validate required fields
        validateRequiredFields(fullName, email);
        
        // Normalize email for consistency
        String normalizedEmail = normalizeEmail(email);

        // Validate email uniqueness with normalized email
        if (technicianRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Technician with email " + normalizedEmail + " already exists");
        }

        // Create technician
        Technician technician = new Technician();
        technician.setFullName(fullName.trim());
        technician.setEmail(normalizedEmail);
        technician.setStatus(TechnicianStatus.ACTIVE);

        // Save technician first
        technician = technicianRepository.save(technician);

        // Add skills if provided
        if (skills != null && !skills.isEmpty()) {
            for (ServiceType skillType : skills) {
                addSkillInternal(technician, skillType);
            }
        }

        return technician;
    }

    /**
     * Creates a new technician from DTO request.
     */
    public Technician createTechnician(com.localtechsupport.dto.request.CreateTechnicianRequest request) {
        return createTechnician(request.getFullName(), request.getEmail(), request.getSkills(), request.getNotes());
    }

    /**
     * Updates an existing technician with validation.
     */
    public Technician updateTechnician(Long technicianId, String fullName, String email, String notes) {
        Technician technician = getTechnicianById(technicianId);

        // Check email uniqueness if email is being changed
        if (email != null && !email.equalsIgnoreCase(technician.getEmail())) {
            String normalizedEmail = normalizeEmail(email);
            if (technicianRepository.existsByEmail(normalizedEmail)) {
                throw new IllegalArgumentException("Technician with email " + normalizedEmail + " already exists");
            }
            technician.setEmail(normalizedEmail);
        }

        // Update fields if provided
        if (fullName != null && !fullName.trim().isEmpty()) {
            technician.setFullName(fullName.trim());
        }

        return technicianRepository.save(technician);
    }

    /**
     * Updates an existing technician from DTO request.
     */
    public Technician updateTechnician(Long technicianId, com.localtechsupport.dto.request.UpdateTechnicianRequest request) {
        return updateTechnician(technicianId, request.getFullName(), request.getEmail(), request.getNotes());
    }

    /**
     * Deletes a technician by ID.
     */
    public void deleteTechnician(Long technicianId) {
        Technician technician = getTechnicianById(technicianId);
        
        // Business rule: Only allow deletion of terminated technicians
        if (technician.getStatus() != TechnicianStatus.TERMINATED) {
            throw new IllegalStateException("Cannot delete technician. Please terminate first.");
        }

        // Check for active assignments
        if (technician.getCurrentLoad() > 0) {
            throw new IllegalStateException("Cannot delete technician with active ticket assignments.");
        }

        technicianRepository.deleteById(technicianId);
    }

    // === STATUS MANAGEMENT ===

    /**
     * Updates technician status with validation.
     */
    public Technician updateStatus(Long technicianId, TechnicianStatus newStatus, String reason, String updatedBy) {
        Technician technician = getTechnicianById(technicianId);

        if (!isValidStatusTransition(technician.getStatus(), newStatus)) {
            throw new IllegalStateException(
                "Invalid status transition from " + technician.getStatus() + " to " + newStatus);
        }

        technician.setStatus(newStatus);
        return technicianRepository.save(technician);
    }

    /**
     * Activates a technician.
     */
    public Technician activateTechnician(Long technicianId, String updatedBy) {
        return updateStatus(technicianId, TechnicianStatus.ACTIVE, "Technician activated", updatedBy);
    }

    /**
     * Deactivates a technician.
     */
    public Technician deactivateTechnician(Long technicianId, String reason, String updatedBy) {
        return updateStatus(technicianId, TechnicianStatus.INACTIVE, reason, updatedBy);
    }

    /**
     * Terminates a technician.
     */
    public Technician terminateTechnician(Long technicianId, String reason, String updatedBy) {
        return updateStatus(technicianId, TechnicianStatus.TERMINATED, reason, updatedBy);
    }

    // === SEARCH AND RETRIEVAL METHODS ===

    @Transactional(readOnly = true)
    public Optional<Technician> findById(Long technicianId) {
        return technicianRepository.findById(technicianId);
    }

    @Transactional(readOnly = true)
    public Technician getTechnicianById(Long technicianId) {
        return technicianRepository.findById(technicianId)
            .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + technicianId));
    }

    @Transactional(readOnly = true)
    public Optional<Technician> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return technicianRepository.findByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Technician getTechnicianByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Technician not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public Page<Technician> findAllTechnicians(Pageable pageable) {
        return technicianRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Technician> findTechniciansByStatus(TechnicianStatus status, Pageable pageable) {
        return technicianRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Technician> searchTechnicians(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllTechnicians(pageable);
        }
        return technicianRepository.searchTechnicians(searchTerm.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Technician> findTechniciansByServiceType(ServiceType serviceType, Pageable pageable) {
        return technicianRepository.findByServiceType(serviceType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Technician> findAvailableTechnicians(ServiceType serviceType, Pageable pageable) {
        return technicianRepository.findByStatusAndServiceType(TechnicianStatus.ACTIVE, serviceType, pageable);
    }

    // === SKILL MANAGEMENT ===

    /**
     * Adds a skill to a technician.
     */
    public Technician addSkill(Long technicianId, ServiceType serviceType) {
        Technician technician = getTechnicianById(technicianId);
        addSkillInternal(technician, serviceType);
        return technicianRepository.findById(technicianId).orElse(technician);
    }

    /**
     * Removes a skill from a technician.
     */
    public Technician removeSkill(Long technicianId, ServiceType serviceType) {
        Technician technician = getTechnicianById(technicianId);
        
        Optional<TechnicianSkill> skillToRemove = technicianSkillRepository
            .findByTechnicianAndServiceType(technician, serviceType);
        
        if (skillToRemove.isPresent()) {
            technicianSkillRepository.delete(skillToRemove.get());
        }
        
        return technicianRepository.findById(technicianId).orElse(technician);
    }

    @Transactional(readOnly = true)
    public Set<ServiceType> getTechnicianSkills(Long technicianId) {
        Technician technician = getTechnicianById(technicianId);
        return technician.getSkills().stream()
            .map(TechnicianSkill::getServiceType)
            .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<Technician> findTechniciansWithSkill(ServiceType serviceType) {
        return technicianRepository.findByServiceType(serviceType);
    }

    // === WORKLOAD AND AVAILABILITY ===

    @Transactional(readOnly = true)
    public long getTechnicianWorkload(Long technicianId) {
        Technician technician = getTechnicianById(technicianId);
        return technician.getCurrentLoad();
    }

    @Transactional(readOnly = true)
    public List<Technician> getAvailableTechniciansForService(ServiceType serviceType) {
        return technicianRepository.findAvailableTechniciansForService(
            TechnicianStatus.ACTIVE, serviceType, DEFAULT_MAX_WORKLOAD);
    }

    @Transactional(readOnly = true)
    public List<Technician> getAvailableTechnicians() {
        return technicianRepository.findAvailableTechnicians(
            TechnicianStatus.ACTIVE, DEFAULT_MAX_WORKLOAD);
    }

    // === STATISTICS AND ANALYTICS ===

    @Transactional(readOnly = true)
    public Map<String, Object> getTechnicianStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalTechnicians", technicianRepository.count());
        stats.put("activeTechnicians", technicianRepository.countByStatus(TechnicianStatus.ACTIVE));
        stats.put("inactiveTechnicians", technicianRepository.countByStatus(TechnicianStatus.INACTIVE));
        stats.put("inTrainingTechnicians", technicianRepository.countByStatus(TechnicianStatus.IN_TRAINING));
        stats.put("onVacationTechnicians", technicianRepository.countByStatus(TechnicianStatus.ON_VACATION));
        stats.put("terminatedTechnicians", technicianRepository.countByStatus(TechnicianStatus.TERMINATED));
        
        stats.put("availableTechnicians", technicianRepository.countAvailableTechnicians(
            TechnicianStatus.ACTIVE, DEFAULT_MAX_WORKLOAD));
            
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<TechnicianStatus, Long> getWorkloadDistribution() {
        Map<TechnicianStatus, Long> distribution = new HashMap<>();
        
        for (TechnicianStatus status : TechnicianStatus.values()) {
            distribution.put(status, technicianRepository.countByStatus(status));
        }
        
        return distribution;
    }

    @Transactional(readOnly = true)
    public Map<ServiceType, Long> getSkillCoverage() {
        Map<ServiceType, Long> coverage = new HashMap<>();
        
        for (ServiceType serviceType : ServiceType.values()) {
            coverage.put(serviceType, technicianRepository.countByServiceType(serviceType));
        }
        
        return coverage;
    }

    // === EMAIL VALIDATION ===

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return !technicianRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    // === PRIVATE HELPER METHODS ===

    private void validateRequiredFields(String fullName, String email) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.length() > 3;
    }

    private boolean isValidStatusTransition(TechnicianStatus from, TechnicianStatus to) {
        if (from == to) {
            return true;
        }
        
        // Define valid transitions
        switch (from) {
            case ACTIVE:
                return to == TechnicianStatus.INACTIVE || to == TechnicianStatus.IN_TRAINING || 
                       to == TechnicianStatus.ON_VACATION || to == TechnicianStatus.TERMINATED;
            case INACTIVE:
                return to == TechnicianStatus.ACTIVE || to == TechnicianStatus.TERMINATED;
            case IN_TRAINING:
                return to == TechnicianStatus.ACTIVE || to == TechnicianStatus.INACTIVE || 
                       to == TechnicianStatus.TERMINATED;
            case ON_VACATION:
                return to == TechnicianStatus.ACTIVE || to == TechnicianStatus.INACTIVE || 
                       to == TechnicianStatus.TERMINATED;
            case TERMINATED:
                return false; // No transitions from terminated
            default:
                return false;
        }
    }

    private void addSkillInternal(Technician technician, ServiceType serviceType) {
        // Check if skill already exists
        Optional<TechnicianSkill> existingSkill = technicianSkillRepository
            .findByTechnicianAndServiceType(technician, serviceType);
        
        if (existingSkill.isEmpty()) {
            TechnicianSkill skill = new TechnicianSkill();
            skill.setTechnician(technician);
            skill.setServiceType(serviceType);
            technicianSkillRepository.save(skill);
        }
    }
} 
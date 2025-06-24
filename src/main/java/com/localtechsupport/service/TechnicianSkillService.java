package com.localtechsupport.service;

import com.localtechsupport.entity.*;
import com.localtechsupport.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for TechnicianSkill management operations.
 * 
 * Handles comprehensive business logic for:
 * - Skill assignment and validation with duplicate prevention
 * - Technician qualification checking and verification
 * - Service type coverage analysis and optimization
 * - Skill gap analysis and training recommendations
 * - Workload balancing and assignment optimization
 * - Team competency management and reporting
 */
@Service
@Transactional
public class TechnicianSkillService {

    private final TechnicianSkillRepository technicianSkillRepository;
    private final TechnicianRepository technicianRepository;

    @Autowired
    public TechnicianSkillService(TechnicianSkillRepository technicianSkillRepository,
                                 TechnicianRepository technicianRepository) {
        this.technicianSkillRepository = technicianSkillRepository;
        this.technicianRepository = technicianRepository;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Adds a skill to a technician with validation.
     */
    public TechnicianSkill addSkill(Long technicianId, ServiceType serviceType) {
        Technician technician = getTechnicianById(technicianId);
        
        if (technician.getStatus() != TechnicianStatus.ACTIVE) {
            throw new IllegalStateException("Cannot assign skills to inactive technician: " + technician.getEmail());
        }

        if (technicianSkillRepository.existsByTechnicianAndServiceType(technician, serviceType)) {
            throw new IllegalArgumentException("Technician " + technician.getFullName() + 
                " already has skill: " + serviceType);
        }

        TechnicianSkill skill = new TechnicianSkill();
        skill.setTechnician(technician);
        skill.setServiceType(serviceType);

        return technicianSkillRepository.save(skill);
    }

    /**
     * Removes a skill from a technician.
     */
    public void removeSkill(Long technicianId, ServiceType serviceType) {
        Technician technician = getTechnicianById(technicianId);
        
        TechnicianSkill skill = technicianSkillRepository.findByTechnicianAndServiceType(technician, serviceType)
            .orElseThrow(() -> new IllegalArgumentException("Technician " + technician.getFullName() + 
                " does not have skill: " + serviceType));

        technicianSkillRepository.delete(skill);
    }

    /**
     * Retrieves a skill by ID.
     */
    @Transactional(readOnly = true)
    public Optional<TechnicianSkill> findById(Long skillId) {
        return technicianSkillRepository.findById(skillId);
    }

    /**
     * Gets a skill by ID with exception if not found.
     */
    @Transactional(readOnly = true)
    public TechnicianSkill getSkillById(Long skillId) {
        return technicianSkillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("Skill not found with ID: " + skillId));
    }

    /**
     * Retrieves all skills with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TechnicianSkill> findAllSkills(Pageable pageable) {
        return technicianSkillRepository.findAll(pageable);
    }

    // === TECHNICIAN-BASED OPERATIONS ===

    /**
     * Retrieves all skills for a specific technician.
     */
    @Transactional(readOnly = true)
    public Page<TechnicianSkill> findSkillsByTechnician(Long technicianId, Pageable pageable) {
        Technician technician = getTechnicianById(technicianId);
        return technicianSkillRepository.findByTechnician(technician, pageable);
    }

    /**
     * Gets all service types that a technician is qualified for.
     */
    @Transactional(readOnly = true)
    public List<ServiceType> getTechnicianServiceTypes(Long technicianId) {
        Technician technician = getTechnicianById(technicianId);
        return technicianSkillRepository.findServiceTypesByTechnician(technician);
    }

    /**
     * Counts the number of skills a technician has.
     */
    @Transactional(readOnly = true)
    public long countSkillsByTechnician(Long technicianId) {
        Technician technician = getTechnicianById(technicianId);
        return technicianSkillRepository.countByTechnician(technician);
    }

    /**
     * Checks if a technician is qualified for a specific service type.
     */
    @Transactional(readOnly = true)
    public boolean isTechnicianQualified(Long technicianId, ServiceType serviceType) {
        Technician technician = getTechnicianById(technicianId);
        return technicianSkillRepository.isTechnicianQualifiedFor(technician, serviceType);
    }

    /**
     * Gets missing skills for a technician (skills they don't have).
     */
    @Transactional(readOnly = true)
    public List<ServiceType> getMissingSkillsForTechnician(Long technicianId) {
        Technician technician = getTechnicianById(technicianId);
        return technicianSkillRepository.findMissingSkillsForTechnician(technician);
    }

    // === SERVICE TYPE OPERATIONS ===

    /**
     * Finds all technicians qualified for a specific service type.
     */
    @Transactional(readOnly = true)
    public Page<Technician> findTechniciansByServiceType(ServiceType serviceType, Pageable pageable) {
        return technicianSkillRepository.findTechniciansWithSkill(serviceType, pageable);
    }

    /**
     * Counts technicians qualified for a specific service type.
     */
    @Transactional(readOnly = true)
    public long countTechniciansWithSkill(ServiceType serviceType) {
        return technicianSkillRepository.countTechniciansWithSkill(serviceType);
    }

    /**
     * Finds the best qualified technicians for a service type (least busy first).
     */
    @Transactional(readOnly = true)
    public List<Technician> findBestQualifiedTechnicians(ServiceType serviceType) {
        return technicianSkillRepository.findBestQualifiedTechnicians(serviceType);
    }

    /**
     * Finds technicians with multiple specific skills.
     */
    @Transactional(readOnly = true)
    public List<Technician> findTechniciansWithAllSkills(List<ServiceType> serviceTypes) {
        if (serviceTypes == null || serviceTypes.isEmpty()) {
            throw new IllegalArgumentException("Service types list cannot be null or empty");
        }
        return technicianSkillRepository.findTechniciansWithAllSkills(serviceTypes, serviceTypes.size());
    }

    /**
     * Finds technicians with any of the specified skills.
     */
    @Transactional(readOnly = true)
    public Page<Technician> findTechniciansWithAnySkill(List<ServiceType> serviceTypes, Pageable pageable) {
        if (serviceTypes == null || serviceTypes.isEmpty()) {
            throw new IllegalArgumentException("Service types list cannot be null or empty");
        }
        return technicianSkillRepository.findTechniciansWithAnySkill(serviceTypes, pageable);
    }

    // === ANALYTICS AND REPORTING ===

    /**
     * Gets comprehensive skill coverage analysis.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSkillCoverage() {
        List<Object[]> coverageData = technicianSkillRepository.getSkillCoverageAnalysis();
        
        Map<String, Object> coverage = new HashMap<>();
        Map<String, Long> skillCounts = new HashMap<>();
        
        for (Object[] row : coverageData) {
            ServiceType serviceType = (ServiceType) row[0];
            Long count = (Long) row[1];
            skillCounts.put(serviceType.toString(), count);
        }
        
        coverage.put("skillCounts", skillCounts);
        coverage.put("totalSkillTypes", skillCounts.size());
        coverage.put("averageTechniciansPerSkill", 
            skillCounts.values().stream().mapToDouble(Long::doubleValue).average().orElse(0.0));
        
        return coverage;
    }

    /**
     * Gets technician skill distribution statistics.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTechnicianSkillDistribution() {
        List<Object[]> distribution = technicianSkillRepository.getTechnicianSkillCounts();
        
        return distribution.stream()
            .map(row -> {
                Technician technician = (Technician) row[0];
                Long skillCount = (Long) row[1];
                
                Map<String, Object> technicianData = new HashMap<>();
                technicianData.put("technicianId", technician.getId());
                technicianData.put("technicianName", technician.getFullName());
                technicianData.put("skillCount", skillCount);
                technicianData.put("email", technician.getEmail());
                technicianData.put("status", technician.getStatus().toString());
                
                return technicianData;
            })
            .collect(Collectors.toList());
    }

    /**
     * Finds technicians with no skills (training needs assessment).
     */
    @Transactional(readOnly = true)
    public Page<Technician> findTechniciansWithNoSkills(Pageable pageable) {
        return technicianSkillRepository.findTechniciansWithNoSkills(pageable);
    }

    /**
     * Finds technicians with only one skill (cross-training candidates).
     */
    @Transactional(readOnly = true)
    public List<Technician> findTechniciansWithSingleSkill() {
        return technicianSkillRepository.findTechniciansWithSingleSkill();
    }

    /**
     * Finds fully qualified technicians (with all available skills).
     */
    @Transactional(readOnly = true)
    public List<Technician> findFullyQualifiedTechnicians() {
        return technicianSkillRepository.findFullyQualifiedTechnicians();
    }

    /**
     * Finds most versatile technicians (with multiple skills).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findMostVersatileTechnicians(int minSkills) {
        List<Object[]> versatileData = technicianSkillRepository.findMostVersatileTechnicians(minSkills);
        
        return versatileData.stream()
            .map(row -> {
                Technician technician = (Technician) row[0];
                Long skillCount = (Long) row[1];
                
                Map<String, Object> data = new HashMap<>();
                data.put("technicianId", technician.getId());
                data.put("technicianName", technician.getFullName());
                data.put("skillCount", skillCount);
                data.put("email", technician.getEmail());
                
                return data;
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets skill redundancy analysis (technician count per skill).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSkillRedundancyAnalysis() {
        List<Object[]> redundancyData = technicianSkillRepository.getSkillRedundancyAnalysis();
        
        return redundancyData.stream()
            .map(row -> {
                ServiceType serviceType = (ServiceType) row[0];
                Long technicianCount = (Long) row[1];
                
                Map<String, Object> data = new HashMap<>();
                data.put("serviceType", serviceType.toString());
                data.put("technicianCount", technicianCount);
                data.put("redundancyLevel", technicianCount > 3 ? "HIGH" : 
                                         technicianCount > 1 ? "MEDIUM" : "LOW");
                
                return data;
            })
            .collect(Collectors.toList());
    }

    /**
     * Finds underrepresented skills (skills with few technicians).
     */
    @Transactional(readOnly = true)
    public List<ServiceType> findUnderrepresentedSkills(int maxTechnicians) {
        return technicianSkillRepository.findUnderrepresentedSkills(maxTechnicians);
    }

    /**
     * Finds overrepresented skills (skills with many technicians).
     */
    @Transactional(readOnly = true)
    public List<ServiceType> findOverrepresentedSkills(int minTechnicians) {
        return technicianSkillRepository.findOverrepresentedSkills(minTechnicians);
    }

    /**
     * Gets comprehensive team skill composition summary.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTeamSkillComposition() {
        List<Object[]> compositionData = technicianSkillRepository.getSkillCompositionSummary();
        
        if (compositionData.isEmpty()) {
            Map<String, Object> emptyComposition = new HashMap<>();
            emptyComposition.put("totalTechnicians", 0L);
            emptyComposition.put("totalSkillTypes", 0L);
            emptyComposition.put("totalSkillAssignments", 0L);
            emptyComposition.put("averageSkillsPerTechnician", 0.0);
            return emptyComposition;
        }
        
        Object[] row = compositionData.get(0);
        Long totalTechnicians = (Long) row[0];
        Long totalSkillTypes = (Long) row[1];
        Long totalSkillAssignments = (Long) row[2];
        
        Map<String, Object> composition = new HashMap<>();
        composition.put("totalTechnicians", totalTechnicians);
        composition.put("totalSkillTypes", totalSkillTypes);
        composition.put("totalSkillAssignments", totalSkillAssignments);
        composition.put("averageSkillsPerTechnician", 
            totalTechnicians > 0 ? (double) totalSkillAssignments / totalTechnicians : 0.0);
        
        return composition;
    }

    // === BATCH OPERATIONS ===

    /**
     * Adds multiple skills to a technician.
     */
    public List<TechnicianSkill> addMultipleSkills(Long technicianId, List<ServiceType> serviceTypes) {
        if (serviceTypes == null || serviceTypes.isEmpty()) {
            throw new IllegalArgumentException("Service types list cannot be null or empty");
        }

        List<TechnicianSkill> addedSkills = new ArrayList<>();
        for (ServiceType serviceType : serviceTypes) {
            try {
                TechnicianSkill skill = addSkill(technicianId, serviceType);
                addedSkills.add(skill);
            } catch (IllegalArgumentException e) {
                // Skip duplicates but continue with others
                continue;
            }
        }
        
        return addedSkills;
    }

    /**
     * Removes all skills from a technician.
     */
    public void removeAllSkills(Long technicianId) {
        Technician technician = getTechnicianById(technicianId);
        technicianSkillRepository.deleteByTechnician(technician);
    }

    // === VALIDATION METHODS ===

    /**
     * Validates if a skill assignment would create a duplicate.
     */
    @Transactional(readOnly = true)
    public boolean wouldCreateDuplicate(Long technicianId, ServiceType serviceType) {
        Technician technician = getTechnicianById(technicianId);
        return technicianSkillRepository.countDuplicateSkills(technician, serviceType) > 0;
    }

    // === PRIVATE HELPER METHODS ===

    private Technician getTechnicianById(Long technicianId) {
        return technicianRepository.findById(technicianId)
            .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + technicianId));
    }
} 
package com.localtechsupport.controller;

import com.localtechsupport.dto.request.AddSkillRequest;
import com.localtechsupport.dto.response.SkillResponse;
import com.localtechsupport.dto.response.SkillCoverageResponse;
import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.entity.Technician;
import com.localtechsupport.entity.TechnicianSkill;
import com.localtechsupport.service.TechnicianSkillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for TechnicianSkill management operations.
 * 
 * Provides comprehensive API endpoints for:
 * - Skill assignment and removal with validation
 * - Technician qualification checking and verification
 * - Service type coverage analysis and optimization
 * - Skill analytics and team competency reporting
 * - Bulk operations and batch processing
 */
@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "*")
public class TechnicianSkillController {

    private final TechnicianSkillService technicianSkillService;

    @Autowired
    public TechnicianSkillController(TechnicianSkillService technicianSkillService) {
        this.technicianSkillService = technicianSkillService;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Adds a skill to a technician.
     * POST /api/skills
     */
    @PostMapping
    public ResponseEntity<SkillResponse> addSkill(@Valid @RequestBody AddSkillRequest request) {
        try {
            TechnicianSkill skill = technicianSkillService.addSkill(request.getTechnicianId(), request.getServiceType());
            SkillResponse response = SkillResponse.fromEntity(skill);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Removes a skill from a technician.
     * DELETE /api/skills/technician/{technicianId}/service-type/{serviceType}
     */
    @DeleteMapping("/technician/{technicianId}/service-type/{serviceType}")
    public ResponseEntity<Void> removeSkill(@PathVariable Long technicianId, 
                                          @PathVariable ServiceType serviceType) {
        try {
            technicianSkillService.removeSkill(technicianId, serviceType);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gets a skill by ID.
     * GET /api/skills/{skillId}
     */
    @GetMapping("/{skillId}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long skillId) {
        Optional<TechnicianSkill> skill = technicianSkillService.findById(skillId);
        if (skill.isPresent()) {
            SkillResponse response = SkillResponse.fromEntity(skill.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gets all skills with pagination.
     * GET /api/skills
     */
    @GetMapping
    public ResponseEntity<Page<SkillResponse>> getAllSkills(@PageableDefault(size = 20) Pageable pageable) {
        Page<TechnicianSkill> skills = technicianSkillService.findAllSkills(pageable);
        Page<SkillResponse> responses = skills.map(SkillResponse::fromEntity);
        return ResponseEntity.ok(responses);
    }

    // === TECHNICIAN-BASED OPERATIONS ===

    /**
     * Gets all skills for a specific technician.
     * GET /api/skills/technician/{technicianId}
     */
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<Page<SkillResponse>> getSkillsByTechnician(@PathVariable Long technicianId,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<TechnicianSkill> skills = technicianSkillService.findSkillsByTechnician(technicianId, pageable);
            Page<SkillResponse> responses = skills.map(SkillResponse::fromEntity);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gets all service types that a technician is qualified for.
     * GET /api/skills/technician/{technicianId}/service-types
     */
    @GetMapping("/technician/{technicianId}/service-types")
    public ResponseEntity<List<ServiceType>> getTechnicianServiceTypes(@PathVariable Long technicianId) {
        try {
            List<ServiceType> serviceTypes = technicianSkillService.getTechnicianServiceTypes(technicianId);
            return ResponseEntity.ok(serviceTypes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Counts the number of skills a technician has.
     * GET /api/skills/technician/{technicianId}/count
     */
    @GetMapping("/technician/{technicianId}/count")
    public ResponseEntity<Long> countSkillsByTechnician(@PathVariable Long technicianId) {
        try {
            long count = technicianSkillService.countSkillsByTechnician(technicianId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Checks if a technician is qualified for a specific service type.
     * GET /api/skills/technician/{technicianId}/qualified/{serviceType}
     */
    @GetMapping("/technician/{technicianId}/qualified/{serviceType}")
    public ResponseEntity<Boolean> isTechnicianQualified(@PathVariable Long technicianId,
                                                       @PathVariable ServiceType serviceType) {
        try {
            boolean qualified = technicianSkillService.isTechnicianQualified(technicianId, serviceType);
            return ResponseEntity.ok(qualified);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === SERVICE TYPE OPERATIONS ===

    /**
     * Finds all technicians qualified for a specific service type.
     * GET /api/skills/service-type/{serviceType}/technicians
     */
    @GetMapping("/service-type/{serviceType}/technicians")
    public ResponseEntity<Page<Technician>> getTechniciansByServiceType(@PathVariable ServiceType serviceType,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        Page<Technician> technicians = technicianSkillService.findTechniciansByServiceType(serviceType, pageable);
        return ResponseEntity.ok(technicians);
    }

    /**
     * Counts technicians qualified for a specific service type.
     * GET /api/skills/service-type/{serviceType}/count
     */
    @GetMapping("/service-type/{serviceType}/count")
    public ResponseEntity<Long> countTechniciansWithSkill(@PathVariable ServiceType serviceType) {
        long count = technicianSkillService.countTechniciansWithSkill(serviceType);
        return ResponseEntity.ok(count);
    }

    // === ANALYTICS AND REPORTING ===

    /**
     * Gets comprehensive skill coverage analysis.
     * GET /api/skills/coverage
     */
    @GetMapping("/coverage")
    public ResponseEntity<SkillCoverageResponse> getSkillCoverage() {
        Map<String, Object> coverage = technicianSkillService.getSkillCoverage();
        Map<String, Object> composition = technicianSkillService.getTeamSkillComposition();
        
        SkillCoverageResponse response = SkillCoverageResponse.createBasic(coverage, composition);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets team skill composition summary.
     * GET /api/skills/composition
     */
    @GetMapping("/composition")
    public ResponseEntity<Map<String, Object>> getTeamSkillComposition() {
        Map<String, Object> composition = technicianSkillService.getTeamSkillComposition();
        return ResponseEntity.ok(composition);
    }

    // === BATCH OPERATIONS ===

    /**
     * Adds multiple skills to a technician.
     * POST /api/skills/technician/{technicianId}/batch
     */
    @PostMapping("/technician/{technicianId}/batch")
    public ResponseEntity<List<SkillResponse>> addMultipleSkills(@PathVariable Long technicianId,
                                                               @RequestBody List<ServiceType> serviceTypes) {
        try {
            List<TechnicianSkill> skills = technicianSkillService.addMultipleSkills(technicianId, serviceTypes);
            List<SkillResponse> responses = skills.stream()
                .map(SkillResponse::fromEntity)
                .toList();
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Removes all skills from a technician.
     * DELETE /api/skills/technician/{technicianId}/all
     */
    @DeleteMapping("/technician/{technicianId}/all")
    public ResponseEntity<Void> removeAllSkills(@PathVariable Long technicianId) {
        try {
            technicianSkillService.removeAllSkills(technicianId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === VALIDATION OPERATIONS ===

    /**
     * Validates if a skill assignment would create a duplicate.
     * GET /api/skills/validate/technician/{technicianId}/service-type/{serviceType}
     */
    @GetMapping("/validate/technician/{technicianId}/service-type/{serviceType}")
    public ResponseEntity<Boolean> validateSkillAssignment(@PathVariable Long technicianId,
                                                         @PathVariable ServiceType serviceType) {
        try {
            boolean wouldCreateDuplicate = technicianSkillService.wouldCreateDuplicate(technicianId, serviceType);
            return ResponseEntity.ok(!wouldCreateDuplicate); // Return true if assignment is valid
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === ERROR HANDLING ===

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
} 
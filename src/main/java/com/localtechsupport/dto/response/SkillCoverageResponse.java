package com.localtechsupport.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

/**
 * Response DTO for skill coverage analytics and statistics.
 * 
 * Provides:
 * - Overall skill coverage metrics
 * - Service type distribution analysis
 * - Team competency insights
 * - Training needs assessment data
 */
@Data
@NoArgsConstructor
public class SkillCoverageResponse {

    // Overall coverage metrics
    private int totalSkillTypes;
    private long totalTechnicians;
    private long totalSkillAssignments;
    private double averageSkillsPerTechnician;
    private double skillUtilizationRate;

    // Service type distribution
    private Map<String, Long> skillCounts;
    private double averageTechniciansPerSkill;

    // Gap analysis
    private List<String> underrepresentedSkills;
    private List<String> overrepresentedSkills;
    private long techniciansWithNoSkills;
    private long techniciansWithSingleSkill;

    // Team insights
    private List<VersatileTechnicianData> mostVersatileTechnicians;
    private List<SkillRedundancyData> redundancyAnalysis;

    /**
     * Data class for versatile technician information.
     */
    @Data
    @NoArgsConstructor
    public static class VersatileTechnicianData {
        private Long technicianId;
        private String technicianName;
        private String email;
        private int skillCount;

        public VersatileTechnicianData(Long technicianId, String technicianName, String email, int skillCount) {
            this.technicianId = technicianId;
            this.technicianName = technicianName;
            this.email = email;
            this.skillCount = skillCount;
        }
    }

    /**
     * Data class for skill redundancy analysis.
     */
    @Data
    @NoArgsConstructor
    public static class SkillRedundancyData {
        private String serviceType;
        private long technicianCount;
        private String redundancyLevel;

        public SkillRedundancyData(String serviceType, long technicianCount, String redundancyLevel) {
            this.serviceType = serviceType;
            this.technicianCount = technicianCount;
            this.redundancyLevel = redundancyLevel;
        }
    }

    /**
     * Creates a comprehensive coverage response with all metrics.
     */
    public static SkillCoverageResponse createComprehensive(
            Map<String, Object> coverage,
            Map<String, Object> composition,
            List<Map<String, Object>> versatileTechnicians,
            List<Map<String, Object>> redundancyAnalysis,
            List<String> underrepresented,
            List<String> overrepresented,
            long noSkillsCount,
            long singleSkillCount) {

        SkillCoverageResponse response = new SkillCoverageResponse();
        
        // Set overall metrics
        response.setTotalSkillTypes((Integer) coverage.getOrDefault("totalSkillTypes", 0));
        response.setSkillCounts((Map<String, Long>) coverage.get("skillCounts"));
        response.setAverageTechniciansPerSkill((Double) coverage.getOrDefault("averageTechniciansPerSkill", 0.0));
        
        // Set composition metrics
        response.setTotalTechnicians((Long) composition.getOrDefault("totalTechnicians", 0L));
        response.setTotalSkillAssignments((Long) composition.getOrDefault("totalSkillAssignments", 0L));
        response.setAverageSkillsPerTechnician((Double) composition.getOrDefault("averageSkillsPerTechnician", 0.0));
        response.setSkillUtilizationRate((Double) composition.getOrDefault("skillUtilizationRate", 0.0));
        
        // Set gap analysis
        response.setUnderrepresentedSkills(underrepresented);
        response.setOverrepresentedSkills(overrepresented);
        response.setTechniciansWithNoSkills(noSkillsCount);
        response.setTechniciansWithSingleSkill(singleSkillCount);
        
        // Set versatile technicians
        response.setMostVersatileTechnicians(
            versatileTechnicians.stream()
                .map(data -> new VersatileTechnicianData(
                    (Long) data.get("technicianId"),
                    (String) data.get("technicianName"),
                    (String) data.get("email"),
                    ((Long) data.get("skillCount")).intValue()))
                .toList()
        );
        
        // Set redundancy analysis
        response.setRedundancyAnalysis(
            redundancyAnalysis.stream()
                .map(data -> new SkillRedundancyData(
                    (String) data.get("serviceType"),
                    (Long) data.get("technicianCount"),
                    (String) data.get("redundancyLevel")))
                .toList()
        );
        
        return response;
    }

    /**
     * Creates a basic coverage response with essential metrics only.
     */
    public static SkillCoverageResponse createBasic(Map<String, Object> coverage, Map<String, Object> composition) {
        SkillCoverageResponse response = new SkillCoverageResponse();
        
        response.setTotalSkillTypes((Integer) coverage.getOrDefault("totalSkillTypes", 0));
        response.setSkillCounts((Map<String, Long>) coverage.get("skillCounts"));
        response.setAverageTechniciansPerSkill((Double) coverage.getOrDefault("averageTechniciansPerSkill", 0.0));
        
        response.setTotalTechnicians((Long) composition.getOrDefault("totalTechnicians", 0L));
        response.setTotalSkillAssignments((Long) composition.getOrDefault("totalSkillAssignments", 0L));
        response.setAverageSkillsPerTechnician((Double) composition.getOrDefault("averageSkillsPerTechnician", 0.0));
        response.setSkillUtilizationRate((Double) composition.getOrDefault("skillUtilizationRate", 0.0));
        
        return response;
    }

    /**
     * Returns a string representation for logging and debugging.
     */
    @Override
    public String toString() {
        return String.format("SkillCoverageResponse{totalSkillTypes=%d, totalTechnicians=%d, utilization=%.2f%%}", 
                           totalSkillTypes, totalTechnicians, skillUtilizationRate * 100);
    }
} 
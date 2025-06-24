package com.localtechsupport.dto.response;

import com.localtechsupport.entity.ServiceType;
import com.localtechsupport.entity.TechnicianSkill;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for TechnicianSkill information.
 * 
 * Provides:
 * - Complete skill assignment details
 * - Technician summary information
 * - Service type specification
 * - JSON serialization for API responses
 */
@Data
@NoArgsConstructor
public class SkillResponse {

    private Long skillId;
    private Long technicianId;
    private String technicianName;
    private String technicianEmail;
    private String technicianStatus;
    private ServiceType serviceType;
    private String serviceTypeDescription;

    /**
     * Creates SkillResponse from TechnicianSkill entity.
     */
    public static SkillResponse fromEntity(TechnicianSkill skill) {
        SkillResponse response = new SkillResponse();
        response.setSkillId(skill.getId());
        response.setTechnicianId(skill.getTechnician().getId());
        response.setTechnicianName(skill.getTechnician().getFullName());
        response.setTechnicianEmail(skill.getTechnician().getEmail());
        response.setTechnicianStatus(skill.getTechnician().getStatus().toString());
        response.setServiceType(skill.getServiceType());
        response.setServiceTypeDescription(getServiceTypeDescription(skill.getServiceType()));
        return response;
    }

    /**
     * Provides human-readable descriptions for service types.
     */
    private static String getServiceTypeDescription(ServiceType serviceType) {
        switch (serviceType) {
            case HARDWARE:
                return "Hardware Installation & Repair";
            case SOFTWARE:
                return "Software Configuration & Support";
            default:
                return serviceType.toString();
        }
    }

    /**
     * Creates a simplified skill response with minimal data.
     */
    public static SkillResponse createSimple(Long skillId, Long technicianId, String technicianName, ServiceType serviceType) {
        SkillResponse response = new SkillResponse();
        response.setSkillId(skillId);
        response.setTechnicianId(technicianId);
        response.setTechnicianName(technicianName);
        response.setServiceType(serviceType);
        response.setServiceTypeDescription(getServiceTypeDescription(serviceType));
        return response;
    }

    /**
     * Returns a string representation for logging and debugging.
     */
    @Override
    public String toString() {
        return String.format("SkillResponse{skillId=%d, technicianName='%s', serviceType=%s}", 
                           skillId, technicianName, serviceType);
    }
} 
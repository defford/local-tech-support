package com.localtechsupport.dto.request;

import com.localtechsupport.entity.ServiceType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request DTO for creating a new technician.
 * Contains all required and optional fields for technician registration.
 */
public class CreateTechnicianRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    private Set<ServiceType> skills;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Default constructor
    public CreateTechnicianRequest() {}

    // Constructor with required fields
    public CreateTechnicianRequest(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    // Constructor with all fields
    public CreateTechnicianRequest(String fullName, String email, Set<ServiceType> skills, String notes) {
        this.fullName = fullName;
        this.email = email;
        this.skills = skills;
        this.notes = notes;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<ServiceType> getSkills() {
        return skills;
    }

    public void setSkills(Set<ServiceType> skills) {
        this.skills = skills;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreateTechnicianRequest{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", skills=" + skills +
                ", notes='" + (notes != null ? notes.substring(0, Math.min(notes.length(), 50)) + "..." : null) + '\'' +
                '}';
    }
} 
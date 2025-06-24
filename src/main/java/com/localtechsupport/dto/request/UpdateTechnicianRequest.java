package com.localtechsupport.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing technician.
 * All fields are optional to support partial updates.
 */
public class UpdateTechnicianRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Default constructor
    public UpdateTechnicianRequest() {}

    // Constructor with all fields
    public UpdateTechnicianRequest(String fullName, String email, String notes) {
        this.fullName = fullName;
        this.email = email;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateTechnicianRequest{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", notes='" + (notes != null ? notes.substring(0, Math.min(notes.length(), 50)) + "..." : null) + '\'' +
                '}';
    }
} 
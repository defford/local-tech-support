package com.localtechsupport.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing client.
 * All fields are optional to support partial updates.
 */
public class UpdateClientRequest {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Default constructor
    public UpdateClientRequest() {}

    // Constructor with all fields
    public UpdateClientRequest(String firstName, String lastName, String email, 
                             String phone, String address, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.notes = notes;
    }

    // Utility methods for checking if fields are provided
    public boolean hasFirstName() {
        return firstName != null && !firstName.trim().isEmpty();
    }

    public boolean hasLastName() {
        return lastName != null && !lastName.trim().isEmpty();
    }

    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }

    public boolean hasAddress() {
        return address != null && !address.trim().isEmpty();
    }

    public boolean hasNotes() {
        return notes != null;
    }

    public boolean hasAnyUpdates() {
        return hasFirstName() || hasLastName() || hasEmail() || 
               hasPhone() || hasAddress() || hasNotes();
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateClientRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", notes='" + (notes != null ? notes.substring(0, Math.min(notes.length(), 50)) + "..." : null) + '\'' +
                '}';
    }
} 
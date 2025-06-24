package com.localtechsupport.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for creating new appointments.
 * 
 * Validates that:
 * - All required fields are present
 * - Start time is in the future
 * - End time is after start time (validated in service layer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private Instant startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private Instant endTime;

    private String notes;
} 
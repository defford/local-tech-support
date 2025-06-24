package com.localtechsupport.dto.response;

import com.localtechsupport.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for appointment data.
 * 
 * Includes appointment details plus summary information
 * about the assigned technician and related ticket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private TechnicianSummaryResponse technician;
    private TicketSummaryResponse ticket;
    private Instant startTime;
    private Instant endTime;
    private AppointmentStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Summary DTO for ticket information in appointment context.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketSummaryResponse {
        private Long id;
        private String description;
        private String serviceType;
        private String clientName;
        private String clientEmail;
    }
} 
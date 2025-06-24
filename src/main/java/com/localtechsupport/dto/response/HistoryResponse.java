package com.localtechsupport.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.localtechsupport.entity.TicketStatus;
import lombok.Data;

import java.time.Instant;

/**
 * Response DTO for ticket history entries.
 * 
 * Contains complete history information including ticket summary details
 * for comprehensive audit trail presentation.
 */
@Data
public class HistoryResponse {

    /**
     * Unique identifier for the history entry.
     */
    private Long id;

    /**
     * Basic information about the associated ticket.
     */
    private TicketSummary ticket;

    /**
     * The status recorded in this history entry.
     */
    private TicketStatus status;

    /**
     * Description of the change or action recorded.
     */
    private String description;

    /**
     * Timestamp when this history entry was created.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant createdAt;

    /**
     * Timestamp when this history entry was last updated.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant updatedAt;

    /**
     * User who created this history entry.
     */
    private String createdBy;

    /**
     * Nested class for ticket summary information.
     */
    @Data
    public static class TicketSummary {
        private Long id;
        private String description;
        private TicketStatus currentStatus;
        private String clientName;
        private String clientEmail;
        private String assignedTechnicianName;
        private String serviceType;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        private Instant createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        private Instant dueAt;
    }
} 
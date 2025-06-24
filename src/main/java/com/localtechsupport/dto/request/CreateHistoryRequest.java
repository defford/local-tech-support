package com.localtechsupport.dto.request;

import com.localtechsupport.entity.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for creating new ticket history entries.
 * 
 * This DTO contains the necessary information to create a ticket history record,
 * including the ticket ID, status change, description, and the user who made the change.
 */
@Data
public class CreateHistoryRequest {

    /**
     * ID of the ticket this history entry belongs to.
     * Cannot be null.
     */
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    /**
     * The status being recorded in this history entry.
     * Cannot be null.
     */
    @NotNull(message = "Status is required")
    private TicketStatus status;

    /**
     * Description of the change or action being recorded.
     * Cannot be null or empty.
     */
    @NotBlank(message = "Description is required and cannot be blank")
    private String description;

    /**
     * User ID or username of the person who made this change.
     * Cannot be null or empty.
     */
    @NotBlank(message = "Created by is required and cannot be blank")
    private String createdBy;
} 
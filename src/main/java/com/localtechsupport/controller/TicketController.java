package com.localtechsupport.controller;

import com.localtechsupport.dto.request.*;
import com.localtechsupport.dto.response.*;
import com.localtechsupport.entity.*;
import com.localtechsupport.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Ticket management operations.
 * 
 * Provides comprehensive API endpoints for:
 * - Ticket CRUD operations
 * - Assignment management
 * - Search and filtering
 * - Statistics and reporting
 */
@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Create a new support ticket.
     */
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(
            request.getClientId(),
            request.getServiceType(),
            request.getDescription()
        );
        
        TicketResponse response = mapToTicketResponse(ticket);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a ticket by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        Optional<Ticket> ticket = ticketService.findById(id);
        
        if (ticket.isPresent()) {
            TicketResponse response = mapToTicketResponse(ticket.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all tickets with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) ServiceType serviceType) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Ticket> tickets;
        
        // Apply filters based on query parameters
        if (status != null) {
            tickets = ticketService.findTicketsByStatus(status, pageable);
        } else if (clientId != null) {
            tickets = ticketService.findTicketsByClient(clientId, pageable);
        } else if (technicianId != null) {
            tickets = ticketService.findTicketsByTechnician(technicianId, pageable);
        } else if (serviceType != null) {
            tickets = ticketService.findTicketsByServiceType(serviceType, pageable);
        } else {
            tickets = ticketService.findAllTickets(pageable);
        }
        
        Page<TicketResponse> response = tickets.map(this::mapToTicketResponse);
        return ResponseEntity.ok(response);
    }

    // === ASSIGNMENT OPERATIONS ===

    /**
     * Assign a technician to a ticket.
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody AssignTechnicianRequest request) {
        
        Ticket ticket = ticketService.assignTechnician(id, request.getTechnicianId());
        TicketResponse response = mapToTicketResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Auto-assign the best available technician to a ticket.
     */
    @PostMapping("/{id}/auto-assign")
    public ResponseEntity<TicketResponse> autoAssignTechnician(@PathVariable Long id) {
        Optional<Technician> bestTechnician = ticketService.findBestTechnicianForTicket(id);
        
        if (bestTechnician.isPresent()) {
            Ticket ticket = ticketService.assignTechnician(id, bestTechnician.get().getId());
            TicketResponse response = mapToTicketResponse(ticket);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Unassign technician from a ticket.
     */
    @DeleteMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> unassignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody UnassignTechnicianRequest request) {
        
        Ticket ticket = ticketService.unassignTechnician(id, request.getReason(), request.getUpdatedBy());
        TicketResponse response = mapToTicketResponse(ticket);
        return ResponseEntity.ok(response);
    }

    // === STATUS MANAGEMENT ===

    /**
     * Update ticket status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        
        Ticket ticket = ticketService.updateStatus(
            id, 
            request.getStatus(), 
            request.getReason(), 
            request.getUpdatedBy()
        );
        
        TicketResponse response = mapToTicketResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Close a ticket with resolution notes.
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<TicketResponse> closeTicket(
            @PathVariable Long id,
            @Valid @RequestBody CloseTicketRequest request) {
        
        Ticket ticket = ticketService.closeTicket(
            id, 
            request.getResolutionNotes(), 
            request.getClosedBy()
        );
        
        TicketResponse response = mapToTicketResponse(ticket);
        return ResponseEntity.ok(response);
    }

    // === SEARCH AND FILTERING ===

    /**
     * Search tickets by description content.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TicketResponse>> searchTickets(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Ticket> tickets = ticketService.searchTickets(q, pageable);
        Page<TicketResponse> response = tickets.map(this::mapToTicketResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get unassigned tickets.
     */
    @GetMapping("/unassigned")
    public ResponseEntity<Page<TicketResponse>> getUnassignedTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Ticket> tickets = ticketService.findUnassignedTickets(pageable);
        Page<TicketResponse> response = tickets.map(this::mapToTicketResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get overdue tickets.
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TicketResponse>> getOverdueTickets() {
        List<Ticket> tickets = ticketService.findOverdueTickets();
        List<TicketResponse> response = tickets.stream()
            .map(this::mapToTicketResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get tickets due soon.
     */
    @GetMapping("/due-soon")
    public ResponseEntity<List<TicketResponse>> getTicketsDueSoon(
            @RequestParam(defaultValue = "24") int hours) {
        
        List<Ticket> tickets = ticketService.findTicketsDueSoon(hours);
        List<TicketResponse> response = tickets.stream()
            .map(this::mapToTicketResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    // === STATISTICS AND REPORTING ===

    /**
     * Get comprehensive ticket statistics.
     */
    @GetMapping("/statistics")
    public ResponseEntity<TicketStatisticsResponse> getStatistics() {
        // Gather statistics from service layer
        long totalTickets = ticketService.countTicketsByStatus(TicketStatus.OPEN) + 
                           ticketService.countTicketsByStatus(TicketStatus.CLOSED);
        long openTickets = ticketService.countTicketsByStatus(TicketStatus.OPEN);
        long closedTickets = ticketService.countTicketsByStatus(TicketStatus.CLOSED);
        long overdueTickets = ticketService.countOverdueTickets();
        long unassignedTickets = ticketService.countUnassignedTickets();
        
        // Create response with basic statistics
        TicketStatisticsResponse response = new TicketStatisticsResponse(
            totalTickets, openTickets, closedTickets, overdueTickets, unassignedTickets
        );
        
        // Add service type breakdown
        Map<String, Long> serviceTypeBreakdown = new HashMap<>();
        serviceTypeBreakdown.put("HARDWARE", ticketService.countTicketsByServiceType(ServiceType.HARDWARE));
        serviceTypeBreakdown.put("SOFTWARE", ticketService.countTicketsByServiceType(ServiceType.SOFTWARE));
        response.setTicketsByServiceType(serviceTypeBreakdown);
        
        // Add status breakdown
        Map<String, Long> statusBreakdown = new HashMap<>();
        statusBreakdown.put("OPEN", openTickets);
        statusBreakdown.put("CLOSED", closedTickets);
        response.setTicketsByStatus(statusBreakdown);
        
        return ResponseEntity.ok(response);
    }

    // === PRIVATE HELPER METHODS ===

    /**
     * Maps a Ticket entity to a TicketResponse DTO.
     */
    private TicketResponse mapToTicketResponse(Ticket ticket) {
        ClientSummaryResponse clientResponse = null;
        if (ticket.getClient() != null) {
            Client client = ticket.getClient();
            clientResponse = new ClientSummaryResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail()
            );
        }
        
        TechnicianSummaryResponse technicianResponse = null;
        if (ticket.getAssignedTechnician() != null) {
            Technician technician = ticket.getAssignedTechnician();
            technicianResponse = new TechnicianSummaryResponse(
                technician.getId(),
                technician.getFullName(),
                technician.getEmail()
            );
        }
        
        return new TicketResponse(
            ticket.getId(),
            clientResponse,
            technicianResponse,
            ticket.getServiceType(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getDueAt(),
            ticket.getCreatedAt(),
            ticket.getCreatedAt() // Using createdAt as updatedAt since entity doesn't have updatedAt field
        );
    }
} 
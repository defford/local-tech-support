package com.localtechsupport.controller;

import com.localtechsupport.dto.request.CreateHistoryRequest;
import com.localtechsupport.dto.response.HistoryResponse;
import com.localtechsupport.dto.response.HistoryStatisticsResponse;
import com.localtechsupport.entity.*;
import com.localtechsupport.service.TicketHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for TicketHistory management operations.
 * 
 * Provides comprehensive REST API endpoints for:
 * - Ticket history creation and tracking
 * - Audit trail management and compliance reporting
 * - Historical data analysis and trend identification
 * - User activity tracking and accountability
 * - Workflow pattern analysis and optimization insights
 * - Process efficiency metrics and performance monitoring
 */
@RestController
@RequestMapping("/api/history")
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    @Autowired
    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        this.ticketHistoryService = ticketHistoryService;
    }

    // === CORE CRUD ENDPOINTS ===

    /**
     * Creates a new ticket history entry.
     * POST /api/history
     */
    @PostMapping
    public ResponseEntity<HistoryResponse> createHistoryEntry(@Valid @RequestBody CreateHistoryRequest request) {
        try {
            TicketHistory history = ticketHistoryService.createHistoryEntry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(history));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a ticket history entry by ID.
     * GET /api/history/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistoryResponse> getHistoryById(@PathVariable Long id) {
        try {
            Optional<TicketHistory> history = ticketHistoryService.findById(id);
            return history.map(h -> ResponseEntity.ok(convertToResponse(h)))
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all history entries with pagination and sorting.
     * GET /api/history
     */
    @GetMapping
    public ResponseEntity<Page<HistoryResponse>> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<TicketHistory> historyPage = ticketHistoryService.findAllHistory(pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === TICKET-BASED ENDPOINTS ===

    /**
     * Retrieves complete audit trail for a specific ticket.
     * GET /api/history/ticket/{ticketId}
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<Page<HistoryResponse>> getTicketHistory(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<TicketHistory> historyPage = ticketHistoryService.findHistoryByTicket(ticketId, pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets chronological timeline for a ticket (all changes ordered by time).
     * GET /api/history/ticket/{ticketId}/timeline
     */
    @GetMapping("/ticket/{ticketId}/timeline")
    public ResponseEntity<List<HistoryResponse>> getTicketTimeline(@PathVariable Long ticketId) {
        try {
            List<TicketHistory> timeline = ticketHistoryService.getTicketTimeline(ticketId);
            List<HistoryResponse> responseList = timeline.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responseList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets the latest history entry for a specific ticket.
     * GET /api/history/ticket/{ticketId}/latest
     */
    @GetMapping("/ticket/{ticketId}/latest")
    public ResponseEntity<HistoryResponse> getLatestTicketHistory(@PathVariable Long ticketId) {
        try {
            Optional<TicketHistory> latestHistory = ticketHistoryService.getLatestHistoryForTicket(ticketId);
            return latestHistory.map(h -> ResponseEntity.ok(convertToResponse(h)))
                              .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === SEARCH AND FILTERING ENDPOINTS ===

    /**
     * Finds history entries by status.
     * GET /api/history/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<HistoryResponse>> getHistoryByStatus(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<TicketHistory> historyPage = ticketHistoryService.findHistoryByStatus(status, pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finds history entries by user who created them.
     * GET /api/history/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<HistoryResponse>> getHistoryByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<TicketHistory> historyPage = ticketHistoryService.findHistoryByCreatedBy(userId, pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Searches history entries by description content.
     * GET /api/history/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<HistoryResponse>> searchHistory(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<TicketHistory> historyPage = ticketHistoryService.searchHistoryByDescription(query, pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finds history entries within a date range.
     * GET /api/history/date-range
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<HistoryResponse>> getHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<TicketHistory> historyPage = ticketHistoryService.findHistoryByDateRange(startDate, endDate, pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finds recent changes across all tickets.
     * GET /api/history/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<HistoryResponse>> getRecentHistory(
            @RequestParam(defaultValue = "24") int hoursBack,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Page<TicketHistory> historyPage = ticketHistoryService.findRecentChanges(hoursBack, pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finds user activity within a specific time frame.
     * GET /api/history/user/{userId}/activity
     */
    @GetMapping("/user/{userId}/activity")
    public ResponseEntity<Page<HistoryResponse>> getUserActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "168") int hoursBack, // 7 days default
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Page<TicketHistory> historyPage = ticketHistoryService.findUserActivity(userId, hoursBack, pageable);
            Page<HistoryResponse> responsePage = historyPage.map(this::convertToResponse);
            
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === ANALYTICS AND STATISTICS ENDPOINTS ===

    /**
     * Gets comprehensive change statistics for reporting.
     * GET /api/history/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getChangeStatistics() {
        try {
            Map<String, Object> stats = ticketHistoryService.getStatusChangeStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets average resolution time metrics.
     * GET /api/history/resolution-metrics
     */
    @GetMapping("/resolution-metrics")
    public ResponseEntity<Map<String, Object>> getResolutionMetrics() {
        try {
            Map<String, Object> metrics = ticketHistoryService.getAverageResolutionTime();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets user activity summary for accountability reporting.
     * GET /api/history/user-activity-summary
     */
    @GetMapping("/user-activity-summary")
    public ResponseEntity<List<Map<String, Object>>> getUserActivitySummary(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "30") int daysBack) {
        
        try {
            List<Map<String, Object>> summary = ticketHistoryService.getUserActivitySummary(userId, daysBack);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets process efficiency metrics for workflow optimization.
     * GET /api/history/efficiency-metrics
     */
    @GetMapping("/efficiency-metrics")
    public ResponseEntity<Map<String, Object>> getEfficiencyMetrics() {
        try {
            Map<String, Object> metrics = ticketHistoryService.getProcessEfficiencyMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === COUNT ENDPOINTS ===

    /**
     * Counts history entries for a specific ticket.
     * GET /api/history/count/ticket/{ticketId}
     */
    @GetMapping("/count/ticket/{ticketId}")
    public ResponseEntity<Long> countHistoryForTicket(@PathVariable Long ticketId) {
        try {
            long count = ticketHistoryService.countHistoryForTicket(ticketId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Counts history entries by status.
     * GET /api/history/count/status/{status}
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countHistoryByStatus(@PathVariable TicketStatus status) {
        try {
            long count = ticketHistoryService.countHistoryByStatus(status);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Counts history entries by user.
     * GET /api/history/count/user/{userId}
     */
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<Long> countHistoryByUser(@PathVariable String userId) {
        try {
            long count = ticketHistoryService.countHistoryByUser(userId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Counts recent history entries.
     * GET /api/history/count/recent
     */
    @GetMapping("/count/recent")
    public ResponseEntity<Long> countRecentHistory(@RequestParam(defaultValue = "24") int hoursBack) {
        try {
            long count = ticketHistoryService.countRecentHistory(hoursBack);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === PRIVATE HELPER METHODS ===

    private HistoryResponse convertToResponse(TicketHistory history) {
        HistoryResponse response = new HistoryResponse();
        response.setId(history.getId());
        response.setStatus(history.getStatus());
        response.setDescription(history.getDescription());
        response.setCreatedAt(history.getCreatedAt());
        response.setUpdatedAt(history.getUpdatedAt());
        response.setCreatedBy(history.getCreatedBy());

        // Convert ticket information
        if (history.getTicket() != null) {
            HistoryResponse.TicketSummary ticketSummary = new HistoryResponse.TicketSummary();
            Ticket ticket = history.getTicket();
            
            ticketSummary.setId(ticket.getId());
            ticketSummary.setDescription(ticket.getDescription());
            ticketSummary.setCurrentStatus(ticket.getStatus());
            ticketSummary.setServiceType(ticket.getServiceType() != null ? ticket.getServiceType().toString() : null);
            ticketSummary.setCreatedAt(ticket.getCreatedAt());
            ticketSummary.setDueAt(ticket.getDueAt());
            
            // Client information
            if (ticket.getClient() != null) {
                ticketSummary.setClientName(ticket.getClient().getFullName());
                ticketSummary.setClientEmail(ticket.getClient().getEmail());
            }
            
            // Technician information
            if (ticket.getAssignedTechnician() != null) {
                ticketSummary.setAssignedTechnicianName(ticket.getAssignedTechnician().getFullName());
            }
            
            response.setTicket(ticketSummary);
        }

        return response;
    }
}
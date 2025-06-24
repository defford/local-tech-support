package com.localtechsupport.service;

import com.localtechsupport.dto.request.CreateHistoryRequest;
import com.localtechsupport.entity.*;
import com.localtechsupport.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for TicketHistory management operations.
 * 
 * Handles comprehensive business logic for:
 * - Ticket history creation and tracking with validation
 * - Audit trail management and compliance reporting
 * - Historical data analysis and trend identification
 * - User activity tracking and accountability
 * - Workflow pattern analysis and optimization insights
 * - Process efficiency metrics and performance monitoring
 */
@Service
@Transactional
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public TicketHistoryService(TicketHistoryRepository ticketHistoryRepository,
                               TicketRepository ticketRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketRepository = ticketRepository;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Creates a new ticket history entry with validation.
     */
    public TicketHistory createHistoryEntry(CreateHistoryRequest request) {
        // Validate ticket exists
        Ticket ticket = ticketRepository.findById(request.getTicketId())
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + request.getTicketId()));

        // Validate input
        validateHistoryRequest(request);

        // Create history entry
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatus(request.getStatus());
        history.setDescription(sanitizeDescription(request.getDescription()));
        history.setCreatedBy(request.getCreatedBy().trim());

        return ticketHistoryRepository.save(history);
    }

    /**
     * Retrieves a ticket history entry by ID.
     */
    @Transactional(readOnly = true)
    public Optional<TicketHistory> findById(Long historyId) {
        return ticketHistoryRepository.findById(historyId);
    }

    /**
     * Gets a ticket history entry by ID with exception if not found.
     */
    @Transactional(readOnly = true)
    public TicketHistory getHistoryById(Long historyId) {
        return ticketHistoryRepository.findById(historyId)
            .orElseThrow(() -> new IllegalArgumentException("History entry not found with ID: " + historyId));
    }

    /**
     * Retrieves all history entries with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> findAllHistory(Pageable pageable) {
        return ticketHistoryRepository.findAll(pageable);
    }

    // === TICKET-BASED OPERATIONS ===

    /**
     * Retrieves complete audit trail for a specific ticket.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> findHistoryByTicket(Long ticketId, Pageable pageable) {
        Ticket ticket = getTicketById(ticketId);
        return ticketHistoryRepository.findByTicket(ticket, pageable);
    }

    /**
     * Gets chronological timeline for a ticket (all changes ordered by time).
     */
    @Transactional(readOnly = true)
    public List<TicketHistory> getTicketTimeline(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return ticketHistoryRepository.findByTicketOrderByCreatedAtAsc(ticket);
    }

    /**
     * Gets the latest history entry for a specific ticket.
     */
    @Transactional(readOnly = true)
    public Optional<TicketHistory> getLatestHistoryForTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return ticketHistoryRepository.findTopByTicketOrderByCreatedAtDesc(ticket);
    }

    /**
     * Gets the first (initial) history entry for a specific ticket.
     */
    @Transactional(readOnly = true)
    public Optional<TicketHistory> getFirstHistoryForTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return ticketHistoryRepository.findTopByTicketOrderByCreatedAtAsc(ticket);
    }

    // === SEARCH AND FILTERING OPERATIONS ===

    /**
     * Finds history entries by status with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> findHistoryByStatus(TicketStatus status, Pageable pageable) {
        return ticketHistoryRepository.findByStatus(status, pageable);
    }

    /**
     * Finds history entries by user who created them.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> findHistoryByCreatedBy(String createdBy, Pageable pageable) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }
        return ticketHistoryRepository.findByCreatedBy(createdBy.trim(), pageable);
    }

    /**
     * Finds history entries within a specific date range.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> findHistoryByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return ticketHistoryRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    /**
     * Searches history entries by description content.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> searchHistoryByDescription(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        return ticketHistoryRepository.searchByDescription(searchTerm.trim(), pageable);
    }

    /**
     * Finds recent changes across all tickets.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> findRecentChanges(int hoursBack, Pageable pageable) {
        Instant cutoffTime = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        return ticketHistoryRepository.findRecentChanges(cutoffTime, pageable);
    }

    /**
     * Finds user activity within a specific time frame.
     */
    @Transactional(readOnly = true)
    public Page<TicketHistory> findUserActivity(String createdBy, int hoursBack, Pageable pageable) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }
        Instant cutoffTime = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        return ticketHistoryRepository.findUserActivity(createdBy.trim(), cutoffTime, pageable);
    }

    // === ANALYTICS AND STATISTICS ===

    /**
     * Gets comprehensive change statistics for reporting.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatusChangeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total entries
        stats.put("totalEntries", ticketHistoryRepository.count());
        
        // Status distribution (last 30 days)
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> statusFrequency = ticketHistoryRepository.getStatusChangeFrequency(thirtyDaysAgo);
        Map<String, Long> statusDistribution = statusFrequency.stream()
            .collect(Collectors.toMap(
                arr -> arr[0].toString(),
                arr -> (Long) arr[1]
            ));
        stats.put("statusDistribution", statusDistribution);
        
        // User activity summary (last 30 days)
        List<Object[]> userActivity = ticketHistoryRepository.getUserActivitySummary(thirtyDaysAgo);
        List<Map<String, Object>> userStats = userActivity.stream()
            .limit(10) // Top 10 users
            .map(arr -> {
                Map<String, Object> userStat = new HashMap<>();
                userStat.put("userId", arr[0].toString());
                userStat.put("activityCount", (Long) arr[1]);
                return userStat;
            })
            .collect(Collectors.toList());
        stats.put("topUsers", userStats);
        
        return stats;
    }

    /**
     * Calculates average resolution time based on history data.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAverageResolutionTime() {
        List<Object[]> lifecycleSpans = ticketHistoryRepository.getTicketLifecycleSpans();
        
        List<Long> resolutionTimes = new ArrayList<>();
        for (Object[] span : lifecycleSpans) {
            Instant start = (Instant) span[1];
            Instant end = (Instant) span[2];
            if (start != null && end != null) {
                long hours = ChronoUnit.HOURS.between(start, end);
                resolutionTimes.add(hours);
            }
        }
        
        Map<String, Object> metrics = new HashMap<>();
        if (!resolutionTimes.isEmpty()) {
            double average = resolutionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            metrics.put("averageResolutionHours", average);
            metrics.put("totalTicketsAnalyzed", resolutionTimes.size());
            
            // Calculate median
            Collections.sort(resolutionTimes);
            double median = resolutionTimes.size() % 2 == 0 
                ? (resolutionTimes.get(resolutionTimes.size()/2 - 1) + resolutionTimes.get(resolutionTimes.size()/2)) / 2.0
                : resolutionTimes.get(resolutionTimes.size()/2);
            metrics.put("medianResolutionHours", median);
        } else {
            metrics.put("averageResolutionHours", 0.0);
            metrics.put("medianResolutionHours", 0.0);
            metrics.put("totalTicketsAnalyzed", 0);
        }
        
        return metrics;
    }

    /**
     * Gets user activity summary for accountability reporting.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserActivitySummary(String userId, int daysBack) {
        Instant cutoffTime = Instant.now().minus(daysBack, ChronoUnit.DAYS);
        
        if (userId != null && !userId.trim().isEmpty()) {
            // Specific user activity
            List<TicketHistory> userHistory = ticketHistoryRepository.findUserActivity(userId.trim(), cutoffTime);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("userId", userId.trim());
            summary.put("totalActions", userHistory.size());
            summary.put("uniqueTickets", userHistory.stream().map(h -> h.getTicket().getId()).distinct().count());
            
            // Most common action type
            Map<TicketStatus, Long> actionCounts = userHistory.stream()
                .collect(Collectors.groupingBy(TicketHistory::getStatus, Collectors.counting()));
            String mostCommonAction = actionCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().toString())
                .orElse("N/A");
            summary.put("mostCommonAction", mostCommonAction);
            
            return Arrays.asList(summary);
        } else {
            // All users summary
            List<Object[]> allUserActivity = ticketHistoryRepository.getUserActivitySummary(cutoffTime);
            return allUserActivity.stream()
                .limit(20) // Top 20 users
                .map(arr -> {
                    Map<String, Object> userStat = new HashMap<>();
                    userStat.put("userId", arr[0].toString());
                    userStat.put("activityCount", (Long) arr[1]);
                    return userStat;
                })
                .collect(Collectors.toList());
        }
    }

    /**
     * Gets process efficiency metrics for workflow optimization.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProcessEfficiencyMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Tickets with most changes (potentially problematic)
        List<Object[]> mostChangedTickets = ticketHistoryRepository.getTicketsWithMostChanges(5L);
        List<Map<String, Object>> problematicTickets = mostChangedTickets.stream()
            .limit(10)
            .map(arr -> {
                Ticket ticket = (Ticket) arr[0];
                Long changeCount = (Long) arr[1];
                Map<String, Object> ticketInfo = new HashMap<>();
                ticketInfo.put("ticketId", ticket.getId());
                ticketInfo.put("description", ticket.getDescription());
                ticketInfo.put("changeCount", changeCount);
                ticketInfo.put("currentStatus", ticket.getStatus().toString());
                return ticketInfo;
            })
            .collect(Collectors.toList());
        metrics.put("mostChangedTickets", problematicTickets);
        
        // Status transition patterns
        List<Object[]> transitions = ticketHistoryRepository.getStatusTransitionPatterns();
        List<Map<String, Object>> transitionStats = transitions.stream()
            .limit(10)
            .map(arr -> {
                Map<String, Object> transition = new HashMap<>();
                transition.put("fromStatus", arr[0].toString());
                transition.put("toStatus", arr[1].toString());
                transition.put("occurrences", (Long) arr[2]);
                return transition;
            })
            .collect(Collectors.toList());
        metrics.put("statusTransitions", transitionStats);
        
        return metrics;
    }

    // === COUNT AND SUMMARY OPERATIONS ===

    /**
     * Counts history entries for a specific ticket.
     */
    @Transactional(readOnly = true)
    public long countHistoryForTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return ticketHistoryRepository.countByTicket(ticket);
    }

    /**
     * Counts history entries by status.
     */
    @Transactional(readOnly = true)
    public long countHistoryByStatus(TicketStatus status) {
        return ticketHistoryRepository.countByStatus(status);
    }

    /**
     * Counts history entries by user.
     */
    @Transactional(readOnly = true)
    public long countHistoryByUser(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }
        return ticketHistoryRepository.countByCreatedBy(createdBy.trim());
    }

    /**
     * Counts recent history entries (last N hours).
     */
    @Transactional(readOnly = true)
    public long countRecentHistory(int hoursBack) {
        Instant cutoffTime = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        return ticketHistoryRepository.countByCreatedAtAfter(cutoffTime);
    }

    // === PRIVATE HELPER METHODS ===

    private void validateHistoryRequest(CreateHistoryRequest request) {
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (request.getCreatedBy() == null || request.getCreatedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }
        if (request.getDescription().trim().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
    }

    private String sanitizeDescription(String description) {
        if (description == null) return "";
        return description.trim().length() > 1000 
            ? description.trim().substring(0, 1000) 
            : description.trim();
    }

    private Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
    }
}
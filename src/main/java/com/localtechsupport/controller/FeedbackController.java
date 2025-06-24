package com.localtechsupport.controller;

import com.localtechsupport.dto.request.CreateFeedbackRequest;
import com.localtechsupport.dto.request.UpdateFeedbackRequest;
import com.localtechsupport.dto.response.FeedbackResponse;
import com.localtechsupport.dto.response.FeedbackStatisticsResponse;
import com.localtechsupport.entity.FeedbackEntry;
import com.localtechsupport.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for FeedbackEntry management operations.
 * 
 * Provides endpoints for:
 * - Feedback CRUD operations
 * - Ticket-based feedback tracking
 * - Rating-based filtering and search
 * - Customer satisfaction analytics
 * - Feedback trends and reporting
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Create a new feedback entry.
     */
    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        FeedbackEntry feedback = feedbackService.createFeedback(
            request.getTicketId(),
            request.getRating(),
            request.getComment(),
            request.getCreatedBy()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(FeedbackResponse.from(feedback));
    }

    /**
     * Get feedback by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Long id) {
        Optional<FeedbackEntry> feedback = feedbackService.findById(id);
        return feedback.map(f -> ResponseEntity.ok(FeedbackResponse.from(f)))
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update feedback entry.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFeedbackRequest request) {
        try {
            FeedbackEntry feedback = feedbackService.updateFeedback(
                id,
                request.getRating(),
                request.getComment()
            );
            return ResponseEntity.ok(FeedbackResponse.from(feedback));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete feedback entry.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all feedback with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<FeedbackResponse>> getAllFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                   Sort.by(sortBy).ascending() : 
                   Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FeedbackEntry> feedbackPage = feedbackService.findAllFeedback(pageable);
        
        Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
        return ResponseEntity.ok(responsePage);
    }

    // === TICKET-BASED OPERATIONS ===

    /**
     * Get feedback for a specific ticket.
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbackByTicket(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                       Sort.by(sortBy).ascending() : 
                       Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<FeedbackEntry> feedbackPage = feedbackService.findFeedbackByTicket(ticketId, pageable);
            
            Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get latest feedback for a ticket.
     */
    @GetMapping("/ticket/{ticketId}/latest")
    public ResponseEntity<FeedbackResponse> getLatestFeedbackForTicket(@PathVariable Long ticketId) {
        try {
            Optional<FeedbackEntry> feedback = feedbackService.getLatestFeedbackForTicket(ticketId);
            return feedback.map(f -> ResponseEntity.ok(FeedbackResponse.from(f)))
                          .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get average rating for a ticket.
     */
    @GetMapping("/ticket/{ticketId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRatingForTicket(@PathVariable Long ticketId) {
        try {
            Double averageRating = feedbackService.getAverageRatingForTicket(ticketId);
            long feedbackCount = feedbackService.countFeedbackByTicket(ticketId);
            
            Map<String, Object> response = Map.of(
                "ticketId", ticketId,
                "averageRating", averageRating != null ? averageRating : 0.0,
                "feedbackCount", feedbackCount
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // === RATING-BASED OPERATIONS ===

    /**
     * Get feedback by specific rating.
     */
    @GetMapping("/rating/{rating}")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbackByRating(
            @PathVariable int rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                       Sort.by(sortBy).ascending() : 
                       Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<FeedbackEntry> feedbackPage = feedbackService.findFeedbackByRating(rating, pageable);
            
            Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get feedback by rating range.
     */
    @GetMapping("/rating-range")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbackByRatingRange(
            @RequestParam int minRating,
            @RequestParam int maxRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                       Sort.by(sortBy).ascending() : 
                       Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<FeedbackEntry> feedbackPage = feedbackService.findFeedbackByRatingRange(minRating, maxRating, pageable);
            
            Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get high satisfaction feedback (rating >= 4).
     */
    @GetMapping("/high-satisfaction")
    public ResponseEntity<Page<FeedbackResponse>> getHighSatisfactionFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                   Sort.by(sortBy).ascending() : 
                   Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FeedbackEntry> feedbackPage = feedbackService.findHighSatisfactionFeedback(pageable);
        
        Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get low satisfaction feedback (rating <= 2).
     */
    @GetMapping("/low-satisfaction")
    public ResponseEntity<Page<FeedbackResponse>> getLowSatisfactionFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                   Sort.by(sortBy).ascending() : 
                   Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FeedbackEntry> feedbackPage = feedbackService.findLowSatisfactionFeedback(pageable);
        
        Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
        return ResponseEntity.ok(responsePage);
    }

    // === SEARCH AND FILTERING ===

    /**
     * Search feedback by comment content.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FeedbackResponse>> searchFeedbackByComment(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                   Sort.by(sortBy).ascending() : 
                   Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FeedbackEntry> feedbackPage = feedbackService.searchFeedbackByComment(query, pageable);
        
        Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get feedback by user (createdBy).
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<FeedbackResponse>> getFeedbackByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                       Sort.by(sortBy).ascending() : 
                       Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<FeedbackEntry> feedbackPage = feedbackService.findFeedbackByCreatedBy(userId, pageable);
            
            Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
            return ResponseEntity.ok(responsePage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get recent feedback (last N days).
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<FeedbackResponse>> getRecentFeedback(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                   Sort.by(sortBy).ascending() : 
                   Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FeedbackEntry> feedbackPage = feedbackService.findRecentFeedback(days, pageable);
        
        Page<FeedbackResponse> responsePage = feedbackPage.map(FeedbackResponse::from);
        return ResponseEntity.ok(responsePage);
    }

    // === ANALYTICS AND STATISTICS ===

    /**
     * Get customer satisfaction metrics.
     */
    @GetMapping("/statistics")
    public ResponseEntity<FeedbackStatisticsResponse> getCustomerSatisfactionMetrics() {
        Map<String, Object> metrics = feedbackService.getCustomerSatisfactionMetrics();
        return ResponseEntity.ok(FeedbackStatisticsResponse.from(metrics));
    }

    /**
     * Get feedback trends for the last N days.
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getFeedbackTrends(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> trends = feedbackService.getFeedbackTrends(days);
        return ResponseEntity.ok(trends);
    }

    /**
     * Get overall average rating.
     */
    @GetMapping("/average-rating")
    public ResponseEntity<Map<String, Object>> getOverallAverageRating() {
        Double averageRating = feedbackService.getAverageRating();
        long totalCount = feedbackService.countAllFeedback();
        
        Map<String, Object> response = Map.of(
            "averageRating", averageRating != null ? averageRating : 0.0,
            "totalFeedbackCount", totalCount
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get rating distribution.
     */
    @GetMapping("/rating-distribution")
    public ResponseEntity<Map<Integer, Long>> getRatingDistribution() {
        Map<Integer, Long> distribution = feedbackService.getRatingDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get technician feedback summary.
     */
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<Map<String, Object>> getTechnicianFeedbackSummary(
            @PathVariable Long technicianId) {
        Map<String, Object> summary = feedbackService.getTechnicianFeedbackSummary(technicianId);
        return ResponseEntity.ok(summary);
    }

    // === COUNT ENDPOINTS ===

    /**
     * Get total feedback count.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTotalFeedbackCount() {
        long count = feedbackService.countAllFeedback();
        return ResponseEntity.ok(Map.of("totalCount", count));
    }

    /**
     * Get recent feedback count.
     */
    @GetMapping("/count/recent")
    public ResponseEntity<Map<String, Long>> getRecentFeedbackCount(
            @RequestParam(defaultValue = "7") int days) {
        long count = feedbackService.countRecentFeedback(days);
        return ResponseEntity.ok(Map.of("recentCount", count, "days", (long) days));
    }
} 
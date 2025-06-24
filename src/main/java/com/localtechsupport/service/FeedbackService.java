package com.localtechsupport.service;

import com.localtechsupport.entity.FeedbackEntry;
import com.localtechsupport.entity.Ticket;
import com.localtechsupport.repository.FeedbackEntryRepository;
import com.localtechsupport.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for FeedbackEntry management operations.
 * 
 * Provides business logic for:
 * - Feedback CRUD operations with validation
 * - Ticket-based feedback tracking and history
 * - Rating-based filtering and satisfaction analysis
 * - Search and filtering capabilities
 * - Statistics and analytics for customer satisfaction
 * - Feedback trends and reporting
 */
@Service
@Transactional
public class FeedbackService {

    private final FeedbackEntryRepository feedbackRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public FeedbackService(FeedbackEntryRepository feedbackRepository, TicketRepository ticketRepository) {
        this.feedbackRepository = feedbackRepository;
        this.ticketRepository = ticketRepository;
    }

    // === CORE CRUD OPERATIONS ===

    /**
     * Creates a new feedback entry with validation.
     */
    public FeedbackEntry createFeedback(Long ticketId, int rating, String comment, String createdBy) {
        // Validate ticket exists
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        // Validate required fields
        validateFeedbackFields(rating, comment, createdBy);

        // Create feedback entry
        FeedbackEntry feedback = new FeedbackEntry();
        feedback.setTicket(ticket);
        feedback.setRating(rating);
        feedback.setComment(comment.trim());
        feedback.setCreatedBy(createdBy.trim());

        return feedbackRepository.save(feedback);
    }

    /**
     * Updates an existing feedback entry with validation.
     */
    public FeedbackEntry updateFeedback(Long feedbackId, Integer rating, String comment) {
        FeedbackEntry feedback = getFeedbackById(feedbackId);

        // Update fields if provided
        if (rating != null) {
            validateRating(rating);
            feedback.setRating(rating);
        }
        
        if (comment != null && !comment.trim().isEmpty()) {
            validateComment(comment);
            feedback.setComment(comment.trim());
        }

        return feedbackRepository.save(feedback);
    }

    /**
     * Deletes a feedback entry by ID.
     */
    public void deleteFeedback(Long feedbackId) {
        FeedbackEntry feedback = getFeedbackById(feedbackId);
        feedbackRepository.deleteById(feedbackId);
    }

    // === SEARCH AND RETRIEVAL METHODS ===

    @Transactional(readOnly = true)
    public Optional<FeedbackEntry> findById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId);
    }

    @Transactional(readOnly = true)
    public FeedbackEntry getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Feedback not found with ID: " + feedbackId));
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findAllFeedback(Pageable pageable) {
        return feedbackRepository.findAll(pageable);
    }

    // === TICKET-BASED OPERATIONS ===

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findFeedbackByTicket(Long ticketId, Pageable pageable) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        return feedbackRepository.findByTicket(ticket, pageable);
    }

    @Transactional(readOnly = true)
    public List<FeedbackEntry> findFeedbackByTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        return feedbackRepository.findByTicket(ticket);
    }

    @Transactional(readOnly = true)
    public Optional<FeedbackEntry> getLatestFeedbackForTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        return feedbackRepository.findTopByTicketOrderBySubmittedAtDesc(ticket);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingForTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        return feedbackRepository.getAverageRatingForTicket(ticket);
    }

    // === SEARCH & FILTERING ===

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findFeedbackByRating(int rating, Pageable pageable) {
        validateRating(rating);
        return feedbackRepository.findByRating(rating, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findFeedbackByRatingRange(int minRating, int maxRating, Pageable pageable) {
        validateRating(minRating);
        validateRating(maxRating);
        if (minRating > maxRating) {
            throw new IllegalArgumentException("Minimum rating cannot be greater than maximum rating");
        }
        return feedbackRepository.findByRatingBetween(minRating, maxRating, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findFeedbackByCreatedBy(String createdBy, Pageable pageable) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("CreatedBy cannot be null or empty");
        }
        return feedbackRepository.findByCreatedBy(createdBy.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findFeedbackByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return feedbackRepository.findBySubmittedAtBetween(startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> searchFeedbackByComment(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllFeedback(pageable);
        }
        return feedbackRepository.searchByComment(searchTerm.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findHighSatisfactionFeedback(Pageable pageable) {
        return feedbackRepository.findByRatingGreaterThanEqual(4, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findLowSatisfactionFeedback(Pageable pageable) {
        return feedbackRepository.findByRatingLessThanEqual(2, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackEntry> findRecentFeedback(int days, Pageable pageable) {
        Instant sinceTime = Instant.now().minus(days, ChronoUnit.DAYS);
        return feedbackRepository.findRecentFeedback(sinceTime, pageable);
    }

    // === ANALYTICS & STATISTICS ===

    @Transactional(readOnly = true)
    public Double getAverageRating() {
        return feedbackRepository.getAverageRating();
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getRatingDistribution() {
        List<Object[]> results = feedbackRepository.getRatingDistribution();
        Map<Integer, Long> distribution = new HashMap<>();
        
        // Initialize all ratings 1-5 with 0 count
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        // Populate with actual counts
        for (Object[] result : results) {
            Integer rating = (Integer) result[0];
            Long count = (Long) result[1];
            distribution.put(rating, count);
        }
        
        return distribution;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFeedbackTrends(int days) {
        Instant sinceTime = Instant.now().minus(days, ChronoUnit.DAYS);
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("totalFeedback", feedbackRepository.countBySubmittedAtAfter(sinceTime));
        trends.put("averageRating", feedbackRepository.getAverageRatingSince(sinceTime));
        trends.put("highSatisfaction", feedbackRepository.countByRatingGreaterThanEqual(4));
        trends.put("lowSatisfaction", feedbackRepository.countByRatingLessThanEqual(2));
        
        // Daily trends
        List<Object[]> dailyRatings = feedbackRepository.getDailyAverageRatings(sinceTime);
        trends.put("dailyAverages", dailyRatings);
        
        return trends;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerSatisfactionMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        Double averageRating = getAverageRating();
        metrics.put("overallAverageRating", averageRating != null ? averageRating : 0.0);
        
        Map<Integer, Long> distribution = getRatingDistribution();
        metrics.put("ratingDistribution", distribution);
        
        long totalFeedback = feedbackRepository.count();
        metrics.put("totalFeedbackCount", totalFeedback);
        
        long highSatisfaction = feedbackRepository.countByRatingGreaterThanEqual(4);
        long lowSatisfaction = feedbackRepository.countByRatingLessThanEqual(2);
        
        metrics.put("highSatisfactionCount", highSatisfaction);
        metrics.put("lowSatisfactionCount", lowSatisfaction);
        
        if (totalFeedback > 0) {
            metrics.put("highSatisfactionPercentage", (double) highSatisfaction / totalFeedback * 100);
            metrics.put("lowSatisfactionPercentage", (double) lowSatisfaction / totalFeedback * 100);
        } else {
            metrics.put("highSatisfactionPercentage", 0.0);
            metrics.put("lowSatisfactionPercentage", 0.0);
        }
        
        return metrics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTechnicianFeedbackSummary(Long technicianId) {
        // Get tickets assigned to the technician and then get feedback for those tickets
        Map<String, Object> summary = new HashMap<>();
        
        // This would require joining through tickets - simplified version
        // In real implementation, we'd need to join FeedbackEntry -> Ticket -> Technician
        summary.put("message", "Technician feedback summary requires ticket-technician relationship analysis");
        summary.put("technicianId", technicianId);
        
        return summary;
    }

    // === COUNT METHODS ===

    @Transactional(readOnly = true)
    public long countAllFeedback() {
        return feedbackRepository.count();
    }

    @Transactional(readOnly = true)
    public long countFeedbackByRating(int rating) {
        validateRating(rating);
        return feedbackRepository.countByRating(rating);
    }

    @Transactional(readOnly = true)
    public long countFeedbackByTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));
        return feedbackRepository.countByTicket(ticket);
    }

    @Transactional(readOnly = true)
    public long countRecentFeedback(int days) {
        Instant sinceTime = Instant.now().minus(days, ChronoUnit.DAYS);
        return feedbackRepository.countBySubmittedAtAfter(sinceTime);
    }

    // === VALIDATION METHODS ===

    private void validateFeedbackFields(int rating, String comment, String createdBy) {
        validateRating(rating);
        validateComment(comment);
        validateCreatedBy(createdBy);
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private void validateComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be null or empty");
        }
        if (comment.trim().length() > 1000) {
            throw new IllegalArgumentException("Comment must not exceed 1000 characters");
        }
    }

    private void validateCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("CreatedBy cannot be null or empty");
        }
        if (createdBy.trim().length() > 100) {
            throw new IllegalArgumentException("CreatedBy must not exceed 100 characters");
        }
    }
} 
package com.localtechsupport.repository;

import com.localtechsupport.entity.FeedbackEntry;
import com.localtechsupport.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FeedbackEntry entity operations.
 * 
 * Provides standard CRUD operations plus custom finder methods for:
 * - Ticket-based feedback tracking and history
 * - Rating-based filtering and satisfaction analysis
 * - Time-based queries for feedback trends and reporting
 * - User activity tracking (createdBy queries)
 * - Comment content searching and analysis
 * - Customer satisfaction metrics and dashboard queries
 * - Feedback quality and trend analysis
 */
@Repository
public interface FeedbackEntryRepository extends JpaRepository<FeedbackEntry, Long> {

    // JPQL query constants
    String SEARCH_COMMENTS_QUERY = "SELECT f FROM FeedbackEntry f WHERE " +
            "LOWER(f.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%'))";

    String FIND_BY_RATING_RANGE_QUERY = "SELECT f FROM FeedbackEntry f WHERE " +
            "f.rating >= :minRating AND f.rating <= :maxRating";

    String FIND_RECENT_FEEDBACK_QUERY = "SELECT f FROM FeedbackEntry f WHERE " +
            "f.submittedAt >= :sinceTime ORDER BY f.submittedAt DESC";

    String AVERAGE_RATING_QUERY = "SELECT AVG(f.rating) FROM FeedbackEntry f";

    String AVERAGE_RATING_BY_TICKET_QUERY = "SELECT AVG(f.rating) FROM FeedbackEntry f WHERE f.ticket = :ticket";

    String AVERAGE_RATING_SINCE_QUERY = "SELECT AVG(f.rating) FROM FeedbackEntry f WHERE f.submittedAt >= :sinceTime";

    String COUNT_BY_RATING_QUERY = "SELECT COUNT(f) FROM FeedbackEntry f WHERE f.rating = :rating";

    String RATING_DISTRIBUTION_QUERY = "SELECT f.rating, COUNT(f) FROM FeedbackEntry f GROUP BY f.rating ORDER BY f.rating";

    // Ticket-based queries
    Page<FeedbackEntry> findByTicket(Ticket ticket, Pageable pageable);
    
    List<FeedbackEntry> findByTicket(Ticket ticket);
    
    // Get latest feedback for a ticket
    Optional<FeedbackEntry> findTopByTicketOrderBySubmittedAtDesc(Ticket ticket);
    
    // Get earliest feedback for a ticket
    Optional<FeedbackEntry> findTopByTicketOrderBySubmittedAtAsc(Ticket ticket);

    // Rating-based queries
    Page<FeedbackEntry> findByRating(int rating, Pageable pageable);
    
    List<FeedbackEntry> findByRating(int rating);
    
    // Rating range queries
    @Query(FIND_BY_RATING_RANGE_QUERY)
    Page<FeedbackEntry> findByRatingBetween(@Param("minRating") int minRating, 
                                          @Param("maxRating") int maxRating, 
                                          Pageable pageable);
    
    @Query(FIND_BY_RATING_RANGE_QUERY)
    List<FeedbackEntry> findByRatingBetween(@Param("minRating") int minRating, 
                                          @Param("maxRating") int maxRating);

    // High/Low satisfaction queries
    Page<FeedbackEntry> findByRatingGreaterThanEqual(int rating, Pageable pageable);
    
    List<FeedbackEntry> findByRatingGreaterThanEqual(int rating);
    
    Page<FeedbackEntry> findByRatingLessThanEqual(int rating, Pageable pageable);
    
    List<FeedbackEntry> findByRatingLessThanEqual(int rating);

    // User-based queries (createdBy)
    Page<FeedbackEntry> findByCreatedBy(String createdBy, Pageable pageable);
    
    List<FeedbackEntry> findByCreatedBy(String createdBy);
    
    // User feedback with rating filter
    Page<FeedbackEntry> findByCreatedByAndRating(String createdBy, int rating, Pageable pageable);
    
    List<FeedbackEntry> findByCreatedByAndRating(String createdBy, int rating);

    // Time-based queries
    Page<FeedbackEntry> findBySubmittedAtAfter(Instant dateTime, Pageable pageable);
    
    List<FeedbackEntry> findBySubmittedAtAfter(Instant dateTime);
    
    Page<FeedbackEntry> findBySubmittedAtBetween(Instant startDate, Instant endDate, Pageable pageable);
    
    List<FeedbackEntry> findBySubmittedAtBetween(Instant startDate, Instant endDate);

    // Recent feedback
    @Query(FIND_RECENT_FEEDBACK_QUERY)
    List<FeedbackEntry> findRecentFeedback(@Param("sinceTime") Instant sinceTime);
    
    @Query(FIND_RECENT_FEEDBACK_QUERY)
    Page<FeedbackEntry> findRecentFeedback(@Param("sinceTime") Instant sinceTime, Pageable pageable);

    // Comment content search
    @Query(SEARCH_COMMENTS_QUERY)
    Page<FeedbackEntry> searchByComment(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query(SEARCH_COMMENTS_QUERY)
    List<FeedbackEntry> searchByComment(@Param("searchTerm") String searchTerm);

    // Combined filtering queries
    Page<FeedbackEntry> findByTicketAndRating(Ticket ticket, int rating, Pageable pageable);
    
    Page<FeedbackEntry> findByTicketAndRatingGreaterThanEqual(Ticket ticket, int rating, Pageable pageable);
    
    Page<FeedbackEntry> findByRatingAndSubmittedAtAfter(int rating, Instant dateTime, Pageable pageable);
    
    Page<FeedbackEntry> findByCreatedByAndSubmittedAtBetween(String createdBy, 
                                                           Instant startDate, 
                                                           Instant endDate, 
                                                           Pageable pageable);

    // Count queries for dashboard/stats
    long countByRating(int rating);
    
    @Query(COUNT_BY_RATING_QUERY)
    long countByRatingValue(@Param("rating") int rating);
    
    long countByTicket(Ticket ticket);
    
    long countByCreatedBy(String createdBy);
    
    long countBySubmittedAtAfter(Instant dateTime);
    
    long countByRatingGreaterThanEqual(int rating);
    
    long countByRatingLessThanEqual(int rating);

    // Satisfaction metrics
    @Query(AVERAGE_RATING_QUERY)
    Double getAverageRating();
    
    @Query(AVERAGE_RATING_BY_TICKET_QUERY)
    Double getAverageRatingForTicket(@Param("ticket") Ticket ticket);
    
    @Query(AVERAGE_RATING_SINCE_QUERY)
    Double getAverageRatingSince(@Param("sinceTime") Instant sinceTime);

    // Rating distribution for analytics
    @Query("SELECT f.rating, COUNT(f) FROM FeedbackEntry f GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getRatingDistribution();
    
    @Query("SELECT f.rating, COUNT(f) FROM FeedbackEntry f WHERE f.submittedAt >= :sinceTime GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getRatingDistributionSince(@Param("sinceTime") Instant sinceTime);

    // Trend analysis queries
    @Query("SELECT DATE(f.submittedAt), AVG(f.rating) FROM FeedbackEntry f WHERE f.submittedAt >= :sinceTime GROUP BY DATE(f.submittedAt) ORDER BY DATE(f.submittedAt)")
    List<Object[]> getDailyAverageRatings(@Param("sinceTime") Instant sinceTime);

    // User satisfaction patterns
    @Query("SELECT f.createdBy, AVG(f.rating), COUNT(f) FROM FeedbackEntry f GROUP BY f.createdBy HAVING COUNT(f) >= :minFeedbackCount ORDER BY AVG(f.rating) ASC")
    List<Object[]> getUserSatisfactionSummary(@Param("minFeedbackCount") long minFeedbackCount);

    // Low satisfaction alerts
    @Query("SELECT f FROM FeedbackEntry f WHERE f.rating <= :lowRatingThreshold AND f.submittedAt >= :recentTime ORDER BY f.submittedAt DESC")
    List<FeedbackEntry> findRecentLowRatings(@Param("lowRatingThreshold") int lowRatingThreshold,
                                           @Param("recentTime") Instant recentTime);

    // High satisfaction highlights
    @Query("SELECT f FROM FeedbackEntry f WHERE f.rating >= :highRatingThreshold AND f.submittedAt >= :recentTime ORDER BY f.submittedAt DESC")
    List<FeedbackEntry> findRecentHighRatings(@Param("highRatingThreshold") int highRatingThreshold,
                                            @Param("recentTime") Instant recentTime);

    // Ticket satisfaction summary
    @Query("SELECT t, AVG(f.rating), COUNT(f) FROM FeedbackEntry f JOIN f.ticket t GROUP BY t HAVING COUNT(f) >= :minFeedbackCount ORDER BY AVG(f.rating) ASC")
    List<Object[]> getTicketSatisfactionSummary(@Param("minFeedbackCount") long minFeedbackCount);
} 
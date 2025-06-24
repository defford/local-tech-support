package com.localtechsupport.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for ticket history statistics and analytics.
 * 
 * Contains various metrics and insights derived from ticket history data,
 * including change patterns, user activity, and workflow analytics.
 */
@Data
public class HistoryStatisticsResponse {

    /**
     * Total number of history entries in the system.
     */
    private long totalHistoryEntries;

    /**
     * Distribution of status changes by status type.
     * Key: Status name, Value: Count of changes to that status
     */
    private Map<String, Long> statusChangeDistribution;

    /**
     * Top users by activity (number of changes made).
     */
    private List<UserActivitySummary> topUsersByActivity;

    /**
     * Daily change activity for trend analysis.
     */
    private List<DailyActivity> dailyChangeActivity;

    /**
     * Tickets with the most history entries (most changed tickets).
     */
    private List<TicketChangeSummary> mostChangedTickets;

    /**
     * Status transition patterns showing common workflows.
     */
    private List<StatusTransition> statusTransitionPatterns;

    /**
     * Average resolution time metrics.
     */
    private ResolutionMetrics resolutionMetrics;

    /**
     * Recent activity summary.
     */
    private RecentActivitySummary recentActivity;

    /**
     * Nested class for user activity summaries.
     */
    @Data
    public static class UserActivitySummary {
        private String userId;
        private long activityCount;
        private long uniqueTicketsModified;
        private String mostCommonAction;
    }

    /**
     * Nested class for daily activity tracking.
     */
    @Data
    public static class DailyActivity {
        private String date;
        private long changeCount;
        private long uniqueTickets;
        private long uniqueUsers;
    }

    /**
     * Nested class for ticket change summaries.
     */
    @Data
    public static class TicketChangeSummary {
        private Long ticketId;
        private String ticketDescription;
        private long changeCount;
        private String currentStatus;
        private String clientName;
    }

    /**
     * Nested class for status transition patterns.
     */
    @Data
    public static class StatusTransition {
        private String fromStatus;
        private String toStatus;
        private long occurrenceCount;
        private double percentage;
    }

    /**
     * Nested class for resolution metrics.
     */
    @Data
    public static class ResolutionMetrics {
        private double averageResolutionTimeHours;
        private double medianResolutionTimeHours;
        private long ticketsResolved;
        private double averageChangesPerTicket;
    }

    /**
     * Nested class for recent activity summary.
     */
    @Data
    public static class RecentActivitySummary {
        private long changesLast24Hours;
        private long changesLast7Days;
        private long changesLast30Days;
        private long activeUsersLast24Hours;
        private long activeUsersLast7Days;
    }
} 
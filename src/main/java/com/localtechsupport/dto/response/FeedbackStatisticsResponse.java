package com.localtechsupport.dto.response;

import java.util.Map;

/**
 * Response DTO for feedback statistics and analytics.
 * Contains customer satisfaction metrics and trends.
 */
public class FeedbackStatisticsResponse {

    private double overallAverageRating;
    private long totalFeedbackCount;
    private Map<Integer, Long> ratingDistribution;
    private long highSatisfactionCount;
    private long lowSatisfactionCount;
    private double highSatisfactionPercentage;
    private double lowSatisfactionPercentage;

    // Default constructor
    public FeedbackStatisticsResponse() {}

    // Constructor with all fields
    public FeedbackStatisticsResponse(double overallAverageRating, 
                                    long totalFeedbackCount,
                                    Map<Integer, Long> ratingDistribution,
                                    long highSatisfactionCount,
                                    long lowSatisfactionCount,
                                    double highSatisfactionPercentage,
                                    double lowSatisfactionPercentage) {
        this.overallAverageRating = overallAverageRating;
        this.totalFeedbackCount = totalFeedbackCount;
        this.ratingDistribution = ratingDistribution;
        this.highSatisfactionCount = highSatisfactionCount;
        this.lowSatisfactionCount = lowSatisfactionCount;
        this.highSatisfactionPercentage = highSatisfactionPercentage;
        this.lowSatisfactionPercentage = lowSatisfactionPercentage;
    }

    // Static factory method from service metrics
    public static FeedbackStatisticsResponse from(Map<String, Object> metrics) {
        FeedbackStatisticsResponse response = new FeedbackStatisticsResponse();
        response.setOverallAverageRating((Double) metrics.getOrDefault("overallAverageRating", 0.0));
        response.setTotalFeedbackCount((Long) metrics.getOrDefault("totalFeedbackCount", 0L));
        response.setRatingDistribution((Map<Integer, Long>) metrics.get("ratingDistribution"));
        response.setHighSatisfactionCount((Long) metrics.getOrDefault("highSatisfactionCount", 0L));
        response.setLowSatisfactionCount((Long) metrics.getOrDefault("lowSatisfactionCount", 0L));
        response.setHighSatisfactionPercentage((Double) metrics.getOrDefault("highSatisfactionPercentage", 0.0));
        response.setLowSatisfactionPercentage((Double) metrics.getOrDefault("lowSatisfactionPercentage", 0.0));
        return response;
    }

    // Getters and Setters
    public double getOverallAverageRating() {
        return overallAverageRating;
    }

    public void setOverallAverageRating(double overallAverageRating) {
        this.overallAverageRating = overallAverageRating;
    }

    public long getTotalFeedbackCount() {
        return totalFeedbackCount;
    }

    public void setTotalFeedbackCount(long totalFeedbackCount) {
        this.totalFeedbackCount = totalFeedbackCount;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }

    public long getHighSatisfactionCount() {
        return highSatisfactionCount;
    }

    public void setHighSatisfactionCount(long highSatisfactionCount) {
        this.highSatisfactionCount = highSatisfactionCount;
    }

    public long getLowSatisfactionCount() {
        return lowSatisfactionCount;
    }

    public void setLowSatisfactionCount(long lowSatisfactionCount) {
        this.lowSatisfactionCount = lowSatisfactionCount;
    }

    public double getHighSatisfactionPercentage() {
        return highSatisfactionPercentage;
    }

    public void setHighSatisfactionPercentage(double highSatisfactionPercentage) {
        this.highSatisfactionPercentage = highSatisfactionPercentage;
    }

    public double getLowSatisfactionPercentage() {
        return lowSatisfactionPercentage;
    }

    public void setLowSatisfactionPercentage(double lowSatisfactionPercentage) {
        this.lowSatisfactionPercentage = lowSatisfactionPercentage;
    }

    @Override
    public String toString() {
        return "FeedbackStatisticsResponse{" +
                "overallAverageRating=" + overallAverageRating +
                ", totalFeedbackCount=" + totalFeedbackCount +
                ", ratingDistribution=" + ratingDistribution +
                ", highSatisfactionCount=" + highSatisfactionCount +
                ", lowSatisfactionCount=" + lowSatisfactionCount +
                ", highSatisfactionPercentage=" + highSatisfactionPercentage +
                ", lowSatisfactionPercentage=" + lowSatisfactionPercentage +
                '}';
    }
} 
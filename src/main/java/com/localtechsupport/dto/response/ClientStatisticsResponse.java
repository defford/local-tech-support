package com.localtechsupport.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for client statistics and analytics.
 * Contains counts, percentages, and trend data for client management.
 */
public class ClientStatisticsResponse {

    // Basic counts
    private long totalClients;
    private long activeClients;
    private long inactiveClients;

    // Time-based statistics
    private long clientsCreatedToday;
    private long clientsCreatedThisWeek;
    private long clientsCreatedThisMonth;

    // Status distribution
    private Map<String, Long> clientsByStatus;

    // Percentage calculations
    private double activePercentage;
    private double inactivePercentage;

    // Date range for statistics
    private LocalDateTime statisticsGeneratedAt;
    private LocalDateTime dataFromDate;
    private LocalDateTime dataToDate;

    // Default constructor
    public ClientStatisticsResponse() {
        this.statisticsGeneratedAt = LocalDateTime.now();
    }

    // Constructor with basic counts
    public ClientStatisticsResponse(long totalClients, long activeClients, long inactiveClients) {
        this();
        this.totalClients = totalClients;
        this.activeClients = activeClients;
        this.inactiveClients = inactiveClients;
        calculatePercentages();
    }

    // Full constructor
    public ClientStatisticsResponse(long totalClients, long activeClients, long inactiveClients,
                                   long clientsCreatedToday, long clientsCreatedThisWeek, 
                                   long clientsCreatedThisMonth, Map<String, Long> clientsByStatus,
                                   LocalDateTime dataFromDate, LocalDateTime dataToDate) {
        this();
        this.totalClients = totalClients;
        this.activeClients = activeClients;
        this.inactiveClients = inactiveClients;
        this.clientsCreatedToday = clientsCreatedToday;
        this.clientsCreatedThisWeek = clientsCreatedThisWeek;
        this.clientsCreatedThisMonth = clientsCreatedThisMonth;
        this.clientsByStatus = clientsByStatus;
        this.dataFromDate = dataFromDate;
        this.dataToDate = dataToDate;
        calculatePercentages();
    }

    // Calculation methods
    private void calculatePercentages() {
        if (totalClients > 0) {
            this.activePercentage = (double) activeClients / totalClients * 100;
            this.inactivePercentage = (double) inactiveClients / totalClients * 100;
        } else {
            this.activePercentage = 0.0;
            this.inactivePercentage = 0.0;
        }
    }

    // Utility methods
    public boolean hasActiveClients() {
        return activeClients > 0;
    }

    public boolean hasInactiveClients() {
        return inactiveClients > 0;
    }

    public boolean hasGrowthData() {
        return clientsCreatedToday > 0 || clientsCreatedThisWeek > 0 || clientsCreatedThisMonth > 0;
    }

    public double getGrowthRate() {
        if (clientsCreatedThisMonth > 0 && totalClients > 0) {
            return (double) clientsCreatedThisMonth / totalClients * 100;
        }
        return 0.0;
    }

    // Getters and Setters
    public long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(long totalClients) {
        this.totalClients = totalClients;
        calculatePercentages();
    }

    public long getActiveClients() {
        return activeClients;
    }

    public void setActiveClients(long activeClients) {
        this.activeClients = activeClients;
        calculatePercentages();
    }

    public long getInactiveClients() {
        return inactiveClients;
    }

    public void setInactiveClients(long inactiveClients) {
        this.inactiveClients = inactiveClients;
        calculatePercentages();
    }

    public long getClientsCreatedToday() {
        return clientsCreatedToday;
    }

    public void setClientsCreatedToday(long clientsCreatedToday) {
        this.clientsCreatedToday = clientsCreatedToday;
    }

    public long getClientsCreatedThisWeek() {
        return clientsCreatedThisWeek;
    }

    public void setClientsCreatedThisWeek(long clientsCreatedThisWeek) {
        this.clientsCreatedThisWeek = clientsCreatedThisWeek;
    }

    public long getClientsCreatedThisMonth() {
        return clientsCreatedThisMonth;
    }

    public void setClientsCreatedThisMonth(long clientsCreatedThisMonth) {
        this.clientsCreatedThisMonth = clientsCreatedThisMonth;
    }

    public Map<String, Long> getClientsByStatus() {
        return clientsByStatus;
    }

    public void setClientsByStatus(Map<String, Long> clientsByStatus) {
        this.clientsByStatus = clientsByStatus;
    }

    public double getActivePercentage() {
        return activePercentage;
    }

    public void setActivePercentage(double activePercentage) {
        this.activePercentage = activePercentage;
    }

    public double getInactivePercentage() {
        return inactivePercentage;
    }

    public void setInactivePercentage(double inactivePercentage) {
        this.inactivePercentage = inactivePercentage;
    }

    public LocalDateTime getStatisticsGeneratedAt() {
        return statisticsGeneratedAt;
    }

    public void setStatisticsGeneratedAt(LocalDateTime statisticsGeneratedAt) {
        this.statisticsGeneratedAt = statisticsGeneratedAt;
    }

    public LocalDateTime getDataFromDate() {
        return dataFromDate;
    }

    public void setDataFromDate(LocalDateTime dataFromDate) {
        this.dataFromDate = dataFromDate;
    }

    public LocalDateTime getDataToDate() {
        return dataToDate;
    }

    public void setDataToDate(LocalDateTime dataToDate) {
        this.dataToDate = dataToDate;
    }

    @Override
    public String toString() {
        return "ClientStatisticsResponse{" +
                "totalClients=" + totalClients +
                ", activeClients=" + activeClients +
                ", inactiveClients=" + inactiveClients +
                ", clientsCreatedToday=" + clientsCreatedToday +
                ", clientsCreatedThisWeek=" + clientsCreatedThisWeek +
                ", clientsCreatedThisMonth=" + clientsCreatedThisMonth +
                ", activePercentage=" + String.format("%.2f", activePercentage) + "%" +
                ", inactivePercentage=" + String.format("%.2f", inactivePercentage) + "%" +
                ", statisticsGeneratedAt=" + statisticsGeneratedAt +
                '}';
    }
} 
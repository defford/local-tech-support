package com.localtechsupport.dto.response;

import java.util.Map;

/**
 * Response DTO for ticket statistics and reporting.
 */
public class TicketStatisticsResponse {

    private long totalTickets;
    private long openTickets;
    private long closedTickets;
    private long overdueTickets;
    private long unassignedTickets;
    private Map<String, Long> ticketsByServiceType;
    private Map<String, Long> ticketsByStatus;

    // Default constructor
    public TicketStatisticsResponse() {}

    // Constructor with core fields
    public TicketStatisticsResponse(long totalTickets, long openTickets, long closedTickets, 
                                   long overdueTickets, long unassignedTickets) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.closedTickets = closedTickets;
        this.overdueTickets = overdueTickets;
        this.unassignedTickets = unassignedTickets;
    }

    // Getters and setters
    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getOverdueTickets() {
        return overdueTickets;
    }

    public void setOverdueTickets(long overdueTickets) {
        this.overdueTickets = overdueTickets;
    }

    public long getUnassignedTickets() {
        return unassignedTickets;
    }

    public void setUnassignedTickets(long unassignedTickets) {
        this.unassignedTickets = unassignedTickets;
    }

    public Map<String, Long> getTicketsByServiceType() {
        return ticketsByServiceType;
    }

    public void setTicketsByServiceType(Map<String, Long> ticketsByServiceType) {
        this.ticketsByServiceType = ticketsByServiceType;
    }

    public Map<String, Long> getTicketsByStatus() {
        return ticketsByStatus;
    }

    public void setTicketsByStatus(Map<String, Long> ticketsByStatus) {
        this.ticketsByStatus = ticketsByStatus;
    }

    // Utility methods
    public double getClosureRate() {
        return totalTickets > 0 ? (double) closedTickets / totalTickets * 100 : 0.0;
    }

    public double getOverdueRate() {
        return openTickets > 0 ? (double) overdueTickets / openTickets * 100 : 0.0;
    }

    public double getAssignmentRate() {
        return openTickets > 0 ? (double) (openTickets - unassignedTickets) / openTickets * 100 : 0.0;
    }

    @Override
    public String toString() {
        return "TicketStatisticsResponse{" +
                "totalTickets=" + totalTickets +
                ", openTickets=" + openTickets +
                ", closedTickets=" + closedTickets +
                ", overdueTickets=" + overdueTickets +
                ", unassignedTickets=" + unassignedTickets +
                ", ticketsByServiceType=" + ticketsByServiceType +
                ", ticketsByStatus=" + ticketsByStatus +
                '}';
    }
} 
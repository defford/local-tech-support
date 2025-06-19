package com.localtechsupport.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician assignedTechnician;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackEntry> feedbackEntries = new ArrayList<>();

    // constructors
    public Ticket(Client client, ServiceType serviceType, String description, Instant dueAt) {
        this.client = client;
        this.serviceType = serviceType;
        this.description = description;
        this.dueAt = dueAt;
    }

// lifecycle hooks
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // methods
    public boolean isOpen() {
        return status == TicketStatus.OPEN;
      }
      
      public boolean isOverdue() {
        return isOpen() && dueAt != null && Instant.now().isAfter(dueAt);
      }
      
      public void addHistory(TicketHistory entry) {
        if (history == null) {
            history = new ArrayList<>();
        }
        entry.setTicket(this);
        history.add(entry);
      }
      
      public Optional<FeedbackEntry> latestFeedback() {
        if (feedbackEntries == null || feedbackEntries.isEmpty()) {
            return Optional.empty();
        }
        return feedbackEntries.stream()
                 .max(Comparator.comparing(FeedbackEntry::getSubmittedAt));
      }
}
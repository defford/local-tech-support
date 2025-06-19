package com.localtechsupport.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "feedback_entries")
public class FeedbackEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "rating", nullable = false)
    private int rating;
    @Column(name = "comment", nullable = false)
    private String comment;
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        submittedAt = Instant.now();
    }

}

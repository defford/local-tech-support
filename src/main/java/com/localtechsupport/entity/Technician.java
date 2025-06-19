package com.localtechsupport.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList; 
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "technicians")
@Data
@NoArgsConstructor
public class Technician {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  private TechnicianStatus status = TechnicianStatus.ACTIVE;

  @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<TechnicianSkill> skills = new HashSet<>();

  @OneToMany(mappedBy = "assignedTechnician")
  private List<Ticket> assignedTickets = new ArrayList<>();

  @OneToMany(mappedBy = "technician")
  private List<Appointment> appointments = new ArrayList<>();

  
  public long getCurrentLoad() {
    return assignedTickets.stream()
        .filter(ticket -> ticket.getStatus() == TicketStatus.OPEN)
        .count();
  }

  public boolean isQualifiedFor(ServiceType type) {
    return skills.stream().anyMatch(skill -> skill.getServiceType() == type);
  }
}

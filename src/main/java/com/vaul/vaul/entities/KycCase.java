package com.vaul.vaul.entities;

import com.vaul.vaul.enums.kyc.KycCaseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KycCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycCaseStatus status;

    @Column(name = "review_notes", length = 255)
    private String reviewNotes;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @PrePersist
    void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}

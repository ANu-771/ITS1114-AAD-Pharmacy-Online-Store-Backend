package lk.ijse.pharmacy_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lk.ijse.pharmacy_backend.enumiration.PrescriptionStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(length = 150)
    private String doctorName;

    @Column(length = 150)
    private String patientName;

    @Column(nullable = false, length = 500)
    private String prescriptionUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    @Column(length = 500)
    private String notes;

    private LocalDateTime reviewedAt;

    @Column(length = 100)
    private String reviewedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

package lk.ijse.pharmacy_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "medicine_details")
public class MedicineDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(length = 255)
    private String activeIngredient;

    @Column(length = 150)
    private String dosageForm;

    @Column(length = 100)
    private String strength;

    @Column(length = 200)
    private String manufacturer;

    @Column(length = 500)
    private String storageInfo;
}

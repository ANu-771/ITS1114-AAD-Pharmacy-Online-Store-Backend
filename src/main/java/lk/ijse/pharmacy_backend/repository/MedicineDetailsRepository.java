package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.MedicineDetails;
import lk.ijse.pharmacy_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineDetailsRepository extends JpaRepository<MedicineDetails, Long> {
    Optional<MedicineDetails> findByProduct(Product product);
}

package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.Prescription;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.enumiration.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByUserOrderByCreatedAtDesc(User user);
    List<Prescription> findByStatusOrderByCreatedAtDesc(PrescriptionStatus status);
    Optional<Prescription> findByIdAndUser(Long id, User user);
    long countByStatus(PrescriptionStatus status);
}

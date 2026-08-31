package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.Order;
import lk.ijse.pharmacy_backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
}

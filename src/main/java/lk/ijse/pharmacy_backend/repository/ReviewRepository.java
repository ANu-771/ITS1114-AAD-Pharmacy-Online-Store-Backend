package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.Product;
import lk.ijse.pharmacy_backend.entity.Review;
import lk.ijse.pharmacy_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    Optional<Review> findByUserAndProduct(User user, Product product);
    long countByProduct(Product product);
}

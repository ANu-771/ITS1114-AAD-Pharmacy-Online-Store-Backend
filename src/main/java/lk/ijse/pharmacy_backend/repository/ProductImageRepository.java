package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.Product;
import lk.ijse.pharmacy_backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductOrderByDisplayOrderAsc(Product product);
}

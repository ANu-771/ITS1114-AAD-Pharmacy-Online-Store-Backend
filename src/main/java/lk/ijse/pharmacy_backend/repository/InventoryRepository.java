package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.Inventory;
import lk.ijse.pharmacy_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct(Product product);

    Optional<Inventory> findByProduct_Id(Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.currentStock <= i.reorderLevel")
    List<Inventory> findLowStockInventories();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.currentStock <= i.reorderLevel")
    long countLowStock();
}

package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.Inventory;
import lk.ijse.pharmacy_backend.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByInventory(Inventory inventory);

    @Query("SELECT b FROM InventoryBatch b WHERE b.expiryDate < :currentDate")
    List<InventoryBatch> findExpiredBatches(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT b FROM InventoryBatch b WHERE b.expiryDate >= :currentDate AND b.expiryDate <= :nearExpiryDate")
    List<InventoryBatch> findNearExpiryBatches(@Param("currentDate") LocalDate currentDate, @Param("nearExpiryDate") LocalDate nearExpiryDate);
}

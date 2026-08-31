package lk.ijse.pharmacy_backend.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {

    private Long id;
    private Long productId;
    private String name;
    private String productName;
    private String sku;
    private String category;
    private Integer stock;
    private Integer currentStock;
    private Integer reorderLevel;
    private String batch;
    private String latestBatchNumber;
    private String expiry;
    private String latestExpiryDate;
    private String locationAisle;
    private String status; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    private List<InventoryBatchDTO> batches;
    private LocalDateTime updatedAt;
}

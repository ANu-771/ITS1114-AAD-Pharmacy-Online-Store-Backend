package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.inventory.AddBatchRequest;
import lk.ijse.pharmacy_backend.dto.inventory.InventoryBatchDTO;
import lk.ijse.pharmacy_backend.dto.inventory.InventoryDTO;

import java.util.List;

public interface InventoryService {

    List<InventoryDTO> getInventoryList();

    List<InventoryDTO> getLowStockItems();

    InventoryBatchDTO addBatch(AddBatchRequest request);

    List<InventoryBatchDTO> getBatchesByProductId(Long productId);

    List<InventoryBatchDTO> getNearExpiryBatches();

    List<InventoryBatchDTO> getExpiredBatches();
}

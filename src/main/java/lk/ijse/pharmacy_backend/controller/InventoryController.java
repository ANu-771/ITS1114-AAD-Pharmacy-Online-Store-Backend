package lk.ijse.pharmacy_backend.controller;

import jakarta.validation.Valid;
import lk.ijse.pharmacy_backend.dto.inventory.AddBatchRequest;
import lk.ijse.pharmacy_backend.dto.inventory.InventoryBatchDTO;
import lk.ijse.pharmacy_backend.dto.inventory.InventoryDTO;
import lk.ijse.pharmacy_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getInventoryList() {
        return ResponseEntity.ok(inventoryService.getInventoryList());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    @PostMapping("/batch")
    public ResponseEntity<InventoryBatchDTO> addBatch(@Valid @RequestBody AddBatchRequest request) {
        InventoryBatchDTO created = inventoryService.addBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/product/{productId}/batches")
    public ResponseEntity<List<InventoryBatchDTO>> getBatchesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getBatchesByProductId(productId));
    }

    @GetMapping("/near-expiry")
    public ResponseEntity<List<InventoryBatchDTO>> getNearExpiryBatches() {
        return ResponseEntity.ok(inventoryService.getNearExpiryBatches());
    }

    @GetMapping("/expired")
    public ResponseEntity<List<InventoryBatchDTO>> getExpiredBatches() {
        return ResponseEntity.ok(inventoryService.getExpiredBatches());
    }
}

package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.inventory.AddBatchRequest;
import lk.ijse.pharmacy_backend.dto.inventory.InventoryBatchDTO;
import lk.ijse.pharmacy_backend.dto.inventory.InventoryDTO;
import lk.ijse.pharmacy_backend.entity.Inventory;
import lk.ijse.pharmacy_backend.entity.InventoryBatch;
import lk.ijse.pharmacy_backend.entity.Product;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.InventoryBatchRepository;
import lk.ijse.pharmacy_backend.repository.InventoryRepository;
import lk.ijse.pharmacy_backend.repository.ProductRepository;
import lk.ijse.pharmacy_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryList() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getLowStockItems() {
        return inventoryRepository.findLowStockInventories().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryBatchDTO addBatch(AddBatchRequest request) {
        Inventory inventory = null;

        if (request.getInventoryId() != null) {
            inventory = inventoryRepository.findById(request.getInventoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + request.getInventoryId()));
        } else if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
            inventory = inventoryRepository.findByProduct(product)
                    .orElseGet(() -> inventoryRepository.save(Inventory.builder()
                            .product(product)
                            .currentStock(0)
                            .reorderLevel(10)
                            .locationAisle("Aisle 1")
                            .build()));
        } else {
            throw new BadRequestException("Either inventoryId or productId must be provided");
        }

        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot add batch that is already expired: " + request.getExpiryDate());
        }

        InventoryBatch batch = InventoryBatch.builder()
                .inventory(inventory)
                .batchNumber(request.getBatchNumber().trim())
                .manufacturingDate(request.getManufacturingDate())
                .expiryDate(request.getExpiryDate())
                .quantity(request.getQuantity())
                .build();

        InventoryBatch savedBatch = inventoryBatchRepository.save(batch);

        // Increase inventory stock
        int newStock = (inventory.getCurrentStock() != null ? inventory.getCurrentStock() : 0) + request.getQuantity();
        inventory.setCurrentStock(newStock);
        inventoryRepository.save(inventory);

        log.info("Added new batch {} with quantity {} to inventory ID {}",
                savedBatch.getBatchNumber(), request.getQuantity(), inventory.getId());

        return mapBatchToDTO(savedBatch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryBatchDTO> getBatchesByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + product.getName()));

        return inventoryBatchRepository.findByInventory(inventory).stream()
                .map(this::mapBatchToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryBatchDTO> getNearExpiryBatches() {
        LocalDate today = LocalDate.now();
        LocalDate nearExpiryThreshold = today.plusMonths(3);
        return inventoryBatchRepository.findNearExpiryBatches(today, nearExpiryThreshold).stream()
                .map(this::mapBatchToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryBatchDTO> getExpiredBatches() {
        LocalDate today = LocalDate.now();
        return inventoryBatchRepository.findExpiredBatches(today).stream()
                .map(this::mapBatchToDTO)
                .collect(Collectors.toList());
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        Product p = inventory.getProduct();
        List<InventoryBatch> batches = inventoryBatchRepository.findByInventory(inventory);

        String latestBatchNumber = "N/A";
        String latestExpiry = "N/A";

        if (!batches.isEmpty()) {
            InventoryBatch latest = batches.stream()
                    .filter(b -> b.getExpiryDate() != null)
                    .max(Comparator.comparing(InventoryBatch::getExpiryDate))
                    .orElse(batches.get(0));

            latestBatchNumber = latest.getBatchNumber();
            latestExpiry = latest.getExpiryDate() != null ? latest.getExpiryDate().toString() : "N/A (Device)";
        }

        int stock = inventory.getCurrentStock() != null ? inventory.getCurrentStock() : 0;
        int reorder = inventory.getReorderLevel() != null ? inventory.getReorderLevel() : 10;

        String status = stock == 0 ? "OUT_OF_STOCK" : (stock <= reorder ? "LOW_STOCK" : "IN_STOCK");

        List<InventoryBatchDTO> batchDTOs = batches.stream()
                .map(this::mapBatchToDTO)
                .collect(Collectors.toList());

        return InventoryDTO.builder()
                .id(inventory.getId())
                .productId(p.getId())
                .name(p.getName())
                .productName(p.getName())
                .sku(p.getSku())
                .category(p.getCategory() != null ? p.getCategory().getName() : "General")
                .stock(stock)
                .currentStock(stock)
                .reorderLevel(reorder)
                .batch(latestBatchNumber)
                .latestBatchNumber(latestBatchNumber)
                .expiry(latestExpiry)
                .latestExpiryDate(latestExpiry)
                .locationAisle(inventory.getLocationAisle() != null ? inventory.getLocationAisle() : "Aisle 1")
                .status(status)
                .batches(batchDTOs)
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private InventoryBatchDTO mapBatchToDTO(InventoryBatch batch) {
        LocalDate today = LocalDate.now();
        boolean isExpired = batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(today);
        boolean isNear = batch.getExpiryDate() != null &&
                !isExpired &&
                batch.getExpiryDate().isBefore(today.plusMonths(3));

        return InventoryBatchDTO.builder()
                .id(batch.getId())
                .inventoryId(batch.getInventory() != null ? batch.getInventory().getId() : null)
                .batchNumber(batch.getBatchNumber())
                .manufacturingDate(batch.getManufacturingDate())
                .expiryDate(batch.getExpiryDate())
                .quantity(batch.getQuantity())
                .expired(isExpired)
                .nearExpiry(isNear)
                .createdAt(batch.getCreatedAt())
                .build();
    }
}

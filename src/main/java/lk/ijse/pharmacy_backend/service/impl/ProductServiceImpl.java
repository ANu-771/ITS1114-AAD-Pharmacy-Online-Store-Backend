package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.common.PageResponse;
import lk.ijse.pharmacy_backend.dto.product.ProductDTO;
import lk.ijse.pharmacy_backend.dto.product.ProductRequestDTO;
import lk.ijse.pharmacy_backend.entity.*;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.*;
import lk.ijse.pharmacy_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final MedicineDetailsRepository medicineDetailsRepository;
    private final ProductImageRepository productImageRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> getProducts(
            String category,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int size
    ) {
        Sort sorting = Sort.by(Sort.Direction.DESC, "createdAt");

        if ("price_asc".equalsIgnoreCase(sort) || "price-asc".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sort) || "price-desc".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "price");
        } else if ("rating".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "rating");
        } else if ("name".equalsIgnoreCase(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "name");
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 12, sorting);

        String catFilter = (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category)) ? category.trim() : null;
        String searchFilter = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Product> productPage = productRepository.findFilteredProducts(catFilter, searchFilter, minPrice, maxPrice, pageable);

        List<ProductDTO> dtos = productPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponse.<ProductDTO>builder()
                .content(dtos)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findTop8ByActiveTrueOrderByRatingDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_IdAndActiveTrue(categoryId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getFeaturedProducts();
        }
        return productRepository.searchProducts(query.trim()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductRequestDTO request) {
        Category category;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        } else if (request.getCategory() != null) {
            category = categoryRepository.findBySlug(request.getCategory())
                    .orElseGet(() -> categoryRepository.findByName(request.getCategory())
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategory())));
        } else {
            throw new BadRequestException("Category is required");
        }

        Brand brand;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + request.getBrandId()));
        } else if (request.getBrand() != null) {
            brand = brandRepository.findByName(request.getBrand())
                    .orElseGet(() -> brandRepository.save(Brand.builder().name(request.getBrand()).verified(true).build()));
        } else {
            throw new BadRequestException("Brand is required");
        }

        String sku = (request.getSku() != null && !request.getSku().trim().isEmpty())
                ? request.getSku().trim().toUpperCase()
                : "KKP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        boolean rx = request.isRxRequired() || request.isRequiresPrescription();

        Product product = Product.builder()
                .name(request.getName().trim())
                .sku(sku)
                .category(category)
                .brand(brand)
                .price(request.getPrice())
                .oldPrice(request.getOldPrice())
                .rxRequired(rx)
                .badge(request.getBadge())
                .description(request.getDescription())
                .active(true)
                .rating(5.0)
                .reviewsCount(0)
                .build();

        Product savedProduct = productRepository.save(product);

        // Save Medicine Details
        MedicineDetails details = MedicineDetails.builder()
                .product(savedProduct)
                .activeIngredient(request.getActiveIngredient() != null ? request.getActiveIngredient() : "N/A")
                .dosageForm(request.getDosageForm() != null ? request.getDosageForm() : "Standard")
                .strength(request.getStrength() != null ? request.getStrength() : "Standard")
                .manufacturer(request.getManufacturer() != null ? request.getManufacturer() : brand.getName())
                .storageInfo(request.getStorageInfo() != null ? request.getStorageInfo() : "Store in a cool dry place")
                .build();
        medicineDetailsRepository.save(details);

        // Save Primary Image
        String primaryImg = request.getImage() != null && !request.getImage().isEmpty()
                ? request.getImage()
                : "assets/images/medicine_1.png";

        ProductImage img = ProductImage.builder()
                .product(savedProduct)
                .imageUrl(primaryImg)
                .isPrimary(true)
                .displayOrder(0)
                .build();
        productImageRepository.save(img);

        if (request.getImages() != null) {
            int order = 1;
            for (String extraImg : request.getImages()) {
                if (!extraImg.equals(primaryImg)) {
                    ProductImage extra = ProductImage.builder()
                            .product(savedProduct)
                            .imageUrl(extraImg)
                            .isPrimary(false)
                            .displayOrder(order++)
                            .build();
                    productImageRepository.save(extra);
                }
            }
        }

        // Save Inventory
        int initialStock = request.getInitialStock() != null ? request.getInitialStock() : 50;
        int reorderLvl = request.getReorderLevel() != null ? request.getReorderLevel() : 10;
        Inventory inventory = Inventory.builder()
                .product(savedProduct)
                .currentStock(initialStock)
                .reorderLevel(reorderLvl)
                .locationAisle(request.getLocationAisle() != null ? request.getLocationAisle() : "Aisle 1")
                .build();
        inventoryRepository.save(inventory);

        log.info("Created new pharmacy product: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
        return mapToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (request.getName() != null) product.setName(request.getName().trim());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOldPrice() != null) product.setOldPrice(request.getOldPrice());
        if (request.getBadge() != null) product.setBadge(request.getBadge());
        if (request.getDescription() != null) product.setDescription(request.getDescription());

        boolean rx = request.isRxRequired() || request.isRequiresPrescription();
        product.setRxRequired(rx);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
            product.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(product);

        // Update medicine details
        medicineDetailsRepository.findByProduct(product).ifPresent(details -> {
            if (request.getActiveIngredient() != null) details.setActiveIngredient(request.getActiveIngredient());
            if (request.getDosageForm() != null) details.setDosageForm(request.getDosageForm());
            if (request.getStrength() != null) details.setStrength(request.getStrength());
            if (request.getManufacturer() != null) details.setManufacturer(request.getManufacturer());
            if (request.getStorageInfo() != null) details.setStorageInfo(request.getStorageInfo());
            medicineDetailsRepository.save(details);
        });

        return mapToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setActive(false); // soft delete
        productRepository.save(product);
        log.info("Soft deleted product with ID: {}", id);
    }

    private ProductDTO mapToDTO(Product product) {
        MedicineDetails details = medicineDetailsRepository.findByProduct(product).orElse(null);
        List<ProductImage> images = productImageRepository.findByProductOrderByDisplayOrderAsc(product);
        Inventory inventory = inventoryRepository.findByProduct(product).orElse(null);

        String primaryImageUrl = "assets/images/medicine_1.png";
        List<String> imageUrls = new ArrayList<>();
        for (ProductImage img : images) {
            imageUrls.add(img.getImageUrl());
            if (img.isPrimary()) {
                primaryImageUrl = img.getImageUrl();
            }
        }
        if (!images.isEmpty() && primaryImageUrl.equals("assets/images/medicine_1.png")) {
            primaryImageUrl = images.get(0).getImageUrl();
        }

        int stock = inventory != null ? inventory.getCurrentStock() : 0;
        int reorderLevel = inventory != null ? inventory.getReorderLevel() : 10;
        String aisle = inventory != null ? inventory.getLocationAisle() : "Main Pharmacy";

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .brand(product.getBrand() != null ? product.getBrand().getName() : "KK Digital Pharmacy")
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .category(product.getCategory() != null ? product.getCategory().getSlug() : "general")
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "General")
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .rating(product.getRating())
                .reviewsCount(product.getReviewsCount())
                .inStock(stock > 0)
                .requiresPrescription(product.isRxRequired())
                .rxRequired(product.isRxRequired())
                .image(primaryImageUrl)
                .images(imageUrls)
                .badge(product.getBadge())
                .description(product.getDescription())
                .activeIngredient(details != null ? details.getActiveIngredient() : "N/A")
                .dosageForm(details != null ? details.getDosageForm() : "Standard")
                .strength(details != null ? details.getStrength() : "Standard")
                .manufacturer(details != null ? details.getManufacturer() : (product.getBrand() != null ? product.getBrand().getName() : "KK Digital Pharmacy"))
                .storageInfo(details != null ? details.getStorageInfo() : "Store in a cool, dry place")
                .stock(stock)
                .reorderLevel(reorderLevel)
                .locationAisle(aisle)
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}

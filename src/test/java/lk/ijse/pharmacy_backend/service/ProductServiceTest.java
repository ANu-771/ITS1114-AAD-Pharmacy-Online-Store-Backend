package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.product.ProductDTO;
import lk.ijse.pharmacy_backend.dto.product.ProductRequestDTO;
import lk.ijse.pharmacy_backend.entity.*;
import lk.ijse.pharmacy_backend.repository.*;
import lk.ijse.pharmacy_backend.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private MedicineDetailsRepository medicineDetailsRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Brand brand;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Medicines").slug("medicines").build();
        brand = Brand.builder().id(1L).name("GlaxoSmithKline").build();
        product = Product.builder()
                .id(10L)
                .name("Amoxicillin 500mg")
                .sku("RX-AMX-500")
                .price(new BigDecimal("650.00"))
                .rxRequired(true)
                .category(category)
                .brand(brand)
                .active(true)
                .rating(4.8)
                .reviewsCount(12)
                .build();
        inventory = Inventory.builder().id(1L).product(product).currentStock(45).reorderLevel(10).build();
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(medicineDetailsRepository.findByProduct(product)).thenReturn(Optional.empty());
        when(productImageRepository.findByProductOrderByDisplayOrderAsc(product)).thenReturn(Collections.emptyList());
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        ProductDTO dto = productService.getProductById(10L);

        assertNotNull(dto);
        assertEquals("Amoxicillin 500mg", dto.getName());
        assertEquals("Medicines", dto.getCategoryName());
        assertEquals(45, dto.getStock());
        assertTrue(dto.isRxRequired());
    }

    @Test
    void createProduct_Success() {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("Amoxicillin 500mg")
                .categoryId(1L)
                .brandId(1L)
                .price(new BigDecimal("650.00"))
                .rxRequired(true)
                .initialStock(45)
                .reorderLevel(10)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(medicineDetailsRepository.findByProduct(product)).thenReturn(Optional.empty());
        when(productImageRepository.findByProductOrderByDisplayOrderAsc(product)).thenReturn(Collections.emptyList());
        when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

        ProductDTO created = productService.createProduct(request);

        assertNotNull(created);
        assertEquals("Amoxicillin 500mg", created.getName());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }
}

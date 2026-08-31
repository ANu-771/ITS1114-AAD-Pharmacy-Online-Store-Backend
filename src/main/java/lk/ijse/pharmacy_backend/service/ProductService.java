package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.common.PageResponse;
import lk.ijse.pharmacy_backend.dto.product.ProductDTO;
import lk.ijse.pharmacy_backend.dto.product.ProductRequestDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    PageResponse<ProductDTO> getProducts(
            String category,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int size
    );

    ProductDTO getProductById(Long id);

    List<ProductDTO> getFeaturedProducts();

    List<ProductDTO> getProductsByCategory(Long categoryId);

    List<ProductDTO> searchProducts(String query);

    ProductDTO createProduct(ProductRequestDTO request);

    ProductDTO updateProduct(Long id, ProductRequestDTO request);

    void deleteProduct(Long id);
}

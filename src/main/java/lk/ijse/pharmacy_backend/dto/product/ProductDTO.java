package lk.ijse.pharmacy_backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long id;
    private String name;
    private String sku;
    private String brand;
    private Long brandId;
    private String category;
    private String categoryName;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private Double rating;
    private Integer reviewsCount;
    private boolean inStock;
    private boolean requiresPrescription;
    private boolean rxRequired;
    private String image;
    private List<String> images;
    private String badge;
    private String description;
    private String activeIngredient;
    private String strength;
    private String dosageForm;
    private String manufacturer;
    private String storageInfo;
    private Integer stock;
    private Integer reorderLevel;
    private String locationAisle;
    private boolean active;
    private LocalDateTime createdAt;
}

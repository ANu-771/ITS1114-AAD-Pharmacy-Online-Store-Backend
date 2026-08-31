package lk.ijse.pharmacy_backend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    private String name;

    private String sku;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String category; // optional fallback name

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    private String brand; // optional fallback name

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal oldPrice;

    private boolean rxRequired;
    private boolean requiresPrescription;

    private String badge;
    private String description;
    private String image;
    private List<String> images;

    // Medicine details
    private String activeIngredient;
    private String dosageForm;
    private String strength;
    private String manufacturer;
    private String storageInfo;

    // Inventory
    private Integer initialStock;
    private Integer reorderLevel;
    private String locationAisle;
}

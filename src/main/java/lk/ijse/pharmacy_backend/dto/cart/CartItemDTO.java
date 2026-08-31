package lk.ijse.pharmacy_backend.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {

    private Long id;
    private Long productId;
    private String name;
    private String brand;
    private BigDecimal price;
    private String image;
    private String category;
    private boolean requiresPrescription;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer availableStock;
}

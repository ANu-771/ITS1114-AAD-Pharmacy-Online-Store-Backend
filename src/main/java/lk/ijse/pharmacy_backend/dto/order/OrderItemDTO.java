package lk.ijse.pharmacy_backend.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private String name;
    private String productName;
    private BigDecimal price;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String image;
}

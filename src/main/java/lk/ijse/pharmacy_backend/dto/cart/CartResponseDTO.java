package lk.ijse.pharmacy_backend.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {

    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();

    @Builder.Default
    private Integer count = 0;

    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    private boolean freeShippingEligible = false;

    @Builder.Default
    private BigDecimal freeShippingThreshold = new BigDecimal("5000.00");

    @Builder.Default
    private BigDecimal amountNeededForFreeShipping = BigDecimal.ZERO;
}

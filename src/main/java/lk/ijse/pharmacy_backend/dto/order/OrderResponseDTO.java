package lk.ijse.pharmacy_backend.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private String date;
    private LocalDateTime createdAt;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionReference;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal total;
    private BigDecimal totalAmount;
    private String trackingId;
    private LocalDate estimatedDelivery;
    private List<OrderItemDTO> items;
    private Long prescriptionId;
    private String prescriptionStatus;
}

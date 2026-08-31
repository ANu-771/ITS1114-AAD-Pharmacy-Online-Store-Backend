package lk.ijse.pharmacy_backend.dto.order;

import jakarta.validation.constraints.NotBlank;
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
public class OrderCreateRequest {

    private String customerName;
    private String customerEmail;
    private String phone;

    private String address;
    private String addressLine1;
    private String city;
    private String postalCode;
    private String shippingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private List<OrderItemRequest> items;

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal total;
    private BigDecimal totalAmount;

    // Prescription reference if Rx required
    private Long prescriptionId;
    private String doctorName;
    private String prescriptionUrl;

    public String getFullShippingAddress() {
        if (shippingAddress != null && !shippingAddress.trim().isEmpty()) {
            return shippingAddress.trim();
        }
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        else if (address != null) sb.append(address);

        if (city != null) sb.append(", ").append(city);
        if (postalCode != null) sb.append(", ").append(postalCode);
        return sb.toString();
    }
}

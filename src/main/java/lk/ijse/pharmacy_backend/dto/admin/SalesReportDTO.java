package lk.ijse.pharmacy_backend.dto.admin;

import lk.ijse.pharmacy_backend.dto.order.OrderResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDTO {

    private LocalDate fromDate;
    private LocalDate toDate;
    private Long totalOrders;
    private BigDecimal totalSales;
    private BigDecimal averageOrderValue;
    private Map<String, BigDecimal> salesByPaymentMethod;
    private Map<String, Long> salesByCategory;
    private List<OrderResponseDTO> orders;
}

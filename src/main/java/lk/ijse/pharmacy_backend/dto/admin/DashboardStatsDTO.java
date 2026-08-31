package lk.ijse.pharmacy_backend.dto.admin;

import lk.ijse.pharmacy_backend.dto.order.OrderResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {

    private BigDecimal totalRevenue;
    private String revenueChange;
    private Long totalOrders;
    private String ordersChange;
    private Long totalProducts;
    private Long lowStockCount;
    private Long lowStockProducts; // alias
    private Long totalUsers;
    private Long totalCustomers; // alias
    private Long pendingOrders;
    private Long prescriptionRequests;

    private Map<String, Long> categoryDistribution;
    private Map<String, BigDecimal> monthlyRevenue;
    private List<OrderResponseDTO> recentOrders;
}

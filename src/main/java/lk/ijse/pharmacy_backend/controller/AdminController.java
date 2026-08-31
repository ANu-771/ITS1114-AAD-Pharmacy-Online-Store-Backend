package lk.ijse.pharmacy_backend.controller;

import jakarta.validation.Valid;
import lk.ijse.pharmacy_backend.dto.admin.DashboardStatsDTO;
import lk.ijse.pharmacy_backend.dto.admin.SalesReportDTO;
import lk.ijse.pharmacy_backend.dto.admin.UserStatusUpdateRequest;
import lk.ijse.pharmacy_backend.dto.auth.UserSummaryDTO;
import lk.ijse.pharmacy_backend.dto.common.PageResponse;
import lk.ijse.pharmacy_backend.dto.order.OrderResponseDTO;
import lk.ijse.pharmacy_backend.dto.order.OrderStatusUpdateRequest;
import lk.ijse.pharmacy_backend.service.AdminService;
import lk.ijse.pharmacy_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
        List<UserSummaryDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserSummaryDTO> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody(required = false) UserStatusUpdateRequest request
    ) {
        UserStatusUpdateRequest safeRequest = request != null ? request : new UserStatusUpdateRequest();
        UserSummaryDTO updated = adminService.updateUserStatus(userId, safeRequest);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/orders")
    public ResponseEntity<PageResponse<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<OrderResponseDTO> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponseDTO updated = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReportDTO> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate actualFrom = from != null ? from : (startDate != null ? startDate : LocalDate.now().minusMonths(1));
        LocalDate actualTo = to != null ? to : (endDate != null ? endDate : LocalDate.now());

        SalesReportDTO report = adminService.getSalesReport(actualFrom, actualTo);
        return ResponseEntity.ok(report);
    }
}

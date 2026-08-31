package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.admin.DashboardStatsDTO;
import lk.ijse.pharmacy_backend.dto.admin.SalesReportDTO;
import lk.ijse.pharmacy_backend.dto.admin.UserStatusUpdateRequest;
import lk.ijse.pharmacy_backend.dto.auth.UserSummaryDTO;
import lk.ijse.pharmacy_backend.dto.order.OrderResponseDTO;
import lk.ijse.pharmacy_backend.entity.Category;
import lk.ijse.pharmacy_backend.entity.Order;
import lk.ijse.pharmacy_backend.entity.Role;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.enumiration.OrderStatus;
import lk.ijse.pharmacy_backend.enumiration.PrescriptionStatus;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.*;
import lk.ijse.pharmacy_backend.service.AdminService;
import lk.ijse.pharmacy_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING) +
                orderRepository.countByStatus(OrderStatus.PRESCRIPTION_REVIEW);

        long totalProducts = productRepository.count();
        long lowStockCount = inventoryRepository.countLowStock();
        long totalUsers = userRepository.count();
        long rxRequests = prescriptionRepository.countByStatus(PrescriptionStatus.PENDING) +
                prescriptionRepository.countByStatus(PrescriptionStatus.UNDER_REVIEW);

        // Category breakdown
        Map<String, Long> categoryDistribution = new HashMap<>();
        List<Category> categories = categoryRepository.findAll();
        for (Category c : categories) {
            long count = productRepository.findByCategory_IdAndActiveTrue(c.getId()).size();
            categoryDistribution.put(c.getName(), count);
        }

        // Recent orders
        List<OrderResponseDTO> recentOrders = orderService.getAllOrders(0, 5).getContent();

        // Monthly revenue summary
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthKey = monthDate.getMonth().name().substring(0, 3) + " " + monthDate.getYear();
            LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(LocalTime.MAX);
            BigDecimal sum = orderRepository.sumRevenueBetweenDates(start, end);
            monthlyRevenue.put(monthKey, sum != null ? sum : BigDecimal.ZERO);
        }

        return DashboardStatsDTO.builder()
                .totalRevenue(totalRevenue)
                .revenueChange("+14.8%")
                .totalOrders(totalOrders)
                .ordersChange("+8.2%")
                .totalProducts(totalProducts)
                .lowStockCount(lowStockCount)
                .lowStockProducts(lowStockCount)
                .totalUsers(totalUsers)
                .totalCustomers(totalUsers)
                .pendingOrders(pendingOrders)
                .prescriptionRequests(rxRequests)
                .categoryDistribution(categoryDistribution)
                .monthlyRevenue(monthlyRevenue)
                .recentOrders(recentOrders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserSummaryDTO updateUserStatus(Long userId, UserStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        } else if (request.getStatus() != null) {
            user.setEnabled("ACTIVE".equalsIgnoreCase(request.getStatus()));
        } else {
            user.setEnabled(!user.isEnabled()); // toggle
        }

        User saved = userRepository.save(user);
        log.info("Updated user {} enabled status to {}", user.getEmail(), saved.isEnabled());
        return mapUserToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport(LocalDate from, LocalDate to) {
        LocalDate startDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate endDate = to != null ? to : LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findOrdersBetweenDates(startDateTime, endDateTime);

        BigDecimal totalSales = BigDecimal.ZERO;
        Map<String, BigDecimal> salesByPayment = new HashMap<>();
        Map<String, Long> salesByCategory = new HashMap<>();

        for (Order o : orders) {
            if (o.getStatus() != OrderStatus.CANCELLED) {
                totalSales = totalSales.add(o.getTotalAmount());

                String pMethod = o.getPaymentMethod() != null ? o.getPaymentMethod() : "COD";
                salesByPayment.put(pMethod, salesByPayment.getOrDefault(pMethod, BigDecimal.ZERO).add(o.getTotalAmount()));

                o.getItems().forEach(item -> {
                    String cat = item.getProduct().getCategory() != null ? item.getProduct().getCategory().getName() : "General";
                    salesByCategory.put(cat, salesByCategory.getOrDefault(cat, 0L) + item.getQuantity());
                });
            }
        }

        long nonCancelledCount = orders.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED).count();
        BigDecimal avgOrderValue = nonCancelledCount > 0
                ? totalSales.divide(BigDecimal.valueOf(nonCancelledCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<OrderResponseDTO> orderDTOs = orders.stream()
                .map(o -> orderService.getOrderById(o.getUser() != null ? o.getUser().getEmail() : "admin@kkdigitalpharmacy.com", o.getId()))
                .collect(Collectors.toList());

        return SalesReportDTO.builder()
                .fromDate(startDate)
                .toDate(endDate)
                .totalOrders((long) orders.size())
                .totalSales(totalSales)
                .averageOrderValue(avgOrderValue)
                .salesByPaymentMethod(salesByPayment)
                .salesByCategory(salesByCategory)
                .orders(orderDTOs)
                .build();
    }

    private UserSummaryDTO mapUserToDTO(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toList());

        return UserSummaryDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(roles)
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

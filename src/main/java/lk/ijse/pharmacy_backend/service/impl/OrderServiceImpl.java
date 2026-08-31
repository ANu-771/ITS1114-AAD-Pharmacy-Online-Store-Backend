package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.common.PageResponse;
import lk.ijse.pharmacy_backend.dto.order.OrderCreateRequest;
import lk.ijse.pharmacy_backend.dto.order.OrderItemDTO;
import lk.ijse.pharmacy_backend.dto.order.OrderItemRequest;
import lk.ijse.pharmacy_backend.dto.order.OrderResponseDTO;
import lk.ijse.pharmacy_backend.dto.order.OrderStatusUpdateRequest;
import lk.ijse.pharmacy_backend.entity.*;
import lk.ijse.pharmacy_backend.enumiration.OrderStatus;
import lk.ijse.pharmacy_backend.enumiration.PaymentMethod;
import lk.ijse.pharmacy_backend.enumiration.PaymentStatus;
import lk.ijse.pharmacy_backend.enumiration.PrescriptionStatus;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.InsufficientStockException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.*;
import lk.ijse.pharmacy_backend.service.CartService;
import lk.ijse.pharmacy_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository productImageRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("5000.00");
    private static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("350.00");

    @Override
    @Transactional
    public OrderResponseDTO createOrder(String userEmail, OrderCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // 1. Determine order items (from request or active cart)
        List<OrderItemRequest> itemRequests = request.getItems();
        if (itemRequests == null || itemRequests.isEmpty()) {
            List<CartItem> cartItems = cartItemRepository.findByUser(user);
            if (cartItems.isEmpty()) {
                throw new BadRequestException("Cannot place order with an empty cart");
            }
            itemRequests = cartItems.stream()
                    .map(ci -> OrderItemRequest.builder()
                            .productId(ci.getProduct().getId())
                            .quantity(ci.getQuantity())
                            .build())
                    .collect(Collectors.toList());
        }

        // 2. Validate products and inventory, calculate subtotal, check Rx requirements
        BigDecimal subtotal = BigDecimal.ZERO;
        boolean hasRxProduct = false;
        List<OrderItem> orderItemsToSave = new ArrayList<>();

        for (OrderItemRequest itemReq : itemRequests) {
            Long pId = itemReq.getResolvedProductId();
            if (pId == null) {
                throw new BadRequestException("Product ID is missing in order item");
            }

            Product product = productRepository.findById(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + pId));

            if (!product.isActive()) {
                throw new BadRequestException("Product " + product.getName() + " is currently unavailable");
            }

            Inventory inventory = inventoryRepository.findByProduct(product)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + product.getName()));

            int requestedQty = itemReq.getQuantity() != null && itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;
            int currentStock = inventory.getCurrentStock() != null ? inventory.getCurrentStock() : 0;

            if (requestedQty > currentStock) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for %s. Requested: %d, Available: %d",
                                product.getName(), requestedQty, currentStock)
                );
            }

            // Deduct inventory stock
            inventory.setCurrentStock(currentStock - requestedQty);
            inventoryRepository.save(inventory);

            if (product.isRxRequired()) {
                hasRxProduct = true;
            }

            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(requestedQty));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(itemPrice)
                    .quantity(requestedQty)
                    .subtotal(itemSubtotal)
                    .build();

            orderItemsToSave.add(orderItem);
        }

        // 3. Compute delivery fee, discounts, and total
        BigDecimal deliveryFee = request.getDeliveryFee() != null
                ? request.getDeliveryFee()
                : (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : STANDARD_DELIVERY_FEE);

        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(deliveryFee).subtract(discount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 4. Generate order identifiers
        int randomNum = 10000 + new Random().nextInt(90000);
        String orderNumber = "KKP-2026-" + randomNum;
        String trackingId = "KKP-TRK-" + (10000 + new Random().nextInt(90000));
        LocalDate estimatedDelivery = LocalDate.now().plusDays(2);

        String customerName = (request.getCustomerName() != null && !request.getCustomerName().isEmpty())
                ? request.getCustomerName()
                : user.getFullName();

        String customerEmail = (request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty())
                ? request.getCustomerEmail()
                : user.getEmail();

        String shippingAddress = request.getFullShippingAddress();
        if (shippingAddress.isEmpty()) {
            shippingAddress = "No. 120, Galle Road, Colombo 03, Sri Lanka";
        }

        OrderStatus initialStatus = hasRxProduct ? OrderStatus.PRESCRIPTION_REVIEW : OrderStatus.PENDING;

        // 5. Build and save Order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .shippingAddress(shippingAddress)
                .paymentMethod(request.getPaymentMethod())
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .discount(discount)
                .totalAmount(totalAmount)
                .status(initialStatus)
                .trackingId(trackingId)
                .estimatedDelivery(estimatedDelivery)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Associate items with saved order
        for (OrderItem oi : orderItemsToSave) {
            oi.setOrder(savedOrder);
            orderItemRepository.save(oi);
        }
        savedOrder.setItems(orderItemsToSave);

        // 6. Handle Prescription upload/attachment if Rx product
        if (request.getPrescriptionId() != null) {
            prescriptionRepository.findById(request.getPrescriptionId()).ifPresent(p -> {
                p.setOrder(savedOrder);
                prescriptionRepository.save(p);
                savedOrder.setPrescription(p);
            });
        } else if (request.getPrescriptionUrl() != null && !request.getPrescriptionUrl().isEmpty()) {
            Prescription newPrescription = Prescription.builder()
                    .user(user)
                    .order(savedOrder)
                    .doctorName(request.getDoctorName() != null ? request.getDoctorName() : "Doctor Consultation")
                    .patientName(customerName)
                    .prescriptionUrl(request.getPrescriptionUrl())
                    .status(PrescriptionStatus.PENDING)
                    .build();
            Prescription savedRx = prescriptionRepository.save(newPrescription);
            savedOrder.setPrescription(savedRx);
        }

        // 7. Create simulated Payment record
        PaymentMethod pMethod = PaymentMethod.COD;
        String reqMethod = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "COD";
        if (reqMethod.contains("CARD") || reqMethod.contains("CREDIT") || reqMethod.contains("VISA")) {
            pMethod = PaymentMethod.CARD;
        } else if (reqMethod.contains("BANK") || reqMethod.contains("TRANSFER")) {
            pMethod = PaymentMethod.BANK_TRANSFER;
        }

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(pMethod)
                .transactionReference("TXN-" + System.currentTimeMillis())
                .status(pMethod == PaymentMethod.CARD ? PaymentStatus.COMPLETED : PaymentStatus.PENDING)
                .amount(totalAmount)
                .paymentDate(java.time.LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
        savedOrder.setPayment(payment);

        // 8. Clear user cart
        cartService.clearCart(userEmail);

        log.info("Order created successfully: {} for user {}", orderNumber, userEmail);
        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        boolean isAdminOrPharmacist = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ROLE_PHARMACIST"));

        Order order;
        if (isAdminOrPharmacist) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        } else {
            order = orderRepository.findByIdAndUser(orderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        }

        return mapToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponseDTO> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<OrderResponseDTO> dtos = orderPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponse.<OrderResponseDTO>builder()
                .content(dtos)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + request.getStatus());
        }

        if (request.getTrackingId() != null && !request.getTrackingId().isEmpty()) {
            order.setTrackingId(request.getTrackingId());
        }

        Order saved = orderRepository.save(order);
        log.info("Updated order ID {} status to {}", orderId, saved.getStatus());
        return mapToDTO(saved);
    }

    private OrderResponseDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = orderItemRepository.findByOrder(order).stream()
                .map(oi -> {
                    Product p = oi.getProduct();
                    List<ProductImage> images = productImageRepository.findByProductOrderByDisplayOrderAsc(p);
                    String imgUrl = !images.isEmpty() ? images.get(0).getImageUrl() : "assets/images/medicine_1.png";

                    return OrderItemDTO.builder()
                            .id(oi.getId())
                            .productId(p.getId())
                            .name(oi.getProductName())
                            .productName(oi.getProductName())
                            .price(oi.getUnitPrice())
                            .unitPrice(oi.getUnitPrice())
                            .quantity(oi.getQuantity())
                            .subtotal(oi.getSubtotal())
                            .image(imgUrl)
                            .build();
                })
                .collect(Collectors.toList());

        Payment payment = paymentRepository.findByOrder(order).orElse(null);

        String orderDate = order.getCreatedAt() != null
                ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : LocalDate.now().toString();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .date(orderDate)
                .createdAt(order.getCreatedAt())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(payment != null ? payment.getStatus().name() : "PENDING")
                .transactionReference(payment != null ? payment.getTransactionReference() : null)
                .status(order.getStatus().name())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .discount(order.getDiscount())
                .total(order.getTotalAmount())
                .totalAmount(order.getTotalAmount())
                .trackingId(order.getTrackingId())
                .estimatedDelivery(order.getEstimatedDelivery())
                .items(itemDTOs)
                .prescriptionId(order.getPrescription() != null ? order.getPrescription().getId() : null)
                .prescriptionStatus(order.getPrescription() != null ? order.getPrescription().getStatus().name() : null)
                .build();
    }
}

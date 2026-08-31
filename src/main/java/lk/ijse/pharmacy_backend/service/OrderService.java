package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.common.PageResponse;
import lk.ijse.pharmacy_backend.dto.order.OrderCreateRequest;
import lk.ijse.pharmacy_backend.dto.order.OrderResponseDTO;
import lk.ijse.pharmacy_backend.dto.order.OrderStatusUpdateRequest;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(String userEmail, OrderCreateRequest request);

    List<OrderResponseDTO> getMyOrders(String userEmail);

    OrderResponseDTO getOrderById(String userEmail, Long orderId);

    PageResponse<OrderResponseDTO> getAllOrders(int page, int size);

    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
}

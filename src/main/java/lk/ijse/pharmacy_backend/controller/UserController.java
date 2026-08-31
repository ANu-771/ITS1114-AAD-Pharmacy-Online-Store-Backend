package lk.ijse.pharmacy_backend.controller;

import jakarta.validation.Valid;
import lk.ijse.pharmacy_backend.dto.auth.UserSummaryDTO;
import lk.ijse.pharmacy_backend.dto.order.OrderResponseDTO;
import lk.ijse.pharmacy_backend.dto.user.UserProfileUpdateDTO;
import lk.ijse.pharmacy_backend.service.AuthService;
import lk.ijse.pharmacy_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final OrderService orderService;

    @GetMapping("/profile")
    public ResponseEntity<UserSummaryDTO> getProfile(Authentication authentication) {
        UserSummaryDTO profile = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserSummaryDTO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateDTO dto
    ) {
        UserSummaryDTO updated = authService.updateProfile(authentication.getName(), dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(Authentication authentication) {
        List<OrderResponseDTO> orders = orderService.getMyOrders(authentication.getName());
        return ResponseEntity.ok(orders);
    }
}

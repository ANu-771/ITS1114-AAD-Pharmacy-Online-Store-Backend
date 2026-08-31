package lk.ijse.pharmacy_backend.controller;

import jakarta.validation.Valid;
import lk.ijse.pharmacy_backend.dto.cart.AddToCartRequest;
import lk.ijse.pharmacy_backend.dto.cart.CartResponseDTO;
import lk.ijse.pharmacy_backend.dto.cart.UpdateCartQuantityRequest;
import lk.ijse.pharmacy_backend.dto.common.ApiResponse;
import lk.ijse.pharmacy_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(Authentication authentication) {
        CartResponseDTO response = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request
    ) {
        CartResponseDTO response = cartService.addToCart(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponseDTO> updateQuantity(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartQuantityRequest request
    ) {
        CartResponseDTO response = cartService.updateQuantity(authentication.getName(), id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponseDTO> removeFromCart(
            Authentication authentication,
            @PathVariable Long id
    ) {
        CartResponseDTO response = cartService.removeFromCart(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared successfully"));
    }
}

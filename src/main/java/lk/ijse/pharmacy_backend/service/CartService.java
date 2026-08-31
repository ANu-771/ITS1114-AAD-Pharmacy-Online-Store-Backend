package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.cart.AddToCartRequest;
import lk.ijse.pharmacy_backend.dto.cart.CartResponseDTO;

public interface CartService {

    CartResponseDTO getCart(String userEmail);

    CartResponseDTO addToCart(String userEmail, AddToCartRequest request);

    CartResponseDTO updateQuantity(String userEmail, Long cartItemId, Integer quantity);

    CartResponseDTO removeFromCart(String userEmail, Long cartItemId);

    void clearCart(String userEmail);
}

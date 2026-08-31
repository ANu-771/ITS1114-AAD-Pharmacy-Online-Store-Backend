package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.wishlist.WishlistResponseDTO;

import java.util.List;

public interface WishlistService {

    List<WishlistResponseDTO> getWishlist(String userEmail);

    WishlistResponseDTO addToWishlist(String userEmail, Long productId);

    void removeFromWishlist(String userEmail, Long productId);
}

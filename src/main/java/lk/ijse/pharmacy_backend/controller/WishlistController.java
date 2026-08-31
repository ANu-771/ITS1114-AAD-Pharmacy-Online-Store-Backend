package lk.ijse.pharmacy_backend.controller;

import lk.ijse.pharmacy_backend.dto.common.ApiResponse;
import lk.ijse.pharmacy_backend.dto.wishlist.WishlistResponseDTO;
import lk.ijse.pharmacy_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistResponseDTO>> getWishlist(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.getWishlist(authentication.getName()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistResponseDTO> addToWishlist(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        WishlistResponseDTO dto = wishlistService.addToWishlist(authentication.getName(), productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        wishlistService.removeFromWishlist(authentication.getName(), productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Product removed from wishlist"));
    }
}

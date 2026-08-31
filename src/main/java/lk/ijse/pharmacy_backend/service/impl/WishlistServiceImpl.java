package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.product.ProductDTO;
import lk.ijse.pharmacy_backend.dto.wishlist.WishlistResponseDTO;
import lk.ijse.pharmacy_backend.entity.Product;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.entity.Wishlist;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.ProductRepository;
import lk.ijse.pharmacy_backend.repository.UserRepository;
import lk.ijse.pharmacy_backend.repository.WishlistRepository;
import lk.ijse.pharmacy_backend.service.ProductService;
import lk.ijse.pharmacy_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    @Transactional(readOnly = true)
    public List<WishlistResponseDTO> getWishlist(String userEmail) {
        User user = getUser(userEmail);
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WishlistResponseDTO addToWishlist(String userEmail, Long productId) {
        User user = getUser(userEmail);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        Wishlist wishlist = wishlistRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder()
                        .user(user)
                        .product(product)
                        .build()));

        return mapToDTO(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(String userEmail, Long productId) {
        User user = getUser(userEmail);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        wishlistRepository.deleteByUserAndProduct(user, product);
        log.info("Removed product ID {} from wishlist of user {}", productId, userEmail);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private WishlistResponseDTO mapToDTO(Wishlist wishlist) {
        ProductDTO pDto = productService.getProductById(wishlist.getProduct().getId());

        return WishlistResponseDTO.builder()
                .id(wishlist.getId())
                .productId(wishlist.getProduct().getId())
                .product(pDto)
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}

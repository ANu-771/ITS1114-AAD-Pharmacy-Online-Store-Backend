package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.review.ReviewRequestDTO;
import lk.ijse.pharmacy_backend.dto.review.ReviewResponseDTO;
import lk.ijse.pharmacy_backend.entity.Order;
import lk.ijse.pharmacy_backend.entity.Product;
import lk.ijse.pharmacy_backend.entity.Review;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.OrderRepository;
import lk.ijse.pharmacy_backend.repository.ProductRepository;
import lk.ijse.pharmacy_backend.repository.ReviewRepository;
import lk.ijse.pharmacy_backend.repository.UserRepository;
import lk.ijse.pharmacy_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(String userEmail, ReviewRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        // Check if verified purchase
        List<Order> userOrders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        boolean isVerified = userOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));

        Review review = reviewRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> Review.builder()
                        .user(user)
                        .product(product)
                        .build());

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setVerifiedPurchase(isVerified);

        Review saved = reviewRepository.save(review);

        // Update product average rating
        List<Review> allProductReviews = reviewRepository.findByProductOrderByCreatedAtDesc(product);
        double avg = allProductReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(5.0);

        product.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
        product.setReviewsCount(allProductReviews.size());
        productRepository.save(product);

        log.info("Saved review for product {} by user {}", product.getName(), userEmail);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        return reviewRepository.findByProductOrderByCreatedAtDesc(product).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO mapToDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .isVerifiedPurchase(review.isVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

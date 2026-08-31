package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.review.ReviewRequestDTO;
import lk.ijse.pharmacy_backend.dto.review.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO createReview(String userEmail, ReviewRequestDTO request);

    List<ReviewResponseDTO> getProductReviews(Long productId);
}

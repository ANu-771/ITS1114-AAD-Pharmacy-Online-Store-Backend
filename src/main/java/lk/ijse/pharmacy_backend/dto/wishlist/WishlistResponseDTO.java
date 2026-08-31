package lk.ijse.pharmacy_backend.dto.wishlist;

import lk.ijse.pharmacy_backend.dto.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistResponseDTO {

    private Long id;
    private Long productId;
    private ProductDTO product;
    private LocalDateTime createdAt;
}

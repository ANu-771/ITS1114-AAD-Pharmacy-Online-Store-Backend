package lk.ijse.pharmacy_backend.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String slug;

    private String description;

    private String iconClass;

    @Builder.Default
    private boolean active = true;

    private Long productCount;
}

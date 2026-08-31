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
public class BrandDTO {

    private Long id;

    @NotBlank(message = "Brand name is required")
    private String name;

    private String manufacturerCountry;

    @Builder.Default
    private boolean verified = true;
}

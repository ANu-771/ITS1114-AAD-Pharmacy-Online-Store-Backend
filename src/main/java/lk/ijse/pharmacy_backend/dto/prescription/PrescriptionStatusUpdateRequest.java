package lk.ijse.pharmacy_backend.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionStatusUpdateRequest {

    @NotBlank(message = "Status is required (APPROVED, REJECTED, UNDER_REVIEW)")
    private String status;

    private String notes;
}

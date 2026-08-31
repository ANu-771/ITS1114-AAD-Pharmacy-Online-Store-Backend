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
public class PrescriptionUploadRequest {

    private Long orderId;

    private String doctorName;

    private String patientName;

    @NotBlank(message = "Prescription image/URL is required")
    private String prescriptionUrl;

    private String notes;
}

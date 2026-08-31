package lk.ijse.pharmacy_backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusUpdateRequest {
    private Boolean enabled;
    private String status; // ACTIVE / DISABLED
}

package lk.ijse.pharmacy_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;
    private String currentPassword;
    private String newPassword;
}

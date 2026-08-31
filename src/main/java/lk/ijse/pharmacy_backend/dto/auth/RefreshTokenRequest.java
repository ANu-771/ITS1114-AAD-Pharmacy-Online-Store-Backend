package lk.ijse.pharmacy_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // Optional field if client sends { token: "..." }
    private String token;

    public String getTokenValue() {
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            return refreshToken.trim();
        }
        return token != null ? token.trim() : "";
    }
}

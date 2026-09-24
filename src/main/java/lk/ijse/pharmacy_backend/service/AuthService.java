package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.auth.*;
import lk.ijse.pharmacy_backend.dto.user.UserProfileUpdateDTO;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserSummaryDTO getCurrentUser(String email);

    UserSummaryDTO updateProfile(String email, UserProfileUpdateDTO dto);

    void sendForgotPasswordOtp(ForgotPasswordRequest request);

    boolean verifyPasswordResetOtp(VerifyOtpRequest request);

    void resetPasswordWithOtp(ResetPasswordRequest request);
}

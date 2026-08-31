package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.auth.AuthResponse;
import lk.ijse.pharmacy_backend.dto.auth.LoginRequest;
import lk.ijse.pharmacy_backend.dto.auth.RefreshTokenRequest;
import lk.ijse.pharmacy_backend.dto.auth.RegisterRequest;
import lk.ijse.pharmacy_backend.dto.auth.UserSummaryDTO;
import lk.ijse.pharmacy_backend.dto.user.UserProfileUpdateDTO;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserSummaryDTO getCurrentUser(String email);

    UserSummaryDTO updateProfile(String email, UserProfileUpdateDTO dto);
}

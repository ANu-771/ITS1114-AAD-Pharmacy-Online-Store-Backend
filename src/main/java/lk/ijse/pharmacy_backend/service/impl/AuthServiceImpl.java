package lk.ijse.pharmacy_backend.service.impl;

import io.jsonwebtoken.JwtException;
import lk.ijse.pharmacy_backend.dto.auth.*;
import lk.ijse.pharmacy_backend.dto.user.UserProfileUpdateDTO;
import lk.ijse.pharmacy_backend.entity.PasswordResetOtp;
import lk.ijse.pharmacy_backend.entity.Role;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.enumiration.UserRole;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.exception.UnauthorizedException;
import lk.ijse.pharmacy_backend.repository.PasswordResetOtpRepository;
import lk.ijse.pharmacy_backend.repository.RoleRepository;
import lk.ijse.pharmacy_backend.repository.UserRepository;
import lk.ijse.pharmacy_backend.security.JwtUtil;
import lk.ijse.pharmacy_backend.service.AuthService;
import lk.ijse.pharmacy_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email address is already in use: " + email);
        }

        Role userRole = roleRepository.findByName(UserRole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(UserRole.ROLE_USER).build()));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .enabled(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Successfully registered new user: {}", savedUser.getEmail());

        return createAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
            );

            User user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + identifier));

            if (!user.isEnabled()) {
                throw new UnauthorizedException("Account is disabled. Please contact pharmacy support.");
            }

            log.info("User {} logged in successfully", user.getEmail());
            return createAuthResponse(user);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for identifier: {}", identifier);
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getTokenValue();

        if (token == null || token.isEmpty()) {
            throw new BadRequestException("Refresh token cannot be blank");
        }

        try {
            String username = jwtUtil.extractUsername(token);

            if (jwtUtil.isTokenExpired(token)) {
                throw new UnauthorizedException("Refresh token has expired. Please sign in again.");
            }

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!user.isEnabled()) {
                throw new UnauthorizedException("User account is disabled");
            }

            return createAuthResponse(user);

        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid refresh token: " + e.getMessage());
        }
    }

    @Override
    public UserSummaryDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToSummary(user);
    }

    @Override
    @Transactional
    public UserSummaryDTO updateProfile(String email, UserProfileUpdateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName().trim());
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone().trim());
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
            if (dto.getCurrentPassword() == null || !passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password does not match");
            }
            if (dto.getNewPassword().length() < 6) {
                throw new BadRequestException("New password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToSummary(updatedUser);
    }

    @Override
    @Transactional
    public void sendForgotPasswordOtp(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Initiating password reset OTP flow for email: {}", email);

        // Find user if exists (do not leak existence to client)
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Return silently so attackers cannot enumerate valid user emails
            return;
        }

        User user = userOpt.get();

        // Generate cryptographically secure 6-digit OTP (e.g. 100000 - 999999)
        int randomPin = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(randomPin);

        // Save OTP entity (valid for 10 minutes)
        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .email(email)
                .otpCode(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .used(false)
                .build();

        passwordResetOtpRepository.save(resetOtp);
        log.info("Generated 6-digit OTP [{}] for user: {} (Expires in 10 mins)", otpCode, email);

        // Dispatch Email
        emailService.sendPasswordResetOtp(email, otpCode, user.getFullName());
    }

    @Override
    @Transactional
    public boolean verifyPasswordResetOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getOtpCode().trim();

        PasswordResetOtp otp = passwordResetOtpRepository
                .findTopByEmailAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired. Please request a new code.");
        }

        otp.setVerified(true);
        passwordResetOtpRepository.save(otp);
        log.info("Successfully verified OTP for email: {}", email);
        return true;
    }

    @Override
    @Transactional
    public void resetPasswordWithOtp(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getOtpCode().trim();
        String newPassword = request.getNewPassword().trim();

        if (newPassword.length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters");
        }

        PasswordResetOtp otp = passwordResetOtpRepository
                .findTopByEmailAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired. Please request a new code.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark OTP as consumed to prevent replay attacks
        otp.setUsed(true);
        otp.setVerified(true);
        passwordResetOtpRepository.save(otp);

        log.info("Successfully reset password for user: {}", email);
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        UserSummaryDTO userSummary = mapToSummary(user);
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .token(accessToken) // alias for compatibility
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime() / 1000)
                .user(userSummary)
                .roles(roleNames)
                .build();
    }

    private UserSummaryDTO mapToSummary(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toList());

        return UserSummaryDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(roleNames)
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

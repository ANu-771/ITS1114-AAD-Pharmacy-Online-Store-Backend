package lk.ijse.pharmacy_backend.service.impl;

import io.jsonwebtoken.JwtException;
import lk.ijse.pharmacy_backend.dto.auth.*;
import lk.ijse.pharmacy_backend.dto.user.UserProfileUpdateDTO;
import lk.ijse.pharmacy_backend.entity.Role;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.enumiration.UserRole;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.exception.UnauthorizedException;
import lk.ijse.pharmacy_backend.repository.RoleRepository;
import lk.ijse.pharmacy_backend.repository.UserRepository;
import lk.ijse.pharmacy_backend.security.JwtUtil;
import lk.ijse.pharmacy_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

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

package lk.ijse.pharmacy_backend.security;

import lk.ijse.pharmacy_backend.entity.Role;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.enumiration.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L);
    }

    @Test
    void generateAndValidateToken_Success() {
        Role role = Role.builder().id(1L).name(UserRole.ROLE_USER).build();
        User user = User.builder()
                .id(100L)
                .email("user@example.com")
                .fullName("Test Customer")
                .roles(new HashSet<>(Collections.singletonList(role)))
                .enabled(true)
                .build();

        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedEmail = jwtUtil.extractUsername(token);
        assertEquals("user@example.com", extractedEmail);

        assertFalse(jwtUtil.isTokenExpired(token));
    }
}

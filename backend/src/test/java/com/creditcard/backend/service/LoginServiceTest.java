
package com.creditcard.backend.service;

import com.creditcard.backend.dto.LoginRequest;
import com.creditcard.backend.dto.LoginResponse;
import com.creditcard.backend.entity.User;
import com.creditcard.backend.repository.UserRepository;
import com.creditcard.backend.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private LoginService loginService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = mock(JwtService.class);

        loginService = new LoginService(
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    private User createTestUser() {
        User user = new User(
                "Test User",
                "test@example.com",
                passwordEncoder.encode("TestPass123!")
        );

        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void shouldLoginWithValidCredentials() {
        User user = createTestUser();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        LoginResponse response = loginService.login(
                new LoginRequest("test@example.com", "TestPass123!")
        );

        assertEquals(1L, response.id());
        assertEquals("Test User", response.name());
        assertEquals("test@example.com", response.email());
        assertEquals("Login successful", response.message());
        assertEquals("test-jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());

        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(createTestUser()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.login(
                        new LoginRequest(
                                "test@example.com",
                                "WrongPassword123!"
                        )
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid email or password", exception.getReason());

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.login(
                        new LoginRequest(
                                "unknown@example.com",
                                "TestPass123!"
                        )
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldNormalizeEmailBeforeLookup() {
        User user = createTestUser();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        LoginResponse response = loginService.login(
                new LoginRequest(
                        "  TEST@EXAMPLE.COM  ",
                        "TestPass123!"
                )
        );

        assertEquals("test@example.com", response.email());

        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldNotExposePasswordInResponse() {
        User user = createTestUser();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        LoginResponse response = loginService.login(
                new LoginRequest("test@example.com", "TestPass123!")
        );

        assertFalse(response.toString().contains("TestPass123!"));
        assertFalse(response.toString().contains(user.getPasswordHash()));
    }

    @Test
    void shouldNeverGenerateTokenForInvalidCredentials() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(createTestUser()));

        assertThrows(
                ResponseStatusException.class,
                () -> loginService.login(
                        new LoginRequest("test@example.com", "invalid-password")
                )
        );

        verifyNoInteractions(jwtService);
    }
}

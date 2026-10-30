
package com.creditcard.backend.service;

import com.creditcard.backend.dto.SignupRequest;
import com.creditcard.backend.dto.SignupResponse;
import com.creditcard.backend.entity.User;
import com.creditcard.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {

        userRepository = mock(UserRepository.class);

        passwordEncoder = new BCryptPasswordEncoder();

        userService = new UserService(
                userRepository,
                passwordEncoder
        );
    }

    // TC_SERVICE_001
    // Verify email normalization and persistence

    @Test
    void shouldNormalizeEmailAndSaveUser() {

        SignupRequest request = new SignupRequest(
                "Test User",
                "  TEST@EXAMPLE.COM  ",
                "TestPass123!"
        );

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    assignId(user, 1L);

                    return user;
                });

        SignupResponse response = userService.register(request);

        assertEquals(1L, response.id());

        assertEquals(
                "test@example.com",
                response.email()
        );

        verify(userRepository)
                .existsByEmail("test@example.com");

        verify(userRepository)
                .saveAndFlush(any(User.class));
    }

    // TC_SERVICE_002
    // Verify password is BCrypt hashed

    @Test
    void shouldHashPasswordBeforeSaving() {

        SignupRequest request = new SignupRequest(
                "Test User",
                "secure@example.com",
                "TestPass123!"
        );

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> {

                    User user = invocation.getArgument(0);

                    assignId(user, 2L);

                    assertNotEquals(
                            request.password(),
                            user.getPasswordHash()
                    );

                    assertTrue(
                            passwordEncoder.matches(
                                    request.password(),
                                    user.getPasswordHash()
                            )
                    );

                    return user;
                });

        SignupResponse response = userService.register(request);

        assertEquals(2L, response.id());

        verify(userRepository)
                .saveAndFlush(any(User.class));
    }

    // TC_SERVICE_003
    // Verify duplicate email returns HTTP 409

    @Test
    void shouldRejectDuplicateEmail() {

        SignupRequest request = new SignupRequest(
                "Another User",
                "test@example.com",
                "TestPass123!"
        );

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.register(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(userRepository, never())
                .saveAndFlush(any(User.class));
    }

    // TC_SERVICE_004
    // Verify signup response never exposes password

    @Test
    void shouldNotReturnPasswordInSignupResponse() {

        SignupRequest request = new SignupRequest(
                "Test User",
                "safe@example.com",
                "SecretPass123!"
        );

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> {

                    User user = invocation.getArgument(0);

                    assignId(user, 3L);

                    return user;
                });

        SignupResponse response = userService.register(request);

        assertEquals(
                "safe@example.com",
                response.email()
        );

        assertFalse(
                response.toString().contains(request.password())
        );

        assertFalse(
                response.toString().contains("$2a$")
        );

        assertFalse(
                response.toString().contains("$2b$")
        );
    }

    // TC_SERVICE_005
    // Reject passwords exceeding BCrypt's 72-byte limit

    @Test
    void shouldRejectPasswordExceeding72Bytes() {

        String longPassword = "a".repeat(73);

        SignupRequest request = new SignupRequest(
                "Test User",
                "longpassword@example.com",
                longPassword
        );

        assertTrue(
                longPassword.getBytes(StandardCharsets.UTF_8).length > 72
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.register(request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(userRepository, never())
                .saveAndFlush(any(User.class));
    }

    // TC_SERVICE_006
    // Verify a database uniqueness conflict is handled

    @Test
    void shouldHandleDatabaseConstraintViolation() {

        SignupRequest request = new SignupRequest(
                "Test User",
                "conflict@example.com",
                "TestPass123!"
        );

        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "Unique email constraint violated"
                        )
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.register(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }

    // Simulates an ID assigned by JPA during persistence.
    // No public setId() method is required in User.java.

    private void assignId(User user, Long id) {

        ReflectionTestUtils.setField(
                user,
                "id",
                id
        );
    }
}

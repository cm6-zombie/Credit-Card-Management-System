
package com.creditcard.backend.controller;

import com.creditcard.backend.dto.LoginResponse;
import com.creditcard.backend.exception.GlobalExceptionHandler;
import com.creditcard.backend.service.LoginService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginControllerTest {

    private MockMvc mockMvc;
    private LoginService loginService;

    @BeforeEach
    void setup() {
        loginService = mock(LoginService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new LoginController(loginService)
                )
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {

        when(loginService.login(any())).thenReturn(
                new LoginResponse(
                        1L,
                        "Test User",
                        "test@example.com",
                        "Login successful",
                        "test-jwt-token",
                        "Bearer"
                )
        );

        String json = """
                {
                    "email": "test@example.com",
                    "password": "TestPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email")
                        .value("test@example.com"))
                .andExpect(jsonPath("$.message")
                        .value("Login successful"))
                .andExpect(jsonPath("$.accessToken")
                        .value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(loginService).login(any());
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception {

        when(loginService.login(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        String json = """
                {
                    "email": "test@example.com",
                    "password": "WrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void shouldRejectInvalidEmailFormat() throws Exception {

        String json = """
                {
                    "email": "invalid-email",
                    "password": "TestPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email")
                        .value("Enter a valid email address"));

        verifyNoInteractions(loginService);
    }

    @Test
    void shouldRejectMissingPassword() throws Exception {

        String json = """
                {
                    "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password is required"));

        verifyNoInteractions(loginService);
    }
}

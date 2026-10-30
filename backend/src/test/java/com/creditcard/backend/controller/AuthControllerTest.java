
package com.creditcard.backend.controller;

import com.creditcard.backend.dto.SignupResponse;
import com.creditcard.backend.exception.GlobalExceptionHandler;
import com.creditcard.backend.service.UserService;

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

class AuthControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setup() {

        userService = mock(UserService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // TC_AUTH_001 - Successful signup

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        SignupResponse response = new SignupResponse(
                1L,
                "Test User",
                "testuser@example.com",
                "User registered successfully"
        );

        when(userService.register(any())).thenReturn(response);

        String requestBody = """
                {
                    "name": "Test User",
                    "email": "testuser@example.com",
                    "password": "TestPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email")
                        .value("testuser@example.com"))
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"));

        verify(userService).register(any());
    }

    // TC_AUTH_002 - Duplicate email

    @Test
    void shouldRejectDuplicateEmail() throws Exception {

        String requestBody = """
                {
                    "name": "Another User",
                    "email": "testuser@example.com",
                    "password": "AnotherPass123!"
                }
                """;

        when(userService.register(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already registered"
                ));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());

        verify(userService).register(any());
    }

    // TC_AUTH_003 - Invalid email

    @Test
    void shouldRejectInvalidEmail() throws Exception {

        String requestBody = """
                {
                    "name": "Invalid User",
                    "email": "invalid-email",
                    "password": "TestPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // TC_AUTH_004 - Short password

    @Test
    void shouldRejectShortPassword() throws Exception {

        String requestBody = """
                {
                    "name": "Short Password User",
                    "email": "short@example.com",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // TC_AUTH_005 - Empty name

    @Test
    void shouldRejectEmptyName() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "email": "emptyname@example.com",
                    "password": "TestPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // TC_AUTH_006 - Missing password

    @Test
    void shouldRejectMissingPassword() throws Exception {

        String requestBody = """
                {
                    "name": "Missing Password User",
                    "email": "missing@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // TC_AUTH_007 - Structured validation error

    @Test
    void shouldReturnStructuredValidationError() throws Exception {

        String requestBody = """
                {
                    "name": "Test User",
                    "email": "invalid-email",
                    "password": "TestPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .value("Enter a valid email address"));

        verifyNoInteractions(userService);
    }

    // TC_AUTH_008 - Structured duplicate-email error

    @Test
    void shouldReturnStructuredDuplicateEmailError() throws Exception {

        String requestBody = """
                {
                    "name": "Another User",
                    "email": "testuser@example.com",
                    "password": "AnotherPass123!"
                }
                """;

        when(userService.register(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already registered"
                ));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Email already registered"))
                .andExpect(jsonPath("$.fieldErrors").isMap());

        verify(userService).register(any());
    }
}


package com.creditcard.backend.controller;

import com.creditcard.backend.dto.SignupResponse;
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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class AuthControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setup() {

        userService = mock(UserService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(userService))
                .build();
    }

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
                .andExpect(jsonPath("$.email")
                        .value("testuser@example.com"));

        verify(userService).register(any());
    }

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

}

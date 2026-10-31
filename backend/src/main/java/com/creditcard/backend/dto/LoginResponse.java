package com.creditcard.backend.dto;

public record LoginResponse(
        Long id,
        String name,
        String email,
        String message,
        String accessToken,
        String tokenType
) {
}
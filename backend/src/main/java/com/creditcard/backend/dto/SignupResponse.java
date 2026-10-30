
package com.creditcard.backend.dto;

public record SignupResponse(
    Long id,
    String name,
    String email,
    String message
) {}

package com.cobroapp.backend.dto.auth;

public record LoginResponse(
        Long id,
        String email,
        String message,
        String accessToken,
        String tokenType
) {}
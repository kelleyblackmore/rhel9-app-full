package com.kelleyblackmore.secureledger.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        String username,
        String role
) {
}

package com.utp.productosapi.dto;

public record TokenResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds) {
}

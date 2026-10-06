package com.miniloja.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Resposta da autenticação")
public record LoginResponse(
        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,
        @Schema(description = "Token JWT")
        String accessToken) {
}

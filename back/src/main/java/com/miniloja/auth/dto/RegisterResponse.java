package com.miniloja.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterResponse", description = "Resposta do cadastro de usuário")
public record RegisterResponse(
        @Schema(description = "ID do usuário criado")
        Long id,

        @Schema(description = "Nome completo")
        String nomeCompleto,

        @Schema(description = "E-mail")
        String email,

        @Schema(description = "Telefone/WhatsApp (opcional)", nullable = true)
        String telefone) {
}

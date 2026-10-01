package com.miniloja.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UserUpdateRequest", description = "Dados para atualização de usuário")
public record UserUpdateRequest(
        @Schema(description = "Nome completo", example = "Rodrigo Alencar")
        @NotBlank(message = "nomeCompleto é obrigatório")
        @Size(max = 120, message = "nomeCompleto deve ter no máximo 120 caracteres")
        String nomeCompleto,

        @Schema(description = "E-mail (login)", example = "rodrigo@mercadinhoesperanca.com.br")
        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        @Size(max = 254, message = "email deve ter no máximo 254 caracteres")
        String email,

        @Schema(description = "Telefone/WhatsApp (opcional)", example = "(11) 98765-4321", nullable = true)
        @Size(max = 30, message = "telefone deve ter no máximo 30 caracteres")
        String telefone) {
}

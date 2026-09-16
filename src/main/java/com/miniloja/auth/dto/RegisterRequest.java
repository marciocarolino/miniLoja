package com.miniloja.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Dados para cadastro de usuário")
public record RegisterRequest(
        @Schema(description = "Nome completo do usuário", example = "Rodrigo Alencar")
        @NotBlank(message = "nomeCompleto é obrigatório")
        @Size(max = 120, message = "nomeCompleto deve ter no máximo 120 caracteres")
        String nomeCompleto,

        @Schema(description = "E-mail profissional ou de acesso (único)", example = "rodrigo@mercadinhoesperanca.com.br")
        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        @Size(max = 254, message = "email deve ter no máximo 254 caracteres")
        String email,

        @Schema(description = "Telefone/WhatsApp (opcional)", example = "(11) 98765-4321", nullable = true)
        @Size(max = 30, message = "telefone deve ter no máximo 30 caracteres")
        String telefone,

        @Schema(description = "Senha", example = "Abcdef12")
        @NotBlank(message = "senha é obrigatória")
        @Size(min = 8, max = 72, message = "senha deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "senha deve conter ao menos 1 letra maiúscula, 1 letra minúscula e 1 número")
        String senha,

        @Schema(description = "Confirmação de senha", example = "Abcdef12")
        @NotBlank(message = "confirmarSenha é obrigatória")
        String confirmarSenha,

        @Schema(description = "Aceite dos termos de uso e política de privacidade", example = "true")
        boolean aceitouTermos) {
}

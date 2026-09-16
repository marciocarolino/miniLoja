package com.miniloja.auth.controller;

import com.miniloja.auth.dto.RegisterResponse;
import com.miniloja.auth.service.UserAccountQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Usuários", description = "Consulta de usuários cadastrados")
public class UserAccountController {

    private final UserAccountQueryService userAccountQueryService;

    public UserAccountController(UserAccountQueryService userAccountQueryService) {
        this.userAccountQueryService = userAccountQueryService;
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários cadastrados")
    public List<RegisterResponse> listAll() {
        return userAccountQueryService.listAll();
    }

    @GetMapping("/by-email")
    @Operation(summary = "Buscar usuário por e-mail", description = "Retorna o usuário cadastrado pelo e-mail informado")
    public RegisterResponse getByEmail(
            @RequestParam("email") @NotBlank @Email String email) {
        return userAccountQueryService.getByEmail(email);
    }
}

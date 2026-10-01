package com.miniloja.auth.controller;

import com.miniloja.auth.dto.RegisterResponse;
import com.miniloja.auth.dto.UserUpdateRequest;
import com.miniloja.auth.service.UserAccountCommandService;
import com.miniloja.auth.service.UserAccountQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Usuários", description = "Consulta e manutenção de usuários cadastrados")
public class UserAccountController {

    private final UserAccountQueryService userAccountQueryService;
    private final UserAccountCommandService userAccountCommandService;

    public UserAccountController(
            UserAccountQueryService userAccountQueryService, UserAccountCommandService userAccountCommandService) {
        this.userAccountQueryService = userAccountQueryService;
        this.userAccountCommandService = userAccountCommandService;
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários ATIVOS cadastrados")
    public List<RegisterResponse> listAll() {
        return userAccountQueryService.listAll();
    }

    @GetMapping("/by-email")
    @Operation(summary = "Buscar usuário por e-mail", description = "Retorna o usuário ATIVO cadastrado pelo e-mail informado")
    public RegisterResponse getByEmail(@RequestParam("email") @NotBlank @Email String email) {
        return userAccountQueryService.getByEmail(email);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza nome, e-mail e telefone do usuário")
    public RegisterResponse update(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userAccountCommandService.update(id, request);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desativar usuário", description = "Desativa (soft delete) um usuário. Usuário desativado não pode autenticar.")
    public void deactivate(@PathVariable("id") Long id) {
        userAccountCommandService.deactivate(id);
    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reativar usuário", description = "Reativa um usuário desativado")
    public void activate(@PathVariable("id") Long id) {
        userAccountCommandService.activate(id);
    }
}

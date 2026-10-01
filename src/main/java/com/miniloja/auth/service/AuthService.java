package com.miniloja.auth.service;

import com.miniloja.auth.dto.LoginRequest;
import com.miniloja.auth.dto.LoginResponse;
import com.miniloja.auth.dto.RegisterRequest;
import com.miniloja.auth.dto.RegisterResponse;
import com.miniloja.auth.model.UserAccount;
import com.miniloja.auth.repository.UserAccountRepository;
import com.miniloja.auth.security.JwtService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository userAccountRepository, JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String emailNorm = request.email() == null ? null : request.email().trim();

        if (emailNorm == null || emailNorm.isBlank()) {
            throw new IllegalArgumentException("email é obrigatório");
        }

        UserAccount user =
                userAccountRepository
                        .findByEmailIgnoreCaseAndAtivadoTrue(emailNorm)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário ou senha inválidos."));

        if (!passwordEncoder.matches(request.senha(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new LoginResponse("Bearer", token);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.aceitouTermos()) {
            throw new IllegalArgumentException("É necessário aceitar os termos para continuar.");
        }

        if (!request.senha().equals(request.confirmarSenha())) {
            throw new IllegalArgumentException("Senha e confirmação de senha não conferem.");
        }

        String emailNorm = request.email() == null ? null : request.email().trim();

        if (emailNorm == null || emailNorm.isBlank()) {
            throw new IllegalArgumentException("email é obrigatório");
        }

        if (userAccountRepository.existsByEmailIgnoreCase(emailNorm)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        UserAccount user = new UserAccount();
        user.setNomeCompleto(request.nomeCompleto().trim());
        user.setEmail(emailNorm);
        user.setTelefone(request.telefone());
        user.setAceitouTermos(true);
        user.setAtivado(true);
        user.setPasswordHash(passwordEncoder.encode(request.senha()));

        UserAccount saved = userAccountRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getNomeCompleto(), saved.getEmail(), saved.getTelefone());
    }
}

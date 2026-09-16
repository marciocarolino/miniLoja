package com.miniloja.auth.service;

import com.miniloja.auth.dto.RegisterResponse;
import com.miniloja.auth.model.UserAccount;
import com.miniloja.auth.repository.UserAccountRepository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountQueryService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountQueryService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<RegisterResponse> listAll() {
        return userAccountRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegisterResponse getByEmail(String email) {
        String emailNorm = email == null ? null : email.trim();
        if (emailNorm == null || emailNorm.isBlank()) {
            throw new IllegalArgumentException("email é obrigatório");
        }

        UserAccount user =
                userAccountRepository
                        .findByEmailIgnoreCase(emailNorm)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para o e-mail informado."));

        return toResponse(user);
    }

    private RegisterResponse toResponse(UserAccount user) {
        return new RegisterResponse(user.getId(), user.getNomeCompleto(), user.getEmail(), user.getTelefone());
    }
}

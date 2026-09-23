package com.miniloja.auth.service;

import com.miniloja.auth.dto.RegisterResponse;
import com.miniloja.auth.dto.UserUpdateRequest;
import com.miniloja.auth.model.UserAccount;
import com.miniloja.auth.repository.UserAccountRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountCommandService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountCommandService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public RegisterResponse update(Long id, UserUpdateRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("id é obrigatório");
        }

        UserAccount user =
                userAccountRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        user.setNomeCompleto(request.nomeCompleto().trim());
        user.setEmail(request.email().trim());
        user.setTelefone(request.telefone());

        try {
            UserAccount saved = userAccountRepository.save(user);
            return new RegisterResponse(saved.getId(), saved.getNomeCompleto(), saved.getEmail(), saved.getTelefone());
        } catch (DataIntegrityViolationException e) {
            // Possível violação de unique constraint do e-mail.
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
    }

    @Transactional
    public void deactivate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id é obrigatório");
        }

        UserAccount user =
                userAccountRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        user.setAtivado(false);
        userAccountRepository.save(user);
    }

    @Transactional
    public void activate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id é obrigatório");
        }

        UserAccount user =
                userAccountRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        user.setAtivado(true);
        userAccountRepository.save(user);
    }
}

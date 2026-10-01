# Endpoints da API — MiniLoja

> Documento de referência para integração entre back-end e front-end.
>
> Regra: os agentes de front-end devem consultar este arquivo antes de criar chamadas HTTP, telas, formulários ou integrações com a API.
>
> Última atualização: 23/09/2026

## Informações gerais

- Base URL local: `http://localhost:8080`
- Formato padrão de requisição: `application/json`
- Formato padrão de resposta: `application/json`
- Autenticação: definir conforme implementação do Spring Security.
- Documentação interativa, quando disponível: `/swagger-ui/index.html`

## Autenticação

> Status: Implementado.

### Rate limit (anti abuso)

- `POST /api/auth/login`: **10 requisições por minuto por IP**.
- Endpoints autenticados (ex.: `/api/users/**`): **120 requisições por minuto por usuário autenticado (subject/email do JWT)**.
- Ao exceder: API retorna `429 Too Many Requests` e pode enviar header `Retry-After`.
- Implementação: **Redis (rate limit distribuído)** com fallback em memória caso Redis esteja indisponível (dev).

### Login (JWT)

- Status: Implementado
- Método: `POST`
- Rota: `/api/auth/login`
- Autenticação: não (endpoint público)
- Objetivo: autentica via e-mail e senha e retorna um token JWT para uso em endpoints protegidos.

#### Payload esperado

```json
{
  "email": "rodrigo@mercadinhoesperanca.com.br",
  "senha": "Abcdef12"
}
```

#### Resposta esperada — 200 OK

```json
{
  "tokenType": "Bearer",
  "accessToken": "<jwt>"
}
```

#### Possíveis erros

|  Status | Quando ocorre                                                        |
| ------: | -------------------------------------------------------------------- |
|     400 | Payload inválido (validações)                                        |
| 401/403 | Credenciais inválidas ou usuário desativado (conforme implementação) |

---

## Usuários

> Status: Implementado.

### Cadastrar usuário

- Status: Implementado
- Método: `POST`
- Rota: `/api/auth/register`
- Autenticação: não (endpoint público — fase inicial)
- Objetivo: cria um usuário (inicia com `ativado=true`).

### Listar usuários ativos

- Status: Implementado
- Método: `GET`
- Rota: `/api/users`
- Autenticação: sim (Bearer JWT)
- Objetivo: lista usuários ativos.

### Buscar usuário ativo por e-mail

- Status: Implementado
- Método: `GET`
- Rota: `/api/users/by-email?email=...`
- Autenticação: sim (Bearer JWT)
- Objetivo: busca usuário ativo pelo e-mail.

### Atualizar usuário

- Status: Implementado
- Método: `PUT`
- Rota: `/api/users/{id}`
- Autenticação: sim (Bearer JWT)
- Objetivo: atualiza nome, e-mail e telefone.

### Desativar usuário (soft delete)

- Status: Implementado
- Método: `POST`
- Rota: `/api/users/{id}/deactivate`
- Autenticação: sim (Bearer JWT)
- Objetivo: desativa um usuário (`ativado=false`). Usuário desativado não autentica.

### Reativar usuário

- Status: Implementado
- Método: `POST`
- Rota: `/api/users/{id}/activate`
- Autenticação: sim (Bearer JWT)
- Objetivo: reativa um usuário (`ativado=true`).

---

## Convenções de resposta

### Sucesso

````json
{
  "id": 1
}
```text

### Erro de validação

```json
{
  "timestamp": "2026-09-17T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/exemplo"
}
```text

### Erro de recurso não encontrado

```json
{
  "timestamp": "2026-09-17T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Recurso não encontrado",
  "path": "/api/exemplo/1"
}
```text

---

# Produtos

> Status: Ainda não implementado.

## Criar produto

- Método: `POST`
- Rota: `/api/produtos`
- Autenticação: a definir
- Descrição: cadastra um novo produto.

### Payload esperado

```json
{
  "nome": "Café 500g",
  "preco": 18.90,
  "quantidadeEstoque": 20
}
```text

### Parâmetros

| Local | Nome | Tipo | Obrigatório | Descrição |
|---|---|---:|---|---|
| Body | `nome` | string | Sim | Nome do produto |
| Body | `preco` | decimal | Sim | Deve ser maior que zero |
| Body | `quantidadeEstoque` | inteiro | Sim | Não pode ser negativo |

### Resposta esperada — 201 Created

```json
{
  "id": 1,
  "nome": "Café 500g",
  "preco": 18.90,
  "quantidadeEstoque": 20,
  "dataCadastro": "2026-09-17T10:30:00"
}
```text

### Possíveis erros

| Status | Quando ocorre |
|---:|---|
| 400 | Campos obrigatórios ausentes ou valores inválidos |
| 401 | Usuário não autenticado |
| 403 | Usuário sem permissão |
````

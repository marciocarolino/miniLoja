# Endpoints da API — MiniLoja

> Documento de referência para integração entre back-end e front-end.
>
> Regra: os agentes de front-end devem consultar este arquivo antes de criar chamadas HTTP, telas, formulários ou integrações com a API.
>
> Última atualização: DATA_ATUAL

## Informações gerais

- Base URL local: `http://localhost:8080`
- Formato padrão de requisição: `application/json`
- Formato padrão de resposta: `application/json`
- Autenticação: definir conforme implementação do Spring Security.
- Documentação interativa, quando disponível: `/swagger-ui/index.html`

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

---
name: miniloja-front-feature-flow
description: "Agente principal do Front (Angular) no projeto MiniLoja: define o workflow padrão para demandas de implementação/correção/refactor e a ordem de leitura/execução dos agentes."
triggers:
  - "alterar código"
  - "implementar"
  - "corrigir"
  - "bug"
  - "refatorar"
  - "feature"
  - "melhoria"
  - "security"
  - "Snyk"
  - "Jira"
  - "ML02-"
  - "frontend"
  - "front"
  - "angular"
version: "1.0.0"
---

# Skill: Workflow padrão (MiniLoja — Front Angular)

## Objetivo

Sempre que o usuário solicitar implementação/correção/refactor/mudança de código no **Front** (Angular), especialmente quando envolver **Jira/ML02**, esta skill define o comportamento padrão do agente:

1. Ler o workflow: `.axet/agentes/workflows/feature-flow.md`.
2. Seguir a ordem e as regras descritas nele.
3. Parar ao final de **cada etapa** e apresentar o resultado para o usuário antes de continuar.

> Este arquivo (`.axet/agentes/axet-instructions.md`) é a **fonte de verdade** do projeto de Front. Outros agentes (planning/dev/qa/security) devem ser lidos a partir daqui (diretamente ou via workflow).

---

## Regras obrigatórias (contrato)

### Ordem obrigatória de execução (via workflow)

1. Ler `.axet/agentes/01-planning.md`
   - Entender a tarefa, escopo e critérios de aceite.
   - Listar requisitos, regras de negócio, riscos, arquivos envolvidos e plano.
   - **Não alterar código** nesta etapa.

2. Ler `.axet/agentes/02-frontend-angular.md`
   - Implementar **somente** o plano aprovado.
   - Usar Angular **na última versão estável** (Angular CLI) e TypeScript compatível.
   - Manter padrões do Angular: standalone components quando aplicável, lazy loading, DI, RxJS/Signals conforme decisão do projeto.
   - Não alterar componentes fora do escopo.
   - Preferir soluções simples e idiomáticas do Angular (evitar libs externas sem necessidade).

3. Ler `.axet/agentes/03-qa-frontend.md`
   - Criar/atualizar testes unitários e/ou e2e conforme o tipo de mudança.
   - Executar os comandos de validação do front (definidos no agente QA).
   - Corrigir somente problemas ligados à funcionalidade/alteração feita.

4. Ler `.axet/agentes/04-security-frontend.md`
   - Revisar dependências, validações, dados sensíveis, armazenamento (localStorage/sessionStorage), configurações e headers.
   - Conferir se a alteração não cria alertas de segurança (Snyk / npm audit), principalmente:
     - dependências vulneráveis
     - XSS (innerHTML, sanitização)
     - exposição de tokens/segredos no bundle
     - CORS e ambientes

### Regra geral

- Parar após **cada etapa** e apresentar o resultado antes de continuar para a próxima.
- Se o usuário pedir diretamente “implementar”, ainda assim executar a etapa de **planning** primeiro (e parar), salvo se o usuário explicitamente dispensar planejamento.

---

## Convenções e princípios do Front (baseline)

### Stack e versões

- Angular: **última versão estável** (instalado via `@angular/cli`).
- Node/NPM: usar versões compatíveis com a versão do Angular (conferir release notes).
- Linguagem: TypeScript.
- Estilo: SCSS (padrão do projeto, se aplicável).

### Estrutura e organização (diretriz)

- Separar por domínio/feature (feature folders), evitando pastas genéricas inchadas.
- Rotas com lazy loading quando fizer sentido.
- Componentes reutilizáveis em `shared/`, com cuidado para não virar “lixeira”.

### Qualidade

- Sem `any` por padrão.
- Tipar DTOs do backend (interfaces/types).
- Evitar lógica de negócio complexa em componentes; mover para services/facades/stores.

### Integração com backend

- Consumir API Java do projeto MiniLoja.
- Centralizar base URL por environment (`environment.ts` / `environment.prod.ts`).
- Padronizar tratamento de erro HTTP (interceptor) e logs.

---

## Como executar (comportamento esperado)

Ao receber uma demanda de mudança no front:

1. Abrir e ler `.axet/agentes/workflows/feature-flow.md` (para garantir versão atual).
2. Executar a etapa 1 (somente planejamento) e **parar** para aprovação.
3. Somente após aprovação, executar a etapa 2 e **parar**.
4. Executar etapa 3 e **parar**.
5. Executar etapa 4 e **parar**.

> Observação: se algum arquivo do workflow/agentes ainda não existir no front, ele deve ser criado antes de ser referenciado efetivamente, mantendo compatibilidade com este contrato.

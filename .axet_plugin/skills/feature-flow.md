---
name: miniloja-feature-flow
description: "Aplica o workflow padrão do projeto MiniLoja: ler .axet/agents/workflows/feature-flow.md e seguir os agentes em ordem, parando a cada etapa para aprovação."
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
version: "1.0.0"
---

# Skill: Workflow padrão (MiniLoja)

## Objetivo

Sempre que o usuário solicitar implementação/correção/refactor/mudança de código (especialmente quando envolver Jira/ML02), esta skill define como comportamento padrão:

1. Ler o arquivo `.axet/agents/workflows/feature-flow.md`.
2. Seguir a ordem e regras descritas nele.
3. Parar ao final de **cada etapa** e apresentar o resultado para o usuário antes de continuar.

## Regras obrigatórias (copiadas do workflow)

### Ordem obrigatória

1. Ler `.axet/agents/01-planning.md`
   - Entender a tarefa.
   - Listar requisitos, regras de negócio, arquivos envolvidos e plano.
   - Não alterar código.

2. Ler `.axet/agents/02-backend-spring.md`
   - Implementar somente o plano aprovado.
   - Usar Java 21, Spring Boot, JPA e H2.
   - Não alterar componentes fora do escopo.

3. Ler `.axet/agents/03-qa.md`
   - Criar ou atualizar testes.
   - Executar:
     ````powershell
     .\mvnw.cmd clean test
     ```text
     ````
   - Corrigir somente problemas ligados à funcionalidade.

4. Ler `.axet/agents/04-security.md`
   - Revisar dependências, validações, dados sensíveis e configurações.
   - Conferir se a alteração não cria alertas no Snyk.

### Regra geral

Pare após cada etapa e apresente o resultado antes de continuar para a próxima.

## Como executar (comportamento esperado)

- Ao receber uma demanda de mudança:
  - Abrir e ler `.axet/agents/workflows/feature-flow.md` (para garantir versão atual).
  - Executar a etapa 1 (somente planejamento) e **parar**.
  - Perguntar/aguardar aprovação do usuário.
  - Somente então prosseguir para etapa 2, e assim por diante.

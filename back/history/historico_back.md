### Regras

1. Crie a pasta `history` caso ela não exista.
2. Crie `history/historico_back.txt` caso ele não exista.
3. Nunca apague ou substitua registros anteriores.
4. Sempre adicione o novo registro ao final do arquivo.
5. Use a data e hora atuais no formato `dd/MM/yyyy HH:mm:ss`.
6. Não registre dados sensíveis, como senhas, tokens, chaves ou credenciais.
7. Registre o resultado real: não declare testes, endpoints ou arquivos como concluídos se não foram realmente criados, alterados ou executados.

### Formato obrigatório do histórico

```text
============================================================
Task Completed - DATA_ATUAL
Tarefa: NOME_DA_TAREFA
Card Jira: CHAVE_DO_CARD_OU_NÃO_INFORMADO
Agente: Agente de Implementação Back-end Spring
============================================================

Resumo da implementação

DESCREVA A FUNCIONALIDADE IMPLEMENTADA.

Arquivos criados ou alterados

- ARQUIVO: MOTIVO DA ALTERAÇÃO.
- ARQUIVO: MOTIVO DA ALTERAÇÃO.

Endpoints criados ou alterados

- MÉTODO ROTA: DESCRIÇÃO.
- Se não houver alteração de endpoint, escreva: Nenhum endpoint alterado.

Regras e validações aplicadas

- REGRA IMPLEMENTADA.
- VALIDAÇÃO IMPLEMENTADA.

Testes executados

- Comando: .\mvnw.cmd clean test
- Resultado: BUILD SUCCESS ou descrição real da falha.
- Testes criados ou alterados: LISTA.

Documentação atualizada

- docs/endpoints.md: DESCREVA A ALTERAÇÃO.
- Se não houver alteração, escreva: Nenhuma alteração necessária.

Pendências, riscos ou próximos passos

- LISTE PENDÊNCIAS REAIS.
- Se não houver, escreva: Nenhuma pendência identificada.

------------------------------------------------------------
```

Ao concluir a implementação, registre o resultado detalhado no arquivo:

```text
history/historico_back.md
```

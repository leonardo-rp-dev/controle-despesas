# Prompts COSTAR - 4 - Release v0.3.0 (Edição e exclusão)

Escopo da v0.3.0 (docs/backlog.md): RF-06, RF-07, RF-08, RT-08.
Rotas desta release: `PUT /api/despesas/{id}` e `DELETE /api/despesas/{id}`. GET por id e filtro por mês não fazem parte dela.

Ordem de execução: 1 → 2 → 3 → 4 → 5.

| # | Prompt | Itens cobertos |
|---|---|---|
| 1 | Exceção de recurso não encontrado e erros 404/400 de rota | RF-07 |
| 2 | Service: editar e excluir | RF-06, RF-07, RF-08 |
| 3 | Controller: PUT e DELETE | RF-06, RF-07, RF-08 |
| 4 | Interface de edição e exclusão | RT-08 |
| 5 | Testes da release | RF-06, RF-07, RF-08 |

## 1. Criar a exceção de recurso não encontrado

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.
Ler antes: docs/backlog.md (RF-07), exception/ValidacaoException.java e exception/GlobalExceptionHandler.java.
Erros da API seguem o formato dto/ErroResposta {status, mensagem, timestamp}.
O handler genérico de Exception devolve 500; um id não numérico na rota (ex.: /api/despesas/abc) lança MethodArgumentTypeMismatchException, que hoje cairia nele.

# Objetivo
1. Criar exception/NaoEncontradoException (RuntimeException) com a mensagem do erro.
2. Alterar o GlobalExceptionHandler para converter:
   - NaoEncontradoException → HTTP 404 com a mensagem da exceção
   - MethodArgumentTypeMismatchException → HTTP 400 com mensagem "Identificador inválido"
Manter os handlers existentes sem alteração de comportamento.

# Estilo
Spring Boot idiomático, mensagens em português, sem dependências novas.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar/alterar os arquivos em disco, sem exibir o código. Ao final, listar os arquivos alterados e confirmar que `./mvnw compile` passa.
````

## 2. Criar editar e excluir no service

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.
Ler antes: docs/escopo-mvp.md (RF-06, RF-07, RF-08, RNF-09, RNF-13), docs/backlog.md (v0.3.0), service/DespesaService.java e exception/NaoEncontradoException.java.
Por RNF-09, regras e validações ficam no service. A edição aplica as mesmas validações do cadastro.

# Objetivo
Alterar service/DespesaService.java:
1. atualizar(Long id, DespesaRequest): busca a despesa pelo id (NaoEncontradoException se não existir), valida com as mesmas regras do cadastro, atualiza descrição, valor (em centavos) e data, salva e devolve DespesaResponse.
2. excluir(Long id): lança NaoEncontradoException se o id não existir; caso contrário remove a despesa.
Reaproveitar a validação e a conversão já existentes, sem duplicar código. A busca por id deve acontecer antes da validação do corpo, e nada é salvo quando alguma delas falha. Usar @Transactional nos métodos de escrita se necessário para a atualização.
Mensagem de não encontrado: "Despesa não encontrada".

# Estilo
Métodos curtos, sem Lombok, sem alterar o comportamento de cadastrar e listar.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Alterar o arquivo em disco, sem exibir o código. Ao final, listar os métodos adicionados, as mensagens de erro e confirmar que `./mvnw compile` passa.
````

## 3. Criar as rotas PUT e DELETE

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.controller.
Ler antes: docs/escopo-mvp.md (RF-06, RF-07, RF-08, RNF-09), docs/backlog.md (v0.3.0), controller/DespesaController.java e service/DespesaService.java.
Os erros saem pelo GlobalExceptionHandler (404 para id inexistente, 400 para validação).

# Objetivo
Alterar controller/DespesaController.java para incluir:
1. PUT /api/despesas/{id}: recebe DespesaRequest e retorna HTTP 200 com o DespesaResponse atualizado.
2. DELETE /api/despesas/{id}: retorna HTTP 204 sem corpo.
O controller continua só delegando ao service, sem regra de negócio nem try/catch.

# Estilo
Spring Boot idiomático, seguindo o padrão do POST e do GET existentes.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Alterar o arquivo em disco, sem exibir o código. Ao final, listar as rotas e testar com a aplicação rodando em um user.home temporário (sem tocar em ~/.despesas): editar, excluir, editar id inexistente (404), excluir id inexistente (404) e editar com dado inválido (400). Mostrar o resultado de cada chamada.
````

## 4. Criar a interface de edição e exclusão

````markdown
# Contexto
Projeto "Controle de Despesas": frontend em src/main/resources/static (index.html, css/estilo.css, js/app.js com a função despesas(), Alpine.js local, sem CDN).
Ler antes: docs/escopo-mvp.md (RF-13, RNF-03, RNF-04, RNF-05), docs/backlog.md (RT-08) e os arquivos static atuais.
API: PUT /api/despesas/{id} e DELETE /api/despesas/{id}. Erros chegam como JSON {status, mensagem, timestamp}.
O usuário digita o valor com vírgula (12,34); a API recebe número com ponto (12.34).

# Objetivo
Alterar a tela existente para:
1. Adicionar em cada item da lista os botões Editar e Excluir.
2. Editar: ao clicar, carregar descrição, valor (com vírgula, ex.: 12,34) e data no formulário existente, mudar o botão para "Atualizar" e oferecer "Cancelar". Ao confirmar, enviar PUT e atualizar o item na lista sem recarregar a página, mantendo a ordenação por data e id decrescentes.
3. Excluir: pedir confirmação (confirm) antes de enviar DELETE e remover o item da lista sem recarregar a página.
4. Em caso de erro da API (inclusive 404, quando o item já foi removido) ou falha de rede, exibir a mensagem retornada e manter os dados digitados. Em caso de 404, recarregar a lista.
5. Reaproveitar a montagem do corpo e o tratamento de erros já existentes em app.js; manter o layout sem rolagem horizontal a partir de 360 px.

# Estilo
HTML semântico, CSS simples em estilo.css, lógica em app.js, sem frameworks além do Alpine.js.

# Tom
Objetivo.

# Público
Desenvolvedores do projeto e usuário final.

# Resposta
Alterar os arquivos em disco, sem exibir o código. Ao final, listar os arquivos alterados e descrever o roteiro de teste manual (editar com sucesso, editar com descrição vazia, cancelar edição, excluir confirmando, excluir cancelando a confirmação, excluir item já removido em outra aba).
````

## 5. Criar os testes da release

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, JUnit 5, Mockito, MockMvc, pastas src/test/java/br/com/despesas/{service,controller}.
Ler antes: docs/backlog.md (RF-06, RF-07, RF-08), service/DespesaServiceTest.java, controller/DespesaControllerTest.java e o código de service, controller e exception.

# Objetivo
Alterar os testes existentes, mantendo o estilo atual:
1. DespesaServiceTest (Mockito, sem banco):
   - atualizar com dados válidos altera descrição, valor em centavos e data
   - atualizar com id inexistente lança NaoEncontradoException e não salva
   - atualizar com dados inválidos (descrição vazia, valor com três casas, data nula) lança ValidacaoException e não salva
   - excluir com id existente remove a despesa
   - excluir com id inexistente lança NaoEncontradoException e não remove
2. DespesaControllerTest (@WebMvcTest com MockMvc):
   - PUT válido retorna 200 com o corpo atualizado
   - PUT com dado inválido retorna 400 no formato ErroResposta
   - PUT e DELETE com id inexistente retornam 404 no formato ErroResposta
   - DELETE válido retorna 204 sem corpo
   - id não numérico na rota retorna 400
Os testes não podem tocar em ~/.despesas.

# Estilo
Um comportamento por teste, nomes descritivos em português.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Alterar os arquivos em disco, sem exibir o código. Ao final, rodar `./mvnw verify` e informar o resultado e o total de testes.
````

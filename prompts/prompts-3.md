# Prompts COSTAR - 3 - Release v0.2.0 (Cadastro e listagem)

Escopo da v0.2.0 (docs/backlog.md): RF-01, RF-02, RF-03, RF-04, RF-05, RF-13, RT-05, RT-06, RT-07.
Edição e exclusão (PUT/DELETE, RF-06 a RF-08) ficam na v0.3.0, por isso as rotas desta release são só `POST` e `GET` em `/api/despesas`.

Ordem de execução: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8. Os prompts 3, 6, 7 e 8 foram acrescentados aos quatro pedidos originais porque a release não fecha sem eles.

| # | Prompt | Itens cobertos | Origem |
|---|---|---|---|
| 1 | Model e DTOs | RT-05 | pedido |
| 2 | Persistência | RT-04, RT-05 | pedido |
| 3 | Exceções e tratamento global de erros | RT-06 | acrescentado |
| 4 | Service com regras | RF-01 a RF-05, RT-05 | pedido |
| 5 | Controller (rotas) | RF-01, RF-05 | pedido |
| 6 | Interface de cadastro e listagem | RF-13, RT-07 | acrescentado |
| 7 | Testes da release | RF-01 a RF-05, RT-06 | acrescentado |
| 8 | Mensagens de commit | - | acrescentado |

## 1. Criar a camada model e os DTOs

````markdown
# Contexto
Projeto "Controle de Despesas": Java 21, Spring Boot 3.5, Spring Data JPA, SQLite, pacote base br.com.despesas.
Ler antes: docs/escopo-mvp.md, docs/backlog.md (v0.2.0) e docs/diagramas.md (modelo de dados).
Tabela despesa: id (PK autoincremento), descricao (até 200), valor_centavos (inteiro), data (AAAA-MM-DD).
A API trabalha com valor em reais (BigDecimal); o banco guarda centavos inteiros (RNF-13).
Por RNF-09, as validações ficam no service; esta etapa só cria as estruturas de dados.

# Objetivo
Criar em disco:
1. model/Despesa.java: entidade JPA mapeada para a tabela despesa, com id (Long, IDENTITY), descricao (String, length 200, not null), valorCentavos (long, coluna valor_centavos, not null) e data (LocalDate, not null). Construtor padrão e getters/setters.
2. dto/DespesaRequest.java: record com descricao (String), valor (BigDecimal) e data (LocalDate), sem anotações de validação.
3. dto/DespesaResponse.java: record com id, descricao, valor (BigDecimal em reais) e data.
Remover os .gitkeep das pastas model e dto. Não adicionar dependências ao pom.xml.

# Estilo
Java idiomático, sem Lombok, sem comentários óbvios.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar os arquivos em disco, sem exibir o código. Ao final, listar os arquivos criados e confirmar que `./mvnw compile` passa.
````

## 2. Criar a camada de persistência

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Data JPA com SQLite (~/.despesas/despesas.db), ddl-auto=update.
Ler antes: src/main/resources/application.properties, model/Despesa.java e docs/backlog.md (RT-04, RT-05).
O sqlite-jdbc não cria diretórios: o ~/.despesas precisa existir antes da conexão.

# Objetivo
1. Criar repository/DespesaRepository.java estendendo JpaRepository<Despesa, Long>, com o método derivado que lista todas as despesas ordenadas por data decrescente e, em empate, por id decrescente (RF-05).
2. Garantir que o diretório ~/.despesas seja criado antes da conexão (RT-04), com uma classe de configuração em br.com.despesas (por exemplo, um initializer ou bean executado antes do DataSource). Não usar caminho fixo: usar user.home. Se já estiver resolvido, apenas confirmar.
3. Remover o .gitkeep da pasta repository.
Não criar consultas por mês nem soma: ficam para a v0.4.0.

# Estilo
Spring Boot idiomático, sem SQL nativo e sem dependências novas.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar os arquivos em disco, sem exibir o código. Ao final, listar os arquivos criados/alterados e confirmar que a aplicação sobe do zero (sem ~/.despesas) e cria o arquivo despesas.db.
````

## 3. Criar exceções e tratamento global de erros

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.exception.
Ler antes: docs/backlog.md (RT-06, RF-02, RF-03, RF-04) e docs/diagramas.md (componentes).
Toda resposta de erro da API deve ter um formato único. JSON malformado ou data fora do formato yyyy-MM-dd chegam como HttpMessageNotReadableException, antes do service.

# Objetivo
Criar:
1. exception/ValidacaoException (RuntimeException) com a mensagem do erro de validação.
2. dto/ErroResposta.java: record com status (int), mensagem (String) e timestamp (ISO-8601).
3. exception/GlobalExceptionHandler (@RestControllerAdvice) que converte:
   - ValidacaoException → HTTP 400 com a mensagem da exceção
   - HttpMessageNotReadableException → HTTP 400 com mensagem clara (ex.: "Data inválida. Use o formato yyyy-MM-dd")
   - Exception genérica → HTTP 500 com mensagem genérica, sem expor detalhes internos
Remover o .gitkeep da pasta exception.

# Estilo
Spring Boot idiomático, mensagens em português, sem dependências novas.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar os arquivos em disco, sem exibir o código. Ao final, listar os arquivos criados e o formato JSON de erro retornado.
````

## 4. Criar a camada service com as regras

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.
Ler antes: docs/escopo-mvp.md (RF-01 a RF-05, RNF-09, RNF-13), docs/backlog.md (v0.2.0) e o código de model, dto, repository e exception já criados.
Por RNF-09, regras e validações ficam no service; o controller só delega.
Por RNF-13, o banco guarda centavos inteiros, sem ponto flutuante.

# Objetivo
Criar service/DespesaService.java (@Service) com:
1. cadastrar(DespesaRequest): valida, converte reais em centavos, salva e devolve DespesaResponse com id.
2. listar(): devolve List<DespesaResponse> da data mais recente para a mais antiga, convertendo centavos em reais.
Validações, todas lançando ValidacaoException com mensagem específica e sem salvar nada:
- descrição nula, vazia ou só espaços, ou com mais de 200 caracteres (RF-02)
- valor nulo, zero ou negativo, ou com mais de duas casas decimais (RF-03)
- data nula (RF-04; formato inválido já é tratado pelo GlobalExceptionHandler)
Conversão: 12.34 → 1234 e 1234 → 12.34, usando BigDecimal (sem double/float). Remover o .gitkeep da pasta service.

# Estilo
Métodos curtos, validação em método privado separado, sem Lombok.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar o arquivo em disco, sem exibir o código. Ao final, listar as validações implementadas com a mensagem de cada uma e confirmar que `./mvnw compile` passa.
````

## 5. Criar as rotas de cadastro e listagem

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.controller.
Ler antes: docs/escopo-mvp.md (RF-01, RF-05, RNF-09), docs/backlog.md (v0.2.0), controller/HealthController.java e service/DespesaService.java.
Nesta release só existem cadastro e listagem. PUT, DELETE, GET por id e filtro por mês ficam para as v0.3.0 e v0.4.0.

# Objetivo
Criar controller/DespesaController.java (@RestController, base /api/despesas) com:
1. POST /api/despesas: recebe DespesaRequest e retorna HTTP 201 com DespesaResponse e o cabeçalho Location (/api/despesas/{id}).
2. GET /api/despesas: retorna HTTP 200 com a lista de DespesaResponse, da data mais recente para a mais antiga.
O controller não contém regra de negócio nem try/catch: apenas delega ao service, e os erros saem pelo GlobalExceptionHandler.

# Estilo
Spring Boot idiomático, injeção por construtor, sem dependências novas.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar o arquivo em disco, sem exibir o código. Ao final, listar as rotas e mostrar dois comandos curl (um POST válido e um GET) para testar manualmente.
````

## 6. Criar a interface de cadastro e listagem

````markdown
# Contexto
Projeto "Controle de Despesas": frontend em src/main/resources/static (index.html, css/estilo.css, js/app.js, js/alpine.min.js local, sem CDN).
Ler antes: docs/escopo-mvp.md (RF-13, RNF-03, RNF-04, RNF-05, RNF-07), docs/backlog.md (RF-13, RT-07) e os arquivos static atuais (hoje só há o botão de /health).
API: POST /api/despesas e GET /api/despesas. Erros chegam como JSON {status, mensagem, timestamp}.
O usuário digita o valor com vírgula (12,34); a API recebe número com ponto (12.34).

# Objetivo
Substituir a página de teste por uma tela única, mantendo o Alpine.js local:
1. Formulário com descrição, valor e data, e botão Salvar.
2. Ao salvar: trocar vírgula por ponto no valor, enviar POST e inserir a nova despesa na lista sem recarregar a página (RT-07).
3. Lista de despesas carregada do GET ao abrir a página, exibindo valor como R$ 1.234,56 e data como dd/MM/yyyy.
4. Em caso de erro da API ou falha de rede, exibir a mensagem retornada e manter os dados digitados no formulário (RF-13).
5. Layout utilizável sem rolagem horizontal a partir de 360 px.
O botão de /health pode ser removido da tela principal, sem quebrar o index.html.

# Estilo
HTML semântico, CSS simples em estilo.css, lógica em app.js, sem frameworks além do Alpine.js.

# Tom
Objetivo.

# Público
Desenvolvedores do projeto e usuário final.

# Resposta
Criar/alterar os arquivos em disco, sem exibir o código. Ao final, listar os arquivos alterados e descrever o roteiro de teste manual (cadastro válido, descrição vazia, valor com 3 casas, data ausente).
````

## 7. Criar os testes da release

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, spring-boot-starter-test (JUnit 5, Mockito, MockMvc), pastas src/test/java/br/com/despesas/{service,controller}.
Ler antes: docs/backlog.md (RF-01 a RF-05, RT-06) e o código de service, controller e exception.
Os testes completos estão previstos para a v1.0.0, mas esta release precisa de uma rede mínima para as regras novas.

# Objetivo
Criar:
1. service/DespesaServiceTest (Mockito, sem banco): cadastro válido com conversão 12.34 → 1234; descrição vazia; descrição com 201 caracteres; valor zero, negativo e com três casas; data nula; listagem da data mais recente para a mais antiga.
2. controller/DespesaControllerTest (@WebMvcTest com MockMvc): POST válido retorna 201 e Location; POST com descrição inválida retorna 400 no formato ErroResposta; POST com data 15/10/2026 retorna 400; GET retorna 200 com a lista.
Os testes não podem tocar em ~/.despesas. Remover os .gitkeep das pastas de teste.

# Estilo
Um comportamento por teste, nomes descritivos em português.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar os arquivos em disco, sem exibir o código. Ao final, rodar `./mvnw verify` e informar o resultado e o total de testes.
````

## 8. Criar as mensagens de commit

````markdown
# Contexto
Projeto "Controle de Despesas", release v0.2.0 (cadastro e listagem). Mudanças, na ordem dos prompts 1 a 7:
- model Despesa e DTOs (DespesaRequest, DespesaResponse)
- DespesaRepository e criação do diretório ~/.despesas
- ValidacaoException, ErroResposta e GlobalExceptionHandler
- DespesaService com validações e conversão reais/centavos
- DespesaController com POST e GET /api/despesas
- interface de cadastro e listagem
- testes de service e controller

# Objetivo
Criar uma mensagem de commit no padrão Conventional Commits para cada item, na ordem acima.

# Estilo
Tipo adequado (feat, test), em português, imperativo, minúscula, sem ponto final,
título com até 72 caracteres e corpo com itens curtos.

# Tom
Objetivo.

# Público
Quem lê o histórico do Git.

# Resposta
As sete mensagens, cada uma em um bloco de código, na ordem dos commits.
````

# Prompts COSTAR - 6 - Release v1.0.0 (Testes e documentação)

Escopo da v1.0.0 (docs/backlog.md): RT-09, RT-10, RT-11, RT-12.
As funcionalidades já estão prontas (v0.2.0 a v0.4.0) e há 69 testes. Esta release fecha lacunas de teste, mede o desempenho, documenta a API e publica a versão 1.0.0. Por isso os prompts 1 e 2 começam com uma auditoria do que já existe, em vez de reescrever testes.

Ordem de execução: 1 → 2 → 3 → 4 → 5.

| # | Prompt | Itens cobertos |
|---|---|---|
| 1 | Auditar e completar os testes de service | RT-09, RNF-10 |
| 2 | Auditar e completar os testes de endpoints (inclui `/health`) | RT-10, RNF-10 |
| 3 | Teste de desempenho com 1.000 despesas | RT-11, RNF-01 |
| 4 | Documentação da API | RT-12 |
| 5 | README, versão 1.0.0 e checklist final | RT-12, RNF-02, RNF-06 |

## 1. Auditar e completar os testes de service

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, JUnit 5, Mockito, pasta src/test/java/br/com/despesas/service.
Ler antes: docs/backlog.md (RT-09, RF-01 a RF-11), docs/escopo-mvp.md (RNF-10) e service/DespesaServiceTest.java, service/DespesaService.java.
RT-09: os testes da camada service cobrem validações, cálculo de total e id inexistente, e todos passam.
Já existem testes de cadastro, validações, listagem, atualização, exclusão, filtro por mês e total.

# Objetivo
1. Montar uma matriz de rastreabilidade (requisito → teste existente) para RF-01 a RF-11 e para cada regra de validação do service, e identificar o que não está coberto. Exemplos de lacunas a verificar: despesa com data no limite do mês, descrição com espaços nas pontas contando para o limite de 200, valor com zeros à direita, valor mínimo (0.01), mês com espaços, total depois de valores que somam centavos (ex.: 0.10 + 0.20).
2. Criar apenas os testes que faltam, em DespesaServiceTest, mantendo o estilo atual (um comportamento por teste, nomes em português).
3. Não duplicar testes já existentes nem alterar código de produção.

# Estilo
Seguir o estilo do arquivo atual.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar/alterar o arquivo em disco, sem exibir o código. Ao final, mostrar a matriz requisito → teste, listar os testes acrescentados, rodar `./mvnw verify` e informar o resultado e o total de testes.
````

## 2. Auditar e completar os testes de endpoints

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, MockMvc, pasta src/test/java/br/com/despesas/controller.
Ler antes: docs/backlog.md (RT-10), docs/escopo-mvp.md (RNF-10), controller/DespesaControllerTest.java, controller/DespesaController.java e controller/HealthController.java.
RT-10: cada endpoint tem ao menos um teste de sucesso e um de erro, e todos passam.
Endpoints: POST /api/despesas, GET /api/despesas (com e sem mes), GET /api/despesas/total (com e sem mes), PUT /api/despesas/{id}, DELETE /api/despesas/{id} e GET /health. Hoje não há teste para o /health.

# Objetivo
1. Montar uma matriz endpoint → testes de sucesso e de erro existentes e apontar o que falta.
2. Criar controller/HealthControllerTest (@WebMvcTest): 200 com status "ok" e timestamp em ISO-8601 válido; e um teste de erro para o endpoint (por exemplo, método não permitido retorna 405 no formato de erro da API).
3. Completar lacunas nos demais endpoints, como erro 405 para método não suportado e 404 para rota inexistente com corpo no formato ErroResposta, se o comportamento atual estiver correto. Se um teste revelar defeito real, não corrigir o código de produção: relatar o defeito.
4. Não duplicar testes existentes.

# Estilo
Seguir o estilo dos testes atuais.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar/alterar os arquivos em disco, sem exibir o código. Ao final, mostrar a matriz endpoint → testes, listar os testes acrescentados, rodar `./mvnw verify` e informar o resultado e o total de testes.
````

## 3. Criar o teste de desempenho

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, SQLite, JUnit 5.
Ler antes: docs/backlog.md (RT-11), docs/escopo-mvp.md (RNF-01), repository/DespesaRepository.java, service/DespesaService.java e repository/DespesaRepositoryTest.java (que já usa SQLite em arquivo temporário).
RT-11 / RNF-01: dado um banco com 1.000 despesas em um mês, a listagem desse mês responde em até 500 ms.
Os testes não podem tocar em ~/.despesas.

# Objetivo
1. Criar um teste de desempenho (por exemplo, src/test/java/br/com/despesas/DesempenhoListagemTest) com @SpringBootTest e SQLite em arquivo temporário (spring.datasource.url sobrescrita), sem usar ~/.despesas.
2. Inserir 1.000 despesas no mesmo mês (e algumas centenas em outros meses, para o filtro ter o que descartar), em lote.
3. Medir a chamada completa GET /api/despesas?mes=AAAA-MM (MockMvc com contexto completo ou servidor em porta aleatória), incluindo serialização JSON. Fazer um aquecimento antes de medir e usar o menor tempo ou a mediana de poucas execuções, para evitar falso negativo por JIT ou disco.
4. Afirmar que o tempo é de até 500 ms e que a resposta traz 1.000 itens ordenados por data e id decrescentes. Medir também GET /api/despesas/total?mes=AAAA-MM.
5. Se o tempo estourar, investigar a causa antes de relaxar o limite (por exemplo, índice na coluna data em model/Despesa.java) e relatar o que foi feito.

# Estilo
Teste simples e legível; o limite de 500 ms em uma constante nomeada.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar os arquivos em disco, sem exibir o código. Ao final, informar os tempos medidos (listagem e total), rodar `./mvnw verify` e informar o resultado e o total de testes.
````

## 4. Documentar a API

````markdown
# Contexto
Projeto "Controle de Despesas": API REST em Spring Boot, sem autenticação, em http://localhost:8080.
Ler antes: docs/escopo-mvp.md (RF-01 a RF-11), docs/backlog.md (RT-12), controller/DespesaController.java, controller/HealthController.java, dto/ e exception/GlobalExceptionHandler.java.
Endpoints: POST /api/despesas, GET /api/despesas?mes=AAAA-MM, GET /api/despesas/total?mes=AAAA-MM, PUT /api/despesas/{id}, DELETE /api/despesas/{id}, GET /health.
Erros seguem o formato {status, mensagem, timestamp}.

# Objetivo
Criar o arquivo docs/api.md com:
1. Visão geral: base URL, formato JSON, valores em reais com ponto decimal e no máximo duas casas, datas em yyyy-MM-dd, mês em yyyy-MM.
2. Uma seção por endpoint, com: método e rota, parâmetros, corpo de exemplo, respostas de sucesso (status e JSON) e erros possíveis (status e mensagem exata do código).
3. Exemplo curl de cada endpoint.
4. Tabela de erros: todas as mensagens de validação do service e do GlobalExceptionHandler, com status HTTP.
Os exemplos devem ser reais: executar a aplicação em um user.home temporário (sem tocar em ~/.despesas) e conferir cada exemplo antes de documentar.

# Estilo
Markdown simples, sem emojis, trechos de código em blocos com linguagem indicada.

# Tom
Objetivo e técnico.

# Público
Desenvolvedores que vão consumir a API.

# Resposta
Criar o arquivo em disco, sem exibir o conteúdo. Ao final, informar o caminho, a lista de endpoints documentados e confirmar que os exemplos foram executados contra a aplicação.
````

## 5. Atualizar o README, a versão e fazer o checklist final

````markdown
# Contexto
Projeto "Controle de Despesas": todas as funcionalidades da v0.4.0 prontas; esta é a release v1.0.0.
Ler antes: README.md, docs/escopo-mvp.md, docs/backlog.md, docs/api.md e pom.xml (versão atual 0.0.1-SNAPSHOT).
RT-12: ao ler o README.md e a documentação da API, encontra-se como rodar, os endpoints com exemplos e o roadmap atualizado.

# Objetivo
1. README.md: descrever o que a aplicação faz hoje (cadastrar, listar, editar, excluir, filtrar por mês e total), manter "Como rodar" e "Banco de dados", incluir resumo dos endpoints com link para docs/api.md, seção "Testes" (`./mvnw verify`), link para escopo-mvp.md, backlog.md e diagramas.md, nome correto do jar da versão final e roadmap com todas as releases marcadas como concluídas.
2. pom.xml: alterar a versão para 1.0.0 (sem -SNAPSHOT) e ajustar no README o nome do jar gerado.
3. Checklist final contra docs/escopo-mvp.md: para cada RF-01 a RF-13 e RNF-01 a RNF-13, indicar se está atendido e por qual evidência (teste, execução ou leitura do código). Medir o RNF-02 (aplicação responde em /health em até 15 s) e confirmar o RNF-07 (Alpine servido localmente, sem CDN). Itens que não puderem ser verificados sem navegador devem ser apontados como pendentes de verificação manual.
4. Não criar tag nem release no GitHub; apenas deixar o repositório pronto para isso.

# Estilo
Markdown simples, sem emojis ou badges. Checklist em tabela.

# Tom
Objetivo e profissional.

# Público
Desenvolvedores que vão rodar o projeto e avaliadores da entrega.

# Resposta
Alterar os arquivos em disco, sem exibir o conteúdo. Ao final, mostrar a tabela do checklist, listar os arquivos alterados, rodar `./mvnw verify` e informar o resultado, o total de testes e o nome do jar gerado.
````

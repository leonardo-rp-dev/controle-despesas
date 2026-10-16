# Prompts COSTAR - 5 - Release v0.4.0 (Filtro por mês e total)

Escopo da v0.4.0 (docs/backlog.md): RF-09, RF-10, RF-11, RF-12.
Rotas desta release: `GET /api/despesas?mes=AAAA-MM` (filtro) e `GET /api/despesas/total?mes=AAAA-MM` (total). Sem `mes`, as duas rotas consideram todas as despesas.

Ordem de execução: 1 → 2 → 3 → 4 → 5.

| # | Prompt | Itens cobertos |
|---|---|---|
| 1 | Persistência: consulta por período e soma | RF-09, RF-10, RF-11 |
| 2 | Service: filtro por mês e total | RF-09, RF-10, RF-11 |
| 3 | Controller: parâmetro `mes` e rota de total | RF-09, RF-10, RF-11 |
| 4 | Interface: filtro por mês, total e lista consistente | RF-12 |
| 5 | Testes da release | RF-09, RF-10, RF-11 |

## 1. Criar as consultas por período e a soma

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Data JPA com SQLite, pacote br.com.despesas.repository.
Ler antes: docs/backlog.md (RF-09, RF-10, RF-11), docs/diagramas.md (sequência: "busca por período" e "soma de valor_centavos no período"), repository/DespesaRepository.java e model/Despesa.java.
O valor é guardado em centavos inteiros (RNF-13); a soma deve ser feita no banco e devolvida em centavos.

# Objetivo
Alterar repository/DespesaRepository.java para incluir:
1. Consulta derivada das despesas com data entre um início e um fim, ambos inclusivos, ordenadas por data decrescente e, em empate, por id decrescente.
2. Soma de valorCentavos de todas as despesas, devolvendo 0 quando não houver nenhuma (JPQL com coalesce, sem SQL nativo).
3. Soma de valorCentavos das despesas com data entre um início e um fim, ambos inclusivos, devolvendo 0 quando não houver nenhuma.
O tipo de retorno das somas é long. Não adicionar dependências.
Verificar que a comparação de datas funciona de fato no SQLite (as datas são gravadas como texto): conferir o primeiro e o último dia do mês e o dia anterior e o seguinte ao período.

# Estilo
Spring Boot idiomático, métodos com nomes em português seguindo o padrão atual do repositório.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Alterar o arquivo em disco, sem exibir o código. Ao final, listar os métodos adicionados e mostrar a verificação feita com o SQLite (aplicação ou teste em um user.home temporário, sem tocar em ~/.despesas), incluindo os casos de borda de data.
````

## 2. Criar o filtro por mês e o total no service

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.
Ler antes: docs/escopo-mvp.md (RF-09, RF-10, RF-11, RNF-09, RNF-13), docs/backlog.md (v0.4.0), service/DespesaService.java, exception/ValidacaoException.java e os métodos novos de repository/DespesaRepository.java.
Por RNF-09, regras e validações ficam no service. O parâmetro de mês chega como texto no formato AAAA-MM (ex.: 2026-10).

# Objetivo
1. Criar dto/TotalResponse.java: record com total (BigDecimal em reais).
2. Alterar service/DespesaService.java:
   - listar(String mes): sem mês (nulo ou em branco) devolve todas as despesas, como hoje; com mês devolve só as do período, da data mais recente para a mais antiga.
   - total(String mes): sem mês devolve a soma de todas as despesas; com mês, a soma das despesas do período. Converter de centavos para reais com BigDecimal, sempre com duas casas (0 vira 0.00).
   - Mês fora do formato AAAA-MM ou inexistente (ex.: 2026-13, 10/2026, abc) lança ValidacaoException com a mensagem "Mês inválido. Use o formato yyyy-MM".
O período vai do primeiro ao último dia do mês, inclusivos (YearMonth). Compartilhar a conversão do mês e a conversão para reais entre os dois métodos, sem duplicar código. Manter o listar() sem argumentos como está, ou ajustar seus usos, sem quebrar o comportamento atual.

# Estilo
Métodos curtos, sem Lombok, sem ponto flutuante.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar/alterar os arquivos em disco, sem exibir o código. Ao final, listar os métodos e DTOs adicionados, a mensagem de erro e confirmar que `./mvnw compile` passa.
````

## 3. Criar o parâmetro mes e a rota de total

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, pacote br.com.despesas.controller.
Ler antes: docs/escopo-mvp.md (RF-09, RF-10, RF-11, RNF-09), docs/backlog.md (v0.4.0), controller/DespesaController.java e service/DespesaService.java.
Hoje existem POST, GET, PUT /{id} e DELETE /{id} em /api/despesas. Não existe GET por id, então GET /total não conflita com /{id}.

# Objetivo
Alterar controller/DespesaController.java para:
1. GET /api/despesas aceitar o parâmetro opcional mes (AAAA-MM) e delegar a listar(mes). Sem o parâmetro, o comportamento atual é mantido.
2. Criar GET /api/despesas/total com o parâmetro opcional mes, retornando HTTP 200 com TotalResponse ({"total": 12.34}).
O controller continua só delegando ao service; o erro de mês inválido sai como 400 pelo GlobalExceptionHandler.

# Estilo
Spring Boot idiomático, seguindo o padrão das rotas existentes.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Alterar o arquivo em disco, sem exibir o código. Ao final, listar as rotas e testar com a aplicação rodando em um user.home temporário (sem tocar em ~/.despesas): cadastrar despesas em meses diferentes (incluindo primeiro e último dia de um mês), listar sem filtro, listar com mes, total sem filtro, total com mes, total de um mês sem despesas (0.00) e mes inválido (400). Mostrar o resultado de cada chamada.
````

## 4. Criar o filtro e o total na interface

````markdown
# Contexto
Projeto "Controle de Despesas": frontend em src/main/resources/static (index.html, css/estilo.css, js/app.js com a função despesas(), Alpine.js local, sem CDN).
Ler antes: docs/escopo-mvp.md (RF-09, RF-10, RF-11, RF-12, RNF-04, RNF-05), docs/backlog.md (v0.4.0) e os arquivos static atuais.
API: GET /api/despesas?mes=AAAA-MM e GET /api/despesas/total?mes=AAAA-MM; sem mes, valem para todas as despesas. O total chega como {"total": 12.34}.
Hoje, ao cadastrar ou editar, a tela insere/atualiza o item direto na lista local, o que seria inconsistente com um filtro ativo (uma despesa de outro mês apareceria na lista).

# Objetivo
Alterar a tela existente para:
1. Adicionar um seletor de mês (input type="month", valor AAAA-MM) e um botão "Limpar filtro" acima da lista. Ao escolher o mês, carregar a lista filtrada e o total do período; ao limpar, voltar a todas as despesas e ao total geral.
2. Exibir o total no formato R$ 1.234,56, com rótulo que indique se é do mês selecionado ou de todas as despesas.
3. Manter lista e total consistentes após cadastrar, editar ou excluir: com filtro ativo, recarregar a lista e o total do servidor em vez de inserir localmente; sem filtro, pode manter o comportamento atual, mas o total deve ser sempre atualizado pelo servidor.
4. Mensagem de lista vazia adequada ao filtro (ex.: "Nenhuma despesa neste mês").
5. Erros da API ou falha de rede exibidos com a mensagem retornada, reaproveitando o tratamento existente; o seletor de mês mantém o valor escolhido.
6. Layout sem rolagem horizontal a partir de 360 px.

# Estilo
HTML semântico, CSS simples em estilo.css, lógica em app.js, sem frameworks além do Alpine.js.

# Tom
Objetivo.

# Público
Desenvolvedores do projeto e usuário final.

# Resposta
Alterar os arquivos em disco, sem exibir o código. Ao final, listar os arquivos alterados e descrever o roteiro de teste manual (filtrar um mês com despesas, filtrar um mês vazio, limpar o filtro, cadastrar com filtro ativo em outro mês, editar mudando a data para fora do mês filtrado, excluir com filtro ativo e conferir o total em cada passo).
````

## 5. Criar os testes da release

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, JUnit 5, Mockito, MockMvc, pastas src/test/java/br/com/despesas/{service,controller,repository}.
Ler antes: docs/backlog.md (RF-09, RF-10, RF-11), service/DespesaServiceTest.java, controller/DespesaControllerTest.java e o código de repository, service e controller.
Os testes não podem tocar em ~/.despesas.

# Objetivo
1. DespesaServiceTest (Mockito, sem banco), acrescentar:
   - listar com mês usa a consulta por período, do primeiro ao último dia do mês
   - listar sem mês (nulo e em branco) usa a consulta de todas as despesas
   - total com mês converte a soma de centavos em reais (ex.: 1234 → 12.34)
   - total sem mês soma todas as despesas
   - total sem despesas devolve 0.00
   - mês inválido (2026-13, 10/2026, abc) lança ValidacaoException com "Mês inválido. Use o formato yyyy-MM" em listar e total
2. DespesaControllerTest (@WebMvcTest), acrescentar:
   - GET com mes repassa o mês ao service
   - GET /total retorna 200 com {"total": ...}, com e sem mes
   - mês inválido retorna 400 no formato ErroResposta
3. repository/DespesaRepositoryTest (@DataJpaTest contra SQLite em arquivo temporário, configurado por propriedades do teste, sem usar ~/.despesas):
   - consulta por período inclui o primeiro e o último dia do mês e exclui o dia anterior e o seguinte
   - ordenação por data decrescente e id decrescente
   - soma por período e soma geral em centavos, e 0 quando vazio
Se o @DataJpaTest com o dialeto do SQLite exigir ajuste de configuração, documentar o ajuste no próprio teste.

# Estilo
Um comportamento por teste, nomes descritivos em português, seguindo o estilo atual.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar/alterar os arquivos em disco, sem exibir o código. Ao final, rodar `./mvnw verify` e informar o resultado e o total de testes.
````

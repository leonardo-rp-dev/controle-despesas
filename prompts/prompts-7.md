# Prompts COSTAR - 7 - Qualidade, refatoração e README final

Estes prompts partem da v1.0.0 pronta: 102 testes passando e cobertura medida com JaCoCo de 96,9% das linhas e 93,3% dos branches. Lacunas conhecidas: o `main` de `DespesasApplication`, dois branches de `GlobalExceptionHandler` e a interface web, sem teste automatizado.

Ordem de execução: 1 → 2 → 3 → 4. O README fica por último porque descreve o resultado dos demais.

| # | Prompt | Natureza |
|---|---|---|
| 1 | Suíte de testes cobrindo todas as funcionalidades | Cria testes |
| 2 | Testes de cliente de rota (201, 200, 204, 404) com repositório isolado | Cria testes |
| 3 | Análise de refatoração (DRY e SRP) em service e repository | Somente análise, sem alterar código |
| 4 | README completo | Altera documentação |

## 1. Gerar a suíte de testes das funcionalidades

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, Java 21, JUnit 5, Mockito, MockMvc, SQLite.
Ler antes: docs/escopo-mvp.md (RF-01 a RF-13, RNF-01 a RNF-13), docs/backlog.md, docs/api.md e os testes atuais em src/test/java (service, controller, repository e DesempenhoListagemTest).
Já existem 102 testes. Cobertura medida com `./mvnw -B org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.12:report` (relatório em target/site/jacoco, sem alterar o pom.xml).
Lacunas conhecidas: DespesasApplication.main (cria ~/.despesas), os ramos do GlobalExceptionHandler para erro 5xx do Spring MVC e para formato inválido que não seja data nem valor, e os fluxos combinados entre funcionalidades.
Os testes não podem tocar em ~/.despesas.

# Objetivo
1. Montar uma matriz funcionalidade → teste para RF-01 a RF-11 (e os itens de RNF que são testáveis no backend), apontando o que está coberto só por teste isolado e o que não tem teste de fluxo.
2. Criar testes de fluxo ponta a ponta (@SpringBootTest com MockMvc e SQLite em arquivo temporário), por exemplo: cadastrar → listar → filtrar por mês → conferir total → editar mudando o mês → conferir total dos dois meses → excluir → conferir lista e total; erro de validação não altera lista nem total; id inexistente não altera nada.
3. Cobrir as lacunas conhecidas que forem testáveis sem alterar código de produção (por exemplo, testar o GlobalExceptionHandler diretamente com exceções construídas à mão). Se algo exigir mudança no código de produção (como a criação de ~/.despesas dentro do main), não alterar: registrar a limitação e propor a mudança.
4. Medir a cobertura antes e depois e informar a diferença.
5. A interface web não terá teste automatizado nesta etapa: listar, como proposta, o que seria testado e com qual ferramenta, sem implementar.

# Estilo
Seguir o estilo atual (um comportamento por teste, nomes em português). Não duplicar testes existentes.

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar/alterar os arquivos em disco, sem exibir o código. Ao final, mostrar a matriz funcionalidade → teste, listar os testes acrescentados, a cobertura antes e depois, e o resultado e o total de testes do `./mvnw verify`.
````

## 2. Gerar testes de cliente de rota com repositório isolado

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, Java 21, JUnit 5, Mockito.
Ler antes: docs/api.md, controller/DespesaController.java, service/DespesaService.java, repository/DespesaRepository.java e os testes de controller atuais (que isolam o service, não o repositório).
Objetivo de qualidade: testar as rotas como um cliente HTTP, passando por controller, service e tratamento de erros reais, mas sem banco, para que nenhum teste dependa de estado global nem de ordem de execução.

# Objetivo
Criar controller/DespesaRotasClienteTest com:
1. @SpringBootTest com servidor em porta aleatória (webEnvironment RANDOM_PORT) e um cliente HTTP real (TestRestTemplate ou RestClient), com o DespesaRepository substituído por um mock (@MockitoBean) e redefinido a cada teste (@BeforeEach/@AfterEach ou reset automático).
2. Garantir que nada toque em ~/.despesas: com o repositório mockado, o contexto ainda configura o DataSource; sobrescrever spring.datasource.url para um arquivo temporário ou excluir a autoconfiguração de DataSource e JPA nas propriedades do teste, e documentar a escolha em um comentário.
3. Um teste por status, com o comportamento do repositório definido no próprio teste (stubs), conferindo status, cabeçalhos relevantes e corpo JSON:
   - 201: POST /api/despesas válido (com cabeçalho Location e corpo com id)
   - 200: GET /api/despesas, GET /api/despesas?mes=..., GET /api/despesas/total e PUT /api/despesas/{id}
   - 204: DELETE /api/despesas/{id}, sem corpo
   - 404: PUT e DELETE com id inexistente (corpo no formato ErroResposta, mensagem "Despesa não encontrada") e rota inexistente
4. Verificar nas interações com o mock que, nos casos de erro, nada foi salvo nem removido (verify never).
5. Os testes devem passar isoladamente e em qualquer ordem (confirmar executando com a ordem aleatória do JUnit ativada, por exemplo junit.jupiter.testmethod.order.default ou execução de um teste por vez).

# Estilo
Um comportamento por teste, nomes em português, sem compartilhar estado entre testes (nada de campos estáticos mutáveis nem @Order).

# Tom
Técnico.

# Público
Desenvolvedores Java do projeto.

# Resposta
Criar o arquivo em disco, sem exibir o código. Ao final, listar os testes por status, explicar como o isolamento foi obtido, rodar `./mvnw verify` e informar o resultado e o total de testes.
````

## 3. Analisar service e repository para refatoração (DRY e SRP)

````markdown
# Contexto
Projeto "Controle de Despesas": Spring Boot 3.5, Java 21, pacote br.com.despesas.
Ler antes: service/DespesaService.java, repository/DespesaRepository.java, model/Despesa.java, dto/ e os testes de service e repository (que protegem o comportamento atual: 102 testes passando).
Restrição: o comportamento externo não pode mudar (rotas, status, mensagens de erro, formato JSON, ordenação, regras de validação, conversão em centavos).

# Objetivo
Analisar service e repository e produzir uma análise de refatoração com foco em DRY e SRP. Esta etapa é somente de análise: não alterar nenhum arquivo do projeto.
1. Mapear as responsabilidades de DespesaService (por exemplo: orquestração dos casos de uso, validação, conversão reais/centavos, leitura do mês, mapeamento entidade/DTO) e dizer quais violam o SRP.
2. Listar duplicações (DRY) no service e no repository, com arquivo e linhas (por exemplo: cálculo de início e fim do mês repetido em mais de um método; pares de consultas "todas" e "por período"; padrão de conversão repetido).
3. Para cada sugestão, informar: problema, solução proposta (classe/método novo, com um esboço curto), ganho, risco e quais testes existentes protegem a mudança.
4. Ordenar as sugestões por custo-benefício e indicar o que não vale refatorar neste tamanho de projeto (evitar excesso de abstração).
5. Propor a sequência de passos pequenos e seguros para aplicar, cada um terminando com `./mvnw verify` verde.

# Estilo
Relatório em Markdown, objetivo, com tabela resumo (sugestão, tipo DRY/SRP, esforço, risco) e esboços de código curtos.

# Tom
Técnico e crítico, sem rodeios.

# Público
Desenvolvedores Java do projeto.

# Resposta
Entregar a análise na resposta, sem criar nem alterar arquivos. Ao final, perguntar quais sugestões aplicar.
````

## 4. Gerar o README completo

````markdown
# Contexto
Projeto "Controle de Despesas": versão 1.0.0, Java 21, Spring Boot 3, SQLite, frontend em HTML e Alpine.js.
Ler antes: README.md atual, pom.xml, docs/escopo-mvp.md, docs/backlog.md, docs/diagramas.md, docs/api.md, a estrutura de src/ e os resultados dos prompts anteriores desta série (testes e, se aplicadas, refatorações).
O README deve refletir apenas o que existe e foi verificado: não inventar funcionalidades, comandos nem números.

# Objetivo
Reescrever o README.md na raiz com as seções, nesta ordem:
1. Título e descrição curta, com a versão.
2. Funcionalidades.
3. Stack.
4. Instalação: pré-requisitos (JDK 21, Git), clonar o repositório e nota sobre o Maven Wrapper (incluindo a permissão de execução do mvnw no Linux e no macOS).
5. Execução: `./mvnw spring-boot:run` e `.\mvnw spring-boot:run` (Windows), endereços, geração e execução do jar, localização e reinício do banco (~/.despesas/despesas.db), solução para erro PKIX no Windows.
6. Testes: `./mvnw verify`, o que cada grupo de testes cobre, como medir cobertura com JaCoCo (comando de linha, sem alterar o pom.xml) e a cobertura atual medida, com a data da medição.
7. Arquitetura: camadas e responsabilidades, fluxo de uma requisição, modelo de dados (centavos inteiros), e um diagrama Mermaid de componentes resumido, com link para docs/diagramas.md.
8. API: tabela de endpoints, um exemplo de cadastro com curl e link para docs/api.md.
9. Estrutura de pastas (árvore resumida).
10. Limitações: apenas as reais e verificáveis (por exemplo: uso local de um único usuário, sem autenticação; banco SQLite em arquivo; apenas moeda BRL; limite de valor; mensagens genéricas nos erros 404/405/415; interface web sem teste automatizado; verificação feita apenas no Windows).
11. Próximos passos: sugestões realistas, separadas do que já existe (por exemplo: CI com GitHub Actions rodando `./mvnw verify`, testes automatizados da interface, cobertura no build, mensagens de erro específicas, exportação de dados), marcadas como propostas.
12. Documentação adicional (links) e roadmap das releases concluídas.
Conferir cada comando e cada link citados (executar os comandos de build e teste; confirmar que os arquivos linkados existem).

# Estilo
Markdown profissional e objetivo, sem emojis nem badges, tabelas onde ajudarem, blocos de código com linguagem indicada, frases curtas.

# Tom
Objetivo e profissional.

# Público
Desenvolvedores que vão instalar e rodar o projeto, e avaliadores da entrega.

# Resposta
Alterar o arquivo em disco, sem exibir o conteúdo. Ao final, listar as seções criadas, os comandos e links conferidos, e qualquer informação do README que não pôde ser verificada.
````

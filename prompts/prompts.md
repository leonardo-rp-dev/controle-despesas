# Prompts COSTAR - Sistema de Controle de Despesas

## 1. Criar o .gitignore

````markdown
# Contexto
Projeto "Controle de Despesas": Java 21, Spring Boot 3, Maven com wrapper, SQLite, frontend HTML + Alpine.js.

# Objetivo
Criar o arquivo .gitignore na raiz do projeto, cobrindo: target/, maven-wrapper.jar, *.class, logs, banco SQLite
(*.db, *.db-journal, *.db-wal, *.db-shm), .env, application-local.properties,
arquivos de IDE (IntelliJ, Eclipse, VS Code) e de sistema operacional.

# Estilo
Seções com comentário curto em português.

# Tom
Técnico.

# Público
Desenvolvedores do projeto.

# Resposta
Criar o arquivo em disco, sem exibir o conteúdo. Ao final, listar o caminho do arquivo criado.
````

## 2. Criar a estrutura básica com GET /health e página de teste

````markdown
# Contexto
Projeto "Controle de Despesas": monólito Java 21, Spring Boot 3, Maven com wrapper,
SQLite (sqlite-jdbc + hibernate-community-dialects, banco em ~/.despesas/despesas.db).
Pacote base br.com.despesas, camadas controller, service, repository, model, dto, exception.
Frontend em src/main/resources/static com Alpine.js local.

# Objetivo
Criar em disco, na raiz do projeto, os diretórios e arquivos abaixo:
```
pom.xml
mvnw, mvnw.cmd, .mvn/wrapper/maven-wrapper.properties
src/main/java/br/com/despesas/DespesasApplication.java
src/main/java/br/com/despesas/{controller,service,repository,model,dto,exception}/   (vazios com .gitkeep, exceto o HealthController)
src/main/resources/application.properties
src/main/resources/static/index.html
src/main/resources/static/css/estilo.css
src/main/resources/static/js/app.js
src/main/resources/static/js/alpine.min.js
src/test/java/br/com/despesas/{controller,service}/   (vazios com .gitkeep)
```
Funcionalidades:
1. GET /health em controller/HealthController retornando {"status": "ok", "timestamp": "<ISO-8601>"}, sem Actuator.
2. index.html com um único botão que chama /health e exibe o retorno.

# Estilo
Código Spring Boot idiomático, sem dependências extras.

# Tom
Técnico.

# Público
Desenvolvedores Java.

# Resposta
Criar os arquivos em disco, sem exibir o código. Ao final, mostrar a árvore dos diretórios e arquivos criados e o comando para rodar.
````

## 3. Criar o README inicial

````markdown
# Contexto
Projeto "Controle de Despesas": app pessoal, sem login, para registrar despesas mensais.
Stack: Java 21, Spring Boot 3, Maven com wrapper, SQLite (~/.despesas/despesas.db), HTML + Alpine.js.
Hoje existe só a estrutura básica com GET /health.

# Objetivo
Criar o arquivo README.md na raiz do projeto com: objetivo, stack, como rodar localmente (./mvnw spring-boot:run,
http://localhost:8080) e roadmap de releases:
- v0.1.0: estrutura e /health
- v0.2.0: cadastro e listagem
- v0.3.0: edição e exclusão
- v0.4.0: filtro por mês e total
- v1.0.0: testes, CI e documentação

# Estilo
Markdown simples, sem emojis ou badges.

# Tom
Objetivo e profissional.

# Público
Desenvolvedores que vão rodar o projeto.

# Resposta
Criar o arquivo em disco, sem exibir o conteúdo. Ao final, mostrar a árvore completa do projeto com todos os diretórios e arquivos criados pelos prompts 1, 2 e 3.
````

## 4. Criar a mensagem de commit

````markdown
# Contexto
Mudanças: .gitignore, README inicial, estrutura Spring Boot, endpoint GET /health
e página com botão que chama /health.

# Objetivo
Criar a mensagem de commit no padrão Conventional Commits.

# Estilo
Em português, imperativo, minúscula, sem ponto final, título com até 72 caracteres
e corpo com itens curtos.

# Tom
Objetivo.

# Público
Quem lê o histórico do Git.

# Resposta
Somente a mensagem de commit em um bloco de código.
````

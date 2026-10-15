# Controle de Despesas

Aplicação pessoal, sem autenticação, para registrar e acompanhar despesas mensais. Roda localmente e guarda os dados em um arquivo SQLite no diretório do usuário.

## Stack

- Java 21
- Spring Boot 3 (Web e Data JPA)
- Maven, com Maven Wrapper
- SQLite (`sqlite-jdbc` e `hibernate-community-dialects`)
- Frontend em HTML e Alpine.js, servido como recurso estático

## Requisitos

- JDK 21
- Acesso à internet na primeira execução, para o wrapper baixar o Maven e as dependências

Não é necessário instalar o Maven.

## Como rodar localmente

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw spring-boot:run
```

Com a aplicação no ar:

- Frontend: http://localhost:8080
- Health check: http://localhost:8080/health

Resposta esperada de `GET /health`:

```json
{"status": "ok", "timestamp": "2026-01-01T12:00:00-03:00"}
```

### Banco de dados

O banco fica em `~/.despesas/despesas.db`. O diretório e o arquivo são criados automaticamente na primeira execução. Para zerar os dados, encerre a aplicação e remova o arquivo.

### Build

```bash
./mvnw clean package
java -jar target/controle-despesas-0.0.1-SNAPSHOT.jar
```

### Erro de certificado (PKIX) no Windows

Em redes com inspeção TLS, o Maven pode falhar com `PKIX path building failed`. Use o repositório de certificados do Windows:

```powershell
$env:MAVEN_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
.\mvnw spring-boot:run
```

## Estrutura

Pacote base `br.com.despesas`:

| Pacote | Responsabilidade |
|---|---|
| `controller` | Endpoints REST |
| `service` | Regras de negócio |
| `repository` | Acesso a dados |
| `model` | Entidades |
| `dto` | Objetos de entrada e saída da API |
| `exception` | Exceções e tratamento de erros |

O frontend está em `src/main/resources/static`.

## Roadmap

| Release | Escopo |
|---|---|
| v0.1.0 | Estrutura do projeto e endpoint `/health` |
| v0.2.0 | Cadastro e listagem de despesas |
| v0.3.0 | Edição e exclusão de despesas |
| v0.4.0 | Filtro por mês e total do período |
| v1.0.0 | Testes e documentação |

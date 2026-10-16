# Controle de Despesas

Aplicação pessoal, sem autenticação, para registrar e acompanhar despesas mensais. Roda localmente e guarda os dados em um arquivo SQLite no diretório do usuário.

Versão atual: 1.0.0.

## O que a aplicação faz

- Cadastra despesas com descrição (até 200 caracteres), valor em reais e data.
- Lista as despesas da data mais recente para a mais antiga.
- Edita e exclui despesas, com confirmação antes de excluir.
- Filtra a listagem por mês e ano.
- Mostra o total do mês filtrado ou, sem filtro, o total de todas as despesas.
- Exibe valores como `R$ 1.234,56` e datas como `dd/MM/yyyy`.

Fora do escopo: receitas, orçamento, gráficos, login, importação de extrato e recursos de IA.

## Stack

- Java 21
- Spring Boot 3 (Web e Data JPA)
- Maven, com Maven Wrapper
- SQLite (`sqlite-jdbc` e `hibernate-community-dialects`)
- Frontend em HTML e Alpine.js, servido como recurso estático, sem CDN

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
java -jar target/controle-despesas-1.0.0.jar
```

### Erro de certificado (PKIX) no Windows

Em redes com inspeção TLS, o Maven pode falhar com `PKIX path building failed`. Use o repositório de certificados do Windows:

```powershell
$env:MAVEN_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
.\mvnw spring-boot:run
```

## API

Resumo dos endpoints. A documentação completa, com parâmetros, exemplos `curl`, respostas e todas as mensagens de erro, está em [docs/api.md](docs/api.md).

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/despesas` | Cadastra uma despesa |
| GET | `/api/despesas?mes=yyyy-MM` | Lista as despesas, com filtro opcional por mês |
| GET | `/api/despesas/total?mes=yyyy-MM` | Total das despesas, com filtro opcional por mês |
| PUT | `/api/despesas/{id}` | Edita uma despesa |
| DELETE | `/api/despesas/{id}` | Exclui uma despesa |
| GET | `/health` | Verifica se a aplicação está no ar |

Exemplo de cadastro:

```bash
curl -X POST http://localhost:8080/api/despesas \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Mercado","valor":12.34,"data":"2026-10-10"}'
```

```json
{"id":1,"descricao":"Mercado","valor":12.34,"data":"2026-10-10"}
```

## Testes

```bash
./mvnw verify
```

Executa os testes de service, controller (endpoints), repository (contra um SQLite em arquivo temporário) e o teste de desempenho da listagem de um mês com 1.000 despesas. Os testes não usam `~/.despesas`.

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

## Documentação

- [docs/escopo-mvp.md](docs/escopo-mvp.md): requisitos funcionais e não funcionais do MVP.
- [docs/backlog.md](docs/backlog.md): releases e backlog, com critérios de aceite.
- [docs/diagramas.md](docs/diagramas.md): diagramas de componentes, fluxo de dados, sequência, modelo de dados e implantação.
- [docs/api.md](docs/api.md): documentação da API.
- [prompts/](prompts/): prompts usados na geração do projeto.

## Roadmap

| Release | Escopo | Situação |
|---|---|---|
| v0.1.0 | Estrutura do projeto e endpoint `/health` | Concluída |
| v0.2.0 | Cadastro e listagem de despesas | Concluída |
| v0.3.0 | Edição e exclusão de despesas | Concluída |
| v0.4.0 | Filtro por mês e total do período | Concluída |
| v1.0.0 | Testes e documentação | Concluída |

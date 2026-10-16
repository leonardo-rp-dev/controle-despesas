# Diagramas

Os diagramas usam Mermaid e são renderizados pelo GitHub.

## 1. Componentes

Camadas da aplicação e suas dependências: o controller usa DTOs e delega ao service, que acessa o banco pelo repository.

```mermaid
flowchart LR
    subgraph Navegador
        UI["Frontend<br/>HTML + Alpine.js"]
    end

    subgraph App["Aplicação Spring Boot"]
        C[Controller]
        H[GlobalExceptionHandler]
        D[DTOs]
        S[Service]
        R[Repository]
        M[Model]
    end

    DB[("SQLite")]

    UI -->|"HTTP + JSON"| C
    C --> D
    C --> S
    C -.->|exceções| H
    S --> D
    S --> M
    S --> R
    R --> M
    R -->|JDBC| DB
```

## 2. Fluxo de dados

Caminho do clique no navegador até o SQLite e a volta, com as conversões de vírgula para ponto e de reais para centavos.

```mermaid
flowchart TD
    subgraph Ida["Ida: salvar despesa"]
        A["Clique em Salvar<br/>campo valor: 12,34"] --> B["Alpine troca vírgula por ponto<br/>valor: 12.34"]
        B --> C["POST /api/despesas<br/>JSON com valor em reais"]
        C --> D["Controller recebe o DTO"]
        D --> E["Service valida e converte<br/>12.34 reais para 1234 centavos"]
        E --> F["Repository grava<br/>valor_centavos = 1234"]
        F --> G[("SQLite")]
    end

    subgraph Volta["Volta: listar despesas"]
        G2[("SQLite")] --> H["Repository lê<br/>valor_centavos = 1234"]
        H --> I["Service converte<br/>1234 centavos para 12.34 reais"]
        I --> J["Controller devolve o DTO<br/>JSON com valor: 12.34"]
        J --> K["Alpine formata a exibição<br/>R$ 12,34"]
    end

    G -.->|"mesmo arquivo"| G2
```

## 3. Sequência

Cadastro de uma despesa seguido da listagem do mês com o total.

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as Frontend
    participant C as Controller
    participant S as Service
    participant R as Repository
    participant B as SQLite

    U->>F: preenche o formulário e salva
    F->>C: POST /api/despesas
    C->>S: cadastrar(dto)
    S->>S: valida e converte reais em centavos
    S->>R: save(despesa)
    R->>B: INSERT INTO despesa
    B-->>R: id gerado
    R-->>S: despesa salva
    S-->>C: dto de saída
    C-->>F: 201 Created

    U->>F: seleciona o mês 2026-10
    F->>C: GET /api/despesas?mes=2026-10
    C->>S: listar(mes)
    S->>R: busca por período
    R->>B: SELECT por intervalo de data
    B-->>R: linhas
    R-->>S: despesas
    S-->>C: dtos com valor em reais
    C-->>F: 200 OK com a lista

    F->>C: GET /api/despesas/total?mes=2026-10
    C->>S: total(mes)
    S->>R: soma de valor_centavos no período
    R->>B: SELECT SUM(valor_centavos)
    B-->>R: soma em centavos
    R-->>S: soma
    S-->>C: total em reais
    C-->>F: 200 OK com o total
    F-->>U: exibe lista e total em R$
```

## 4. Modelo de dados

Tabela `despesa`, com o valor guardado em centavos inteiros.

```mermaid
erDiagram
    despesa {
        INTEGER id PK
        TEXT descricao "ate 200 caracteres"
        INTEGER valor_centavos "valor em centavos"
        DATE data "AAAA-MM-DD"
    }
```

## 5. Implantação

Tudo roda na máquina local: o navegador acessa a aplicação em um único processo Java, que lê e grava o arquivo do banco no diretório do usuário.

```mermaid
flowchart LR
    subgraph PC["Máquina local"]
        subgraph Nav["Navegador"]
            UI["Frontend<br/>HTML + Alpine.js"]
        end

        subgraph JVM["Processo JVM Java 21"]
            App["Aplicação Spring Boot<br/>Tomcat embutido na porta 8080"]
        end

        subgraph FS["Sistema de arquivos"]
            DB[("~/.despesas/despesas.db")]
        end
    end

    UI -->|"HTTP localhost:8080"| App
    App -.->|"serve HTML, CSS e JS"| UI
    App -->|"JDBC sqlite-jdbc"| DB
```

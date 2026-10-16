# Prompts COSTAR - 2 - Sistema de Controle de Despesas

## 1. Criar docs/escopo-mvp.md

````markdown
# Contexto
Projeto "Controle de Despesas": app pessoal, sem login, executado localmente.
Stack: Java 21, Spring Boot 3, Maven, SQLite, HTML + Alpine.js.
Funcionalidades previstas na v1: cadastrar, listar, editar, excluir e filtrar despesas por mês com total.
Despesa: descrição (até 200 caracteres), valor (em reais na API, centavos no banco) e data.
Fora da v1: receitas, orçamento, gráficos, login, importação de extrato e IA.

# Objetivo
Criar o arquivo docs/escopo-mvp.md com as seções:
1. Objetivo do MVP
2. Requisitos funcionais (RF-01, RF-02...)
3. Requisitos não funcionais (RNF-01, RNF-02...): desempenho, usabilidade, portabilidade, manutenibilidade e dados locais
4. Fora do escopo

# Estilo
Markdown simples. Cada requisito em uma linha, com ID e texto verificável.

# Tom
Objetivo e profissional.

# Público
Desenvolvedores do projeto.

# Resposta
Criar o arquivo em disco, sem exibir o conteúdo. Ao final, informar o caminho e o total de requisitos de cada tipo.
````

## 2. Criar docs/backlog.md

````markdown
# Contexto
Projeto "Controle de Despesas". O escopo do MVP está em docs/escopo-mvp.md (ler antes de começar).
Roadmap de releases:
- v0.1.0: estrutura e /health
- v0.2.0: cadastro e listagem
- v0.3.0: edição e exclusão
- v0.4.0: filtro por mês e total
- v1.0.0: testes e documentação

# Objetivo
Criar o arquivo docs/backlog.md com:
1. Releases previstas: tabela com versão, entrega e IDs dos itens.
2. Backlog mínimo: um item por linha, com ID, título e critérios de aceite em formato "Dado/Quando/Então".
   - RF-xx: requisitos funcionais, com os mesmos IDs de docs/escopo-mvp.md
   - RT-xx: requisitos técnicos (estrutura, banco, testes, documentação)
Cada item deve pertencer a uma release.

# Estilo
Markdown simples. Itens curtos e verificáveis, sem repetir texto do escopo.

# Tom
Objetivo e profissional.

# Público
Desenvolvedores do projeto.

# Resposta
Criar o arquivo em disco, sem exibir o conteúdo. Ao final, informar o caminho e a quantidade de itens por release.
````

## 3. Criar os diagramas de arquitetura em Mermaid

````markdown
# Contexto
Projeto "Controle de Despesas": monólito Spring Boot com um único processo.
Camadas: frontend (HTML + Alpine.js) → Controller → Service → Repository → SQLite (~/.despesas/despesas.db).
O controller usa DTOs e um GlobalExceptionHandler.
Tabela despesa: id, descricao, valor_centavos, data.
Endpoints: POST/GET /api/despesas, GET/PUT/DELETE /api/despesas/{id}, GET /api/despesas/total?mes=AAAA-MM, GET /health.

# Objetivo
Criar o arquivo docs/diagramas.md com os diagramas Mermaid abaixo, cada um com título e uma linha de explicação:
1. Componentes (flowchart): camadas e dependências
2. Fluxo de dados (flowchart): do clique no navegador até o SQLite e a volta, incluindo conversão vírgula/ponto e reais/centavos
3. Sequência: cadastrar despesa e listar o mês com total
4. Modelo de dados (erDiagram): tabela despesa
5. Implantação (flowchart): navegador, aplicação Spring Boot e arquivo do banco na máquina local

# Estilo
Mermaid válido, renderizável no GitHub, com nomes curtos em português.

# Tom
Técnico.

# Público
Desenvolvedores do projeto.

# Resposta
Criar o arquivo em disco, sem exibir o conteúdo. Ao final, informar o caminho e listar os diagramas criados.
````

## 4. Criar as mensagens de commit (Conventional Commits)

````markdown
# Contexto
Projeto "Controle de Despesas". Criação dos arquivos:
- prompts/prompts-2.md (prompts para criação de documentação)
- docs/escopo-mvp.md (escopo do MVP)
- docs/backlog.md (releases e backlog)
- docs/diagramas.md (diagramas Mermaid)

# Objetivo
Criar uma mensagem de commit no padrão Conventional Commits para cada arquivo, na ordem acima.

# Estilo
Tipo adequado (docs), em português, imperativo, minúscula, sem ponto final,
título com até 72 caracteres e corpo com itens curtos.

# Tom
Objetivo.

# Público
Quem lê o histórico do Git.

# Resposta
As três mensagens, cada uma em um bloco de código, na ordem dos commits.
````

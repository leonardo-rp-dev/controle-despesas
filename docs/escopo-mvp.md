# Escopo do MVP

## 1. Objetivo do MVP

Entregar a v1.0.0 do Controle de Despesas: uma aplicação pessoal, sem login e executada localmente, que permite cadastrar, listar, editar, excluir e filtrar despesas por mês, com o total do período.

Uma despesa possui:

- descrição: texto de até 200 caracteres
- valor: em reais na API e em centavos no banco
- data

## 2. Requisitos funcionais

- RF-01: O sistema permite cadastrar uma despesa informando descrição, valor e data.
- RF-02: O sistema rejeita descrição vazia ou com mais de 200 caracteres, retornando HTTP 400 com mensagem de erro.
- RF-03: O sistema rejeita valor ausente, zero ou negativo, e valor com mais de duas casas decimais, retornando HTTP 400.
- RF-04: O sistema rejeita data ausente ou fora do formato ISO-8601 (`yyyy-MM-dd`), retornando HTTP 400.
- RF-05: O sistema lista todas as despesas cadastradas, da data mais recente para a mais antiga.
- RF-06: O sistema permite editar descrição, valor e data de uma despesa existente, aplicando as mesmas validações do cadastro.
- RF-07: O sistema retorna HTTP 404 ao editar ou excluir uma despesa com id inexistente.
- RF-08: O sistema permite excluir uma despesa por id, que deixa de aparecer nas listagens seguintes.
- RF-09: O sistema permite filtrar a listagem por mês e ano, retornando apenas as despesas com data nesse período.
- RF-10: O sistema exibe o total do período filtrado, igual à soma dos valores das despesas listadas.
- RF-11: O sistema exibe o total de todas as despesas quando nenhum filtro de mês é aplicado.
- RF-12: A interface web, servida pela própria aplicação em `http://localhost:8080`, permite executar cadastro, listagem, edição, exclusão e filtro por mês.
- RF-13: A interface exibe ao usuário a mensagem de erro quando uma operação é rejeitada pela API ou falha.

## 3. Requisitos não funcionais

### Desempenho

- RNF-01: A listagem de um mês com até 1.000 despesas responde em até 500 ms em uma máquina pessoal comum.
- RNF-02: A aplicação inicia e passa a responder em `/health` em até 15 segundos.

### Usabilidade

- RNF-03: O cadastro de uma despesa é feito em uma única tela, sem navegação entre páginas.
- RNF-04: Os valores são exibidos no formato monetário brasileiro (exemplo: `R$ 1.234,56`) e as datas no formato `dd/MM/yyyy`.
- RNF-05: A interface permanece utilizável, sem rolagem horizontal, em telas a partir de 360 px de largura.

### Portabilidade

- RNF-06: A aplicação roda em Windows, Linux e macOS com JDK 21 instalado, usando apenas `./mvnw spring-boot:run`.
- RNF-07: A interface funciona sem acesso à internet, com Alpine.js servido localmente, sem CDN.

### Manutenibilidade

- RNF-08: O código segue a separação em pacotes `controller`, `service`, `repository`, `model`, `dto` e `exception` sob `br.com.despesas`.
- RNF-09: As regras de negócio e validações ficam na camada `service`, e os controllers apenas recebem a requisição e delegam.
- RNF-10: Os testes automatizados cobrem as regras da camada `service` e os endpoints, e `./mvnw verify` termina sem falhas.

### Dados locais

- RNF-11: Os dados são gravados em `~/.despesas/despesas.db`, e o diretório e o arquivo são criados automaticamente na primeira execução.
- RNF-12: A aplicação não envia dados a serviços externos.
- RNF-13: Os valores são armazenados no banco como inteiros em centavos, sem uso de ponto flutuante.

## 4. Fora do escopo

Itens que não fazem parte da v1:

- receitas
- orçamento
- gráficos
- login e controle de usuários
- importação de extrato
- recursos de IA

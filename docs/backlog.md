# Backlog

Os itens `RF-xx` correspondem aos requisitos funcionais de [escopo-mvp.md](escopo-mvp.md). Os itens `RT-xx` são requisitos técnicos. Cada item pertence a uma única release.

## 1. Releases previstas

| Versão | Entrega | Itens |
|---|---|---|
| v0.1.0 | Estrutura e `/health` | RT-01, RT-02, RT-03, RT-04 |
| v0.2.0 | Cadastro e listagem | RF-01, RF-02, RF-03, RF-04, RF-05, RF-13, RT-05, RT-06, RT-07 |
| v0.3.0 | Edição e exclusão | RF-06, RF-07, RF-08, RT-08 |
| v0.4.0 | Filtro por mês e total | RF-09, RF-10, RF-11, RF-12 |
| v1.0.0 | Testes e documentação | RT-09, RT-10, RT-11, RT-12 |

## 2. Backlog mínimo

### v0.1.0: estrutura e /health

- **RT-01 Estrutura base.** Dado o repositório clonado com JDK 21, quando executo `./mvnw spring-boot:run`, então a aplicação sobe sem erros com os pacotes `controller`, `service`, `repository`, `model`, `dto` e `exception` presentes.
- **RT-02 Endpoint de saúde.** Dada a aplicação em execução, quando envio `GET /health`, então recebo HTTP 200 com `status` igual a `ok` e `timestamp` em ISO-8601.
- **RT-03 Página de verificação.** Dada a página inicial aberta, quando clico no botão, então o retorno de `/health` é exibido na tela.
- **RT-04 Configuração do SQLite.** Dada a primeira execução sem `~/.despesas`, quando a aplicação inicia, então o diretório e o arquivo `despesas.db` são criados e a conexão funciona.

### v0.2.0: cadastro e listagem

- **RF-01 Cadastrar despesa.** Dados descrição, valor e data válidos, quando envio o cadastro, então a despesa é salva e retornada com um id.
- **RF-02 Validar descrição.** Dada uma descrição vazia ou com 201 caracteres, quando envio o cadastro, então recebo HTTP 400 e nada é salvo.
- **RF-03 Validar valor.** Dado um valor zero, negativo ou com três casas decimais, quando envio o cadastro, então recebo HTTP 400 e nada é salvo.
- **RF-04 Validar data.** Dada uma data ausente ou como `15/10/2026`, quando envio o cadastro, então recebo HTTP 400 e nada é salvo.
- **RF-05 Listar despesas.** Dadas três despesas com datas diferentes, quando listo, então elas voltam da data mais recente para a mais antiga.
- **RF-13 Exibir erros na interface.** Dada uma operação rejeitada pela API, quando a resposta chega, então a interface mostra a mensagem retornada e mantém os dados digitados.
- **RT-05 Entidade e persistência em centavos.** Dado o valor `12.34` enviado à API, quando a despesa é salva, então o banco guarda o inteiro `1234` e a API devolve `12.34`.
- **RT-06 Tratamento global de erros.** Dada uma exceção de validação ou de negócio, quando ela ocorre em qualquer endpoint, então a resposta segue um formato único de erro com código HTTP adequado.
- **RT-07 Interface de cadastro e listagem.** Dada a página inicial, quando preencho o formulário e confirmo, então a nova despesa aparece na lista sem recarregar a página.

### v0.3.0: edição e exclusão

- **RF-06 Editar despesa.** Dada uma despesa existente, quando envio novos valores válidos, então ela é atualizada e a listagem reflete a mudança.
- **RF-07 Id inexistente.** Dado um id que não existe, quando tento editar ou excluir, então recebo HTTP 404.
- **RF-08 Excluir despesa.** Dada uma despesa existente, quando a excluo, então ela não aparece mais na listagem.
- **RT-08 Interface de edição e exclusão.** Dada a lista de despesas, quando edito ou excluo um item, então a lista é atualizada sem recarregar a página, com confirmação antes de excluir.

### v0.4.0: filtro por mês e total

- **RF-09 Filtrar por mês.** Dadas despesas em meses diferentes, quando filtro por mês e ano, então só voltam as despesas desse período.
- **RF-10 Total do período.** Dado um mês filtrado, quando a listagem é exibida, então o total mostrado é igual à soma dos valores listados.
- **RF-11 Total geral.** Dado que nenhum mês está selecionado, quando a listagem é exibida, então o total é a soma de todas as despesas.
- **RF-12 Interface completa.** Dada a página inicial, quando uso a aplicação, então consigo cadastrar, listar, editar, excluir e filtrar por mês sem usar a API diretamente.

### v1.0.0: testes e documentação

- **RT-09 Testes de service.** Dado `./mvnw verify`, quando executo, então os testes da camada `service` cobrem validações, cálculo de total e id inexistente, e todos passam.
- **RT-10 Testes de endpoints.** Dado `./mvnw verify`, quando executo, então cada endpoint tem ao menos um teste de sucesso e um de erro, e todos passam.
- **RT-11 Verificação de desempenho.** Dado um banco com 1.000 despesas em um mês, quando listo esse mês, então a resposta leva até 500 ms.
- **RT-12 Documentação.** Dado o repositório na release final, quando leio o `README.md` e a documentação da API, então encontro como rodar, os endpoints com exemplos e o roadmap atualizado.

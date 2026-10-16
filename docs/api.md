# API

API REST do Controle de Despesas. Os exemplos abaixo foram executados contra a aplicação em execução, e as respostas são as retornadas por ela (os valores de `timestamp` variam a cada chamada).

## 1. Visão geral

| Item | Valor |
|---|---|
| Base URL | `http://localhost:8080` |
| Autenticação | Nenhuma |
| Formato | JSON (`Content-Type: application/json`) em requisições com corpo e em todas as respostas |
| Valor | Reais, com ponto decimal e no máximo duas casas (`12.34`). Nas respostas, sempre com duas casas (`1500.00`) |
| Data | `yyyy-MM-dd` (`2026-10-15`) |
| Mês | `yyyy-MM` (`2026-10`) |

Uma despesa tem os campos:

| Campo | Tipo | Regra |
|---|---|---|
| `id` | número | Gerado pela aplicação. Só aparece nas respostas |
| `descricao` | texto | Obrigatória, até 200 caracteres. Espaços nas pontas são removidos antes de contar e de gravar |
| `valor` | número | Obrigatório, maior que zero, no máximo duas casas decimais, até `92233720368547758.07` |
| `data` | texto | Obrigatória, no formato `yyyy-MM-dd` |

Endpoints:

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/despesas` | Cadastra uma despesa |
| GET | `/api/despesas` | Lista as despesas, com filtro opcional por mês |
| GET | `/api/despesas/total` | Soma das despesas, com filtro opcional por mês |
| PUT | `/api/despesas/{id}` | Edita uma despesa |
| DELETE | `/api/despesas/{id}` | Exclui uma despesa |
| GET | `/health` | Verifica se a aplicação está no ar |

### Formato de erro

Toda resposta de erro tem o mesmo formato:

```json
{
  "status": 400,
  "mensagem": "Descrição é obrigatória",
  "timestamp": "2026-10-16T00:12:12.261719400-03:00"
}
```

A lista completa de mensagens está na [seção 8](#8-tabela-de-erros).

### Nota sobre os exemplos curl

Os exemplos usam aspas simples, como no Bash. No PowerShell, chame `curl.exe` (e não o alias `curl`) e grave o JSON em um arquivo, enviando-o com `--data-binary "@despesa.json"`.

## 2. Cadastrar despesa

`POST /api/despesas`

Corpo:

```json
{"descricao": "Mercado", "valor": 12.34, "data": "2026-10-10"}
```

```bash
curl -i -X POST http://localhost:8080/api/despesas \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Mercado","valor":12.34,"data":"2026-10-10"}'
```

Sucesso: **201 Created**, com o cabeçalho `Location: /api/despesas/1`.

```json
{"id":1,"descricao":"Mercado","valor":12.34,"data":"2026-10-10"}
```

Um valor sem casas decimais é devolvido com duas: `"valor": 1500` retorna `"valor":1500.00`.

Erros possíveis: 400 (validação, corpo inválido), 415 (content-type diferente de JSON). Veja a [seção 8](#8-tabela-de-erros).

Exemplo de erro (descrição vazia), **400 Bad Request**:

```json
{"status":400,"mensagem":"Descrição é obrigatória","timestamp":"2026-10-16T00:12:12.261719400-03:00"}
```

## 3. Listar despesas

`GET /api/despesas`

| Parâmetro | Obrigatório | Descrição |
|---|---|---|
| `mes` | Não | Mês no formato `yyyy-MM`. Sem ele (ou em branco), lista todas as despesas |

A lista vem da data mais recente para a mais antiga e, em datas iguais, do maior id para o menor.

```bash
curl http://localhost:8080/api/despesas
```

Sucesso: **200 OK**.

```json
[{"id":2,"descricao":"Aluguel","valor":1500.00,"data":"2026-10-15"},{"id":1,"descricao":"Mercado","valor":12.34,"data":"2026-10-10"},{"id":3,"descricao":"Farmácia","valor":10.10,"data":"2026-09-30"}]
```

Com filtro por mês (inclui o primeiro e o último dia do mês):

```bash
curl "http://localhost:8080/api/despesas?mes=2026-10"
```

```json
[{"id":2,"descricao":"Aluguel","valor":1500.00,"data":"2026-10-15"},{"id":1,"descricao":"Mercado","valor":12.34,"data":"2026-10-10"}]
```

Um mês sem despesas retorna **200 OK** com a lista vazia:

```bash
curl "http://localhost:8080/api/despesas?mes=2026-12"
```

```json
[]
```

Erro: **400 Bad Request** se `mes` estiver fora do formato (por exemplo, `2026-13`):

```json
{"status":400,"mensagem":"Mês inválido. Use o formato yyyy-MM","timestamp":"2026-10-16T00:12:13.113628400-03:00"}
```

## 4. Total das despesas

`GET /api/despesas/total`

| Parâmetro | Obrigatório | Descrição |
|---|---|---|
| `mes` | Não | Mês no formato `yyyy-MM`. Sem ele, soma todas as despesas |

Sem filtro:

```bash
curl http://localhost:8080/api/despesas/total
```

Sucesso: **200 OK**.

```json
{"total":1522.44}
```

Com filtro por mês:

```bash
curl "http://localhost:8080/api/despesas/total?mes=2026-10"
```

```json
{"total":1512.34}
```

Sem despesas no período, o total é `0.00`:

```bash
curl "http://localhost:8080/api/despesas/total?mes=2026-12"
```

```json
{"total":0.00}
```

Erro: **400 Bad Request** se `mes` estiver fora do formato (por exemplo, `10/2026`), com a mesma mensagem da listagem.

## 5. Editar despesa

`PUT /api/despesas/{id}`

| Parâmetro | Descrição |
|---|---|
| `id` | Identificador da despesa, numérico |

O corpo é o mesmo do cadastro e substitui descrição, valor e data. As validações são as do cadastro.

```bash
curl -X PUT http://localhost:8080/api/despesas/1 \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Mercado do mês","valor":99.9,"data":"2026-10-12"}'
```

Sucesso: **200 OK**, com a despesa atualizada.

```json
{"id":1,"descricao":"Mercado do mês","valor":99.90,"data":"2026-10-12"}
```

Erros:

- **404 Not Found** se o id não existe (verificado antes da validação do corpo, então um id inexistente com corpo inválido também retorna 404).
- **400 Bad Request** para corpo inválido ou id não numérico.

```json
{"status":404,"mensagem":"Despesa não encontrada","timestamp":"2026-10-16T00:12:12.928219100-03:00"}
```

## 6. Excluir despesa

`DELETE /api/despesas/{id}`

```bash
curl -i -X DELETE http://localhost:8080/api/despesas/3
```

Sucesso: **204 No Content**, sem corpo. A despesa deixa de aparecer nas listagens seguintes.

Erros:

- **404 Not Found**, "Despesa não encontrada", se o id não existe.
- **400 Bad Request**, "Identificador inválido", se o id não é numérico.

## 7. Health check

`GET /health`

```bash
curl http://localhost:8080/health
```

Sucesso: **200 OK**.

```json
{"timestamp":"2026-10-16T00:12:11.1753467-03:00","status":"ok"}
```

O `timestamp` está em ISO-8601, com o fuso do servidor.

## 8. Tabela de erros

| Status | Mensagem | Quando ocorre |
|---|---|---|
| 400 | Descrição é obrigatória | `descricao` ausente, nula, vazia ou só com espaços |
| 400 | Descrição deve ter no máximo 200 caracteres | `descricao` com mais de 200 caracteres, sem contar os espaços das pontas |
| 400 | Valor é obrigatório | `valor` ausente ou nulo |
| 400 | Valor deve ser maior que zero | `valor` igual a zero ou negativo |
| 400 | Valor deve ter no máximo duas casas decimais | `valor` com mais de duas casas (por exemplo, `12.345`) |
| 400 | Valor acima do limite permitido | `valor` maior que `92233720368547758.07` |
| 400 | Valor inválido. Informe um número, por exemplo 12.34 | `valor` que não é número (por exemplo, `"abc"`) |
| 400 | Data é obrigatória | `data` ausente ou nula |
| 400 | Data inválida. Use o formato yyyy-MM-dd | `data` fora do formato (por exemplo, `15/10/2026`) |
| 400 | Corpo da requisição ausente | Corpo nulo recebido pelo service |
| 400 | Corpo da requisição inválido ou ausente | JSON malformado ou requisição sem corpo |
| 400 | Mês inválido. Use o formato yyyy-MM | `mes` fora do formato (`2026-13`, `10/2026`, `abc`) |
| 400 | Identificador inválido | `id` não numérico na rota |
| 404 | Despesa não encontrada | PUT ou DELETE com id inexistente |
| 404 | Requisição inválida | Rota inexistente |
| 405 | Requisição inválida | Método HTTP não suportado pela rota (por exemplo, `PATCH /api/despesas` ou `GET /api/despesas/1`) |
| 415 | Requisição inválida | `Content-Type` diferente de JSON em requisição com corpo |
| 500 | Erro interno do servidor | Falha inesperada. Os detalhes ficam só no log da aplicação |

Nos erros 404 de rota, 405 e 415, a mensagem é genérica e o status HTTP indica a causa. O erro 500 não foi provocado nos exemplos desta página; a mensagem vem do código.

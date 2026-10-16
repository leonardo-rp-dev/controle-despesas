# Checklist de release: v1.0.0

Data da verificação: 2026-10-16.

Estado do Git na verificação:

| Item | Valor |
|---|---|
| Branch | `feature/testes-documentacao` |
| HEAD | `bf5662c` build: adiciona Makefile com install, run e test |
| `origin/feature/testes-documentacao` | igual ao HEAD |
| `origin/main` | `fba4886` (v0.4.0), 14 commits atrás do HEAD |
| Branch padrão no GitHub | `main` |
| Árvore de trabalho | limpa (antes de adicionar este arquivo) |
| Tags | nenhuma, local ou remota |

Legenda: `[x]` verificado, com a evidência na própria linha. `[ ]` pendente, com o passo exato a executar.

## 1. Técnico

### Build e artefato

- [x] Build limpo e testes verdes. Evidência: `./mvnw -B clean verify` terminou com BUILD SUCCESS, 121 testes, 0 falhas, em 36 s.
- [x] Versão 1.0.0 no `pom.xml` e no jar. Evidência: `<version>1.0.0</version>` na linha 16 do `pom.xml`; gerou `target/controle-despesas-1.0.0.jar` (63,2 MB).
- [x] Jar sobe e responde. Evidência: `java -jar` com `user.home` temporário respondeu `GET /health` com `status: ok` em 12,6 s, `GET /` com 200 e `POST /api/despesas` com id 1.
- [x] Banco criado do zero. Evidência: com `user.home` vazio, `.despesas/despesas.db` foi criado na primeira subida.
- [x] Cobertura medida. Evidência: JaCoCo em 2026-10-16: linhas 97,6% (121 de 124), branches 100% (30 de 30). As 3 linhas descobertas estão no `main` de `DespesasApplication`.

### Repositório

- [x] Nenhum arquivo indevido versionado. Evidência: dos 46 arquivos de `git ls-files`, nenhum casa com `target/`, `*.db`, `*.jar`, `*.class`, `*.log`, `.env`, `application-local*`, `.idea/` ou `.vscode/`.
- [x] Sem segredos versionados. Evidência: busca por `password`, `secret`, `token`, `api_key` e chaves privadas só encontra variáveis de proxy do Maven Wrapper (`MVNW_USERNAME`, `MVNW_PASSWORD`) em `mvnw` e `mvnw.cmd`.
- [x] `mvnw` com permissão de execução. Evidência: `git ls-files -s mvnw` mostra modo `100755`.
- [x] CI. Evidência: não há workflow versionado; a CI está registrada como próximo passo no README.
- [x] Branch remota atualizada até o HEAD de referência. Evidência: `git rev-parse origin/feature/testes-documentacao` igual a `git rev-parse HEAD` (`bf5662c`) após `git fetch`.
- [ ] Árvore limpa e branch remota igual ao HEAD depois de commitar este checklist.

  ```bash
  git add docs/release-checklist.md
  git commit -m "docs: adiciona checklist de release da v1.0.0"
  git push
  git status -sb
  ```

- [ ] Pull request mesclado na `main` (squash). Abrir o PR de `feature/testes-documentacao` para `main` no GitHub, conferir a lista de arquivos e mesclar. Depois:

  ```bash
  git switch main
  git pull
  git log --oneline -3
  grep -n "<version>1.0.0</version>" pom.xml
  ```

### Verificação que depende do autor

- [ ] Roteiro manual da interface no navegador (fecha RF-12, RF-13 e RNF-03 a RNF-05). Subir com `./mvnw spring-boot:run`, abrir http://localhost:8080 e conferir:
  - cadastro válido (valor com vírgula); descrição vazia; valor com 3 casas; data ausente. As mensagens da API aparecem e o formulário mantém os dados;
  - editar e atualizar; editar e cancelar; excluir confirmando; excluir cancelando a confirmação;
  - filtro por mês com despesas, mês vazio ("Nenhuma despesa neste mês." e total `R$ 0,00`) e "Limpar filtro";
  - cadastrar com filtro ativo em outro mês (a despesa não entra na lista) e editar mudando a data para fora do mês filtrado;
  - valores como `R$ 1.234,56`, datas como `dd/MM/yyyy`, e sem rolagem horizontal em 360 px (DevTools).
- [ ] Teste em Linux ou macOS (fecha RNF-06). Em uma máquina com JDK 21, Git e `curl` ou `wget` (o wrapper é `only-script`):

  ```bash
  git clone https://github.com/leonardo-rp-dev/controle-despesas.git
  cd controle-despesas
  ./mvnw verify
  ./mvnw spring-boot:run
  curl http://localhost:8080/health
  ```

- [ ] Opcional: executar o `Makefile` com um Make real (`make install`, `make run`, `make test`). Hoje só os comandos das receitas foram validados.

## 2. Documentação e publicação

### Documentação

- [x] README com links válidos. Evidência: os 5 links relativos do README apontam para arquivos existentes; o jar citado é `controle-despesas-1.0.0.jar`.
- [x] `docs/` coerente com o código. Evidência: as 18 mensagens da tabela de erros de `docs/api.md` existem no código (a de 200 caracteres é montada em tempo de execução e é confirmada por teste e por `curl`); as 6 rotas do `api.md` existem nos controllers; não há `SNAPSHOT` no README nem em `docs/`; o único link relativo de `docs/backlog.md` é válido.
- [x] Notas da release redigidas. Evidência: rascunho na seção 3 deste arquivo.
- [ ] Licença. Evidência: nenhum `LICENSE` versionado. Decisão pendente do autor, entre:
  - adicionar uma licença (por exemplo, MIT): criar `LICENSE` na raiz e commitar;
  - publicar sem licença (todos os direitos reservados): registrar a decisão aqui.

  Decisão: _______________

### Publicação

- [ ] Tag `v1.0.0` na `main`, após o merge. Nenhuma tag existe hoje.

  ```bash
  git switch main
  git pull
  git tag -a v1.0.0 -m "Controle de Despesas v1.0.0"
  git push origin v1.0.0
  ```

- [ ] GitHub Release com notas e o jar. Decisão recomendada: anexar `target/controle-despesas-1.0.0.jar` (63,2 MB, jar executável). Ele não está no repositório (`*.jar` está no `.gitignore`), então só chega aos usuários como artefato da release. A CLI `gh` não está instalada nesta máquina; pela web, use Releases > Draft a new release, escolha a tag `v1.0.0`, cole as notas da seção 3 e anexe o jar. Com a `gh`, salve o texto da seção 3 em um arquivo fora do repositório (por exemplo, `notas-v1.0.0.md`) e rode:

  ```bash
  gh release create v1.0.0 target/controle-despesas-1.0.0.jar --title "v1.0.0" --notes-file notas-v1.0.0.md
  ```

  Decisão sobre anexar o jar: _______________

- [ ] Roteiro de rollback. Se for preciso retirar a release:

  ```bash
  gh release delete v1.0.0 --yes        # ou excluir pela página de Releases
  git push --delete origin v1.0.0
  git tag -d v1.0.0
  ```

  Se o problema estiver no código mesclado, reverter o commit do PR na `main`:

  ```bash
  git switch main
  git pull
  git revert <hash-do-commit-do-pr>
  git push
  ```

## 3. Notas da release (rascunho)

**Controle de Despesas v1.0.0**

Aplicação pessoal, sem autenticação, para registrar e acompanhar despesas mensais. Roda localmente e guarda os dados em um SQLite em `~/.despesas/despesas.db`.

Funcionalidades:

- cadastrar, listar, editar e excluir despesas (descrição até 200 caracteres, valor em reais, data);
- filtrar por mês e ver o total do mês ou o total geral;
- interface web em HTML e Alpine.js, servida pela própria aplicação, sem CDN;
- API REST documentada em `docs/api.md`.

Como rodar: JDK 21, `./mvnw spring-boot:run` e abrir http://localhost:8080. Ou, com o jar anexado: `java -jar controle-despesas-1.0.0.jar`.

Qualidade: 121 testes automatizados; cobertura medida em 2026-10-16 de 97,6% das linhas e 100% dos branches; listagem de um mês com 1.000 despesas em até 500 ms.

Limitações:

- uso local por uma pessoa, sem autenticação;
- apenas moeda em reais; valor máximo `92233720368547758.07`;
- sem paginação;
- erros 404 de rota, 405 e 415 com mensagem genérica;
- interface web sem teste automatizado;
- execução verificada apenas no Windows.

Documentação: README, `docs/escopo-mvp.md`, `docs/backlog.md`, `docs/diagramas.md` e `docs/api.md`.

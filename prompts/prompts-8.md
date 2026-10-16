# Prompts COSTAR - 8 - Makefile, auditoria do README e checklist de release

Estes prompts partem da v1.0.0 pronta (121 testes passando, README reescrito) e preparam a publicação no GitHub.

Ordem sugerida: 1 → 2 → 3. O Makefile entra no README antes da auditoria, e o checklist de release fecha a série.

| # | Prompt | Natureza |
|---|---|---|
| 1 | Makefile com `install`, `run` e `test` | Cria arquivo |
| 2 | Auditoria do README: reprodutibilidade e onboarding | Somente análise, resposta em checklist |
| 3 | Checklist final de release da v1.0.0 | Cria `docs/release-checklist.md` |

## 1. Gerar o Makefile

````markdown
# Contexto
Projeto "Controle de Despesas": Java 21, Spring Boot 3, Maven com Maven Wrapper (mvnw e mvnw.cmd), sem Makefile hoje.
Ler antes: README.md (seções Instalação, Execução e Testes), pom.xml e o .gitignore.
Comandos atuais: `./mvnw spring-boot:run` (Linux e macOS), `.\mvnw spring-boot:run` (Windows, PowerShell), `./mvnw verify` (testes) e `./mvnw clean package` (jar em target/controle-despesas-1.0.0.jar).
O Make não vem instalado no Windows; nesse sistema ele costuma rodar pelo Git Bash, WSL ou instalado à parte.

# Objetivo
Criar o Makefile na raiz com três targets:
1. `install`: baixar as dependências e compilar o projeto sem rodar os testes (por exemplo, `clean package -DskipTests`), deixando a máquina pronta para `run` e `test`.
2. `run`: iniciar a aplicação com `spring-boot:run`.
3. `test`: executar `verify` (todos os testes).
Requisitos:
- Simples e portável: usar o wrapper do projeto, não um Maven global; escolher `./mvnw` ou `mvnw.cmd` conforme o sistema (por exemplo, testando a variável OS = Windows_NT), em uma variável no topo do arquivo.
- Declarar os targets como `.PHONY`; target padrão `help` listando os três comandos, em uma linha cada.
- Sem dependências além do Make e do wrapper; sem recursos exclusivos do GNU Make que quebrem em ambientes comuns.
- Receitas curtas, sem lógica complicada, com `JAVA_HOME` e JDK 21 tratados apenas por uma mensagem clara quando o Maven falhar (sem scripts de detecção).
Depois, acrescentar ao README.md uma seção curta "Uso com Make" (3 comandos e uma nota sobre o Make no Windows), sem reescrever o resto.

# Estilo
Makefile enxuto, com comentário de uma linha por target, indentação de receita com TAB.

# Tom
Técnico e direto.

# Público
Desenvolvedores que vão instalar e rodar o projeto.

# Resposta
Criar/alterar os arquivos em disco, sem exibir o conteúdo. Ao final, informar se o Make estava disponível nesta máquina e o que foi validado de fato: se não houver Make, validar as receitas executando manualmente cada comando que elas rodariam e dizer claramente que o Makefile em si não foi executado. Rodar `make test` (ou o equivalente) e informar o resultado e o total de testes.
````

## 2. Analisar o README (reprodutibilidade e onboarding)

````markdown
# Contexto
Projeto "Controle de Despesas": versão 1.0.0, Java 21, Spring Boot 3, SQLite, Maven Wrapper, repositório no GitHub (leonardo-rp-dev/controle-despesas).
Ler antes: README.md, pom.xml, .gitignore, application.properties, docs/api.md e a estrutura do repositório (incluindo se existem LICENSE, .gitattributes, .editorconfig e CI).
Esta etapa é somente de análise: não alterar nenhum arquivo.

# Objetivo
Analisar o README.md e responder a duas perguntas:
1. O que falta para o projeto ser reproduzível em uma máquina limpa?
2. Quais seções estão fracas para onboarding técnico?
Para a primeira pergunta, não basta ler: simular uma máquina limpa. Clonar o repositório local em uma pasta temporária nova, usar um `user.home` temporário e um repositório Maven local vazio (`-Dmaven.repo.local`), e executar literalmente os comandos do README (instalar, rodar, `/health`, jar, testes). Registrar o que falhar ou precisar de passo não documentado. Verificar também: versão do JDK exigida e a mensagem quando o JDK está errado, permissão de execução e fim de linha do mvnw, porta 8080 ocupada, necessidade de internet, tempo da primeira execução, e arquivos que o clone não traz (por exemplo, o que está no .gitignore e o que a aplicação espera encontrar).
Para a segunda, avaliar o README como um desenvolvedor novo que nunca viu o projeto: o que ele precisa saber nos primeiros 30 minutos e não encontra ou encontra confuso (por exemplo: como o frontend e a API se encontram, onde mudar uma regra, como depurar, como ler os testes, convenções).

# Estilo
Checklist objetivo em Markdown, em duas seções (Reprodutibilidade e Onboarding). Cada item: `[ ]` para o que falta ou está fraco e `[x]` para o que está bom, uma linha de evidência (comando executado ou trecho do README) e, nos itens pendentes, a correção sugerida em uma frase e a prioridade (alta, média, baixa). Sem texto corrido além de um resumo de duas linhas no início.

# Tom
Técnico, crítico e direto.

# Público
O autor do projeto, que vai decidir o que corrigir antes da publicação.

# Resposta
Entregar o checklist na resposta, sem criar nem alterar arquivos (a pasta temporária do clone deve ser apagada ao final). Terminar perguntando quais itens corrigir.
````

## 3. Gerar o checklist de release da v1.0.0

````markdown
# Contexto
Projeto "Controle de Despesas": versão 1.0.0 pronta localmente na branch feature/testes-documentacao, com commits ainda não enviados ao GitHub. O fluxo do repositório usa branches feature/*, pull requests para a main e merges em squash.
Ler antes: README.md, pom.xml, .gitignore, docs/ (escopo-mvp.md, backlog.md, api.md, diagramas.md), a árvore de arquivos versionados (`git ls-files`) e o estado do Git (branch, commits à frente do remoto, árvore limpa).
Pendências conhecidas: roteiro manual no navegador (RF-12, RF-13, RNF-03 a RNF-05), teste em Linux ou macOS (RNF-06), e a decisão sobre tag e release no GitHub.

# Objetivo
Criar docs/release-checklist.md com o checklist final de release da v1.0.0, em duas partes e com um item por linha (`- [ ]` ou `- [x]`):
1. Técnico: build limpo (`./mvnw clean verify` verde e total de testes), versão 1.0.0 no pom.xml e no nome do jar, jar sobe e responde em /health, banco criado do zero, nenhum arquivo indevido versionado (banco .db, target/, segredos, configs locais), permissão de execução do mvnw, árvore limpa, commits enviados ao GitHub, pull request mesclado na main, CI (se existir), verificação manual da interface no navegador, teste em Linux ou macOS.
2. Documentação e publicação: README atualizado e com links válidos, docs/ coerentes com o código (versões, rotas, mensagens de erro), licença (verificar se existe LICENSE e registrar a decisão), notas da release (resumo das funcionalidades e limitações), tag v1.0.0 na main após o merge, GitHub Release com notas e o jar como artefato (decisão registrada), roteiro de rollback (como retirar a tag e a release).
Para cada item, executar a verificação que for possível localmente e marcar `[x]` somente com evidência (comando e resultado resumido em uma linha). Itens que dependem de ação do autor (push, merge do PR, tag, release, teste em outro sistema, verificação no navegador) ficam `[ ]` com o comando ou passo exato a executar. Não executar push, não criar tag nem release.

# Estilo
Markdown objetivo, sem emojis, com os comandos em blocos de código e a data e o estado do Git no topo do arquivo.

# Tom
Objetivo e profissional.

# Público
O autor do projeto e quem for avaliar a entrega.

# Resposta
Criar o arquivo em disco, sem exibir o conteúdo. Ao final, informar o caminho, quantos itens estão marcados e quantos pendentes, e listar os pendentes que bloqueiam a publicação.
````

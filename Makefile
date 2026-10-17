# Atalhos para o Maven Wrapper do Controle de Despesas.
# Requer JDK 21 instalado e JAVA_HOME apontando para ele.

ifeq ($(OS),Windows_NT)
MVNW = mvnw.cmd
else
MVNW = ./mvnw
endif

AVISO = Falha no Maven: confira se o JDK 21 esta instalado e se JAVA_HOME aponta para ele.

.PHONY: help install run test

# Lista os comandos disponiveis (target padrao).
help:
	@echo make install: baixa as dependencias e compila, sem rodar os testes
	@echo make run: inicia a aplicacao em http://localhost:8080
	@echo make test: executa todos os testes

# Baixa as dependencias e compila, sem rodar os testes.
install:
	$(MVNW) clean package -DskipTests || (echo $(AVISO) && exit 1)

# Inicia a aplicacao em http://localhost:8080.
run:
	$(MVNW) spring-boot:run || (echo $(AVISO) && exit 1)

# Executa todos os testes.
test:
	$(MVNW) verify || (echo $(AVISO) && exit 1)

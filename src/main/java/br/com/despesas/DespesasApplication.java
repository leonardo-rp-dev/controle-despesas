package br.com.despesas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DespesasApplication {

    public static void main(String[] args) throws IOException {
        // O SQLite não cria diretórios: garante ~/.despesas antes de abrir o banco
        Files.createDirectories(Path.of(System.getProperty("user.home"), ".despesas"));
        SpringApplication.run(DespesasApplication.class, args);
    }
}

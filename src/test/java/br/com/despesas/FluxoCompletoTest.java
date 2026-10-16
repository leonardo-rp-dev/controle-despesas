package br.com.despesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import br.com.despesas.repository.DespesaRepository;

/**
 * Fluxos ponta a ponta: controller, service, repository e SQLite real em arquivo temporário.
 * Cada teste começa com a tabela vazia. Não toca em ~/.despesas.
 * As descrições não usam acentos para que a leitura do corpo não dependa de charset.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoCompletoTest {

    private static final Path BANCO = criarBancoTemporario();

    private static Path criarBancoTemporario() {
        try {
            Path arquivo = Files.createTempFile("despesas-fluxo", ".db");
            arquivo.toFile().deleteOnExit();
            return arquivo;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + BANCO);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DespesaRepository repository;

    @BeforeEach
    void limparTabela() {
        repository.deleteAll();
    }

    @Test
    void cadastraListaFiltraEditaEExcluiMantendoListaETotalConsistentes() throws Exception {
        int mercado = cadastrar("Mercado", "10.10", "2026-10-10");
        int aluguel = cadastrar("Aluguel", "20.20", "2026-10-31");
        cadastrar("Farmacia", "5.05", "2026-09-30");

        assertThat(descricoes(null)).containsExactly("Aluguel", "Mercado", "Farmacia");
        assertThat(descricoes("2026-10")).containsExactly("Aluguel", "Mercado");
        assertThat(total("2026-10")).isEqualByComparingTo("30.30");
        assertThat(total("2026-09")).isEqualByComparingTo("5.05");
        assertThat(total(null)).isEqualByComparingTo("35.35");

        // Edita o aluguel, movendo-o para setembro
        mockMvc.perform(put("/api/despesas/" + aluguel)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Aluguel", "21.00", "2026-09-15")))
                .andExpect(status().isOk());

        assertThat(descricoes("2026-10")).containsExactly("Mercado");
        assertThat(descricoes("2026-09")).containsExactly("Farmacia", "Aluguel");
        assertThat(total("2026-10")).isEqualByComparingTo("10.10");
        assertThat(total("2026-09")).isEqualByComparingTo("26.05");
        assertThat(total(null)).isEqualByComparingTo("36.15");

        // Exclui o mercado: outubro fica vazio
        mockMvc.perform(delete("/api/despesas/" + mercado)).andExpect(status().isNoContent());

        assertThat(descricoes("2026-10")).isEmpty();
        assertThat(total("2026-10")).isEqualByComparingTo("0.00");
        assertThat(descricoes(null)).containsExactly("Farmacia", "Aluguel");
        assertThat(total(null)).isEqualByComparingTo("26.05");
    }

    @Test
    void erroDeValidacaoNaoAlteraListaNemTotal() throws Exception {
        cadastrar("Mercado", "10.10", "2026-10-10");

        enviarPost(json("", "5.00", "2026-10-11")).andExpect(status().isBadRequest());
        enviarPost(json("Padaria", "12.345", "2026-10-11")).andExpect(status().isBadRequest());
        enviarPost("{\"descricao\":\"Padaria\",\"valor\":5,\"data\":\"11/10/2026\"}").andExpect(status().isBadRequest());
        enviarPost("{\"descricao\":\"Padaria\",\"data\":\"2026-10-11\"}").andExpect(status().isBadRequest());

        assertThat(descricoes(null)).containsExactly("Mercado");
        assertThat(total(null)).isEqualByComparingTo("10.10");
    }

    @Test
    void idInexistenteNaoAlteraNada() throws Exception {
        cadastrar("Mercado", "10.10", "2026-10-10");

        mockMvc.perform(put("/api/despesas/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Outro", "99.00", "2026-10-12")))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/despesas/999")).andExpect(status().isNotFound());

        assertThat(descricoes(null)).containsExactly("Mercado");
        assertThat(total(null)).isEqualByComparingTo("10.10");
    }

    @Test
    void edicaoInvalidaNaoAlteraADespesaExistente() throws Exception {
        int id = cadastrar("Mercado", "10.10", "2026-10-10");

        mockMvc.perform(put("/api/despesas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Mercado", "0", "2026-10-10")))
                .andExpect(status().isBadRequest());

        assertThat(descricoes(null)).containsExactly("Mercado");
        assertThat(total(null)).isEqualByComparingTo("10.10");
    }

    @Test
    void filtroDoMesIncluiPrimeiroEUltimoDiaEExcluiOsDiasVizinhos() throws Exception {
        cadastrar("Anterior", "1.00", "2026-09-30");
        cadastrar("Primeiro", "2.00", "2026-10-01");
        cadastrar("Ultimo", "4.00", "2026-10-31");
        cadastrar("Seguinte", "8.00", "2026-11-01");

        assertThat(descricoes("2026-10")).containsExactly("Ultimo", "Primeiro");
        assertThat(total("2026-10")).isEqualByComparingTo("6.00");
    }

    @Test
    void somaDeCentavosEExataSemErroDePontoFlutuante() throws Exception {
        cadastrar("Um", "0.10", "2026-10-10");
        cadastrar("Dois", "0.20", "2026-10-10");

        String corpo = mockMvc.perform(get("/api/despesas/total?mes=2026-10")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(corpo).isEqualTo("{\"total\":0.30}");
    }

    private int cadastrar(String descricao, String valor, String data) throws Exception {
        String corpo = enviarPost(json(descricao, valor, data))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private org.springframework.test.web.servlet.ResultActions enviarPost(String corpo) throws Exception {
        return mockMvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private List<String> descricoes(String mes) throws Exception {
        String url = mes == null ? "/api/despesas" : "/api/despesas?mes=" + mes;
        String corpo = mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$[*].descricao");
    }

    private BigDecimal total(String mes) throws Exception {
        String url = mes == null ? "/api/despesas/total" : "/api/despesas/total?mes=" + mes;
        String corpo = mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new BigDecimal(JsonPath.read(corpo, "$.total").toString());
    }

    private String json(String descricao, String valor, String data) {
        return "{\"descricao\":\"" + descricao + "\",\"valor\":" + valor + ",\"data\":\"" + data + "\"}";
    }
}

package br.com.despesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import br.com.despesas.model.Despesa;
import br.com.despesas.repository.DespesaRepository;

/**
 * RT-11 / RNF-01: com 1.000 despesas em um mês, a listagem do mês responde em até 500 ms.
 * Usa o contexto completo (controller, service, repository e SQLite real em arquivo temporário),
 * sem tocar em ~/.despesas. A medição inclui a serialização JSON; é feito aquecimento e vale a mediana.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DesempenhoListagemTest {

    private static final long LIMITE_MS = 500;
    private static final int DESPESAS_NO_MES = 1000;
    private static final int DESPESAS_EM_OUTROS_MESES = 300;
    private static final int AQUECIMENTOS = 3;
    private static final int MEDICOES = 5;
    private static final String MES = "2026-10";

    private static final Path BANCO = criarBancoTemporario();

    private static Path criarBancoTemporario() {
        try {
            Path arquivo = Files.createTempFile("despesas-desempenho", ".db");
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

    @BeforeAll
    void popularBanco() {
        List<Despesa> despesas = new ArrayList<>();
        for (int i = 0; i < DESPESAS_NO_MES; i++) {
            despesas.add(despesa(i, LocalDate.of(2026, 10, 1 + i % 28)));
        }
        for (int i = 0; i < DESPESAS_EM_OUTROS_MESES; i++) {
            despesas.add(despesa(i, LocalDate.of(2026, 9 + i % 2 * 2, 1 + i % 28)));
        }
        repository.saveAll(despesas);
    }

    @Test
    void listagemDoMesComMilDespesasRespondeEmAte500ms() throws Exception {
        String corpo = "";
        for (int i = 0; i < AQUECIMENTOS; i++) {
            corpo = consultar("/api/despesas?mes=" + MES);
        }

        long mediana = medianaEmMs("/api/despesas?mes=" + MES);

        System.out.println("DESEMPENHO listagem do mês (1.000 despesas): mediana " + mediana + " ms");
        assertThat(mediana).isLessThanOrEqualTo(LIMITE_MS);
        assertThat(corpo).isNotEmpty();
        verificarListagem(consultar("/api/despesas?mes=" + MES));
    }

    @Test
    void totalDoMesComMilDespesasRespondeEmAte500ms() throws Exception {
        for (int i = 0; i < AQUECIMENTOS; i++) {
            consultar("/api/despesas/total?mes=" + MES);
        }

        long mediana = medianaEmMs("/api/despesas/total?mes=" + MES);

        System.out.println("DESEMPENHO total do mês (1.000 despesas): mediana " + mediana + " ms");
        assertThat(mediana).isLessThanOrEqualTo(LIMITE_MS);
        BigDecimal total = new BigDecimal(JsonPath.read(consultar("/api/despesas/total?mes=" + MES), "$.total").toString());
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(DESPESAS_NO_MES * 1050L, 2));
    }

    private void verificarListagem(String corpo) {
        List<String> datas = JsonPath.read(corpo, "$[*].data");
        List<Number> ids = JsonPath.read(corpo, "$[*].id");

        assertThat(datas).hasSize(DESPESAS_NO_MES).allMatch(data -> data.startsWith(MES));
        for (int i = 1; i < datas.size(); i++) {
            int comparacao = datas.get(i - 1).compareTo(datas.get(i));
            assertThat(comparacao).isGreaterThanOrEqualTo(0);
            if (comparacao == 0) {
                assertThat(ids.get(i - 1).longValue()).isGreaterThan(ids.get(i).longValue());
            }
        }
    }

    private long medianaEmMs(String url) throws Exception {
        List<Long> tempos = new ArrayList<>();
        for (int i = 0; i < MEDICOES; i++) {
            long inicio = System.nanoTime();
            consultar(url);
            tempos.add(Duration.ofNanos(System.nanoTime() - inicio).toMillis());
        }
        Collections.sort(tempos);
        return tempos.get(tempos.size() / 2);
    }

    private String consultar(String url) throws Exception {
        return mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private Despesa despesa(int i, LocalDate data) {
        Despesa despesa = new Despesa();
        despesa.setDescricao("Despesa " + i);
        despesa.setValorCentavos(1050L);
        despesa.setData(data);
        return despesa;
    }
}

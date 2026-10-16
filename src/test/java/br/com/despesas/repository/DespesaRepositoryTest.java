package br.com.despesas.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.com.despesas.model.Despesa;

/**
 * Roda contra um SQLite real em arquivo temporário, pois as datas ficam gravadas como texto
 * e a comparação por período precisa ser validada nesse banco.
 * Ajustes em relação ao padrão do @DataJpaTest:
 * - Replace.NONE evita trocar o DataSource por um banco embutido (não há H2 no projeto);
 * - spring.datasource.url é sobrescrita para um arquivo temporário, sem tocar em ~/.despesas.
 * Driver, dialeto e ddl-auto vêm do application.properties.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DespesaRepositoryTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 10, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 10, 31);

    private static final Path BANCO = criarBancoTemporario();

    private static Path criarBancoTemporario() {
        try {
            Path arquivo = Files.createTempFile("despesas-repository-test", ".db");
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
    private DespesaRepository repository;

    @Test
    void consultaPorPeriodoIncluiPrimeiroEUltimoDiaEExcluiDiaAnteriorESeguinte() {
        salvar("anterior", 100, LocalDate.of(2026, 9, 30));
        salvar("primeiro", 200, INICIO);
        salvar("meio", 300, LocalDate.of(2026, 10, 15));
        salvar("ultimo", 400, FIM);
        salvar("seguinte", 500, LocalDate.of(2026, 11, 1));

        List<Despesa> periodo = repository.findByDataBetweenOrderByDataDescIdDesc(INICIO, FIM);

        assertThat(periodo).extracting(Despesa::getDescricao).containsExactly("ultimo", "meio", "primeiro");
    }

    @Test
    void consultaPorPeriodoOrdenaPorDataDecrescenteEIdDecrescente() {
        salvar("a", 100, LocalDate.of(2026, 10, 10));
        salvar("b", 100, LocalDate.of(2026, 10, 20));
        salvar("c", 100, LocalDate.of(2026, 10, 20));
        salvar("d", 100, LocalDate.of(2026, 10, 5));

        List<Despesa> periodo = repository.findByDataBetweenOrderByDataDescIdDesc(INICIO, FIM);

        assertThat(periodo).extracting(Despesa::getDescricao).containsExactly("c", "b", "a", "d");
    }

    @Test
    void listagemGeralOrdenaPorDataDecrescenteEIdDecrescente() {
        salvar("antiga", 100, LocalDate.of(2026, 9, 1));
        salvar("mesmo-dia-1", 100, LocalDate.of(2026, 10, 20));
        salvar("mesmo-dia-2", 100, LocalDate.of(2026, 10, 20));

        List<Despesa> todas = repository.findAllByOrderByDataDescIdDesc();

        assertThat(todas).extracting(Despesa::getDescricao)
                .containsExactly("mesmo-dia-2", "mesmo-dia-1", "antiga");
    }

    @Test
    void somaPorPeriodoConsideraSoAsDespesasDoPeriodoEmCentavos() {
        salvar("anterior", 100, LocalDate.of(2026, 9, 30));
        salvar("primeiro", 200, INICIO);
        salvar("ultimo", 400, FIM);
        salvar("seguinte", 800, LocalDate.of(2026, 11, 1));

        assertThat(repository.somarValorCentavosPorPeriodo(INICIO, FIM)).isEqualTo(600L);
    }

    @Test
    void somaGeralConsideraTodasAsDespesasEmCentavos() {
        salvar("a", 100, LocalDate.of(2026, 9, 30));
        salvar("b", 200, INICIO);
        salvar("c", 800, LocalDate.of(2026, 11, 1));

        assertThat(repository.somarValorCentavos()).isEqualTo(1100L);
    }

    @Test
    void somasDevolvemZeroQuandoNaoHaDespesas() {
        assertThat(repository.somarValorCentavos()).isZero();
        assertThat(repository.somarValorCentavosPorPeriodo(INICIO, FIM)).isZero();
    }

    @Test
    void somaPorPeriodoDevolveZeroQuandoSoHaDespesasForaDoPeriodo() {
        salvar("fora", 500, LocalDate.of(2026, 9, 15));

        assertThat(repository.somarValorCentavosPorPeriodo(INICIO, FIM)).isZero();
    }

    private void salvar(String descricao, long centavos, LocalDate data) {
        Despesa despesa = new Despesa();
        despesa.setDescricao(descricao);
        despesa.setValorCentavos(centavos);
        despesa.setData(data);
        repository.saveAndFlush(despesa);
    }
}

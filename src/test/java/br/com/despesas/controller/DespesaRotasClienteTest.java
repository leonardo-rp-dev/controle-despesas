package br.com.despesas.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.despesas.dto.DespesaRequest;
import br.com.despesas.dto.DespesaResponse;
import br.com.despesas.dto.ErroResposta;
import br.com.despesas.dto.TotalResponse;
import br.com.despesas.model.Despesa;
import br.com.despesas.repository.DespesaRepository;

/**
 * Testa as rotas como um cliente HTTP real (servidor em porta aleatória), passando pelo controller,
 * pelo service e pelo tratamento de erros reais, com o DespesaRepository substituído por um mock.
 *
 * Isolamento:
 * - O repositório é um mock; cada teste define seus próprios stubs e o Spring redefine o mock
 *   depois de cada teste (comportamento padrão de @MockitoBean), então não há estado compartilhado
 *   nem dependência de ordem.
 * - Com o repositório mockado, o contexto ainda configura o DataSource e o JPA (o service usa
 *   @Transactional, que precisa de um gerenciador de transações). Por isso a URL do banco é
 *   sobrescrita para um arquivo temporário, em vez de excluir a autoconfiguração. Nenhuma consulta
 *   chega a esse arquivo e ~/.despesas nunca é tocado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DespesaRotasClienteTest {

    private static final Path BANCO_TEMPORARIO = criarBancoTemporario();

    private static Path criarBancoTemporario() {
        try {
            Path arquivo = Files.createTempFile("despesas-rotas-cliente", ".db");
            arquivo.toFile().deleteOnExit();
            return arquivo;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + BANCO_TEMPORARIO);
    }

    @Autowired
    private TestRestTemplate cliente;

    @MockitoBean
    private DespesaRepository repository;

    @Test
    void postValidoRetorna201ComLocationECorpoComId() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> {
            Despesa d = inv.getArgument(0);
            d.setId(5L);
            return d;
        });

        ResponseEntity<DespesaResponse> resposta = cliente.postForEntity("/api/despesas",
                new DespesaRequest("Mercado", new BigDecimal("12.34"), LocalDate.of(2026, 10, 10)),
                DespesaResponse.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        assertThat(resposta.getHeaders().getLocation()).hasToString("/api/despesas/5");
        assertThat(resposta.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(resposta.getBody().id()).isEqualTo(5L);
        assertThat(resposta.getBody().descricao()).isEqualTo("Mercado");
        assertThat(resposta.getBody().valor()).isEqualByComparingTo("12.34");
        assertThat(resposta.getBody().data()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    @Test
    void getListaRetorna200ComAsDespesasDoRepositorio() {
        when(repository.findAllByOrderByDataDescIdDesc()).thenReturn(List.of(
                despesa(2L, "Aluguel", 150000L, LocalDate.of(2026, 10, 15)),
                despesa(1L, "Mercado", 1234L, LocalDate.of(2026, 9, 1))));

        ResponseEntity<DespesaResponse[]> resposta = cliente.getForEntity("/api/despesas", DespesaResponse[].class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody()).extracting(DespesaResponse::descricao).containsExactly("Aluguel", "Mercado");
        assertThat(resposta.getBody()[0].valor()).isEqualByComparingTo("1500.00");
    }

    @Test
    void getListaComMesRetorna200UsandoOPeriodoDoMes() {
        when(repository.findByDataBetweenOrderByDataDescIdDesc(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(despesa(2L, "Aluguel", 150000L, LocalDate.of(2026, 10, 15))));

        ResponseEntity<DespesaResponse[]> resposta = cliente.getForEntity(
                "/api/despesas?mes=2026-10", DespesaResponse[].class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody()).extracting(DespesaResponse::id).containsExactly(2L);
        verify(repository, never()).findAllByOrderByDataDescIdDesc();
    }

    @Test
    void getTotalRetorna200ComASomaConvertidaParaReais() {
        when(repository.somarValorCentavos()).thenReturn(151244L);

        ResponseEntity<TotalResponse> resposta = cliente.getForEntity("/api/despesas/total", TotalResponse.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().total()).isEqualByComparingTo("1512.44");
    }

    @Test
    void putValidoRetorna200ComADespesaAtualizada() {
        when(repository.findById(3L)).thenReturn(Optional.of(despesa(3L, "Mercado", 1234L, LocalDate.of(2026, 10, 10))));
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<DespesaResponse> resposta = cliente.exchange("/api/despesas/3", HttpMethod.PUT,
                new HttpEntity<>(new DespesaRequest("Feira", new BigDecimal("99.90"), LocalDate.of(2026, 10, 12))),
                DespesaResponse.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().id()).isEqualTo(3L);
        assertThat(resposta.getBody().descricao()).isEqualTo("Feira");
        assertThat(resposta.getBody().valor()).isEqualByComparingTo("99.90");
        assertThat(resposta.getBody().data()).isEqualTo(LocalDate.of(2026, 10, 12));
    }

    @Test
    void deleteValidoRetorna204SemCorpo() {
        Despesa existente = despesa(3L, "Mercado", 1234L, LocalDate.of(2026, 10, 10));
        when(repository.findById(3L)).thenReturn(Optional.of(existente));

        ResponseEntity<Void> resposta = cliente.exchange("/api/despesas/3", HttpMethod.DELETE, null, Void.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
        assertThat(resposta.getBody()).isNull();
        verify(repository).delete(existente);
    }

    @Test
    void putComIdInexistenteRetorna404ESemSalvar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ErroResposta> resposta = cliente.exchange("/api/despesas/99", HttpMethod.PUT,
                new HttpEntity<>(new DespesaRequest("Feira", BigDecimal.ONE, LocalDate.of(2026, 10, 12))),
                ErroResposta.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().status()).isEqualTo(404);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Despesa não encontrada");
        assertThat(resposta.getBody().timestamp()).isNotBlank();
        verify(repository, never()).save(any(Despesa.class));
    }

    @Test
    void deleteComIdInexistenteRetorna404ESemRemover() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ErroResposta> resposta = cliente.exchange("/api/despesas/99", HttpMethod.DELETE, null,
                ErroResposta.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Despesa não encontrada");
        verify(repository, never()).delete(any(Despesa.class));
    }

    @Test
    void rotaInexistenteRetorna404NoFormatoErroResposta() {
        ResponseEntity<ErroResposta> resposta = cliente.getForEntity("/api/inexistente", ErroResposta.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().status()).isEqualTo(404);
        assertThat(resposta.getBody().mensagem()).isNotBlank();
        verifyNoInteractions(repository);
    }

    private Despesa despesa(Long id, String descricao, long centavos, LocalDate data) {
        Despesa despesa = new Despesa();
        despesa.setId(id);
        despesa.setDescricao(descricao);
        despesa.setValorCentavos(centavos);
        despesa.setData(data);
        return despesa;
    }
}

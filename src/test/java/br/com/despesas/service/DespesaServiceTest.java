package br.com.despesas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.despesas.dto.DespesaRequest;
import br.com.despesas.dto.DespesaResponse;
import br.com.despesas.dto.TotalResponse;
import br.com.despesas.exception.NaoEncontradoException;
import br.com.despesas.exception.ValidacaoException;
import br.com.despesas.model.Despesa;
import br.com.despesas.repository.DespesaRepository;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 15);

    @Mock
    private DespesaRepository repository;

    @InjectMocks
    private DespesaService service;

    @Test
    void cadastraDespesaValidaConvertendoReaisParaCentavos() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> {
            Despesa d = inv.getArgument(0);
            d.setId(7L);
            return d;
        });

        DespesaResponse resposta = service.cadastrar(
                new DespesaRequest("Mercado", new BigDecimal("12.34"), DATA));

        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(salva.capture());
        assertThat(salva.getValue().getValorCentavos()).isEqualTo(1234L);
        assertThat(salva.getValue().getDescricao()).isEqualTo("Mercado");
        assertThat(salva.getValue().getData()).isEqualTo(DATA);
        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.valor()).isEqualByComparingTo("12.34");
    }

    @Test
    void aceitaValorComZerosAMaisAposADuasCasas() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cadastrar(new DespesaRequest("Mercado", new BigDecimal("12.340"), DATA));

        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(salva.capture());
        assertThat(salva.getValue().getValorCentavos()).isEqualTo(1234L);
    }

    @Test
    void aceitaDescricaoComExatamente200Caracteres() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaResponse resposta = service.cadastrar(
                new DespesaRequest("a".repeat(200), BigDecimal.ONE, DATA));

        assertThat(resposta.descricao()).hasSize(200);
    }

    @Test
    void rejeitaDescricaoVazia() {
        assertRejeitada(new DespesaRequest("", BigDecimal.ONE, DATA), "Descrição é obrigatória");
    }

    @Test
    void rejeitaDescricaoSoComEspacos() {
        assertRejeitada(new DespesaRequest("   ", BigDecimal.ONE, DATA), "Descrição é obrigatória");
    }

    @Test
    void rejeitaDescricaoNula() {
        assertRejeitada(new DespesaRequest(null, BigDecimal.ONE, DATA), "Descrição é obrigatória");
    }

    @Test
    void rejeitaDescricaoCom201Caracteres() {
        assertRejeitada(new DespesaRequest("a".repeat(201), BigDecimal.ONE, DATA),
                "Descrição deve ter no máximo 200 caracteres");
    }

    @Test
    void rejeitaValorZero() {
        assertRejeitada(new DespesaRequest("Mercado", BigDecimal.ZERO, DATA),
                "Valor deve ser maior que zero");
    }

    @Test
    void rejeitaValorNegativo() {
        assertRejeitada(new DespesaRequest("Mercado", new BigDecimal("-1.00"), DATA),
                "Valor deve ser maior que zero");
    }

    @Test
    void rejeitaValorComTresCasasDecimais() {
        assertRejeitada(new DespesaRequest("Mercado", new BigDecimal("12.345"), DATA),
                "Valor deve ter no máximo duas casas decimais");
    }

    @Test
    void rejeitaValorNulo() {
        assertRejeitada(new DespesaRequest("Mercado", null, DATA), "Valor é obrigatório");
    }

    @Test
    void rejeitaValorAcimaDoLimiteDeCentavos() {
        assertRejeitada(new DespesaRequest("Mercado", new BigDecimal("999999999999999999"), DATA),
                "Valor acima do limite permitido");
    }

    @Test
    void rejeitaDataNula() {
        assertRejeitada(new DespesaRequest("Mercado", BigDecimal.ONE, null), "Data é obrigatória");
    }

    @Test
    void listaUsandoConsultaOrdenadaPorDataEConverteCentavosParaReais() {
        Despesa recente = despesa(2L, "Aluguel", 150000L, LocalDate.of(2026, 10, 15));
        Despesa antiga = despesa(1L, "Mercado", 1234L, LocalDate.of(2026, 9, 1));
        when(repository.findAllByOrderByDataDescIdDesc()).thenReturn(List.of(recente, antiga));

        List<DespesaResponse> lista = service.listar(null);

        assertThat(lista).extracting(DespesaResponse::id).containsExactly(2L, 1L);
        assertThat(lista.get(0).valor()).isEqualByComparingTo("1500.00");
        assertThat(lista.get(1).valor()).isEqualByComparingTo("12.34");
    }

    @Test
    void listaVaziaQuandoNaoHaDespesas() {
        when(repository.findAllByOrderByDataDescIdDesc()).thenReturn(List.of());

        assertThat(service.listar(null)).isEmpty();
    }

    @Test
    void atualizaDescricaoValorEDataDaDespesaExistente() {
        Despesa existente = despesa(3L, "Mercado", 1234L, LocalDate.of(2026, 10, 10));
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaResponse resposta = service.atualizar(3L,
                new DespesaRequest("  Feira  ", new BigDecimal("99.90"), DATA));

        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(salva.capture());
        assertThat(salva.getValue().getId()).isEqualTo(3L);
        assertThat(salva.getValue().getDescricao()).isEqualTo("Feira");
        assertThat(salva.getValue().getValorCentavos()).isEqualTo(9990L);
        assertThat(salva.getValue().getData()).isEqualTo(DATA);
        assertThat(resposta.id()).isEqualTo(3L);
        assertThat(resposta.valor()).isEqualByComparingTo("99.90");
    }

    @Test
    void atualizarComIdInexistenteLancaNaoEncontradoESemSalvar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, new DespesaRequest("Mercado", BigDecimal.ONE, DATA)))
                .isInstanceOf(NaoEncontradoException.class)
                .hasMessage("Despesa não encontrada");
        verify(repository, never()).save(any(Despesa.class));
    }

    @Test
    void atualizarComIdInexistenteEDadosInvalidosRetornaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, new DespesaRequest("", BigDecimal.ONE, DATA)))
                .isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void atualizarRejeitaDescricaoVazia() {
        assertAtualizacaoRejeitada(new DespesaRequest("", BigDecimal.ONE, DATA), "Descrição é obrigatória");
    }

    @Test
    void atualizarRejeitaValorComTresCasasDecimais() {
        assertAtualizacaoRejeitada(new DespesaRequest("Mercado", new BigDecimal("12.345"), DATA),
                "Valor deve ter no máximo duas casas decimais");
    }

    @Test
    void atualizarRejeitaDataNula() {
        assertAtualizacaoRejeitada(new DespesaRequest("Mercado", BigDecimal.ONE, null), "Data é obrigatória");
    }

    @Test
    void excluiDespesaExistente() {
        Despesa existente = despesa(3L, "Mercado", 1234L, DATA);
        when(repository.findById(3L)).thenReturn(Optional.of(existente));

        service.excluir(3L);

        verify(repository).delete(existente);
    }

    @Test
    void excluirComIdInexistenteLancaNaoEncontradoESemRemover() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(NaoEncontradoException.class)
                .hasMessage("Despesa não encontrada");
        verify(repository, never()).delete(any(Despesa.class));
    }

    @Test
    void listaComMesUsaConsultaPorPeriodoDoPrimeiroAoUltimoDia() {
        Despesa despesa = despesa(1L, "Mercado", 1234L, LocalDate.of(2026, 10, 15));
        when(repository.findByDataBetweenOrderByDataDescIdDesc(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(despesa));

        List<DespesaResponse> lista = service.listar("2026-10");

        assertThat(lista).extracting(DespesaResponse::id).containsExactly(1L);
        verify(repository, never()).findAllByOrderByDataDescIdDesc();
    }

    @Test
    void listaComMesDeFevereiroBissextoVaiAteODia29() {
        when(repository.findByDataBetweenOrderByDataDescIdDesc(LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29)))
                .thenReturn(List.of());

        assertThat(service.listar("2028-02")).isEmpty();

        verify(repository).findByDataBetweenOrderByDataDescIdDesc(LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void listaSemMesUsaConsultaDeTodasAsDespesas(String mes) {
        when(repository.findAllByOrderByDataDescIdDesc()).thenReturn(List.of());

        assertThat(service.listar(mes)).isEmpty();

        verify(repository, never()).findByDataBetweenOrderByDataDescIdDesc(any(), any());
    }

    @Test
    void totalComMesConverteSomaDeCentavosParaReais() {
        when(repository.somarValorCentavosPorPeriodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(1234L);

        TotalResponse total = service.total("2026-10");

        assertThat(total.total()).isEqualTo(new BigDecimal("12.34"));
        verify(repository, never()).somarValorCentavos();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void totalSemMesSomaTodasAsDespesas(String mes) {
        when(repository.somarValorCentavos()).thenReturn(150000L);

        TotalResponse total = service.total(mes);

        assertThat(total.total()).isEqualTo(new BigDecimal("1500.00"));
        verify(repository, never()).somarValorCentavosPorPeriodo(any(), any());
    }

    @Test
    void totalSemDespesasDevolveZeroComDuasCasas() {
        when(repository.somarValorCentavos()).thenReturn(0L);

        assertThat(service.total(null).total()).isEqualTo(new BigDecimal("0.00"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-13", "10/2026", "abc", "2026-1", "2026-10-15"})
    void listarRejeitaMesInvalido(String mes) {
        assertThatThrownBy(() -> service.listar(mes))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Mês inválido. Use o formato yyyy-MM");
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-13", "10/2026", "abc", "2026-1", "2026-10-15"})
    void totalRejeitaMesInvalido(String mes) {
        assertThatThrownBy(() -> service.total(mes))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("Mês inválido. Use o formato yyyy-MM");
        verifyNoInteractions(repository);
    }

    @Test
    void cadastraDescricaoSemEspacosNasPontas() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cadastrar(new DespesaRequest("  Mercado  ", BigDecimal.ONE, DATA));

        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(salva.capture());
        assertThat(salva.getValue().getDescricao()).isEqualTo("Mercado");
    }

    @Test
    void espacosNasPontasNaoContamParaOLimiteDe200Caracteres() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaResponse resposta = service.cadastrar(
                new DespesaRequest("  " + "a".repeat(200) + "  ", BigDecimal.ONE, DATA));

        assertThat(resposta.descricao()).hasSize(200);
    }

    @Test
    void rejeitaDescricaoComMaisDe200CaracteresMesmoAposRemoverEspacos() {
        assertRejeitada(new DespesaRequest("  " + "a".repeat(201) + "  ", BigDecimal.ONE, DATA),
                "Descrição deve ter no máximo 200 caracteres");
    }

    @Test
    void aceitaValorMinimoDeUmCentavo() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaResponse resposta = service.cadastrar(
                new DespesaRequest("Bala", new BigDecimal("0.01"), DATA));

        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(salva.capture());
        assertThat(salva.getValue().getValorCentavos()).isEqualTo(1L);
        assertThat(resposta.valor()).isEqualTo(new BigDecimal("0.01"));
    }

    @Test
    void rejeitaValorMenorQueUmCentavo() {
        assertRejeitada(new DespesaRequest("Mercado", new BigDecimal("0.001"), DATA),
                "Valor deve ter no máximo duas casas decimais");
    }

    @Test
    void converteValorInteiroSemCasasParaCentavos() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        DespesaResponse resposta = service.cadastrar(
                new DespesaRequest("Aluguel", new BigDecimal("1500"), DATA));

        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(salva.capture());
        assertThat(salva.getValue().getValorCentavos()).isEqualTo(150000L);
        assertThat(resposta.valor()).isEqualTo(new BigDecimal("1500.00"));
    }

    @Test
    void aceitaOMaiorValorQueCabeEmCentavos() {
        when(repository.save(any(Despesa.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cadastrar(new DespesaRequest("Limite", new BigDecimal("92233720368547758.07"), DATA));

        ArgumentCaptor<Despesa> salva = ArgumentCaptor.forClass(Despesa.class);
        verify(repository).save(salva.capture());
        assertThat(salva.getValue().getValorCentavos()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void rejeitaValorUmCentavoAcimaDoLimiteDeCentavos() {
        assertRejeitada(new DespesaRequest("Limite", new BigDecimal("92233720368547758.08"), DATA),
                "Valor acima do limite permitido");
    }

    @Test
    void rejeitaCorpoNuloNoCadastro() {
        assertRejeitada(null, "Corpo da requisição ausente");
    }

    @Test
    void atualizarRejeitaCorpoNulo() {
        assertAtualizacaoRejeitada(null, "Corpo da requisição ausente");
    }

    @Test
    void atualizarRejeitaDescricaoComMaisDe200Caracteres() {
        assertAtualizacaoRejeitada(new DespesaRequest("a".repeat(201), BigDecimal.ONE, DATA),
                "Descrição deve ter no máximo 200 caracteres");
    }

    @Test
    void atualizarRejeitaValorZero() {
        assertAtualizacaoRejeitada(new DespesaRequest("Mercado", BigDecimal.ZERO, DATA),
                "Valor deve ser maior que zero");
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01, 2026-01-31",
            "2026-04, 2026-04-30",
            "2026-12, 2026-12-31",
            "2027-02, 2027-02-28"
    })
    void listaComMesUsaOUltimoDiaCorretoDoMes(String mes, LocalDate ultimoDia) {
        LocalDate primeiroDia = ultimoDia.withDayOfMonth(1);
        when(repository.findByDataBetweenOrderByDataDescIdDesc(primeiroDia, ultimoDia)).thenReturn(List.of());

        assertThat(service.listar(mes)).isEmpty();

        verify(repository).findByDataBetweenOrderByDataDescIdDesc(primeiroDia, ultimoDia);
    }

    @Test
    void listaComMesIgnoraEspacosNasPontas() {
        when(repository.findByDataBetweenOrderByDataDescIdDesc(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of());

        assertThat(service.listar(" 2026-10 ")).isEmpty();

        verify(repository, never()).findAllByOrderByDataDescIdDesc();
    }

    @Test
    void totalComMesIgnoraEspacosNasPontas() {
        when(repository.somarValorCentavosPorPeriodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(500L);

        assertThat(service.total(" 2026-10 ").total()).isEqualTo(new BigDecimal("5.00"));
    }

    @Test
    void totalDeTrintaCentavosEExatoSemErroDePontoFlutuante() {
        when(repository.somarValorCentavos()).thenReturn(30L);

        assertThat(service.total(null).total()).isEqualTo(new BigDecimal("0.30"));
    }

    @Test
    void totalDeUmCentavoDevolveZeroVirgulaZeroUm() {
        when(repository.somarValorCentavos()).thenReturn(1L);

        assertThat(service.total(null).total()).isEqualTo(new BigDecimal("0.01"));
    }

    private void assertAtualizacaoRejeitada(DespesaRequest request, String mensagem) {
        Despesa existente = despesa(3L, "Mercado", 1234L, DATA);
        when(repository.findById(3L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizar(3L, request))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage(mensagem);
        verify(repository, never()).save(any(Despesa.class));
        assertThat(existente.getDescricao()).isEqualTo("Mercado");
        assertThat(existente.getValorCentavos()).isEqualTo(1234L);
    }

    private void assertRejeitada(DespesaRequest request, String mensagem) {
        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage(mensagem);
        verifyNoInteractions(repository);
    }

    private Despesa despesa(Long id, String descricao, long centavos, LocalDate data) {
        Despesa d = new Despesa();
        d.setId(id);
        d.setDescricao(descricao);
        d.setValorCentavos(centavos);
        d.setData(data);
        return d;
    }
}

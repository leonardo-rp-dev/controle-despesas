package br.com.despesas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.despesas.dto.DespesaRequest;
import br.com.despesas.dto.DespesaResponse;
import br.com.despesas.exception.NaoEncontradoException;
import br.com.despesas.exception.ValidacaoException;
import br.com.despesas.service.DespesaService;

@WebMvcTest(DespesaController.class)
class DespesaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DespesaService service;

    @Test
    void postValidoRetorna201ComLocationECorpo() throws Exception {
        when(service.cadastrar(any(DespesaRequest.class))).thenReturn(
                new DespesaResponse(5L, "Mercado", new BigDecimal("12.34"), LocalDate.of(2026, 10, 15)));

        mockMvc.perform(post("/api/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Mercado\",\"valor\":12.34,\"data\":\"2026-10-15\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/despesas/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.descricao").value("Mercado"))
                .andExpect(jsonPath("$.valor").value(12.34))
                .andExpect(jsonPath("$.data").value("2026-10-15"));
    }

    @Test
    void postComDescricaoInvalidaRetorna400NoFormatoErroResposta() throws Exception {
        when(service.cadastrar(any(DespesaRequest.class)))
                .thenThrow(new ValidacaoException("Descrição é obrigatória"));

        mockMvc.perform(post("/api/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"\",\"valor\":12.34,\"data\":\"2026-10-15\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Descrição é obrigatória"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void postComDataForaDoFormatoIsoRetorna400SemChamarOService() throws Exception {
        mockMvc.perform(post("/api/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Mercado\",\"valor\":12.34,\"data\":\"15/10/2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Data inválida. Use o formato yyyy-MM-dd"));

        verifyNoInteractions(service);
    }

    @Test
    void postComValorNaoNumericoRetorna400() throws Exception {
        mockMvc.perform(post("/api/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Mercado\",\"valor\":\"abc\",\"data\":\"2026-10-15\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Valor inválido. Informe um número, por exemplo 12.34"));

        verifyNoInteractions(service);
    }

    @Test
    void postSemCorpoRetorna400() throws Exception {
        mockMvc.perform(post("/api/despesas").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void errosInesperadosRetornam500SemExporDetalhes() throws Exception {
        when(service.listar(null)).thenThrow(new IllegalStateException("detalhe interno"));

        mockMvc.perform(get("/api/despesas"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem").value("Erro interno do servidor"));
    }

    @Test
    void getRetorna200ComAListaNaOrdemDoService() throws Exception {
        when(service.listar(null)).thenReturn(List.of(
                new DespesaResponse(2L, "Aluguel", new BigDecimal("1500.00"), LocalDate.of(2026, 10, 15)),
                new DespesaResponse(1L, "Mercado", new BigDecimal("12.34"), LocalDate.of(2026, 9, 1))));

        mockMvc.perform(get("/api/despesas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].descricao").value("Aluguel"))
                .andExpect(jsonPath("$[1].descricao").value("Mercado"));
    }

    @Test
    void putValidoRetorna200ComCorpoAtualizado() throws Exception {
        when(service.atualizar(eq(5L), any(DespesaRequest.class))).thenReturn(
                new DespesaResponse(5L, "Feira", new BigDecimal("99.90"), LocalDate.of(2026, 10, 12)));

        mockMvc.perform(put("/api/despesas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Feira\",\"valor\":99.90,\"data\":\"2026-10-12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.descricao").value("Feira"))
                .andExpect(jsonPath("$.valor").value(99.90))
                .andExpect(jsonPath("$.data").value("2026-10-12"));
    }

    @Test
    void putComDadoInvalidoRetorna400NoFormatoErroResposta() throws Exception {
        when(service.atualizar(eq(5L), any(DespesaRequest.class)))
                .thenThrow(new ValidacaoException("Descrição é obrigatória"));

        mockMvc.perform(put("/api/despesas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"\",\"valor\":1,\"data\":\"2026-10-12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Descrição é obrigatória"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void putComIdInexistenteRetorna404NoFormatoErroResposta() throws Exception {
        when(service.atualizar(eq(99L), any(DespesaRequest.class)))
                .thenThrow(new NaoEncontradoException("Despesa não encontrada"));

        mockMvc.perform(put("/api/despesas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Feira\",\"valor\":1,\"data\":\"2026-10-12\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Despesa não encontrada"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void deleteValidoRetorna204SemCorpo() throws Exception {
        mockMvc.perform(delete("/api/despesas/5"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).excluir(5L);
    }

    @Test
    void deleteComIdInexistenteRetorna404NoFormatoErroResposta() throws Exception {
        doThrow(new NaoEncontradoException("Despesa não encontrada")).when(service).excluir(99L);

        mockMvc.perform(delete("/api/despesas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Despesa não encontrada"));
    }

    @Test
    void idNaoNumericoNaRotaRetorna400SemChamarOService() throws Exception {
        mockMvc.perform(delete("/api/despesas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Identificador inválido"));

        verifyNoInteractions(service);
    }

    @Test
    void getRetorna200ComListaVaziaQuandoNaoHaDespesas() throws Exception {
        when(service.listar(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/despesas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

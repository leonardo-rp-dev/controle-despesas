package br.com.despesas.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import br.com.despesas.dto.ErroResposta;

/**
 * Testa o handler diretamente, com exceções construídas à mão, para os ramos
 * que as rotas atuais não conseguem provocar.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void erroDoSpringMvcComStatus5xxMantemOStatusEOcultaOsDetalhes() {
        ResponseEntity<ErroResposta> resposta = handler.tratarGenerica(
                new ErrorResponseException(HttpStatus.BAD_GATEWAY));

        assertThat(resposta.getStatusCode().value()).isEqualTo(502);
        assertThat(resposta.getBody().status()).isEqualTo(502);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Erro interno do servidor");
    }

    @Test
    void erroDoSpringMvcComStatus4xxMantemOStatusComMensagemGenerica() {
        ResponseEntity<ErroResposta> resposta = handler.tratarGenerica(
                new ErrorResponseException(HttpStatus.NOT_ACCEPTABLE));

        assertThat(resposta.getStatusCode().value()).isEqualTo(406);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Requisição inválida");
    }

    @Test
    void excecaoInesperadaRetorna500ComMensagemGenericaSemExporODetalhe() {
        ResponseEntity<ErroResposta> resposta = handler.tratarGenerica(new IllegalStateException("detalhe interno"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(500);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Erro interno do servidor");
    }

    @Test
    void formatoInvalidoDeOutroTipoRetornaMensagemGenericaDeCorpoInvalido() {
        InvalidFormatException causa = InvalidFormatException.from(null, "formato inválido", "abc", Integer.class);
        HttpMessageNotReadableException excecao = new HttpMessageNotReadableException(
                "corpo ilegível", causa, Mockito.mock(HttpInputMessage.class));

        ResponseEntity<ErroResposta> resposta = handler.tratarCorpoIlegivel(excecao);

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Corpo da requisição inválido ou ausente");
    }
}

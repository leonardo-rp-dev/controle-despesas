package br.com.despesas.exception;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import br.com.despesas.dto.ErroResposta;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(ValidacaoException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoIlegivel(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, mensagemDoCorpo(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarGenerica(Exception e) {
        // Erros do próprio Spring MVC (rota inexistente, método não permitido) mantêm seu status
        if (e instanceof ErrorResponse erro) {
            HttpStatusCode status = erro.getStatusCode();
            return resposta(status, status.is4xxClientError()
                    ? "Requisição inválida"
                    : "Erro interno do servidor");
        }
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private String mensagemDoCorpo(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException formato) {
            if (formato.getTargetType() == LocalDate.class) {
                return "Data inválida. Use o formato yyyy-MM-dd";
            }
            if (formato.getTargetType() == BigDecimal.class) {
                return "Valor inválido. Informe um número, por exemplo 12.34";
            }
        }
        return "Corpo da requisição inválido ou ausente";
    }

    private ResponseEntity<ErroResposta> resposta(HttpStatusCode status, String mensagem) {
        return ResponseEntity.status(status).body(ErroResposta.de(status.value(), mensagem));
    }
}

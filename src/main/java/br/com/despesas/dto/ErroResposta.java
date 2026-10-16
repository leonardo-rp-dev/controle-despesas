package br.com.despesas.dto;

import java.time.OffsetDateTime;

public record ErroResposta(int status, String mensagem, String timestamp) {

    public static ErroResposta de(int status, String mensagem) {
        return new ErroResposta(status, mensagem, OffsetDateTime.now().toString());
    }
}

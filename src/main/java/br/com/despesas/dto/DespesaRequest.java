package br.com.despesas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaRequest(String descricao, BigDecimal valor, LocalDate data) {
}

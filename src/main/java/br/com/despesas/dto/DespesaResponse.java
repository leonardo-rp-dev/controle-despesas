package br.com.despesas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaResponse(Long id, String descricao, BigDecimal valor, LocalDate data) {
}

package br.com.despesas.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import br.com.despesas.exception.ValidacaoException;

/**
 * Período de um mês, do primeiro ao último dia (ambos inclusivos).
 */
record PeriodoMes(LocalDate inicio, LocalDate fim) {

    /**
     * Lê um mês no formato yyyy-MM. Devolve vazio quando o mês é nulo ou em branco,
     * e lança ValidacaoException quando o texto não é um mês válido.
     */
    static Optional<PeriodoMes> de(String mes) {
        if (mes == null || mes.isBlank()) {
            return Optional.empty();
        }
        try {
            YearMonth anoMes = YearMonth.parse(mes.trim());
            return Optional.of(new PeriodoMes(anoMes.atDay(1), anoMes.atEndOfMonth()));
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Mês inválido. Use o formato yyyy-MM");
        }
    }
}

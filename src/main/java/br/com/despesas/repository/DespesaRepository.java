package br.com.despesas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.despesas.model.Despesa;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    List<Despesa> findAllByOrderByDataDescIdDesc();

    List<Despesa> findByDataBetweenOrderByDataDescIdDesc(LocalDate inicio, LocalDate fim);

    @Query("select coalesce(sum(d.valorCentavos), 0L) from Despesa d")
    long somarValorCentavos();

    @Query("select coalesce(sum(d.valorCentavos), 0L) from Despesa d where d.data between :inicio and :fim")
    long somarValorCentavosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}

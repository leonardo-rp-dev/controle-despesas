package br.com.despesas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.despesas.model.Despesa;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    List<Despesa> findAllByOrderByDataDescIdDesc();
}

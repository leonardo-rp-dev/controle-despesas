package br.com.despesas.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.despesas.dto.DespesaRequest;
import br.com.despesas.dto.DespesaResponse;
import br.com.despesas.dto.TotalResponse;
import br.com.despesas.service.DespesaService;

@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    private final DespesaService service;

    public DespesaController(DespesaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DespesaResponse> cadastrar(@RequestBody DespesaRequest request) {
        DespesaResponse criada = service.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/despesas/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<DespesaResponse> listar(@RequestParam(required = false) String mes) {
        return service.listar(mes);
    }

    @GetMapping("/total")
    public TotalResponse total(@RequestParam(required = false) String mes) {
        return service.total(mes);
    }

    @PutMapping("/{id}")
    public DespesaResponse atualizar(@PathVariable Long id, @RequestBody DespesaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

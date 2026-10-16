package br.com.despesas.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.despesas.dto.DespesaRequest;
import br.com.despesas.dto.DespesaResponse;
import br.com.despesas.exception.NaoEncontradoException;
import br.com.despesas.exception.ValidacaoException;
import br.com.despesas.model.Despesa;
import br.com.despesas.repository.DespesaRepository;

@Service
public class DespesaService {

    private static final int TAMANHO_MAXIMO_DESCRICAO = 200;
    private static final int CASAS_DECIMAIS = 2;

    private final DespesaRepository repository;

    public DespesaService(DespesaRepository repository) {
        this.repository = repository;
    }

    public DespesaResponse cadastrar(DespesaRequest request) {
        validar(request);

        Despesa despesa = new Despesa();
        preencher(despesa, request);

        return paraResponse(repository.save(despesa));
    }

    @Transactional
    public DespesaResponse atualizar(Long id, DespesaRequest request) {
        Despesa despesa = buscar(id);
        validar(request);
        preencher(despesa, request);

        return paraResponse(repository.save(despesa));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    public List<DespesaResponse> listar() {
        return repository.findAllByOrderByDataDescIdDesc().stream()
                .map(this::paraResponse)
                .toList();
    }

    private Despesa buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Despesa não encontrada"));
    }

    private void preencher(Despesa despesa, DespesaRequest request) {
        despesa.setDescricao(request.descricao().trim());
        despesa.setValorCentavos(paraCentavos(request.valor()));
        despesa.setData(request.data());
    }

    private void validar(DespesaRequest request) {
        if (request == null) {
            throw new ValidacaoException("Corpo da requisição ausente");
        }
        validarDescricao(request.descricao());
        validarValor(request.valor());
        if (request.data() == null) {
            throw new ValidacaoException("Data é obrigatória");
        }
    }

    private void validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new ValidacaoException("Descrição é obrigatória");
        }
        if (descricao.trim().length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new ValidacaoException(
                    "Descrição deve ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres");
        }
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new ValidacaoException("Valor é obrigatório");
        }
        if (valor.signum() <= 0) {
            throw new ValidacaoException("Valor deve ser maior que zero");
        }
        if (valor.stripTrailingZeros().scale() > CASAS_DECIMAIS) {
            throw new ValidacaoException("Valor deve ter no máximo duas casas decimais");
        }
    }

    private long paraCentavos(BigDecimal valor) {
        try {
            return valor.movePointRight(CASAS_DECIMAIS).longValueExact();
        } catch (ArithmeticException e) {
            throw new ValidacaoException("Valor acima do limite permitido");
        }
    }

    private BigDecimal paraReais(long centavos) {
        return BigDecimal.valueOf(centavos, CASAS_DECIMAIS);
    }

    private DespesaResponse paraResponse(Despesa despesa) {
        return new DespesaResponse(
                despesa.getId(),
                despesa.getDescricao(),
                paraReais(despesa.getValorCentavos()),
                despesa.getData());
    }
}

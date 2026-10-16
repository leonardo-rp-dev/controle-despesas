const formatadorMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

function hojeIso() {
    const agora = new Date();
    const mes = String(agora.getMonth() + 1).padStart(2, '0');
    const dia = String(agora.getDate()).padStart(2, '0');
    return agora.getFullYear() + '-' + mes + '-' + dia;
}

function formularioVazio() {
    return { descricao: '', valor: '', data: hojeIso() };
}

function despesas() {
    return {
        lista: [],
        total: null,
        mes: '',
        form: formularioVazio(),
        editandoId: null,
        excluindoId: null,
        erro: '',
        erroLista: '',
        carregando: false,
        salvando: false,
        requisicao: 0,

        // Lista e total vêm sempre do servidor, com o mesmo filtro de mês
        async carregar() {
            const atual = ++this.requisicao;
            const consulta = this.mes ? '?mes=' + encodeURIComponent(this.mes) : '';
            this.carregando = true;
            this.erroLista = '';
            try {
                const [resLista, resTotal] = await Promise.all([
                    fetch('/api/despesas' + consulta),
                    fetch('/api/despesas/total' + consulta)
                ]);
                if (!resLista.ok) {
                    throw await this.erroDaResposta(resLista);
                }
                if (!resTotal.ok) {
                    throw await this.erroDaResposta(resTotal);
                }
                const lista = await resLista.json();
                const total = (await resTotal.json()).total;
                if (atual !== this.requisicao) {
                    return;
                }
                this.lista = lista;
                this.total = total;
            } catch (e) {
                if (atual !== this.requisicao) {
                    return;
                }
                this.erroLista = this.mensagemDaFalha(e);
                if (e.status === 400) {
                    // Filtro rejeitado: não mostra dados de outro filtro
                    this.lista = [];
                    this.total = null;
                }
            } finally {
                if (atual === this.requisicao) {
                    this.carregando = false;
                }
            }
        },

        limparFiltro() {
            this.mes = '';
            return this.carregar();
        },

        async salvar() {
            const editando = this.editandoId !== null;
            this.salvando = true;
            this.erro = '';
            try {
                const res = await fetch(editando ? '/api/despesas/' + this.editandoId : '/api/despesas', {
                    method: editando ? 'PUT' : 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.montarCorpo())
                });
                if (!res.ok) {
                    throw await this.erroDaResposta(res);
                }
                if (editando) {
                    this.cancelar();
                } else {
                    this.form.descricao = '';
                    this.form.valor = '';
                }
                await this.carregar();
            } catch (e) {
                this.erro = this.mensagemDaFalha(e);
                if (editando && e.status === 404) {
                    // O item já não existe: sai do modo edição, mantém o que foi digitado e atualiza a lista
                    this.editandoId = null;
                    await this.carregar();
                }
            } finally {
                this.salvando = false;
            }
        },

        editar(despesa) {
            this.editandoId = despesa.id;
            this.form = {
                descricao: despesa.descricao,
                valor: despesa.valor.toFixed(2).replace('.', ','),
                data: despesa.data
            };
            this.erro = '';
            window.scrollTo({ top: 0, behavior: 'smooth' });
        },

        cancelar() {
            this.editandoId = null;
            this.form = formularioVazio();
            this.erro = '';
        },

        async excluir(despesa) {
            if (!confirm('Excluir a despesa "' + despesa.descricao + '"?')) {
                return;
            }
            this.excluindoId = despesa.id;
            this.erroLista = '';
            try {
                const res = await fetch('/api/despesas/' + despesa.id, { method: 'DELETE' });
                if (!res.ok) {
                    throw await this.erroDaResposta(res);
                }
                if (this.editandoId === despesa.id) {
                    this.cancelar();
                }
                await this.carregar();
            } catch (e) {
                const mensagem = this.mensagemDaFalha(e);
                if (e.status === 404) {
                    await this.carregar();
                }
                this.erroLista = mensagem;
            } finally {
                this.excluindoId = null;
            }
        },

        // Vírgula vira ponto; texto não numérico segue como veio para a API rejeitar
        montarCorpo() {
            const texto = this.form.valor.trim().replace(',', '.');
            let valor = texto === '' ? null : texto;
            if (/^\d+(\.\d+)?$/.test(texto)) {
                valor = Number(texto);
            }
            return {
                descricao: this.form.descricao,
                valor: valor,
                data: this.form.data || null
            };
        },

        // Erro com a mensagem da API e o status HTTP, para tratar casos como o 404
        async erroDaResposta(res) {
            let mensagem = 'Erro ao processar a requisição (HTTP ' + res.status + ')';
            try {
                const corpo = await res.json();
                if (corpo.mensagem) {
                    mensagem = corpo.mensagem;
                }
            } catch (e) {
                // resposta sem JSON: mantém a mensagem padrão
            }
            const erro = new Error(mensagem);
            erro.status = res.status;
            return erro;
        },

        mensagemDaFalha(e) {
            return e instanceof TypeError ? 'Não foi possível conectar ao servidor' : e.message;
        },

        rotuloTotal() {
            return this.mes ? 'Total de ' + this.formatarMes(this.mes) : 'Total de todas as despesas';
        },

        formatarValor(valor) {
            return formatadorMoeda.format(valor);
        },

        formatarData(iso) {
            const [ano, mes, dia] = iso.split('-');
            return dia + '/' + mes + '/' + ano;
        },

        formatarMes(iso) {
            const partes = iso.split('-');
            return partes.length === 2 ? partes[1] + '/' + partes[0] : iso;
        }
    };
}

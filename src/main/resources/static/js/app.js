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
        form: formularioVazio(),
        editandoId: null,
        excluindoId: null,
        erro: '',
        erroLista: '',
        carregando: false,
        salvando: false,

        async carregar() {
            this.carregando = true;
            this.erroLista = '';
            try {
                const res = await fetch('/api/despesas');
                if (!res.ok) {
                    throw await this.erroDaResposta(res);
                }
                this.lista = await res.json();
            } catch (e) {
                this.erroLista = this.mensagemDaFalha(e);
            } finally {
                this.carregando = false;
            }
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
                const despesa = await res.json();
                if (editando) {
                    this.lista = this.lista.filter(d => d.id !== despesa.id);
                }
                this.inserir(despesa);
                if (editando) {
                    this.cancelar();
                } else {
                    this.form.descricao = '';
                    this.form.valor = '';
                }
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
                this.lista = this.lista.filter(d => d.id !== despesa.id);
                if (this.editandoId === despesa.id) {
                    this.cancelar();
                }
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

        inserir(despesa) {
            this.lista.push(despesa);
            this.lista.sort((a, b) => b.data.localeCompare(a.data) || b.id - a.id);
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

        formatarValor(valor) {
            return formatadorMoeda.format(valor);
        },

        formatarData(iso) {
            const [ano, mes, dia] = iso.split('-');
            return dia + '/' + mes + '/' + ano;
        }
    };
}

const formatadorMoeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

function hojeIso() {
    const agora = new Date();
    const mes = String(agora.getMonth() + 1).padStart(2, '0');
    const dia = String(agora.getDate()).padStart(2, '0');
    return agora.getFullYear() + '-' + mes + '-' + dia;
}

function despesas() {
    return {
        lista: [],
        form: { descricao: '', valor: '', data: hojeIso() },
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
                    throw new Error(await this.mensagemDeErro(res));
                }
                this.lista = await res.json();
            } catch (e) {
                this.erroLista = this.mensagemDaFalha(e);
            } finally {
                this.carregando = false;
            }
        },

        async salvar() {
            this.salvando = true;
            this.erro = '';
            try {
                const res = await fetch('/api/despesas', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.montarCorpo())
                });
                if (!res.ok) {
                    throw new Error(await this.mensagemDeErro(res));
                }
                this.inserir(await res.json());
                this.form.descricao = '';
                this.form.valor = '';
            } catch (e) {
                this.erro = this.mensagemDaFalha(e);
            } finally {
                this.salvando = false;
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

        async mensagemDeErro(res) {
            try {
                const corpo = await res.json();
                if (corpo.mensagem) {
                    return corpo.mensagem;
                }
            } catch (e) {
                // resposta sem JSON: usa a mensagem padrão abaixo
            }
            return 'Erro ao processar a requisição (HTTP ' + res.status + ')';
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

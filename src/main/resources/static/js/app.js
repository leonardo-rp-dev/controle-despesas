function healthCheck() {
    return {
        resposta: '',
        erro: '',
        carregando: false,

        async verificar() {
            this.carregando = true;
            this.erro = '';
            this.resposta = '';
            try {
                const res = await fetch('/health');
                if (!res.ok) {
                    throw new Error('HTTP ' + res.status);
                }
                this.resposta = JSON.stringify(await res.json(), null, 2);
            } catch (e) {
                this.erro = 'Falha ao consultar /health: ' + e.message;
            } finally {
                this.carregando = false;
            }
        }
    };
}

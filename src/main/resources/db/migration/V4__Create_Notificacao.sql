CREATE TABLE notificacao (
    id_notificacao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL,
    mensagem TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT current_timestamp,
    lida BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_notificacao_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

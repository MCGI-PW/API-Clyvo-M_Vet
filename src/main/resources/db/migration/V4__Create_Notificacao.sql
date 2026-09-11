-- =============================================================================
-- V4: Criação da Tabela Notificação (Compatível com Oracle e H2)
-- =============================================================================

CREATE TABLE notificacao (
    id_notificacao RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    mensagem VARCHAR2(4000) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    lida NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT fk_notificacao_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

-- =============================================================================
-- V4: Criação da Tabela Notificação (T-SQL - Azure SQL / SQL Server)
-- =============================================================================

CREATE TABLE notificacao (
    id_notificacao UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    mensagem NVARCHAR(4000) NOT NULL,
    data_criacao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    lida BIT NOT NULL DEFAULT 0,
    CONSTRAINT pk_notificacao PRIMARY KEY (id_notificacao),
    CONSTRAINT fk_notificacao_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);
GO

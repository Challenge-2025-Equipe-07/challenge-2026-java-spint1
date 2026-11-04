--------------------------------------------------------------------------------
-- CHALLENGE FIAP 2026 - CLYVO VET (aplicacao Veti)
-- Disciplina: Mastering Relational and Non-Relational Database - Entrega Sprint 4
-- Turma: 2TDSPW
--
-- Script unico de entrega: executar inteiro no SQL Developer como script (F5).
-- Funciona no Oracle 19c (servidor da FIAP) e no Oracle 23ai (container usado pela API Java).
--
--   BLOCO 0: limpeza (permite rodar do zero quantas vezes for preciso)
--   BLOCO 1: sequences e tabelas - modelo fisico da API Java (mesmos nomes das entidades JPA)
--   BLOCO 2: comentarios do dicionario de dados
--   BLOCO 3: carga de dados (minimo de 5 registros por tabela)
--   BLOCO 4: TB_AUDITORIA, package PKG_VETI e trigger TRG_AUDITORIA_CONSULTA
--            (copia de src/main/resources/db/pkg_veti.sql, o mesmo arquivo que a API aplica na subida)
--   BLOCO 5: testes e execucoes para os prints
--
-- Este arquivo e gerado por db/gerar_script_entrega.sh a partir de db/partes e do pkg_veti.sql.
-- ATENCAO: o BLOCO 0 apaga as tabelas do Veti no schema conectado. Nao rode no banco de producao.
--------------------------------------------------------------------------------

SET SERVEROUTPUT ON SIZE UNLIMITED;

--------------------------------------------------------------------------------
-- BLOCO 0: LIMPEZA
--------------------------------------------------------------------------------
BEGIN
    FOR o IN (SELECT object_name, object_type
              FROM   user_objects
              WHERE  object_type IN ('TRIGGER', 'PACKAGE')
              AND    object_name IN ('TRG_AUDITORIA_CONSULTA', 'PKG_VETI')) LOOP
        EXECUTE IMMEDIATE 'DROP ' || o.object_type || ' ' || o.object_name;
    END LOOP;

    FOR t IN (SELECT table_name
              FROM   user_tables
              WHERE  table_name IN ('TB_AUDITORIA', 'TB_RECEITA_MEDICAMENTO', 'TB_EXAME_MEDICAMENTO',
                                    'TB_RECEITA', 'TB_EXAME', 'TB_CONSULTA', 'TB_PET',
                                    'TB_MEDICAMENTO', 'TB_VETERINARIO', 'TB_TUTOR')) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
    END LOOP;

    FOR s IN (SELECT sequence_name
              FROM   user_sequences
              WHERE  sequence_name IN ('SQ_TUTOR', 'SQ_VETERINARIO', 'SQ_MEDICAMENTO', 'SQ_PET', 'SQ_CONSULTA',
                                       'SQ_EXAME', 'SQ_EXAME_MEDICAMENTO', 'SQ_RECEITA', 'SQ_RECEITA_MEDICAMENTO')) LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
    END LOOP;
END;
/

--------------------------------------------------------------------------------
-- BLOCO 1: SEQUENCES E TABELAS
-- Mesmo modelo que o Hibernate cria a partir das entidades da API (docs/ddl-oracle.sql), com duas
-- adaptacoes para rodar tambem no 19c: FL_CASTRADO e NUMBER(1) com CHECK (o tipo BOOLEAN so existe
-- no 23ai) e as restricoes de dominio das entidades (@Min, @Max) viraram CHECK no banco.
--------------------------------------------------------------------------------
CREATE SEQUENCE SQ_TUTOR START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_VETERINARIO START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_MEDICAMENTO START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_PET START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_CONSULTA START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_EXAME START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_EXAME_MEDICAMENTO START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_RECEITA START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SQ_RECEITA_MEDICAMENTO START WITH 1 INCREMENT BY 1;

-- Tabelas sem dependencia
CREATE TABLE TB_TUTOR (
    ID_TUTOR    NUMBER(19)         NOT NULL,
    NM_TUTOR    VARCHAR2(300 CHAR) NOT NULL,
    DS_CPF      VARCHAR2(11 CHAR)  NOT NULL,
    DS_EMAIL    VARCHAR2(320 CHAR) NOT NULL,
    DS_TELEFONE VARCHAR2(13 CHAR),
    CONSTRAINT TB_TUTOR_PK PRIMARY KEY (ID_TUTOR),
    CONSTRAINT TB_TUTOR_DS_CPF_UN UNIQUE (DS_CPF),
    CONSTRAINT TB_TUTOR_DS_EMAIL_UN UNIQUE (DS_EMAIL),
    CONSTRAINT TB_TUTOR_DS_CPF_CK CHECK (REGEXP_LIKE(DS_CPF, '^[0-9]{11}$'))
);

CREATE TABLE TB_VETERINARIO (
    ID_VETERINARIO NUMBER(19)         NOT NULL,
    DS_EMAIL       VARCHAR2(320 CHAR) NOT NULL,
    DS_PASSWORD    VARCHAR2(100 CHAR) NOT NULL,
    DS_ROLE        VARCHAR2(20 CHAR)  NOT NULL,
    CONSTRAINT TB_VETERINARIO_PK PRIMARY KEY (ID_VETERINARIO),
    CONSTRAINT TB_VETERINARIO_DS_EMAIL_UN UNIQUE (DS_EMAIL),
    CONSTRAINT TB_VETERINARIO_DS_ROLE_CK CHECK (DS_ROLE IN ('USER', 'ADMIN'))
);

CREATE TABLE TB_MEDICAMENTO (
    ID_MEDICAMENTO NUMBER(19)         NOT NULL,
    NM_MEDICAMENTO VARCHAR2(100 CHAR) NOT NULL,
    DS_MEDICAMENTO VARCHAR2(400 CHAR) NOT NULL,
    CONSTRAINT TB_MEDICAMENTO_PK PRIMARY KEY (ID_MEDICAMENTO)
);

-- Depende de TB_TUTOR
CREATE TABLE TB_PET (
    ID_PET            NUMBER(19)         NOT NULL,
    NM_PET            VARCHAR2(100 CHAR) NOT NULL,
    DS_RACA           VARCHAR2(100 CHAR) NOT NULL,
    DS_PESO           NUMBER(5,2)        NOT NULL,
    DS_IDADE          NUMBER(10)         NOT NULL,
    FL_CASTRADO       NUMBER(1)          NOT NULL,
    TB_TUTOR_ID_TUTOR NUMBER(19)         NOT NULL,
    CONSTRAINT TB_PET_PK PRIMARY KEY (ID_PET),
    CONSTRAINT TB_PET_TB_TUTOR_FK FOREIGN KEY (TB_TUTOR_ID_TUTOR) REFERENCES TB_TUTOR (ID_TUTOR),
    CONSTRAINT TB_PET_DS_PESO_CK CHECK (DS_PESO > 0),
    CONSTRAINT TB_PET_DS_IDADE_CK CHECK (DS_IDADE BETWEEN 0 AND 99),
    CONSTRAINT TB_PET_FL_CASTRADO_CK CHECK (FL_CASTRADO IN (0, 1))
);

-- Depende de TB_PET e TB_VETERINARIO
CREATE TABLE TB_CONSULTA (
    ID_CONSULTA                   NUMBER(19) NOT NULL,
    DT_CONSULTA                   TIMESTAMP  NOT NULL,
    TB_PET_ID_PET                 NUMBER(19) NOT NULL,
    TB_VETERINARIO_ID_VETERINARIO NUMBER(19) NOT NULL,
    CONSTRAINT TB_CONSULTA_PK PRIMARY KEY (ID_CONSULTA),
    CONSTRAINT TB_CON_DT_VET_UN UNIQUE (DT_CONSULTA, TB_VETERINARIO_ID_VETERINARIO),
    CONSTRAINT TB_CONSULTA_TB_PET_FK FOREIGN KEY (TB_PET_ID_PET) REFERENCES TB_PET (ID_PET),
    CONSTRAINT TB_CON_TB_VET_FK FOREIGN KEY (TB_VETERINARIO_ID_VETERINARIO) REFERENCES TB_VETERINARIO (ID_VETERINARIO)
);

-- Dependem de TB_CONSULTA
CREATE TABLE TB_EXAME (
    ID_EXAME                NUMBER(19)         NOT NULL,
    DS_EXAME                VARCHAR2(100 CHAR) NOT NULL,
    DS_URL                  VARCHAR2(500 CHAR),
    TB_CONSULTA_ID_CONSULTA NUMBER(19)         NOT NULL,
    CONSTRAINT TB_EXAME_PK PRIMARY KEY (ID_EXAME),
    CONSTRAINT TB_EXM_TB_CON_FK FOREIGN KEY (TB_CONSULTA_ID_CONSULTA) REFERENCES TB_CONSULTA (ID_CONSULTA)
);

CREATE TABLE TB_RECEITA (
    ID_RECEITA              NUMBER(19)         NOT NULL,
    DS_RECEITA              VARCHAR2(500 CHAR) NOT NULL,
    TB_CONSULTA_ID_CONSULTA NUMBER(19)         NOT NULL,
    CONSTRAINT TB_RECEITA_PK PRIMARY KEY (ID_RECEITA),
    CONSTRAINT TB_RCT_CON_FK FOREIGN KEY (TB_CONSULTA_ID_CONSULTA) REFERENCES TB_CONSULTA (ID_CONSULTA)
);

CREATE INDEX TB_RECEITA__IDX ON TB_RECEITA (TB_CONSULTA_ID_CONSULTA);

-- Associativas (N:N) com medicamento
CREATE TABLE TB_EXAME_MEDICAMENTO (
    ID_PEDIDO                     NUMBER(19)          NOT NULL,
    QT_MEDICAMENTO                NUMBER(10)          NOT NULL,
    DS_POSOLOGIA                  VARCHAR2(1000 CHAR) NOT NULL,
    TB_EXAME_ID_EXAME             NUMBER(19)          NOT NULL,
    TB_MEDICAMENTO_ID_MEDICAMENTO NUMBER(19)          NOT NULL,
    CONSTRAINT TB_EXAME_MEDICAMENTO_PK PRIMARY KEY (ID_PEDIDO),
    CONSTRAINT TB_EXM_MED_EXM_FK FOREIGN KEY (TB_EXAME_ID_EXAME) REFERENCES TB_EXAME (ID_EXAME),
    CONSTRAINT TB_EXM_MDCNT_MDCNT_FK FOREIGN KEY (TB_MEDICAMENTO_ID_MEDICAMENTO) REFERENCES TB_MEDICAMENTO (ID_MEDICAMENTO),
    CONSTRAINT TB_EXM_MED_QT_CK CHECK (QT_MEDICAMENTO BETWEEN 1 AND 99)
);

CREATE TABLE TB_RECEITA_MEDICAMENTO (
    ID_PEDIDO                     NUMBER(19)          NOT NULL,
    QT_MEDICAMENTO                NUMBER(10)          NOT NULL,
    DS_POSOLOGIA                  VARCHAR2(1000 CHAR) NOT NULL,
    TB_RECEITA_ID_RECEITA         NUMBER(19)          NOT NULL,
    TB_MEDICAMENTO_ID_MEDICAMENTO NUMBER(19)          NOT NULL,
    CONSTRAINT TB_RECEITA_MEDICAMENTO_PK PRIMARY KEY (ID_PEDIDO),
    CONSTRAINT TB_MEDICAMENTO_RECEITA_FK FOREIGN KEY (TB_RECEITA_ID_RECEITA) REFERENCES TB_RECEITA (ID_RECEITA),
    CONSTRAINT TB_RCT_MDCMNT_FK FOREIGN KEY (TB_MEDICAMENTO_ID_MEDICAMENTO) REFERENCES TB_MEDICAMENTO (ID_MEDICAMENTO),
    CONSTRAINT TB_RCT_MED_QT_CK CHECK (QT_MEDICAMENTO BETWEEN 1 AND 99)
);

--------------------------------------------------------------------------------
-- BLOCO 2: DICIONARIO DE DADOS
--------------------------------------------------------------------------------
COMMENT ON TABLE TB_TUTOR IS 'Responsavel pelo pet';
COMMENT ON COLUMN TB_TUTOR.ID_TUTOR IS 'Identificador do tutor (PK, SQ_TUTOR)';
COMMENT ON COLUMN TB_TUTOR.NM_TUTOR IS 'Nome completo';
COMMENT ON COLUMN TB_TUTOR.DS_CPF IS 'CPF com 11 digitos, sem pontuacao (unico)';
COMMENT ON COLUMN TB_TUTOR.DS_EMAIL IS 'E-mail para lembretes de consulta (unico)';
COMMENT ON COLUMN TB_TUTOR.DS_TELEFONE IS 'Telefone com DDD, ate 13 caracteres';

COMMENT ON TABLE TB_VETERINARIO IS 'Conta de acesso da equipe da clinica';
COMMENT ON COLUMN TB_VETERINARIO.ID_VETERINARIO IS 'Identificador do veterinario (PK, SQ_VETERINARIO)';
COMMENT ON COLUMN TB_VETERINARIO.DS_EMAIL IS 'E-mail de login (unico)';
COMMENT ON COLUMN TB_VETERINARIO.DS_PASSWORD IS 'Hash BCrypt da senha; a senha e validada antes pela PKG_VETI.FN_VALIDA_SENHA';
COMMENT ON COLUMN TB_VETERINARIO.DS_ROLE IS 'Perfil de acesso: USER ou ADMIN';

COMMENT ON TABLE TB_MEDICAMENTO IS 'Catalogo de medicamentos';
COMMENT ON COLUMN TB_MEDICAMENTO.ID_MEDICAMENTO IS 'Identificador do medicamento (PK, SQ_MEDICAMENTO)';
COMMENT ON COLUMN TB_MEDICAMENTO.NM_MEDICAMENTO IS 'Nome do medicamento';
COMMENT ON COLUMN TB_MEDICAMENTO.DS_MEDICAMENTO IS 'Descricao, apresentacao e indicacao';

COMMENT ON TABLE TB_PET IS 'Animal acompanhado pela clinica';
COMMENT ON COLUMN TB_PET.ID_PET IS 'Identificador do pet (PK, SQ_PET)';
COMMENT ON COLUMN TB_PET.NM_PET IS 'Nome do pet';
COMMENT ON COLUMN TB_PET.DS_RACA IS 'Raca (valores do enum Raca da API)';
COMMENT ON COLUMN TB_PET.DS_PESO IS 'Peso em kg (maior que zero)';
COMMENT ON COLUMN TB_PET.DS_IDADE IS 'Idade em anos (0 a 99)';
COMMENT ON COLUMN TB_PET.FL_CASTRADO IS 'Castrado: 1 = sim, 0 = nao';
COMMENT ON COLUMN TB_PET.TB_TUTOR_ID_TUTOR IS 'FK para TB_TUTOR';

COMMENT ON TABLE TB_CONSULTA IS 'Atendimento agendado (auditado pela TRG_AUDITORIA_CONSULTA)';
COMMENT ON COLUMN TB_CONSULTA.ID_CONSULTA IS 'Identificador da consulta (PK, SQ_CONSULTA)';
COMMENT ON COLUMN TB_CONSULTA.DT_CONSULTA IS 'Data e hora do atendimento; um veterinario nao tem duas consultas no mesmo horario';
COMMENT ON COLUMN TB_CONSULTA.TB_PET_ID_PET IS 'FK para TB_PET';
COMMENT ON COLUMN TB_CONSULTA.TB_VETERINARIO_ID_VETERINARIO IS 'FK para TB_VETERINARIO';

COMMENT ON TABLE TB_EXAME IS 'Exame solicitado em uma consulta';
COMMENT ON COLUMN TB_EXAME.ID_EXAME IS 'Identificador do exame (PK, SQ_EXAME)';
COMMENT ON COLUMN TB_EXAME.DS_EXAME IS 'Descricao do exame';
COMMENT ON COLUMN TB_EXAME.DS_URL IS 'Link para o laudo (opcional)';
COMMENT ON COLUMN TB_EXAME.TB_CONSULTA_ID_CONSULTA IS 'FK para TB_CONSULTA';

COMMENT ON TABLE TB_RECEITA IS 'Receita emitida em uma consulta';
COMMENT ON COLUMN TB_RECEITA.ID_RECEITA IS 'Identificador da receita (PK, SQ_RECEITA)';
COMMENT ON COLUMN TB_RECEITA.DS_RECEITA IS 'Descricao e orientacoes gerais';
COMMENT ON COLUMN TB_RECEITA.TB_CONSULTA_ID_CONSULTA IS 'FK para TB_CONSULTA (indexada)';

COMMENT ON TABLE TB_EXAME_MEDICAMENTO IS 'Medicamento associado a um exame';
COMMENT ON COLUMN TB_EXAME_MEDICAMENTO.ID_PEDIDO IS 'Identificador do item (PK, SQ_EXAME_MEDICAMENTO)';
COMMENT ON COLUMN TB_EXAME_MEDICAMENTO.QT_MEDICAMENTO IS 'Quantidade (1 a 99)';
COMMENT ON COLUMN TB_EXAME_MEDICAMENTO.DS_POSOLOGIA IS 'Como administrar';
COMMENT ON COLUMN TB_EXAME_MEDICAMENTO.TB_EXAME_ID_EXAME IS 'FK para TB_EXAME';
COMMENT ON COLUMN TB_EXAME_MEDICAMENTO.TB_MEDICAMENTO_ID_MEDICAMENTO IS 'FK para TB_MEDICAMENTO';

COMMENT ON TABLE TB_RECEITA_MEDICAMENTO IS 'Itens da receita (tabela de fatos da PRC_RESUMO_RECEITAS)';
COMMENT ON COLUMN TB_RECEITA_MEDICAMENTO.ID_PEDIDO IS 'Identificador do item (PK, SQ_RECEITA_MEDICAMENTO)';
COMMENT ON COLUMN TB_RECEITA_MEDICAMENTO.QT_MEDICAMENTO IS 'Quantidade prescrita (1 a 99), somada no relatorio';
COMMENT ON COLUMN TB_RECEITA_MEDICAMENTO.DS_POSOLOGIA IS 'Como administrar';
COMMENT ON COLUMN TB_RECEITA_MEDICAMENTO.TB_RECEITA_ID_RECEITA IS 'FK para TB_RECEITA (categoria 1 do relatorio)';
COMMENT ON COLUMN TB_RECEITA_MEDICAMENTO.TB_MEDICAMENTO_ID_MEDICAMENTO IS 'FK para TB_MEDICAMENTO (categoria 2 do relatorio)';

--------------------------------------------------------------------------------
-- BLOCO 3: CARGA DE DADOS (minimo de 5 registros por tabela)
-- Os Ids saem das mesmas sequences usadas pela API, entao a aplicacao continua a numeracao.
-- Senhas dos veterinarios (hash BCrypt, como a API grava): Veti2026, Pata2026, Focinho9, Animal2026, VetForte7
--------------------------------------------------------------------------------
INSERT INTO TB_TUTOR (ID_TUTOR, NM_TUTOR, DS_CPF, DS_EMAIL, DS_TELEFONE) VALUES (SQ_TUTOR.NEXTVAL, 'Ana Paula Ribeiro', '11111111111', 'ana.ribeiro@example.com', '11911111111');
INSERT INTO TB_TUTOR (ID_TUTOR, NM_TUTOR, DS_CPF, DS_EMAIL, DS_TELEFONE) VALUES (SQ_TUTOR.NEXTVAL, 'Bruno Costa Lima', '22222222222', 'bruno.lima@example.com', '11922222222');
INSERT INTO TB_TUTOR (ID_TUTOR, NM_TUTOR, DS_CPF, DS_EMAIL, DS_TELEFONE) VALUES (SQ_TUTOR.NEXTVAL, 'Carla Mendes Souza', '33333333333', 'carla.souza@example.com', '11933333333');
INSERT INTO TB_TUTOR (ID_TUTOR, NM_TUTOR, DS_CPF, DS_EMAIL, DS_TELEFONE) VALUES (SQ_TUTOR.NEXTVAL, 'Diego Fernandes Alves', '44444444444', 'diego.alves@example.com', '11944444444');
INSERT INTO TB_TUTOR (ID_TUTOR, NM_TUTOR, DS_CPF, DS_EMAIL, DS_TELEFONE) VALUES (SQ_TUTOR.NEXTVAL, 'Elaine Torres Pinto', '55555555555', 'elaine.pinto@example.com', NULL);

INSERT INTO TB_VETERINARIO (ID_VETERINARIO, DS_EMAIL, DS_PASSWORD, DS_ROLE) VALUES (SQ_VETERINARIO.NEXTVAL, 'carlos.vet@example.com', '$2a$10$3ATOAPOgVNsZWsmGDDYH6OMZM4ZJyVy09IoG426ifYiKCCB/FLNZa', 'ADMIN');
INSERT INTO TB_VETERINARIO (ID_VETERINARIO, DS_EMAIL, DS_PASSWORD, DS_ROLE) VALUES (SQ_VETERINARIO.NEXTVAL, 'fernanda.vet@example.com', '$2a$10$0UmPwuBjtmq2ULnQhYSWq.Iul0YlwQz4btdjcCnVTdjfdvcEnjYPm', 'USER');
INSERT INTO TB_VETERINARIO (ID_VETERINARIO, DS_EMAIL, DS_PASSWORD, DS_ROLE) VALUES (SQ_VETERINARIO.NEXTVAL, 'marcelo.vet@example.com', '$2a$10$X8rbz3qYjGfM6RUWoGOFCOsKMblTqiwfw0xIl/JBAXmLK3I0arPC6', 'USER');
INSERT INTO TB_VETERINARIO (ID_VETERINARIO, DS_EMAIL, DS_PASSWORD, DS_ROLE) VALUES (SQ_VETERINARIO.NEXTVAL, 'juliana.vet@example.com', '$2a$10$b5FLoOwpJwZvWcUd/91W8uOA0BKQBlBFT9b7IMRKBskgXuQt7dyFq', 'USER');
INSERT INTO TB_VETERINARIO (ID_VETERINARIO, DS_EMAIL, DS_PASSWORD, DS_ROLE) VALUES (SQ_VETERINARIO.NEXTVAL, 'rafael.vet@example.com', '$2a$10$V5RrvepUXlFu7K0iMek32.OIRl3aCaAtsT9eZ1pFemCB3NVNMDpva', 'USER');

INSERT INTO TB_MEDICAMENTO (ID_MEDICAMENTO, NM_MEDICAMENTO, DS_MEDICAMENTO) VALUES (SQ_MEDICAMENTO.NEXTVAL, 'Amoxicilina', 'Antibiotico de amplo espectro, comprimido de 250 mg');
INSERT INTO TB_MEDICAMENTO (ID_MEDICAMENTO, NM_MEDICAMENTO, DS_MEDICAMENTO) VALUES (SQ_MEDICAMENTO.NEXTVAL, 'Meloxicam', 'Anti-inflamatorio nao esteroidal, 0,2 mg por kg');
INSERT INTO TB_MEDICAMENTO (ID_MEDICAMENTO, NM_MEDICAMENTO, DS_MEDICAMENTO) VALUES (SQ_MEDICAMENTO.NEXTVAL, 'Ivermectina', 'Antiparasitario, solucao a 1%');
INSERT INTO TB_MEDICAMENTO (ID_MEDICAMENTO, NM_MEDICAMENTO, DS_MEDICAMENTO) VALUES (SQ_MEDICAMENTO.NEXTVAL, 'Dipirona', 'Analgesico e antitermico, gotas 500 mg por ml');
INSERT INTO TB_MEDICAMENTO (ID_MEDICAMENTO, NM_MEDICAMENTO, DS_MEDICAMENTO) VALUES (SQ_MEDICAMENTO.NEXTVAL, 'Prednisolona', 'Corticoide, comprimido de 20 mg');

-- a tutora 1 tem dois pets (Rex e Nina), o que aparece como array no documento do MongoDB
INSERT INTO TB_PET (ID_PET, NM_PET, DS_RACA, DS_PESO, DS_IDADE, FL_CASTRADO, TB_TUTOR_ID_TUTOR) VALUES (SQ_PET.NEXTVAL, 'Rex', 'LABRADOR_RETRIEVER', 32.5, 4, 1, 1);
INSERT INTO TB_PET (ID_PET, NM_PET, DS_RACA, DS_PESO, DS_IDADE, FL_CASTRADO, TB_TUTOR_ID_TUTOR) VALUES (SQ_PET.NEXTVAL, 'Mimi', 'SIAMESE', 4.2, 2, 1, 2);
INSERT INTO TB_PET (ID_PET, NM_PET, DS_RACA, DS_PESO, DS_IDADE, FL_CASTRADO, TB_TUTOR_ID_TUTOR) VALUES (SQ_PET.NEXTVAL, 'Thor', 'FRENCH_BULLDOG', 12.8, 3, 0, 3);
INSERT INTO TB_PET (ID_PET, NM_PET, DS_RACA, DS_PESO, DS_IDADE, FL_CASTRADO, TB_TUTOR_ID_TUTOR) VALUES (SQ_PET.NEXTVAL, 'Luna', 'PERSIAN', 3.9, 5, 1, 4);
INSERT INTO TB_PET (ID_PET, NM_PET, DS_RACA, DS_PESO, DS_IDADE, FL_CASTRADO, TB_TUTOR_ID_TUTOR) VALUES (SQ_PET.NEXTVAL, 'Bidu', 'MIXED_BREED', 18, 6, 0, 5);
INSERT INTO TB_PET (ID_PET, NM_PET, DS_RACA, DS_PESO, DS_IDADE, FL_CASTRADO, TB_TUTOR_ID_TUTOR) VALUES (SQ_PET.NEXTVAL, 'Nina', 'BEAGLE', 10.4, 1, 0, 1);

-- o veterinario 1 atende Rex duas vezes e Nina uma vez (subtotal com mais de uma linha na PRC_RESUMO_CONSULTAS)
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO) VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-10-05 09:00:00', 1, 1);
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO) VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-10-05 10:00:00', 2, 2);
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO) VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-10-06 14:30:00', 3, 3);
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO) VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-10-07 08:30:00', 4, 4);
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO) VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-10-07 16:00:00', 5, 5);
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO) VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-10-08 11:00:00', 6, 1);
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO) VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-10-19 09:00:00', 1, 1);

INSERT INTO TB_EXAME (ID_EXAME, DS_EXAME, DS_URL, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_EXAME.NEXTVAL, 'Hemograma completo', 'https://laudos.example.com/exames/1.pdf', 1);
INSERT INTO TB_EXAME (ID_EXAME, DS_EXAME, DS_URL, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_EXAME.NEXTVAL, 'Teste rapido FIV e FeLV', NULL, 2);
INSERT INTO TB_EXAME (ID_EXAME, DS_EXAME, DS_URL, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_EXAME.NEXTVAL, 'Raspado de pele', 'https://laudos.example.com/exames/3.pdf', 3);
INSERT INTO TB_EXAME (ID_EXAME, DS_EXAME, DS_URL, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_EXAME.NEXTVAL, 'Ultrassom abdominal', 'https://laudos.example.com/exames/4.pdf', 4);
INSERT INTO TB_EXAME (ID_EXAME, DS_EXAME, DS_URL, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_EXAME.NEXTVAL, 'Radiografia de quadril', NULL, 5);

INSERT INTO TB_EXAME_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_EXAME_ID_EXAME, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_EXAME_MEDICAMENTO.NEXTVAL, 1, 'Suspender 24 horas antes da coleta', 1, 1);
INSERT INTO TB_EXAME_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_EXAME_ID_EXAME, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_EXAME_MEDICAMENTO.NEXTVAL, 1, 'Dose unica apos o resultado negativo', 2, 3);
INSERT INTO TB_EXAME_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_EXAME_ID_EXAME, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_EXAME_MEDICAMENTO.NEXTVAL, 4, 'Uma dose ao dia ate o retorno', 3, 2);
INSERT INTO TB_EXAME_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_EXAME_ID_EXAME, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_EXAME_MEDICAMENTO.NEXTVAL, 2, '10 gotas antes do exame', 4, 4);
INSERT INTO TB_EXAME_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_EXAME_ID_EXAME, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_EXAME_MEDICAMENTO.NEXTVAL, 3, 'Um comprimido ao dia por 3 dias', 5, 5);

INSERT INTO TB_RECEITA (ID_RECEITA, DS_RECEITA, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_RECEITA.NEXTVAL, 'Tratamento de otite bacteriana', 1);
INSERT INTO TB_RECEITA (ID_RECEITA, DS_RECEITA, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_RECEITA.NEXTVAL, 'Tratamento de infeccao urinaria', 2);
INSERT INTO TB_RECEITA (ID_RECEITA, DS_RECEITA, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_RECEITA.NEXTVAL, 'Controle de dermatite alergica', 3);
INSERT INTO TB_RECEITA (ID_RECEITA, DS_RECEITA, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_RECEITA.NEXTVAL, 'Controle de dor articular', 5);
INSERT INTO TB_RECEITA (ID_RECEITA, DS_RECEITA, TB_CONSULTA_ID_CONSULTA) VALUES (SQ_RECEITA.NEXTVAL, 'Vermifugacao de retorno', 7);

-- tabela de fatos do relatorio: a receita 3 repete o Meloxicam, somado na mesma linha (receita 3, medicamento 2)
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 14, 'Um comprimido a cada 12 horas por 7 dias', 1, 1);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 6, '15 gotas a cada 8 horas se houver dor', 1, 4);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 10, 'Um comprimido a cada 12 horas por 5 dias', 2, 1);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 7, 'Uma dose ao dia pela manha', 3, 2);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 3, 'Dose extra em caso de crise de coceira', 3, 2);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 10, 'Um comprimido ao dia, reduzindo na segunda semana', 3, 5);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 8, 'Uma dose ao dia apos a refeicao', 4, 2);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 12, '20 gotas a cada 8 horas', 4, 4);
INSERT INTO TB_RECEITA_MEDICAMENTO (ID_PEDIDO, QT_MEDICAMENTO, DS_POSOLOGIA, TB_RECEITA_ID_RECEITA, TB_MEDICAMENTO_ID_MEDICAMENTO) VALUES (SQ_RECEITA_MEDICAMENTO.NEXTVAL, 1, 'Dose unica', 5, 3);

COMMIT;

--------------------------------------------------------------------------------
-- BLOCO 4: TB_AUDITORIA, PACKAGE PKG_VETI E TRIGGER (copia do pkg_veti.sql)
--------------------------------------------------------------------------------
--------------------------------------------------------------------------------
-- CHALLENGE FIAP 2026 - CLYVO VET
-- Disciplina: Mastering Relational and Non-Relational Database - Sprint 4
-- Objetos PL-SQL usados pela API Java (Veti), escritos sobre o schema criado pelo Hibernate.
--
-- Conteudo:
--   1. TB_AUDITORIA (criada aqui porque nao e uma entidade JPA)
--   2. Package PKG_VETI
--        Funcoes:    FN_MONTA_JSON, FN_VALIDA_SENHA
--        Procedures: PRC_CONSULTAS_JSON, PRC_RESUMO_CONSULTAS, PRC_RESUMO_RECEITAS,
--                    PRC_EXPORTAR_DATASET, PRC_REGISTRAR_AUDITORIA
--   3. Trigger TRG_AUDITORIA_CONSULTA (DML em TB_CONSULTA, grava pelo PKG_VETI)
--   4. Verificacao: falha se algum objeto ficar INVALID
--
-- Execucao:
--   - Automatica: a API roda este arquivo na subida (spring.sql.init), depois de o Hibernate criar as tabelas.
--   - Manual (SQL Developer ou DBeaver): rodar como script (F5) com o usuario da aplicacao.
-- O script e idempotente: pode ser executado varias vezes.
-- Regras do leitor de script do Spring (separador "barra"):
--   - cada bloco termina com uma linha contendo apenas a barra; nao use a barra fora de textos entre aspas;
--   - nao use a barra invertida: ela e trocada por CHR(92).
-- O DBMS_OUTPUT so aparece no SQL Developer; para a API o resultado volta nos parametros OUT CLOB.
--------------------------------------------------------------------------------

-- BLOCO 1: TABELA DE AUDITORIA (alimentada pela trigger do Bloco 3)
DECLARE
    e_ja_existe EXCEPTION;
    PRAGMA EXCEPTION_INIT(e_ja_existe, -955);
BEGIN
    EXECUTE IMMEDIATE '
        CREATE TABLE TB_AUDITORIA (
            ID_AUDITORIA          NUMBER(10) GENERATED BY DEFAULT ON NULL AS IDENTITY,
            NM_USUARIO            VARCHAR2(100)  NOT NULL,
            TP_OPERACAO           VARCHAR2(10)   NOT NULL,
            DT_OPERACAO           TIMESTAMP      NOT NULL,
            DS_VALORES_ANTERIORES VARCHAR2(4000),
            DS_VALORES_NOVOS      VARCHAR2(4000),
            CONSTRAINT TB_AUDITORIA_PK PRIMARY KEY (ID_AUDITORIA),
            CONSTRAINT TB_AUDITORIA_TP_OPERACAO_CK CHECK (TP_OPERACAO IN (''INSERT'', ''UPDATE'', ''DELETE''))
        )';
EXCEPTION
    WHEN e_ja_existe THEN
        NULL;
END;
/

-- BLOCO 2: PACKAGE PKG_VETI (especificacao)
CREATE OR REPLACE PACKAGE PKG_VETI AS

    -- lista generica de strings (chaves, valores e tipos da FN_MONTA_JSON)
    TYPE t_lista IS TABLE OF VARCHAR2(32767);

    -- Monta manualmente um objeto JSON, sem TO_JSON, JSON_OBJECT ou qualquer funcao nativa de JSON.
    -- p_tipos: 'S' = texto (padrao) | 'N' = numero | 'B' = booleano (1, 0, TRUE ou FALSE)
    --          'J' = objeto ou array JSON ja montado (inserido sem aspas, para aninhar documentos)
    FUNCTION fn_monta_json(
        p_chaves  IN t_lista,
        p_valores IN t_lista,
        p_tipos   IN t_lista DEFAULT NULL
    ) RETURN VARCHAR2;

    -- Valida a senha de um veterinario. Retorna 'SENHA_VALIDA' ou 'ERRO: <motivo>'.
    FUNCTION fn_valida_senha(p_senha IN VARCHAR2) RETURN VARCHAR2;

    -- JOIN entre TB_CONSULTA, TB_PET, TB_TUTOR e TB_VETERINARIO, cada linha convertida em JSON pela fn_monta_json.
    -- Devolve um array JSON em p_json.
    PROCEDURE prc_consultas_json(p_json OUT CLOB);

    -- Contagem manual (sem ROLLUP, CUBE ou GROUPING SETS) de consultas por veterinario (categoria 1)
    -- e pet (categoria 2), com subtotal por veterinario e total geral. Devolve o relatorio em texto.
    PROCEDURE prc_resumo_consultas(p_resumo OUT CLOB);

    -- Soma manual (sem ROLLUP, CUBE, GROUPING SETS ou GROUPING) de QT_MEDICAMENTO da tabela de fatos
    -- TB_RECEITA_MEDICAMENTO por receita (categoria 1) e medicamento (categoria 2), com subtotal por
    -- receita e total geral. Devolve o relatorio em texto.
    PROCEDURE prc_resumo_receitas(p_resumo OUT CLOB);

    -- Exporta o banco relacional em um documento JSON aninhado para importacao no MongoDB:
    -- tutores -> pets -> consultas (veterinario, exames e receitas, com seus medicamentos),
    -- mais os catalogos de veterinarios (sem senha) e de medicamentos.
    PROCEDURE prc_exportar_dataset(p_json OUT CLOB);

    -- Grava um registro em TB_AUDITORIA (chamada pela trigger TRG_AUDITORIA_CONSULTA).
    PROCEDURE prc_registrar_auditoria(
        p_tipo_operacao      IN VARCHAR2,
        p_valores_anteriores IN VARCHAR2,
        p_valores_novos      IN VARCHAR2
    );

END PKG_VETI;
/

-- BLOCO 2: PACKAGE PKG_VETI (corpo)
CREATE OR REPLACE PACKAGE BODY PKG_VETI AS

    -- (privada) escapa barra invertida, aspas duplas e quebras de linha de um texto JSON.
    -- CHR(92) e a barra invertida; o leitor de script do Spring a trataria como escape.
    FUNCTION fn_escapa(p_texto IN VARCHAR2) RETURN VARCHAR2
    IS
    BEGIN
        RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p_texto,
                   CHR(92), CHR(92) || CHR(92)),
                   '"',     CHR(92) || '"'),
                   CHR(10), CHR(92) || 'n'),
                   CHR(13), CHR(92) || 'r'),
                   CHR(9),  CHR(92) || 't');
    END fn_escapa;

    -- (privada) imprime um CLOB no DBMS_OUTPUT, uma linha por quebra CHR(10)
    PROCEDURE prc_imprime(p_texto IN CLOB)
    IS
        v_inicio  PLS_INTEGER := 1;
        v_fim     PLS_INTEGER;
        v_tamanho PLS_INTEGER;
    BEGIN
        IF p_texto IS NULL THEN
            RETURN;
        END IF;
        v_tamanho := DBMS_LOB.GETLENGTH(p_texto);
        WHILE v_inicio <= v_tamanho LOOP
            v_fim := DBMS_LOB.INSTR(p_texto, CHR(10), v_inicio);
            IF v_fim = 0 THEN
                v_fim := v_tamanho + 1;
            END IF;
            DBMS_OUTPUT.PUT_LINE(DBMS_LOB.SUBSTR(p_texto, v_fim - v_inicio, v_inicio));
            v_inicio := v_fim + 1;
        END LOOP;
    END prc_imprime;


    FUNCTION fn_monta_json(
        p_chaves  IN t_lista,
        p_valores IN t_lista,
        p_tipos   IN t_lista DEFAULT NULL
    ) RETURN VARCHAR2
    IS
        v_json  VARCHAR2(32767) := '{';
        v_valor VARCHAR2(32767);
        v_tipo  VARCHAR2(10);
        e_listas_vazias       EXCEPTION;
        e_tamanhos_diferentes EXCEPTION;
        e_tipo_invalido       EXCEPTION;
        e_json_aninhado       EXCEPTION;
    BEGIN
        -- Excecao 1: listas vazias ou nulas
        IF p_chaves IS NULL OR p_valores IS NULL OR p_chaves.COUNT = 0 THEN
            RAISE e_listas_vazias;
        END IF;

        -- Excecao 2: chaves e valores com quantidades diferentes
        IF p_chaves.COUNT <> p_valores.COUNT THEN
            RAISE e_tamanhos_diferentes;
        END IF;

        FOR i IN 1 .. p_chaves.COUNT LOOP
            IF p_tipos IS NOT NULL AND p_tipos.COUNT = p_chaves.COUNT THEN
                v_tipo := UPPER(p_tipos(i));
            ELSE
                v_tipo := 'S';
            END IF;

            -- Excecao 3: tipo fora do dominio aceito
            IF v_tipo IS NULL OR v_tipo NOT IN ('S', 'N', 'B', 'J') THEN
                RAISE e_tipo_invalido;
            END IF;

            v_valor := p_valores(i);

            IF v_valor IS NULL THEN
                v_valor := 'null';
            ELSIF v_tipo = 'N' THEN
                -- Excecao 4 (VALUE_ERROR nativa): o valor precisa ser numerico; a saida usa ponto decimal
                v_valor := TO_CHAR(TO_NUMBER(v_valor), 'TM9', 'NLS_NUMERIC_CHARACTERS=''.,''');
                IF SUBSTR(v_valor, 1, 1) = '.' THEN
                    v_valor := '0' || v_valor;
                ELSIF SUBSTR(v_valor, 1, 2) = '-.' THEN
                    v_valor := '-0' || SUBSTR(v_valor, 2);
                END IF;
            ELSIF v_tipo = 'B' THEN
                IF UPPER(v_valor) IN ('1', 'TRUE') THEN
                    v_valor := 'true';
                ELSIF UPPER(v_valor) IN ('0', 'FALSE') THEN
                    v_valor := 'false';
                ELSE
                    RAISE VALUE_ERROR;
                END IF;
            ELSIF v_tipo = 'J' THEN
                -- Excecao 5: o valor aninhado precisa ser um objeto ou um array JSON
                IF SUBSTR(LTRIM(v_valor), 1, 1) NOT IN ('{', '[') THEN
                    RAISE e_json_aninhado;
                END IF;
            ELSE
                v_valor := '"' || fn_escapa(v_valor) || '"';
            END IF;

            v_json := v_json || '"' || fn_escapa(p_chaves(i)) || '":' || v_valor;

            IF i < p_chaves.COUNT THEN
                v_json := v_json || ',';
            END IF;
        END LOOP;

        RETURN v_json || '}';

    EXCEPTION
        WHEN e_listas_vazias THEN
            RAISE_APPLICATION_ERROR(-20001, 'FN_MONTA_JSON: nao ha colunas para converter em JSON.');
        WHEN e_tamanhos_diferentes THEN
            RAISE_APPLICATION_ERROR(-20002, 'FN_MONTA_JSON: quantidade de chaves e valores diferente.');
        WHEN e_tipo_invalido THEN
            RAISE_APPLICATION_ERROR(-20003, 'FN_MONTA_JSON: tipo de campo invalido (use S, N, B ou J).');
        WHEN VALUE_ERROR THEN
            RAISE_APPLICATION_ERROR(-20004, 'FN_MONTA_JSON: valor incompativel com o tipo informado ou JSON acima do tamanho maximo.');
        WHEN e_json_aninhado THEN
            RAISE_APPLICATION_ERROR(-20005, 'FN_MONTA_JSON: valor do tipo J deve ser um objeto ou array JSON.');
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20099, 'FN_MONTA_JSON: erro inesperado - ' || SQLERRM);
    END fn_monta_json;


    FUNCTION fn_valida_senha(p_senha IN VARCHAR2) RETURN VARCHAR2
    IS
        e_senha_nula       EXCEPTION;
        e_senha_curta      EXCEPTION;
        e_senha_sem_numero EXCEPTION;
        e_senha_sem_letra  EXCEPTION;
    BEGIN
        IF p_senha IS NULL THEN
            RAISE e_senha_nula;
        END IF;

        IF LENGTH(p_senha) < 8 THEN
            RAISE e_senha_curta;
        END IF;

        IF NOT REGEXP_LIKE(p_senha, '[0-9]') THEN
            RAISE e_senha_sem_numero;
        END IF;

        IF NOT REGEXP_LIKE(p_senha, '[A-Za-z]') THEN
            RAISE e_senha_sem_letra;
        END IF;

        RETURN 'SENHA_VALIDA';

    EXCEPTION
        WHEN e_senha_nula THEN
            RETURN 'ERRO: senha nao informada.';
        WHEN e_senha_curta THEN
            RETURN 'ERRO: a senha deve ter no minimo 8 caracteres.';
        WHEN e_senha_sem_numero THEN
            RETURN 'ERRO: a senha deve conter ao menos um numero.';
        WHEN e_senha_sem_letra THEN
            RETURN 'ERRO: a senha deve conter ao menos uma letra.';
        WHEN OTHERS THEN
            RETURN 'ERRO: falha inesperada ao validar a senha - ' || SQLERRM;
    END fn_valida_senha;


    PROCEDURE prc_consultas_json(p_json OUT CLOB)
    IS
        CURSOR c_consultas IS
            SELECT c.ID_CONSULTA,
                   TO_CHAR(c.DT_CONSULTA, 'YYYY-MM-DD"T"HH24:MI') AS DT_CONSULTA,
                   p.NM_PET,
                   p.DS_RACA,
                   t.NM_TUTOR,
                   t.DS_EMAIL AS TUTOR_EMAIL,
                   v.DS_EMAIL AS VET_EMAIL
            FROM   TB_CONSULTA c
            JOIN   TB_PET p         ON p.ID_PET = c.TB_PET_ID_PET
            JOIN   TB_TUTOR t       ON t.ID_TUTOR = p.TB_TUTOR_ID_TUTOR
            JOIN   TB_VETERINARIO v ON v.ID_VETERINARIO = c.TB_VETERINARIO_ID_VETERINARIO
            ORDER BY c.DT_CONSULTA;

        v_chaves     t_lista := t_lista('consulta_id', 'data_consulta', 'pet_nome', 'pet_raca',
                                        'tutor_nome', 'tutor_email', 'veterinario_email');
        v_tipos      t_lista := t_lista('N', 'S', 'S', 'S', 'S', 'S', 'S');
        v_linha      VARCHAR2(4000);
        v_qtd_linhas NUMBER := 0;
    BEGIN
        p_json := '[';

        FOR r IN c_consultas LOOP
            v_linha := fn_monta_json(
                v_chaves,
                t_lista(TO_CHAR(r.ID_CONSULTA), r.DT_CONSULTA, r.NM_PET, r.DS_RACA,
                        r.NM_TUTOR, r.TUTOR_EMAIL, r.VET_EMAIL),
                v_tipos);

            IF v_qtd_linhas > 0 THEN
                p_json := p_json || ',';
            END IF;
            p_json := p_json || v_linha;
            DBMS_OUTPUT.PUT_LINE(v_linha);
            v_qtd_linhas := v_qtd_linhas + 1;
        END LOOP;

        p_json := p_json || ']';

        -- Excecao 1: nenhuma consulta (devolve array vazio e avisa no DBMS_OUTPUT)
        IF v_qtd_linhas = 0 THEN
            RAISE NO_DATA_FOUND;
        END IF;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_json := '[]';
            DBMS_OUTPUT.PUT_LINE('AVISO: nenhuma consulta cadastrada para gerar o JSON.');
        -- Excecao 2: erro de conversao de tipo
        WHEN VALUE_ERROR THEN
            RAISE_APPLICATION_ERROR(-20011, 'PRC_CONSULTAS_JSON: valor invalido ao converter a consulta para JSON.');
        -- Excecao 3: qualquer outro erro
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20019, 'PRC_CONSULTAS_JSON: erro inesperado - ' || SQLERRM);
    END prc_consultas_json;


    PROCEDURE prc_resumo_consultas(p_resumo OUT CLOB)
    IS
        CURSOR c_consultas IS
            SELECT v.DS_EMAIL AS VET_EMAIL,
                   p.NM_PET
            FROM   TB_CONSULTA c
            JOIN   TB_PET p         ON p.ID_PET = c.TB_PET_ID_PET
            JOIN   TB_VETERINARIO v ON v.ID_VETERINARIO = c.TB_VETERINARIO_ID_VETERINARIO
            ORDER BY v.DS_EMAIL, p.NM_PET;

        v_vet_atual   TB_VETERINARIO.DS_EMAIL%TYPE := NULL;
        v_pet_atual   TB_PET.NM_PET%TYPE := NULL;
        v_qtd_pet     NUMBER := 0;
        v_subtotal    NUMBER := 0;
        v_total_geral NUMBER := 0;
        v_qtd_linhas  NUMBER := 0;

        PROCEDURE escreve(p_texto IN VARCHAR2) IS
        BEGIN
            p_resumo := p_resumo || p_texto || CHR(10);
            DBMS_OUTPUT.PUT_LINE(p_texto);
        END escreve;

        PROCEDURE fecha_pet IS
        BEGIN
            IF v_pet_atual IS NOT NULL THEN
                escreve(RPAD(v_vet_atual, 40) || RPAD(v_pet_atual, 25) || TO_CHAR(v_qtd_pet));
            END IF;
        END fecha_pet;

        PROCEDURE fecha_veterinario IS
        BEGIN
            IF v_vet_atual IS NOT NULL THEN
                escreve(RPAD(' ', 40) || RPAD('Sub Total', 25) || TO_CHAR(v_subtotal));
            END IF;
        END fecha_veterinario;
    BEGIN
        p_resumo := '';
        escreve(RPAD('Veterinario', 40) || RPAD('Pet', 25) || 'Consultas');
        escreve(RPAD('-', 38, '-') || '  ' || RPAD('-', 23, '-') || '  ' || RPAD('-', 9, '-'));

        FOR r IN c_consultas LOOP
            -- quebra de controle 1: mudou o veterinario, fecha o pet e o subtotal anteriores
            IF v_vet_atual IS NOT NULL AND r.VET_EMAIL <> v_vet_atual THEN
                fecha_pet;
                fecha_veterinario;
                v_subtotal  := 0;
                v_pet_atual := NULL;
                v_qtd_pet   := 0;
            -- quebra de controle 2: mesmo veterinario, mudou o pet
            ELSIF v_pet_atual IS NOT NULL AND r.NM_PET <> v_pet_atual THEN
                fecha_pet;
                v_qtd_pet := 0;
            END IF;

            v_vet_atual   := r.VET_EMAIL;
            v_pet_atual   := r.NM_PET;
            v_qtd_pet     := v_qtd_pet + 1;
            v_subtotal    := v_subtotal + 1;
            v_total_geral := v_total_geral + 1;
            v_qtd_linhas  := v_qtd_linhas + 1;
        END LOOP;

        -- Excecao 1: nenhuma consulta para resumir
        IF v_qtd_linhas = 0 THEN
            RAISE NO_DATA_FOUND;
        END IF;

        fecha_pet;
        fecha_veterinario;
        escreve(RPAD(' ', 40) || RPAD('Total Geral', 25) || TO_CHAR(v_total_geral));

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            escreve('AVISO: nao ha consultas cadastradas para resumir.');
        -- Excecao 2: erro de conversao ou aritmetico
        WHEN VALUE_ERROR THEN
            RAISE_APPLICATION_ERROR(-20021, 'PRC_RESUMO_CONSULTAS: valor invalido ao montar o resumo.');
        -- Excecao 3: qualquer outro erro
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20029, 'PRC_RESUMO_CONSULTAS: erro inesperado - ' || SQLERRM);
    END prc_resumo_consultas;


    PROCEDURE prc_resumo_receitas(p_resumo OUT CLOB)
    IS
        CURSOR c_itens IS
            SELECT rm.TB_RECEITA_ID_RECEITA         AS ID_RECEITA,
                   rm.TB_MEDICAMENTO_ID_MEDICAMENTO AS ID_MEDICAMENTO,
                   m.NM_MEDICAMENTO,
                   rm.QT_MEDICAMENTO
            FROM   TB_RECEITA_MEDICAMENTO rm
            JOIN   TB_MEDICAMENTO m ON m.ID_MEDICAMENTO = rm.TB_MEDICAMENTO_ID_MEDICAMENTO
            ORDER BY rm.TB_RECEITA_ID_RECEITA, rm.TB_MEDICAMENTO_ID_MEDICAMENTO;

        v_receita_atual     TB_RECEITA_MEDICAMENTO.TB_RECEITA_ID_RECEITA%TYPE := NULL;
        v_medicamento_atual TB_RECEITA_MEDICAMENTO.TB_MEDICAMENTO_ID_MEDICAMENTO%TYPE := NULL;
        v_nome_atual        TB_MEDICAMENTO.NM_MEDICAMENTO%TYPE := NULL;
        v_qtd_combinacao    NUMBER := 0;
        v_subtotal          NUMBER := 0;
        v_total_geral       NUMBER := 0;
        v_qtd_linhas        NUMBER := 0;
        e_quantidade_invalida EXCEPTION;

        PROCEDURE escreve(p_texto IN VARCHAR2) IS
        BEGIN
            p_resumo := p_resumo || p_texto || CHR(10);
            DBMS_OUTPUT.PUT_LINE(p_texto);
        END escreve;

        -- linha de detalhe: soma da combinacao completa (receita, medicamento)
        PROCEDURE fecha_combinacao IS
        BEGIN
            IF v_medicamento_atual IS NOT NULL THEN
                escreve(RPAD(TO_CHAR(v_receita_atual), 12)
                        || RPAD(SUBSTR(v_medicamento_atual || ' - ' || v_nome_atual, 1, 38), 40)
                        || TO_CHAR(v_qtd_combinacao));
            END IF;
        END fecha_combinacao;

        -- subtotal da receita: categorias ausentes, rotulo na coluna do medicamento
        PROCEDURE fecha_receita IS
        BEGIN
            IF v_receita_atual IS NOT NULL THEN
                escreve(RPAD(' ', 12) || RPAD('Sub Total', 40) || TO_CHAR(v_subtotal));
            END IF;
        END fecha_receita;
    BEGIN
        p_resumo := '';
        escreve(RPAD('Receita', 12) || RPAD('Medicamento', 40) || 'Quantidade');
        escreve(RPAD('-', 10, '-') || '  ' || RPAD('-', 38, '-') || '  ' || RPAD('-', 10, '-'));

        FOR r IN c_itens LOOP
            -- Excecao 1 (regra de negocio): quantidade nula ou menor que 1
            IF r.QT_MEDICAMENTO IS NULL OR r.QT_MEDICAMENTO < 1 THEN
                RAISE e_quantidade_invalida;
            END IF;

            -- quebra de controle 1: mudou a receita, fecha a combinacao e o subtotal anteriores
            IF v_receita_atual IS NOT NULL AND r.ID_RECEITA <> v_receita_atual THEN
                fecha_combinacao;
                fecha_receita;
                v_subtotal          := 0;
                v_medicamento_atual := NULL;
                v_qtd_combinacao    := 0;
            -- quebra de controle 2: mesma receita, mudou o medicamento
            ELSIF v_medicamento_atual IS NOT NULL AND r.ID_MEDICAMENTO <> v_medicamento_atual THEN
                fecha_combinacao;
                v_qtd_combinacao := 0;
            END IF;

            v_receita_atual     := r.ID_RECEITA;
            v_medicamento_atual := r.ID_MEDICAMENTO;
            v_nome_atual        := r.NM_MEDICAMENTO;
            v_qtd_combinacao    := v_qtd_combinacao + r.QT_MEDICAMENTO;
            v_subtotal          := v_subtotal + r.QT_MEDICAMENTO;
            v_total_geral       := v_total_geral + r.QT_MEDICAMENTO;
            v_qtd_linhas        := v_qtd_linhas + 1;
        END LOOP;

        -- Excecao 2: nenhum item de receita para somar
        IF v_qtd_linhas = 0 THEN
            RAISE NO_DATA_FOUND;
        END IF;

        fecha_combinacao;
        fecha_receita;
        escreve(RPAD(' ', 12) || RPAD('Total Geral', 40) || TO_CHAR(v_total_geral));

    EXCEPTION
        WHEN e_quantidade_invalida THEN
            RAISE_APPLICATION_ERROR(-20031, 'PRC_RESUMO_RECEITAS: item de receita com quantidade invalida (menor que 1).');
        WHEN NO_DATA_FOUND THEN
            escreve('AVISO: nao ha medicamentos em receitas para resumir.');
        -- Excecao 3: erro de conversao ou aritmetico
        WHEN VALUE_ERROR THEN
            RAISE_APPLICATION_ERROR(-20032, 'PRC_RESUMO_RECEITAS: valor invalido ao somar as quantidades.');
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20039, 'PRC_RESUMO_RECEITAS: erro inesperado - ' || SQLERRM);
    END prc_resumo_receitas;


    -- (privadas) montagem dos documentos aninhados do dataset
    FUNCTION fn_json_itens_exame(p_exame_id IN NUMBER) RETURN VARCHAR2
    IS
        v_array VARCHAR2(32767) := '[';
    BEGIN
        FOR i IN (SELECT em.TB_MEDICAMENTO_ID_MEDICAMENTO AS ID_MEDICAMENTO, m.NM_MEDICAMENTO,
                         em.QT_MEDICAMENTO, em.DS_POSOLOGIA
                  FROM   TB_EXAME_MEDICAMENTO em
                  JOIN   TB_MEDICAMENTO m ON m.ID_MEDICAMENTO = em.TB_MEDICAMENTO_ID_MEDICAMENTO
                  WHERE  em.TB_EXAME_ID_EXAME = p_exame_id
                  ORDER BY em.ID_PEDIDO) LOOP
            IF v_array <> '[' THEN
                v_array := v_array || ',';
            END IF;
            v_array := v_array || fn_monta_json(
                t_lista('medicamento_id', 'nome', 'quantidade', 'posologia'),
                t_lista(TO_CHAR(i.ID_MEDICAMENTO), i.NM_MEDICAMENTO, TO_CHAR(i.QT_MEDICAMENTO), i.DS_POSOLOGIA),
                t_lista('N', 'S', 'N', 'S'));
        END LOOP;
        RETURN v_array || ']';
    END fn_json_itens_exame;

    FUNCTION fn_json_itens_receita(p_receita_id IN NUMBER) RETURN VARCHAR2
    IS
        v_array VARCHAR2(32767) := '[';
    BEGIN
        FOR i IN (SELECT rm.TB_MEDICAMENTO_ID_MEDICAMENTO AS ID_MEDICAMENTO, m.NM_MEDICAMENTO,
                         rm.QT_MEDICAMENTO, rm.DS_POSOLOGIA
                  FROM   TB_RECEITA_MEDICAMENTO rm
                  JOIN   TB_MEDICAMENTO m ON m.ID_MEDICAMENTO = rm.TB_MEDICAMENTO_ID_MEDICAMENTO
                  WHERE  rm.TB_RECEITA_ID_RECEITA = p_receita_id
                  ORDER BY rm.ID_PEDIDO) LOOP
            IF v_array <> '[' THEN
                v_array := v_array || ',';
            END IF;
            v_array := v_array || fn_monta_json(
                t_lista('medicamento_id', 'nome', 'quantidade', 'posologia'),
                t_lista(TO_CHAR(i.ID_MEDICAMENTO), i.NM_MEDICAMENTO, TO_CHAR(i.QT_MEDICAMENTO), i.DS_POSOLOGIA),
                t_lista('N', 'S', 'N', 'S'));
        END LOOP;
        RETURN v_array || ']';
    END fn_json_itens_receita;

    FUNCTION fn_json_exames(p_consulta_id IN NUMBER) RETURN VARCHAR2
    IS
        v_array VARCHAR2(32767) := '[';
    BEGIN
        FOR e IN (SELECT ID_EXAME, DS_EXAME, DS_URL
                  FROM   TB_EXAME
                  WHERE  TB_CONSULTA_ID_CONSULTA = p_consulta_id
                  ORDER BY ID_EXAME) LOOP
            IF v_array <> '[' THEN
                v_array := v_array || ',';
            END IF;
            v_array := v_array || fn_monta_json(
                t_lista('id', 'descricao', 'url', 'medicamentos'),
                t_lista(TO_CHAR(e.ID_EXAME), e.DS_EXAME, e.DS_URL, fn_json_itens_exame(e.ID_EXAME)),
                t_lista('N', 'S', 'S', 'J'));
        END LOOP;
        RETURN v_array || ']';
    END fn_json_exames;

    FUNCTION fn_json_receitas(p_consulta_id IN NUMBER) RETURN VARCHAR2
    IS
        v_array VARCHAR2(32767) := '[';
    BEGIN
        FOR r IN (SELECT ID_RECEITA, DS_RECEITA
                  FROM   TB_RECEITA
                  WHERE  TB_CONSULTA_ID_CONSULTA = p_consulta_id
                  ORDER BY ID_RECEITA) LOOP
            IF v_array <> '[' THEN
                v_array := v_array || ',';
            END IF;
            v_array := v_array || fn_monta_json(
                t_lista('id', 'descricao', 'medicamentos'),
                t_lista(TO_CHAR(r.ID_RECEITA), r.DS_RECEITA, fn_json_itens_receita(r.ID_RECEITA)),
                t_lista('N', 'S', 'J'));
        END LOOP;
        RETURN v_array || ']';
    END fn_json_receitas;

    FUNCTION fn_json_consultas(p_pet_id IN NUMBER) RETURN VARCHAR2
    IS
        v_array VARCHAR2(32767) := '[';
    BEGIN
        FOR c IN (SELECT c.ID_CONSULTA,
                         TO_CHAR(c.DT_CONSULTA, 'YYYY-MM-DD"T"HH24:MI:SS') AS DT_CONSULTA,
                         v.ID_VETERINARIO, v.DS_EMAIL AS VET_EMAIL
                  FROM   TB_CONSULTA c
                  JOIN   TB_VETERINARIO v ON v.ID_VETERINARIO = c.TB_VETERINARIO_ID_VETERINARIO
                  WHERE  c.TB_PET_ID_PET = p_pet_id
                  ORDER BY c.DT_CONSULTA) LOOP
            IF v_array <> '[' THEN
                v_array := v_array || ',';
            END IF;
            v_array := v_array || fn_monta_json(
                t_lista('id', 'data_consulta', 'veterinario', 'exames', 'receitas'),
                t_lista(TO_CHAR(c.ID_CONSULTA), c.DT_CONSULTA,
                        fn_monta_json(t_lista('id', 'email'),
                                      t_lista(TO_CHAR(c.ID_VETERINARIO), c.VET_EMAIL),
                                      t_lista('N', 'S')),
                        fn_json_exames(c.ID_CONSULTA),
                        fn_json_receitas(c.ID_CONSULTA)),
                t_lista('N', 'S', 'J', 'J', 'J'));
        END LOOP;
        RETURN v_array || ']';
    END fn_json_consultas;

    FUNCTION fn_json_pets(p_tutor_id IN NUMBER) RETURN VARCHAR2
    IS
        v_array VARCHAR2(32767) := '[';
    BEGIN
        -- TO_CHAR do FL_CASTRADO: TRUE ou FALSE no Oracle 23ai (BOOLEAN), 1 ou 0 no 19c (NUMBER)
        FOR p IN (SELECT ID_PET, NM_PET, DS_RACA, DS_PESO, DS_IDADE, TO_CHAR(FL_CASTRADO) AS FL_CASTRADO
                  FROM   TB_PET
                  WHERE  TB_TUTOR_ID_TUTOR = p_tutor_id
                  ORDER BY ID_PET) LOOP
            IF v_array <> '[' THEN
                v_array := v_array || ',';
            END IF;
            v_array := v_array || fn_monta_json(
                t_lista('id', 'nome', 'raca', 'peso', 'idade', 'castrado', 'consultas'),
                t_lista(TO_CHAR(p.ID_PET), p.NM_PET, p.DS_RACA, TO_CHAR(p.DS_PESO), TO_CHAR(p.DS_IDADE),
                        p.FL_CASTRADO, fn_json_consultas(p.ID_PET)),
                t_lista('N', 'S', 'S', 'N', 'N', 'B', 'J'));
        END LOOP;
        RETURN v_array || ']';
    END fn_json_pets;


    PROCEDURE prc_exportar_dataset(p_json OUT CLOB)
    IS
        -- um elemento por linha: a quebra e um espaco valido no JSON e facilita a leitura do arquivo
        c_separador CONSTANT VARCHAR2(2) := ',' || CHR(10);
        v_qtd       PLS_INTEGER := 0;
    BEGIN
        p_json := '{"origem":"Oracle - PKG_VETI.PRC_EXPORTAR_DATASET","gerado_em":"'
                  || TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD"T"HH24:MI:SS') || '",' || CHR(10)
                  || '"tutores":[' || CHR(10);

        FOR t IN (SELECT ID_TUTOR, NM_TUTOR, DS_CPF, DS_EMAIL, DS_TELEFONE FROM TB_TUTOR ORDER BY ID_TUTOR) LOOP
            IF v_qtd > 0 THEN
                p_json := p_json || c_separador;
            END IF;
            p_json := p_json || fn_monta_json(
                t_lista('id', 'nome', 'cpf', 'email', 'telefone', 'pets'),
                t_lista(TO_CHAR(t.ID_TUTOR), t.NM_TUTOR, t.DS_CPF, t.DS_EMAIL, t.DS_TELEFONE, fn_json_pets(t.ID_TUTOR)),
                t_lista('N', 'S', 'S', 'S', 'S', 'J'));
            v_qtd := v_qtd + 1;
        END LOOP;

        -- Excecao 1: sem tutores nao ha dataset (devolve a estrutura vazia e avisa no DBMS_OUTPUT)
        IF v_qtd = 0 THEN
            RAISE NO_DATA_FOUND;
        END IF;

        -- catalogo de veterinarios: a senha (hash) nao sai do banco relacional
        p_json := p_json || CHR(10) || '],' || CHR(10) || '"veterinarios":[' || CHR(10);
        v_qtd := 0;
        FOR v IN (SELECT ID_VETERINARIO, DS_EMAIL, DS_ROLE FROM TB_VETERINARIO ORDER BY ID_VETERINARIO) LOOP
            IF v_qtd > 0 THEN
                p_json := p_json || c_separador;
            END IF;
            p_json := p_json || fn_monta_json(t_lista('id', 'email', 'perfil'),
                                              t_lista(TO_CHAR(v.ID_VETERINARIO), v.DS_EMAIL, v.DS_ROLE),
                                              t_lista('N', 'S', 'S'));
            v_qtd := v_qtd + 1;
        END LOOP;

        p_json := p_json || CHR(10) || '],' || CHR(10) || '"medicamentos":[' || CHR(10);
        v_qtd := 0;
        FOR m IN (SELECT ID_MEDICAMENTO, NM_MEDICAMENTO, DS_MEDICAMENTO FROM TB_MEDICAMENTO ORDER BY ID_MEDICAMENTO) LOOP
            IF v_qtd > 0 THEN
                p_json := p_json || c_separador;
            END IF;
            p_json := p_json || fn_monta_json(t_lista('id', 'nome', 'descricao'),
                                              t_lista(TO_CHAR(m.ID_MEDICAMENTO), m.NM_MEDICAMENTO, m.DS_MEDICAMENTO),
                                              t_lista('N', 'S', 'S'));
            v_qtd := v_qtd + 1;
        END LOOP;

        p_json := p_json || CHR(10) || ']' || CHR(10) || '}';
        prc_imprime(p_json);

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_json := '{"tutores":[],"veterinarios":[],"medicamentos":[]}';
            DBMS_OUTPUT.PUT_LINE('AVISO: nao ha tutores cadastrados para exportar.');
        -- Excecao 2: valor incompativel ou documento de um tutor acima do tamanho maximo
        WHEN VALUE_ERROR THEN
            RAISE_APPLICATION_ERROR(-20041, 'PRC_EXPORTAR_DATASET: valor invalido ou documento acima do tamanho maximo.');
        -- Excecao 3: qualquer outro erro (inclui os erros da FN_MONTA_JSON)
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20049, 'PRC_EXPORTAR_DATASET: erro inesperado - ' || SQLERRM);
    END prc_exportar_dataset;


    PROCEDURE prc_registrar_auditoria(
        p_tipo_operacao      IN VARCHAR2,
        p_valores_anteriores IN VARCHAR2,
        p_valores_novos      IN VARCHAR2
    )
    IS
        e_tipo_invalido EXCEPTION;
        e_sem_valores   EXCEPTION;
    BEGIN
        -- Excecao 1: somente operacoes DML sao auditadas
        IF p_tipo_operacao IS NULL OR p_tipo_operacao NOT IN ('INSERT', 'UPDATE', 'DELETE') THEN
            RAISE e_tipo_invalido;
        END IF;

        -- Excecao 2: toda operacao precisa ter ao menos os valores anteriores ou os novos
        IF p_valores_anteriores IS NULL AND p_valores_novos IS NULL THEN
            RAISE e_sem_valores;
        END IF;

        -- usa o CLIENT_IDENTIFIER da sessao quando houver; sem ele, o usuario do banco
        INSERT INTO TB_AUDITORIA (NM_USUARIO, TP_OPERACAO, DT_OPERACAO, DS_VALORES_ANTERIORES, DS_VALORES_NOVOS)
        VALUES (NVL(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), SYS_CONTEXT('USERENV', 'SESSION_USER')),
                p_tipo_operacao, SYSTIMESTAMP, p_valores_anteriores, p_valores_novos);

    EXCEPTION
        WHEN e_tipo_invalido THEN
            RAISE_APPLICATION_ERROR(-20051, 'PRC_REGISTRAR_AUDITORIA: tipo de operacao invalido (' || p_tipo_operacao || ').');
        WHEN e_sem_valores THEN
            RAISE_APPLICATION_ERROR(-20052, 'PRC_REGISTRAR_AUDITORIA: operacao sem valores anteriores nem novos.');
        -- Excecao 3: valores maiores que as colunas de auditoria
        WHEN VALUE_ERROR THEN
            RAISE_APPLICATION_ERROR(-20053, 'PRC_REGISTRAR_AUDITORIA: valores excedem o tamanho da coluna de auditoria.');
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20059, 'PRC_REGISTRAR_AUDITORIA: erro inesperado - ' || SQLERRM);
    END prc_registrar_auditoria;

END PKG_VETI;
/

-- BLOCO 3: TRIGGER DE AUDITORIA (DML) EM TB_CONSULTA
-- A trigger so coleta OLD e NEW (trigger nao pode ficar dentro de package) e delega a gravacao ao PKG_VETI.
-- Se a auditoria falhar, o erro sobe e a operacao em TB_CONSULTA e desfeita: nenhuma alteracao fica sem registro.
CREATE OR REPLACE TRIGGER TRG_AUDITORIA_CONSULTA
AFTER INSERT OR UPDATE OR DELETE ON TB_CONSULTA
FOR EACH ROW
DECLARE
    v_tipo_operacao VARCHAR2(10);
    v_valores_old   VARCHAR2(4000);
    v_valores_new   VARCHAR2(4000);
BEGIN
    IF INSERTING THEN
        v_tipo_operacao := 'INSERT';
    ELSIF UPDATING THEN
        v_tipo_operacao := 'UPDATE';
    ELSE
        v_tipo_operacao := 'DELETE';
    END IF;

    -- valores anteriores (:OLD) em UPDATE e DELETE
    IF UPDATING OR DELETING THEN
        v_valores_old := 'ID_CONSULTA=' || :OLD.ID_CONSULTA
                      || ';DT_CONSULTA=' || TO_CHAR(:OLD.DT_CONSULTA, 'YYYY-MM-DD HH24:MI')
                      || ';ID_PET=' || :OLD.TB_PET_ID_PET
                      || ';ID_VETERINARIO=' || :OLD.TB_VETERINARIO_ID_VETERINARIO;
    END IF;

    -- valores novos (:NEW) em INSERT e UPDATE
    IF INSERTING OR UPDATING THEN
        v_valores_new := 'ID_CONSULTA=' || :NEW.ID_CONSULTA
                      || ';DT_CONSULTA=' || TO_CHAR(:NEW.DT_CONSULTA, 'YYYY-MM-DD HH24:MI')
                      || ';ID_PET=' || :NEW.TB_PET_ID_PET
                      || ';ID_VETERINARIO=' || :NEW.TB_VETERINARIO_ID_VETERINARIO;
    END IF;

    PKG_VETI.prc_registrar_auditoria(v_tipo_operacao, v_valores_old, v_valores_new);
END;
/

-- BLOCO 4: VERIFICACAO (erros de compilacao de PL-SQL nao geram excecao no JDBC; aqui viram erro)
DECLARE
    v_erros VARCHAR2(4000);
BEGIN
    FOR r IN (SELECT o.OBJECT_NAME, o.OBJECT_TYPE
              FROM   USER_OBJECTS o
              WHERE  o.OBJECT_NAME IN ('PKG_VETI', 'TRG_AUDITORIA_CONSULTA')
              AND    o.STATUS <> 'VALID') LOOP
        v_erros := v_erros || r.OBJECT_TYPE || ' ' || r.OBJECT_NAME || ': ';
        FOR e IN (SELECT LINE, TEXT FROM USER_ERRORS
                  WHERE NAME = r.OBJECT_NAME AND TYPE = r.OBJECT_TYPE
                  ORDER BY SEQUENCE) LOOP
            v_erros := SUBSTR(v_erros || 'linha ' || e.LINE || ' ' || e.TEXT || '; ', 1, 3900);
        END LOOP;
    END LOOP;

    IF v_erros IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(-20900, 'Objetos PL-SQL invalidos - ' || v_erros);
    END IF;
END;
/

--------------------------------------------------------------------------------
-- BLOCO 5: TESTES E EXECUCOES PARA OS PRINTS (saida na aba DBMS Output / Saida de Script)
--------------------------------------------------------------------------------

-- 5.0) Todos os objetos devem estar VALID
SELECT object_name, object_type, status
FROM   user_objects
WHERE  object_name IN ('PKG_VETI', 'TRG_AUDITORIA_CONSULTA')
ORDER BY object_name, object_type;

-- 5.1) Procedimento 1 - JOIN de consulta, pet, tutor e veterinario convertido em JSON pela FN_MONTA_JSON
DECLARE
    v_json CLOB;
BEGIN
    PKG_VETI.prc_consultas_json(v_json);
END;
/

-- 5.2) Procedimento 2 - soma manual de QT_MEDICAMENTO por receita e medicamento, subtotal e total geral
DECLARE
    v_resumo CLOB;
BEGIN
    PKG_VETI.prc_resumo_receitas(v_resumo);
END;
/

-- 5.3) Procedimento extra - contagem de consultas por veterinario e pet, subtotal e total geral
DECLARE
    v_resumo CLOB;
BEGIN
    PKG_VETI.prc_resumo_consultas(v_resumo);
END;
/

-- 5.4) Funcao 1 - FN_MONTA_JSON: caso valido (texto com aspas, numero, booleano e JSON aninhado) e excecao tratada
DECLARE
    v_json VARCHAR2(4000);
BEGIN
    v_json := PKG_VETI.fn_monta_json(
        PKG_VETI.t_lista('nome', 'peso', 'castrado', 'tutor'),
        PKG_VETI.t_lista('Rex "o bravo"', '32.5', '1', '{"id":1}'),
        PKG_VETI.t_lista('S', 'N', 'B', 'J'));
    DBMS_OUTPUT.PUT_LINE('FN_MONTA_JSON (caso valido): ' || v_json);

    BEGIN
        v_json := PKG_VETI.fn_monta_json(PKG_VETI.t_lista('nome', 'peso'), PKG_VETI.t_lista('Rex'));
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('FN_MONTA_JSON (excecao esperada): ' || SQLERRM);
    END;
END;
/

-- 5.5) Funcao 2 - FN_VALIDA_SENHA: senha valida e excecoes tratadas
BEGIN
    DBMS_OUTPUT.PUT_LINE('Senha "Veti2026":  ' || PKG_VETI.fn_valida_senha('Veti2026'));
    DBMS_OUTPUT.PUT_LINE('Senha "abc":       ' || PKG_VETI.fn_valida_senha('abc'));
    DBMS_OUTPUT.PUT_LINE('Senha "abcdefgh":  ' || PKG_VETI.fn_valida_senha('abcdefgh'));
    DBMS_OUTPUT.PUT_LINE('Senha "12345678":  ' || PKG_VETI.fn_valida_senha('12345678'));
    DBMS_OUTPUT.PUT_LINE('Senha nula:        ' || PKG_VETI.fn_valida_senha(NULL));
END;
/

-- 5.6) Excecoes tratadas nas procedures: com as tabelas vazias cada procedure cai no seu tratamento
-- (NO_DATA_FOUND). O ROLLBACK ao final devolve todos os dados, inclusive a auditoria gerada pelos DELETEs.
DECLARE
    v_clob CLOB;
BEGIN
    SAVEPOINT antes_dos_testes;

    DELETE FROM TB_RECEITA_MEDICAMENTO;
    DELETE FROM TB_EXAME_MEDICAMENTO;
    DELETE FROM TB_RECEITA;
    DELETE FROM TB_EXAME;
    DELETE FROM TB_CONSULTA;
    DELETE FROM TB_PET;
    DELETE FROM TB_TUTOR;

    PKG_VETI.prc_consultas_json(v_clob);
    DBMS_OUTPUT.PUT_LINE('PRC_CONSULTAS_JSON devolveu: ' || v_clob);
    PKG_VETI.prc_resumo_receitas(v_clob);
    PKG_VETI.prc_resumo_consultas(v_clob);
    PKG_VETI.prc_exportar_dataset(v_clob);
    DBMS_OUTPUT.PUT_LINE('PRC_EXPORTAR_DATASET devolveu: ' || v_clob);

    ROLLBACK TO antes_dos_testes;
END;
/

-- 5.7) Trigger de auditoria: INSERT, UPDATE e DELETE em TB_CONSULTA e SELECT em TB_AUDITORIA
INSERT INTO TB_CONSULTA (ID_CONSULTA, DT_CONSULTA, TB_PET_ID_PET, TB_VETERINARIO_ID_VETERINARIO)
VALUES (SQ_CONSULTA.NEXTVAL, TIMESTAMP '2026-11-25 15:00:00', 2, 3);

UPDATE TB_CONSULTA SET DT_CONSULTA = TIMESTAMP '2026-11-26 15:30:00'
WHERE  DT_CONSULTA = TIMESTAMP '2026-11-25 15:00:00' AND TB_VETERINARIO_ID_VETERINARIO = 3;

DELETE FROM TB_CONSULTA
WHERE  DT_CONSULTA = TIMESTAMP '2026-11-26 15:30:00' AND TB_VETERINARIO_ID_VETERINARIO = 3;

COMMIT;

SELECT ID_AUDITORIA, NM_USUARIO, TP_OPERACAO, DT_OPERACAO, DS_VALORES_ANTERIORES, DS_VALORES_NOVOS
FROM   TB_AUDITORIA
ORDER BY ID_AUDITORIA;

-- 5.8) Excecao tratada na gravacao da auditoria (operacao fora de INSERT, UPDATE e DELETE)
BEGIN
    PKG_VETI.prc_registrar_auditoria('MERGE', NULL, 'teste');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('PRC_REGISTRAR_AUDITORIA (excecao esperada): ' || SQLERRM);
END;
/

-- 5.9) Exportacao do dataset para o MongoDB: a saida abaixo e o conteudo do arquivo veti_dataset.json.
-- Pela API: GET /veti/relatorios/dataset-json?download=true (ou o botao "Exportar dataset JSON" em Relatorios).
DECLARE
    v_json CLOB;
BEGIN
    PKG_VETI.prc_exportar_dataset(v_json);
END;
/

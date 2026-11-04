
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

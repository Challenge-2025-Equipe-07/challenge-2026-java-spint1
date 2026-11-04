# Modelo de dados (MER/DER) — Veti

Banco: **Oracle Database 23ai**. As tabelas e sequences são criadas pelo Hibernate a partir das entidades em [`src/main/java/br/com/fiap/veti/entity`](../src/main/java/br/com/fiap/veti/entity). A `TB_AUDITORIA`, o package `PKG_VETI` e a trigger `TRG_AUDITORIA_CONSULTA` vêm de [`pkg_veti.sql`](../src/main/resources/db/pkg_veti.sql).

- Modelo físico (DDL gerado pelo Hibernate): [`ddl-oracle.sql`](ddl-oracle.sql)
- Objetos PL-SQL: [`pkg_veti.sql`](../src/main/resources/db/pkg_veti.sql)

## Diagrama entidade-relacionamento

```mermaid
erDiagram
    TB_TUTOR ||--o{ TB_PET : "é responsável por"
    TB_PET ||--o{ TB_CONSULTA : "é atendido em"
    TB_VETERINARIO ||--o{ TB_CONSULTA : "realiza"
    TB_CONSULTA ||--o{ TB_EXAME : "solicita"
    TB_CONSULTA ||--o{ TB_RECEITA : "gera"
    TB_EXAME ||--o{ TB_EXAME_MEDICAMENTO : "prevê"
    TB_MEDICAMENTO ||--o{ TB_EXAME_MEDICAMENTO : "é usado em"
    TB_RECEITA ||--o{ TB_RECEITA_MEDICAMENTO : "prescreve"
    TB_MEDICAMENTO ||--o{ TB_RECEITA_MEDICAMENTO : "é prescrito em"
    TB_CONSULTA ||..o{ TB_AUDITORIA : "auditada por (trigger)"

    TB_TUTOR {
        NUMBER ID_TUTOR PK "SQ_TUTOR"
        VARCHAR2_300 NM_TUTOR "NOT NULL"
        VARCHAR2_11 DS_CPF UK "NOT NULL"
        VARCHAR2_320 DS_EMAIL UK "NOT NULL"
        VARCHAR2_13 DS_TELEFONE
    }

    TB_PET {
        NUMBER ID_PET PK "SQ_PET"
        VARCHAR2_100 NM_PET "NOT NULL"
        VARCHAR2_100 DS_RACA "NOT NULL"
        NUMBER_5_2 DS_PESO "NOT NULL"
        NUMBER DS_IDADE "NOT NULL, 0 a 99"
        BOOLEAN FL_CASTRADO "NOT NULL"
        NUMBER TB_TUTOR_ID_TUTOR FK "NOT NULL"
    }

    TB_VETERINARIO {
        NUMBER ID_VETERINARIO PK "SQ_VETERINARIO"
        VARCHAR2_320 DS_EMAIL UK "NOT NULL"
        VARCHAR2_100 DS_PASSWORD "NOT NULL, hash BCrypt"
        VARCHAR2_20 DS_ROLE "NOT NULL, USER ou ADMIN"
    }

    TB_CONSULTA {
        NUMBER ID_CONSULTA PK "SQ_CONSULTA"
        TIMESTAMP DT_CONSULTA "NOT NULL"
        NUMBER TB_PET_ID_PET FK "NOT NULL"
        NUMBER TB_VETERINARIO_ID_VETERINARIO FK "NOT NULL"
    }

    TB_EXAME {
        NUMBER ID_EXAME PK "SQ_EXAME"
        VARCHAR2_100 DS_EXAME "NOT NULL"
        VARCHAR2_500 DS_URL
        NUMBER TB_CONSULTA_ID_CONSULTA FK "NOT NULL"
    }

    TB_MEDICAMENTO {
        NUMBER ID_MEDICAMENTO PK "SQ_MEDICAMENTO"
        VARCHAR2_100 NM_MEDICAMENTO "NOT NULL"
        VARCHAR2_400 DS_MEDICAMENTO "NOT NULL"
    }

    TB_EXAME_MEDICAMENTO {
        NUMBER ID_PEDIDO PK "SQ_EXAME_MEDICAMENTO"
        NUMBER QT_MEDICAMENTO "NOT NULL, 1 a 99"
        VARCHAR2_1000 DS_POSOLOGIA "NOT NULL"
        NUMBER TB_EXAME_ID_EXAME FK "NOT NULL"
        NUMBER TB_MEDICAMENTO_ID_MEDICAMENTO FK "NOT NULL"
    }

    TB_RECEITA {
        NUMBER ID_RECEITA PK "SQ_RECEITA"
        VARCHAR2_500 DS_RECEITA "NOT NULL"
        NUMBER TB_CONSULTA_ID_CONSULTA FK "NOT NULL, indexada"
    }

    TB_RECEITA_MEDICAMENTO {
        NUMBER ID_PEDIDO PK "SQ_RECEITA_MEDICAMENTO"
        NUMBER QT_MEDICAMENTO "NOT NULL, 1 a 99"
        VARCHAR2_1000 DS_POSOLOGIA "NOT NULL"
        NUMBER TB_RECEITA_ID_RECEITA FK "NOT NULL"
        NUMBER TB_MEDICAMENTO_ID_MEDICAMENTO FK "NOT NULL"
    }

    TB_AUDITORIA {
        NUMBER ID_AUDITORIA PK "IDENTITY"
        VARCHAR2_100 NM_USUARIO "NOT NULL"
        VARCHAR2_10 TP_OPERACAO "INSERT, UPDATE ou DELETE"
        TIMESTAMP DT_OPERACAO "NOT NULL"
        VARCHAR2_4000 DS_VALORES_ANTERIORES
        VARCHAR2_4000 DS_VALORES_NOVOS
    }
```

## Entidades e regras

| Tabela | Descrição | Regras principais |
|---|---|---|
| `TB_TUTOR` | Responsável pelo pet | CPF (11 dígitos) e e-mail únicos |
| `TB_PET` | Animal acompanhado pela clínica | Pertence a um tutor; peso > 0; idade de 0 a 99 |
| `TB_VETERINARIO` | Conta de acesso da equipe | E-mail único; senha em BCrypt validada antes pela `PKG_VETI.FN_VALIDA_SENHA`; perfil USER ou ADMIN |
| `TB_CONSULTA` | Atendimento agendado | Um veterinário não pode ter duas consultas no mesmo horário (`TB_CON_DT_VET_UN`) |
| `TB_EXAME` | Exame pedido numa consulta | — |
| `TB_MEDICAMENTO` | Catálogo de medicamentos | — |
| `TB_EXAME_MEDICAMENTO` | Medicamento associado a um exame | Quantidade de 1 a 99 |
| `TB_RECEITA` | Receita emitida numa consulta | — |
| `TB_RECEITA_MEDICAMENTO` | Itens da receita | Quantidade de 1 a 99 |
| `TB_AUDITORIA` | Histórico de alterações em consultas | Preenchida pela trigger `TRG_AUDITORIA_CONSULTA`; sem FK, para guardar também exclusões |

## Objetos PL-SQL (`PKG_VETI`)

| Objeto | Tipo | Uso na aplicação |
|---|---|---|
| `FN_MONTA_JSON` | Função | Monta JSON manualmente (sem funções nativas de JSON); usada pela `PRC_CONSULTAS_JSON` |
| `FN_VALIDA_SENHA` | Função | Chamada no cadastro de veterinário (`VeterinarioService.cadastrar`) |
| `PRC_CONSULTAS_JSON` | Procedure | `GET /relatorios/consultas-json` e tela **Relatórios** |
| `PRC_RESUMO_CONSULTAS` | Procedure | `GET /relatorios/resumo-consultas` e tela **Relatórios** |
| `TRG_AUDITORIA_CONSULTA` | Trigger | Grava `TB_AUDITORIA`; exibida em `GET /relatorios/auditoria-consultas` |

# Entrega de Banco de Dados – Sprint 4

Mastering Relational and Non-Relational Database · CLYVO VET (aplicação Veti) · 2TDSPW

O banco relacional é o mesmo usado pelo backend Java. O Hibernate cria as tabelas, e a API aplica o [pkg_veti.sql](../src/main/resources/db/pkg_veti.sql) na subida. As procedures do package são chamadas pela aplicação.

## Arquivos

| Entregável | Arquivo |
|---|---|
| 1. Modelos lógico e físico | [docs/der.md](../docs/der.md) (DER) e [docs/ddl-oracle.sql](../docs/ddl-oracle.sql) (DDL do Hibernate). **O PDF exportado do Data Modeler ainda precisa ser gerado.** |
| 1. Codificação (SQL) | [2TDSPW_2026_CodigoSql_CLYVOVET.sql](2TDSPW_2026_CodigoSql_CLYVOVET.sql): script único com tabelas, comentários, carga (5 ou mais registros por tabela), package, trigger e testes |
| 2. Empacotamento com exceções | Package `PKG_VETI` e trigger `TRG_AUDITORIA_CONSULTA`, no [pkg_veti.sql](../src/main/resources/db/pkg_veti.sql) e no bloco 4 do script único |
| 3. JSON gerado do banco relacional | [mongodb/veti_dataset.json](mongodb/veti_dataset.json), exportado pela `PRC_EXPORTAR_DATASET` via API |
| 4. Estrutura MongoDB | [mongodb/01_criar_estrutura.js](mongodb/01_criar_estrutura.js) (coleções, validação e índices), [02_importar_dataset.js](mongodb/02_importar_dataset.js) e [03_consultas.js](mongodb/03_consultas.js) |
| 5. Vídeo | Roteiro abaixo |

O script único é gerado com `sh db/gerar_script_entrega.sh`, a partir de [partes/](partes) e do `pkg_veti.sql`. Assim, o package entregue é o mesmo que a API usa. Depois de alterar o package, gere o script de novo.

## Package `PKG_VETI`

| Objeto | O que faz | Exceções tratadas | Chamado pela API em |
|---|---|---|---|
| `FN_MONTA_JSON` (Função 1) | Converte colunas em JSON manualmente, sem funções nativas. Aceita texto, número, booleano e JSON aninhado | listas vazias, tamanhos diferentes, tipo inválido, valor incompatível, JSON aninhado inválido | indiretamente, pelas procedures |
| `FN_VALIDA_SENHA` (Função 2) | Política de senha do veterinário | senha nula, curta, sem número, sem letra | `POST /veterinarios` e cadastro |
| `PRC_CONSULTAS_JSON` (Procedimento 1) | JOIN de consulta, pet, tutor e veterinário, convertido em JSON | sem dados, `VALUE_ERROR`, `OTHERS` | `GET /relatorios/consultas-json` |
| `PRC_RESUMO_RECEITAS` (Procedimento 2) | Soma manual de `QT_MEDICAMENTO` por receita e medicamento, com subtotal e total geral (sem ROLLUP, CUBE ou GROUPING) | quantidade inválida, sem dados, `VALUE_ERROR`, `OTHERS` | `GET /relatorios/resumo-receitas` |
| `PRC_RESUMO_CONSULTAS` | Contagem de consultas por veterinário e pet, com subtotal e total | sem dados, `VALUE_ERROR`, `OTHERS` | `GET /relatorios/resumo-consultas` |
| `PRC_EXPORTAR_DATASET` | Exporta o banco em JSON aninhado para o MongoDB | sem dados, `VALUE_ERROR`, `OTHERS` | `GET /relatorios/dataset-json?download=true` |
| `PRC_REGISTRAR_AUDITORIA` | Grava `TB_AUDITORIA` | tipo inválido, sem valores, `VALUE_ERROR`, `OTHERS` | pela trigger |
| `TRG_AUDITORIA_CONSULTA` | `AFTER INSERT OR UPDATE OR DELETE ON TB_CONSULTA`. O Oracle não aceita trigger dentro de package, então ela coleta `:OLD`/`:NEW` e delega ao package | um erro na auditoria desfaz a operação | toda inclusão, alteração ou exclusão de consulta |

## Como executar

### Oracle (SQL Developer)

Conecte com o usuário do grupo e rode `db/2TDSPW_2026_CodigoSql_CLYVOVET.sql` como script (F5).

* O script funciona no 19c (servidor da FIAP) e no 23ai. `FL_CASTRADO` é `NUMBER(1)` com CHECK, porque o tipo BOOLEAN só existe no 23ai.
* O **bloco 0 apaga as tabelas do Veti** no schema conectado. Não rode no banco de produção da aplicação.
* O bloco 5 gera os prints: os objetos `VALID`, cada procedure, as duas funções, as exceções tratadas, a trigger (INSERT, UPDATE e DELETE com `SELECT` em `TB_AUDITORIA`) e o dataset JSON.
* Logins da carga: `carlos.vet@example.com` com a senha `Veti2026` é ADMIN. Os demais veterinários estão no comentário do bloco 3.

### Aplicação em cima desse banco

Aponte `DB_URL`, `DB_USER` e `DB_PASSWORD` para o schema onde o script rodou e suba a API. Na primeira subida no 23ai, o Hibernate registra um aviso ao tentar trocar `FL_CASTRADO` para BOOLEAN. O aviso é inofensivo: a coluna continua `NUMBER(1)` e a API lê e grava normalmente.

### Exportar o JSON

Entre como ADMIN e vá em **Relatórios → Exportar dataset JSON**. Outra opção é `GET /veti/relatorios/dataset-json?download=true` com o token. Salve o arquivo como `db/mongodb/veti_dataset.json`.

### MongoDB

Funciona com Atlas ou local, a partir da raiz do repositório:

```bash
mongosh "<connection string>" db/mongodb/01_criar_estrutura.js db/mongodb/02_importar_dataset.js db/mongodb/03_consultas.js
```

O resultado fica no banco `veti`, com as coleções `tutores` (prontuário aninhado), `veterinarios` e `medicamentos`. O modelo está descrito em [docs/der.md](../docs/der.md#modelo-de-documentos-mongodb).

## Testado em 04/11/2026

* **Oracle 23ai em container:**
  * O script único rodou duas vezes seguidas sem erro, com todos os objetos `VALID` e os prints esperados.
  * O resumo de receitas fechou com total geral de 71.
* **API Java sobre esse banco:**
  * O `pkg_veti.sql` novo foi aplicado pelo Spring na subida.
  * Login com o hash da carga funcionou, e o pet castrado foi lido e gravado corretamente.
  * Os endpoints novos e o painel responderam, e a coleção Postman do CI passou: 22 requisições e 41 asserções.
* **MongoDB 8.0:**
  * Os 3 scripts rodaram sem erro e os 5 documentos de cada coleção foram importados.
  * O validador recusou um CPF inválido.
  * As somas por medicamento batem com o Oracle (71).
* **Não testado no Oracle 19c da FIAP.** Rode o script lá antes de entregar.

## Roteiro do vídeo (com narração)

1. No SQL Developer, rode o script único e mostre os objetos `VALID` e os prints do bloco 5.
2. Na aplicação, logado como ADMIN, faça as inserções pelo backend:
   * Cadastre um veterinário com a senha `abcdefgh`, que é recusada pela `FN_VALIDA_SENHA`, e depois com `Veti2026`.
   * Cadastre um tutor, um pet e uma consulta.
3. Abra **Relatórios**: mostre a consulta nova no JSON da `PRC_CONSULTAS_JSON`, os dois resumos e a auditoria gravada pela trigger. Mostre também as linhas novas no SQL Developer.
4. Clique em **Exportar dataset JSON** e abra o arquivo.
5. Rode os 3 scripts do MongoDB e mostre as coleções no Compass ou no Atlas.

## Pendências do grupo

- [ ] PDF com os modelos lógico e físico (Data Modeler), incluindo as correções do feedback da Sprint 3.
- [ ] Rodar o script no Oracle da FIAP e tirar os prints.
- [ ] Gravar o vídeo narrado.
- [ ] Confirmar com o professor o padrão de nome dos arquivos.

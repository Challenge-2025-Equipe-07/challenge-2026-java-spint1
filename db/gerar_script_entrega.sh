#!/usr/bin/env sh
# Monta o script unico de entrega da disciplina de Banco de Dados:
#   estrutura e carga (db/partes) + PL-SQL da API (src/main/resources/db/pkg_veti.sql) + testes (db/partes).
# Assim o package entregue e sempre o mesmo que a API aplica na subida.
# Uso (na raiz do repositorio): sh db/gerar_script_entrega.sh
set -e
cd "$(dirname "$0")/.."
cat db/partes/01_estrutura_e_carga.sql \
    src/main/resources/db/pkg_veti.sql \
    db/partes/03_testes.sql > db/2TDSPW_2026_CodigoSql_CLYVOVET.sql
echo "Gerado: db/2TDSPW_2026_CodigoSql_CLYVOVET.sql"

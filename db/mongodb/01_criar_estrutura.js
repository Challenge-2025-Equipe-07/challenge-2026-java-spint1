// -----------------------------------------------------------------------------
// CHALLENGE FIAP 2026 - CLYVO VET (Veti) - Banco de Dados - Sprint 4
// MongoDB 1/3: banco, colecoes com validacao ($jsonSchema) e indices
//
// Execucao: mongosh "<connection string>" db/mongodb/01_criar_estrutura.js
//
// Modelagem de documentos a partir do modelo relacional Oracle:
//   tutores      -> documento raiz (_id = TB_TUTOR.ID_TUTOR). Embute os pets; cada pet embute as
//                   consultas; cada consulta embute o veterinario resumido, os exames e as receitas,
//                   e estes embutem os medicamentos usados (TB_EXAME_MEDICAMENTO e
//                   TB_RECEITA_MEDICAMENTO). E o prontuario do tutor, lido sempre junto e com
//                   crescimento limitado: uma leitura substitui o JOIN de 9 tabelas.
//   veterinarios -> colecao propria: a conta e compartilhada por muitas consultas, que guardam uma
//                   copia resumida {id, email} (padrao "extended reference"). A senha nao e exportada.
//   medicamentos -> catalogo proprio; os itens embutidos guardam medicamento_id e nome para leitura direta
//                   e o catalogo completo e obtido com $lookup quando preciso.
// -----------------------------------------------------------------------------

db = db.getSiblingDB("veti");

// recria a estrutura do zero (permite reexecutar o script)
["tutores", "veterinarios", "medicamentos"].forEach((nome) => {
  if (db.getCollectionNames().includes(nome)) {
    db.getCollection(nome).drop();
  }
});

const itemMedicamento = {
  bsonType: "object",
  required: ["medicamento_id", "nome", "quantidade", "posologia"],
  properties: {
    medicamento_id: { bsonType: "int" },
    nome: { bsonType: "string" },
    quantidade: { bsonType: "int", minimum: 1, maximum: 99 },
    posologia: { bsonType: "string" }
  }
};

db.createCollection("tutores", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "nome", "cpf", "email", "pets"],
      properties: {
        _id: { bsonType: "int", description: "TB_TUTOR.ID_TUTOR" },
        nome: { bsonType: "string", maxLength: 300 },
        cpf: { bsonType: "string", pattern: "^[0-9]{11}$" },
        email: { bsonType: "string", pattern: "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$" },
        telefone: { bsonType: ["string", "null"], maxLength: 13 },
        pets: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["id", "nome", "raca", "peso", "idade", "castrado", "consultas"],
            properties: {
              id: { bsonType: "int" },
              nome: { bsonType: "string" },
              raca: { bsonType: "string" },
              peso: { bsonType: "double", minimum: 0, exclusiveMinimum: true },
              idade: { bsonType: "int", minimum: 0, maximum: 99 },
              castrado: { bsonType: "bool" },
              consultas: {
                bsonType: "array",
                items: {
                  bsonType: "object",
                  required: ["id", "data_consulta", "veterinario", "exames", "receitas"],
                  properties: {
                    id: { bsonType: "int" },
                    data_consulta: { bsonType: "date" },
                    veterinario: {
                      bsonType: "object",
                      required: ["id", "email"],
                      properties: { id: { bsonType: "int" }, email: { bsonType: "string" } }
                    },
                    exames: {
                      bsonType: "array",
                      items: {
                        bsonType: "object",
                        required: ["id", "descricao", "medicamentos"],
                        properties: {
                          id: { bsonType: "int" },
                          descricao: { bsonType: "string", maxLength: 100 },
                          url: { bsonType: ["string", "null"] },
                          medicamentos: { bsonType: "array", items: itemMedicamento }
                        }
                      }
                    },
                    receitas: {
                      bsonType: "array",
                      items: {
                        bsonType: "object",
                        required: ["id", "descricao", "medicamentos"],
                        properties: {
                          id: { bsonType: "int" },
                          descricao: { bsonType: "string", maxLength: 500 },
                          medicamentos: { bsonType: "array", items: itemMedicamento }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});

db.createCollection("veterinarios", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "email", "perfil"],
      properties: {
        _id: { bsonType: "int", description: "TB_VETERINARIO.ID_VETERINARIO" },
        email: { bsonType: "string", pattern: "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$" },
        perfil: { enum: ["USER", "ADMIN"] }
      }
    }
  }
});

db.createCollection("medicamentos", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "nome", "descricao"],
      properties: {
        _id: { bsonType: "int", description: "TB_MEDICAMENTO.ID_MEDICAMENTO" },
        nome: { bsonType: "string", maxLength: 100 },
        descricao: { bsonType: "string", maxLength: 400 }
      }
    }
  }
});

// indices: as restricoes UNIQUE do Oracle e os campos mais consultados
db.tutores.createIndex({ cpf: 1 }, { unique: true, name: "uq_tutores_cpf" });
db.tutores.createIndex({ email: 1 }, { unique: true, name: "uq_tutores_email" });
db.tutores.createIndex({ "pets.id": 1 }, { name: "ix_tutores_pet" });
db.tutores.createIndex({ "pets.consultas.data_consulta": 1 }, { name: "ix_tutores_data_consulta" });
db.tutores.createIndex({ "pets.consultas.veterinario.id": 1 }, { name: "ix_tutores_veterinario" });
db.veterinarios.createIndex({ email: 1 }, { unique: true, name: "uq_veterinarios_email" });
db.medicamentos.createIndex({ nome: 1 }, { name: "ix_medicamentos_nome" });

print("Estrutura criada no banco 'veti':");
db.getCollectionInfos().forEach((c) => print(" - " + c.name));

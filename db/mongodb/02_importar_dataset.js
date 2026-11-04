// -----------------------------------------------------------------------------
// CHALLENGE FIAP 2026 - CLYVO VET (Veti) - Banco de Dados - Sprint 4
// MongoDB 2/3: importacao do JSON exportado do Oracle (veti_dataset.json)
//
// O arquivo vem da procedure PKG_VETI.PRC_EXPORTAR_DATASET, pela API
// (GET /veti/relatorios/dataset-json?download=true) ou pelo SQL Developer (bloco 5.9 do script de entrega).
//
// Execucao (na raiz do repositorio):
//   mongosh "<connection string>" db/mongodb/02_importar_dataset.js
// Outro arquivo:
//   VETI_JSON=/caminho/veti_dataset.json mongosh "<connection string>" db/mongodb/02_importar_dataset.js
//
// Conversoes na importacao:
//   * o "id" do documento raiz vira "_id";
//   * inteiros viram Int32 (o validador exige bsonType "int"); o peso continua double;
//   * data_consulta (texto ISO sem fuso) vira Date.
// -----------------------------------------------------------------------------

const fs = require("fs");

db = db.getSiblingDB("veti");

// procura o arquivo a partir da raiz do repositorio, da pasta db ou da propria pasta mongodb
const caminho = process.env.VETI_JSON ||
  ["./db/mongodb/veti_dataset.json", "./mongodb/veti_dataset.json", "./veti_dataset.json"].find((c) => fs.existsSync(c));
if (!caminho) {
  throw new Error("veti_dataset.json nao encontrado; informe o caminho em VETI_JSON");
}
const dataset = JSON.parse(fs.readFileSync(caminho, "utf8"));

const CAMPOS_DATA = ["data_consulta"];
const CAMPOS_DECIMAIS = ["peso"];

function converter(valor, chave) {
  if (Array.isArray(valor)) {
    return valor.map((item) => converter(item, null));
  }
  if (valor !== null && typeof valor === "object") {
    const novo = {};
    Object.keys(valor).forEach((k) => {
      novo[k] = converter(valor[k], k);
    });
    return novo;
  }
  if (valor !== null && CAMPOS_DATA.includes(chave)) {
    // o Oracle grava o horario local da clinica sem fuso; ele e mantido como esta (UTC no BSON)
    return new Date(valor + "Z");
  }
  if (typeof valor === "number" && CAMPOS_DECIMAIS.includes(chave)) {
    return Double(valor);
  }
  if (typeof valor === "number" && Number.isInteger(valor)) {
    return NumberInt(valor);
  }
  return valor;
}

function paraDocumento(registro) {
  const doc = converter(registro, null);
  doc._id = doc.id;
  delete doc.id;
  return doc;
}

["tutores", "veterinarios", "medicamentos"].forEach((nome) => {
  const docs = (dataset[nome] || []).map(paraDocumento);
  db.getCollection(nome).deleteMany({});
  if (docs.length > 0) {
    const resultado = db.getCollection(nome).insertMany(docs, { ordered: true });
    print(nome + ": " + Object.keys(resultado.insertedIds).length + " documento(s) importado(s)");
  } else {
    print(nome + ": nenhum documento no arquivo");
  }
});

print("Dataset gerado no Oracle em: " + dataset.gerado_em);
print("Exemplo de documento importado (tutor 1):");
printjson(db.tutores.findOne({ _id: 1 }));

// -----------------------------------------------------------------------------
// CHALLENGE FIAP 2026 - CLYVO VET (Veti) - Banco de Dados - Sprint 4
// MongoDB 3/3: consultas de demonstracao sobre a estrutura de documentos
//
// Execucao: mongosh "<connection string>" db/mongodb/03_consultas.js
// -----------------------------------------------------------------------------

db = db.getSiblingDB("veti");

function titulo(texto) {
  print("\n==== " + texto + " ====");
}

// 1) Prontuario completo do tutor em UMA leitura (no Oracle: JOIN de 9 tabelas)
titulo("1. Prontuario completo do tutor pelo CPF");
printjson(db.tutores.findOne({ cpf: "33333333333" }));

// 2) Pets castrados, com o tutor (projecao so dos campos necessarios)
titulo("2. Pets castrados");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $match: { "pets.castrado": true } },
  { $project: { _id: 0, tutor: "$nome", pet: "$pets.nome", raca: "$pets.raca", peso: "$pets.peso" } }
]).forEach(printjson);

// 3) Consultas por veterinario (equivale ao GROUP BY do relacional)
titulo("3. Consultas por veterinario");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $unwind: "$pets.consultas" },
  { $group: { _id: "$pets.consultas.veterinario.email", consultas: { $sum: 1 } } },
  { $sort: { consultas: -1, _id: 1 } }
]).forEach(printjson);

// 4) Quantidade prescrita por medicamento nas receitas (mesma soma da PRC_RESUMO_RECEITAS)
titulo("4. Quantidade prescrita por medicamento");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $unwind: "$pets.consultas" },
  { $unwind: "$pets.consultas.receitas" },
  { $unwind: "$pets.consultas.receitas.medicamentos" },
  {
    $group: {
      _id: "$pets.consultas.receitas.medicamentos.nome",
      quantidade_total: { $sum: "$pets.consultas.receitas.medicamentos.quantidade" },
      itens: { $sum: 1 }
    }
  },
  { $sort: { quantidade_total: -1, _id: 1 } }
]).forEach(printjson);

// 5) Agenda de um periodo (usa o indice em pets.consultas.data_consulta)
titulo("5. Consultas entre 05/10/2026 e 07/10/2026");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $unwind: "$pets.consultas" },
  {
    $match: {
      "pets.consultas.data_consulta": { $gte: ISODate("2026-10-05T00:00:00Z"), $lt: ISODate("2026-10-08T00:00:00Z") }
    }
  },
  { $sort: { "pets.consultas.data_consulta": 1 } },
  {
    $project: {
      _id: 0,
      data: { $dateToString: { format: "%d/%m/%Y %H:%M", date: "$pets.consultas.data_consulta" } },
      pet: "$pets.nome",
      tutor: "$nome",
      veterinario: "$pets.consultas.veterinario.email"
    }
  }
]).forEach(printjson);

// 6) Consultas que ainda nao geraram receita
titulo("6. Consultas sem receita");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $unwind: "$pets.consultas" },
  { $match: { "pets.consultas.receitas": { $size: 0 } } },
  { $project: { _id: 0, consulta: "$pets.consultas.id", pet: "$pets.nome", tutor: "$nome" } }
]).forEach(printjson);

// 7) $lookup: completa os itens das receitas com a descricao do catalogo de medicamentos
titulo("7. Itens de receita com a descricao do catalogo");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $unwind: "$pets.consultas" },
  { $unwind: "$pets.consultas.receitas" },
  { $unwind: "$pets.consultas.receitas.medicamentos" },
  {
    $lookup: {
      from: "medicamentos",
      localField: "pets.consultas.receitas.medicamentos.medicamento_id",
      foreignField: "_id",
      as: "catalogo"
    }
  },
  { $unwind: "$catalogo" },
  {
    $project: {
      _id: 0,
      receita: "$pets.consultas.receitas.descricao",
      medicamento: "$catalogo.nome",
      descricao: "$catalogo.descricao",
      quantidade: "$pets.consultas.receitas.medicamentos.quantidade"
    }
  }
]).forEach(printjson);

// 8) Flexibilidade do modelo: nova consulta embutida no pet Rex (tutor 1, pet 1) e depois removida
titulo("8. Nova consulta embutida no pet Rex");
db.tutores.updateOne(
  { _id: 1, "pets.id": 1 },
  {
    $push: {
      "pets.$.consultas": {
        id: NumberInt(9999),
        data_consulta: ISODate("2026-11-30T10:00:00Z"),
        veterinario: { id: NumberInt(2), email: "fernanda.vet@example.com" },
        exames: [],
        receitas: []
      }
    }
  }
);
printjson(db.tutores.findOne({ _id: 1 }, { _id: 0, nome: 1, "pets.nome": 1, "pets.consultas.id": 1, "pets.consultas.data_consulta": 1 }));

db.tutores.updateOne({ _id: 1, "pets.id": 1 }, { $pull: { "pets.$.consultas": { id: 9999 } } });
print("\nConsulta de demonstracao removida (o dataset continua igual ao do Oracle).");

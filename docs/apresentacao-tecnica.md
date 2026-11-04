# Apresentação técnica do Veti (10 minutos)

Roteiro para apresentar a arquitetura e as decisões técnicas. A demonstração funcional para o cliente está no [README](../README.md#roteiro-de-demonstração-para-o-cliente).

## Deixe aberto antes de começar

1. **Aplicação** logada como administrador, numa janela; uma janela anônima logada como veterinário (USER).
2. **IDE** com o projeto aberto.
3. **Swagger** em `/veti/swagger-ui.html`.
4. **SQL Developer ou DBeaver** conectado ao Oracle, com `SELECT * FROM TB_AUDITORIA ORDER BY ID_AUDITORIA DESC;` pronto.
5. **`/health`** numa aba.
6. **GitHub Actions** (última execução verde do CI), um terminal SSH na EC2 (`docker compose ps`) e o **e-mail** do tutor de exemplo no celular.

## Cronograma

| Tempo | Bloco | Mostrar |
|---|---|---|
| 0:00 – 1:00 | Problema e solução | App aberta na tela Início |
| 1:00 – 2:30 | Arquitetura | Diagrama do README e pacotes na IDE |
| 2:30 – 4:00 | Segurança | `SecurityConfigurations`, login e 403 ao vivo |
| 4:00 – 5:30 | Regras de negócio e validação | `ConsultaService`, conflito de agenda ao vivo |
| 5:30 – 7:30 | Oracle e PL-SQL | `pkg_veti.sql`, `VetiPackageRepository`, tela Relatórios e `TB_AUDITORIA` |
| 7:30 – 8:30 | Integração: e-mail | Notificar tutor e e-mail chegando |
| 8:30 – 9:30 | Qualidade, observabilidade e deploy | Testes, `/health`, logs, GitHub Actions, EC2 |
| 9:30 – 10:00 | Fechamento | Próximos passos |

## Roteiro com falas

### 0:00 – 1:00 · Problema e solução
- Clínicas tratam cada consulta como evento isolado; retornos são esquecidos.
- O Veti organiza tutores, pets e agenda, e lembra o tutor da consulta.
- Uma aplicação **Spring Boot 4 / Java 21** que serve **telas web (Thymeleaf)** e uma **API REST** para o app mobile, com o mesmo código de negócio.

### 1:00 – 2:30 · Arquitetura
- Camadas **controller → service → repository**, com DTOs em `record` e **MapStruct** para converter entidade ↔ DTO.
- Dois tipos de controller sobre os **mesmos services**: `controller/` (REST, JSON) e `controller/web/` (telas). Mostre `ConsultaController` e `ConsultaPainelController` chamando o mesmo `ConsultaService`.
- **Injeção por construtor** (`@RequiredArgsConstructor`), **Spring Cache** (`@Cacheable`/`@CacheEvict`) nas listagens.
- Infra na AWS: **Oracle 23ai**, aplicação e proxy HTTPS (**Caddy**) com **Docker Compose** na mesma EC2, e **CloudFront** para a URL personalizada.

### 2:30 – 4:00 · Segurança
- **Spring Security stateless com JWT**: o mesmo token serve no header `Authorization` (API) e num cookie **HttpOnly SameSite=Lax** (`veti_token`, telas). Mostre `SecurityFilter` e `AuthCookies`.
- Senhas com **BCrypt**; login social opcional com **Google OAuth2**.
- Autorização por perfil em `SecurityConfigurations`: USER agenda e notifica; ADMIN cadastra, gerencia equipe e relatórios.
- **Ao vivo**: na janela do veterinário, abra `/veti/veterinarios/painel` → página 403. No Swagger, chame um endpoint sem token → 403.
- O primeiro ADMIN é criado pelo `AdminSeeder` a partir de variáveis de ambiente; `/auth/register` público só cria USER.

### 4:00 – 5:30 · Regras de negócio e validação
- **Bean Validation** nos records (`@NotBlank`, `@Pattern` para CPF, `@Future` para data) com mensagens em português; na API vira 400 com campo → mensagem, na tela aparece ao lado do campo (`FormErrors`).
- Regras no service: CPF/e-mail únicos, veterinário sem duas consultas no mesmo horário.
- `ControllerAdvice` centraliza os erros: 404, 409 (regra de negócio), 400 (validação), 502 (falha de e-mail).
- **Ao vivo**: agende duas consultas para o mesmo veterinário e horário → *"Veterinário já possui consulta nessa data"*.

### 5:30 – 7:30 · Oracle e PL-SQL (o ponto mais forte)
- O **Hibernate cria as tabelas** (`ddl-auto=update`); as regras do banco ficam num package **`PKG_VETI`**, aplicado automaticamente na subida via `spring.sql.init` depois do Hibernate. O script é idempotente e termina verificando se algum objeto ficou `INVALID`.
- Objetos: `FN_VALIDA_SENHA`, `FN_MONTA_JSON`, `PRC_CONSULTAS_JSON` (OUT CLOB), `PRC_RESUMO_CONSULTAS` (subtotais sem ROLLUP) e a trigger `TRG_AUDITORIA_CONSULTA`.
- Chamada pela API com **`SimpleJdbcCall`** (`VetiPackageRepository`); `RAISE_APPLICATION_ERROR` (ORA-20xxx) vira mensagem de negócio (409).
- **Ao vivo**:
  1. Em Equipe, crie uma conta com senha sem número → mensagem vinda da `FN_VALIDA_SENHA`.
  2. Abra **Relatórios**: resumo e JSON gerados pelas procedures.
  3. Agende uma consulta e rode o `SELECT` em `TB_AUDITORIA` → linha de INSERT gravada pela trigger.
- Modelo documentado em `docs/der.md` (MER/DER) e `docs/ddl-oracle.sql`.

### 7:30 – 8:30 · Integração: e-mail ao tutor
- `EmailService` com `JavaMailSender` (Gmail, SMTP 587 com STARTTLS, senha de app em variável de ambiente), e-mail HTML com versão em texto.
- Sem credenciais, cai em modo simulado (log), o que mantém CI e testes independentes do Gmail; falha do SMTP vira 502 na API e mensagem na tela.
- **Ao vivo**: **Notificar tutor** → mostre o e-mail no celular.

### 8:30 – 9:30 · Qualidade, observabilidade e deploy
- **37 testes unitários** (JUnit 5 + Mockito, padrão AAA) e **testes de API** (Postman/Newman) cobrindo login, permissões, procedures e trigger.
- **Observabilidade**: `/health` com banco e Google (DEGRADED sem derrubar a instância), logs JSON com `traceId`/`correlationId`, tracing OpenTelemetry até o JDBC, métricas no Actuator.
- **CI no GitHub Actions**: a cada push na main, testes â JAR â API contra Oracle em container â Newman. **Deploy na AWS**: na EC2, `git pull` + `docker compose up -d --build`; segredos sÃ³ no `.env` da EC2.

### 9:30 – 10:00 · Fechamento
- Recapitule: arquitetura em camadas, segurança JWT/OAuth2 por perfil, PL-SQL integrado à API, e-mail real, testes e pipeline.
- Próximos passos: lembrete por WhatsApp, plano de cuidado sugerido por IA a partir do histórico do pet, app mobile consumindo a mesma API.

## Perguntas prováveis

| Pergunta | Resposta curta |
|---|---|
| Por que JWT e não sessão? | Mesmo mecanismo para telas e app mobile, sem estado no servidor; o cookie HttpOnly protege o token de JavaScript |
| Por que parte da regra no banco? | Requisito da disciplina de banco e regras que valem para qualquer cliente do banco (auditoria por trigger não depende da API) |
| Como evitam conflito de horário com duas requisições ao mesmo tempo? | Além da checagem no service, há a constraint única `TB_CON_DT_VET_UN` (data + veterinário) no Oracle |
| E se o Gmail cair? | O e-mail falha com mensagem clara (502 na API), o restante do sistema continua; o `/health` não depende do SMTP |
| Por que tudo numa EC2? | Um Ãºnico `docker compose` sobe Oracle, app e HTTPS; simples de demonstrar e barato. Para escalar, o prÃ³ximo passo seria RDS para o banco e ECS para a app |
| Como o front fica atualizado? | Thymeleaf renderiza no servidor; CSS e imagens versionados por hash do conteúdo, sem cache antigo |

## Plano B

- **Sem internet ou EC2 fora**: rode local com `docker compose up --build` (Oracle + app) e siga o mesmo roteiro.
- **E-mail não chega**: mostre a mensagem de sucesso na tela e o log `E-mail enviado para ...`; explique o modo simulado.
- **Oracle lento na primeira subida**: suba tudo 5 minutos antes da apresentação.

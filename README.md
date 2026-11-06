# Veti — gestão de cuidado contínuo para clínicas veterinárias

Solução do **Challenge 2026 (FIAP · 2TDS · Clyvo Vet)** na disciplina **Java Advanced**: uma aplicação Spring Boot (API REST + telas web Thymeleaf) que organiza tutores, pets e consultas de uma clínica e ajuda a equipe a manter o pet em acompanhamento contínuo (agenda, histórico e lembretes ao tutor).

| | |
|---|---|
| **Aplicação online** | https://18-207-27-110.sslip.io/veti/ (AWS: EC2 + Docker + Caddy com HTTPS) |
| **Vídeo da apresentação** | _<preencher com o link do vídeo (máx. 15 min)>_ |
| **Swagger (API)** | https://18-207-27-110.sslip.io/veti/swagger-ui.html |
| **Health check** | https://18-207-27-110.sslip.io/health |

## Integrantes

| Nome                | RM            |
|---------------------|---------------|
| Carlos André        | 564968 |
| Camilo Michelleto   | 564113 |
| Guilherme Costa     | 562700 |
| Laura Silva         | 566376 |

## O problema e a proposta

Clínicas veterinárias costumam tratar cada atendimento como um evento isolado: o histórico do pet fica espalhado, retornos são esquecidos e o tutor só volta quando o animal já está doente. A proposta da Clyvo Vet é transformar isso numa **jornada contínua de cuidado**.

A Veti resolve a parte operacional dessa jornada:

- **Cadastro centralizado** de tutores e pets (raça, peso, idade, castração), ligando cada pet ao responsável.
- **Agenda de consultas** com regra de negócio que impede o mesmo veterinário de ter dois atendimentos no mesmo horário e bloqueia agendamentos no passado.
- **Lembrete ao tutor**: com um clique a equipe envia ao tutor um e-mail com pet, data e veterinário da próxima consulta (Gmail via SMTP). O próximo passo é oferecer também o WhatsApp.
- **Painel inicial** com consultas do dia, próximas consultas e números da clínica.
- **Dois perfis de acesso**: o veterinário (USER) cuida da agenda, e o administrador (ADMIN) cadastra tutores, pets e a equipe.

**Benefícios**: menos faltas e retornos esquecidos, histórico único do pet, agenda sem conflitos e uma base de dados estruturada que pode alimentar recomendações de cuidado (o componente de IA da disciplina de Disruptive Architectures).

## Roteiro de demonstração para o cliente

Passeio por todas as funcionalidades em cerca de **15 minutos**, na ordem em que a clínica usaria o sistema no dia a dia: montar a equipe, cadastrar quem chega, agendar, lembrar o tutor e acompanhar.

### Antes de começar (5 minutos de preparo)

| O quê | Como |
|---|---|
| Aplicação no ar | Abra https://18-207-27-110.sslip.io/veti/ (ou http://localhost:8080/veti/ rodando local) e confira `/health` com `"status":"UP"` |
| Conta de administrador | O `ADMIN_EMAIL`/`ADMIN_PASSWORD` configurado na subida |
| E-mail do tutor de exemplo | Use um e-mail que você consiga abrir na hora (no celular, por exemplo), para mostrar o lembrete chegando. O envio precisa de `MAIL_USERNAME`/`MAIL_PASSWORD` configurados (veja [E-mail do lembrete](#e-mail-do-lembrete-gmail)) |
| Duas janelas | Uma normal (administrador) e uma anônima (veterinário), para alternar entre os perfis sem sair da conta |
| Celular ou modo responsivo | Opcional, para mostrar o layout mobile com a barra de navegação inferior |

Dados sugeridos para digitar durante a demonstração:

| Cadastro | Valores |
|---|---|
| Veterinária | `ana@veti.com`, senha `Veti2026`, perfil Veterinário |
| Tutora | Clara Silva, `<e-mail que você vai abrir>`, CPF `12345678901`, telefone `11999999999` |
| Pet | Rupert, raça Beagle, 12,5 kg, 4 anos, castrado |

### Passo a passo

| # | Tela | O que fazer | O que destacar para o cliente |
|---|---|---|---|
| 1 | **Login** | Entre com a conta de administrador. Se o login Google estiver ativo, mostre o botão **Entrar com Google** | Acesso restrito à equipe; também dá para entrar com a conta Google, sem criar senha |
| 2 | **Início** | Mostre a saudação, o total de consultas do dia e os contadores (agendadas, pets, tutores, equipe) | É a primeira coisa que o veterinário vê pela manhã: quantos atendimentos tem hoje e qual é o próximo |
| 3 | **Equipe** (ícone de engrenagem) | Tente criar a conta da veterinária com a senha `abcdefgh` → aparece *"a senha deve conter ao menos um numero"*. Corrija para `Veti2026` e crie como **Veterinário** | O administrador controla quem acessa e com qual perfil; a política de senha é aplicada pelo próprio banco de dados |
| 4 | **Tutores** | Clique em cadastrar com o formulário vazio para mostrar as mensagens em cada campo. Depois cadastre a Clara. Tente cadastrar de novo com o mesmo CPF → *"CPF já cadastrado"* | Cadastro único do responsável, sem duplicidade; os erros aparecem ao lado do campo, em português |
| 5 | **Pets** | Cadastre o Rupert, escolhendo a Clara como tutora e marcando **O pet é castrado?** | Cada pet fica ligado ao seu responsável, com os dados que importam para o cuidado (raça, peso, idade, castração) |
| 6 | **Consultas** | Agende o Rupert para amanhã com a Ana. Tente agendar outra consulta para a Ana **no mesmo horário** → *"Veterinário já possui consulta nessa data"*. Tente uma data no passado → a tela recusa | A agenda não deixa um veterinário com dois atendimentos ao mesmo tempo nem marcações no passado |
| 7 | **Consultas → Notificar tutor** | Clique em **Notificar tutor** na consulta do Rupert e abra o e-mail no celular | O tutor recebe na hora um lembrete com pet, data e veterinário, com a identidade visual da clínica. Menos faltas e retornos esquecidos |
| 8 | **Início** | Volte ao início: a consulta aparece em destaque como próxima, com o botão **Detalhes do pet** | O painel se atualiza sozinho com a agenda |
| 9 | **Perfil Veterinário** (janela anônima) | Entre como `ana@veti.com`. Mostre que não existem as opções Equipe e Relatórios, e que em Tutores e Pets ela só consulta. Digite `/veti/veterinarios/painel` na barra → *"Você não tem permissão para acessar esta página"* | Cada pessoa vê só o que precisa: o veterinário agenda e notifica, o administrador gerencia cadastros e equipe |
| 10 | **Relatórios** (volte ao administrador) | Mostre o resumo de consultas por veterinário e pet (com subtotais e total), a lista em JSON e o histórico de alterações | Visão gerencial da clínica e **auditoria**: toda consulta criada, alterada ou cancelada fica registrada com quem fez e quando |
| 11 | **Consultas → Cancelar** | Cancele uma consulta e volte em **Relatórios** → o cancelamento aparece no histórico | Nada some sem rastro: o cancelamento também fica registrado |
| 12 | **Celular** (opcional) | Abra a aplicação no celular ou reduza a janela | Mesmo sistema no balcão e no celular do veterinário, com a barra de navegação na parte de baixo |
| 13 | **Sair** | Clique em **Sair** | Encerramento da sessão |

**Para fechar**, lembre o cliente dos ganhos: agenda sem conflitos, histórico único do pet, tutor lembrado automaticamente e uma base organizada que permite sugerir cuidados no futuro (vacinas, vermífugos, check-ups).

Se alguém perguntar sobre integração, a mesma funcionalidade está disponível como API REST documentada no Swagger (`/veti/swagger-ui.html`), pronta para um aplicativo mobile.

## Perfis e permissões

| Ação | Sem login | USER (veterinário) | ADMIN |
|---|---|---|---|
| Ver telas e API de leitura | ❌ (redireciona ao login / 403) | ✅ | ✅ |
| Agendar, notificar e cancelar consultas | ❌ | ✅ | ✅ |
| Cadastrar, alterar e excluir tutores e pets | ❌ | ❌ | ✅ |
| Gerir a equipe (`/veterinarios/**`), relatórios do banco (`/relatorios/**`) e Actuator | ❌ | ❌ | ✅ |
| `POST /auth/register` | cria **sempre** USER | cria USER | pode criar ADMIN |

O primeiro ADMIN é criado na subida da aplicação a partir de `ADMIN_EMAIL`/`ADMIN_PASSWORD`. Contas criadas pelo login com Google entram como USER.

## Arquitetura e decisões técnicas

```
Navegador (Thymeleaf) ─┐                                         ┌─> Repositories (JPA) ─────────┐
                       ├─> Spring Security ─> Controllers ─> Services                          ├─> Oracle 23ai Free
Cliente REST / Mobile ─┘   (JWT + OAuth2)      (web e REST)  (regras, └─> PKG_VETI (SimpleJdbcCall) ┘
                                                              cache, e-mail SMTP)

Produção na AWS (uma EC2, Docker Compose):
  Internet ─> [CloudFront, opcional: domínio próprio + cache de CSS/imagens]
           ─> Caddy (HTTPS Let's Encrypt, :443) ─> app Spring Boot (:8080, rede interna) ─> Oracle (container, volume persistente)
  Observabilidade: Actuator (/health), OpenTelemetry, logs JSON com traceId
```

| Escolha | Por quê |
|---|---|
| **Spring Boot 4 + Java 21** | Stack da disciplina; injeção de dependência, validação e segurança integradas. |
| **Camadas controller → service → repository** com DTOs (records) e **MapStruct** | Separa as regras de negócio do transporte; os DTOs evitam expor entidades. |
| **Controllers web separados dos REST** (`controller/web`) | As mesmas regras (services) atendem às telas e à API do app mobile. |
| **Spring Security stateless com JWT** (header `Authorization` ou cookie `veti_token`) | Um único mecanismo para o app mobile e para as telas. O cookie é HttpOnly e SameSite=Lax. |
| **OAuth2 Google opcional** | Login social sem senha, ativado só quando as credenciais existem. |
| **Bean Validation** com mensagens em português | Mesmas regras na API (400 com campo → mensagem) e nos formulários. |
| **Spring Cache** nos services | Evita consultas repetidas nas listagens; invalidado a cada escrita. |
| **Thymeleaf + CSS próprio** | Server-side rendering, sem build de frontend separado; layout responsivo. |
| **Actuator, OpenTelemetry e logs estruturados** | Health check para monitorar a aplicação na AWS e tracing de controller → service → JDBC. |
| **Oracle 23ai Free** em container na AWS (EC2) | Mesmo banco da disciplina de Banco de Dados, rodando ao lado da aplicação, com dados num volume Docker persistente. |
| **Hibernate cria as tabelas; PL-SQL num package** (`PKG_VETI`) | O modelo fica nas entidades JPA; as regras que pertencem ao banco (validação de senha, relatórios, auditoria) ficam em PL-SQL, aplicado automaticamente na subida (`spring.sql.init`). |
| **`SimpleJdbcCall`** para funções e procedures | Chamada nativa ao package; erros `RAISE_APPLICATION_ERROR` (ORA-20xxx) viram mensagens de negócio (409). |
| **AWS EC2 + Docker Compose + Caddy** | Oracle, aplicação e proxy HTTPS sobem com um único comando; o Caddy emite e renova o certificado sozinho. |
| **CloudFront (CDN)** | URL personalizada com certificado do ACM e cache dos arquivos estáticos, sem mudar a EC2. |
| **GitHub Actions** | CI a cada push: testes unitários, build e testes de API contra um Oracle em container. |

## Integração com as demais disciplinas

| Disciplina | Como aparece na solução | Evidência |
|---|---|---|
| DevOps Tools & Cloud Computing | CI no GitHub Actions e deploy na AWS: EC2 com Docker Compose (Oracle + app + Caddy HTTPS) e CloudFront para a URL personalizada | [ci.yml](.github/workflows/ci.yml), [docker-compose.aws.yml](docker-compose.aws.yml), [Caddyfile](Caddyfile), [Dockerfile](Dockerfile) |
| Compliance, QA & Tests | Testes unitários (JUnit/Mockito, AAA) e de API (Newman) rodando no CI | [src/test](src/test), [api-tests](api-tests) |
| Mastering Relational and Non-Relational Database | A API usa o Oracle como banco principal e chama o package `PKG_VETI`: as funções `FN_MONTA_JSON` e `FN_VALIDA_SENHA` e as procedures `PRC_CONSULTAS_JSON`, `PRC_RESUMO_CONSULTAS`, `PRC_RESUMO_RECEITAS` e `PRC_EXPORTAR_DATASET`. A trigger `TRG_AUDITORIA_CONSULTA` audita as consultas. O dataset exportado em JSON é importado no MongoDB | [entrega de BD](db/README.md), [MER/DER](docs/der.md), [DDL](docs/ddl-oracle.sql), [pkg_veti.sql](src/main/resources/db/pkg_veti.sql), [VetiPackageRepository](src/main/java/br/com/fiap/veti/repository/procedure/VetiPackageRepository.java), [MongoDB](db/mongodb) |

## Como executar localmente

Pré-requisitos: Docker (ou Java 21 + um Oracle 19c ou superior acessível).

1. Copie `.env.example` para `.env` e preencha os valores (o `.env` não é versionado). O `ADMIN_PASSWORD` precisa ter 8 caracteres, com letra e número (regra da `FN_VALIDA_SENHA`).
2. (Opcional) Crie credenciais Google OAuth2 com redirect `http://localhost:8080/veti/login/oauth2/code/google` e preencha `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`.
3. Suba tudo:

   ```bash
   docker compose up --build
   ```

   Na primeira vez o container do Oracle leva 1 a 2 minutos para criar o banco; a API só sobe depois que ele fica saudável.
4. Acesse http://localhost:8080/veti/ e entre com o `ADMIN_EMAIL`/`ADMIN_PASSWORD` do `.env`.
5. Crie uma conta de veterinário (USER) em **Equipe** para testar o segundo perfil.

Para consultar o banco (SQL Developer, DBeaver ou `sqlplus`): host `localhost`, porta `1521`, serviço `FREEPDB1`, usuário e senha do `DB_USER`/`DB_PASSWORD`.

## Deploy na AWS (EC2 com HTTPS)

Produção: **https://18-207-27-110.sslip.io/veti/**. Oracle, aplicação e um proxy **Caddy** (HTTPS automático com Let's Encrypt) rodam numa única EC2, a partir do [docker-compose.yml](docker-compose.yml) mais o [docker-compose.aws.yml](docker-compose.aws.yml). Na primeira subida, o Hibernate cria as tabelas e a API aplica o `pkg_veti.sql` (package, trigger e `TB_AUDITORIA`); os dados ficam no volume Docker `veti-oradata`.

1. **EC2**: Amazon Linux 2023, **t3.medium** (4 GB: o Oracle usa cerca de 2 GB), disco de 30 GB gp3, com um **Elastic IP** associado.
2. **Security Group**:

   | Porta | Origem | Uso |
   |---|---|---|
   | 22 | IP da equipe (`x.x.x.x/32`, IPv4) | SSH |
   | 80, 443 | `0.0.0.0/0` | Aplicação (o 80 é usado pelo Let's Encrypt e redireciona para HTTPS) |
   | 1521 | IP da equipe (`x.x.x.x/32`, IPv4) | SQL Developer (opcional) |

   Digite o IPv4 à mão (veja em https://checkip.amazonaws.com): a opção *My IP* do console pode gravar o IPv6 do navegador, e aí o SSH dá timeout.
3. **Docker e código** (na EC2):

   ```bash
   sudo dnf install -y docker git && sudo systemctl enable --now docker && sudo usermod -aG docker ec2-user
   sudo mkdir -p /usr/local/lib/docker/cli-plugins && sudo curl -SL https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose && sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
   exit   # entre de novo por SSH para o grupo docker valer
   git clone https://github.com/Challenge-2025-Equipe-07/challenge-2026-java-spint1.git veti && cd veti
   ```

4. **Variáveis**: `cp .env.example .env` e preencha com valores de produção (novo `JWT_SECRET`, senhas fortes). Defina `APP_DOMAIN` com o Elastic IP trocando pontos por hífens: `18.207.27.110` → `18-207-27-110.sslip.io`.
5. **Subir**:

   ```bash
   docker compose -f docker-compose.yml -f docker-compose.aws.yml up -d --build
   docker compose logs -f app   # aguarde "Started VetiApplication"
   ```

   A primeira subida leva de 5 a 10 minutos (build do JAR e criação do banco).
6. **Testar**: `https://<APP_DOMAIN>/health` e `https://<APP_DOMAIN>/veti/`. Para o login Google, cadastre `https://<APP_DOMAIN>/veti/login/oauth2/code/google` no Google Cloud (em produção: `https://18-207-27-110.sslip.io/veti/login/oauth2/code/google`).

Para atualizar depois de um push: `git pull && docker compose -f docker-compose.yml -f docker-compose.aws.yml up -d --build`. Logs: `docker compose logs -f app`.

Para consultar o banco de produção (SQL Developer/DBeaver): host = Elastic IP da EC2, porta `1521`, serviço `FREEPDB1`, usuário e senha do `DB_USER`/`DB_PASSWORD` do `.env` da EC2 (a porta só abre para o IP liberado no Security Group).

### URL personalizada com CloudFront (CDN)

O CloudFront fica na frente da EC2, atende pelo domínio da equipe (ex.: `veti.seudominio.com`) e guarda em cache o CSS e as imagens (versionados por hash). A EC2 continua sendo a origem, pelo endereço `sslip.io`.

1. **Domínio**: registre um no Route 53 (*Registered domains*) ou use um que a equipe já tenha.
2. **Certificado**: no **ACM, região `us-east-1`** (obrigatório para o CloudFront), peça um certificado público para `veti.seudominio.com`, com validação por **DNS**, e crie o registro CNAME que o ACM pede (no Route 53 há o botão *Create records in Route 53*).
3. **Distribuição** (CloudFront → *Create distribution*):

   | Campo | Valor |
   |---|---|
   | Origin domain | `18-207-27-110.sslip.io` (o `APP_DOMAIN` da EC2) |
   | Protocol | **HTTPS only** |
   | Add custom header | `X-Forwarded-Host` = `veti.seudominio.com` |
   | Viewer protocol policy | **Redirect HTTP to HTTPS** |
   | Allowed HTTP methods | **GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE** |
   | Cache policy | **CachingDisabled** (telas e API são dinâmicas) |
   | Origin request policy | **AllViewerExceptHostHeader** (repassa cookies, query string e `Authorization`) |
   | Alternate domain name (CNAME) | `veti.seudominio.com` |
   | Custom SSL certificate | o certificado do passo 2 |
   | WAF | *Do not enable* |

4. **Cache dos arquivos estáticos**: na aba *Behaviors*, crie dois comportamentos, `/veti/css/*` e `/veti/img/*`, com a mesma origem e a cache policy **CachingOptimized**.
5. **DNS**: no Route 53, crie um registro **A (Alias)** `veti` apontando para a distribuição (fora do Route 53, um **CNAME** para `dxxxx.cloudfront.net`).
6. **Google**: cadastre `https://veti.seudominio.com/veti/login/oauth2/code/google` como redirect autorizado.

O header `X-Forwarded-Host` faz a aplicação montar os redirects (login, logout e OAuth) com o domínio público: o [Caddyfile](Caddyfile) repassa esse header e o Spring o lê (`server.tomcat.remoteip.host-header`). A distribuição leva de 5 a 15 minutos para ficar pronta.

## Modelo de dados

- **MER/DER**: [docs/der.md](docs/der.md) (diagrama Mermaid, renderizado pelo GitHub).
- **Modelo físico**: [docs/ddl-oracle.sql](docs/ddl-oracle.sql), gerado pelo próprio Hibernate com o dialeto do Oracle.
- **PL-SQL**: [src/main/resources/db/pkg_veti.sql](src/main/resources/db/pkg_veti.sql). É idempotente e pode ser rodado também à mão no SQL Developer (F5).
- **Entrega da disciplina de Banco de Dados** (script único com tabelas, carga, package, trigger e testes, mais os scripts do MongoDB): [db/README.md](db/README.md).

Para regenerar o DDL depois de mudar uma entidade, suba a aplicação uma vez com:

```bash
--spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create --spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=docs/ddl-oracle.sql
```

## Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | sim | Conexão Oracle, ex.: `jdbc:oracle:thin:@//<host>:1521/FREEPDB1` |
| `ORACLE_PASSWORD` | só no docker-compose | Senha do SYS/SYSTEM do container Oracle local |
| `DB_INIT_MODE` | não | Padrão `never`, para o App Service PostgreSQL não executar o PL/SQL. No Oracle, use `always` para aplicar o `pkg_veti.sql` na subida |
| `JWT_SECRET` | sim | Segredo de assinatura do JWT (sem valor padrão no código) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | sim (1ª subida) | Administrador inicial, criado se ainda não existir |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | não | Ativam o login com Google. Sem elas a aplicação sobe só com login por e-mail/senha |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | não | Conta Gmail e *senha de app* usadas para enviar o lembrete de consulta ao tutor. Sem elas o lembrete só é registrado no log |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM_NAME` | não | Servidor SMTP (padrão `smtp.gmail.com:587` com STARTTLS) e nome do remetente (padrão `Clínica Veti`) |
| `DDL_AUTO` | não | Padrão `update` (o Hibernate cria e atualiza as tabelas) |
| `PORT` / `SERVER_PORT` | não | Porta HTTP (padrão 8080) |
| `LOG_FORMAT` | não | Formato do log no console: `logstash` (padrão), `ecs` ou `gelf` |
| `OTEL_EXPORT_ENABLED` | não | `true` para exportar traces e métricas via OTLP/HTTP |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | não | Endpoint do coletor OTLP (padrão `http://localhost:4318`), ex.: um AWS Distro for OpenTelemetry enviando para o CloudWatch/X-Ray |
| `APP_DOMAIN` | só na EC2 | Domínio do HTTPS no Caddy, ex.: `18-207-27-110.sslip.io` |
| `TRACING_SAMPLING_PROBABILITY` | não | Amostragem de traces (padrão `1.0`) |

### E-mail do lembrete (Gmail)

O botão **Notificar tutor** (e `POST /consultas/{id}/notificar-tutor`) manda ao tutor um e-mail HTML com pet, data e veterinário da consulta, pelo SMTP do Gmail (`smtp.gmail.com:587`, STARTTLS).

1. Na conta Google que vai enviar, ative a **verificação em duas etapas**.
2. Em [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords), crie uma **senha de app** (16 caracteres). O Gmail recusa a senha normal da conta por SMTP.
3. Defina `MAIL_USERNAME=<conta>@gmail.com` e `MAIL_PASSWORD=<senha de app>` no `.env` (local ou da EC2) e recrie o container com `docker compose ... up -d`.

Sem essas variáveis, o lembrete só é registrado no log e a tela avisa que o envio não está configurado; é o modo usado no CI. Se o Gmail recusar o envio, a tela mostra o erro e a API responde `502`. O Gmail limita contas pessoais a cerca de 500 envios por dia.

## Endpoints da API

Documentação completa no Swagger (`/veti/swagger-ui.html`). Resumo:

| Recurso | Endpoints |
|---|---|
| Autenticação | `POST /auth/login` (JSON → token), `POST /auth/register`, `POST /auth/logout` |
| Tutores | `GET/POST /tutores`, `GET/PUT/DELETE /tutores/{id}` |
| Pets | `GET/POST /pets`, `GET/PUT/DELETE /pets/{id}` |
| Consultas | `GET/POST /consultas`, `GET/PUT/DELETE /consultas/{id}`, `POST /consultas/{id}/notificar-tutor` |
| Veterinários | `GET /veterinarios`, `GET/DELETE /veterinarios/{id}` (ADMIN) |
| Relatórios do banco | `GET /relatorios/consultas-json`, `GET /relatorios/resumo-consultas`, `GET /relatorios/resumo-receitas`, `GET /relatorios/dataset-json[?download=true]`, `GET /relatorios/auditoria-consultas` (ADMIN) |

Use o header `Authorization: Bearer <token>` nas chamadas.

## Observabilidade

- **Health check**: `GET /health` (fora do context-path, liberado sem autenticação). Verifica processo (`ping`, `livenessState`, `readinessState`), Oracle (`db`) e, quando o login Google está ativo, o provedor OAuth (`googleOAuth`). Falha do Google retorna `DEGRADED` (HTTP 200) para não reiniciar a instância por causa de um terceiro; falha do banco retorna `DOWN` (HTTP 503).
- **Logs**: JSON (Logstash) no stdout, com `traceId`, `spanId` e `correlationId` em cada linha. O header `X-Correlation-Id` (ou `X-Request-Id`) é aceito e devolvido; sem ele, o `traceId` é usado. Cada requisição gera um log de acesso INFO (2xx/3xx), WARN (4xx) ou ERROR (5xx).
- **Tracing**: OpenTelemetry via Micrometer. Spans de HTTP (controller) → service (`@Observed`) → JDBC (`connection`/`query`, via datasource-micrometer).
- **Métricas**: `http.server.requests` (tempo de resposta com percentis p50/p95/p99 e taxa de erro pelas tags `outcome`/`status`), `veti.service`, `jdbc.*`, JVM e Tomcat. Exportadas por OTLP e consultáveis em `/veti/actuator/metrics` (requer ADMIN).

Teste local de exportação com um coletor OpenTelemetry:

```bash
OTEL_EXPORT_ENABLED=true OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 java -jar target/veti-0.0.1-SNAPSHOT.jar
```

## Testes

Testes unitários (padrão AAA, Mockito, não precisam de banco):

```bash
./mvnw test
```

Testes de API (Postman/Newman) em [api-tests/veti.postman_collection.json](api-tests/veti.postman_collection.json), com a API rodando:

```bash
npx newman run api-tests/veti.postman_collection.json --env-var baseUrl=http://localhost:8080/veti --env-var healthUrl=http://localhost:8080/health --env-var adminEmail=<ADMIN_EMAIL> --env-var adminPassword=<ADMIN_PASSWORD>
```

Casos cobertos: health check com banco, auto-cadastro público (o pedido de ADMIN é ignorado), login JWT, bloqueio sem token (escrita e leitura), USER impedido de cadastrar tutor, login ADMIN, senha sem número recusada pela `FN_VALIDA_SENHA` (409), criar tutor, criar pet com FK do tutor, pet com tutor inexistente (404), criar consulta, JSON da `PRC_CONSULTAS_JSON`, INSERT registrado pela trigger em `TB_AUDITORIA`, relatórios bloqueados para USER, excluir consulta (e confirmar 404) e limpeza. O workflow [.github/workflows/ci.yml](.github/workflows/ci.yml) roda tudo isso no GitHub Actions com Oracle em container.

## Integração contínua (GitHub Actions)

O workflow [.github/workflows/ci.yml](.github/workflows/ci.yml) roda a cada push na `main` e em todo pull request:

1. Sobe um Oracle 23ai Free em container (serviço do workflow).
2. Roda os testes unitários e gera o JAR.
3. Sobe a API contra esse Oracle (o `pkg_veti.sql` é aplicado na subida) e espera o `/health`.
4. Roda os testes de API (Newman) e mostra os logs da API se algo falhar.

O deploy em produção é feito na EC2 (seção [Deploy na AWS](#deploy-na-aws-ec2-com-https)): `git pull` seguido de `docker compose ... up -d --build`. Os segredos de produção ficam só no `.env` da EC2, fora do repositório.

Apresentação ao vivo do time CCGL.

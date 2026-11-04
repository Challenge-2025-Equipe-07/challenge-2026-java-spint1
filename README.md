# Veti — gestão de cuidado contínuo para clínicas veterinárias

Solução do **Challenge 2026 (FIAP · 2TDS · Clyvo Vet)** na disciplina **Java Advanced**: uma aplicação Spring Boot (API REST + telas web Thymeleaf) que organiza tutores, pets e consultas de uma clínica e ajuda a equipe a manter o pet em acompanhamento contínuo (agenda, histórico e lembretes ao tutor).

| | |
|---|---|
| **Aplicação online** | _<preencher com a URL do Azure App Service, ex.: https://veti-app.azurewebsites.net/veti/>_ |
| **Vídeo da apresentação** | _<preencher com o link do vídeo (máx. 15 min)>_ |
| **Swagger (API)** | `<URL da aplicação>/veti/swagger-ui.html` |
| **Health check** | `<URL da aplicação>/health` |

## Integrantes

| Nome                | RM            |
|---------------------|---------------|
| _Carlos André_      | _<RM> 564968_ |
| _Camilo Michelleto_ | _<RM>_        |
| _Guilherme Costa_   | _<RM> 562700_ |
| _Laura Silva_       | _<RM>_        |

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
| Aplicação no ar | Abra a URL da aplicação (ou http://localhost:8080/veti/ rodando local) e confira `/health` com `"status":"UP"` |
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
Cliente REST / Mobile ─┘   (JWT + OAuth2)      (web e REST)  (regras, └─> PKG_VETI (SimpleJdbcCall) ┘   (container Docker
                                                              cache)                                   numa EC2 da AWS)
Azure App Service (JAR) ── Observabilidade: Actuator, OpenTelemetry, logs JSON, Application Insights
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
| **Actuator, OpenTelemetry e logs estruturados** | Health check para o App Service e tracing de controller → service → JDBC. |
| **Oracle 23ai Free** em container na AWS (EC2) | Mesmo banco da disciplina de Banco de Dados; a regra de DevOps aceita Oracle em container na nuvem. |
| **Hibernate cria as tabelas; PL-SQL num package** (`PKG_VETI`) | O modelo fica nas entidades JPA; as regras que pertencem ao banco (validação de senha, relatórios, auditoria) ficam em PL-SQL, aplicado automaticamente na subida (`spring.sql.init`). |
| **`SimpleJdbcCall`** para funções e procedures | Chamada nativa ao package; erros `RAISE_APPLICATION_ERROR` (ORA-20xxx) viram mensagens de negócio (409). |
| **Azure App Service (JAR) + Azure DevOps** | Deploy PaaS sem container, com CI/CD automatizado. |

## Integração com as demais disciplinas

| Disciplina | Como aparece na solução | Evidência |
|---|---|---|
| DevOps Tools & Cloud Computing | Pipeline CI/CD e deploy no Azure App Service | [azure-pipelines.yml](azure-pipelines.yml) |
| Compliance, QA & Tests | Testes unitários (JUnit/Mockito, AAA) e de API (Newman) rodando no CI | [src/test](src/test), [api-tests](api-tests) |
| Mastering Relational and Non-Relational Database | A API usa o Oracle como banco principal e chama o package `PKG_VETI` (funções `FN_MONTA_JSON` e `FN_VALIDA_SENHA`, procedures `PRC_CONSULTAS_JSON` e `PRC_RESUMO_CONSULTAS`); a trigger `TRG_AUDITORIA_CONSULTA` audita as consultas | [MER/DER](docs/der.md), [DDL](docs/ddl-oracle.sql), [pkg_veti.sql](src/main/resources/db/pkg_veti.sql), [VetiPackageRepository](src/main/java/br/com/fiap/veti/repository/procedure/VetiPackageRepository.java) |

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

## Banco Oracle na AWS (container numa EC2)

O banco de produção é o mesmo container `gvenzl/oracle-free`, rodando numa EC2.

1. **EC2**: Amazon Linux 2023 ou Ubuntu, **t3.small ou maior** (o Oracle Free precisa de cerca de 2 GB de RAM; a t3.micro não basta), disco de 20 GB.
2. **IP fixo**: associe um *Elastic IP* à instância, para a `DB_URL` não mudar quando a EC2 reiniciar.
3. **Security Group**: libere a porta **1521** apenas para os *Outbound IP addresses* do Web App (Portal do Azure → Web App → Propriedades) e para o IP da equipe. Libere a 22 (SSH) só para o IP da equipe.
4. **Docker e container** (na EC2):

   ```bash
   sudo dnf install -y docker && sudo systemctl enable --now docker
   sudo docker volume create veti-oradata
   sudo docker run -d --name veti-oracle --restart unless-stopped -p 1521:1521 \
     -e ORACLE_PASSWORD='<senha-sys>' -e APP_USER=veti -e APP_USER_PASSWORD='<senha-app>' \
     -v veti-oradata:/opt/oracle/oradata gvenzl/oracle-free:23-slim
   sudo docker logs -f veti-oracle   # aguarde "DATABASE IS READY TO USE!"
   ```

5. **Aplicação**: no variable group `veti-secrets` e nas *Configurações do aplicativo* do Web App, use
   `DB_URL=jdbc:oracle:thin:@//<elastic-ip>:1521/FREEPDB1`, `DB_USER=veti` e `DB_PASSWORD=<senha-app>`.
   Na primeira subida, o Hibernate cria as tabelas e a API aplica o `pkg_veti.sql` (package, trigger e `TB_AUDITORIA`).

## Deploy completo na AWS (EC2 com HTTPS)

Oracle, aplicação e um proxy **Caddy** (HTTPS automático com Let's Encrypt) rodam numa única EC2, a partir do [docker-compose.yml](docker-compose.yml) mais o [docker-compose.aws.yml](docker-compose.aws.yml).

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
6. **Testar**: `https://<APP_DOMAIN>/health` e `https://<APP_DOMAIN>/veti/`. Para o login Google, cadastre `https://<APP_DOMAIN>/veti/login/oauth2/code/google` no Google Cloud.

Para atualizar depois de um push: `git pull && docker compose -f docker-compose.yml -f docker-compose.aws.yml up -d --build`.

## Modelo de dados

- **MER/DER**: [docs/der.md](docs/der.md) (diagrama Mermaid, renderizado pelo GitHub).
- **Modelo físico**: [docs/ddl-oracle.sql](docs/ddl-oracle.sql), gerado pelo próprio Hibernate com o dialeto do Oracle.
- **PL-SQL**: [src/main/resources/db/pkg_veti.sql](src/main/resources/db/pkg_veti.sql). É idempotente e pode ser rodado também à mão no SQL Developer (F5). O arquivo [db/2TDSPW_2026_CodigoSql_CLYVOVET.sql](db/2TDSPW_2026_CodigoSql_CLYVOVET.sql) é a versão da Sprint 3, feita sobre o schema anterior.

Para regenerar o DDL depois de mudar uma entidade, suba a aplicação uma vez com:

```bash
--spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create --spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=docs/ddl-oracle.sql
```

## Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | sim | Conexão Oracle, ex.: `jdbc:oracle:thin:@//<host>:1521/FREEPDB1` |
| `ORACLE_PASSWORD` | só no docker-compose | Senha do SYS/SYSTEM do container Oracle local |
| `DB_INIT_MODE` | não | Padrão `always`: aplica o `pkg_veti.sql` na subida. Use `never` se o package for mantido à mão |
| `JWT_SECRET` | sim | Segredo de assinatura do JWT (sem valor padrão no código) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | sim (1ª subida) | Administrador inicial, criado se ainda não existir |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | não | Ativam o login com Google. Sem elas a aplicação sobe só com login por e-mail/senha |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | não | Conta Gmail e *senha de app* usadas para enviar o lembrete de consulta ao tutor. Sem elas o lembrete só é registrado no log |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM_NAME` | não | Servidor SMTP (padrão `smtp.gmail.com:587` com STARTTLS) e nome do remetente (padrão `Clínica Veti`) |
| `DDL_AUTO` | não | Padrão `update` (o Hibernate cria e atualiza as tabelas) |
| `PORT` / `SERVER_PORT` | não | Porta HTTP (padrão 8080) |
| `LOG_FORMAT` | não | Formato do log no console: `logstash` (padrão), `ecs` ou `gelf` |
| `OTEL_EXPORT_ENABLED` | não | `true` para exportar traces e métricas via OTLP/HTTP |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | não | Endpoint do coletor OTLP (padrão `http://localhost:4318`) |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | não | Anexa o agente do Azure Application Insights (traces, métricas e logs) |
| `TRACING_SAMPLING_PROBABILITY` | não | Amostragem de traces (padrão `1.0`) |

### E-mail do lembrete (Gmail)

O botão **Notificar tutor** (e `POST /consultas/{id}/notificar-tutor`) manda ao tutor um e-mail HTML com pet, data e veterinário da consulta, pelo SMTP do Gmail (`smtp.gmail.com:587`, STARTTLS).

1. Na conta Google que vai enviar, ative a **verificação em duas etapas**.
2. Em [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords), crie uma **senha de app** (16 caracteres). O Gmail recusa a senha normal da conta por SMTP.
3. Defina `MAIL_USERNAME=<conta>@gmail.com` e `MAIL_PASSWORD=<senha de app>` no `.env` (local) ou nas configurações do Web App/variable group (Azure).

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
| Relatórios do banco | `GET /relatorios/consultas-json`, `GET /relatorios/resumo-consultas`, `GET /relatorios/auditoria-consultas` (ADMIN) |

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

## Deploy no Azure App Service (JAR, sem container)

```bash
./mvnw -DskipTests package
```

Publique `target/veti-0.0.1-SNAPSHOT.jar` num App Service Java SE 21, configure as variáveis acima em *Configurações do aplicativo* e defina o *Health check path* como `/health`. O `Dockerfile` e o `docker-compose.yml` servem só para desenvolvimento local.

## CI/CD no Azure DevOps

A pipeline [azure-pipelines.yml](azure-pipelines.yml) dispara a cada push na `master` (ou `main`) e tem dois estágios:

1. **CI**: testes unitários → JAR → sobe a API contra um Oracle de teste (container) → testes de API (Newman) → publica os resultados dos testes e o artefato `drop` (`app.jar`).
2. **CD**: roda só se o CI gerar o artefato e faz o deploy no Azure Web App (Linux, Java SE 21), aplicando as variáveis da aplicação.

Configuração necessária no projeto do Azure DevOps:

| Item | Onde | Valor |
|---|---|---|
| Service connection | Project settings → Service connections → Azure Resource Manager | nome `veti-azure-connection` |
| Variable group | Pipelines → Library | nome `veti-secrets` com `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD` marcados como secretos |
| Web App | Portal do Azure | Linux, Java SE 21, nome `veti-app` (ou ajuste `webAppName` na pipeline) |
| Environment | Pipelines → Environments | `veti-producao` (criado automaticamente no primeiro deploy) |

O login com Google é opcional: configure `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` direto nas *Configurações do aplicativo* do Web App e cadastre a URL de redirect `https://<app>.azurewebsites.net/veti/login/oauth2/code/google` no Google Cloud.

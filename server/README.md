# Servidor Backend - Rede Cidadã SP

Servidor de API RESTful em **Kotlin** com **Ktor Server 3.6.0** (Engine Netty) e banco de dados **SQLite** (`rede_cidada_server.db`) para o ecossistema do aplicativo **Rede Cidadã SP** (Segurança Comunitária e Mobilidade Urbana em São Paulo).

---

## 🚀 Como Executar o Servidor

Você pode iniciar o servidor backend diretamente pelo Gradle a partir da raiz do projeto:

### No Windows (PowerShell / CMD):
```powershell
.\gradlew.bat :server:run
```

### No Linux / macOS:
```bash
./gradlew :server:run
```

O servidor será iniciado na porta **`8080`** (ou na porta definida pela variável de ambiente `PORT`):
- **Base URL**: `http://localhost:8080/api/v1`
- **Endereço para o Emulador Android**: `http://10.0.2.2:8080/api/v1`

---

## ☁️ Publicação no Railway (produção)

O repositório já traz `railway.json` (na raiz) e `server/Dockerfile`. O Railway compila **só o servidor**
(o app Android não entra na imagem) e roda com memória limitada (~160 MB).

1. **New Project → Deploy from GitHub repo** → escolha `ProjetoMobileRedeCidada` e o branch a publicar.
   O Railway lê o `railway.json` e usa o Dockerfile sozinho.
2. **Volume (obrigatório):** no serviço, *Attach Volume* com o caminho de montagem **`/data`**.
   É onde ficam o banco (`/data/rede_cidada.db`) e as fotos/vídeos (`/data/uploads`). Sem volume, tudo some a cada deploy.
3. **Variables** do serviço:

   | Variável | Valor |
   |---|---|
   | `JWT_SECRET` | texto aleatório com 32+ caracteres (gere com o comando abaixo) |
   | `ADMIN_TOKEN` | chave da moderação, 24+ caracteres (pode copiar a do `secrets.properties` local) |
   | `SPTRANS_TOKEN` | token do Olho Vivo |
   | `ARTESP_API_KEY` | chave da API Trilhos |

   `APP_ENV=production`, `TRUST_PROXY=true`, `SEED_DEMO_DATA=false`, `DATABASE_PATH` e `MEDIA_DIR` já vêm do Dockerfile.

   Para gerar um `JWT_SECRET` no Windows (PowerShell):
   ```powershell
   [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
   ```
4. **Settings → Networking → Generate Domain**: o Railway cria o endereço HTTPS (`https://<nome>.up.railway.app`).
5. Teste: `https://<nome>.up.railway.app/api/v1/integrations/health` deve responder `200`.
6. No app, a versão de release usa `BACKEND_URL=https://<nome>.up.railway.app/api/v1` (em `local.properties` ou `-PBACKEND_URL=...`).

**Moderação em produção:** `GET /api/v1/admin/reports` lista denúncias; `POST /api/v1/admin/incidents/{id}/remove` apaga e
`POST /api/v1/admin/incidents/{id}/keep` mantém. Todas exigem o cabeçalho `X-Admin-Token: <ADMIN_TOKEN>`.

## 🧪 Como Executar os Testes Automatizados

Para executar os testes automatizados de integração dos endpoints REST:

```powershell
.\gradlew.bat :server:test
```

---

## 📐 Estrutura do Módulo (`:server`)

```text
server/
├── build.gradle.kts
├── README.md
└── src/
    ├── main/
    │   ├── kotlin/com/example/projetorevisao/server/
    │   │   ├── Application.kt          # Ponto de entrada e plugins do Ktor
    │   │   ├── database/
    │   │   │   └── DatabaseFactory.kt  # Conexão SQLite, schemas e dados de exemplo (seed)
    │   │   ├── models/
    │   │   │   └── Models.kt           # DTOs e modelos serializáveis JSON
    │   │   ├── repository/
    │   │   │   └── ServerRepository.kt # Regras de negócio e persistência no banco
    │   │   ├── security/
    │   │   │   └── JwtConfig.kt        # Emissão/validação de JWT e refresh tokens
    │   │   └── routes/
    │   │       ├── AuthRoutes.kt       # Autenticação e perfil
    │   │       ├── IncidentRoutes.kt   # Relatos comunitários e confirmações
    │   │       ├── TelemetryRoutes.kt  # Clima, alagamentos e distrito policial
    │   │       ├── MobilityRoutes.kt   # Metrô SP e rotas monitoradas
    │   │       ├── ReelRoutes.kt       # Vídeos orientativos de segurança
    │   │       ├── SosRoutes.kt        # Protocolos de emergência SOS e guardiões
    │   │       └── IntegrationRoutes.kt # Healthcheck e catálogo de APIs
    │   └── resources/
    │       └── logback.xml             # Configuração de logs do servidor
    └── test/
        └── kotlin/com/example/projetorevisao/server/
            └── ServerApiTest.kt        # Testes de integração via Ktor testApplication
```

---

## 📡 Especificação Detalhada das Rotas REST (`/api/v1`)

### 🔐 Autenticação e segurança

- **Senhas** com BCrypt (custo 12). Hashes SHA-256 antigos são migrados no primeiro login correto.
- **Access token**: JWT HS256, 15 min (`ACCESS_TOKEN_TTL_MINUTES`). Envie em `Authorization: Bearer <accessToken>`.
- **Refresh token**: valor aleatório de 256 bits, 30 dias (`REFRESH_TOKEN_TTL_DAYS`). O banco guarda só o hash.
  Cada `/refresh` troca o token por um novo (rotação). Se um token já usado voltar, todas as sessões do usuário são revogadas.
- **Limite de tentativas**: `/login`, `/register` e `/refresh` aceitam `AUTH_RATE_LIMIT_PER_MINUTE` (padrão 10) chamadas por IP por minuto. Acima disso, a resposta é 429.
- **CORS** desligado por padrão (o app Android não precisa). Para um painel web, defina `CORS_ALLOWED_HOSTS`.
- **Erros** não expõem detalhes internos. O stack trace vai só para o log.

| Variável | Padrão | Observação |
|---|---|---|
| `APP_ENV` | `development` | Em `production`, `JWT_SECRET` é obrigatório e os dados de demonstração ficam desligados |
| `JWT_SECRET` | aleatório por execução | Mínimo de 32 caracteres. Sem ele, os tokens deixam de valer quando o servidor reinicia |
| `SEED_DEMO_DATA` / `SEED_DEMO_PASSWORD` | `true` / `senha123` | Conta demo `cidadao@redecidada.sp.gov.br` |
| `DATABASE_PATH` | `rede_cidada_server.db` | Arquivo SQLite |

### 1. Autenticação e Perfis (`/api/v1/auth`)

🔒 = exige `Authorization: Bearer <accessToken>`.

| Método | Endpoint | Descrição | Payload / Parâmetros |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Autenticação do cidadão | `{"email": "cidadao@redecidada.sp.gov.br", "password": "senha123"}` |
| `POST` | `/api/v1/auth/register` | Cadastro (CPF validado e guardado só mascarado; senha com 8+ caracteres, letras e números) | `{"name": "...", "email": "...", "cpf": "529.982.247-25", "password": "...", "neighborhood": "..."}` |
| `POST` | `/api/v1/auth/refresh` | Troca o refresh token por um novo par | `{"refreshToken": "..."}` |
| `POST` | `/api/v1/auth/logout` | Encerra a sessão deste refresh token | `{"refreshToken": "..."}` |
| `POST` | `/api/v1/auth/logout-all` 🔒 | Encerra todas as sessões do usuário | — |
| `POST` | `/api/v1/auth/delete-account` 🔒 | Exclui a conta, sessões, guardiões e alertas SOS (exigência da Play Store) | `{"password": "..."}` |
| `GET` | `/api/v1/auth/me` 🔒 | Dados do perfil logado | — |
| `PUT` | `/api/v1/auth/profile` 🔒 | Atualiza configurações e bairro | `{"neighborhood": "Capão Redondo...", "anonymousReportingDefault": true, "geofenceRadiusKm": 2.0}` |

Resposta de login, cadastro e refresh: `{"accessToken", "refreshToken", "tokenType": "Bearer", "expiresInSeconds", "userId", "name", "email", "neighborhood", "userTrustScore"}`.

### 2. Relatos Comunitários e Ocorrências (`/api/v1/incidents`)

| Método | Endpoint | Descrição | Payload / Parâmetros |
|---|---|---|---|
| `GET` | `/api/v1/incidents` | Lista ocorrências registradas | Query params opcionais: `neighborhood`, `category`, `query` |
| `POST` | `/api/v1/incidents` 🔒 | Registra novo relato comunitário | `{"category": "ROUBO_FURTO", "title": "...", "description": "...", "address": "...", "neighborhood": "...", "isAnonymous": true}` |
| `GET` | `/api/v1/incidents/{id}` | Detalhes de um relato específico | Path param: `id` (ex: `inc-1`) |
| `POST` | `/api/v1/incidents/{id}/confirm` 🔒 | Confirma a ocorrência comunitária | Incrementa confirmações e atualiza a Pontuação de Confiança do usuário |
| `POST` | `/api/v1/incidents/{id}/comments` 🔒 | Adiciona um comentário à ocorrência | `{"content": "Confirmo presença de viatura", "isAnonymous": false}` |

### 3. Telemetria Urbana e Clima (`/api/v1/telemetry`)

| Método | Endpoint | Descrição | Payload / Parâmetros |
|---|---|---|---|
| `GET` | `/api/v1/telemetry/weather` | Clima, precipitação e alagamento CGE | Query param: `neighborhood` |
| `GET` | `/api/v1/telemetry/district` | Dados do Distrito Policial (SSP-SP) e Smart Sampa | Query param: `neighborhood` |

### 4. Mobilidade e Transporte (`/api/v1/mobility`)

| Método | Endpoint | Descrição | Payload / Parâmetros |
|---|---|---|---|
| `GET` | `/api/v1/mobility/metro` | Status operacional em tempo real (L1, L2, L3, L4, L5, L15) | Retorna operadora, frota, lotação e policiamento ativo |
| `GET` | `/api/v1/mobility/routes` | Rotas de pedestres monitoradas e iluminadas | Retorna pontuação de segurança, iluminação % e câmeras |

### 5. Vídeos de Segurança (`/api/v1/reels`)

| Método | Endpoint | Descrição | Payload / Parâmetros |
|---|---|---|---|
| `GET` | `/api/v1/reels` | Lista de vídeos orientativos | Ordenados por engajamento |
| `POST` | `/api/v1/reels/{id}/like` 🔒 | Curtir/Descurtir um vídeo de segurança | Path param: `id` |

### 6. SOS de Emergência e Guardiões (`/api/v1/sos`)

| Método | Endpoint | Descrição | Payload / Parâmetros |
|---|---|---|---|
| `POST` | `/api/v1/sos/alert` 🔒 | Registra o alerta e devolve os guardiões. **Não aciona polícia**: o app abre o SMS para os guardiões e orienta ligar 190 | `{"neighborhood": "...", "latitude": -23.5, "longitude": -46.6}` |
| `GET` | `/api/v1/sos/guardians` 🔒 | Lista de contatos guardiões do usuário logado | — |
| `DELETE` | `/api/v1/sos/guardians/{id}` 🔒 | Remove um guardião do usuário logado | — |
| `POST` | `/api/v1/sos/guardians` 🔒 | Adiciona um contato guardião | `{"name": "...", "relation": "...", "phone": "..."}` |

### 7. Verificação de Saúde e Integrações (`/api/v1/integrations`)

| Método | Endpoint | Descrição | Payload / Parâmetros |
|---|---|---|---|
| `GET` | `/api/v1/integrations/health` | Healthcheck do servidor | Retorna `status: UP`, uptime em segundos e timestamp |
| `GET` | `/api/v1/integrations/catalog` | Status de conexões externas | Open-Meteo, INMET, CGE-SP, Overpass/OSM e Metrô SP |

---

## 🗄️ Banco de Dados SQLite (`rede_cidada_server.db`)

O servidor cria e gerencia as seguintes tabelas automaticamente ao iniciar:
1. `users`: Cadastro de cidadãos, senhas em hash BCrypt, bairro e pontuação de confiança.
2. `incidents`: Relatos de ocorrências comunitárias, categorias, coordenadas, status e recomendações geradas.
3. `comments`: Comentários comunitários vinculados a cada ocorrência.
4. `reels`: Vídeos e dicas oficiais com contagem de curtidas e visualizações.
5. `sos_alerts`: Histórico de chamados de emergência e unidades despachadas.
6. `guardians`: Contatos de emergência e rede de proteção familiar.
7. `refresh_tokens`: Sessões ativas (hash SHA-256 do refresh token, validade e revogação).

---

## 📲 Integração com o Aplicativo Android (`:app`)

O app Android comunica-se com este servidor através do cliente `CitizenBackendApiService.kt`. Quando o servidor está em execução na máquina hospedeira (`http://10.0.2.2:8080/api/v1`), os novos relatos criados no aplicativo são sincronizados com o banco de dados do servidor e disponibilizados em tempo real para os demais usuários da região. Caso o servidor não esteja acessível, o app utiliza fallback transparente para a base SQLite local Room.

# Rede Cidadã: modelagem de dados (briefing para o Miro)

Para: Fred · De: Erick · 25/11/2026
Base: código da branch `agent/claude-design-system` (`server/.../database/DatabaseFactory.kt`, `ServerRepository.kt`, `app/.../data/local` e `data/session`). **O código prevalece sobre este documento.**

## O que fazer
1. **No Miro (prioridade):** desenhar a modelagem de dados do app (diagrama ER) usando o MCP ou plugin do Miro. Três quadros:
   - **Quadro A, "Hoje":** as tabelas atuais do servidor (SQLite), com campos, tipos e relacionamentos, mais o que fica só no celular. Seções 2 e 3.
   - **Quadro B, "Evolução":** as tabelas novas para as funcionalidades planejadas. Seção 5.
   - **Quadro C, "Postgres":** o modelo alvo já com os tipos do Postgres (seção 6) e o fluxo da migração (seção 7).
2. **Depois:** validar o modelo com o Erick e só então partir para o código da migração.

Convenções sugeridas no Miro:
- uma caixa por tabela, com nome, campos, tipo e PK/FK;
- linhas com cardinalidade (1:N, N:N);
- **cinza** = existe hoje, **azul** = novo, **vermelho** = problema a corrigir na migração (seção 4).

---

## 1. Onde os dados ficam hoje

| Dado | Onde | Observação |
|---|---|---|
| Todas as tabelas do servidor | **SQLite**, um arquivo em `/data/rede_cidada.db` (volume do Railway) | Sem backup automático. Sem painel de banco no Railway |
| Fotos e vídeos dos relatos | Disco, em `/data/uploads` | O banco guarda só a referência (`incident_media`) |
| Foto do perfil, telefone, nascimento, endereço | **Só no celular** (`LocalProfileStore`, SharedPreferences + `avatar.jpg`) | O servidor ainda não recebe esses campos (`INTEGRAR`) |
| Sessão (tokens, id, nome, e-mail, bairro) | Celular (`SessionManager`, SharedPreferences) | — |
| Cache dos relatos | Celular (Room `incidents`, versão 3) | Inclui relatos criados offline ainda não enviados |
| Integrações externas | Não ficam no banco | Trilhos (ARTESP via relay), ônibus (SPTrans), clima (Open-Meteo): consultados na hora |

## 2. Tabelas do servidor hoje (SQLite)

Tipos como estão no SQLite: `TEXT`, `INTEGER` (inclusive booleanos 0/1 e datas em ms), `REAL`.

### users
| Campo | Tipo | Regra |
|---|---|---|
| id | TEXT | **PK** (UUID em texto) |
| name | TEXT | obrigatório |
| email | TEXT | obrigatório, **único** |
| cpf_masked | TEXT | obrigatório, mas sempre `''` (CPF desativado por decisão) |
| password_hash | TEXT | BCrypt |
| neighborhood | TEXT | bairro em texto livre |
| trust_score | INTEGER | padrão **940** (fixo, ainda não é calculado) |
| anonymous_default | INTEGER (bool) | padrão 1 |
| geofence_radius | REAL | km, padrão 2.0 |
| terms_accepted_at | INTEGER (ms) | aceite dos termos |
| created_at | INTEGER (ms) | |

### incidents (relatos e alertas do mapa)
| Campo | Tipo | Regra |
|---|---|---|
| id | TEXT | **PK** (`inc-xxxxxxxx`) |
| reporter_id | TEXT | autor (→ users.id), **sem FK declarada** |
| category | TEXT | ROUBO_FURTO, INCENDIO, ALAGAMENTO, TEMPESTADE, TRANSITO, OPERACAO_POLICIAL, UTILIDADE_PUBLICA |
| custom_badge_title | TEXT | opcional |
| title, description | TEXT | obrigatórios |
| address, neighborhood | TEXT | texto livre |
| latitude, longitude | REAL | GPS do app; sem GPS, usa um ponto fixo por bairro |
| is_anonymous | INTEGER (bool) | padrão 1 |
| status | TEXT | |
| authority_assigned | TEXT | texto fixo ("Não é enviado à polícia…") |
| ai_safety_recommendation | TEXT | texto fixo por categoria, **não é IA** |
| verified | INTEGER (bool) | |
| confirmations | INTEGER | recalculado a partir de `incident_confirmations` |
| comments_count, shares_count | INTEGER | contadores soltos |
| hidden, reviewed | INTEGER (bool) | moderação |
| distance_meters, time_ago | INTEGER / TEXT | **legado**: valores de exibição gravados no banco |
| map_x_fraction, map_y_fraction | REAL | **legado** (mapa antigo desenhado) |
| user_confirmed | INTEGER (bool) | **legado** |
| created_at | INTEGER (ms) | |

### incident_media (fotos e vídeos; o feed "Vídeos/Reels" do app vem daqui)
| Campo | Tipo | Regra |
|---|---|---|
| id | TEXT | **PK** |
| incident_id | TEXT | **FK → incidents.id** (cascade) |
| uploader_id | TEXT | (→ users.id), sem FK |
| file_name, mime_type | TEXT | arquivo em `/data/uploads` |
| size_bytes | INTEGER | |
| created_at | INTEGER (ms) | |

### comments
| Campo | Tipo | Regra |
|---|---|---|
| id | TEXT | **PK** |
| incident_id | TEXT | **FK → incidents.id** (cascade) |
| author_name | TEXT | **nome em texto, não o id do usuário** |
| content | TEXT | |
| is_official_authority | INTEGER (bool) | |
| time_ago | TEXT | **legado** |
| created_at | INTEGER (ms) | |

### incident_confirmations ("Eu também vi")
| Campo | Tipo | Regra |
|---|---|---|
| incident_id | TEXT | **FK → incidents.id** (cascade) |
| user_id | TEXT | (→ users.id), sem FK |
| created_at | INTEGER (ms) | |
| — | — | **PK composta** (incident_id, user_id): 1 por pessoa |

### incident_reports (denúncias)
| Campo | Tipo | Regra |
|---|---|---|
| id | TEXT | **PK** |
| incident_id | TEXT | **FK → incidents.id** (cascade) |
| reporter_user_id | TEXT | (→ users.id), sem FK |
| reason, details | TEXT | |
| created_at | INTEGER (ms) | |
| — | — | **único** (incident_id, reporter_user_id). Com 3 denúncias o relato some para todos |

### user_blocks
| Campo | Tipo | Regra |
|---|---|---|
| blocker_id, blocked_id | TEXT | (→ users.id), sem FK; **PK composta** |
| created_at | INTEGER (ms) | |

### guardians (contatos de emergência)
| Campo | Tipo | Regra |
|---|---|---|
| id | TEXT | **PK** |
| user_id | TEXT | dono (→ users.id), **sem FK** |
| name, relation, phone | TEXT | o guardião **não tem conta** no app |
| is_sharing_live_location | INTEGER (bool) | não usado de verdade |
| distance_label | TEXT | **legado** |

### sos_alerts
| Campo | Tipo | Regra |
|---|---|---|
| protocol_id | TEXT | **PK** (`SOS-XXXXXX`) |
| user_id | TEXT | (→ users.id), sem FK |
| neighborhood | TEXT | |
| latitude, longitude | REAL | opcionais |
| emergency_type, trigger_source | TEXT | ex.: BOTAO_SOS_APP |
| dispatched_unit, estimated_arrival_minutes | TEXT / INTEGER | sempre "NENHUMA" / 0 (**não aciona ninguém**) |
| status | TEXT | "REGISTRADO" |
| guardians_alerted_count | INTEGER | só conta os guardiões; quem manda a mensagem é a pessoa |
| created_at | INTEGER (ms) | |

### refresh_tokens
| Campo | Tipo | Regra |
|---|---|---|
| id | TEXT | **PK** |
| user_id | TEXT | **FK → users.id** (cascade) |
| token_hash | TEXT | **único** (só o hash SHA-256) |
| expires_at, created_at | INTEGER (ms) | |
| revoked | INTEGER (bool) | |

### reels (**sem uso, para remover**)
Tabela antiga e solta: título, autor em texto, contadores e um `is_liked` **global**, em que uma curtida vale para todos. O app não usa mais essa tabela: o feed de vídeos vem de `incident_media`.

### Relacionamentos de hoje
```
users 1─N incidents          (reporter_id, sem FK)
incidents 1─N incident_media (FK)
incidents 1─N comments       (FK)   comments.author_name = texto, sem ligação com users
incidents N─N users          via incident_confirmations (confirmar)
incidents N─N users          via incident_reports (denunciar)
users N─N users              via user_blocks (bloquear)
users 1─N guardians          (sem FK)
users 1─N sos_alerts         (sem FK)
users 1─N refresh_tokens     (FK)
reels                        (solta)
```
Hoje **não existe nenhuma relação social** entre pessoas: seguir, amizade e comunidade.

## 3. Dados que ficam só no celular
| Estrutura | Campos |
|---|---|
| `LocalProfileStore` (perfil) | displayName, birthDate (dd/mm/aaaa), phone, cep, street, number, complement, district, city, avatar (arquivo `avatar.jpg`) |
| `SessionManager` | accessToken, refreshToken, userId, name, email, neighborhood, guestMode |
| Room `incidents` (cache) | os mesmos campos de `incidents` + `media` (texto "tipo\|url" por linha), `createdAtEpochMs`, `fromServer` |

## 4. Problemas a corrigir na migração (marcar em vermelho no Miro)
1. **FKs faltando:** `incidents.reporter_id`, `incident_media.uploader_id`, `incident_confirmations.user_id`, `incident_reports.reporter_user_id`, `user_blocks.*`, `guardians.user_id` e `sos_alerts.user_id` precisam de FK para `users`.
2. **`comments.author_name`** passa a ser `author_id` (FK) + flag de anônimo.
3. **Campos de exibição gravados no banco:** `time_ago`, `distance_meters`, `distance_label`, `map_x/y_fraction` e `user_confirmed` saem. Tempo e distância são calculados na consulta ou no app.
4. **Contadores soltos:** `comments_count` e `shares_count` passam a ser contados (ou atualizados por trigger).
5. **Booleanos em 0/1 e datas em ms** viram `BOOLEAN` e `TIMESTAMPTZ`.
6. **Textos fixos:** `authority_assigned` e `ai_safety_recommendation` vão para constantes no código ou para uma tabela `categories`.
7. **`trust_score` fixo em 940:** passa a ser calculado a partir de eventos (seção 5).
8. **Remover `reels`** e o `cpf_masked`.
9. **Perfil:** os campos que hoje ficam só no celular sobem para o servidor (tabela `profiles`).
10. **Coordenadas por bairro fixo:** quando não há GPS, o servidor usa um ponto fixo do bairro. Em Postgres, guardar `GEOGRAPHY(Point)` (PostGIS) para busca por raio.

## 5. Evoluções planejadas → tabelas novas (Quadro B)

| Funcionalidade | Tabela nova | Campos principais | Relação |
|---|---|---|---|
| Perfil completo | `profiles` | user_id (PK/FK), display_name, bio, avatar_url, birth_date, phone, neighborhood_id | users 1─1 profiles |
| Bairros padronizados | `neighborhoods` | id, name, city, geom (polígono, opcional) | 1─N users, incidents, communities |
| Seguir página oficial (Defesa Civil, CET, imprensa, ONG) | `pages` | id, name, type (ORGAO/IMPRENSA/ONG/ESCOLA), verified, avatar_url, owner_user_id | users 1─N pages (quem administra) |
| Posts e notícias de páginas | `page_posts` | id, page_id, title, body, link_url, published_at | pages 1─N page_posts |
| Seguir | `follows` | follower_id, page_id **ou** followed_user_id, created_at | users N─N pages / users |
| Amigo, família, informante | `connections` | id, requester_id, addressee_id, type (AMIGO/FAMILIA/INFORMANTE), status (PENDENTE/ACEITO/RECUSADO), created_at | users N─N users |
| Guardião com conta (recebe o SOS) | `guardians` ganha `guardian_user_id` (FK, opcional) | — | liga com `connections` do tipo FAMILIA |
| SOS que avisa de verdade | `sos_deliveries` | id, sos_id, guardian_id, channel (PUSH/WHATSAPP), status, sent_at | sos_alerts 1─N sos_deliveries |
| Comunidade (bairro, condomínio, igreja, escola) | `communities` + `community_members` | communities: id, name, type, neighborhood_id, privacy · members: community_id, user_id, role (MEMBRO/MODERADOR), joined_at | users N─N communities; incident pode ter `community_id` |
| Comentário com resposta e menção | `comments` (+ parent_id) e `mentions` | mentions: comment_id, mentioned_user_id | comments 1─N comments; N─N users |
| Avaliar notícia ou relato | `ratings` | target_type, target_id, user_id, value (CONFIRMO/FALSO/DESATUALIZADO) | 1 por pessoa |
| Score de confiabilidade real | `trust_events` | id, user_id, reason, delta, source_id, created_at | o score sai da soma dos eventos |
| Notificações | `notifications` + `push_tokens` | notifications: user_id, type, payload, read_at · push_tokens: user_id, fcm_token, device | users 1─N |
| Mídia fora do servidor | `incident_media` ganha `storage_key` e `url` | arquivo no Cloudflare R2 | — |

## 6. Postgres: tipos alvo (Quadro C)
| SQLite hoje | Postgres |
|---|---|
| id TEXT | `UUID` (com `gen_random_uuid()`) |
| INTEGER 0/1 | `BOOLEAN` |
| INTEGER ms | `TIMESTAMPTZ` |
| category, status, type em texto | `ENUM` ou `TEXT` + `CHECK` |
| latitude/longitude REAL | `GEOGRAPHY(Point, 4326)` (PostGIS) + índice GIST |
| contadores | `COUNT` em view, ou coluna mantida por trigger |
| — | índices: `incidents(created_at)`, `incidents` GIST(location), `follows(follower_id)`, `connections(addressee_id, status)` |

## 7. Migração para Postgres (resumo)
- **Hospedagem:** continua no Railway (Postgres do próprio Railway, plano Hobby, ~US$ 5/mês). Alternativa grátis: Neon, com 1 GB. A mídia vai para o Cloudflare R2 (10 GB grátis).
- **Atenção:** o teste do Railway termina por volta de **18/12**. Contas em teste têm os volumes **apagados** 30 dias depois. Assinar o Hobby ou migrar antes disso.
- **Passos:**
  1. Trocar `sqlite-jdbc` por driver Postgres + HikariCP.
  2. Passar a criação das tabelas para migrações versionadas (**Flyway**), no lugar de `CREATE TABLE IF NOT EXISTS` + `addColumnIfMissing`.
  3. Ler o banco da variável `DATABASE_URL`.
  4. Aplicar as correções da seção 4.
  5. Adaptar os 33 testes do servidor.
  6. Script de cópia SQLite → Postgres e `/data/uploads` → R2.
  7. Deploy, checar `/api/v1/integrations/health` e o login, e guardar o SQLite antigo por 1 a 2 semanas.
  8. `pg_dump` diário para o R2.
- **Ordem sugerida das evoluções:**
  1. Postgres + R2 + backup.
  2. Perfil completo + score com eventos + avaliações.
  3. Comentários com menção + push.
  4. Conexões, guardião com conta e SOS que avisa.
  5. Páginas, seguir e comunidades.

## 8. Regras que o modelo precisa respeitar
- **Não coletar documentos** (CPF/CNPJ, RG): decisão do projeto e política de privacidade publicada.
- **Anonimato:** o relato anônimo guarda o `reporter_id` (para moderação e bloqueio), mas nunca o expõe na API.
- **Excluir conta** apaga ou anonimiza tudo do usuário (exigência da Play Store). As FKs precisam de `ON DELETE` pensado tabela a tabela.
- **Moderação:** denúncias, bloqueio e ocultar continuam existindo em tudo o que for conteúdo novo (comentários, posts de página, comunidades).
- Nada de demonstração rotulado como "oficial" ou "verificado": `pages.verified` só vale para página realmente confirmada.

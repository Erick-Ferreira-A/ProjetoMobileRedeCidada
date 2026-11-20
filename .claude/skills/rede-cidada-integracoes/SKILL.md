---
name: rede-cidada-integracoes
description: Como tratar fontes de dados no Rede Cidadã — app ↔ servidor Ktor, SPTrans/ARTESP, Open-Meteo, INMET, CGE, Overpass/OSM; distinguir ao vivo, fallback, demonstração e não implementado; atribuir a fonte correta; tratar erro/timeout; configurar MAPS_API_KEY, BACKEND_URL e segredos do servidor sem expô-los. Use ao mexer em data/remote, server/, relay-artesp, no banner de clima, no catálogo de integrações do Perfil ou em rótulos "ao vivo"/"online".
---

# Integrações e honestidade de dados

## Finalidade
Garantir que nenhuma tela apresente dado fixo como medição real nem uma fonte como oficial sem ser.

## Entradas
- App: `data/remote/` (`BackendHttp`, `CitizenBackendApiService`, `AuthApi`, `SosApi`, `TransitApi`, `SpPublicSafetyApiService`), `data/repository/`.
- Servidor: `server/src/main/kotlin/.../routes`, `transit/TransitService.kt`, `config/AppConfig.kt`.
- Relay: `relay-artesp/api/status.js`.

## Vocabulário de estado (usar nos rótulos da UI)
| Estado | Quando usar |
| --- | --- |
| `LIVE` | A última chamada HTTP desta sessão retornou 200 e o parsing produziu o dado exibido |
| `OFFLINE` | Há código de consulta, mas a última tentativa falhou; a UI mostra dado de exemplo |
| `CHECKING` | Ainda não houve tentativa nesta sessão |
| `DEMO` | Não existe consulta; o dado é fixo no código |
| `NOT_IMPLEMENTED` | Recurso anunciado/planejado sem nenhum código (Gemini, FCM, Flood Hub…) |

Nunca exibir latência, "ONLINE" ou "sincronizado" sem medição real.

## Procedimento ao criar ou alterar uma integração
1. Ler a documentação oficial do endpoint; registrar URL, formato e licença/termos.
2. Retornar do serviço um resultado que diga se foi ao vivo (`isLive…`), em vez de mascarar a falha.
3. Fallback: rotular como "exemplo offline"/"demonstração" — nunca "cache" se não houver cache real.
4. Atribuição: nomear a fonte real (ex.: "OpenStreetMap (Overpass)", não "Smart Sampa").
5. Não somar dados de fontes diferentes num mesmo número (ex.: contagem OSM + base fixa).
6. Atualizar o catálogo do Perfil (`getIntegratedApisCatalog`) e os testes do app/servidor.
7. Rodar `rede-cidada-validacao-mobile`.

## Chaves e segredos
- `MAPS_API_KEY`: `local.properties` (não versionado) ou variável de ambiente. Conferir presença com `grep -q MAPS_API_KEY local.properties` — nunca imprimir o valor.
- Chave Android de Maps vai no APK; proteger por restrição de package/SHA-1 no Google Cloud, não por ocultação.
- `BACKEND_URL`: debug usa `10.0.2.2:8080` (servidor local). Produção (Railway) só para release.
- Servidor: `JWT_SECRET`, `ADMIN_TOKEN`, `SPTRANS_TOKEN`, `ARTESP_API_KEY` em `server/secrets.properties` (local) ou variáveis do Railway.
- Nunca versionar tokens, `local.properties`, `server/secrets.properties`, `keystore.properties`, keystores, `.mcp.json`.

## Backend
Já existe servidor próprio (Ktor + SQLite + Railway). Não introduzir Supabase/Firebase em paralelo sem decisão do usuário. Mudanças de esquema, retenção ou LGPD (relatos, localização e mídia são dados sensíveis) exigem confirmação.

## Limites
Não chamar endpoints que publiquem, enviem SMS/push ou acionem autoridades. Consultas de leitura públicas são aceitáveis em testes manuais; testes unitários não fazem rede.

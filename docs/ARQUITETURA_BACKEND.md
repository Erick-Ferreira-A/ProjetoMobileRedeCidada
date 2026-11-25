# Rede Cidadã — recomendação de back-end, cadastro e integrações

24/11/2026. Base: código atual (servidor Ktor do Fred no Railway) + pesquisa de preços e APIs na mesma data.
Preços e cotas mudam: conferir nas páginas oficiais antes de assinar (links no fim).

## 1. Ponto de partida (o que já existe)

| Peça | Hoje | Funciona para 15 pessoas? |
| --- | --- | --- |
| Servidor | Kotlin + Ktor 3.6, Docker no Railway | Sim |
| Banco | SQLite (um arquivo no volume `/data`) | Aguenta, mas é o ponto fraco para crescer (concorrência de escrita, consultas sociais, backup) |
| Mídia | Arquivos no mesmo volume (`/data/uploads`) | Volume do Hobby tem teto de 5 GB e vídeo passa pelo servidor |
| Cadastro | E-mail + senha (BCrypt) + JWT | Sim |
| Push | Não tem | Falta para SOS, comentários, menções, alertas |
| Integrações | Open-Meteo, ARTESP (via relay na Vercel), SPTrans | Sim |

## 2. Recomendação: evoluir o servidor que já existe (não trocar de plataforma)

Manter **Ktor no Railway** e fortalecer três pontos. Não reescrever o app para Supabase/Firebase agora.

| Camada | Trocar por | Por quê |
| --- | --- | --- |
| Banco | **PostgreSQL** (serviço do próprio Railway) + extensão **PostGIS** | Escritas simultâneas, consultas de rede social (quem segue quem, comentários, menções), raio geográfico de verdade, backup com `pg_dump` |
| Mídia (fotos e vídeos) | **Cloudflare R2** com upload direto do app (URL assinada) | 10 GB grátis/mês e **saída gratuita** (vídeo é o que mais gasta banda); o servidor não carrega o vídeo |
| Vídeo no aparelho | Comprimir antes de enviar (**Media3 Transformer**, 720p, até ~60 s) + reproduzir com **ExoPlayer** | Arquivo pequeno, feed preenchendo a tela, sem serviço pago de transcodificação |
| Push | **Firebase Cloud Messaging (FCM)** | Grátis em qualquer volume; usado só para notificação (o resto continua no nosso servidor) |
| Cadastro | Manter e-mail/senha; adicionar **entrar com Google** (Credential Manager) e verificação de e-mail | Sem custo; **evitar login por SMS** (cobrado por mensagem) |
| Backup | `pg_dump` diário enviado ao R2 | Hoje não há backup automático (item 🤖 pendente do checklist) |

**Por que não Supabase/Firebase como back-end principal agora**
- Exigiria reescrever cadastro e dados no app e jogar fora o servidor que já está em produção e testado (33 testes).
- Supabase grátis: 500 MB de banco, **1 GB de arquivos**, 5 GB de saída/mês e o projeto **pausa após inatividade**; com vídeo, vira o plano Pro (US$ 25/mês).
- Faz sentido reavaliar se o app passar de algumas centenas de usuários ativos ou se a equipe quiser tempo real pronto (Supabase Realtime) para comentários ao vivo.

## 3. Custos para o teste com ~15 pessoas

| Item | Custo | Observação |
| --- | --- | --- |
| Railway Hobby (servidor + Postgres) | **US$ 5/mês** (≈ R$ 30) | Inclui US$ 5 de uso; acima disso cobra o excedente. Teste grátis termina por volta de **18/12** (checklist) |
| Cloudflare R2 | **R$ 0** até 10 GB | 15 pessoas × ~10 vídeos de 15 MB ≈ 2,3 GB |
| FCM | **R$ 0** | — |
| Google Maps (SDK Android) | Conferir no console do Google Cloud | Chave já configurada; restringir ao pacote `br.com.redecidada.app` (item pendente do checklist) |
| Play Console | **R$ 0 para o grupo** | A conta é do professor (a taxa de US$ 25 já foi paga por quem criou a conta) |
| Domínio próprio | Opcional | Não é necessário agora (o endereço do Railway serve) |

**Total previsto: ~US$ 5/mês.** Precisa cadastrar cartão no Railway e na Cloudflare (o R2 pede cartão mesmo dentro da cota grátis).

## 4. Publicar com a conta do professor

- O app aparece na loja com o **nome do desenvolvedor da conta do professor**. Ele precisa adicionar vocês como usuários no Play Console (permissões de versão e ficha da loja).
- **Teste fechado de 12 pessoas por 14 dias:** obrigatório para contas **pessoais criadas depois de 13/11/2023**. Se a conta do professor for de organização ou mais antiga, pode não se aplicar — confirmar com ele. O plano de 15 testadores já cobre o caso mais exigente.
- Conteúdo gerado por usuários (relatos, vídeos, comentários) exige no app: **denunciar**, **bloquear** e moderação — já existem e precisam continuar em comentários e perfis novos.
- Política de privacidade e "Segurança dos dados" precisam listar tudo o que for coletado (foto de perfil, telefone, endereço, comentários, seguidores). **Documentos (CPF/RG) continuam fora** (decisão de 24/11/2026).

## 5. Funcionalidades sociais pedidas → o que o servidor precisa

| Funcionalidade | Tabelas / rotas novas |
| --- | --- |
| Perfil completo e foto | `profiles` (bio, foto no R2, nascimento, bairro), `PUT /profile` ampliado |
| Comentar no vídeo/relato | `comments` (com resposta a comentário), denúncia de comentário |
| Marcar outra pessoa (@) | `mentions` + notificação push |
| Avaliar notícia | `ratings` (confirmo / não é verdade / desatualizado) — alimenta o score |
| Score de confiabilidade | `trust_events` (histórico de cada ponto ganho/perdido) + cálculo; hoje o score é fixo em 940 |
| Seguir páginas | `pages` (Defesa Civil, CET, associações de bairro, escolas) + `page_follows` |
| Amigos e família | `connections` (pedido/aceite, tipo amigo/família) — guardiões viram uma relação de família |
| SOS | já existe; com push, o guardião que tem o app recebe na hora (hoje é WhatsApp) |
| Notificações | `notifications` + FCM |

Ordem sugerida (cada etapa testável com os 15 usuários):
1. Postgres + R2 + backup (base robusta). **Postgres feito** no branch `agent/claude-postgres` (25/11/2026): Flyway, FKs, BOOLEAN/TIMESTAMPTZ; ainda sem PostGIS, R2 e backup.
2. Perfil completo + score com histórico + avaliar notícia.
3. Comentários + menções + push.
4. Amigos/família + seguir páginas.
5. Integrações oficiais (seção 6).

## 6. Integrações em tempo real (Brasil)

| Fonte | Situação encontrada | Como integrar |
| --- | --- | --- |
| **Defesa Civil (IDAP)** | Alertas oficiais são armazenados no padrão **CAP** e são públicos por norma (Portaria MIDR 2.216/2023, art. 7º); **não achamos URL de feed/API documentada** | Pedir o endpoint à Defesa Civil Nacional (Cenad) ou consumir via Google Alertas Públicos; enquanto isso, página "Defesa Civil" no app com link oficial |
| **INMET (avisos)** | INMET registrado como autoridade de alertas na OMM, com feed CAP; feed RSS reportado em `apiprevmet3.inmet.gov.br/avisos/rss` | Ler o CAP/RSS no servidor e cruzar com a localização (substitui a leitura por texto de hoje) |
| **CEMADEN** | Seção "Dados Abertos" no site; webservice de pluviômetros citado em trabalhos acadêmicos, sem documentação atual | Contato com o CEMADEN; usar para chuva acumulada por bairro |
| **Câmeras de SP** | CET publica imagens ao vivo no site (Câmeras CET) e a Prefeitura tem o City Câmeras; **sem API pública**; Smart Sampa não é aberto | Só com autorização da CET/Prefeitura; até lá, link para o site oficial |
| **SPTrans Olho Vivo / ARTESP** | Já integrados | — |
| **Open-Meteo** | Já integrado | — |
| **Fogo Cruzado** | API gratuita com cadastro, mas cobre Rio, Recife, Salvador e Belém, **não SP** | Só se o app expandir para essas regiões |

## Fontes

- Railway: [planos e preços](https://docs.railway.com/pricing/plans), [FAQ de preços](https://docs.railway.com/reference/pricing/faqs), [limite de volume no Hobby](https://station.railway.com/questions/hobby-plan-is-volume-storage-limit-5-f29be926)
- Supabase: [preços](https://supabase.com/pricing.md), [limites do plano grátis em 2026](https://dev.to/mahdi_benrhouma_fe1c6005/supabase-free-tier-limits-in-2026-what-you-actually-get-and-when-you-will-hit-them-4mhe)
- Cloudflare R2: [preços](https://developers.cloudflare.com/r2/pricing)
- Firebase: [preços (FCM sem custo)](https://firebase.google.com/pricing?hl=pt-br), [Identity Platform (SMS cobrado)](https://cloud.google.com/identity-platform/pricing)
- Google Play: [requisito de teste para contas pessoais novas](https://support.google.com/googleplay/android-developer/answer/14151465)
- Defesa Civil: [IDAP (Agência Gov)](https://agenciagov.ebc.com.br/noticias/202506/plataforma-que-integra-estados-na-prevencao-de-desastres-chega-a-todo-o-pais), [Portaria MIDR 2.216/2023](https://legismap.com.br/leis-e-normas/portaria-midr-n-2-216-de-04-07-2023), [regras de alertas (FNP)](https://fnp.org.br/noticias/item/3890-novas-regras-para-emissao-de-alertas-de-desastres-o-que-os-municipios-precisam-saber)
- INMET: [registro de autoridade de alertas (OMM)](https://alertingauthority.wmo.int/authorities.php?recId=22), [CAP no catálogo WIS2](https://wis2-gdc.weather.gc.ca/collections/wis2-discovery-metadata/items/urn:wmo:md:br-inmet:alerts?f=html)
- CEMADEN: [links úteis / dados abertos](https://www.gov.br/cemaden/pt-br/paginas/links-uteis)
- Câmeras: [Câmeras CET](https://www.cetsp.com.br/consultas/cameras-cet.aspx), [anúncio da Prefeitura](https://prefeitura.sp.gov.br/web/mobilidade/w/noticias/261491)
- Fogo Cruzado: [site](https://fogocruzado.org.br/english/)

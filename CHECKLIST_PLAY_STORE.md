# Checklist — do zero à Play Store

Criado em 18/11/2026. Marque `[x]` conforme for concluindo.

Legenda: **👤 Você** · **🤖 Claude** · **👥 Juntos**

---

## Etapa 0 — Decisões
- [x] 👤 Nome do app na loja → **Rede Cidadã**
- [x] 👤 Hospedagem do servidor → **Railway**
- [x] 👤 Domínio próprio? → **agora não** (endereço gratuito do Railway)
- [x] 👤 E-mail de suporte do app → **suporte.redecidada@gmail.com**
- [ ] 👤 Lista de **15 testadores** com e-mail Google (mínimo exigido: 12) — _em andamento_
- [x] 👥 Manter ou remover o CPF do cadastro → **remover**

## Etapa 1 — Correções obrigatórias antes do teste fechado
- [x] 🤖 Remover a conta demo `cidadao@redecidada.sp.gov.br` (virou `demo@redecidada.app`, e só existe fora de produção)
- [x] 🤖 Botão "Denunciar relato" + ocultar relato denunciado (some na hora para quem denunciou; com 3 denúncias, some para todos)
- [x] 🤖 Bloquear usuário + aceite dos termos de uso no cadastro (servidor recusa cadastro sem aceite)
- [x] 🤖 Moderação simples: `GET /api/v1/admin/reports`, `POST /admin/incidents/{id}/remove` e `/keep`, com cabeçalho `X-Admin-Token` (variável `ADMIN_TOKEN`)
- [x] 🤖 Modo visitante não voltar para a apresentação a cada abertura
- [x] 🤖 Chips de bairro seguirem a localização real (trocados por "Perto de você • bairro"; distância e clima vêm do GPS)
- [x] 🤖 "Eu também vi" contar no servidor (1 por pessoa)
- [x] 🤖 Relatos salvos só no aparelho mostrarem o tempo real
- [x] 🤖 Remover o CPF do cadastro (e os CPFs mascarados antigos foram apagados do banco)
- [ ] 👤 Testar no celular: SOS com WhatsApp real, câmera, vídeo, login, exclusão de conta

## Etapa 2 — Servidor na internet
- [x] 👤 Criar conta na hospedagem (Railway, teste de 30 dias / US$ 5 até ~18/12)
- [x] 🤖 Preparar o servidor para produção (`server/Dockerfile` + `railway.json`; testado em modo produção com ~160 MB)
- [ ] 🤖 Variáveis: `APP_ENV=production`, `JWT_SECRET` (32+ caracteres), `SEED_DEMO_DATA=false`, `SPTRANS_TOKEN`, `ARTESP_API_KEY`, `ADMIN_TOKEN` (24+ caracteres)
- [ ] 🤖 Disco persistente para o banco SQLite e a pasta de fotos/vídeos
- [ ] 🤖 Backup automático do banco
- [ ] 🤖 HTTPS funcionando, testado do celular pela rede 4G
- [ ] 🤖 App apontando para o endereço de produção (`BACKEND_URL` do release)
- [x] 🤖 HTTP sem criptografia só na versão debug
- [x] 🤖 IP real do usuário atrás do proxy do Railway (`TRUST_PROXY`), para o limite de login valer por pessoa
- [ ] 👤 Assinar o plano Hobby antes de ~18/12 (fim do teste do Railway)

## Etapa 3 — Versão de release (.aab)
- [ ] 👤 Criar a keystore de upload e guardar arquivo + senha em 2 lugares seguros
- [ ] 👤 Criar `keystore.properties` (nunca vai para o Git)
- [ ] 🤖 Minificação (R8) + regras para Ktor/Room, testando se nada quebrou
- [ ] 🤖 `versionCode = 1`, `versionName = 1.0.0`
- [ ] 🤖 Gerar o `.aab` assinado
- [ ] 👥 Instalar a release no celular e testar tudo de novo
- [ ] 👤 Restringir a chave do Google Maps (só Android, pacote `br.com.redecidada.app`, SHA-1 de upload e da assinatura do Google)

## Etapa 4 — Páginas obrigatórias (link público)
- [ ] 🤖 Política de privacidade
- [ ] 🤖 Página web de exclusão de conta
- [ ] 🤖 Termos de uso (regras de conteúdo; o app não substitui o 190)
- [ ] 👥 Publicar as 3 páginas (GitHub Pages ou o próprio servidor)

## Etapa 5 — Play Console (configuração)
- [ ] 👤 Verificação de identidade da conta de desenvolvedor concluída
- [ ] 👤 Criar o app (nome, pt-BR, gratuito)
- [ ] 👥 Acesso ao app: conta de teste para o revisor do Google
- [ ] 👥 Anúncios: "não contém anúncios"
- [ ] 👥 Classificação de conteúdo (IARC): conteúdo gerado por usuário, localização compartilhada
- [ ] 👥 Público-alvo: 18+
- [ ] 👥 Data safety: localização precisa, fotos/vídeos, nome, e-mail, contatos (guardiões); criptografia em trânsito; exclusão disponível
- [ ] 👥 Declarar que **não** é app de governo
- [ ] 👥 Permissões: localização só em primeiro plano + câmera, com justificativa
- [ ] 👤 Categoria: "Mapas e navegação" ou "Social"

## Etapa 6 — Ficha da loja
- [ ] 🤖 Descrição curta (até 80 caracteres)
- [ ] 🤖 Descrição completa (até 4000 caracteres), sem prometer o que o app não faz
- [ ] 👥 Ícone 512×512 PNG
- [ ] 👥 Imagem de destaque 1024×500
- [ ] 👥 Prints do celular (mínimo 2, ideal 4 a 6)
- [ ] 👤 E-mail de contato e link da política de privacidade

## Etapa 7 — Teste fechado (14 dias)
- [ ] 👤 Criar a faixa de teste fechado e enviar o `.aab`
- [ ] 👤 Adicionar os e-mails dos testadores
- [ ] 👤 Enviar o link de inscrição; cada testador aceita e instala pela Play Store
- [ ] 👤 Confirmar 12+ inscritos (o relógio de 14 dias começa aí)
- [ ] 👤 Pedir uso real (abrir, reportar, testar o SOS)
- [ ] 👥 Coletar feedback (formulário ou grupo)
- [ ] 🤖 Corrigir e enviar atualizações durante o teste (o relógio não zera)

## Etapa 8 — Pedido de produção
- [ ] 👤 Após 14 dias: "Solicitar acesso à produção"
- [ ] 👥 Responder ao questionário do Google
- [ ] 👤 Aguardar a análise (normalmente até 7 dias)
- [ ] 👤 Publicar com lançamento gradual (20% → 100%)

## Etapa 9 — Depois de publicado
- [ ] 🤖 Monitorar erros (Play Console → Android vitals)
- [ ] 👤 Responder às avaliações
- [ ] 👥 Moderar as denúncias de relatos
- [ ] 🤖 Conferir os backups do servidor

---

## Cronograma
| Data | O quê |
|---|---|
| 18–19/11 | Etapas 0, 1 (essencial), 2 e 3 |
| 20/11 | Etapas 4, 5 e 6 → subir o teste fechado |
| 20/11 – 04/12 | 14 dias de teste + ajustes |
| ~04/12 | Pedido de produção |
| ~05–11/12 | Análise do Google → publicação |

## Decisões tomadas
- 18/11: nome na loja **Rede Cidadã** (sem "SP").
- 18/11: servidor no **Railway**, com disco persistente (~US$ 5/mês).
- 18/11: sem domínio próprio por enquanto; usar o endereço `*.up.railway.app`.
- 18/11: **remover o CPF** do cadastro.
- 18/11: e-mail de suporte **suporte.redecidada@gmail.com** (vai na loja, na política de privacidade e nos termos).

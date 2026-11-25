# Checklist: o que falta para a Play Store

Atualizado em **25/11/2026**. Tudo o que o Claude podia fazer sozinho já foi feito (lista no fim).
Aqui ficam **só as pendências que dependem de pessoas**.

Legenda: **🔑 Marnes** (dono da conta do Play Console) · **👤 Fred** · **🎨 Erick** (telas do app)

> **Caminho crítico:** conta do Play Console liberada → 12+ testadores inscritos → **14 dias** de teste fechado → análise do Google.
> Cada dia de atraso na conta empurra a publicação um dia.

---

## 1. Urgente (bloqueia tudo)
- [ ] 🔑 Play Console: declarar o status de comerciante (app gratuito, sem vendas → "não sou comerciante")
- [ ] 🔑 Play Console: verificar o telefone de contato (código por SMS)
- [ ] 🔑 Play Console: confirmar o aparelho Android (instalar o app **Google Play Console** no celular e entrar com a mesma conta)
- [ ] 🔑 Play Console: **Usuários e permissões → Convidar usuário** → e-mail do Fred como administrador, para ele seguir sem depender do Marnes
- [ ] 👤 Railway: **assinar o plano Hobby** (US$ 5/mês). O crédito do teste agora paga servidor + Postgres e pode acabar antes de ~18/12
- [ ] 👤 Lista de **15 e-mails** de testadores (contas Google da Play Store) e avisar que precisam instalar e **usar por 14 dias**

## 2. Telas do app (Erick decide; o Claude não mexeu nas telas)
- [ ] 🎨 **Onboarding, passo 2:** o card "Confiabilidade do relato · 92% · Avaliação de outros moradores + **análise automática**" promete algo que não existe (não há IA; o score é fixo). Promessa falsa é motivo comum de recusa. Sugestão: trocar por "Moradores também viram" (contagem de "Também vi", que já funciona)
- [ ] 🎨 **Perfil → Sua confiabilidade:** conta nova começa com score **0**, o que mostra "Em revisão: seus relatos passam por revisão", e isso não acontece. Sugestão: esconder o número até o score existir. O texto "todas as contas começam com 940" também não é verdade (só a demo)
- [ ] 🎨 **Relatar:** o card verde do "Publicar como anônimo" está com o texto quase invisível, e o botão "Galeria" quebra em duas linhas ("Galeri / a")
- [ ] 🎨 Se mudar alguma tela: subir `versionCode` (hoje 2) e pedir ao Claude um `.aab` novo

## 3. Criar o app e preencher o Console (depois do bloco 1)
> Respostas prontas em [`PLAY_CONSOLE_RESPOSTAS.md`](PLAY_CONSOLE_RESPOSTAS.md). Imagens prontas em [`loja/`](loja/).
- [ ] 👤 Criar o app: **Rede Cidadã**, pt-BR, App, Gratuito
- [ ] 👤 Criar pelo app uma conta só para o revisor do Google (senha no gerenciador de senhas) e colocar em **Acesso ao app**
- [ ] 👤 Preencher: anúncios (não), classificação IARC, público 18+, segurança dos dados, apps governamentais (não)
- [ ] 👤 Política de privacidade: https://projetomobileredecidada-production.up.railway.app/privacidade
- [ ] 👤 Exclusão de conta: https://projetomobileredecidada-production.up.railway.app/excluir-conta
- [ ] 👤 Ficha da loja: textos do guia + `loja/icone-512.png` + `loja/destaque-1024x500.png` + os 4 prints de `loja/prints/`

## 4. Teste fechado (14 dias)
- [ ] 👤 Criar a faixa **Teste fechado** e enviar `app/build/outputs/bundle/release/app-release.aab` (versão 1.0.1)
- [ ] 👤 Aceitar o **Play App Signing** e copiar o **SHA-1 da assinatura do Google** (Configuração → Integridade do app)
- [ ] 👤 No Google Cloud, adicionar esse SHA-1 (e o de upload `90:C3:D0:BD:C5:8B:9F:EA:0E:74:85:0C:FB:29:DC:A1:0F:2A:E9:D2`) à chave do Google Maps, com o pacote `br.com.redecidada.app`. Sem isso o mapa pode não abrir para os testadores
- [ ] 👤 Adicionar os e-mails dos testadores e mandar o link de inscrição
- [ ] 👤 Confirmar **12+ inscritos** (o relógio de 14 dias começa aí)
- [ ] 👤 Grupo ou formulário para os testadores mandarem problemas

## 5. Pedido de produção
- [ ] 👤 Depois dos 14 dias: **Solicitar acesso à produção** e responder ao questionário
- [ ] 👤 Aguardar a análise (até ~7 dias) e publicar com lançamento gradual

## 6. Quando der (não bloqueia)
- [ ] 👤 Guardar a senha da keystore no gerenciador de senhas e uma cópia do `upload-keystore.jks` fora do PC
- [ ] 👤 Conta no Cloudflare R2 (grátis), se quiser backup diário do banco fora do Railway
- [ ] 👤 E-mail à ARTESP pedindo liberação do servidor (hoje funciona pelo intermediário na Vercel)
- [ ] 👤 SPTrans: o token continua recusado; os ônibus aparecem como indisponíveis
- [ ] 📅 ~09/12: pedir ao Claude para apagar o SQLite antigo (`/data/rede_cidada.db`) no Railway

---

## ✅ Feito
- **Decisões:** nome Rede Cidadã · Railway · sem domínio · sem CPF · suporte.redecidada@gmail.com
- **Servidor:** HTTPS no Railway; **Postgres em produção desde 25/11** (dados migrados, sessões mantidas); trilhos via intermediário em SP; denunciar, bloquear e moderação
- **Páginas públicas:** privacidade (atualizada em 25/11: dados do "Editar perfil" ficam só no celular), termos e exclusão de conta
- **Versão de loja 1.0.1:** `.aab` assinado com a chave de upload (SHA-1 conferido), R8, 53 testes passando e lint sem erros; testada no emulador contra a produção. As telas estão iguais às da `main` do Erick
- **Imagens da loja:** ícone 512×512, destaque 1024×500 e 4 prints 1080×2160 (início, relatos, SOS, mobilidade), feitos com relatos de exemplo num servidor local, nunca em produção
- **Guias:** respostas do Play Console e textos da loja

## Cronograma realista
| Quando | O quê |
|---|---|
| até ~27/11 | Bloco 1 (Marnes e Fred) |
| ~28/11 | Console preenchido + `.aab` enviado → **teste fechado aberto** |
| 28/11 → ~12/12 | 14 dias de teste com 12+ pessoas |
| ~12/12 | Pedido de produção |
| **~15–20/12** | Análise do Google → **publicação** |

## Decisões tomadas
- 18/11: nome **Rede Cidadã**; servidor no **Railway**; sem domínio próprio; **sem CPF**; suporte **suporte.redecidada@gmail.com**.
- 19/11: páginas legais servidas pelo próprio servidor.
- 25/11: banco de produção trocado de SQLite para **Postgres**.
- 25/11: publicação pela conta pessoal de Marnes Adão Simão Cassule.
- 25/11: telas e textos do app são do Erick; o Claude não altera o frontend.

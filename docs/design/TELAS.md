# Rede Cidadã — especificação das telas (v1.0)

Como cada tela funciona no novo estilo. Base visual em [`GUIA_VISUAL.md`](GUIA_VISUAL.md).
Escrito em 23/11/2026 sobre o código da `main` @ `eafae4c`. Telas novas devem seguir os mesmos moldes (seção 3).

**Premissas provisórias** (até o grupo decidir):
- Público principal: quem se desloca de transporte público.
- Laranja: marca e ação. Nunca nível de alerta.

---

## 1. Arquitetura de navegação

### Hoje
Barra inferior: Mapa & Feed · Mobilidade · **Reportar** (centro) · Vídeos · SOS. Barra superior com atalhos de SOS e Perfil. O SOS aparece duas vezes e o Perfil fica escondido no topo.

### Proposta

```
┌───────────────────────────────────────────┐
│ Boa noite · Capão Redondo          [🛡]   │  ← barra superior: contexto + SOS (escudo)
│                                           │
│               conteúdo da aba             │
│                                           │
├───────────────────────────────────────────┤
│ Início  Mobilidade   [＋]   Vídeos  Perfil │  ← 4 destinos + ação central
└───────────────────────────────────────────┘
```

| Item | Rota | Por quê |
| --- | --- | --- |
| **Início** | `home` | "O que preciso saber agora": resumo, mapa e alertas perto de mim |
| **Mobilidade** | `mobility` (hoje `metro`) | Trilhos, ônibus e, depois, rotas |
| **＋ Relatar** | sheet `report` | Ação principal do app, no polegar (padrão Instagram/TikTok/Threads) |
| **Vídeos** | `media` (hoje `reels`) | Feed vertical de fotos e vídeos da comunidade |
| **Perfil** | `profile` | Conta, meus relatos, guardiões, preferências, privacidade |
| **SOS** (escudo, topo) | `sos` | Sempre a um toque em qualquer aba, sem disputar espaço com navegação comum (padrão do "safety toolkit" da Uber). O vermelho só aparece dentro da tela |

**Regras de navegação**
- Trocar de aba preserva rolagem e filtros (`saveState`/`restoreState`, já usado hoje).
- Tocar de novo na aba ativa volta ao topo da lista.
- Voltar no Android: de qualquer aba vai para Início; do Início, sai do app. Sheets fecham antes.
- Telas de detalhe (alerta, vídeo, guardião) abrem **por cima** da aba, sem trocar a aba ativa.
- Links profundos futuros: `redecidada://alerta/{id}`, `redecidada://video/{id}`.
- Animação de troca de aba: fade de 150 ms. Telas empilhadas: deslizam 24 dp + fade em 250 ms. Nada de slide lateral inteiro.

### Mapa de telas

```
Onboarding (3 passos) → Localização → Entrar / Criar conta / Continuar sem conta
                                                │
                    ┌──────────────┬────────────┼────────────┬───────────┐
                  Início       Mobilidade     ＋Relatar      Vídeos      Perfil
                    │              │          (3 passos)     │            │
             Detalhe do alerta  Linha/parada       │      Visualizador  Meus relatos
             Mapa em tela cheia                Revisar      (pager)     Guardiões
                                                            │           Preferências
                                                     Detalhe do alerta  Fontes de dados
                                                                        Privacidade/Termos
                                                                        Excluir conta
           🛡 SOS (de qualquer aba) → Acionado → "Estou bem"
```

---

## 2. Telas

Cada tela tem: **pergunta** que responde · **frase-resumo** (headline) · **anatomia** de cima para baixo · **interações** · **estados** · **fora desta tela**.

### 2.1 Início (`home`) — revisado com o usuário em 23/11/2026

- **Pergunta:** "Está tudo bem no meu caminho agora?"
- **Estrutura:**
  1. Cabeçalho: saudação + bairro · escudo SOS.
  2. **Mapa ocupando quase a tela toda** (entre cabeçalho e navegação), livre para navegar. Sem card, sem mancha de calor, sem rótulo de bairro duplicado.
  3. Sobre o mapa, no topo: **Filtros** (mostra quantos estão ativos: "Filtros · 2") e **busca**. Botão "Minha localização" no canto inferior direito, acima do painel.
  4. **Painel de baixo** (padrão Google Maps/Airbnb):
     - Recolhido: frase-resumo (dinâmica) e, logo abaixo, o **clima com ícone** (ensolarado, céu limpo à noite, parcialmente nublado, nublado, garoa, chuva, tempestade) + "N relatos em X km".
     - Puxando para cima: aviso "Relatos de moradores…", cards de alerta por relevância e o fim da lista com "Aumentar o raio" / "Limpar filtro".
- **Painel de Filtros** (sheet): "O que mostrar" com várias categorias ao mesmo tempo ("Tudo" limpa) + "Raio a partir de você" (1 · 2 · 5 km) + "Limpar" e "Mostrar N relatos" (conta antes de aplicar). Só aplica ao confirmar.
- **Fora do Início:** cards grandes de clima e trilhos (trilhos ficam em Mobilidade), título grande, chips soltos na tela, alternância Lista | Mapa.
- **Frase-resumo (dinâmica):**
  - Nada perto: "Seu caminho está tranquilo agora."
  - Alertas: "Atenção a N pontos perto de você."
  - Crítico a menos de 500 m: "Evite a Rua X agora."
  - Busca de relatos falhou: "Não consegui ver os alertas agora." (**nunca** "tranquilo" sem ter conseguido ver).
- **Interações:** tocar no card ou no pino abre o Detalhe · "Também vi" e "Compartilhar" direto no card · avisos do app aparecem como snackbar, sem empurrar o mapa.
- **Implementação:** `BottomSheetScaffold` (painel), `ModalBottomSheet` (filtros), lógica do resumo em `ui/home/HomeSummary.kt` com testes.
- **Nunca pintar regiões de vermelho/verde.**

### 2.2 Mapa em tela cheia (`home/map`)

- **Pergunta:** "Onde exatamente estão as ocorrências?"
- **Anatomia:** mapa ocupando tudo · busca de endereço no topo · chips de categoria · botão "Minha localização" · sheet recolhida com a contagem ("8 alertas nesta área"), que sobe para virar a lista.
- **Pinos:** ícone da categoria num círculo com a cor da função (crítico/atenção/info). Pinos agrupados com número quando se sobrepõem. O pino selecionado cresce 1,2× e abre o card na sheet.
- **Fora:** mancha de calor (heatmap), pelo mesmo motivo de estigma.

### 2.3 Relatar (sheet em tela cheia, 3 passos)

- **Pergunta:** "O que está acontecendo, e onde?"
- **Por que 3 passos:** cada passo tem uma decisão só, e dá para relatar em menos de 15 s.
  1. **O quê:** grade 2 × 4 de categorias com ícone e nome (Assalto/roubo, Incêndio, Alagamento, Tempestade, Trânsito, Polícia, Iluminação/via, Outro). Toque avança.
  2. **Onde:** mapa já na posição atual, com pino arrastável e endereço aproximado embaixo. Botão "Usar este local".
  3. **Detalhes (tudo opcional):**
     - Texto curto (dica: "Não escreva nomes, placas nem endereços de casas").
     - Fotos e vídeos (câmera ou galeria, até 4).
     - Chave **Publicar como anônimo**, ligada por padrão.
- **Revisar:** prévia do card exatamente como os outros vão ver + botão de destaque "Publicar".
- **Depois de publicar:** fecha a sheet, volta ao Início com o novo card no topo e um snackbar "Relato publicado · Desfazer" (10 s).
- **Estados:** enviando (botão com progresso, sheet não fecha) · falha (rascunho guardado, "Tentar de novo") · sem conta (pede login no passo de revisar, sem perder o rascunho).
- **Regras:** indicador de passo (1/3) no topo · voltar retorna ao passo anterior · arrastar para baixo pergunta "Descartar relato?" se houver conteúdo.

### 2.4 Detalhe do alerta (sheet sobre a tela atual)

- **Pergunta:** "Isso é confiável e o que eu faço?"
- **Anatomia:**
  1. Categoria · distância · horário.
  2. Título.
  3. Mídia em carrossel (abre o visualizador de Vídeos).
  4. Descrição.
  5. **Linha do tempo honesta:** "Relatado 18:20 · 12 pessoas confirmaram · última confirmação 18:38".
  6. Selos de origem.
  7. Mini mapa do local.
  8. Dica de segurança (se houver), escrita por pessoas e sem atribuir a "IA".
- **Ações:** **Também vi** (primária) · "Não está mais acontecendo" · Compartilhar · ⋯ (Denunciar, Bloquear autor).
- **Estados:** removido pela moderação ("Este relato foi removido") · relato próprio (ações viram Editar e Excluir).
- A sheet abre direto em altura cheia (sem estado parcial), como já foi corrigido antes.

### 2.5 Vídeos — feed vertical (`media`)

Referência: Reels/TikTok/Shorts, com o propósito trocado: **informar e não prender**.

- **Pergunta:** "O que a comunidade está vendo perto de mim?"
- **Layout:**
  - Tela cheia, borda a borda, **sempre escura** (independente do tema). As barras do sistema ficam transparentes sobre a mídia.
  - `VerticalPager`, um item por página.
  - Barra inferior continua visível (padrão Instagram).
- **Camadas sobre a mídia:**
  - **Topo:** "Perto de você" + seletor de raio (1 · 2 · 5 km) · botão de som.
  - **Trilho direito** (de baixo para cima, alvos de 48 dp, ícone + número):
    - **Também vi** (com contagem).
    - **Ver no mapa.**
    - **Compartilhar.**
    - **⋯** (Denunciar, Bloquear, Não quero ver isso).
  - **Base:**
    - Chip da categoria · distância · "há 20 min".
    - Título (2 linhas) e descrição recolhida ("mais").
    - Selo "Relato de morador" (ou autor, se não for anônimo).
    - Barra de progresso fina do vídeo.
  - Gradiente escuro só na base e no topo (32 % → 0), para o texto ser legível sobre qualquer imagem.
- **Interações:**
  - Deslizar para cima/baixo troca de item (snap).
  - Toque simples pausa/retoma.
  - **Toque duplo = "Também vi"**, com animação curta do ícone. O "curtir" foi trocado de propósito por confirmação.
  - Segurar = pausa enquanto segura.
  - Tocar no título abre o Detalhe do alerta.
- **Reprodução:**
  - Autoplay **sem som** por padrão. O som liga com um toque e a escolha vale para a sessão.
  - Pré-carrega o próximo item.
  - Em dados móveis com economia de dados ligada: não toca sozinho (mostra a capa com ▶).
  - Fotos ficam paradas, com toque duplo para zoom.
- **Conteúdo sensível:** itens com denúncia de violência ou marcados como sensíveis aparecem desfocados com "Pode conter imagem forte · Ver mesmo assim".
- **Fim do feed:** "Você viu tudo das últimas 24 h perto de você." + "Aumentar o raio". **Sem rolagem infinita** nem recomendação de outras regiões para prender a pessoa.
- **Estados:** carregando (capa com skeleton) · vazio ("Ainda não há fotos ou vídeos perto de você" + "Relatar") · erro de mídia (capa + "Não foi possível carregar · Tentar de novo").
- **Implementação:**
  - Hoje o app usa `VideoView`. Migrar para **Media3 ExoPlayer** (`androidx.media3`) com um player reaproveitado entre páginas.
  - Imagens com cache: Coil 3.
  - Requer adicionar dependências: decisão técnica, sem custo.

### 2.6 Mobilidade (`mobility`)

- **Pergunta:** "Consigo ir de transporte agora?"
- **Frase-resumo:** "Linhas operando normalmente." / "Linha 5 com velocidade reduzida."
- **Anatomia:**
  1. Segmentado **Trilhos | Ônibus** (depois: **Rotas**).
  2. **Trilhos:** lista de linhas com faixa de cor oficial, número, nome e status em texto (nunca só cor), com selo "Fonte: ARTESP · 18:35".
  3. **Ônibus:** busca de linha ou parada (SPTrans) → paradas → previsões de chegada.
- **Interações:** tocar numa linha abre o detalhe com as estações e alertas de relatos perto delas (cruza com o Início) · favoritar linha (vai para o topo).
- **Estados:** fonte fora do ar ("A ARTESP não respondeu. Último status às 18:12.") · sem favoritos (dica).
- **Fora:** score de rota segura sem metodologia. Quando houver Rotas, mostrar fatos (tempo, iluminação relatada, alertas no trajeto), não uma nota.

### 2.7 SOS (`sos`)

- **Pergunta:** "Preciso de ajuda agora."
- **Anatomia (repouso):**
  1. Título "Precisa de ajuda?".
  2. **Botão SOS** central (segurar 2 s, com anel de progresso).
  3. Texto curto do que vai acontecer: "Abre o WhatsApp com sua localização para Mariana e Carlos".
  4. Números de emergência em botões grandes: **190 Polícia · 193 Bombeiros · 192 SAMU · 199 Defesa Civil**. Toque abre o discador, **não liga sozinho**.
  5. Meus guardiões (lista + "Adicionar").
  6. Modo discreto (chave).
- **Acionado:**
  - Tela muda para "Aviso pronto". Diz o que foi feito ("Mensagem aberta no WhatsApp para 2 guardiões") e mostra os números de emergência.
  - Botão "Estou bem" encerra.
  - **Honesto:** o app não liga para a polícia, não envia sem a pessoa confirmar no WhatsApp e não rastreia em segundo plano.
- **Modo discreto:** tela escura, sem vibração longa, sem animação.
- **Estados:** sem guardiões (o botão continua funcionando para os números de emergência e pede "Adicione alguém de confiança") · sem conta (guardiões pedem login; os números de emergência funcionam sempre).
- **Regra de teste:** nunca acionar SOS real, ligar para os números nem abrir WhatsApp com guardião real em teste.

### 2.8 Perfil (`profile`) — implementado em 24/11/2026

Implementação atual: cabeçalho com foto/iniciais, nome, idade, bairro e e-mail · "Editar perfil" (com conta) ou "Entrar ou criar conta" (visitante) · card **Sua confiabilidade** (score 0–1000, faixa, barra e "Como funciona"; cálculo automático em integração, hoje todos começam com 940) · Meus relatos, Guardiões, Avaliações · Preferências · Fontes de dados · Sair / Excluir conta. **Editar perfil** (`profile/edit`): foto, nome, nascimento (idade, 18+), telefone, endereço; salvo no aparelho até o servidor aceitar os campos. **Documentos (CPF/CNPJ, RG):** visíveis e desativados — dependem de atualizar a política de privacidade (hoje diz que o app não coleta documentos).

Especificação original:

- **Pergunta:** "Como estou usando o app e o que ele sabe de mim?"
- **Anatomia:**
  1. Cabeçalho: nome (ou "Visitante") e bairro.
  2. Atalhos: **Meus relatos** · **Guardiões** · **Preferências**.
  3. Preferências:
     - Publicar como anônimo por padrão.
     - Raio de alertas.
     - Tema (Sistema / Claro / Escuro).
     - Vídeos: tocar sozinho (Sempre / Só no Wi-Fi / Nunca).
  4. Privacidade e dados: **Fontes de dados** (o catálogo atual) · Política de privacidade · Termos.
  5. Conta: Sair · **Excluir conta** (texto vermelho, confirmação em 2 passos).
- **Fora:** pontuação/"reputação" em destaque (incentiva competição por relatos). Se existir, fica dentro de Meus relatos, discreta.

### 2.9 Onboarding (3 passos)

| Passo | Frase | Conteúdo |
| --- | --- | --- |
| 1 | "Saiba o que acontece no seu caminho." | Clima, transporte e relatos num só lugar |
| 2 | "Avise sem se expor." | Relato anônimo por padrão; o que fica público e o que não fica |
| 3 | "Alguém de confiança, a um toque." | Guardiões e SOS. O app **não substitui o 190** |

- Ilustração ou foto real por passo (sem ilustração genérica de banco de imagens).
- "Pular" sempre visível e indicador de passo.
- Botão de destaque "Continuar".

**Achado de 23/11/2026:** hoje o link "Emergência? Ligar 190" fica cerca de 50 dp abaixo do botão principal no onboarding e na tela de localização. No teste, um toque destinado ao botão abriu o discador. **Regra:** atalho de emergência nunca fica a menos de 24 dp de outro alvo de toque nem na área onde o polegar repousa depois de tocar o botão principal. Nessas telas, ele vai para o topo (ao lado de "Pular"/"Voltar"), como ícone com rótulo.

### 2.10 Localização (`location`)

- **Pergunta:** "Por que o app precisa da minha localização?"
- **Anatomia:** três motivos curtos (alertas perto, clima do lugar, posição no SOS) · "Só enquanto o app está aberto" · botão "Permitir localização" · alternativa "Escolher bairro manualmente" (hoje a permissão é obrigatória; recomendo esta alternativa para a revisão da Play Store).

### 2.11 Entrar / Criar conta (`login`)

- Uma tela com abas **Entrar | Criar conta**.
- Campos com rótulo visível e o teclado certo (e-mail, senha com "mostrar").
- Erros que dizem como resolver.
- **Continuar sem conta** como botão de texto, explicando o que fica limitado (relatar, guardiões).
- Links: Termos e Privacidade.

### 2.12 Termos, privacidade e exclusão de conta

- Leitura confortável: bodyLarge, largura máxima de 600 dp, títulos de seção e índice no topo.
- Exclusão: explica o que é apagado e o que permanece por obrigação legal. Pede para digitar "EXCLUIR" ou confirmar com a senha.

---

## 3. Molde para telas novas

1. Escrever a **pergunta** da tela e a **frase-resumo** antes de desenhar.
2. Montar só com componentes do guia (`ui/designsystem`). Componente novo entra no guia antes de entrar na tela.
3. Definir os 4 estados: carregando, vazio, erro/offline, sem permissão ou sem conta.
4. Dizer de onde vem cada dado (selo de origem).
5. Uma ação principal por tela. Destaque laranja só se a ação for relatar.
6. Testar: tema claro e escuro, fonte 1,3× e 2,0×, TalkBack, tela de 360 dp de largura.

---

## 4. Ordem de implementação sugerida

1. Base do design system: tema, tipografia, componentes (branch `agent/claude-design-system`).
2. Navegação nova (barra inferior + escudo SOS).
3. Início → Detalhe do alerta → Relatar.
4. Vídeos (feed vertical com Media3).
5. SOS → Mobilidade → Perfil.
6. Onboarding → Localização → Login → Termos.

Cada etapa em PR próprio, com capturas claro/escuro.

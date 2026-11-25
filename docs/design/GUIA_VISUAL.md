# Rede Cidadã — guia visual (v1.0)

Consolidação de 23/11/2026. Substitui os rascunhos v0.1/v0.2 (pasta `Documents\REDE CIDADA\02_design\guia-visual`).
Especificação das telas: [`TELAS.md`](TELAS.md). Código: `ui/theme/` e `ui/designsystem/`.

---

## 1. Fundamentos

- **Objetivo:** ajudar quem anda pela cidade a decidir melhor o caminho, reunindo relatos da comunidade, clima, alagamentos e transporte. Também dá um jeito simples de avisar a família. **Não substitui 190/193/192/199.**
- **Nicho:** informação hiperlocal de deslocamento e segurança comunitária para quem anda a pé e de transporte público em São Paulo.
- **Público:** moradores, estudantes e trabalhadores de SP, 18+. Hipótese principal: quem se desloca de transporte público. Perfis:
  - Estudante que volta à noite.
  - Trabalhador(a) que sai cedo.
  - Morador(a) que cuida do bairro.

  São hipóteses a validar com 5–8 conversas.

### Princípios
1. **Informar sem alarmar.** Vermelho só para o que pede ação agora.
2. **Um objetivo por tela.** Cada tela responde a uma pergunta.
3. **Dizer de onde vem cada dado.** Ao vivo, relato de morador, fonte oficial, exemplo.
4. **Feito para uma mão, na rua.** Alvos grandes na metade de baixo, leitura em 3 segundos.
5. **Humano, não máquina.** Neutros quentes, conteúdo real, nada de brilho, vidro ou gradiente decorativo.

---

## 2. Cor

### 2.1 Psicologia aplicada

| Cor | O que comunica | Onde usar | Onde **não** usar |
| --- | --- | --- | --- |
| **Marinho** `#0A2540` | Estabilidade e confiança. Sério sem ser policial | Marca, texto sobre o laranja, momentos institucionais | Fundo de tela inteira (fica frio e "corporativo") |
| **Azul** `#1D3B8B` | Calma e competência; reduz a sensação de urgência | Ação primária, links, item ativo | Alertas: azul de alerta parece "informativo" e banaliza |
| **Laranja** `#FF6B00` | Energia, comunidade, calor humano, convite à ação | **Relatar** (ação de destaque), destaques da marca | Nível de alerta. Laranja = participar, não perigo |
| **Vermelho** (função *crítico*) | Perigo; dispara reação física (atenção, ansiedade) | SOS, ocorrência grave a < 500 m, erro destrutivo | Decoração, categorias comuns, contagem, badge |
| **Âmbar** (função *atenção*) | Cautela sem pânico | Chuva forte, linha com problema, alerta moderado | Ações |
| **Verde** (função *tranquilo*) | Normalidade, resolvido | "Ao vivo", "linhas normais", "não está mais acontecendo" | Pintar bairros como "seguros" |
| **Neutros quentes** | Papel, casa, luz baixa: conforto | Fundos, cards, texto | — |

**Regra de território:** nunca pintar regiões do mapa de vermelho/verde nem usar mancha de calor. Isso estigmatiza bairros periféricos, e estudos sobre apps como o Citizen mostram que esse tipo de visual aumenta medo e preconceito.

### 2.2 Por que neutros quentes (e não azul-escuro)

- O modo escuro anterior (fundo `#0B0F17`, cards `#192238`, borda "glow" `#2E3D66`) é um preto **azulado e saturado**. Lembra painel de ficção científica, é frio e é a assinatura visual mais comum de interfaces geradas por IA.
- O novo escuro usa cinzas com um traço de amarelo-terra (croma muito baixo). É a sensação de um cômodo com luz baixa: confortável à noite, quando a pessoa volta para casa.
- **Não usamos preto puro** (`#000`). Em telas OLED ele causa arrasto ao rolar e faz o texto claro "vazar" (halação).
- O novo claro usa um **off-white de papel** como fundo e branco só nos cards. Cansa menos que o branco puro chapado e dá profundidade sem sombra.
- A cor da marca aparece como **acento**, nunca como fundo de tela.

### 2.3 Tokens

| Token | Claro | Escuro | Uso |
| --- | --- | --- | --- |
| `background` | `#F6F4EF` | `#121110` | Fundo da tela |
| `surface` | `#FFFFFF` | `#1B1A18` | Cards, sheets |
| `surfaceContainer` | `#EEEBE5` | `#252321` | Campos, chips, faixas |
| `surfaceContainerHigh` | `#E6E2DA` | `#2F2D2A` | Pressionado, itens selecionados |
| `onSurface` (texto) | `#1D1C1A` | `#EDEAE4` | Texto principal |
| `onSurfaceVariant` | `#5C574F` | `#B3ADA3` | Texto de apoio |
| `outline` (contorno) | `#7D776D` | `#8A847A` | Borda de componente tocável (≥ 3:1) |
| `outlineVariant` (linha) | `#DDD8CF` | `#36332F` | Divisores e borda de card (decorativa) |
| `primary` | `#1D3B8B` | `#AFC1FF` | Ação primária, item ativo |
| `onPrimary` | `#FFFFFF` | `#0A2540` | Texto sobre primary |
| `accent` | `#FF6B00` | `#FF7A1A` | Botão de destaque (Relatar) |
| `onAccent` | `#0A2540` | `#0A2540` | Texto sobre o laranja (**nunca branco**: 2,86:1) |
| `accentText` | `#B44900` | `#FF9A55` | Laranja como texto (claro: só sobre `background`/`surface`) |
| `critical` | `#B91D20` | `#FFB4AC` | Crítico |
| `warning` | `#8A5100` | `#FFB86E` | Atenção |
| `safe` | `#1B6D24` | `#88D982` | Tranquilo / ao vivo |
| `info` | `#005DB7` | `#A9C7FF` | Informação |
| contêiner de função | 90 % do tom (claro) | cor da função a 16 % sobre `surface` | Fundo de selo e chip de função |

**Contraste medido (WCAG 2.x):**
- Texto principal ≥ 14,5:1 e texto de apoio ≥ 6,1:1 sobre `surface`, nos dois modos.
- Cores de função ≥ 6,4:1 (claro) e ≥ 10,1:1 (escuro) sobre `surface`.
- Contorno: 4,44:1 (claro) e 4,69:1 (escuro).
- Marinho sobre laranja: 5,44:1 (claro) e 5,96:1 (escuro).

### 2.4 Superfícies especiais
- **Vídeos (feed):** sempre fundo `#000000`, porque o conteúdo é a mídia. Texto branco com sombra suave e gradiente só nas bordas.
- **Mapa:** estilo de mapa com pouca cor (ruas em neutro quente). Só os pinos têm cor.

---

## 3. Tipografia: Calçada

- **Títulos:** Bricolage Grotesque (600/800). Só em display, headline e titleLarge.
- **Texto, botões, rótulos, números:** Atkinson Hyperlegible Next (400/700).
- Ambas são OFL (Google Fonts) e vão embutidas no APK em `res/font`.

| Papel M3 | Fonte · peso · tamanho/altura (sp) | Uso |
| --- | --- | --- |
| displaySmall | Bricolage 800 · 36/40 | Onboarding |
| headlineMedium | Bricolage 800 · 28/32 | Frase-resumo (1 por tela) |
| titleLarge | Bricolage 600 · 22/28 | Seção, sheet |
| titleMedium | Atkinson 700 · 17/23 | Título de card |
| bodyLarge | Atkinson 400 · 17/26 | Texto corrido |
| bodyMedium | Atkinson 400 · 15/22 | Metadados |
| labelLarge | Atkinson 700 · 16/20 | Botões |
| labelMedium | Atkinson 700 · 14/18 | Chips, abas |
| labelSmall | Atkinson 700 · 12/16, +0,06 em, caixa alta | Categoria, selos |

**Regras:**
- A frase-resumo diz a resposta, não o nome da tela.
- No máximo dois pesos por card.
- Testar com a fonte do sistema em 1,3× e 2,0×.

---

## 4. Espaço, forma e profundidade

- **Grade de 4 dp:**
  - 4: ícone ↔ texto.
  - 8: dentro de chip.
  - 12: entre itens de card.
  - 16: margem lateral e padding de card.
  - 24: entre cards.
  - 32: entre seções.
- **Alvo de toque:** mínimo de 48 dp.
- **Raios:** 8 (chip, selo) · 14 (card, botão, campo) · 24 (topo de sheet) · pílula (status ao vivo).
- **Profundidade:** superfície + linha de 1 dp. Sombra curta só em sheet e SOS ativo. No escuro, a profundidade vem do tom da superfície.
- **Proibido:** glassmorphism, brilho/glow, gradiente em texto, borda colorida grossa num lado do card, sombra grande difusa.

---

## 5. Movimento

| Situação | Duração | Curva |
| --- | --- | --- |
| Toque, estados de botão | 100 ms | padrão |
| Troca de aba | 150 ms (fade) | padrão |
| Abrir sheet / tela empilhada | 250 ms (sobe 24 dp + fade) | desaceleração |
| "Também vi" (toque duplo no vídeo) | 300 ms (escala 0,8 → 1,1 → 1) | padrão |
| SOS segurando | 2 s (anel linear) | linear |

- Sem efeito de mola exagerado (bounce/elastic).
- Com "remover animações" ligado no Android, as transições viram corte seco e o anel do SOS vira contagem em texto.

---

## 6. Componentes (`ui/designsystem`)

| Componente | Função | Regras |
| --- | --- | --- |
| `RcAccentButton` | Relatar | Laranja + texto marinho. **1 por tela, no máximo** |
| `RcPrimaryButton` | Ação principal | Azul. Estados: pressionado, desativado (com motivo), carregando (rótulo explica) |
| `RcSecondaryButton` | Alternativa | Contorno `outline` |
| `RcTextButton` | Cancelar, ver mais | — |
| `RcSourceBadge` | Origem do dado | Ao vivo HH:MM · Fonte: X · Relato de morador · ✓ N confirmações · Exemplo/sem conexão |
| `RcFilterChip` | Filtro | 40 dp de altura / 48 dp de toque. Selecionado inverte as cores, sem ✓ |
| `RcAlertCard` | Ocorrência | Categoria·distância → título → onde·quando → selos → até 2 ações. A cor da categoria aparece só no rótulo |
| `RcSummaryHeadline` | Frase-resumo | headlineMedium, uma por tela |
| `RcSectionHeader` | Título de seção | titleLarge + ação opcional |
| `RcStatusTile` | Faixa de agora | Ícone, valor, rótulo e selo |
| Campo de texto | — | Rótulo sempre visível; erro diz como resolver |
| Barra inferior | — | 4 destinos + Relatar central; rótulos sempre visíveis |
| SOS | — | Segurar 2 s; números de emergência abrem o discador |

---

## 7. Voz e texto

- Frases curtas, voz ativa, segunda pessoa ("você").
- Botões com verbo ("Relatar agora", "Ver meu trajeto").
- Fato antes de opinião: "Poste apagado na saída da estação", não "Área perigosa!".
- **Proibido sem evidência:** "100% seguro", "100% anônimo", "oficial", "verificado pela polícia", "IA".
- Números: "há 20 min", "350 m", "18:40". Nada de "2 minutes ago".

---

## 8. Acessibilidade (critérios de aceite)
- Contraste AA em todo texto; contorno de componente ≥ 3:1.
- TalkBack: `contentDescription` em ícones de ação e nada em ícones decorativos; cards lidos como uma frase só.
- Fonte 2,0× sem cortar nem sobrepor.
- Nunca usar só cor para informar estado: sempre texto ou ícone junto.

# Rede Cidadã — levantamento de referências para o Guia Visual do app

Pesquisa feita em 23/11/2026. Serve para montar o guia, não é o guia em si.
Os números de acervo e preços vêm dos próprios fornecedores ou de diretórios de terceiros; confira no site antes de assinar.

---

## 0. Ponto de partida

**O que o Guide Visual LEOO tem:** capa, contracapa, divisor, fundos, logos (branco/preto), duas fontes livres (Oswald + Open Sans, licença OFL) e um template de slides. É um **kit de marca para apresentação**.

**O que o guia do Rede Cidadã precisa ter a mais:** é um **guia de produto**. Além da marca, ele precisa dizer como cada tela se comporta: cores com função (alerta, seguro, neutro), tipografia em escala, espaçamentos, botões e estados, cards, mapa, feed, formulário de relato, SOS, estados vazios, de erro e offline, movimento e textos.

**Estado atual do app (main @ eafae4c):**
- Cores com identidade: `BrandNavyDark #0A2540`, `BrandBlue #1D3B8B`, `BrandBlueHero #1E3A8A`, `BrandOrange #FF6B00`, `BrandOrangeLight #FF8533`.
- Tipografia: `FontFamily.Default` (Roboto do sistema) em `Type.kt`. É o principal motivo de o app parecer "padrão". O guia deve escolher uma família própria.

---

## 1. Bibliotecas de telas reais (capturas de apps em produção)

Para ver como Revolut, Monzo, Nubank, Instagram, TikTok, Citizen e outros resolvem cada tela. Use para estudar e comparar, não para copiar.

| Fonte | O que oferece | Revolut? | Custo | Observação |
| --- | --- | --- | --- | --- |
| [Mobbin](https://mobbin.com/) | A maior biblioteca de telas e fluxos de apps; busca por padrão (login, home, carteira, onboarding…) | Não confirmei a página do Revolut | Plano grátis limitado + pago | Referência principal. Há um **plugin Mobbin** no diretório do Claude (MCP + skills) que dá para instalar |
| [Lazyweb — Revolut](https://lazyweb.com/company/revolut) | Telas e fluxos do Revolut (onboarding, verificação, cadastro, busca) | Sim | Só 5 telas abertas sem conta | Bom para fluxos de cadastro e verificação |
| [Nicely Done — Revolut](https://nicelydone.club/apps/revolut/components) | Componentes do Revolut, 76 telas de UI + 72 de marketing (segundo o site) | Sim | Pago | Acervo mais voltado a web |
| [Refero](https://www.producthunt.com/p/refero/refero-4-0) | Telas de web e iOS, fluxos, padrões e fontes | — | Pago | Bom para tipografia em uso |
| [Page Flows](https://www.g2.com/products/page-flows) (absorveu o Screenlane) | **Vídeos** de fluxos completos | — | ~US$ 8/mês no anual, com teste grátis | Ótimo para ver **movimento e microinterações**, que imagem parada não mostra |
| [Dribbble — perfil oficial do Revolut](https://dribbble.com/Revolut) | Shots publicados pela equipe do Revolut | Sim | Grátis | Use o perfil oficial; shots de terceiros "Revolut redesign" são exercícios, não o app |

**Como comparar Mobbin, Refero e outras:** [Mobbin vs Refero](https://mobbin.com/blog/mobbin-vs-refero) e [alternativas ao Mobbin](https://mobbin.com/blog/best-mobbin-alternatives). São textos do próprio Mobbin, então leia sabendo disso.

---

## 2. Base técnica: Material 3 / Material 3 Expressive (a fundação do guia)

O app é Jetpack Compose com Material 3. O guia deve estender o Material, não brigar com ele.

| Fonte | Para quê |
| --- | --- |
| [Material 3 Design Kit (Figma, oficial do Google)](https://material.io/blog/material-3-figma-design-kit) | Kit oficial e gratuito no Figma Community: componentes, variantes, estilos |
| [Material Theme Builder](https://github.com/material-foundation/material-theme-builder) | Gera a paleta completa (claro e escuro) a partir das cores da marca e **exporta `Color.kt` e `Theme.kt` para Compose** ([post oficial](https://m3.material.io/blog/material-theme-builder)) |
| [Material Design 3 no Compose](https://developer.android.com/jetpack/compose/designsystems/material3?hl=pt) | Como o tema vira código |
| [M3 Expressive: design com emoção (codelab)](https://codelabs.developers.google.com/codelabs/mdc-102-objc) | A versão mais "humana" do Material: formas, movimento com física e tipografia mais expressiva |
| [Customizar cores do Material (codelab)](https://codelabs.developers.google.com/customizing-material-color) | Cores de marca e cores de função (alerta, seguro) |
| [android/compose-samples](https://github.com/android/compose-samples) | **Jetsnack** (design system próprio em Compose) e **Reply** (adaptativo, tema claro e escuro): código real para copiar a estrutura |
| [Amostras de design mobile (Android)](https://developer.android.com/design/ui/mobile/samples?hl=pt) | Índice oficial das amostras |

---

## 3. Design systems de produtos que servem de inspiração

| Fonte | Por que olhar |
| --- | --- |
| [Monzo — blog de design](https://monzo.com/blog/design/) | Banco digital no estilo do Revolut que **escreve sobre o próprio processo**: a [nova Home](https://monzo.com/blog/how-we-built-the-new-home-screen), os ["momentos mágicos"](https://monzo.com/blog/2022/11/04/how-we-design-magic-moments-at-monzo) e um [sistema de escrita aberto](https://monzo.com/blog/weve-made-our-writing-system-available-to-all) |
| [Monzo + Jetpack Compose (Google)](https://developer.android.com/stories/apps/monzo-compose) | Como um app fintech montou um design system próprio em Compose fugindo do Material puro. É o caso mais próximo do nosso |
| [Nubank — NuDS (case Figma)](https://www.figma.com/customers/nubank-design-system-accessible-experiences-with-figma/) | Design system brasileiro com tokens, temas e acessibilidade |
| [Revolut — case Figma](https://www.figma.com/customers/how-figma-helps-revolut-deliver-wow-for-customers/) | Como a equipe do Revolut organiza o design. É um case, **não** um kit |
| [Padrão Digital de Governo (DSGov)](https://www.gov.br/governodigital/pt-br/padroes-digitais) + [código no GitLab](https://gitlab.com/govbr-ds) | Referência brasileira de acessibilidade e linguagem cidadã. Útil para as telas de SOS, relato e termos |
| [alexpate/awesome-design-systems](https://github.com/alexpate/awesome-design-systems) | Lista curada (~25 mil estrelas) com dezenas de design systems públicos |

⚠️ **Evite** os "Revolut Design System" não oficiais (shadcn.io, open-design.ai, opendesigner.io, skills.sh). São arquivos de tokens gerados para web/React, não vêm do Revolut e reproduzem justamente a "cara de IA" que você quer evitar. A fonte do Revolut (Aeonik Pro) é paga e proprietária.

---

## 4. Feed estilo Instagram/TikTok (tela de Reels)

| Fonte | Para quê |
| --- | --- |
| Mobbin / Page Flows (seção 1) | Telas e **vídeos** do feed vertical, do player e do gesto de deslizar |
| [Tese da Univ. de Adelaide (Dannaoui, 2022)](https://digital.library.adelaide.edu.au/dspace/bitstream/2440/135568/1/Dannaoui2022_PhD.pdf) | Análise da passagem do Instagram de 2:3 para 9:16 (seções 5.5.2 e 5.5.3) |
| [Mux — Stories/Reels UI](https://www.mux.com/docs/frameworks/react-native-stories-reels-ui.md) | Lista prática do comportamento: paginação, autoplay, gestos (o guia é React Native, mas o comportamento é o mesmo) |
| [Estudo de gestos no vídeo do Instagram (UX Collective)](https://uxdesign.cc/improving-instagram-experience-with-gestures-f40bf429569c) | Gestos e controles sobre o vídeo |
| [TechCrunch — padrões manipulativos do TikTok copiados pelo Instagram](https://techcrunch.com/2022/07/25/instagram-gets-worse-with-dark-patterns-lifted-from-tiktok/) | **O que não fazer**: feed que prende o usuário. Num app de segurança, o feed deve informar, não viciar |

---

## 5. Apps de segurança comunitária: inspiração e alerta

Muito importante para o Rede Cidadã: esses apps têm histórico de **gerar medo e estigma**. O guia precisa tomar posição sobre isso.

| Fonte | Lição |
| --- | --- |
| [Deceptive Design Patterns in Safety Technologies (CHI 2023, sobre o app Citizen)](https://faculty.washington.edu/alexisr/SafetyApps.pdf) | O estudo mais rigoroso: a interface aumenta a ansiedade para gerar engajamento |
| [Continuação do mesmo grupo (2025)](https://faculty.washington.edu/alexisr/beloved.pdf) | Alternativas centradas na comunidade |
| [AIGA Eye on Design — "Apps de crime têm um problema de design?"](https://eyeondesign.aiga.org/do-crime-tracking-apps-have-a-design-problem/) | Debate sobre cor e tom: dá para alertar sem alarmar? |
| [Citizen no Mapbox](https://www.mapbox.com/showcase/citizen) | Como o mapa com camadas de alerta é construído |
| [Caminhar seguro à noite — UX case (U. Illinois)](https://uxdesign.cc/ui-ux-case-study-helping-people-feel-safe-walking-at-night-at-u-of-illinois-2f36db9a2951) | Em emergência, o mapa deve parecer o Google/Apple Maps que a pessoa já conhece |

**Regra que proponho para o guia:** vermelho e laranja só para o que exige ação agora. A Home não abre em "modo alarme".

---

## 6. Fundamentos (para o guia não ser só gosto pessoal)

| Fonte | Para quê |
| --- | --- |
| [Laws of UX (Jon Yablonski)](https://lawsofux.com/) | Fitts (alvos de toque, botão de SOS), Hick (poucas opções por tela), Jakob (padrões que o usuário já conhece) |
| [Refactoring UI](https://refactoringui.com/) (livro, pago) | O melhor manual prático de hierarquia, espaçamento, cor e profundidade, com exemplos de antes e depois |
| [Google Fonts — combinar fontes](https://fonts.google.com/knowledge/choosing_type/pairing_typefaces) | Como combinar duas famílias |
| [Tipografia para apps (Toptal)](https://www.toptal.com/designers/typography/typography-for-mobile-apps) | Escala e legibilidade no celular |
| [Fontes no Compose (variáveis)](https://developer.android.com/jetpack/compose/text/fonts?hl=pt) | Como colocar a fonte escolhida em `res/font` |

---

## 7. Ilustração e arte com "mão humana"

| Fonte | Licença | Observação |
| --- | --- | --- |
| [Open Peeps (Pablo Stanley)](https://openpeeps.com) | CC0, uso comercial livre e sem crédito | Pessoas desenhadas à mão. Combina com a comunidade |
| [Humaaans](https://webdesignerdepot.com/free-download-public-humaans-library) | **Fontes divergem** (livre vs. CC BY-NC) | Confirme a licença antes de usar na Play Store |

Uma alternativa mais autoral: **fotos reais do Capão Redondo e da Zona Sul** (com autorização das pessoas) ou ilustrações feitas pelo grupo. Nada dá mais "cara humana" do que o território real.

---

## 8. Como fugir da "cara de IA"

Consenso dos textos ([Built In](https://builtin.com/articles/ai-design-slop-era), [Creative Bloq](https://www.creativebloq.com/ai/everything-looks-the-same-now-what), [daisyUI](https://trends.daisyui.com/how-to-make-ai-ui-less-generic/), [Kosta Canatselis](https://world.hey.com/kostac/spot-the-slop-a-ui-designer-s-guide-to-fixing-ai-defaults-4c448c9c)): a cara de IA vem de **ficar nos padrões**. Por isso o guia precisa **decidir** o que a IA decidiria sozinha. São opiniões de profissionais, não estudos, mas batem entre si.

**Evitar:**
- Gradiente em texto, brilho neon no modo escuro, glassmorphism.
- Ícone grande e arredondado em cima de todo título.
- Card com borda colorida grossa só de um lado.
- Animação com efeito de mola (bounce/elastic) em tudo.
- Gráficos decorativos que não mostram dado nenhum.
- Fonte padrão (hoje o app usa a do sistema).
- Textos genéricos e números de exemplo sem rótulo.

**Fazer:**
- Uma família tipográfica própria, com escala definida.
- Grade de espaçamento fixa (ex.: 4/8 dp) e raios definidos.
- **Um objetivo por tela**, escrito no guia.
- Conteúdo real de São Paulo nos exemplos.
- Referências fora dos feeds do Dribbble/Pinterest: apps reais (Mobbin), livros e o território.

---

## 9. Proposta de estrutura do guia (focada nas funcionalidades)

1. **Princípios**: informar sem alarmar; um objetivo por tela; honestidade de dados (ao vivo × exemplo); anonimato visível.
2. **Marca**: logo, escudo, nome REDE CIDADÃ, usos corretos e errados.
3. **Cor**: paleta da marca → tokens do Material (Theme Builder) → **cores de função** (crítico, atenção, seguro, informativo, neutro), claro e escuro, com contraste WCAG medido.
4. **Tipografia**: família, escala (display → label), números tabulares para dados.
5. **Espaço, grade, raios e elevação.**
6. **Iconografia**: Material Symbols (já usado no app), traço, tamanho e área de toque.
7. **Componentes**: botões (primário, secundário, SOS), chips de filtro, card de alerta, banner de clima, selo de fonte (ao vivo/exemplo), campos, sheets, navegação inferior.
8. **Telas, uma por página, com objetivo e anatomia**: Onboarding · Localização · Login · Home/mapa · Detalhe do alerta · Relato · Mobilidade/trilhos · Feed (Reels) e visualizador · SOS e guardiões · Perfil e privacidade · Termos e exclusão de conta.
9. **Estados**: carregando, vazio, erro, offline, sem permissão.
10. **Movimento**: durações e quando animar (e quando não, no SOS).
11. **Voz e texto**: tom, palavras proibidas ("100% seguro", "oficial" sem ser), botões com verbo.
12. **Acessibilidade**: contraste, TalkBack, fonte ampliada, alvo ≥ 48 dp.
13. **Do guia ao código**: mapa token → `Color.kt` / `Type.kt` / componentes Compose.

---

## 10. Próximos passos sugeridos

1. Montar um quadro de referências (Figma ou Miro) com 5–10 capturas por tela, tiradas do Mobbin, Lazyweb e Page Flows.
2. Escolher a tipografia e rodar o Material Theme Builder com as cores atuais.
3. Escrever o "objetivo da tela" de cada uma das 11 telas antes de desenhar.
4. Montar o guia (Figma + documento) e só então levar para o Compose.

Para os passos 1 e 4 com ferramentas: o Figma já está registrado no Claude Code e só falta o seu login (`/mcp` → figma). O plugin do Mobbin pode ser instalado se você quiser que eu pesquise as telas por você.

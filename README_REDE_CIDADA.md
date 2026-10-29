# Rede Cidadã SP

**Protótipo acadêmico Android de segurança comunitária e mobilidade urbana.** O projeto propõe reunir relatos, informações de contexto e recursos de apoio inicialmente apenas para pessoas que circulam por São Paulo. 

## Objetivo

O projeto aborda a dificuldade de encontrar informações sobre riscos urbanos durante deslocamentos e o receio de se identificar ao relatar um problema. A proposta apresenta um mapa de ocorrências, informações de clima e mobilidade, opção de relato anônimo, comparação de rotas e uma interface de apoio emergencial.

O app deve ser entendido como um protótipo de interface e fluxo. O código atual não implementa uma plataforma de denúncia conectada a autoridades nem uma conta real de usuário.

## O que o protótipo apresenta

- **Início:** mapa, incidentes de exemplo, busca, filtro por categoria e detalhes de uma ocorrência.
- **Relato:** formulário com categoria, endereço, descrição e opção de anonimato. Ao enviar, o item é acrescentado à lista local do ViewModel.
- **Mobilidade:** cartões de linhas e comparação entre duas rotas de exemplo. As rotas e métricas são fixas; não são calculadas por um serviço de navegação.
- **Reels:** grade e tela visual de relatos. Os itens são dados de exemplo e não há reprodução de arquivos de vídeo.
- **SOS:** canais e contatos de exemplo. O botão alterna a aparência da tela, mas não faz uma ligação nem envia localização ou notificação.
- **Perfil:** pontuação local, preferências e um catálogo informativo de integrações. O catálogo não verifica conexão real.

O login também é demonstrativo: os campos começam preenchidos e os botões seguem para a próxima tela sem autenticar credenciais.

## Integrações: o que o código faz

| Fonte | Implementação encontrada | Limite atual |
|---|---|---|
| **Open-Meteo** | Requisição HTTP para obter condições meteorológicas atuais a partir de coordenadas associadas ao bairro. | Se a consulta falhar, são usados valores fixos de exemplo. |
| **INMET** | Requisição à página de avisos ativos; o código procura trechos de texto para inferir um nível de alerta. | Não trata os dados como uma integração robusta de API estruturada. |
| **CGE-SP** | Requisição à página de alagamentos; conta ocorrências de termos no HTML. | É uma contagem aproximada por texto, não uma leitura estruturada de pontos geográficos. |
| **Overpass / OpenStreetMap** | Consulta objetos OSM marcados como polícia ou vigilância perto da região. | Os indicadores de distrito e a base de câmeras começam com números fixos. A consulta não é um feed oficial da SSP-SP ou do Smart Sampa. |
| **Metrô SP** | Baixa uma página HTML e procura a expressão “Velocidade Reduzida”. | A verificação altera somente a linha `L1`; os outros dados permanecem os valores iniciais do app. Não há consulta completa às linhas da CPTM ou da ViaMobilidade. |
| **Google Maps Compose** | Mostra o SDK Google Maps quando há uma chave configurada. | Sem chave, há uma alternativa WebView com tiles CARTO e um desenho Canvas. O código não implementa a Routes API para calcular as rotas exibidas. |

A classe `SpPublicSafetyApiService` também contém um catálogo estático que menciona serviços como Gemini/Vertex AI, Firebase/FCM, Flood Hub e Routes API. Esses nomes e os indicadores “conectado” não significam que tais serviços estejam integrados: não há chamadas correspondentes no código analisado.

As consultas de rede usam `HttpURLConnection` em funções suspensas e são executadas com `Dispatchers.IO`. O tratamento de falhas mantém dados base ou devolve exemplos fixos. Isso é um fallback; não é um cache persistente/offline.

## Arquitetura e tecnologias

- **Linguagem:** Kotlin.
- **Interface:** Jetpack Compose e componentes Material 3.
- **Organização:** separação entre Activity, telas/componentes, ViewModel e serviço remoto. A estrutura segue parte da ideia MVVM, mas o ViewModel chama diretamente o serviço, sem uma camada de repositório.
- **Estado:** `CitizenViewModel` mantém valores com estados observáveis do Compose, como `mutableStateOf` e `mutableStateListOf`. As alterações provocam a atualização das telas que observam esses dados.
- **Navegação:** `MainActivity` mantém o nome da tela atual e seleciona o conteúdo com `when`. A Activity é única, mas esse fluxo não usa a biblioteca Navigation Compose.
- **Persistência:** os relatos criados, curtidas, pontuação e demais alterações ficam em memória. Não há banco de dados ou envio de relato a um backend nesse fluxo.

## Estrutura principal

```text
app/src/main/java/com/example/projetorevisao/
├── MainActivity.kt
├── data/remote/SpPublicSafetyApiService.kt
├── ui/
│   ├── components/
│   │   ├── CitizenGoogleMapView.kt
│   │   ├── CitizenIcons.kt
│   │   └── CitizenNavigationShell.kt
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── MetroScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── ReelsScreen.kt
│   │   ├── ReportIncidentSheet.kt
│   │   └── SosScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/CitizenViewModel.kt
```

- **`MainActivity.kt`:** inicia o Compose, mantém a navegação simples entre telas e conecta o formulário ao ViewModel.
- **`SpPublicSafetyApiService.kt`:** contém os modelos de telemetria e as consultas HTTP limitadas descritas acima.
- **`ui/components`:** agrupa mapa, ícones e barras de navegação reutilizáveis.
- **`ui/screens`:** contém as funções Compose de cada tela.
- **`CitizenViewModel.kt`:** concentra os dados de demonstração, estado compartilhado, filtros e ações da interface.
- **`ui/theme`:** define cores, tipografia e tema Material 3.

## Executar localmente

1. Clone o repositório e abra-o no Android Studio.
2. Aguarde a sincronização do Gradle e configure `MAPS_API_KEY` em `local.properties` se for usar o SDK Google Maps.
3. Selecione um emulador ou dispositivo e execute a configuração `app` pelo Android Studio.

O `minSdk` configurado é 24 e o `targetSdk` é 37. A experiência do mapa depende da disponibilidade de uma chave válida; o código contém alternativas visuais para a ausência dela.

## Integrantes da Equipe

 **Erick Ferreira, Esther, Duda Gobira, Kaike Santos, Gabriel Lima, Nelson e João Frederico**. 
---





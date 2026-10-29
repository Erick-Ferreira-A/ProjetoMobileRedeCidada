# 📘 Guia do Colaborador & Documentação Oficial — Rede Cidadã SP

Este documento contém:
1. **[Parte 1: Instruções para o Colaborador](#parte-1-instruções-para-o-colaborador)** (Passo a passo de Git, clone, edição e envio do código).
2. **[Parte 2: Conteúdo Completo do README.md](#parte-2-conteúdo-do-readmemd-do-projeto)** (Texto pronto para ser refinado e colocado na raiz do projeto).

---

# PARTE 1: INSTRUÇÕES PARA O COLABORADOR

Olá! Se você recebeu este documento, você faz parte da equipe de desenvolvimento do **Rede Cidadã SP**.  
Siga os passos abaixo para clonar o repositório, testar o projeto no seu computador e subir a documentação atualizada ou novos ajustes.

### 1. Clonar o repositório
Abra o seu terminal (Git Bash, Prompt de Comando ou PowerShell) e execute:
```bash
git clone https://github.com/Erick-Ferreira-A/ProjetoMobileRedeCidada.git
```

### 2. Abrir no Android Studio
1. Abra o **Android Studio** (versão Ladybug 2024.2 ou superior recomendada).
2. Clique em **File** > **Open...** e selecione a pasta `ProjetoMobileRedeCidada`.
3. Aguarde o Gradle baixar as dependências e sincronizar o projeto.
4. Para testar a compilação, selecione um emulador (ou dispositivo físico via USB) e aperte **Shift + F10** (Run).

### 3. Criar uma branch para suas alterações
Nunca envie commits diretamente na branch `main`. Crie uma branch para o seu trabalho:
```bash
git checkout -b feature/atualiza-readme
```

### 4. Como atualizar o README no projeto
1. Na raiz do projeto, crie ou abra o arquivo `README.md`.
2. Copie todo o conteúdo da **[Parte 2](#parte-2-conteúdo-do-readmemd-do-projeto)** deste documento e cole nele.
3. **Refine os campos necessários:**
   - Na seção **Equipe de Desenvolvimento**, adicione o seu nome, RA/função e links do GitHub/LinkedIn.
   - Adicione o nome da sua faculdade, disciplina ou orientador, se aplicável.
   - Ajuste qualquer detalhe adicional que o grupo combinou.

### 5. Salvar, commitar e enviar para o GitHub
Depois de editar e salvar o arquivo `README.md`:
```bash
# 1. Adicionar o arquivo modificado
git add README.md

# 2. Criar o commit com mensagem clara
git commit -m "docs: adiciona documentacao completa do projeto no README"

# 3. Enviar a sua branch para o GitHub
git push -u origin feature/atualiza-readme
```

### 6. Abrir o Pull Request (PR)
1. Acesse o repositório: [https://github.com/Erick-Ferreira-A/ProjetoMobileRedeCidada](https://github.com/Erick-Ferreira-A/ProjetoMobileRedeCidada)
2. Você verá um aviso verde sugerindo **"Compare & pull request"**. Clique nele.
3. Adicione um título e uma breve descrição das melhorias feitas.
4. Clique em **Create pull request** para que o Erick possa revisar e mesclar na branch principal (`main`).

---

# PARTE 2: CONTEÚDO DO README.md DO PROJETO

# 🏙️ Rede Cidadã SP — Aplicativo Android de Segurança & Mobilidade Urbana

![Android](https://img.shields.io/badge/Plataforma-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Linguagem-Kotlin-purple?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-blue?logo=jetpackcompose)
![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2024%20(Android%207.0)-orange)
![Target SDK](https://img.shields.io/badge/Target%20SDK-API%2037-darkgreen)

> Aplicativo móvel colaborativo voltado para a cidadania, prevenção e segurança urbana na Região Metropolitana de São Paulo. Conecta dados oficiais de órgãos públicos em tempo real com relatos verificados pela própria comunidade.

---

## 📌 Sumário
1. [Sobre o Projeto](#-sobre-o-projeto)
2. [Principais Funcionalidades](#-principais-funcionalidades)
3. [Telas do Aplicativo](#-telas-do-aplicativo)
4. [Integrações e APIs Públicas](#-integrações-e-apis-públicas)
5. [Arquitetura e Tecnologias](#-arquitetura-e-tecnologias)
6. [Estrutura do Código](#-estrutura-do-código)
7. [Como Executar o Projeto](#-como-executar-o-projeto)
8. [Equipe de Desenvolvimento](#-equipe-de-desenvolvimento)

---

## 📖 Sobre o Projeto

O **Rede Cidadã SP** nasceu com a missão de transformar o cidadão paulistano em um agente ativo na melhoria e segurança da sua vizinhança. 

Através da combinação de **sensoriamento comunitário** (relatos de incidentes, falta de iluminação, pontos de alagamento) e **dados governamentais abertos** (clima pelo CGE/INMET, monitoramento por câmeras do Smart Sampa e status em tempo real das linhas de Metrô e CPTM), o app entrega um panorama situacional completo para pedestres e passageiros do transporte público.

---

## 🚀 Principais Funcionalidades

* 🗺️ **Mapa Situacional Georreferenciado:** Visualização interativa via Google Maps SDK Compose ou mapa vetorial híbrido com marcação de ocorrências ativas em São Paulo.
* 🌧️ **Telemetria Climática e Risco de Alagamentos:** Dados ao vivo do CGE-SP, INMET e Open-Meteo, alertando sobre chuvas fortes, rajadas de vento e bolsões d'água em vias arteriais.
* 🚇 **Status do Transporte Público (Metrô SP & ViaMobilidade):** Painel em tempo real sobre a operação das linhas (Linha 1-Azul, Linha 4-Amarela, Linha 5-Lilás, Linha 9-Esmeralda, etc.).
* 📹 **Feed de Prevenção e Dicas Urbanas (Reels):** Vídeos curtos com instruções de segurança, prevenção a golpes e alertas da Defesa Civil.
* 🚨 **Botão de Pânico & Acesso Rápido SOS:** Discagem direta para emergência: **190** (Polícia Militar), **193** (Bombeiros), **199** (Defesa Civil), **153** (Guarda Civil Metropolitana) e contatos de confiança.
* 📝 **Registro Colaborativo de Incidentes:** Formulário rápido com categoria, descrição, endereço e suporte a denúncia anônima.
* 🛡️ **Score de Confiabilidade Comunitária:** Mecanismo de reputação para garantir a veracidade dos relatos e engajar a comunidade.

---

## 📱 Telas do Aplicativo

| Tela | Descrição |
| :--- | :--- |
| **Onboarding** | Apresentação inicial da proposta de valor, segurança de dados e objetivos comunitários. |
| **Login** | Acesso com credenciais ou navegação direta como convidado/cidadão anônimo. |
| **Home (Principal)** | Mapa dinâmico, cartões de telemetria climática/policial, filtros por categoria de evento e modal de detalhes expansível. |
| **Metrô & Trens** | Monitoramento operacional das linhas com alertas de velocidade reduzida ou paralisações. |
| **Reels de Segurança** | Feed de vídeos curtos educativos com player em tela cheia e interação comunitária. |
| **SOS Emergência** | Botões de discagem rápida com confirmação, geolocalização e orientações de segurança. |
| **Perfil Cidadão** | Gestão de histórico de colaborações, pontuação de confiança comunitária e configurações. |

---

## 🌐 Integrações e APIs Públicas

O aplicativo possui uma camada de rede resiliente (`SpPublicSafetyApiService`) com **tolerância a falhas e cache offline** caso o dispositivo esteja sem conexão:

| Serviço / Órgão | Finalidade | Tipo de Conexão |
| :--- | :--- | :--- |
| **Open-Meteo API** | Temperatura, umidade, vento e precipitação por coordenadas | HTTP REST / JSON |
| **INMET (Alert-AS)** | Avisos meteorológicos severos vigentes para o estado de SP | HTTP REST Oficial |
| **CGE-SP** | Monitoramento de pontos de alagamento transitáveis e intransitáveis | Extração de boletins públicos |
| **SSP-SP & Smart Sampa** | Estatísticas de distritos policiais e câmeras do GeoSampa | Consulta Geoespacial / OSM Overpass |
| **Metrô SP & ViaMobilidade** | Status operacional em tempo real das linhas de trilhos | Direto dos Trens / Portais oficiais |
| **Google Maps Platform** | Renderização do mapa nativo e satélite com suporte a marcadores | SDK Oficial Google Maps Compose |

---

## 🛠️ Arquitetura e Tecnologias

* **Linguagem:** Kotlin 100% nativo.
* **Padrão de Arquitetura:** **MVVM (Model-View-ViewModel)** com estado reativo unificado (`CitizenViewModel`).
* **Interface:** **Jetpack Compose** moderno, sem layouts em XML tradicional, adotando o **Material 3 Expressive System** (modo claro e escuro de alto contraste).
* **Concorrência e Assincronismo:** Kotlin Coroutines com `Dispatchers.IO` para chamadas de rede sem bloqueio da UI.
* **Acessibilidade (a11y):** Touch targets mínimos de 48dp a 56dp, contrastes compatíveis com as diretrizes WCAG e descrições para leitores de tela.

---

## 📂 Estrutura do Código

```text
app/src/main/java/com/example/projetorevisao/
│
├── MainActivity.kt                     # Activity única com navegação em Jetpack Compose
│
├── data/
│   └── remote/
│       └── SpPublicSafetyApiService.kt # Cliente de rede para Open-Meteo, CGE, INMET e Metrô
│
├── ui/
│   ├── components/                     # Componentes reutilizáveis
│   │   ├── CitizenGoogleMapView.kt     # Wrapper do Google Maps Compose
│   │   ├── CitizenIcons.kt             # Ícones vetoriais Material Symbols
│   │   └── CitizenNavigationShell.kt   # TopBar e BottomNavBar compartilhadas
│   │
│   ├── screens/                        # Telas em Jetpack Compose
│   │   ├── HomeScreen.kt               # Mapa, filtros e lista de ocorrências
│   │   ├── LoginScreen.kt              # Autenticação
│   │   ├── MetroScreen.kt              # Status das linhas de metrô e trens
│   │   ├── OnboardingScreen.kt         # Boas-vindas ao app
│   │   ├── ProfileScreen.kt            # Perfil e score de confiança
│   │   ├── ReelsScreen.kt              # Feed de vídeos e dicas
│   │   ├── ReportIncidentSheet.kt      # BottomSheet de cadastro de incidentes
│   │   └── SosScreen.kt                # Tela de pânico e telefones de emergência
│   │
│   ├── theme/                          # Identidade visual, tipografia e paleta
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   └── viewmodel/                      # Gerenciamento de estado da aplicação
│       └── CitizenViewModel.kt
```

---

## 💻 Como Executar o Projeto

### Pré-requisitos
* **Android Studio:** Ladybug (2024.2+) ou versão recente.
* **JDK:** Java 11 ou superior (recomendado o JBR embutido no Android Studio).
* **Android SDK:** Compilado com SDK 37 (compatível com Android 7.0+ / API 24).

### Execução no emulador ou dispositivo físico
1. Com o projeto aberto no Android Studio, aguarde o Gradle sincronizar.
2. Selecione o dispositivo de destino no menu superior.
3. Clique em **Run 'app'** (`Shift + F10`).

> 💡 *Nota sobre a chave do Google Maps:* Caso deseje ativar o mapa oficial do Google Maps, adicione sua chave no arquivo `local.properties`:  
> `MAPS_API_KEY=sua_chave_aqui`  
> *(Caso não configure uma chave, o app utiliza automaticamente o mapa vetorial integrado sem impedir a execução).*

---

## 👥 Equipe de Desenvolvimento

* **Erick Ferreira** — *Desenvolvedor / Idealizador* — [GitHub](https://github.com/Erick-Ferreira-A)
* **[Nome do Colaborador 1]** — *[Função / Atribuição]* — [GitHub](https://github.com/)
* **[Nome do Colaborador 2]** — *[Função / Atribuição]* — [GitHub](https://github.com/)

---
*Projeto acadêmico e comunitário desenvolvido para a cidade de São Paulo/SP.*

🏙️ Rede Cidadã SP — Aplicativo Android de Segurança & Mobilidade Urbana
Android
Android
Kotlin
Kotlin
Jetpack Compose
Jetpack Compose
Min SDK
Min SDK
Target SDK
Target SDK

Aplicativo móvel colaborativo voltado para a cidadania, prevenção e segurança urbana na Região Metropolitana de São Paulo. Conecta dados oficiais de órgãos públicos em tempo real com relatos verificados pela própria comunidade.

📌 Sumário
Sobre o Projeto
Principais Funcionalidades
Telas do Aplicativo
Integrações e APIs Públicas
Arquitetura e Tecnologias
Estrutura do Código
Como Executar o Projeto
Equipe de Desenvolvimento
📖 Sobre o Projeto
O Rede Cidadã SP nasceu com a missão de transformar o cidadão paulistano em um agente ativo na melhoria e segurança da sua vizinhança.

Através da combinação de sensoriamento comunitário (relatos de incidentes, falta de iluminação, pontos de alagamento) e dados governamentais abertos (clima pelo CGE/INMET, monitoramento por câmeras do Smart Sampa e status em tempo real das linhas de Metrô e CPTM), o app entrega um panorama situacional completo para pedestres e passageiros do transporte público.

🚀 Principais Funcionalidades
🗺️ Mapa Situacional Georreferenciado: Visualização interativa via Google Maps SDK Compose ou mapa vetorial híbrido com marcação de ocorrências ativas em São Paulo.
🌧️ Telemetria Climática e Risco de Alagamentos: Dados ao vivo do CGE-SP, INMET e Open-Meteo, alertando sobre chuvas fortes, rajadas de vento e bolsões d'água em vias arteriais.
🚇 Status do Transporte Público (Metrô SP & ViaMobilidade): Painel em tempo real sobre a operação das linhas (Linha 1-Azul, Linha 4-Amarela, Linha 5-Lilás, Linha 9-Esmeralda, etc.).
📹 Feed de Prevenção e Dicas Urbanas (Reels): Vídeos curtos com instruções de segurança, prevenção a golpes e alertas da Defesa Civil.
🚨 Botão de Pânico & Acesso Rápido SOS: Discagem direta para emergência: 190 (Polícia Militar), 193 (Bombeiros), 199 (Defesa Civil), 153 (Guarda Civil Metropolitana) e contatos de confiança.
📝 Registro Colaborativo de Incidentes: Formulário rápido com categoria, descrição, endereço e suporte a denúncia anônima.
🛡️ Score de Confiabilidade Comunitária: Mecanismo de reputação para garantir a veracidade dos relatos e engajar a comunidade.
📱 Telas do Aplicativo
Tela	Descrição
Onboarding	Apresentação inicial da proposta de valor, segurança de dados e objetivos comunitários.
Login	Acesso com credenciais ou navegação direta como convidado/cidadão anônimo.
Home (Principal)	Mapa dinâmico, cartões de telemetria climática/policial, filtros por categoria de evento e modal de detalhes expansível.
Metrô & Trens	Monitoramento operacional das linhas com alertas de velocidade reduzida ou paralisações.
Reels de Segurança	Feed de vídeos curtos educativos com player em tela cheia e interação comunitária.
SOS Emergência	Botões de discagem rápida com confirmação, geolocalização e orientações de segurança.
Perfil Cidadão	Gestão de histórico de colaborações, pontuação de confiança comunitária e configurações.
🌐 Integrações e APIs Públicas
O aplicativo possui uma camada de rede resiliente (SpPublicSafetyApiService) com tolerância a falhas e cache offline caso o dispositivo esteja sem conexão:

Serviço / Órgão	Finalidade	Tipo de Conexão
Open-Meteo API	Temperatura, umidade, vento e precipitação por coordenadas	HTTP REST / JSON
INMET (Alert-AS)	Avisos meteorológicos severos vigentes para o estado de SP	HTTP REST Oficial
CGE-SP	Monitoramento de pontos de alagamento transitáveis e intransitáveis	Extração de boletins públicos
SSP-SP & Smart Sampa	Estatísticas de distritos policiais e câmeras do GeoSampa	Consulta Geoespacial / OSM Overpass
Metrô SP & ViaMobilidade	Status operacional em tempo real das linhas de trilhos	Direto dos Trens / Portais oficiais
Google Maps Platform	Renderização do mapa nativo e satélite com suporte a marcadores	SDK Oficial Google Maps Compose
🛠️ Arquitetura e Tecnologias
Linguagem: Kotlin 100% nativo.
Padrão de Arquitetura: MVVM (Model-View-ViewModel) com estado reativo unificado (CitizenViewModel).
Interface: Jetpack Compose moderno, sem layouts em XML tradicional, adotando o Material 3 Expressive System (modo claro e escuro de alto contraste).
Concorrência e Assincronismo: Kotlin Coroutines com Dispatchers.IO para chamadas de rede sem bloqueio da UI.
Acessibilidade (a11y): Touch targets mínimos de 48dp a 56dp, contrastes compatíveis com as diretrizes WCAG e descrições para leitores de tela.
📂 Estrutura do Código
text


app/src/main/java/com/example/projetorevisao/
│
├── MainActivity.kt                     # Activity única com navegação em Jetpack Compose
│
├── data/
│   └── remote/
│       └── SpPublicSafetyApiService.kt # Cliente de rede para Open-Meteo, CGE, INMET e Metrô
│
├── ui/
│   ├── components/                     # Componentes reutilizáveis
│   │   ├── CitizenGoogleMapView.kt     # Wrapper do Google Maps Compose
│   │   ├── CitizenIcons.kt             # Ícones vetoriais Material Symbols
│   │   └── CitizenNavigationShell.kt   # TopBar e BottomNavBar compartilhadas
│   │
│   ├── screens/                        # Telas em Jetpack Compose
│   │   ├── HomeScreen.kt               # Mapa, filtros e lista de ocorrências
│   │   ├── LoginScreen.kt              # Autenticação
│   │   ├── MetroScreen.kt              # Status das linhas de metrô e trens
│   │   ├── OnboardingScreen.kt         # Boas-vindas ao app
│   │   ├── ProfileScreen.kt            # Perfil e score de confiança
│   │   ├── ReelsScreen.kt              # Feed de vídeos e dicas
│   │   ├── ReportIncidentSheet.kt      # BottomSheet de cadastro de incidentes
│   │   └── SosScreen.kt                # Tela de pânico e telefones de emergência
│   │
│   ├── theme/                          # Identidade visual, tipografia e paleta
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   └── viewmodel/                      # Gerenciamento de estado da aplicação
│       └── CitizenViewModel.kt
💻 Como Executar o Projeto
Pré-requisitos
Android Studio: Ladybug (2024.2+) ou versão recente.
JDK: Java 11 ou superior (recomendado o JBR embutido no Android Studio).
Android SDK: Compilado com SDK 37 (compatível com Android 7.0+ / API 24).
Execução no emulador ou dispositivo físico
Com o projeto aberto no Android Studio, aguarde o Gradle sincronizar.
Selecione o dispositivo de destino no menu superior.
Clique em Run 'app' (Shift + F10).
💡 Nota sobre a chave do Google Maps: Caso deseje ativar o mapa oficial do Google Maps, adicione sua chave no arquivo local.properties:
MAPS_API_KEY=sua_chave_aqui
(Caso não configure uma chave, o app utiliza automaticamente o mapa vetorial integrado sem impedir a execução).

👥 Equipe de Desenvolvimento
Erick Ferreira — Desenvolvedor / Idealizador — GitHub
[Nome do Colaborador 1] — [Função / Atribuição] — GitHub
[Nome do Colaborador 2] — [Função / Atribuição] — GitHub
Projeto acadêmico e comunitário desenvolvido para a cidade de São Paulo/SP.
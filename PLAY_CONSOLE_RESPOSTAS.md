# Play Console — respostas prontas (etapas 5 e 6)

Guia para preencher o Play Console do **Rede Cidadã** (`br.com.redecidada.app`).
Os menus podem mudar de nome; procure pelo assunto.

Links que o Console vai pedir:
- Política de privacidade: https://projetomobileredecidada-production.up.railway.app/privacidade
- Exclusão de conta: https://projetomobileredecidada-production.up.railway.app/excluir-conta
- E-mail de contato: suporte.redecidada@gmail.com

---

## 5.1 Criar o app
**Página inicial → Criar app**

| Campo | Resposta |
|---|---|
| Nome do app | Rede Cidadã |
| Idioma padrão | Português (Brasil) – pt-BR |
| App ou jogo | App |
| Gratuito ou pago | Gratuito |
| Declarações | Marcar as duas (políticas do programa e leis de exportação dos EUA) |

> Ao enviar o `.aab` pela primeira vez, aceite o **Play App Signing** (padrão). A chave do `.jks` é só a chave de upload.

---

## 5.2 Conteúdo do app
**Menu: Política → Conteúdo do app** (ou "Configurar seu app" no painel)

### Política de privacidade
Colar o link da privacidade (acima).

### Acesso ao app
- Escolha: **Toda ou parte da funcionalidade é restrita**.
- Crie no app (pelo celular) uma conta só para o revisor, por exemplo `revisor.play.redecidada@gmail.com`, com senha forte. **Guarde no gerenciador de senhas.** Não use a sua conta pessoal.
- Instruções para o revisor (colar):

```
O app abre sem login (modo visitante): mapa, relatos próximos, clima e status dos trilhos.
Para publicar relatos, confirmar/denunciar relatos, cadastrar guardiões e usar o SOS,
entre com a conta abaixo (aba Perfil > Entrar). O SOS NÃO aciona a polícia: ele abre o
WhatsApp com uma mensagem pronta para os contatos cadastrados pelo próprio usuário.
O conteúdo e o transporte são da cidade de São Paulo.
```

### Anúncios
**Não, meu app não contém anúncios.**

### Classificação de conteúdo (questionário IARC)
- E-mail: suporte.redecidada@gmail.com
- Categoria: a que fala de **rede social / comunicação / conteúdo gerado pelo usuário** (não é jogo).
- Respostas:

| Pergunta (resumo) | Resposta |
|---|---|
| Violência, sangue, terror, sexo, nudez, palavrões, drogas, apostas criadas pelo app | Não |
| Usuários podem interagir ou trocar conteúdo (relatos, fotos, vídeos) | **Sim** |
| O app compartilha a localização do usuário com outros usuários | **Sim** (local dos relatos é público; no SOS a posição vai aos guardiões) |
| Compras digitais | Não |
| Navegador de internet sem restrição | Não |
| Moderação/denúncia de conteúdo de usuários | Sim (denúncia, bloqueio, ocultação automática com 3 denúncias) |

### Público-alvo e conteúdo
- Faixa etária: **somente 18 anos ou mais**.
- O app atrai crianças? **Não**.

### Apps de notícias
**Não** é um app de notícias.

### Apps governamentais
**Não**. O app é independente e não é feito nem endossado por órgão público. (A descrição da loja deixa isso claro.)

### Recursos financeiros / Saúde / COVID-19
Não se aplica a nenhum.

### Segurança dos dados (Data safety)

**Visão geral**
| Pergunta | Resposta |
|---|---|
| Coleta ou compartilha algum dos tipos de dados exigidos? | Sim |
| Todos os dados são criptografados em trânsito? | **Sim** (HTTPS) |
| Oferece forma de pedir a exclusão dos dados? | **Sim** — link da página de exclusão |
| Login | O app permite criar conta com e-mail e senha; não usa login de terceiros |

**Compartilhamento:** marcar que **não compartilha** dados com terceiros. Hospedagem (Railway), clima (Open-Meteo) e mapa (Google) são prestadores de serviço, que o Google não conta como "compartilhamento". O WhatsApp no SOS é aberto pelo próprio usuário, e isso também não conta.

**Dados coletados** (para todos: *não* é processado só no aparelho; *não* é efêmero)

| Categoria → tipo | Obrigatório ou opcional | Finalidade |
|---|---|---|
| Local → **Local exato** | Opcional | Funcionalidade do app |
| Informações pessoais → **Nome** | Opcional (só quem cria conta) | Funcionalidade do app, Gerenciamento da conta |
| Informações pessoais → **Endereço de e-mail** | Opcional | Gerenciamento da conta |
| Fotos e vídeos → **Fotos** | Opcional | Funcionalidade do app |
| Fotos e vídeos → **Vídeos** | Opcional | Funcionalidade do app |
| Contatos → **Contatos** (nome e telefone dos guardiões, digitados pelo usuário) | Opcional | Funcionalidade do app |
| Atividade no app → **Outros conteúdos gerados pelo usuário** (texto dos relatos) | Opcional | Funcionalidade do app |
| Atividade no app → **Outras ações** (confirmar, denunciar, bloquear) | Opcional | Funcionalidade do app, Segurança/prevenção de fraude |

Não marcar: dados financeiros, saúde, mensagens, áudio, arquivos, agenda, apps instalados, histórico de navegação, IDs de dispositivo ou publicidade, diagnóstico (o app não tem analytics nem relatório de falhas próprio).

### Permissões
- O app usa só **localização em primeiro plano** e **câmera**. Nenhuma delas exige formulário de declaração (só localização em segundo plano, SMS, contatos do sistema ou acesso a todos os arquivos exigiriam).
- Fotos e vídeos da galeria vêm pelo seletor de fotos do Android, sem permissão de armazenamento.

---

## 5.3 Categoria e contato
**Menu: Crescer → Presença na loja → Configurações da loja**

| Campo | Resposta |
|---|---|
| Categoria | **Mapas e navegação** (alternativa: Social) |
| Tags | Segurança, Transporte público, Comunidade (as que existirem na lista) |
| E-mail | suporte.redecidada@gmail.com |
| Site | https://projetomobileredecidada-production.up.railway.app/privacidade (opcional) |
| Telefone | deixar em branco |

---

## 6. Ficha da loja (Presença na loja → Página principal da loja)

### Nome (até 30 caracteres)
```
Rede Cidadã
```

### Descrição curta (até 80 caracteres)
```
Alertas de segurança do bairro, SOS para seus contatos e status dos trens em SP
```

### Descrição completa (até 4000 caracteres)
```
O Rede Cidadã é uma rede de moradores de São Paulo que avisam uns aos outros sobre o que está acontecendo na rua, perto de você.

O QUE VOCÊ PODE FAZER
• Ver no mapa os relatos de moradores perto da sua localização: assaltos, incêndios, alagamentos, tempestades, trânsito, iluminação e problemas na via.
• Publicar um relato com texto, foto ou vídeo, de forma anônima por padrão.
• Confirmar relatos que você também viu, para os outros saberem que é real.
• Acionar o SOS: o app abre o WhatsApp com uma mensagem pronta e a sua localização para os contatos de confiança (guardiões) que você cadastrou.
• Acompanhar o status das linhas de metrô e trem metropolitano de São Paulo (dados públicos da ARTESP).
• Ver o clima agora na sua região (Open-Meteo).

COMUNIDADE SEGURA
• Denuncie relatos falsos, ofensivos ou com dados pessoais. Relatos com várias denúncias ficam ocultos até a revisão.
• Bloqueie autores que você não quer mais ver.
• Regras claras: nada de acusar pessoas, expor rostos, placas ou endereços.

PRIVACIDADE
• Use sem conta, no modo visitante, para ver o mapa e os relatos.
• A conta pede só nome, e-mail e senha. Não pedimos CPF.
• Sem anúncios. Não vendemos dados.
• Exclua sua conta quando quiser, pelo app ou pela web.

IMPORTANTE
O Rede Cidadã é um aplicativo independente e não tem vínculo com governo, polícia, Metrô, CPTM, SPTrans ou ARTESP. Os relatos NÃO são enviados à polícia. Em emergência, ligue 190 (Polícia Militar), 193 (Bombeiros) ou 192 (SAMU).

Disponível para a cidade de São Paulo.

Dúvidas e sugestões: suporte.redecidada@gmail.com
```

### Recursos gráficos (prontos na pasta `loja/`)
| Item | Arquivo |
|---|---|
| Ícone 512 × 512 | `loja/icone-512.png` |
| Imagem de destaque 1024 × 500 | `loja/destaque-1024x500.png` |
| Prints do celular (1080 × 2160) | `loja/prints/01-inicio.png`, `02-relatos.png`, `03-sos.png`, `04-mobilidade.png` |

Os prints usam relatos de exemplo criados num servidor local (nunca em produção). O ícone e o destaque são gerados por `loja/gerador.html` a partir do ícone e das fontes do app.

### Observação para a Segurança dos dados
Foto de perfil, telefone, nascimento e endereço do "Editar perfil" ficam **só no aparelho** (não vão ao servidor). Dados que não saem do aparelho **não** entram no formulário de Segurança dos dados.

# Troca do banco em produção: SQLite → Postgres (Railway)

Passo a passo para trocar o banco do servidor no ar. Leva uns 15 minutos, e o app fica fora do ar só durante o deploy (1 a 2 minutos).

**Importante:** depois que este código estiver na `main`, **não faça deploy no Railway antes do passo 3**.
Sem `DATABASE_URL`, o servidor novo não sobe. Como o Railway confere o healthcheck, a versão antiga deve
continuar no ar nesse caso, mas é melhor não arriscar.

## O que acontece na troca
- O servidor novo cria as tabelas no Postgres (Flyway, migração `V1`).
- Com `MIGRATE_FROM_SQLITE` definida e o Postgres vazio, ele copia **uma vez** os dados de `/data/rede_cidada.db`.
  Vão junto usuários, relatos, anexos, comentários, confirmações, denúncias, bloqueios, guardiões, SOS e as sessões ainda válidas
  (ninguém precisa entrar de novo).
- O arquivo SQLite **não é alterado**. Fotos e vídeos continuam em `/data/uploads`.
- Se a cópia falhar, nada é gravado no Postgres e o servidor novo não sobe (a versão antiga continua no ar).
- Conversões: 0/1 → BOOLEAN, milissegundos → TIMESTAMPTZ. Ficam de fora o conteúdo de demonstração antigo,
  as linhas que apontam para contas ou relatos que não existem mais, as sessões vencidas e a tabela `reels`.
- Comentários antigos com nome passam a aparecer como "Cidadão Colaborador" (o SQLite guardava o nome em texto,
  sem ligação com a conta). Os anônimos continuam anônimos.

## Passo a passo
1. **Backup do volume:** no serviço do servidor, aba **Backups** → crie um backup manual do volume `/data`.
2. **Criar o Postgres:** no projeto do Railway, **New → Database → Add PostgreSQL**. Espere ficar *Active*.
3. **Variáveis do servidor** (aba *Variables* do serviço `ProjetoMobileRedeCidada`):
   - `DATABASE_URL` = `${{Postgres.DATABASE_URL}}` (digite assim mesmo: é uma referência, a senha não fica escrita)
   - `MIGRATE_FROM_SQLITE` = `/data/rede_cidada.db`
4. **Mesclar o PR** desta mudança na `main`.
5. **Deploy:** *Settings → Source → Check for updates* e depois **Deploy**.
6. **Conferir os logs do deploy:**
   - `Banco Postgres pronto (1 migração(ões) aplicada(s) agora)`
   - `Dados importados do SQLite (/data/rede_cidada.db): {users=…, incidents=…, …}`
7. **Testar:** `https://projetomobileredecidada-production.up.railway.app/api/v1/integrations/health` deve dar 200.
   No app: entrar com uma conta que já existia, ver os relatos e os guardiões.
8. **Depois de 1 dia sem problema:** apague a variável `MIGRATE_FROM_SQLITE` e faça um deploy.
   Guarde o `/data/rede_cidada.db` por 1 a 2 semanas e depois apague.

## Se der errado
- **Servidor novo não subiu:** veja o erro no log, corrija e faça deploy de novo. A versão antiga (SQLite) segue no ar.
- **Subiu, mas algo está errado:** em *Deployments*, escolha o deploy anterior → **Redeploy**. Ele volta a usar
  o SQLite, que não foi alterado. Atenção: o que foi criado no Postgres depois da troca não volta junto.
- Para tentar a importação de novo do zero: no Postgres do Railway, aba *Data*, apague as tabelas
  (ou recrie o banco) e faça deploy outra vez com `MIGRATE_FROM_SQLITE` definida.

## Custos e limites
- O Postgres do Railway usa os mesmos créditos do teste (US$ 5). Depois de ~18/12, os dois (servidor e banco)
  precisam do plano **Hobby**. Contas em teste têm os volumes apagados 30 dias depois do fim do teste.
- Backup diário do Postgres (`pg_dump` para o Cloudflare R2) fica para a próxima etapa.

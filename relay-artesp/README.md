# Intermediário ARTESP (Vercel, São Paulo)

A ARTESP não aceita conexões do servidor principal (Railway, EUA). Esta função roda em São Paulo
(`gru1`), consulta a API Trilhos e devolve a resposta ao servidor.

## Publicar
```powershell
cd relay-artesp
npx vercel login
npx vercel link
npx vercel env add ARTESP_API_KEY production
npx vercel env add RELAY_TOKEN production
npx vercel deploy --prod
```

## Ligar no servidor (variáveis do Railway)
- `ARTESP_RELAY_URL` = `https://<projeto>.vercel.app/api/status`
- `ARTESP_RELAY_TOKEN` = o mesmo valor de `RELAY_TOKEN`

Sem `ARTESP_RELAY_URL`, o servidor volta a consultar a ARTESP direto.

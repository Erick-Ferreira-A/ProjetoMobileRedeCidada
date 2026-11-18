// Intermediário da Rede Cidadã para a API Trilhos (ARTESP), rodando em São Paulo (região gru1).
// Só repassa o status das linhas. Exige o cabeçalho X-Relay-Token igual a RELAY_TOKEN.
// Variáveis na Vercel: ARTESP_API_KEY (chave da ARTESP) e RELAY_TOKEN (texto aleatório, 32+ caracteres).
const crypto = require("node:crypto");

const ARTESP_URL = "https://ccm.artesp.sp.gov.br/metroferroviario/api/status/";

function sameSecret(a, b) {
  const x = Buffer.from(String(a || ""));
  const y = Buffer.from(String(b || ""));
  return x.length === y.length && x.length > 0 && crypto.timingSafeEqual(x, y);
}

module.exports = async (req, res) => {
  res.setHeader("Cache-Control", "no-store");
  const expected = process.env.RELAY_TOKEN;
  if (!expected || expected.length < 32 || !sameSecret(req.headers["x-relay-token"], expected)) {
    return res.status(404).json({ message: "Não encontrado." });
  }
  if (req.method !== "GET") {
    return res.status(405).json({ message: "Use GET." });
  }
  try {
    const upstream = await fetch(ARTESP_URL, {
      headers: { Authorization: `Api-Key ${process.env.ARTESP_API_KEY}`, Accept: "application/json" },
      signal: AbortSignal.timeout(12000),
    });
    const body = await upstream.text();
    res.status(upstream.status);
    res.setHeader("Content-Type", upstream.headers.get("content-type") || "application/json");
    return res.send(body);
  } catch (err) {
    return res.status(502).json({ message: "ARTESP sem resposta.", detail: String(err && err.message) });
  }
};

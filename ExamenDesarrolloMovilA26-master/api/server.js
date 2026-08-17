const express = require('express');
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;
const SERVICE_ACCOUNT_PATH = process.env.SERVICE_ACCOUNT_PATH || path.join(__dirname, 'serviceAccountKey.json');
const DATA_DIR = process.env.DATA_DIR || path.join(__dirname, 'data');
const TOKENS_FILE = path.join(DATA_DIR, 'tokens.json');

if (!fs.existsSync(SERVICE_ACCOUNT_PATH)) {
  console.error(`No se encontro el service account en: ${SERVICE_ACCOUNT_PATH}`);
  console.error('Descargalo desde Firebase Console > Configuracion del proyecto > Cuentas de servicio.');
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(require(SERVICE_ACCOUNT_PATH)),
});

if (!fs.existsSync(DATA_DIR)) fs.mkdirSync(DATA_DIR, { recursive: true });

function loadTokens() {
  try {
    return JSON.parse(fs.readFileSync(TOKENS_FILE, 'utf8'));
  } catch {
    return [];
  }
}

function saveTokens(tokens) {
  fs.writeFileSync(TOKENS_FILE, JSON.stringify(tokens, null, 2));
}

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// La app movil registra su token FCM aqui
app.post('/api/devices', (req, res) => {
  const { token, name } = req.body;
  if (!token) return res.status(400).json({ error: 'token es requerido' });

  const tokens = loadTokens();
  const existing = tokens.find((t) => t.token === token);
  if (existing) {
    existing.name = name || existing.name;
    existing.updatedAt = new Date().toISOString();
  } else {
    tokens.push({ token, name: name || 'Dispositivo', createdAt: new Date().toISOString() });
  }
  saveTokens(tokens);
  res.json({ ok: true, total: tokens.length });
});

app.get('/api/devices', (req, res) => {
  res.json(loadTokens());
});

app.delete('/api/devices/:token', (req, res) => {
  const tokens = loadTokens().filter((t) => t.token !== req.params.token);
  saveTokens(tokens);
  res.json({ ok: true, total: tokens.length });
});

// Envio de notificaciones desde la interfaz web
// body: { title, body, imageUrl?, data?, target: 'all' | 'token' | 'topic', token?, topic? }
app.post('/api/notifications/send', async (req, res) => {
  const { title, body, imageUrl, data, target, token, topic } = req.body;
  if (!title || !body) return res.status(400).json({ error: 'title y body son requeridos' });

  // La app procesa message.getData(), por eso title/body van tambien en data
  const dataPayload = {
    title,
    body,
    ...(data || {}),
  };

  const base = {
    notification: { title, body, ...(imageUrl ? { imageUrl } : {}) },
    data: dataPayload,
    android: {
      priority: 'high',
      notification: { channelId: 'veterinaria_notification' },
    },
  };

  try {
    if (target === 'topic') {
      if (!topic) return res.status(400).json({ error: 'topic es requerido' });
      const id = await admin.messaging().send({ ...base, topic });
      return res.json({ ok: true, messageId: id });
    }

    if (target === 'token') {
      if (!token) return res.status(400).json({ error: 'token es requerido' });
      const id = await admin.messaging().send({ ...base, token });
      return res.json({ ok: true, messageId: id });
    }

    // target === 'all': enviar a todos los tokens registrados
    const tokens = loadTokens().map((t) => t.token);
    if (tokens.length === 0) return res.status(400).json({ error: 'No hay dispositivos registrados' });

    const result = await admin.messaging().sendEachForMulticast({ ...base, tokens });

    // Limpiar tokens invalidos
    const invalid = [];
    result.responses.forEach((r, i) => {
      if (!r.success && ['messaging/registration-token-not-registered', 'messaging/invalid-registration-token'].includes(r.error?.code)) {
        invalid.push(tokens[i]);
      }
    });
    if (invalid.length) saveTokens(loadTokens().filter((t) => !invalid.includes(t.token)));

    res.json({ ok: true, successCount: result.successCount, failureCount: result.failureCount, removedInvalid: invalid.length });
  } catch (err) {
    console.error('Error enviando notificacion:', err);
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/health', (req, res) => res.json({ ok: true }));

app.listen(PORT, () => {
  console.log(`Servidor de notificaciones escuchando en http://localhost:${PORT}`);
});

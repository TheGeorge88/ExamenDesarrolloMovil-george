# Veterinaria Lemas — Backend de Notificaciones Push (FCM)

Backend + panel web para enviar notificaciones push personalizadas a la app móvil
mediante Firebase Cloud Messaging, empaquetado en Docker.

## Requisitos previos

1. **Credencial de Firebase (obligatorio):**
   - Firebase Console → tu proyecto → ⚙️ *Configuración del proyecto* → pestaña
     **Cuentas de servicio** → botón **Generar nueva clave privada**.
   - Guarda el archivo descargado como `serviceAccountKey.json` **dentro de esta carpeta `api/`**.
   - Este archivo NO se sube a git (ya está en `.gitignore`).
2. Docker Desktop instalado y corriendo.

## Levantar con Docker

```bash
cd api
docker compose up -d --build
```

Panel web: **http://localhost:3000**

- Ver logs: `docker compose logs -f`
- Detener: `docker compose down`
- Los tokens registrados se guardan en el volumen `notifications-data` (persisten entre reinicios).

## Levantar sin Docker (opcional, para desarrollo)

```bash
cd api
npm install
npm start
```

## API

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/devices` | Registra el token FCM del móvil. Body: `{ "token": "...", "name": "Telefono de Juan" }` |
| GET | `/api/devices` | Lista dispositivos registrados |
| DELETE | `/api/devices/:token` | Elimina un dispositivo |
| POST | `/api/notifications/send` | Envía notificación. Body: `{ "title", "body", "imageUrl?", "data?", "target": "all"\|"token"\|"topic", "token?", "topic?" }` |
| GET | `/api/health` | Health check |

El servidor envía el mensaje con bloque `notification` **y** con `title`/`body`
duplicados en `data`, porque la app procesa `message.getData()` en
`AppFirebaseMessagingService`. También fija `channelId: veterinaria_notification`
(el mismo canal que crea `NotificationHelper`).

## Integración en la app Android

1. **Registrar el token en el backend.** El emulador accede a tu PC con
   `http://10.0.2.2:3000` (dispositivo físico: usa la IP local de tu PC, ej.
   `http://192.168.1.X:3000`, ambos en la misma red).

   En `AppFirebaseMessagingService` agrega:

   ```java
   @Override
   public void onNewToken(@NonNull String token) {
       super.onNewToken(token);
       registrarToken(token);
   }

   private void registrarToken(String token) {
       new Thread(() -> {
           try {
               java.net.HttpURLConnection con = (java.net.HttpURLConnection)
                       new java.net.URL("http://10.0.2.2:3000/api/devices").openConnection();
               con.setRequestMethod("POST");
               con.setRequestProperty("Content-Type", "application/json");
               con.setDoOutput(true);
               String json = "{\"token\":\"" + token + "\",\"name\":\"" + android.os.Build.MODEL + "\"}";
               con.getOutputStream().write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
               con.getResponseCode();
               con.disconnect();
           } catch (Exception e) {
               android.util.Log.e("FCM", "Error registrando token", e);
           }
       }).start();
   }
   ```

   Y al iniciar la app (ej. en `MainActivity.onCreate`) fuerza el registro inicial:

   ```java
   FirebaseMessaging.getInstance().getToken()
           .addOnSuccessListener(token -> Log.d("FCM", "Token: " + token));
   ```

2. **Mostrar la notificación en primer plano.** Completa `onMessageReceived`:

   ```java
   Map<String, String> data = message.getData();
   if (data.isEmpty()) return;
   new NotificationHelper(this).showNotification(
           data.get("title"), data.get("body"), data);
   ```

   (Cambia `showNotification` a `public` en `NotificationHelper`.)

3. Si usas HTTP (no HTTPS) en desarrollo, asegúrate de tener en el manifest:
   `android:usesCleartextTraffic="true"` en `<application>`.

## Prueba rápida sin la app

```bash
curl -X POST http://localhost:3000/api/devices \
  -H "Content-Type: application/json" \
  -d '{"token":"EL_TOKEN_DEL_LOGCAT","name":"Mi telefono"}'
```

Luego abre http://localhost:3000, llena título y mensaje y pulsa **Enviar notificación**.

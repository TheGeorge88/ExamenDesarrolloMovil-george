package com.example.veterinarialemas.notification;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        if (data.isEmpty()) {
            return;
        }

        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
        notificationHelper.showNotification(data.get("title"), data.get("body"), data);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        registerTokenInBackend(token);
    }

    /**
     * No olvidar cambiar la url que esta en la linea 41 debe ser la ip de su maquina.
     * @param token
     */
    public static void registerTokenInBackend(String token) {
        new Thread(() -> {
            try {
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new
                                            java.net.URL("http://192.168.100.19:3000/api/devices").openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String json = "{\"token\":\"" + token + "\",\"name\":\"" + android.os.Build.MODEL + "\"}";
                connection.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
                android.util.Log.d("FCM", "Registro del token, respuesta: "+ connection.getResponseCode());
                connection.disconnect();
            } catch (Exception e) {
                android.util.Log.e("FCM", "Error registrando token", e);
            }
        }).start();
    }
}

package com.example.veterinarialemas.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utilidad para subir imágenes a ImgBB (servicio gratuito de almacenamiento de imágenes).
 *
 * ImgBB ofrece almacenamiento gratuito de imágenes con las siguientes características:
 * - Completamente gratuito
 * - No requiere registro ni API key para uso básico
 * - URLs directas a las imágenes
 * - Sin límite de almacenamiento (con límites razonables de uso)
 *
 * Documentación: https://api.imgbb.com/
 *
 * @author Cafeteria App
 * @version 1.0
 */
public class ImageUploader {
    private static final MediaType IMAGES_JPEG = MediaType.parse("image/jpeg");
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * API Key de ImgBB (opcional, pero recomendado para mejor rendimiento).
     * Puedes obtener una API key gratuita en: https://api.imgbb.com/
     * Si no proporcionas una API key, ImgBB funcionará pero con límites más estrictos.
     *
     * Para obtener una API key gratuita:
     * 1. Ve a https://api.imgbb.com/
     * 2. Haz clic en "Get API Key"
     * 3. Completa el formulario (es gratis)
     * 4. Copia tu API key
     * 5. Reemplaza "TU_API_KEY_AQUI" con tu API key real (o déjalo vacío para usar sin API key)
     */
    private static final String IMGBB_API_KEY= "3d0250110a6fe1e0b5556bd222f24629"; // No compartir por nada del mundo esta key

    /**
     * URL del endpoint de ImgBB para subir imagenes
     */
    private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";

    /**
     * Interfaz para recibir el resultado de la subida de la imagen
     */
    public interface UploadCallBack {
        /**
         * Se ejecuta cuando la imagen se sube exitosamente
         * @param imageUrl
         */
        void onSuccess(String imageUrl);

        /**
         * Se ejecuta cuando ocurre un error al subir la imagen.
         * @param error
         */
        void onError(String error);
    }

    public static void uploadImage(@NonNull Bitmap bitmap, @NonNull UploadCallBack callBack) {
        //Convertir bitmap a bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        byte[] imageBytes = baos.toByteArray();

        // Crear el cuerpo de la peticion multipart
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                                                    .setType(MultipartBody.FORM)
                                                    .addFormDataPart("image", "image.jpg",
                                                            RequestBody.create(imageBytes, IMAGES_JPEG));

        // Agregar API key si esta configurada
        if (IMGBB_API_KEY != null && !IMGBB_API_KEY.isEmpty()) {
            requestBodyBuilder.addFormDataPart("key", IMGBB_API_KEY);
        }

        RequestBody requestBody = requestBodyBuilder.build();

        // Crear la peticion
        Request request = new Request.Builder()
                                     .url(IMGBB_UPLOAD_URL)
                                     .post(requestBody)
                                     .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onError("Error de conexion: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Error desconocido";
                    callBack.onError("Error de consumo: "+errorBody);
                    return;
                }

                try {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                        JSONObject data = jsonResponse.getJSONObject("data");
                        String imageURL = data.getString("url");
                        callBack.onSuccess(imageURL);
                    } else {
                        String errorMsg = jsonResponse.optJSONObject("error") != null ?
                                            jsonResponse.getJSONObject("error").optString("message", "Error desconocido") :
                                            "Error al subir imagen";
                        callBack.onError(errorMsg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onError("Error al mapear la respuesta: "+ e.getMessage());
                }
            }
        });
    }

}

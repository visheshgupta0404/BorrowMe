package com.example.borrowme.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImgBBUploader {
    private static final String API_KEY = "961bd14c38209410d795fccce7c8a7ec";
    private static final OkHttpClient client = new OkHttpClient();

    public interface UploadCallback {
        void onSuccess(String url);
        void onFailure(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        File file = getFileFromUri(context, imageUri);
        if (file == null) {
            callback.onFailure("Could not get file from URI");
            return;
        }

        // OkHttp 4.x Java syntax
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/*"));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", API_KEY)
                .addFormDataPart("image", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage() != null ? e.getMessage() : "Unknown error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String bodyString = responseBody != null ? responseBody.string() : null;
                    if (response.isSuccessful() && bodyString != null) {
                        try {
                            JSONObject json = new JSONObject(bodyString);
                            String url = json.getJSONObject("data").getString("url");
                            callback.onSuccess(url);
                        } catch (Exception e) {
                            callback.onFailure("JSON Parsing error: " + e.getMessage());
                        }
                    } else {
                        callback.onFailure("Upload failed: " + response.message());
                    }
                }
            }
        });
    }

    private static File getFileFromUri(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;
            File file = new File(context.getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return file;
        } catch (IOException e) {
            return null;
        }
    }
}

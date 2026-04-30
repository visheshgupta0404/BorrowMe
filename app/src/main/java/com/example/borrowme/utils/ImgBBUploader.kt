package com.example.borrowme.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImgBBUploader {
    private const val API_KEY = "961bd14c38209410d795fccce7c8a7ec" // I will replace this or ask the user to
    private val client = OkHttpClient()

    interface UploadCallback {
        fun onSuccess(url: String)
        fun onFailure(error: String)
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: UploadCallback) {
        val file = getFileFromUri(context, imageUri) ?: run {
            callback.onFailure("Could not get file from URI")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", API_KEY)
            .addFormDataPart("image", file.name, file.asRequestBody("image/*".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        val url = json.getJSONObject("data").getString("url")
                        callback.onSuccess(url)
                    } catch (e: Exception) {
                        callback.onFailure("JSON Parsing error: ${e.message}")
                    }
                } else {
                    callback.onFailure("Upload failed: ${response.message}")
                }
            }
        })
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}

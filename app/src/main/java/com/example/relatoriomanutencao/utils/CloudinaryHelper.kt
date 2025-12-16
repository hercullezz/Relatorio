package com.example.relatoriomanutencao.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dsk8ki4zs"
            config["secure"] = "true"
            MediaManager.init(context, config)
            isInitialized = true
        }
    }

    suspend fun uploadImage(context: Context, uri: Uri, uploadPreset: String = "ml_default"): String {
        return suspendCancellableCoroutine { continuation ->
            // Cloudinary SDK precisa do init antes de usar
            init(context)

            val requestId = MediaManager.get().upload(uri)
                .unsigned(uploadPreset)
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("Cloudinary", "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Opcional: reportar progresso
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        if (secureUrl != null) {
                            Log.d("Cloudinary", "Upload success: $secureUrl")
                            continuation.resume(secureUrl)
                        } else {
                            continuation.resumeWithException(Exception("Upload successful but URL is null"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("Cloudinary", "Upload error: ${error.description}")
                        continuation.resumeWithException(Exception(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        // Upload reagendado (sem rede, etc)
                    }
                })
                .dispatch()
            
            // Permite cancelar a coroutine se necessário
            continuation.invokeOnCancellation { 
                MediaManager.get().cancelRequest(requestId)
            }
        }
    }
}

package com.example.relatoriomanutencao.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dsk8ki4zs"
            config["api_key"] = "575411487817246" // Adicionado
            config["secure"] = "true"
            MediaManager.init(context, config)
            isInitialized = true
        }
    }

    suspend fun uploadImage(context: Context, uri: Uri, uploadPreset: String = "ml_default"): String {
        return withContext(Dispatchers.IO) {
            // 1. Comprimir imagem localmente antes do upload
            val compressedFile = compressImage(context, uri)
            
            // 2. Gerar tags para facilitar a limpeza futura
            // Ex: "mes_05_2024" permite apagar todas as fotos de maio de uma vez
            val monthTag = "mes_" + SimpleDateFormat("MM_yyyy", Locale.getDefault()).format(Date())
            val autoDeleteTag = "pode_apagar" // Tag genérica para identificar fotos temporárias

            suspendCancellableCoroutine { continuation ->
                init(context)

                // Usamos o arquivo comprimido para o upload
                val requestId = MediaManager.get().upload(compressedFile.absolutePath)
                    .unsigned(uploadPreset)
                    .option("resource_type", "image")
                    .option("tags", listOf(monthTag, autoDeleteTag)) // Adiciona tags de organização
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d("Cloudinary", "Upload started: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            // Limpa arquivo temporário
                            try { compressedFile.delete() } catch (e: Exception) {}

                            val secureUrl = resultData["secure_url"] as? String
                            if (secureUrl != null) {
                                Log.d("Cloudinary", "Upload success: $secureUrl")
                                continuation.resume(secureUrl)
                            } else {
                                continuation.resumeWithException(Exception("Upload successful but URL is null"))
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            // Limpa arquivo temporário mesmo em erro
                            try { compressedFile.delete() } catch (e: Exception) {}

                            Log.e("Cloudinary", "Upload error: ${error.description}")
                            continuation.resumeWithException(Exception(error.description))
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                        }
                    })
                    .dispatch()
                
                continuation.invokeOnCancellation { 
                    MediaManager.get().cancelRequest(requestId)
                    try { compressedFile.delete() } catch (e: Exception) {}
                }
            }
        }
    }

    /**
     * Comprime a imagem para reduzir uso de dados no upload.
     * Redimensiona para no máx 1920px e qualidade JPEG 80%.
     */
    private fun compressImage(context: Context, uri: Uri): File {
        // A. Ler dimensões apenas
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { 
            BitmapFactory.decodeStream(it, null, options)
        }

        // B. Calcular redução se for muito grande (> 1920px)
        val maxDim = 1920
        var inSampleSize = 1
        if (options.outHeight > maxDim || options.outWidth > maxDim) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }

        // C. Carregar imagem reduzida na memória
        val finalOptions = BitmapFactory.Options().apply { inSampleSize = inSampleSize }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { 
            BitmapFactory.decodeStream(it, null, finalOptions)
        } ?: throw Exception("Falha ao processar imagem")

        // D. Ajuste fino de redimensionamento
        var finalBitmap = bitmap
        if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth = if (bitmap.width > bitmap.height) maxDim else (maxDim * ratio).toInt()
            val newHeight = if (bitmap.width > bitmap.height) (maxDim / ratio).toInt() else maxDim
            finalBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            if (finalBitmap != bitmap) bitmap.recycle()
        }

        // E. Salvar em arquivo temporário com compressão JPEG 80%
        val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        finalBitmap.recycle()

        return tempFile
    }
}

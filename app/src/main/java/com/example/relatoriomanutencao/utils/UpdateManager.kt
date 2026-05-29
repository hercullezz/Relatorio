package com.example.relatoriomanutencao.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

class UpdateManager(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun checkForUpdates(url: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val response = URL(url).readText()
            val json = JSONObject(response)
            UpdateInfo(
                versionCode = json.getInt("versionCode"),
                versionName = json.getString("versionName"),
                downloadUrl = json.getString("downloadUrl"),
                releaseNotes = json.optString("releaseNotes", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadAndInstall(updateInfo: UpdateInfo) {
        val file = java.io.File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "update.apk"
        )
        if (file.exists()) {
            file.delete()
        }

        val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
            .setTitle("Atualizando Relatório")
            .setDescription("Baixando nova versão...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)
        val appContext = context.applicationContext

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id == downloadId) {
                    installApk(appContext)
                    appContext.unregisterReceiver(this)
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            appContext.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    private fun installApk(context: Context) {
        val file = java.io.File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "update.apk"
        )
        
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Verifica permissão para instalar apps desconhecidos no Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                // Após o usuário permitir, ele terá que clicar em instalar novamente ou o app pode tentar dnv.
                // Mas por enquanto, redirecionar é o passo 1.
                return
            }
        }

        context.startActivity(intent)
    }
}

package com.example.relatoriomanutencao.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.example.relatoriomanutencao.data.MaintenanceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.time.Instant
import java.util.Locale
import com.example.relatoriomanutencao.utils.ShiftManager

object PdfGenerator {

    // Configurações de Layout A4
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    suspend fun generateConsolidatedReport(context: Context, items: List<MaintenanceItem>) {
        withContext(Dispatchers.IO) {
            if (items.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Nenhum item para gerar relatório.", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val document = PdfDocument()

            // Estado da paginação
            var pageNumber = 1
            // A info da página é a mesma para todas as páginas
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPosition = MARGIN

            // --- Estilos de Texto ---
            val paintTitle = Paint().apply {
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val paintSubtitle = Paint().apply {
                textSize = 12f
                color = Color.DKGRAY
            }
            val paintSectionHeader = Paint().apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(0, 51, 102) // Azul escuro profissional
            }
            val paintTextBold = Paint().apply {
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val paintTextNormal = Paint().apply {
                textSize = 10f
                color = Color.BLACK
            }
            val paintLine = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            // --- Lógica de Data e Turno ---
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fileNameDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            // Inferir turno a partir dos itens; prefira valores vindos do servidor quando disponíveis
            val shiftInfos = items.map { item ->
                val serverWorkMillis = item.workDateMillisFromServer
                val serverShiftId = item.shiftId

                if (serverWorkMillis != null && serverShiftId != null) {
                    // Use the server-provided workDate + shiftId to ensure 3rd-shift midnight logic
                    ShiftManager.getShiftInfoForShiftId(serverShiftId, Instant.ofEpochMilli(serverWorkMillis))
                } else if (serverShiftId != null) {
                    // No server workDate, but shiftId known: compute workDate for that shift at item's timestamp
                    ShiftManager.getShiftInfoForShiftId(serverShiftId, Instant.ofEpochMilli(item.date))
                } else {
                    // Fallback: infer shift from the item's local timestamp
                    ShiftManager.getShiftInfo(item.date)
                }
            }
            val distinctWorkDates = shiftInfos.map { it.workDate }.distinct()
            val distinctShiftIds = shiftInfos.map { it.shiftId }.distinct()

            val reportDate: String
            val reportDateFilename: String
            val shiftName: String
            val shiftIdForFilename: String

            if (distinctWorkDates.size == 1 && distinctShiftIds.size == 1) {
                val si = shiftInfos.first()
                reportDate = dateFormat.format(si.workDate)
                reportDateFilename = fileNameDateFormat.format(si.workDate)
                shiftName = si.shiftName
                shiftIdForFilename = "T${si.shiftId}"
            } else {
                // Múltiplas datas/turnos: consolidado entre min e max
                val minDate = distinctWorkDates.minOrNull() ?: Date()
                val maxDate = distinctWorkDates.maxOrNull() ?: Date()
                reportDate = "${dateFormat.format(minDate)} - ${dateFormat.format(maxDate)}"
                reportDateFilename = "${fileNameDateFormat.format(minDate)}_to_${fileNameDateFormat.format(maxDate)}"
                shiftName = "Consolidado"
                shiftIdForFilename = "Consolidado"
            }

            // --- Helpers de Paginação e Rodapé ---
            fun drawFooter(canvas: android.graphics.Canvas, pageNum: Int) {
                val yPos = PAGE_HEIGHT - 20f // Posição a 20px da parte inferior
                val paintFooter = Paint().apply {
                    textSize = 8f
                    color = Color.GRAY
                    textAlign = Paint.Align.CENTER
                }
                val footerText = "Página $pageNum - Relatório gerado em ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}"
                canvas.drawText(footerText, PAGE_WIDTH / 2f, yPos, paintFooter)
            }

            fun checkPageBreak(heightNeeded: Float) {
                if (yPosition + heightNeeded > PAGE_HEIGHT - MARGIN) {
                    drawFooter(canvas, pageNumber)
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                }
            }

            // --- DESENHAR CABEÇALHO ---
            canvas.drawText("RELATÓRIO DIÁRIO DE MANUTENÇÃO", MARGIN, yPosition + 20, paintTitle)
            yPosition += 35
            canvas.drawText("Data do Turno: $reportDate", MARGIN, yPosition, paintSubtitle)
            canvas.drawText("Turno: $shiftName", MARGIN + 250, yPosition, paintSubtitle)
            yPosition += 15
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, paintLine)
            yPosition += 20

            // --- SEPARAÇÃO DOS DADOS ---
            val graphicsItems = items.filter { it.serviceType == "Gráfico de Produção" }
            val serviceItems = items.filter { it.serviceType != "Gráfico de Produção" }
                .groupBy { it.machine }
                .toSortedMap()

            // --- DESENHAR GRÁFICOS (GRID DINÂMICO) ---
            if (graphicsItems.isNotEmpty()) {
                checkPageBreak(30f)
                canvas.drawText("GRÁFICOS DE PRODUÇÃO E LINHAS", MARGIN, yPosition, paintSectionHeader)
                yPosition += 20

                val chunks = graphicsItems.chunked(2)

                for (chunk in chunks) {
                    val rowHeight = 200f
                    checkPageBreak(rowHeight)

                    if (chunk.size == 1) {
                        val item = chunk[0]
                        drawGraphItem(context, canvas, item, MARGIN + (CONTENT_WIDTH / 4), yPosition)
                    }
                    else {
                        val item1 = chunk[0]
                        val item2 = chunk[1]

                        drawGraphItem(context, canvas, item1, MARGIN, yPosition)
                        drawGraphItem(context, canvas, item2, MARGIN + (CONTENT_WIDTH / 2) + 10, yPosition)
                    }

                    yPosition += rowHeight + 20
                }

                yPosition += 10
                canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, paintLine)
                yPosition += 20
            }

            // --- DESENHAR SERVIÇOS (AGRUPADOS) ---
            if (serviceItems.isNotEmpty()) {
                checkPageBreak(30f)
                canvas.drawText("DETALHAMENTO DE SERVIÇOS REALIZADOS", MARGIN, yPosition, paintSectionHeader)
                yPosition += 20

                serviceItems.forEach { (machineName, maintenanceList) ->
                    checkPageBreak(40f)

                    val bgPaint = Paint().apply { color = Color.rgb(240, 240, 240) }
                    canvas.drawRect(MARGIN, yPosition - 12, PAGE_WIDTH - MARGIN, yPosition + 8, bgPaint)
                    canvas.drawText(machineName, MARGIN + 5, yPosition, paintTextBold)
                    yPosition += 20

                    for (item in maintenanceList.reversed()) {
                        val maxWidth = CONTENT_WIDTH - 20
                        val descLines = breakTextIntoLines(item.description, paintTextNormal, maxWidth)
                        val heightNeeded = 20f + (descLines.size * 12f)

                        val photoUris = item.photoUris.split(",").filter { it.isNotBlank() }.take(3)
                        val hasPhotos = photoUris.isNotEmpty()

                        val photosRowHeight = if (hasPhotos) 110f else 0f

                        checkPageBreak(heightNeeded + photosRowHeight)

                        val typePaint = Paint().apply {
                            textSize = 10f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            color = when(item.serviceType) {
                                "Corretiva" -> Color.RED
                                "Preventiva" -> Color.rgb(0, 100, 0)
                                "Informação" -> Color.rgb(0, 0, 150)
                                else -> Color.BLACK
                            }
                        }
                        canvas.drawText(item.serviceType.uppercase(), MARGIN + 10, yPosition, typePaint)
                        yPosition += 12

                        for (line in descLines) {
                            canvas.drawText(line, MARGIN + 10, yPosition, paintTextNormal)
                            yPosition += 12
                        }

                        if (hasPhotos) {
                            yPosition += 5

                            val destWidth = 150f
                            val destHeight = 100f
                            var currentX = MARGIN + 10f

                            for (uriString in photoUris) {
                                val bitmap = getBitmapFromUrlOrUri(context, uriString)
                                if (bitmap != null) {
                                    try {
                                        val dstRect = RectF(currentX, yPosition, currentX + destWidth, yPosition + destHeight)
                                        canvas.drawBitmap(bitmap, null, dstRect, null)
                                        bitmap.recycle()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                currentX += destWidth + 10
                            }

                            yPosition += photosRowHeight
                        }

                        yPosition += 8
                    }
                    yPosition += 10
                }
            }

            // Finaliza a última página
            drawFooter(canvas, pageNumber)
            document.finishPage(page)

            val timeStamp = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
            val fileName = "Relatorio_${shiftIdForFilename}_${reportDateFilename}_${timeStamp}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            try {
                document.writeTo(FileOutputStream(file))
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Relatório salvo: ${file.name}", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro ao salvar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                document.close()
            }
        }
    }

    private fun breakTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val finalLines = mutableListOf<String>()
        val paragraphs = text.split("\n") // Correção da quebra de linha

        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                finalLines.add("")
                continue
            }

            val words = paragraph.split(" ")
            val currentLine = StringBuilder()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = paint.measureText(testLine)

                if (width <= maxWidth) {
                    currentLine.setLength(0)
                    currentLine.append(testLine)
                } else {
                    if (currentLine.isNotEmpty()) {
                        finalLines.add(currentLine.toString())
                    }
                    currentLine.setLength(0)
                    currentLine.append(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                finalLines.add(currentLine.toString())
            }
        }
        return finalLines
    }

    private fun drawGraphItem(context: Context, canvas: android.graphics.Canvas, item: MaintenanceItem, x: Float, y: Float) {
        val paintTextBold = Paint().apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val paintTextSmall = Paint().apply {
            textSize = 8f
            color = Color.DKGRAY
        }
        canvas.drawText(item.machine, x, y, paintTextBold)

        val photoUris = item.photoUris.split(",").filter { it.isNotBlank() }
        if (photoUris.isNotEmpty()) {
            val imgUrl = photoUris[0]
            val bitmap = getBitmapFromUrlOrUri(context, imgUrl)
            if (bitmap != null) {
                val destWidth = 230f
                val destHeight = 150f
                val dstRect = RectF(x, y + 10, x + destWidth, y + 10 + destHeight)
                canvas.drawBitmap(bitmap, null, dstRect, null)
                bitmap.recycle()
            } else {
                canvas.drawText("[Imagem não carregada]", x, y + 50, paintTextSmall)
            }
        }

        if (item.description.isNotBlank() && item.description != "Registro de Gráfico de Produção") {
             val wrappedDesc = breakTextIntoLines(item.description, paintTextSmall, 230f)
             var textY = y + 170
             wrappedDesc.take(2).forEach { line ->
                 canvas.drawText(line, x, textY, paintTextSmall)
                 textY += 10
             }
        }
    }

    // Tenta carregar imagem da URL (Nuvem) ou URI (Local - Cache)
    private fun getBitmapFromUrlOrUri(context: Context, path: String): Bitmap? {
        Log.d("PdfGenerator", "Tentando baixar imagem: $path")
        return try {
            if (path.startsWith("http")) {
                var downloadUrl = path
                if (downloadUrl.contains("cloudinary.com") && downloadUrl.contains("/upload/")) {
                    downloadUrl = downloadUrl.replace("/upload/", "/upload/w_1024,q_auto,f_jpg/")
                    Log.d("PdfGenerator", "URL Otimizada: $downloadUrl")
                }
                val url = URL(downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()
                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()
                bitmap
            } else {
                // Uso da KTX para conversão de String para Uri
                val uri = path.toUri()
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Erro ao baixar imagem: $path", e)
            e.printStackTrace()
            null
        }
    }
}

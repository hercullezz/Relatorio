package com.example.relatoriomanutencao.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
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
import java.util.Locale

object PdfGenerator {

    // Configurações de Layout A4
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    suspend fun generateMaintenanceReport(context: Context, item: MaintenanceItem) {
        generateConsolidatedReport(context, listOf(item))
    }

    suspend fun generateConsolidatedReport(context: Context, items: List<MaintenanceItem>) {
        withContext(Dispatchers.IO) {
            if (items.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Nenhum item para gerar relatório.", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            
            // Estado da paginação
            var pageNumber = 1
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
            val now = Calendar.getInstance()
            if (now.get(Calendar.HOUR_OF_DAY) < 10) {
                now.add(Calendar.DAY_OF_YEAR, -1)
            }
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fileNameDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val reportDate = dateFormat.format(now.time)
            val reportDateFilename = fileNameDateFormat.format(now.time)
            val shiftName = "3º Turno (21:30 - 05:20)"

            // --- Helper de Nova Página ---
            fun checkPageBreak(heightNeeded: Float) {
                if (yPosition + heightNeeded > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    pageNumber++
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
                
                // Processa de 2 em 2
                val chunks = graphicsItems.chunked(2)
                
                for (chunk in chunks) {
                    // Altura da linha de gráficos (estimada: Título + Imagem + Obs)
                    val rowHeight = 200f
                    checkPageBreak(rowHeight)
                    
                    // Se o chunk tem 1 item, desenha centralizado
                    if (chunk.size == 1) {
                        val item = chunk[0]
                        drawGraphItem(context, canvas, item, MARGIN + (CONTENT_WIDTH / 4), yPosition)
                    } 
                    // Se tem 2 itens, desenha lado a lado
                    else {
                        val item1 = chunk[0]
                        val item2 = chunk[1]
                        
                        // Coluna 1
                        drawGraphItem(context, canvas, item1, MARGIN, yPosition)
                        
                        // Coluna 2 (Meio da página + margemzinha)
                        drawGraphItem(context, canvas, item2, MARGIN + (CONTENT_WIDTH / 2) + 10, yPosition)
                    }
                    
                    yPosition += rowHeight + 20 // Avança para próxima linha
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

                    for (item in maintenanceList) {
                        val descLines = item.description.chunked(95)
                        val heightNeeded = 20f + (descLines.size * 12f)
                        
                        // --- TRATAMENTO DE FOTOS MÚLTIPLAS ---
                        // Pega até 3 fotos
                        val photoUris = item.photoUris.split(",").filter { it.isNotBlank() }.take(3)
                        val hasPhotos = photoUris.isNotEmpty()
                        
                        // Se tiver foto, reserva altura (100px imagem + 10px margem)
                        val photosRowHeight = if (hasPhotos) 110f else 0f
                        
                        checkPageBreak(heightNeeded + photosRowHeight)

                        val typePaint = Paint().apply { 
                            textSize = 10f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            color = when(item.serviceType) {
                                "Corretiva" -> Color.RED
                                "Preventiva" -> Color.rgb(0, 100, 0)
                                "Informação" -> Color.rgb(0, 0, 150) // Azul
                                else -> Color.BLACK
                            }
                        }
                        canvas.drawText(item.serviceType.uppercase(), MARGIN + 10, yPosition, typePaint)
                        yPosition += 12

                        for (line in descLines) {
                            canvas.drawText(line, MARGIN + 10, yPosition, paintTextNormal)
                            yPosition += 12
                        }
                        
                        // --- DESENHAR ATÉ 3 FOTOS LADO A LADO ---
                        if (hasPhotos) {
                            yPosition += 5
                            
                            // Tamanho fixo para cada miniatura
                            val targetWidth = 150
                            val targetHeight = 100
                            var currentX = MARGIN + 10f
                            
                            for (uriString in photoUris) {
                                val bitmap = getBitmapFromUrlOrUri(context, uriString)
                                if (bitmap != null) {
                                    try {
                                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                                        canvas.drawBitmap(scaledBitmap, currentX, yPosition, null)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                // Avança X para a próxima foto (largura + 10px de espaçamento)
                                currentX += targetWidth + 10
                            }
                            
                            yPosition += photosRowHeight
                        }
                        
                        yPosition += 8
                    }
                    yPosition += 10
                }
            }

            // Rodapé
            checkPageBreak(30f)
            yPosition += 20
            val paintFooter = Paint().apply { textSize = 8f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
            canvas.drawText("Relatório gerado via App Relatório Manutenção em ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())}", PAGE_WIDTH / 2f, yPosition, paintFooter)

            document.finishPage(page)

            val timeStamp = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
            val fileName = "Relatorio_3Turno_${reportDateFilename}_${timeStamp}.pdf"
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
    
    // Função auxiliar para desenhar um item gráfico (Mini Card)
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
        
        // Título (Nome da Linha)
        canvas.drawText(item.machine, x, y, paintTextBold)
        
        // Imagem
        val photoUris = item.photoUris.split(",").filter { it.isNotBlank() }
        if (photoUris.isNotEmpty()) {
            val imgUrl = photoUris[0]
            val bitmap = getBitmapFromUrlOrUri(context, imgUrl)
            
            if (bitmap != null) {
                // Área da imagem: Largura fixa (metade da página - margem), Altura fixa (150)
                val targetWidth = 230
                val targetHeight = 150
                
                // Scale to fit
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                canvas.drawBitmap(scaledBitmap, x, y + 10, null)
            } else {
                canvas.drawText("[Imagem não carregada]", x, y + 50, paintTextSmall)
            }
        }
        
        // Obs (se houver)
        if (item.description.isNotBlank() && item.description != "Registro de Gráfico de Produção") {
             val shortDesc = if (item.description.length > 45) item.description.substring(0, 42) + "..." else item.description
             canvas.drawText("Obs: $shortDesc", x, y + 170, paintTextSmall)
        }
    }
    
    // Tenta carregar imagem da URL (Nuvem) ou URI (Local - Cache)
    private fun getBitmapFromUrlOrUri(context: Context, path: String): Bitmap? {
        // Log para debug
        Log.d("PdfGenerator", "Tentando baixar imagem: $path")
        return try {
            if (path.startsWith("http")) {
                val url = URL(path)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 5000 // 5 segundos de timeout
                connection.readTimeout = 5000
                connection.connect()
                val input: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()
                bitmap
            } else {
                val uri = Uri.parse(path)
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

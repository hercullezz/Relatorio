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
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.example.relatoriomanutencao.data.MaintenanceItem
import com.parse.ParseCloud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    suspend fun generateConsolidatedReport(
        context: Context,
        items: List<MaintenanceItem>,
        currentShiftInfo: ShiftManager.ShiftInfo? = null
    ) {
        withContext(Dispatchers.IO) {
            if (items.isEmpty()) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Nenhum item.", Toast.LENGTH_SHORT).show() }
                return@withContext
            }

            val document = PdfDocument()
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPosition = MARGIN

            val paintTitle = Paint().apply { textSize = 24f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = Color.BLACK }
            val paintSubtitle = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); color = Color.DKGRAY }
            val paintSectionHeader = Paint().apply { textSize = 16f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = Color.rgb(0, 51, 102) }
            val paintTextBold = Paint().apply { textSize = 12f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = Color.BLACK }
            val paintTextNormal = Paint().apply { textSize = 12f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); color = Color.BLACK }
            val paintLine = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fileNameDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val shiftInfos = items.map { item ->
                val serverWorkMillis = item.workDateMillisFromServer
                val serverShiftId = item.shiftId
                if (serverWorkMillis != null && serverShiftId != null) {
                    ShiftManager.getShiftInfoForShiftIdAndWorkDate(serverShiftId, serverWorkMillis)
                } else if (serverShiftId != null) {
                    ShiftManager.getShiftInfoForShiftId(serverShiftId, Instant.ofEpochMilli(item.date))
                } else {
                    ShiftManager.getShiftInfo(Instant.ofEpochMilli(item.date))
                }
            }

            val reportDate: String
            val reportDateFilename: String
            val shiftName: String
            val shiftIdForFilename: String

            if (currentShiftInfo != null) {
                reportDate = dateFormat.format(currentShiftInfo.workDate)
                reportDateFilename = fileNameDateFormat.format(currentShiftInfo.workDate)
                shiftName = currentShiftInfo.shiftName
                shiftIdForFilename = "T${currentShiftInfo.shiftId}"
            } else {
                val distinctWorkDates = shiftInfos.map { it.workDate }.distinct()
                val distinctShiftIds = shiftInfos.map { it.shiftId }.distinct()
                if (distinctWorkDates.size == 1 && distinctShiftIds.size == 1) {
                    val si = shiftInfos.first()
                    reportDate = dateFormat.format(si.workDate); reportDateFilename = fileNameDateFormat.format(si.workDate)
                    shiftName = si.shiftName; shiftIdForFilename = "T${si.shiftId}"
                } else {
                    val minDate = distinctWorkDates.minOrNull() ?: Date()
                    val maxDate = distinctWorkDates.maxOrNull() ?: Date()
                    reportDate = "${dateFormat.format(minDate)} - ${dateFormat.format(maxDate)}"
                    reportDateFilename = "${fileNameDateFormat.format(minDate)}_to_${fileNameDateFormat.format(maxDate)}"
                    shiftName = "Consolidado"; shiftIdForFilename = "Consolidado"
                }
            }

            fun drawFooter(canvas: android.graphics.Canvas, pageNum: Int) {
                val yPos = PAGE_HEIGHT - 20f
                val paintFooter = Paint().apply { textSize = 8f; color = Color.GRAY; textAlign = Paint.Align.CENTER }
                canvas.drawText("Página $pageNum - Gerado em ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", PAGE_WIDTH / 2f, yPos, paintFooter)
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

            canvas.drawText("RELATÓRIO DIÁRIO DE MANUTENÇÃO", MARGIN, yPosition + 20, paintTitle)
            yPosition += 45
            canvas.drawText("Data do Turno: $reportDate", MARGIN, yPosition, paintSubtitle)
            canvas.drawText("Turno: $shiftName", MARGIN + 250, yPosition, paintSubtitle)
            yPosition += 20
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, paintLine)
            yPosition += 25

            // --- GRÁFICOS NO TOPO ---
            val graphItems = items.filter { it.serviceType == "Gráfico de Produção" }
                .groupBy { it.machine }
                .map { (_, list) -> list.maxByOrNull { it.date }!! }
                .sortedBy { it.machine }

            if (graphItems.isNotEmpty()) {
                checkPageBreak(30f)
                val paintBoldCenter = Paint().apply { textSize = 17f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = Color.rgb(0, 84, 166); textAlign = Paint.Align.CENTER }
                canvas.drawText("GRÁFICOS DE PRODUÇÃO", PAGE_WIDTH / 2f, yPosition, paintBoldCenter)
                yPosition += 30
                
                val graphHeight = 150f
                val gap = 15f
                val graphWidth = (CONTENT_WIDTH - gap) / 2f
                val titleHeight = 15f
                
                graphItems.chunked(2).forEach { rowItems ->
                    checkPageBreak(graphHeight + titleHeight + 20f)
                    if (rowItems.size == 1) {
                        val xStart = MARGIN + (CONTENT_WIDTH - graphWidth) / 2f
                        drawChart(canvas, rowItems[0], xStart, yPosition, graphWidth, graphHeight, paintTextBold, context)
                    } else {
                        drawChart(canvas, rowItems[0], MARGIN, yPosition, graphWidth, graphHeight, paintTextBold, context)
                        drawChart(canvas, rowItems[1], MARGIN + graphWidth + gap, yPosition, graphWidth, graphHeight, paintTextBold, context)
                    }
                    yPosition += graphHeight + titleHeight + 20f
                }
                canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, paintLine)
                yPosition += 25f
            }

            // Agrupamento por Linha -> Máquina
            val lineGroups = items.filter { it.serviceType != "Gráfico de Produção" }
                .groupBy { parseMachineName(it.machine).first }
                .toSortedMap()

            lineGroups.forEach { (lineName, machines) ->
                // Header da Linha (Italac Blue)
                checkPageBreak(40f)
                val paintLineHeader = Paint().apply { color = Color.rgb(0, 84, 166) }
                canvas.drawRect(MARGIN - 5, yPosition - 15, PAGE_WIDTH - MARGIN + 5, yPosition + 10, paintLineHeader)
                val paintLineText = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = Color.WHITE }
                canvas.drawText(lineName.uppercase(), MARGIN + 5, yPosition, paintLineText)
                yPosition += 35

                val machinesGrouped = machines.groupBy { parseMachineName(it.machine).second }.toSortedMap()
                
                machinesGrouped.forEach { (machineOnly, maintenanceList) ->
                    checkPageBreak(30f)
                    val paintMachineHeader = Paint().apply { color = Color.rgb(245, 245, 245) }
                    canvas.drawRect(MARGIN, yPosition - 14, PAGE_WIDTH - MARGIN, yPosition + 8, paintMachineHeader)
                    canvas.drawText("MÁQUINA: $machineOnly", MARGIN + 5, yPosition, paintTextBold)
                    yPosition += 28

                    for (item in maintenanceList.reversed()) {
                        val descLines = breakTextIntoLines(item.description, paintTextNormal, CONTENT_WIDTH - 20)
                        val photoUris = item.photoUris.split(",").filter { it.isNotBlank() }.take(3)
                        val photosRowHeight = if (photoUris.isNotEmpty()) 120f else 0f
                        checkPageBreak(30f + (descLines.size * 18f) + photosRowHeight)

                        // Tipo de Serviço com destaque (Italac Red)
                        val paintType = Paint().apply { textSize = 11f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); color = Color.rgb(238, 28, 37) }
                        canvas.drawText(item.serviceType.uppercase(), MARGIN + 10, yPosition, paintType)
                        yPosition += 18
                        
                        for (line in descLines) { 
                            canvas.drawText(line, MARGIN + 10, yPosition, paintTextNormal)
                            yPosition += 18 
                        }

                        if (photoUris.isNotEmpty()) {
                            yPosition += 8; var currentX = MARGIN + 10f
                            val photoWidth = 150f
                            val photoHeight = 110f
                            for (uriString in photoUris) {
                                val bitmap = getBitmapFromUrlOrUri(context, uriString)
                                if (bitmap != null) {
                                    drawBitmapFitCenter(canvas, bitmap, currentX, yPosition, photoWidth, photoHeight)
                                    bitmap.recycle()
                                }
                                currentX += photoWidth + 15f
                            }
                            yPosition += photoHeight + 10f
                        }
                        yPosition += 5
                        canvas.drawLine(MARGIN + 10, yPosition, PAGE_WIDTH - MARGIN - 10, yPosition, Paint().apply { color = Color.rgb(240, 240, 240); strokeWidth = 0.5f })
                        yPosition += 15
                    }
                    yPosition += 5
                }
                yPosition += 15
            }

            drawFooter(canvas, pageNumber)
            document.finishPage(page)
            val fileName = "Relatorio_${shiftIdForFilename}_${reportDateFilename}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            try {
                document.writeTo(FileOutputStream(file))
                withContext(Dispatchers.Main) { Toast.makeText(context, "Salvo: ${file.name}", Toast.LENGTH_LONG).show() }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show() }
            } finally { document.close() }
        }
    }

    private fun parseMachineName(fullName: String): Pair<String, String> {
        val parts = fullName.split(" - ", " / ")
        return if (parts.size >= 2) {
            Pair(parts[0].trim(), parts[1].trim())
        } else {
            Pair("Geral", fullName.trim())
        }
    }

    private fun drawChart(canvas: android.graphics.Canvas, item: MaintenanceItem, x: Float, y: Float, w: Float, h: Float, paint: Paint, context: Context) {
        val rawName = item.machine.replace("LINHA ", "", ignoreCase = true).trim()
        val cleanName = if (rawName.all { it.isDigit() }) "Linha $rawName" else rawName
        
        val oldAlign = paint.textAlign
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(cleanName, x + (w / 2f), y + 10f, paint)
        paint.textAlign = oldAlign
        
        val photoUri = item.photoUris.split(",").firstOrNull { it.isNotBlank() }
        if (photoUri != null) {
            val bitmap = getBitmapFromUrlOrUri(context, photoUri)
            if (bitmap != null) {
                drawBitmapFitCenter(canvas, bitmap, x, y + 15f, w, h)
                bitmap.recycle()
            }
        }
    }

    private fun breakTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>(); var currentLine = StringBuilder()
        for (word in text.split(" ")) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) currentLine.append(if (currentLine.isEmpty()) word else " $word")
            else { lines.add(currentLine.toString()); currentLine = StringBuilder(word) }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    private fun getBitmapFromUrlOrUri(context: Context, path: String): Bitmap? {
        return try {
            if (path.startsWith("http")) {
                val result: Map<String, Any> = ParseCloud.callFunction("getPhotoAsBase64", mapOf("photoUrl" to path))
                val base64String = result["base64"] as? String
                if (base64String != null) { val bytes = Base64.decode(base64String, Base64.DEFAULT); BitmapFactory.decodeByteArray(bytes, 0, bytes.size) } else null
            } else { context.contentResolver.openInputStream(path.toUri())?.use { BitmapFactory.decodeStream(it) } }
        } catch (e: Exception) { null }
    }

    private fun drawBitmapFitCenter(canvas: android.graphics.Canvas, bitmap: Bitmap, dstX: Float, dstY: Float, dstW: Float, dstH: Float) {
        val scale = kotlin.math.min(dstW / bitmap.width.toFloat(), dstH / bitmap.height.toFloat())
        val fitW = bitmap.width * scale
        val fitH = bitmap.height * scale
        val left = dstX + (dstW - fitW) / 2f
        val top = dstY + (dstH - fitH) / 2f
        canvas.drawBitmap(bitmap, null, RectF(left, top, left + fitW, top + fitH), null)
    }
}

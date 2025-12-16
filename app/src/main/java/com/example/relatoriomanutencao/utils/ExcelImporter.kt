package com.example.relatoriomanutencao.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.relatoriomanutencao.data.StockItem
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object ExcelImporter {

    private data class TempItem(
        var description: String = "",
        var quantity: Int = 0,
        val addresses: MutableSet<String> = mutableSetOf() 
    )

    fun parseExcel(context: Context, uri: Uri): List<StockItem> {
        // Verifica extensão ou MIME type simples para decidir estratégia
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        
        // Se for CSV ou texto, usa o parser leve
        if (type?.contains("csv") == true || type?.contains("text") == true || uri.toString().endsWith(".csv")) {
            return parseCSV(context, uri)
        }

        // Se for Excel, tenta usar Apache POI (com risco de memória)
        return parseXLSX(context, uri)
    }

    private fun parseCSV(context: Context, uri: Uri): List<StockItem> {
        val stockMap = mutableMapOf<String, TempItem>()
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine() // Pular cabeçalho se tiver
                    // Detecção simples de cabeçalho: se a primeira linha tem "CÓDIGO" ou "CODIGO"
                    if (line != null && !line.uppercase().contains("CODIGO") && !line.uppercase().contains("CÓDIGO")) {
                         // Se não parece cabeçalho, processa ela. Se parece, ignora e lê a próxima.
                         // Assumindo que TEM cabeçalho padrão, ignoramos a primeira.
                    }

                    while (line != null) {
                        line = reader.readLine() ?: break
                        
                        // CSV simples separado por ; ou ,
                        // Vamos assumir ; que é comum no Excel BR, mas tentar , se falhar
                        val tokens = line.split(";")
                        if (tokens.size < 2) {
                             val tokensComma = line.split(",")
                             if (tokensComma.size >= 2) processCsvLine(tokensComma, stockMap)
                        } else {
                            processCsvLine(tokens, stockMap)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ExcelImporter", "Erro ao ler CSV", e)
            throw Exception("Erro ao ler CSV: ${e.message}. Verifique o formato.")
        }

        return convertMapToList(stockMap)
    }

    private fun processCsvLine(tokens: List<String>, map: MutableMap<String, TempItem>) {
        // Layout esperado CSV (Flexível):
        // 0: Código
        // 1 ou 2: Descrição
        // 5: Quantidade (ou procurar numérico)
        // 8: Endereço
        
        // Vamos tentar ser robustos. Pegar Código (0) e Descrição (2 ou 1)
        if (tokens.isEmpty()) return
        val code = tokens[0].trim()
        if (code.isBlank()) return

        val desc = if (tokens.size > 2) tokens[2].trim() else if (tokens.size > 1) tokens[1].trim() else ""
        
        // Tentar achar quantidade na coluna 5 (F) ou 4 ou 3
        var qty = 0
        if (tokens.size > 5) {
            qty = tokens[5].replace(",", ".").toDoubleOrNull()?.toInt() ?: 0
        }
        
        // Endereço na coluna 8 (I)
        val address = if (tokens.size > 8) tokens[8].trim() else ""

        val item = map.getOrPut(code) { TempItem() }
        if (item.description.isBlank()) item.description = desc
        item.quantity += qty
        if (address.isNotBlank()) item.addresses.add(address)
    }

    private fun parseXLSX(context: Context, uri: Uri): List<StockItem> {
        val stockMap = mutableMapOf<String, TempItem>()
        var inputStream: InputStream? = null
        
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            // CUIDADO: WorkbookFactory carrega tudo na RAM.
            val workbook = WorkbookFactory.create(inputStream)
            
            val sheetDados = workbook.getSheet("DADOS") ?: run {
                if (workbook.numberOfSheets > 0) workbook.getSheetAt(0) else null
            }

            if (sheetDados != null) {
                processDadosSheet(sheetDados, stockMap)
            }

            val sheetArmazem = workbook.getSheet("ARMAZÉM 05") ?: run {
                if (workbook.numberOfSheets > 1) workbook.getSheetAt(1) else null
            }

            if (sheetArmazem != null) {
                processArmazemSheet(sheetArmazem, stockMap)
            }

            workbook.close()
        } catch (e: Exception) {
            if (e is OutOfMemoryError) {
                throw Exception("Arquivo Excel muito grande! Por favor, salve como CSV e tente novamente.")
            }
            Log.e("ExcelImporter", "Erro ao importar XLSX: ${e.message}")
            // Se falhar o Excel, lançamos para o ViewModel tratar
            throw e
        } finally {
            inputStream?.close()
        }
        
        return convertMapToList(stockMap)
    }
    
    private fun convertMapToList(map: Map<String, TempItem>): List<StockItem> {
        return map.map { (code, temp) ->
            val finalAddress = temp.addresses.joinToString(" / ")
            StockItem(
                code = code,
                description = temp.description,
                address = finalAddress,
                quantity = temp.quantity
            )
        }
    }

    private fun processDadosSheet(sheet: Sheet, map: MutableMap<String, TempItem>) {
        val iterator = sheet.rowIterator()
        if (iterator.hasNext()) iterator.next()

        while (iterator.hasNext()) {
            val row = iterator.next()
            val code = getCellValueAsString(row.getCell(0)).trim()
            if (code.isBlank()) continue

            val desc = getCellValueAsString(row.getCell(2)).trim()
            val detail = getCellValueAsString(row.getCell(3)).trim()
            val fullDesc = if (detail.isNotBlank()) "$desc - $detail" else desc

            val item = map.getOrPut(code) { TempItem() }
            item.description = fullDesc
        }
    }

    private fun processArmazemSheet(sheet: Sheet, map: MutableMap<String, TempItem>) {
        val iterator = sheet.rowIterator()
        if (iterator.hasNext()) iterator.next()

        while (iterator.hasNext()) {
            val row = iterator.next()
            val code = getCellValueAsString(row.getCell(0)).trim()
            if (code.isBlank()) continue

            val desc = getCellValueAsString(row.getCell(2)).trim()
            val qtyStr = getCellValueAsString(row.getCell(5))
            val address = getCellValueAsString(row.getCell(8)).trim()

            val qty = try {
                qtyStr.toDouble().toInt()
            } catch (e: Exception) {
                0
            }

            val item = map.getOrPut(code) { TempItem() }
            if (item.description.isBlank()) {
                item.description = desc
            }
            item.quantity += qty
            if (address.isNotBlank()) {
                item.addresses.add(address)
            }
        }
    }

    private fun getCellValueAsString(cell: org.apache.poi.ss.usermodel.Cell?): String {
        if (cell == null) return ""
        return try {
            when (cell.cellType) {
                CellType.STRING -> cell.stringCellValue
                CellType.NUMERIC -> {
                    val value = cell.numericCellValue
                    if (value == value.toLong().toDouble()) {
                        value.toLong().toString()
                    } else {
                        value.toString()
                    }
                }
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}

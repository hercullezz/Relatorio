package com.example.relatoriomanutencao.utils

import android.content.Context
import android.net.Uri
import com.example.relatoriomanutencao.data.StockItem
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvImporter {
    /**
     * Lê um arquivo CSV linha por linha e executa uma ação para cada lote de itens.
     *
     * Formato esperado:
     * Coluna 1: CODIGO
     * Coluna 2: DESCRICAO
     * Coluna 3: Desc Detalh.
     *
     * O separador deve ser ";" (ponto e vírgula).
     */
    suspend fun processCsvInBatches(
        context: Context,
        uri: Uri,
        batchSize: Int = 400,
        onBatchReady: suspend (List<StockItem>, Int) -> Unit
    ) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        val currentBatch = ArrayList<StockItem>(batchSize)
        var totalProcessed = 0
        var lineCount = 0

        try {
            var line = reader.readLine()
            while (line != null) {
                lineCount++

                // Pular cabeçalho (Linha 1)
                if (lineCount == 1) {
                    line = reader.readLine()
                    continue
                }

                if (line.isBlank()) {
                    line = reader.readLine()
                    continue
                }

                // Usa nossa função personalizada para quebrar a linha respeitando aspas
                val parts = splitCsvLine(line)

                if (parts.isNotEmpty()) {
                    // Coluna 1: Código (Remove aspas se houver e espaços)
                    val code = parts.getOrElse(0) { "" }.replace("\"", "").trim()
                    
                    // Coluna 2: Descrição (Remove aspas)
                    val descSimples = parts.getOrElse(1) { "" }.replace("\"", "").trim()
                    
                    // Coluna 3: Descrição Detalhada
                    val descDetalhada = parts.getOrElse(2) { "" }.replace("\"", "").trim()
                    
                    // Lógica Inteligente para Descrição
                    val fullDescription = when {
                        descDetalhada.isBlank() -> descSimples
                        descSimples.equals(descDetalhada, ignoreCase = true) -> descSimples
                        else -> "$descSimples | $descDetalhada"
                    }

                    if (code.isNotBlank()) {
                        val item = StockItem(
                            code = code,
                            description = fullDescription,
                            quantity = 0, 
                            address = "" 
                        )
                        
                        currentBatch.add(item)
                        
                        if (currentBatch.size >= batchSize) {
                            // Agora o onBatchReady funciona pois estamos fora de uma lambda bloqueante
                            onBatchReady(currentBatch.toList(), totalProcessed)
                            totalProcessed += currentBatch.size
                            currentBatch.clear()
                        }
                    }
                }
                
                // Lê a próxima linha
                line = reader.readLine()
            }
            
            // Processar o restante
            if (currentBatch.isNotEmpty()) {
                onBatchReady(currentBatch.toList(), totalProcessed)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            reader.close()
            inputStream.close()
        }
    }
    
    // Função auxiliar para quebrar linha CSV respeitando aspas
    private fun splitCsvLine(line: String): List<String> {
        val result = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes 
                char == ';' && !inQuotes -> { 
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(char)
            }
        }
        result.add(sb.toString()) 
        return result
    }
}

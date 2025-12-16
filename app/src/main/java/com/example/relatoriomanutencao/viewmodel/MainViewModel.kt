package com.example.relatoriomanutencao.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.relatoriomanutencao.data.AppDatabase
import com.example.relatoriomanutencao.data.Machine
import com.example.relatoriomanutencao.data.MachineConfigurationRepository
import com.example.relatoriomanutencao.data.MaintenanceItem
import com.example.relatoriomanutencao.data.ProductionLine
import com.example.relatoriomanutencao.data.StockItem
import com.example.relatoriomanutencao.utils.CsvImporter
import com.parse.ParseCloud
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val stockDao = database.stockDao()
    private val machineConfigRepository = MachineConfigurationRepository(
        database.productionLineDao(),
        database.machineDao()
    )
    
    // --- Maintenance State ---
    private val _maintenanceItems = MutableStateFlow<List<MaintenanceItem>>(emptyList())
    val maintenanceItems: StateFlow<List<MaintenanceItem>> = _maintenanceItems.asStateFlow()

    // --- Stock State ---
    private val _stockSearchQuery = MutableStateFlow("")
    val stockSearchQuery: StateFlow<String> = _stockSearchQuery.asStateFlow()
    
    private val _cloudStockItems = MutableStateFlow<List<StockItem>>(emptyList())
    val stockItems: StateFlow<List<StockItem>> = _cloudStockItems.asStateFlow()
    
    private val _isSearchByCode = MutableStateFlow(true)
    val isSearchByCode: StateFlow<Boolean> = _isSearchByCode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- Machine Configuration State ---
    val allProductionLines: StateFlow<List<ProductionLine>> = machineConfigRepository.allProductionLines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMachines: StateFlow<List<Machine>> = machineConfigRepository.allMachines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val machinesWithoutLine: StateFlow<List<Machine>> = machineConfigRepository.machinesWithoutLine
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshMaintenanceList()
        syncMachineConfiguration()
        
        viewModelScope.launch {
            _stockSearchQuery
                .debounce(800L)
                .collect { query ->
                    searchStockInBack4App(query)
                }
        }
    }
    
    // --- MANUTENÇÃO (BACK4APP) ---
    
    fun refreshMaintenanceList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.orderByDescending("createdAt")
                query.limit = 100
                
                val results = query.find()
                
                val items = results.map { obj ->
                    val photos = obj.getList<ParseFile>("photos")
                    val photoUrls = photos?.joinToString(",") { it.url } ?: ""

                    MaintenanceItem(
                        id = 0, 
                        machine = obj.getString("machine") ?: "",
                        serviceType = obj.getString("type") ?: "",
                        description = obj.getString("description") ?: "",
                        date = obj.createdAt.time,
                        photoUris = photoUrls 
                    )
                }
                _maintenanceItems.value = items
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao buscar serviços: ${e.message}")
            }
        }
    }

    fun addMaintenanceItem(machine: String, serviceType: String, description: String, photoUris: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val serviceData = hashMapOf(
                    "machine" to machine,
                    "type" to serviceType,
                    "description" to description
                )
                val photosAsBase64 = processPhotos(photoUris)
                val params = hashMapOf(
                    "serviceData" to serviceData,
                    "photosAsBase64" to photosAsBase64
                )

                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Enviando...", Toast.LENGTH_SHORT).show() }

                ParseCloud.callFunction<Any>("saveServiceWithPhotos", params)

                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Serviço salvo!", Toast.LENGTH_SHORT).show()
                    refreshMaintenanceList() 
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateMaintenanceItem(originalItem: MaintenanceItem, newDescription: String, newPhotoUris: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.whereEqualTo("machine", originalItem.machine)
                query.whereEqualTo("description", originalItem.description)
                val results = query.find()
                
                if (results.isNotEmpty()) {
                    // Encontra o objeto correto (pela data mais próxima)
                    val parseObject = results.minByOrNull { Math.abs(it.createdAt.time - originalItem.date) }
                    
                    if (parseObject != null) {
                        parseObject.put("description", newDescription)
                        
                        // --- Lógica de Atualização de Fotos ---
                        val allUris = newPhotoUris.split(",").filter { it.isNotBlank() }
                        
                        // 1. Fotos Antigas (URLs do Back4App) que devem ser MANTIDAS
                        val keptUrls = allUris.filter { it.startsWith("http") }
                        
                        // 2. Fotos Novas (URIs Locais) que devem ser ENVIADAS
                        val newLocalUris = allUris.filter { !it.startsWith("http") }

                        // Recupera as fotos que já existem no objeto
                        val currentParseFiles = parseObject.getList<ParseFile>("photos") ?: emptyList()
                        
                        // Filtra a lista do servidor para manter apenas as que ainda estão na lista 'keptUrls'
                        val updatedParseFiles = currentParseFiles.filter { pf -> 
                            keptUrls.any { url -> url.contains(pf.name) || pf.url == url } 
                        }.toMutableList()
                        
                        // Processa e sobe as novas fotos
                        if (newLocalUris.isNotEmpty()) {
                             withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Enviando novas fotos...", Toast.LENGTH_SHORT).show() }
                             
                             for (uriString in newLocalUris) {
                                 val bytes = getBytesFromUri(uriString)
                                 if (bytes != null) {
                                     val file = ParseFile("photo_${System.currentTimeMillis()}.jpg", bytes)
                                     file.save() // Upload síncrono
                                     updatedParseFiles.add(file)
                                 }
                             }
                        }
                        
                        parseObject.put("photos", updatedParseFiles)
                        parseObject.save()
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(getApplication(), "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            refreshMaintenanceList()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Serviço original não encontrado.", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show() }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun getBytesFromUri(uriString: String): ByteArray? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                outputStream.toByteArray()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Upload", "Erro ao converter imagem: $uriString", e)
            null
        }
    }
    
    private fun processPhotos(photoUris: String): ArrayList<HashMap<String, String>> {
        val photosAsBase64 = ArrayList<HashMap<String, String>>()
        val uriList = photoUris.split(",").filter { it.isNotBlank() }
        for (uriString in uriList) {
            try {
                if (uriString.startsWith("http")) continue
                val byteArr = getBytesFromUri(uriString)
                if (byteArr != null) {
                    val base64String = Base64.encodeToString(byteArr, Base64.NO_WRAP)
                    photosAsBase64.add(hashMapOf(
                        "name" to "photo_${System.currentTimeMillis()}.jpg",
                        "base64" to base64String
                    ))
                }
            } catch (e: Exception) {
                Log.e("Upload", "Erro ao processar imagem: $uriString", e)
            }
        }
        return photosAsBase64
    }

    fun deleteMaintenanceItem(item: MaintenanceItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.whereEqualTo("machine", item.machine)
                query.whereEqualTo("description", item.description)
                val result = query.find()
                if (result.isNotEmpty()) {
                    val target = result.minByOrNull { Math.abs(it.createdAt.time - item.date) }
                    target?.delete()
                    refreshMaintenanceList()
                    withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Excluído.", Toast.LENGTH_SHORT).show() }
                } else {
                     withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Item não encontrado.", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }
    
    fun cleanOldCloudData() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Iniciando limpeza...", Toast.LENGTH_SHORT).show() }
            try {
                ParseCloud.callFunction<String>("deleteReportsOlderThan8Days", emptyMap<String, Any>())                
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Limpeza concluída!", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro na limpeza: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }
    
    // --- ESTOQUE (BACK4APP) ---
    private suspend fun searchStockInBack4App(query: String) {
        if (query.length < 2) {
             _cloudStockItems.value = emptyList()
             return
        }
        
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val queryCode = ParseQuery.getQuery<ParseObject>("Peca")
                queryCode.whereStartsWith("codigo", query)

                val queryDesc = ParseQuery.getQuery<ParseObject>("Peca")
                queryDesc.whereContains("descricao", query.uppercase())

                val mainQuery = ParseQuery.or(listOf(queryCode, queryDesc))
                mainQuery.limit = 50 
                mainQuery.orderByAscending("descricao") 
                val results = mainQuery.find()
                
                val mappedItems = results.map { parseObj ->
                    StockItem(
                        id = 0,
                        code = parseObj.getString("codigo") ?: "",
                        description = parseObj.getString("descricao") ?: "",
                        quantity = parseObj.getNumber("saldo")?.toInt() ?: 0,
                        address = parseObj.getString("endereco") ?: ""
                    )
                }
                _cloudStockItems.value = mappedItems
            } catch (e: Exception) {
                Log.e("Back4App", "Erro na busca: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) { _stockSearchQuery.value = query }
    fun toggleSearchMode() { _isSearchByCode.value = !_isSearchByCode.value }
    fun importStockData() {}

    fun importCsvDirectToCloud(uri: Uri) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Enviando para Back4App...", Toast.LENGTH_SHORT).show() }
                CsvImporter.processCsvInBatches(getApplication(), uri, batchSize = 200) { batchItems, totalProcessed ->
                    val params = HashMap<String, Any>()
                    val pecasList = ArrayList<HashMap<String, Any>>()
                    for (item in batchItems) {
                        val map = HashMap<String, Any>()
                        map["codigo"] = item.code
                        if (item.description.isNotBlank()) map["descricao"] = item.description
                        if (item.address.isNotBlank()) map["endereco"] = item.address
                        pecasList.add(map)
                    }
                    params["pecas"] = pecasList
                    try {
                        ParseCloud.callFunction<HashMap<String, Any>>("importPecas", params)
                        withContext(Dispatchers.Main) {
                            val count = totalProcessed + batchItems.size
                            if (count % 1000 == 0) Toast.makeText(getApplication(), "Processados: $count...", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        throw e 
                    }
                }
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Catálogo atualizado!", Toast.LENGTH_LONG).show() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importExcelData(uri: Uri) { importCsvDirectToCloud(uri) }
    fun syncStockToCloud() {}

    // --- Machine Configuration Actions ---
    
    fun syncMachineConfiguration() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                machineConfigRepository.syncFromCloud()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao sincronizar configs: ${e.message}")
                withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Erro ao sincronizar: ${e.message}", Toast.LENGTH_SHORT).show() 
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Wrapped actions with error handling
    fun addProductionLine(name: String) { 
        viewModelScope.launch { 
            _isLoading.value = true
            try {
                machineConfigRepository.insertProductionLine(name)
                withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Linha salva na nuvem!", Toast.LENGTH_SHORT).show() 
                }
            } catch (e: Exception) {
                 withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Erro ao salvar linha: ${e.message}", Toast.LENGTH_LONG).show() 
                }
            } finally {
                _isLoading.value = false
            }
        } 
    }

    fun deleteProductionLine(productionLine: ProductionLine) { 
        viewModelScope.launch { 
            _isLoading.value = true
            try {
                machineConfigRepository.deleteProductionLine(productionLine)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Erro ao excluir linha: ${e.message}", Toast.LENGTH_LONG).show() 
                }
            } finally {
                _isLoading.value = false
            }
        } 
    }

    fun addMachine(name: String, lineId: Long?) { 
        viewModelScope.launch { 
             _isLoading.value = true
            try {
                machineConfigRepository.insertMachine(name, lineId)
                withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Máquina salva na nuvem!", Toast.LENGTH_SHORT).show() 
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Erro ao salvar máquina: ${e.message}", Toast.LENGTH_LONG).show() 
                }
            } finally {
                _isLoading.value = false
            }
        } 
    }

    fun deleteMachine(machine: Machine) { 
        viewModelScope.launch { 
            _isLoading.value = true
            try {
                machineConfigRepository.deleteMachine(machine)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Erro ao excluir máquina: ${e.message}", Toast.LENGTH_LONG).show() 
                }
            } finally {
                _isLoading.value = false
            }
        } 
    }
    
    fun getMachinesByLine(lineId: Long): StateFlow<List<Machine>> {
         return machineConfigRepository.getMachinesByLineId(lineId)
             .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}

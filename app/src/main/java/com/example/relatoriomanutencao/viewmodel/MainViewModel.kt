package com.example.relatoriomanutencao.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.relatoriomanutencao.data.AppDatabase
import com.example.relatoriomanutencao.data.Machine
import com.example.relatoriomanutencao.data.MachineConfigurationRepository
import com.example.relatoriomanutencao.data.MaintenanceItem
import com.example.relatoriomanutencao.data.ProductionLine
import com.example.relatoriomanutencao.data.StockItem
import com.example.relatoriomanutencao.utils.CloudinaryHelper
import com.example.relatoriomanutencao.utils.CsvImporter
import com.example.relatoriomanutencao.utils.ShiftManager
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
import java.util.Date
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
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
    
    // Lista de Locais de Estoque (Armazém 05, Caixa 1, etc)
    private val _stockLocations = MutableStateFlow<List<String>>(emptyList())
    val stockLocations: StateFlow<List<String>> = _stockLocations.asStateFlow()

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
        fetchStockLocations() // Busca os locais ao iniciar
        
        viewModelScope.launch {
            _stockSearchQuery
                .debounce(800L)
                .collect { query ->
                    searchStockInBack4App(query)
                }
        }
    }

    // --- Admin Users (for admin screen) ---
    private val _adminUsers = MutableStateFlow<List<com.parse.ParseUser>>(emptyList())
    val adminUsers: StateFlow<List<com.parse.ParseUser>> = _adminUsers.asStateFlow()

    fun fetchUsersForAdmin() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery(com.parse.ParseUser::class.java)
                query.orderByAscending("username")
                val results = query.find()
                _adminUsers.value = results
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao buscar usuários (admin): ${e.message}")
            }
        }
    }

    fun approveUser(objectId: String, approve: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery(com.parse.ParseUser::class.java)
                val user = query.get(objectId)
                user.put("isApproved", approve)
                user.save()
                // refresh list
                fetchUsersForAdmin()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro aprovar usuário: ${e.message}")
            }
        }
    }

    fun toggleAdmin(objectId: String, makeAdmin: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery(com.parse.ParseUser::class.java)
                val user = query.get(objectId)
                user.put("isAdmin", makeAdmin)
                user.save()
                fetchUsersForAdmin()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro toggle admin: ${e.message}")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                com.parse.ParseUser.requestPasswordReset(email)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao solicitar reset de senha: ${e.message}")
            }
        }
    }
    
    // --- MANUTENÇÃO (BACK4APP + CLOUDINARY) ---
    
    fun refreshMaintenanceList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.orderByDescending("createdAt")
                query.limit = 100
                
                val results = query.find()
                
                val items = results.map { obj ->
                    // 1. Fotos Antigas (Arquivos do Back4App)
                    val legacyPhotos = obj.getList<ParseFile>("photos")
                    val legacyUrls = legacyPhotos?.mapNotNull { it.url } ?: emptyList()
                    
                    // 2. Novas Fotos (Links do Cloudinary)
                    val externalPhotos = obj.getList<String>("external_photos") ?: emptyList()
                    
                    // Junta tudo
                    val allPhotoUrls = (legacyUrls + externalPhotos).joinToString(",")

                    val workDateObj = obj.getDate("workDate")
                    val workDateMillis = workDateObj?.time

                    val mi = MaintenanceItem(
                        id = 0,
                        machine = obj.getString("machine") ?: "",
                        serviceType = obj.getString("type") ?: "",
                        description = obj.getString("description") ?: "",
                        date = workDateMillis ?: obj.createdAt.time,
                        photoUris = allPhotoUrls
                    )
                    // Preenche dados auxiliares (não persistidos localmente)
                    mi.shiftId = obj.getNumber("shiftId")?.toInt()
                    mi.workDateMillisFromServer = workDateMillis
                    mi
                }
                _maintenanceItems.value = items
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao buscar serviços: ${e.message}")
            }
        }
    }

    fun addMaintenanceItem(
        machine: String,
        serviceType: String,
        description: String,
        photoUris: String,
        overrideShiftId: Int? = null,
        overrideWorkDateMillis: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val uploadedUrls = uploadPhotosToCloudinary(photoUris)
                
                val serviceObject = ParseObject("Servico")
                serviceObject.put("machine", machine)
                serviceObject.put("type", serviceType)
                serviceObject.put("description", description)
                // Preencher shiftId e workDate usando ShiftManager (usa fuso do dispositivo),
                // permitindo override a partir da UI
                try {
                    if (overrideShiftId != null && overrideWorkDateMillis != null) {
                        serviceObject.put("shiftId", overrideShiftId)
                        serviceObject.put("workDate", Date(overrideWorkDateMillis))
                    } else {
                        val shiftInfo = ShiftManager.getCurrentShiftInfo()
                        serviceObject.put("shiftId", shiftInfo.shiftId)
                        serviceObject.put("workDate", shiftInfo.workDate)
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Não foi possível calcular shiftInfo: ${e.message}")
                }
                
                if (uploadedUrls.isNotEmpty()) {
                    serviceObject.put("external_photos", uploadedUrls)
                }
                
                serviceObject.save()

                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Serviço salvo!", Toast.LENGTH_SHORT).show()
                    refreshMaintenanceList() 
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { 
                    Log.e("AddService", "Erro", e)
                    Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_LONG).show() 
                }
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
                    val parseObject = results.minByOrNull { abs(it.createdAt.time - originalItem.date) }
                    
                    if (parseObject != null) {
                        parseObject.put("description", newDescription)
                        
                        val allUris = newPhotoUris.split(",").filter { it.isNotBlank() }
                        val existingLinks = allUris.filter { it.startsWith("http") }
                        val newLocalUris = allUris.filter { !it.startsWith("http") }
                        
                        val newUploadedUrls = uploadPhotosToCloudinary(newLocalUris.joinToString(","))
                        val finalExternalList = existingLinks.filter { !it.contains("back4app") } + newUploadedUrls
                        
                        parseObject.put("external_photos", finalExternalList)
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
                 withContext(Dispatchers.Main) { 
                    Log.e("UpdateService", "Erro", e)
                    Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show() 
                 }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun uploadPhotosToCloudinary(photoUris: String): List<String> {
        val urls = mutableListOf<String>()
        val uriList = photoUris.split(",").filter { it.isNotBlank() }
        
        if (uriList.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "Enviando fotos para Cloudinary...", Toast.LENGTH_SHORT).show()
            }
        }
        
        for (uriString in uriList) {
            if (uriString.startsWith("http")) {
                urls.add(uriString) // Já é URL
                continue
            }
            
            try {
                val uri = uriString.toUri()
                val url = CloudinaryHelper.uploadImage(getApplication(), uri)
                urls.add(url)
            } catch (e: Exception) {
                Log.e("Cloudinary", "Falha ao enviar imagem $uriString", e)
            }
        }
        return urls
    }

    fun deleteMaintenanceItem(item: MaintenanceItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.whereEqualTo("machine", item.machine)
                query.whereEqualTo("description", item.description)
                val result = query.find()
                if (result.isNotEmpty()) {
                    val target = result.minByOrNull { abs(it.createdAt.time - item.date) }
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
    
    fun cleanOldImagesOnly() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Limpando fotos antigas...", Toast.LENGTH_SHORT).show() }
            try {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -30) // Ex: 30 dias atrás
                val cutOffDate = calendar.time
                
                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.whereLessThan("createdAt", cutOffDate)
                query.limit = 1000
                val oldServices = query.find()
                
                var count = 0
                for (service in oldServices) {
                    val photos = service.getList<String>("external_photos")
                    val legacyPhotos = service.get("photos")
                    
                    if ((photos != null && photos.isNotEmpty()) || legacyPhotos != null) {
                        service.remove("external_photos")
                        service.remove("photos") 
                        service.save()
                        count++
                    }
                }
                
                refreshMaintenanceList()
                withContext(Dispatchers.Main) { 
                    Toast.makeText(getApplication(), "Fotos removidas de $count relatórios antigos.\nTexto preservado.", Toast.LENGTH_LONG).show() 
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro na limpeza: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    // --- GESTÃO DE ESTOQUE (MOVIMENTAÇÃO) ---

    // 1. Entrada de Estoque (Manual)
    fun addStockEntry(code: String, quantity: Int, location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val query = ParseQuery.getQuery<ParseObject>("Peca")
                query.whereEqualTo("codigo", code)
                val result = query.find()

                if (result.isNotEmpty()) {
                    val item = result[0]
                    val currentQty = item.getNumber("saldo")?.toInt() ?: 0
                    
                    item.put("saldo", currentQty + quantity)
                    if (location.isNotBlank()) {
                        item.put("endereco", location)
                    }
                    item.save()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Entrada realizada! Novo saldo: ${currentQty + quantity}", Toast.LENGTH_SHORT).show()
                        // Recarrega a busca se estiver na tela
                        if (_stockSearchQuery.value.isNotBlank()) searchStockInBack4App(_stockSearchQuery.value)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Erro: Código '$code' não encontrado no catálogo.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show() }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. Consumo de Estoque (Baixa)
    fun consumeStock(itemCode: String, quantityToConsume: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val query = ParseQuery.getQuery<ParseObject>("Peca")
                query.whereEqualTo("codigo", itemCode)
                val result = query.find()

                if (result.isNotEmpty()) {
                    val item = result[0]
                    val currentQty = item.getNumber("saldo")?.toInt() ?: 0

                    if (currentQty >= quantityToConsume) {
                        item.put("saldo", currentQty - quantityToConsume)
                        item.save()
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(getApplication(), "Baixa realizada! Restam: ${currentQty - quantityToConsume}", Toast.LENGTH_SHORT).show()
                            if (_stockSearchQuery.value.isNotBlank()) searchStockInBack4App(_stockSearchQuery.value)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(getApplication(), "ERRO: Saldo insuficiente! Disponível: $currentQty", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                     withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Item não encontrado.", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show() }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 3. Gerenciamento de Locais
    private fun fetchStockLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("StockLocation")
                query.orderByAscending("name")
                val results = query.find()
                val names = results.map { it.getString("name") ?: "" }.filter { it.isNotBlank() }
                _stockLocations.value = names
            } catch (e: Exception) {
                Log.e("Stock", "Erro ao buscar locais: ${e.message}")
            }
        }
    }

    fun addStockLocation(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Evita duplicados
                val query = ParseQuery.getQuery<ParseObject>("StockLocation")
                query.whereEqualTo("name", name)
                if (query.count() == 0) {
                    val loc = ParseObject("StockLocation")
                    loc.put("name", name)
                    loc.save()
                    fetchStockLocations()
                    withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Local salvo!", Toast.LENGTH_SHORT).show() }
                } else {
                    withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Local já existe.", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    fun deleteStockLocation(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("StockLocation")
                query.whereEqualTo("name", name)
                val results = query.find()
                results.forEach { it.delete() }
                fetchStockLocations()
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Local removido.", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }
    
    // --- ESTOQUE (Busca) ---
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
    
    // --- Machine Configuration Actions ---
    
    fun syncMachineConfiguration() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                machineConfigRepository.syncFromCloud()
                fetchStockLocations() // Sync locations too
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
}

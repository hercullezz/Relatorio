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
import java.time.Instant
import java.util.TimeZone
import kotlin.math.abs

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val machineConfigRepository = MachineConfigurationRepository(
        database.productionLineDao(),
        database.machineDao()
    )
    
    private val _maintenanceItems = MutableStateFlow<List<MaintenanceItem>>(emptyList())
    val maintenanceItems: StateFlow<List<MaintenanceItem>> = _maintenanceItems.asStateFlow()

    private val _stockSearchQuery = MutableStateFlow("")
    val stockSearchQuery: StateFlow<String> = _stockSearchQuery.asStateFlow()
    
    private val _cloudStockItems = MutableStateFlow<List<StockItem>>(emptyList())
    val stockItems: StateFlow<List<StockItem>> = _cloudStockItems.asStateFlow()
    
    private val _stockLocations = MutableStateFlow<List<String>>(emptyList())
    val stockLocations: StateFlow<List<String>> = _stockLocations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _itemToEdit = MutableStateFlow<MaintenanceItem?>(null)
    val itemToEdit: StateFlow<MaintenanceItem?> = _itemToEdit.asStateFlow()

    private val _usePreviousShift = MutableStateFlow(false)
    val usePreviousShift: StateFlow<Boolean> = _usePreviousShift.asStateFlow()

    fun setUsePreviousShift(value: Boolean) {
        _usePreviousShift.value = value
    }

    fun setItemToEdit(item: MaintenanceItem?) { _itemToEdit.value = item }

    val allProductionLines: StateFlow<List<ProductionLine>> = machineConfigRepository.allProductionLines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMachines: StateFlow<List<Machine>> = machineConfigRepository.allMachines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val machinesWithoutLine: StateFlow<List<Machine>> = machineConfigRepository.machinesWithoutLine
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshMaintenanceList()
        syncMachineConfiguration()
        fetchStockLocations()
        viewModelScope.launch {
            _stockSearchQuery.debounce(800L).collect { query -> searchStockInBack4App(query) }
        }
    }

    // --- Admin Actions ---
    private val _adminUsers = MutableStateFlow<List<com.parse.ParseUser>>(emptyList())
    val adminUsers: StateFlow<List<com.parse.ParseUser>> = _adminUsers.asStateFlow()

    fun fetchUsersForAdmin() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery(com.parse.ParseUser::class.java).orderByAscending("username")
                _adminUsers.value = query.find()
            } catch (e: Exception) { Log.e("MainViewModel", "Erro admin: ${e.message}") }
        }
    }

    fun approveUser(objectId: String, approve: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ParseCloud.callFunction<Any>("adminApproveUser", mapOf("objectId" to objectId, "isApproved" to approve))
                fetchUsersForAdmin()
            } catch (e: Exception) { Log.e("MainViewModel", "Erro aprovar: ${e.message}") }
        }
    }

    fun toggleAdmin(objectId: String, makeAdmin: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ParseCloud.callFunction<Any>("adminToggleAdmin", mapOf("objectId" to objectId, "isAdmin" to makeAdmin))
                fetchUsersForAdmin()
            } catch (e: Exception) { Log.e("MainViewModel", "Erro admin toggle: ${e.message}") }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try { com.parse.ParseUser.requestPasswordReset(email) } catch (e: Exception) { }
        }
    }

    fun deleteUser(objectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ParseCloud.callFunction<Any>("adminDeleteUser", mapOf("objectId" to objectId))
                fetchUsersForAdmin()
            } catch (e: Exception) { Log.e("MainViewModel", "Erro deletar usuário: ${e.message}") }
        }
    }
    
    // --- MANUTENÇÃO ---
    fun refreshMaintenanceList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Busca itens locais que têm QUALQUER pendência (novo, editado ou deletado)
                val pendingItems = database.maintenanceDao().getPendingSyncItemsSync()
                val pendingObjectIds = pendingItems.mapNotNull { it.objectId }.toSet()

                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.orderByDescending("createdAt")
                query.limit = 150
                
                val results = query.find()
                val cloudItems = results.mapNotNull { obj ->
                    // Se o item tem uma pendência local (ex: editado offline), ignoramos a versão da nuvem
                    if (pendingObjectIds.contains(obj.objectId)) return@mapNotNull null

                    val allUrls = mutableListOf<String>()
                    obj.getList<ParseFile>("photos")?.mapNotNull { it.url }?.let { allUrls.addAll(it) }
                    obj.getList<String>("external_photos")?.let { allUrls.addAll(it) }
                    
                    val realDate = obj.getDate("timestampLocal") ?: obj.createdAt ?: Date()
                    val workDateObj = obj.getDate("workDate")

                    MaintenanceItem(
                        id = 0, // Item vindo da nuvem temporariamente
                        machine = obj.getString("machine") ?: "",
                        serviceType = obj.getString("type") ?: "",
                        description = obj.getString("description") ?: "",
                        date = realDate.time,
                        photoUris = allUrls.joinToString(","),
                        isSynced = true,
                        shiftId = obj.getNumber("shiftId")?.toInt(),
                        workDateMillisFromServer = workDateObj?.time,
                        objectId = obj.objectId
                    )
                }
                
                // Itens pendentes primeiro, depois itens da nuvem
                _maintenanceItems.value = pendingItems + cloudItems
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao buscar serviços na nuvem: ${e.message}")
                // Fallback: mostra tudo o que tiver no banco local + o que já tínhamos em memória
                val pendingItems = database.maintenanceDao().getPendingSyncItemsSync()
                val currentSynced = _maintenanceItems.value.filter { it.isSynced && !it.isPendingUpdate && !it.isPendingDeletion }
                _maintenanceItems.value = (pendingItems + currentSynced).distinctBy { it.objectId ?: it.date }
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
            // No UI bloqueamos apenas o tempo de salvar no banco local (milisegundos)
            _isLoading.value = true
            try {
                val shiftIdToSave = overrideShiftId ?: ShiftManager.getCurrentShiftInfo().shiftId
                val workDateMillisToSave = overrideWorkDateMillis ?: ShiftManager.getCurrentShiftInfo().workDate.time
                val nowMillis = Date().time

                val localItem = MaintenanceItem(
                    machine = machine,
                    serviceType = serviceType,
                    description = description,
                    photoUris = photoUris,
                    date = nowMillis,
                    isSynced = false, // Pendente de adição
                    shiftId = shiftIdToSave,
                    workDateMillisFromServer = workDateMillisToSave
                )
                val localId = database.maintenanceDao().insertMaintenanceItem(localItem)
                
                // Libera a UI IMEDIATAMENTE após o save local
                withContext(Dispatchers.Main) { 
                    _isLoading.value = false 
                    refreshMaintenanceList()
                }

                // --- Tentar Sincronismo em Background ---
                try {
                    val uploadedUrls = uploadPhotosToCloudinary(photoUris)
                    val serviceObject = ParseObject("Servico")
                    serviceObject.put("machine", machine)
                    serviceObject.put("type", serviceType)
                    serviceObject.put("description", description)
                    serviceObject.put("timestampLocal", Date(nowMillis))
                    serviceObject.put("shiftId", shiftIdToSave)
                    serviceObject.put("workDate", Date(workDateMillisToSave))
                    
                    if (uploadedUrls.isNotEmpty()) serviceObject.put("external_photos", uploadedUrls)
                    serviceObject.save()

                    // Sucesso: Marca como Sincronizado e armazena o objectId
                    database.maintenanceDao().markAsSynced(localId, serviceObject.objectId)
                    withContext(Dispatchers.Main) { refreshMaintenanceList() }
                } catch (e: Exception) {
                    Log.d("MainViewModel", "Adicionado localmente. Sync pendente: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao salvar local: ${e.message}")
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }
    
    fun updateMaintenanceItem(
        originalItem: MaintenanceItem,
        newDescription: String,
        newPhotoUris: String,
        newShiftId: Int? = null,
        newWorkDateMillis: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                // 1. Atualizar Localmente Primeiro
                val updatedItem = originalItem.copy(
                    description = newDescription,
                    photoUris = newPhotoUris,
                    shiftId = newShiftId ?: originalItem.shiftId,
                    workDateMillisFromServer = newWorkDateMillis ?: originalItem.workDateMillisFromServer,
                    isPendingUpdate = originalItem.isSynced // Se já era sincronizado, agora está pendente de update
                )

                if (originalItem.id == 0L && originalItem.objectId != null) {
                    // Item vindo da nuvem que ainda não estava no banco local
                    database.maintenanceDao().insertMaintenanceItem(updatedItem)
                } else {
                    database.maintenanceDao().updateMaintenanceItem(updatedItem)
                }

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    refreshMaintenanceList()
                }

                // 2. Tentar Sincronismo
                val objId = originalItem.objectId
                if (objId != null) {
                    val query = ParseQuery.getQuery<ParseObject>("Servico")
                    val parseObject = query.get(objId)

                    parseObject.put("description", newDescription)

                    // Atualiza turno e data de trabalho se foram alterados
                    if (newShiftId != null) parseObject.put("shiftId", newShiftId)
                    if (newWorkDateMillis != null) parseObject.put("workDate", java.util.Date(newWorkDateMillis))

                    val allUris = newPhotoUris.split(",").filter { it.isNotBlank() }
                    val existingLinks = allUris.filter { it.startsWith("http") }
                    val newLocalUris = allUris.filter { !it.startsWith("http") }
                    val newUploadedUrls = uploadPhotosToCloudinary(newLocalUris.joinToString(","))

                    parseObject.put("external_photos", existingLinks.filter { !it.contains("back4app") } + newUploadedUrls)
                    parseObject.save()

                    // Sucesso: Marca como Sincronizado novamente
                    val finalId = if (originalItem.id == 0L) {
                        database.maintenanceDao().getPendingSyncItemsSync().find { it.objectId == objId }?.id ?: 0L
                    } else originalItem.id

                    if (finalId != 0L) database.maintenanceDao().markAsSynced(finalId, objId)
                    withContext(Dispatchers.Main) { refreshMaintenanceList() }
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Update salvo localmente: ${e.message}")
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }
    
    private suspend fun uploadPhotosToCloudinary(photoUris: String): List<String> {
        val urls = mutableListOf<String>()
        val uriList = photoUris.split(",").filter { it.isNotBlank() }
        for (uriString in uriList) {
            if (uriString.startsWith("http")) { urls.add(uriString); continue }
            try { urls.add(CloudinaryHelper.uploadImage(getApplication(), uriString.toUri())) } catch (e: Exception) { }
        }
        return urls
    }

    fun deleteMaintenanceItem(item: MaintenanceItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!item.isSynced) {
                    // Se nunca foi pra nuvem, deleta direto
                    database.maintenanceDao().deleteMaintenanceItem(item.id)
                    withContext(Dispatchers.Main) { refreshMaintenanceList() }
                    return@launch
                }

                // Se já estava na nuvem, marca como pendente de deleção localmente
                val pendingDelete = item.copy(isPendingDeletion = true)
                if (item.id == 0L) database.maintenanceDao().insertMaintenanceItem(pendingDelete)
                else database.maintenanceDao().updateMaintenanceItem(pendingDelete)
                
                withContext(Dispatchers.Main) { refreshMaintenanceList() }

                // Tenta deletar na nuvem
                val objId = item.objectId
                if (objId != null) {
                    ParseObject.createWithoutData("Servico", objId).delete()
                    // Sucesso: Remove do banco local definitivamente
                    val finalId = database.maintenanceDao().getPendingSyncItemsSync().find { it.objectId == objId }?.id ?: item.id
                    database.maintenanceDao().deleteMaintenanceItem(finalId)
                    withContext(Dispatchers.Main) { refreshMaintenanceList() }
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Deleção marcada para depois: ${e.message}")
            }
        }
    }
    
    fun syncPendingItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val pending = database.maintenanceDao().getPendingSyncItemsSync()
                for (item in pending) {
                    try {
                        when {
                            item.isPendingDeletion -> {
                                val objId = item.objectId
                                if (objId != null) {
                                    ParseObject.createWithoutData("Servico", objId).delete()
                                }
                                database.maintenanceDao().deleteMaintenanceItem(item.id)
                            }
                            
                            item.isPendingUpdate -> {
                                val objId = item.objectId
                                if (objId != null) {
                                    val parseObject = ParseObject.createWithoutData("Servico", objId)
                                    parseObject.put("description", item.description)
                                    
                                    val allUris = item.photoUris.split(",").filter { it.isNotBlank() }
                                    val existingLinks = allUris.filter { it.startsWith("http") }
                                    val newLocalUris = allUris.filter { !it.startsWith("http") }
                                    val newUploadedUrls = uploadPhotosToCloudinary(newLocalUris.joinToString(","))
                                    
                                    parseObject.put("external_photos", existingLinks.filter { !it.contains("back4app") } + newUploadedUrls)
                                    parseObject.save()
                                    database.maintenanceDao().markAsSynced(item.id, objId)
                                }
                            }
                            
                            !item.isSynced -> {
                                // Novo Item (Add)
                                val uploadedUrls = uploadPhotosToCloudinary(item.photoUris)
                                val serviceObject = ParseObject("Servico")
                                serviceObject.put("machine", item.machine)
                                serviceObject.put("type", item.serviceType)
                                serviceObject.put("description", item.description)
                                serviceObject.put("timestampLocal", Date(item.date))
                                item.shiftId?.let { serviceObject.put("shiftId", it) }
                                item.workDateMillisFromServer?.let { serviceObject.put("workDate", Date(it)) }
                                
                                if (uploadedUrls.isNotEmpty()) serviceObject.put("external_photos", uploadedUrls)
                                serviceObject.save()
                                
                                database.maintenanceDao().markAsSynced(item.id, serviceObject.objectId)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Erro ao sincronizar item ${item.id}: ${e.message}")
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Sincronização Finalizada!", Toast.LENGTH_SHORT).show()
                    refreshMaintenanceList()
                }
            } catch (e: Exception) { 
            } finally { _isLoading.value = false }
        }
    }
    
    fun cleanOldImagesOnly() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
                val query = ParseQuery.getQuery<ParseObject>("Servico")
                query.whereLessThan("createdAt", calendar.time).limit = 1000
                val oldServices = query.find()
                for (service in oldServices) {
                    service.remove("external_photos")
                    service.remove("photos") 
                    service.save()
                }
                refreshMaintenanceList()
            } catch (e: Exception) { }
        }
    }

    // --- ESTOQUE ---
    fun addStockEntry(code: String, quantity: Int, location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val query = ParseQuery.getQuery<ParseObject>("Peca").whereEqualTo("codigo", code)
                val result = query.find()
                if (result.isNotEmpty()) {
                    val item = result[0]
                    val currentQty = item.getNumber("saldo")?.toInt() ?: 0
                    item.put("saldo", currentQty + quantity)
                    if (location.isNotBlank()) item.put("endereco", location)
                    item.save()
                    withContext(Dispatchers.Main) { if (_stockSearchQuery.value.isNotBlank()) searchStockInBack4App(_stockSearchQuery.value) }
                }
            } catch (e: Exception) { } finally { _isLoading.value = false }
        }
    }

    fun consumeStock(itemCode: String, quantityToConsume: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val query = ParseQuery.getQuery<ParseObject>("Peca").whereEqualTo("codigo", itemCode)
                val result = query.find()
                if (result.isNotEmpty()) {
                    val item = result[0]
                    val currentQty = item.getNumber("saldo")?.toInt() ?: 0
                    if (currentQty >= quantityToConsume) {
                        item.put("saldo", currentQty - quantityToConsume)
                        item.save()
                        withContext(Dispatchers.Main) { if (_stockSearchQuery.value.isNotBlank()) searchStockInBack4App(_stockSearchQuery.value) }
                    }
                }
            } catch (e: Exception) { } finally { _isLoading.value = false }
        }
    }

    private fun fetchStockLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("StockLocation").orderByAscending("name")
                _stockLocations.value = query.find().map { it.getString("name") ?: "" }.filter { it.isNotBlank() }
            } catch (e: Exception) { }
        }
    }

    fun addStockLocation(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loc = ParseObject("StockLocation")
                loc.put("name", name)
                loc.save()
                fetchStockLocations()
            } catch (e: Exception) { }
        }
    }

    fun updateStockLocation(oldName: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("StockLocation").whereEqualTo("name", oldName)
                query.find().forEach { 
                    it.put("name", newName)
                    it.save() 
                }
                fetchStockLocations()
            } catch (e: Exception) { }
        }
    }

    fun deleteStockLocation(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>("StockLocation").whereEqualTo("name", name)
                query.find().forEach { it.delete() }
                fetchStockLocations()
            } catch (e: Exception) { }
        }
    }
    
    private suspend fun searchStockInBack4App(query: String) {
        if (query.length < 2) { _cloudStockItems.value = emptyList(); return }
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val queryCode = ParseQuery.getQuery<ParseObject>("Peca").whereStartsWith("codigo", query)
                val queryDesc = ParseQuery.getQuery<ParseObject>("Peca").whereContains("descricao", query.uppercase())
                val results = ParseQuery.or(listOf(queryCode, queryDesc)).setLimit(50).find()
                _cloudStockItems.value = results.map { parseObj ->
                    StockItem(0, parseObj.getString("codigo") ?: "", parseObj.getString("descricao") ?: "", parseObj.getString("endereco") ?: "", parseObj.getNumber("saldo")?.toInt() ?: 0)
                }
            } catch (e: Exception) { } finally { _isLoading.value = false }
        }
    }

    fun onSearchQueryChanged(query: String) { _stockSearchQuery.value = query }

    fun importCsvDirectToCloud(uri: Uri) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                CsvImporter.processCsvInBatches(getApplication(), uri, 200) { batchItems, _ ->
                    val pecasList = batchItems.map { item ->
                        val map = HashMap<String, Any>()
                        map["codigo"] = item.code
                        if (item.description.isNotBlank()) map["descricao"] = item.description
                        if (item.address.isNotBlank()) map["endereco"] = item.address
                        map
                    }
                    ParseCloud.callFunction<HashMap<String, Any>>("importPecas", mapOf("pecas" to pecasList))
                }
                withContext(Dispatchers.Main) { Toast.makeText(getApplication(), "Concluído!", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) { } finally { _isLoading.value = false }
        }
    }

    fun importExcelData(uri: Uri) { importCsvDirectToCloud(uri) }
    
    fun syncMachineConfiguration() {
        viewModelScope.launch {
            _isLoading.value = true
            try { machineConfigRepository.syncFromCloud(); fetchStockLocations() } catch (e: Exception) { } finally { _isLoading.value = false }
        }
    }

    fun addProductionLine(name: String) { 
        viewModelScope.launch { 
            _isLoading.value = true
            try { machineConfigRepository.insertProductionLine(name) } catch (e: Exception) { } finally { _isLoading.value = false }
        } 
    }

    fun updateProductionLine(oldName: String, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { machineConfigRepository.updateProductionLine(oldName, newName) } catch (e: Exception) { } finally { _isLoading.value = false }
        }
    }

    fun deleteProductionLine(productionLine: ProductionLine) { 
        viewModelScope.launch { 
            _isLoading.value = true
            try { machineConfigRepository.deleteProductionLine(productionLine) } catch (e: Exception) { } finally { _isLoading.value = false }
        } 
    }

    fun addMachine(name: String, lineId: Long?) { 
        viewModelScope.launch { 
             _isLoading.value = true
            try { machineConfigRepository.insertMachine(name, lineId) } catch (e: Exception) { } finally { _isLoading.value = false }
        } 
    }

    fun updateMachine(oldName: String, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { machineConfigRepository.updateMachine(oldName, newName) } catch (e: Exception) { } finally { _isLoading.value = false }
        }
    }

    fun deleteMachine(machine: Machine) { 
        viewModelScope.launch { 
            _isLoading.value = true
            try { machineConfigRepository.deleteMachine(machine) } catch (e: Exception) { } finally { _isLoading.value = false }
        } 
    }
    
    fun clearSyncedLocalData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                database.maintenanceDao().clearSyncedMaintenanceItems()
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Memória Local Otimizada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erro ao limpar cache local", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun loadImagesForEditing(photoUris: String): List<Any> {
        val imageList = mutableListOf<Any>()
        if (photoUris.isBlank()) return imageList
        val uriList = photoUris.split(",").filter { it.isNotBlank() }
        for (path in uriList) {
            val image = withContext(Dispatchers.IO) {
                try {
                    if (path.startsWith("http")) {
                        val result: Map<String, Any> = ParseCloud.callFunction("getPhotoAsBase64", mapOf("photoUrl" to path))
                        val base64String = result["base64"] as? String
                        if (base64String != null) {
                            val bytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else null
                    } else path.toUri()
                } catch (e: Exception) { null }
            }
            image?.let { imageList.add(it) }
        }
        return imageList
    }
}

package com.example.relatoriomanutencao.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.relatoriomanutencao.data.Line
import com.example.relatoriomanutencao.data.MachineConfigurationRepository
import com.example.relatoriomanutencao.data.Report
import com.example.relatoriomanutencao.data.ReportItem
import com.example.relatoriomanutencao.data.ReportRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID // Adicionado import para UUID

class ReportViewModel : ViewModel() {

    private val reportRepository = ReportRepository()
    private val machineConfigRepository = MachineConfigurationRepository() // Novo repositório
    private val auth = Firebase.auth
    
    private val TAG = "ReportViewModel"

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    private val _currentReport = MutableStateFlow<Report?>(null)
    val currentReport: StateFlow<Report?> = _currentReport

    private val _lines = MutableStateFlow<List<Line>>(emptyList()) // Novo StateFlow para linhas
    val lines: StateFlow<List<Line>> = _lines.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentReportJob: Job? = null
    
    init {
        fetchReports()
        fetchLines()
    }

    private fun fetchReports() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                reportRepository.getReports().collect { reportList ->
                    _reports.value = reportList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Ignora CancellationException pois é esperada
                if (e !is CancellationException) {
                    Log.e(TAG, "Erro ao buscar relatórios", e)
                    _error.value = "Falha ao conectar ao banco de dados: ${e.message}"
                }
                _isLoading.value = false
            }
        }
    }

    fun fetchReport(reportId: String) {
        // Cancela o job anterior para evitar múltiplos listeners
        currentReportJob?.cancel()
        
        currentReportJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                reportRepository.getReport(reportId).collect { report ->
                    Log.d(TAG, "Relatório atualizado: ${report?.title}, itens: ${report?.items?.size ?: 0}")
                    _currentReport.value = report
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Ignora CancellationException pois é esperada quando cancelamos o job anterior
                if (e !is CancellationException) {
                    Log.e(TAG, "Erro ao buscar relatório", e)
                    _error.value = "Erro ao buscar relatório: ${e.message}"
                }
                _isLoading.value = false
            }
        }
    }

    private fun fetchLines() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                machineConfigRepository.getLines().collect { lineList ->
                    _lines.value = lineList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Ignora CancellationException pois é esperada
                if (e !is CancellationException) {
                    Log.e(TAG, "Erro ao buscar linhas para ReportViewModel", e)
                    _error.value = "Erro ao carregar configurações de máquinas: ${e.message}"
                }
                _isLoading.value = false
            }
        }
    }

    suspend fun createOrOpenDailyReport(): Report? {
        return try {
            _isLoading.value = true
            _error.value = null
            val user = auth.currentUser
            val userEmail = user?.email ?: "Usuário Local"
            val report = reportRepository.createOrOpenReportForToday() // Usando reportRepository
            _isLoading.value = false
            report
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar ou abrir relatório diário", e)
            _error.value = "Erro ao criar ou abrir relatório diário: ${e.message}"
            _isLoading.value = false
            null
        }
    }

    fun addReportItem(reportId: String, item: ReportItem) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val userEmail = user?.email ?: "Usuário Local"
                
                // Cria uma cópia do item com o autor e ID único
                val itemWithAuthorAndTimestamp = item.copy(
                    author = userEmail,
                    id = UUID.randomUUID().toString()
                )
                
                Log.d(TAG, "Iniciando adição de item: $itemWithAuthorAndTimestamp")
                
                // Atualização otimista do _currentReport ANTES de salvar
                val updatedReport = _currentReport.value?.copy(
                    items = _currentReport.value?.items.orEmpty() + itemWithAuthorAndTimestamp
                )
                _currentReport.value = updatedReport
                Log.d(TAG, "Atualização otimista feita, total de itens: ${updatedReport?.items?.size}")
                
                // Salva no Firebase
                reportRepository.addReportItem(reportId, itemWithAuthorAndTimestamp)
                Log.d(TAG, "Item salvo no Firebase com sucesso")
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adicionar item", e)
                _error.value = "Erro ao adicionar item: ${e.message}"
                // Reverte a atualização otimista em caso de erro
                fetchReport(reportId)
            }
        }
    }

    fun updateReportItem(reportId: String, oldItem: ReportItem, newItem: ReportItem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val user = auth.currentUser
                val userEmail = user?.email ?: "Usuário Local"
                // Certifica-se de que o newItem tem o mesmo ID que o oldItem para manter a referência
                val newItemWithAuthor = newItem.copy(author = userEmail, id = oldItem.id)
                reportRepository.updateReportItem(reportId, oldItem, newItemWithAuthor)

                // Atualização otimista
                _currentReport.value = _currentReport.value?.copy(
                    items = _currentReport.value?.items.orEmpty().map { 
                        if (it.id == oldItem.id) newItemWithAuthor else it 
                    }
                )
                fetchReport(reportId) // Para garantir a consistência eventual
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar item do relatório", e)
                _error.value = "Erro ao atualizar item do relatório: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteReportItem(reportId: String, itemToDelete: ReportItem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                reportRepository.deleteReportItem(reportId, itemToDelete)

                // Atualização otimista
                _currentReport.value = _currentReport.value?.copy(
                    items = _currentReport.value?.items.orEmpty().filter { 
                        it.id != itemToDelete.id 
                    }
                )
                fetchReport(reportId) // Para garantir a consistência eventual
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao excluir item do relatório", e)
                _error.value = "Erro ao excluir item do relatório: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                reportRepository.deleteReport(reportId)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao excluir relatório", e)
                _error.value = "Erro ao excluir relatório: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

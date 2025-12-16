package com.example.relatoriomanutencao.viewmodel
<<<<<<< HEAD
// Arquivo limpo para reiniciar o projeto.
=======

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.relatoriomanutencao.data.Line
import com.example.relatoriomanutencao.data.MachineConfigurationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MachineConfigurationViewModel : ViewModel() {

    private val repository = MachineConfigurationRepository()
    private val TAG = "MachineConfigViewModel"

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines: StateFlow<List<Line>> = _lines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchLines()
    }

    private fun fetchLines() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.getLines().collect { lineList ->
                    _lines.value = lineList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar linhas", e)
                _error.value = "Erro ao buscar linhas: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun addLine(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val newLine = Line(name = name)
                repository.addLine(newLine)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adicionar linha", e)
                _error.value = "Erro ao adicionar linha: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateLine(line: Line) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.updateLine(line)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar linha", e)
                _error.value = "Erro ao atualizar linha: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteLine(lineId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteLine(lineId)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao excluir linha", e)
                _error.value = "Erro ao excluir linha: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun addMachineToLine(lineId: String, machineName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val currentLine = _lines.value.firstOrNull { it.id == lineId }
                if (currentLine != null && !currentLine.machines.contains(machineName)) {
                    val updatedMachines = currentLine.machines + machineName
                    val updatedLine = currentLine.copy(machines = updatedMachines)
                    repository.updateLine(updatedLine)
                } else if (currentLine?.machines?.contains(machineName) == true) {
                    _error.value = "A máquina '$machineName' já existe nesta linha."
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adicionar máquina à linha", e)
                _error.value = "Erro ao adicionar máquina à linha: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun removeMachineFromLine(lineId: String, machineName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val currentLine = _lines.value.firstOrNull { it.id == lineId }
                if (currentLine != null) {
                    val updatedMachines = currentLine.machines.filter { it != machineName }
                    val updatedLine = currentLine.copy(machines = updatedMachines)
                    repository.updateLine(updatedLine)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover máquina da linha", e)
                _error.value = "Erro ao remover máquina da linha: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun editMachineInLine(lineId: String, oldMachineName: String, newMachineName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val currentLine = _lines.value.firstOrNull { it.id == lineId }
                if (currentLine != null) {
                    val updatedMachines = currentLine.machines.toMutableList()
                    val index = updatedMachines.indexOf(oldMachineName)
                    if (index != -1) {
                        updatedMachines[index] = newMachineName
                        val updatedLine = currentLine.copy(machines = updatedMachines)
                        repository.updateLine(updatedLine)
                    } else {
                        _error.value = "Máquina antiga não encontrada para edição."
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao editar máquina na linha", e)
                _error.value = "Erro ao editar máquina na linha: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19

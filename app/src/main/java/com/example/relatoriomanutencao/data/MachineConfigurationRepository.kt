package com.example.relatoriomanutencao.data

import android.util.Log
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MachineConfigurationRepository(
    private val productionLineDao: ProductionLineDao,
    private val machineDao: MachineDao
) {
    // Classes no Back4App
    private val CLASS_LINE = "ProductionLine"
    private val CLASS_MACHINE = "Machine"

    val allProductionLines: Flow<List<ProductionLine>> = productionLineDao.getAllProductionLines()
    val allMachines: Flow<List<Machine>> = machineDao.getAllMachines()
    val machinesWithoutLine: Flow<List<Machine>> = machineDao.getMachinesWithoutLine()

    fun getMachinesByLineId(lineId: Long): Flow<List<Machine>> {
        return machineDao.getMachinesByLineId(lineId)
    }

    // --- Sync Logic (Back4App) ---
    suspend fun syncFromCloud() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Sync Production Lines
                val queryLines = ParseQuery.getQuery<ParseObject>(CLASS_LINE)
                val cloudLines = queryLines.find()
                
                // Limpa banco local para evitar duplicatas e recria baseado na nuvem (Source of Truth)
                // Nota: Em apps maiores, faríamos diff, mas para configs pequenas, limpar e repopular é mais seguro contra inconsistências.
                val currentLocalLines = productionLineDao.getAllProductionLines().first()
                
                // Mapeamento para preservar IDs se possível ou apenas recriar
                // Para simplificar e resolver o bug de duplicação: Vamos confiar na nuvem.
                
                // A estratégia aqui muda: Vamos verificar um por um.
                
                // A. Adicionar/Atualizar Linhas
                for (obj in cloudLines) {
                    val name = obj.getString("name") ?: continue
                    val existingLine = currentLocalLines.find { it.name.equals(name, ignoreCase = true) }
                    
                    if (existingLine == null) {
                        productionLineDao.insertProductionLine(ProductionLine(name = name))
                    }
                }
                
                // B. Remover Linhas locais que não estão na nuvem
                val updatedLocalLinesSnapshot = productionLineDao.getAllProductionLines().first()
                val cloudLineNames = cloudLines.mapNotNull { it.getString("name")?.trim()?.lowercase() }
                
                for (localLine in updatedLocalLinesSnapshot) {
                    if (!cloudLineNames.contains(localLine.name.trim().lowercase())) {
                        productionLineDao.deleteProductionLine(localLine)
                    }
                }

                // Recarrega para ter os IDs corretos para vincular máquinas
                val finalLocalLines = productionLineDao.getAllProductionLines().first()

                // 2. Sync Machines
                val queryMachines = ParseQuery.getQuery<ParseObject>(CLASS_MACHINE)
                val cloudMachines = queryMachines.find()
                val currentLocalMachines = machineDao.getAllMachines().first()

                // A. Adicionar/Atualizar Máquinas
                for (obj in cloudMachines) {
                    val name = obj.getString("name") ?: continue
                    val lineName = obj.getString("lineName") 
                    
                    // Descobre o ID da linha local baseado no nome da linha
                    val lineId = if (lineName != null) {
                        finalLocalLines.find { it.name.equals(lineName, ignoreCase = true) }?.id
                    } else {
                        null
                    }

                    val existingMachine = currentLocalMachines.find { it.name.equals(name, ignoreCase = true) }

                    if (existingMachine == null) {
                        machineDao.insertMachine(Machine(name = name, lineId = lineId))
                    } else {
                        // Se existir, atualiza a linha se mudou
                        if (existingMachine.lineId != lineId) {
                            // Deleta e recria para garantir vínculo correto
                            machineDao.deleteMachine(existingMachine)
                            machineDao.insertMachine(Machine(name = name, lineId = lineId))
                        }
                    }
                }
                
                // B. Remover Máquinas que NÃO estão na nuvem
                val updatedLocalMachinesSnapshot = machineDao.getAllMachines().first()
                val cloudMachineNames = cloudMachines.mapNotNull { it.getString("name")?.trim()?.lowercase() }
                
                for (localMachine in updatedLocalMachinesSnapshot) {
                    if (!cloudMachineNames.contains(localMachine.name.trim().lowercase())) {
                        machineDao.deleteMachine(localMachine)
                    }
                }

                Log.d("Sync", "Configuration synced successfully from Back4App")
            } catch (e: Exception) {
                Log.e("Sync", "Error syncing configuration", e)
                throw e // Propaga erro para ViewModel saber
            }
        }
    }

    // --- Actions with Cloud Support ---

    suspend fun insertProductionLine(name: String) {
        // Envia para a nuvem PRIMEIRO
        withContext(Dispatchers.IO) {
            try {
                // Verifica se já existe na nuvem para evitar duplicação no server
                val query = ParseQuery.getQuery<ParseObject>(CLASS_LINE)
                query.whereEqualTo("name", name)
                if (query.count() == 0) {
                    val line = ParseObject(CLASS_LINE)
                    line.put("name", name)
                    line.save()
                }
            } catch (e: Exception) {
                Log.e("Back4App", "Error adding line", e)
                throw e
            }
        }
        // Depois sincroniza para atualizar o local
        syncFromCloud()
    }

    suspend fun deleteProductionLine(productionLine: ProductionLine) {
        // Delete Cloud (Find by name)
        withContext(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>(CLASS_LINE)
                query.whereEqualTo("name", productionLine.name)
                val results = query.find()
                for (obj in results) {
                    obj.delete()
                }
            } catch (e: Exception) {
                Log.e("Back4App", "Error deleting line", e)
                throw e
            }
        }
        // Sincroniza
        syncFromCloud()
    }

    suspend fun insertMachine(name: String, lineId: Long?) {
        // Get Line Name if exists
        var lineName: String? = null
        if (lineId != null) {
            val lines = productionLineDao.getAllProductionLines().first()
            lineName = lines.find { it.id == lineId }?.name
        }

        // Save Cloud PRIMEIRO
        withContext(Dispatchers.IO) {
            try {
                 // Verifica duplicidade na nuvem
                val query = ParseQuery.getQuery<ParseObject>(CLASS_MACHINE)
                query.whereEqualTo("name", name)
                if (query.count() == 0) {
                    val machine = ParseObject(CLASS_MACHINE)
                    machine.put("name", name)
                    if (lineName != null) {
                        machine.put("lineName", lineName)
                    }
                    machine.save()
                }
            } catch (e: Exception) {
                Log.e("Back4App", "Error adding machine", e)
                throw e
            }
        }
        // Sincroniza
        syncFromCloud()
    }

    suspend fun deleteMachine(machine: Machine) {
        // Delete Cloud (Find by name)
        withContext(Dispatchers.IO) {
            try {
                val query = ParseQuery.getQuery<ParseObject>(CLASS_MACHINE)
                query.whereEqualTo("name", machine.name)
                val results = query.find()
                for (obj in results) {
                    obj.delete()
                }
            } catch (e: Exception) {
                Log.e("Back4App", "Error deleting machine", e)
                throw e
            }
        }
        // Sincroniza
        syncFromCloud()
    }
}

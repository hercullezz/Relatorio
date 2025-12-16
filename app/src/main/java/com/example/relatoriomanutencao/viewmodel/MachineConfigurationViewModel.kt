package com.example.relatoriomanutencao.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.relatoriomanutencao.data.AppDatabase
import com.example.relatoriomanutencao.data.MachineConfigurationRepository

// This ViewModel is deprecated in favor of MainViewModel which now centralizes this logic
// Keeping the file but empty to avoid build errors if referenced elsewhere by mistake
class MachineConfigurationViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MachineConfigurationRepository(
        database.productionLineDao(),
        database.machineDao()
    )
}

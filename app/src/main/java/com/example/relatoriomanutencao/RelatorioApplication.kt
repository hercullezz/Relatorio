package com.example.relatoriomanutencao

import android.app.Application
import com.parse.Parse

class RelatorioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicializa o Back4App com suas chaves reais
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("ROCIeGtnrnqvg6g0f5MTgEfdEFfzqGYWpAxAbLn0")
                .clientKey("S59WReggWY0S6ufKQXOauLyCt6BXpCcz4qYY8EAy")
                .server("https://parseapi.back4app.com/")
                .build()
        )
    }
}

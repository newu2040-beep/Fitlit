package com.example

import android.app.Application
import com.example.data.local.FitlitDatabase
import com.example.data.repository.FitlitRepository
import com.example.data.repository.GeminiAiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FitlitApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { FitlitDatabase.getDatabase(this, applicationScope) }
    val aiRepository by lazy { GeminiAiRepository() }
    val repository by lazy { FitlitRepository(database.fitlitDao(), aiRepository) }
}

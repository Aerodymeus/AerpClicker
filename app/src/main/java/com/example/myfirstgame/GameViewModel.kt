package com.example.myfirstgame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    var score by mutableIntStateOf(0)
        private set

    var clickMultiplier by mutableIntStateOf(1)
        private set

    var isAutoClickerActive by mutableStateOf(false)
        private set

    var autoClickerJob: Job? = null

    // Kosten der Upgrades
    val doubleClickCost = 50
    val autoClickerCost = 200

    fun onCookieClicked() {
        score += clickMultiplier
    }

    fun buyDoubleClickUpgrade() {
        if (score >= doubleClickCost) {
            score -= doubleClickCost
            clickMultiplier *= 2
        }
    }

    fun buyAutoClickerUpgrade() {
        if (score >= autoClickerCost && !isAutoClickerActive) {
            score -= autoClickerCost
            isAutoClickerActive = true
            startAutoClicker()
        }
    }

    private fun startAutoClicker() {
        autoClickerJob?.cancel() // Sicherstellen, dass nur ein Auto-Clicker läuft
        autoClickerJob = viewModelScope.launch {
            while (isAutoClickerActive) {
                delay(10000) // 10 Sekunden warten
                onCookieClicked() // Automatisch klicken
            }
        }
    }

    // Optional: Den Auto-Clicker stoppen, wenn das ViewModel zerstört wird
    override fun onCleared() {
        super.onCleared()
        autoClickerJob?.cancel()
    }
}
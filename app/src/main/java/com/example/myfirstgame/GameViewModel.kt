package com.example.myfirstgame // Or the correct package

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
    // UI State
    var score by mutableStateOf(0)
        private set
    var clickMultiplier by mutableStateOf(1)
        private set
    var doubleClickCost by mutableStateOf(50)
        private set
    var isAutoClickerActive by mutableStateOf(false)
        private set
    var autoClickerCost by mutableStateOf(200)
        private set
    // Cooldown für Auto-Klicker (in Sekunden)
    var autoClickerCooldown by mutableIntStateOf(0)
        private set
    private val autoClickerInterval = 10 // Sekunden

    var isPassiveScoreGeneratorActive by mutableStateOf(false)
        private set
    var passiveScoreGeneratorCost by mutableStateOf(500) // Kosten für den passiven Generator
        private set
    private val passiveScoreAmount = 5 // Menge, die alle 10 Sekunden hinzugefügt wird
    // Cooldown für Cookie-Fabrik (in Sekunden)
    var passiveGeneratorCooldown by mutableIntStateOf(0)
        private set
    private val passiveGeneratorInterval = 10 // Sekunden

    private var autoClickJob: Job? = null
    private var passiveScoreJob: Job? = null // Job für den passiven Score Generator

    // Events
    fun onCookieClicked() {
        score += clickMultiplier
    }

    fun buyDoubleClickUpgrade() {
        if (score >= doubleClickCost) {
            score -= doubleClickCost
            clickMultiplier *= 2
            doubleClickCost *= 2
        }
    }

    fun buyAutoClickerUpgrade() {
        if (score >= autoClickerCost && !isAutoClickerActive) {
            score -= autoClickerCost
            isAutoClickerActive = true
            startAutoClicker()
        }
    }

    // Neues Item kaufen
    fun buyPassiveScoreGenerator() {
        if (score >= passiveScoreGeneratorCost && !isPassiveScoreGeneratorActive) {
            score -= passiveScoreGeneratorCost
            isPassiveScoreGeneratorActive = true
            startPassiveScoreGenerator()
        }
    }
    // Coroutine für den AutoClicker
    private fun startAutoClicker() {
        autoClickJob?.cancel()
        autoClickJob = viewModelScope.launch {
            while (isAutoClickerActive) {
                // Countdown für den Cooldown
                for (i in autoClickerInterval downTo 1) {
                    autoClickerCooldown = i
                    delay(1000) // 1 Sekunde warten
                }
                autoClickerCooldown = 0 // Cooldown abgelaufen
                onCookieClicked()
            }
            // Wenn der Auto-Klicker deaktiviert wird, Cooldown zurücksetzen
            if (!isAutoClickerActive) {
                autoClickerCooldown = 0
            }
        }
    }

    // Coroutine für den passiven Score Generator
    private fun startPassiveScoreGenerator() {
        passiveScoreJob?.cancel()
        passiveScoreJob = viewModelScope.launch {
            while (isPassiveScoreGeneratorActive) {
                // Countdown für den Cooldown
                for (i in passiveGeneratorInterval downTo 1) {
                    passiveGeneratorCooldown = i
                    delay(1000) // 1 Sekunde warten
                }
                passiveGeneratorCooldown = 0 // Cooldown abgelaufen
                score += passiveScoreAmount
            }
            // Wenn der Generator deaktiviert wird, Cooldown zurücksetzen
            if (!isPassiveScoreGeneratorActive) {
                passiveGeneratorCooldown = 0
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        autoClickJob?.cancel()
        passiveScoreJob?.cancel() // Den neuen Job ebenfalls aufräumen
    }
}

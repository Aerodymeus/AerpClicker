package com.example.myfirstgame // Or the correct package

import androidx.compose.runtime.getValue
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

    // Neues Item: Passive Score Erhöhung
    var isPassiveScoreGeneratorActive by mutableStateOf(false)
        private set
    var passiveScoreGeneratorCost by mutableStateOf(500) // Kosten für den passiven Generator
        private set
    private val passiveScoreAmount = 5 // Menge, die alle 10 Sekunden hinzugefügt wird

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
            doubleClickCost = (doubleClickCost * 2).toInt() // Einfachheitshalber verdoppeln wir hier
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

    private fun startAutoClicker() {
        autoClickJob?.cancel()
        autoClickJob = viewModelScope.launch {
            while (isAutoClickerActive) {
                delay(10000)
                // score += clickMultiplier // Der Auto-Klicker verwendet jetzt den clickMultiplier
                // Wenn du willst, dass er immer nur 1 hinzufügt, ändere es zu score++
                onCookieClicked() // Lässt den Auto-Klicker auch vom Multiplikator profitieren
            }
        }
    }

    // Coroutine für den passiven Score Generator
    private fun startPassiveScoreGenerator() {
        passiveScoreJob?.cancel() // Bestehenden Job abbrechen, falls vorhanden
        passiveScoreJob = viewModelScope.launch {
            while (isPassiveScoreGeneratorActive) {
                delay(10000) // Alle 10 Sekunden
                score += passiveScoreAmount
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoClickJob?.cancel()
        passiveScoreJob?.cancel() // Den neuen Job ebenfalls aufräumen
    }
}

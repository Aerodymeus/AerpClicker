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
    var score by mutableIntStateOf(0) // Empfehlung: Int statt State<Int> für einfache Zahlentypen
        private set
    var clickMultiplier by mutableIntStateOf(1)
        private set
    var doubleClickCost by mutableIntStateOf(50)
        private set
    // NEU: Level-Zähler für das Klick-Upgrade
    var doubleClickLevel by mutableIntStateOf(0)
        private set
    var isAutoClickerActive by mutableStateOf(false)
        private set
            // Auto-Clicker
    var autoClickerCost by mutableIntStateOf(200)
        private set
    var autoClickerCooldown by mutableIntStateOf(0)
        private set
    val autoClickerInterval = 10 // Sekunden
            // passiver Score Generator
    var isPassiveScoreGeneratorActive by mutableStateOf(false)
        private set
    var passiveScoreGeneratorCost by mutableIntStateOf(500) // Kosten für den passiven Generator
        private set
    private val basePassiveScoreAmount = 5 // Basis-Score für den passiven Generator
            // Effektiver Score des passiven Generators (Basis + Bonus)
    var effectivePassiveScoreAmount by mutableIntStateOf(basePassiveScoreAmount)
        private set
    var passiveGeneratorCooldown by mutableIntStateOf(0)
        private set
    private val passiveGeneratorInterval = 10 // Sekunden
            // Fabrik-Upgrade
            var factoryUpgradeLevel by mutableIntStateOf(0)
                private set
    var factoryUpgradeCost by mutableIntStateOf(500) // Startkosten
        private set
    private val factoryUpgradeBonusPerLevel = 5 // Bonus pro Level


    private var autoClickJob: Job? = null
    private var passiveScoreJob: Job? = null // Job für den passiven Score Generator

            // Events
    fun onCookieClicked() {
        score += clickMultiplier
    }
            // Double Click
    fun buyDoubleClickUpgrade() {
        if (score >= doubleClickCost) {
            score -= doubleClickCost
            doubleClickLevel++ // Level erhöhen
            clickMultiplier *= 2 // Die Multiplikator-Logik bleibt gleich (1 -> 2 -> 4 -> 8...)
            doubleClickCost *= 2 // Kosten steigen exponentiell an (oder z.B. (doubleClickCost * 2.5).toInt())
        }
    }
            // Auto Click
    fun buyAutoClickerUpgrade() {
        if (score >= autoClickerCost && !isAutoClickerActive) {
            score -= autoClickerCost
            isAutoClickerActive = true
            startAutoClicker()
        }
    }
            // Passiver Score Generator
    fun buyPassiveScoreGenerator() {
        if (score >= passiveScoreGeneratorCost && !isPassiveScoreGeneratorActive) {
            score -= passiveScoreGeneratorCost
            isPassiveScoreGeneratorActive = true
            updateEffectivePassiveScoreAmount()
            startPassiveScoreGenerator()
        }
    }
            // Fabrik-Upgrade
            fun buyFactoryUpgrade() {
                // Die Prüfung !isFactoryUpgradeActive wird entfernt, da man immer wieder kaufen kann.
                if (score >= factoryUpgradeCost && isPassiveScoreGeneratorActive) {
                    score -= factoryUpgradeCost
                    factoryUpgradeLevel++ // Level erhöhen statt Boolean zu setzen

                    // Kosten für das nächste Level erhöhen (z.B. um 50%)
                    factoryUpgradeCost = (factoryUpgradeCost * 1.5).toInt()

                    updateEffectivePassiveScoreAmount()
                }
            }

    private fun updateEffectivePassiveScoreAmount() {
        // Der Bonus ist jetzt Level * Bonus pro Level
        val totalBonus = factoryUpgradeLevel * factoryUpgradeBonusPerLevel
        effectivePassiveScoreAmount = basePassiveScoreAmount + totalBonus
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
                score += effectivePassiveScoreAmount // Den effektiven Wert verwenden
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

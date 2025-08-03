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
import kotlin.math.pow
import kotlin.math.roundToInt

class GameViewModel : ViewModel() {
    // UI State
    var score by mutableIntStateOf(0) // Empfehlung: Int statt State<Int> für einfache Zahlentypen
        private set
    var clickMultiplier by mutableStateOf(1.0) // Startwert als Double
        private set
    var clickBoostCost by mutableIntStateOf(50) // Umbenannt von doubleClickCost
        private set
    var clickBoostLevel by mutableIntStateOf(0) // Umbenannt von doubleClickLevel
        private set
    private val baseClickValue = 1 // Der Grundwert für einen Klick, bevor Multiplikatoren wirken
    var isAutoClickerActive by mutableStateOf(false)
        private set
            // Auto-Clicker
    var autoClickerCost by mutableIntStateOf(100)
        private set
    var autoClickerCooldown by mutableIntStateOf(0)
        private set
    val autoClickerInterval = 10 // Sekunden
            // passiver Score Generator
    var isPassiveScoreGeneratorActive by mutableStateOf(false)
        private set
    var passiveScoreGeneratorCost by mutableIntStateOf(100) // Kosten für den passiven Generator
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
    var factoryUpgradeCost by mutableIntStateOf(100) // Startkosten
        private set
    private val factoryUpgradeBonusPerLevel = 5 // Bonus pro Level


    private var autoClickJob: Job? = null
    private var passiveScoreJob: Job? = null // Job für den passiven Score Generator

            // Events
    fun onAerpClicked() { // Der Score wird als Int addiert, clickMultiplier ist jetzt Double
        score += clickMultiplier.roundToInt() // Runden auf den nächsten Integer für die Score-Anzeige
    }
        // Double Click
    fun buyClickBoostUpgrade() { // Umbenannt von buyDoubleClickUpgrade
        if (score >= clickBoostCost) {
            score -= clickBoostCost
            clickBoostLevel++

            // Multiplikator-Logik: Basis * 1.2^Level
            // Beispiel: Level 0 -> 1.0 * 1.2^0 = 1.0
            // Level 1 -> 1.0 * 1.2^1 = 1.2
            // Level 2 -> 1.0 * 1.2^2 = 1.44
            // Level 3 -> 1.0 * 1.2^3 = 1.728
            clickMultiplier = baseClickValue * (1.2).pow(clickBoostLevel)

            // Kostensteigerung (Beispiel: * 1.8 für das nächste Level)
            clickBoostCost = (clickBoostCost * 2.0).roundToInt()
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
                onAerpClicked()
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

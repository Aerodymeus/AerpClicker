package com.example.myfirstgame // Or the correct package

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import kotlin.math.max


class GameViewModel : ViewModel() {
    // UI State
    private var internalScore by mutableDoubleStateOf(0.0) // Interner Score als Double
    var displayedScore by mutableIntStateOf(0)       // Score für die UI als Int
        private set

    // Hilfsfunktion, um den UI-Score zu aktualisieren, wenn sich der interne Score ändert
    private fun updateDisplayedScore() {
        displayedScore = internalScore.roundToInt()
    }
    var clickMultiplier by mutableDoubleStateOf(1.0) // Startwert als Double
        private set
    var clickBoostCost by mutableIntStateOf(50) // Umbenannt von doubleClickCost
        private set
    var clickBoostLevel by mutableIntStateOf(0) // Umbenannt von doubleClickLevel
        private set
    private val baseClickValue = 1.0 // Der Grundwert für einen Klick, bevor Multiplikatoren wirken
    var isAutoClickerActive by mutableStateOf(false)
        private set
            // Auto-Clicker
    var autoClickerCost by mutableIntStateOf(100)
        private set
    var autoClickerCooldown by mutableDoubleStateOf(0.0)
        private set
    var autoClickerInterval by mutableDoubleStateOf(10.0) // Sekunden, jetzt veränderbar
        private set
    val minAutoClickerInterval = 0.5 // Minimal erlaubtes Intervall (z.B. 0.5 Sekunden)
    private val autoClickerIntervalReduction = 0.1 // Senkung pro Upgrade
    var autoClickerIntervalUpgradeCost by mutableIntStateOf(150) // Startkosten für das Intervall-Upgrade
        private set
    var autoClickerIntervalUpgradeLevel by mutableIntStateOf(0)
        private set
            // passiver Score Generator
    var isPassiveScoreGeneratorActive by mutableStateOf(false)
        private set
    var passiveScoreGeneratorCost by mutableIntStateOf(100) // Kosten für den passiven Generator
        private set
    private val basePassiveScoreAmount = 5.0 // Basis-Score für den passiven Generator
    var effectivePassiveScoreAmount by mutableDoubleStateOf(basePassiveScoreAmount) // Ist jetzt Double
        private set
    var passiveGeneratorCooldown by mutableIntStateOf(0)
        private set
    private val passiveGeneratorInterval = 10 // Sekunden
            // Fabrik-Upgrade
     var factoryUpgradeLevel by mutableIntStateOf(0)
                private set
    var factoryUpgradeCost by mutableIntStateOf(100) // Startkosten
        private set
    private val factoryUpgradeBonusPerLevel = 5.0 // Bonus pro Level


    private var autoClickJob: Job? = null
    private var passiveScoreJob: Job? = null // Job für den passiven Score Generator

            // Events

    init {
        updateDisplayedScore() // Initialen Anzeigewert setzen
    }

    // In GameViewModel.kt
    private var lastUiUpdateTime = 0L
    private val uiUpdateInterval = 100L // Millisekunden, z.B. 100ms = 10 UI-Updates pro Sekunde
    fun onAerpClicked() {
        internalScore += clickMultiplier
        // Aktualisiere displayedScore nur periodisch
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUiUpdateTime > uiUpdateInterval) {
            updateDisplayedScore()
            lastUiUpdateTime = currentTime
        }
        // Wenn das Spiel beendet wird oder an bestimmten Punkten, stelle sicher, dass der letzte Score angezeigt wird:
        // z.B. in onCleared() oder wenn die App in den Hintergrund geht, updateDisplayedScore() einmal final aufrufen.
    }

    // Call updateDisplayedScore() explicitly after purchases or significant score changes
    private fun handleScoreChangeWithImmediateUpdate() {
        updateDisplayedScore()
        lastUiUpdateTime = System.currentTimeMillis() // Reset timer after explicit update
    }


    // Double Click
    fun buyClickBoostUpgrade() {
        if (internalScore >= clickBoostCost) {
           internalScore -= clickBoostCost
           clickBoostLevel++
           clickMultiplier = baseClickValue * (1.2).pow(clickBoostLevel)
           clickBoostCost = (clickBoostCost * 1.5).roundToInt()
           updateDisplayedScore()
        }
    }
            // Auto Click
    fun buyAutoClickerUpgrade() {
        if (internalScore >= autoClickerCost && !isAutoClickerActive) {
            internalScore -= autoClickerCost
            isAutoClickerActive = true
            startAutoClicker()
            updateDisplayedScore()
            }
        }
    fun buyAutoClickerIntervalUpgrade() {
        if (internalScore >= autoClickerIntervalUpgradeCost && autoClickerInterval > minAutoClickerInterval) {
            internalScore -= autoClickerIntervalUpgradeCost
            autoClickerIntervalUpgradeLevel++

            // Berechne das neue Intervall, stelle sicher, dass es nicht unter das Minimum fällt
            val newInterval = autoClickerInterval - autoClickerIntervalReduction
            autoClickerInterval =
                max(minAutoClickerInterval, newInterval) // max stellt sicher, dass es nicht unter minAutoClickerInterval fällt

            autoClickerIntervalUpgradeCost = (autoClickerIntervalUpgradeCost * 1.6).roundToInt() // Kostensteigerung

            // Wenn der Auto-Clicker bereits aktiv ist, starte ihn mit dem neuen Intervall neu
            if (isAutoClickerActive) {
                startAutoClicker()
            }
            updateDisplayedScore()
        }
    }


    // Passiver Score Generator
    fun buyPassiveScoreGenerator() {
        if (internalScore >= passiveScoreGeneratorCost && !isPassiveScoreGeneratorActive) {
            internalScore -= passiveScoreGeneratorCost
            isPassiveScoreGeneratorActive = true
            updateEffectivePassiveScoreAmount() // Stellt sicher, dass effectivePassiveScoreAmount korrekt ist
            startPassiveScoreGenerator()
            updateDisplayedScore()
        }
    }
            // Fabrik-Upgrade
    fun buyFactoryUpgrade() {
        if (internalScore >= factoryUpgradeCost && isPassiveScoreGeneratorActive) {
            internalScore -= factoryUpgradeCost
            factoryUpgradeLevel++
            factoryUpgradeCost = (factoryUpgradeCost * 1.5).toInt()
            updateEffectivePassiveScoreAmount()
            updateDisplayedScore()
        }
    }

    private fun updateEffectivePassiveScoreAmount() {
        val totalBonus = factoryUpgradeLevel * factoryUpgradeBonusPerLevel
        effectivePassiveScoreAmount = basePassiveScoreAmount + totalBonus
    }

    // Coroutine für den AutoClicker
    private fun startAutoClicker() {
        autoClickJob?.cancel()
        if (!isAutoClickerActive || autoClickerInterval <= 0) { // Zusätzliche Prüfung für Intervall
            autoClickerCooldown = 0.0
            return
        }

        autoClickJob = viewModelScope.launch {
            while (isAutoClickerActive) {
                val delayMillis = (autoClickerInterval * 1000).toLong()
                if (delayMillis <= 0) break // Sicherheitsabbruch, falls Intervall zu klein wird

                // Update Cooldown über die Dauer des Delays (optional, kann Performance kosten)
                // Wenn du eine laufende Cooldown-Anzeige möchtest:
                var timePassedMillis = 0L
                val uiUpdateRate = 100L // Aktualisiere die UI alle 100ms für den Cooldown
                while (timePassedMillis < delayMillis) {
                    if (!isAutoClickerActive) break // Aus der inneren Schleife ausbrechen
                    val remainingMillis = delayMillis - timePassedMillis
                    autoClickerCooldown = remainingMillis / 1000.0
                    val currentLoopDelay = minOf(uiUpdateRate, remainingMillis.coerceAtLeast(0))
                    delay(currentLoopDelay)
                    timePassedMillis += currentLoopDelay
                }

                if (!isAutoClickerActive) { // Erneut prüfen nach der Cooldown-Schleife
                    autoClickerCooldown = 0.0
                    break // Aus der äußeren while-Schleife ausbrechen
                }

                autoClickerCooldown = 0.0
                onAerpClicked() // Klick ausführen

                // Kurzer zusätzlicher Delay, um dem System etwas Luft zu geben, falls das Intervall extrem klein ist.
                // Dies ist ein Workaround und sollte idealerweise durch eine bessere Begrenzung der Klickrate ersetzt werden.
//                if (delayMillis < 50) { // z.B. wenn Intervall unter 50ms ist
//                    delay(50 - delayMillis)
//                }
            }
            if (!isAutoClickerActive) {
                autoClickerCooldown = 0.0
            }
        }
    }


    // Coroutine für den passiven Score Generator
    private fun startPassiveScoreGenerator() {
        passiveScoreJob?.cancel()
        passiveScoreJob = viewModelScope.launch {
            while (isPassiveScoreGeneratorActive) {
                for (i in passiveGeneratorInterval downTo 1) {
                    passiveGeneratorCooldown = i
                    delay(1000)
                }
                passiveGeneratorCooldown = 0
                internalScore += effectivePassiveScoreAmount // effectivePassiveScoreAmount ist Double
                updateDisplayedScore()
            }
            if (!isPassiveScoreGeneratorActive) {
                passiveGeneratorCooldown = 0
            }
        }
    }




    override fun onCleared() {
        super.onCleared()
        autoClickJob?.cancel()
        passiveScoreJob?.cancel() // Den neuen Job ebenfalls aufräumen
        updateDisplayedScore() // Ensure final score is displayed
    }
}

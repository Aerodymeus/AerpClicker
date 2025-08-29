package dev.aerodymeus.aerpclicker // Or the correct package

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
    var internalScore by mutableDoubleStateOf(0.0) // Interner Score als Double
        private set
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


    // Passiver Score Generator (Aerp-Fabrik)
    var isPassiveScoreGeneratorActive by mutableStateOf(false)
        private set
    var passiveScoreGeneratorCost by mutableIntStateOf(100) // Kosten ggf. anpassen
        private set
    private val basePassiveScoreAmount = 5.0
    var effectivePassiveScoreAmount by mutableDoubleStateOf(basePassiveScoreAmount)
        private set
    var passiveGeneratorCooldown by mutableDoubleStateOf(0.0) // WIRD JETZT DOUBLE
        private set
    var passiveGeneratorInterval by mutableDoubleStateOf(10.0) // Sekunden, JETZT VAR & DOUBLE
        private set
    var minPassiveGeneratorInterval = 1.0 // Minimales Intervall für die Fabrik, z.B. 1 Sekunde
        private set // Getter, um es in der UI zu verwenden, aber nicht von außen ändern
    private val passiveGeneratorIntervalReduction = 0.1 // Senkung pro Upgrade

    // NEU: Zustandsvariablen für Fabrik-Intervall-Upgrade
    var factoryIntervalUpgradeCost by mutableIntStateOf(250) // Startkosten
        private set
    var factoryIntervalUpgradeLevel by mutableIntStateOf(0)
        private set


    // Fabrik-Produktions-Upgrade
    var factoryProductionUpgradeLevel by mutableIntStateOf(0) // Umbenannt von factoryUpgradeLevel
        private set
    var factoryProductionUpgradeCost by mutableIntStateOf(150) // Umbenannt von factoryUpgradeCost
        private set
    var factoryProductionBonusPerLevel = 5.0 // Umbenannt von factoryUpgradeBonusPerLevel
        private set

    private var autoClickJob: Job? = null
    private var passiveScoreJob: Job? = null // Job für den passiven Score Generator


    // In GameViewModel.kt
    private var lastUiUpdateTime = 0L
    private val uiUpdateInterval = 100L // Millisekunden, z.B. 100ms = 10 UI-Updates pro Sekunde

    init {
        updateDisplayedScore() // Initialen Anzeigewert setzen
    }

    // Events
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

    private fun handleScoreChangeWithImmediateUpdate() {
        updateDisplayedScore()
        lastUiUpdateTime = System.currentTimeMillis()
    }


    // Double Click
    fun buyClickBoostUpgrade() {
        if (internalScore >= clickBoostCost) {
           internalScore -= clickBoostCost
           clickBoostLevel++
           clickMultiplier = baseClickValue * (1.2).pow(clickBoostLevel)
           clickBoostCost = (clickBoostCost * 1.5).roundToInt()
            handleScoreChangeWithImmediateUpdate()
        }
    }
            // Auto Click
    fun buyAutoClickerUpgrade() {
        if (internalScore >= autoClickerCost && !isAutoClickerActive) {
            internalScore -= autoClickerCost
            isAutoClickerActive = true
            startAutoClicker()
            handleScoreChangeWithImmediateUpdate()
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
            handleScoreChangeWithImmediateUpdate()
        }
    }


    // Passiver Score Generator
    fun buyPassiveScoreGenerator() {
        if (internalScore >= passiveScoreGeneratorCost && !isPassiveScoreGeneratorActive) {
            internalScore -= passiveScoreGeneratorCost
            isPassiveScoreGeneratorActive = true
            updateEffectivePassiveScoreAmount() // Stellt sicher, dass effectivePassiveScoreAmount korrekt ist
            startPassiveScoreGenerator()
            handleScoreChangeWithImmediateUpdate()
        }
    }
            // Fabrik-Upgrade
            // Umbenannt zu buyFactoryProductionUpgrade
            fun buyFactoryProductionUpgrade() {
                if (internalScore >= factoryProductionUpgradeCost && isPassiveScoreGeneratorActive) {
                    internalScore -= factoryProductionUpgradeCost
                    factoryProductionUpgradeLevel++
                    factoryProductionUpgradeCost = (factoryProductionUpgradeCost * 1.5).toInt()
                    updateEffectivePassiveScoreAmount()
                    handleScoreChangeWithImmediateUpdate()
                }
            }

    // NEU: Funktion zum Kaufen des Fabrik-Intervall-Upgrades
    fun buyFactoryIntervalUpgrade() {
        if (internalScore >= factoryIntervalUpgradeCost && isPassiveScoreGeneratorActive && passiveGeneratorInterval > minPassiveGeneratorInterval) {
            internalScore -= factoryIntervalUpgradeCost
            factoryIntervalUpgradeLevel++
            passiveGeneratorInterval = max(minPassiveGeneratorInterval, passiveGeneratorInterval - passiveGeneratorIntervalReduction)
            factoryIntervalUpgradeCost = (factoryIntervalUpgradeCost * 1.7).roundToInt() // Eigene Kostensteigerung
            if (isPassiveScoreGeneratorActive) {
                startPassiveScoreGenerator() // Neu starten mit neuem Intervall
            }
            handleScoreChangeWithImmediateUpdate()
        }
    }

    private fun updateEffectivePassiveScoreAmount() {
        val totalBonus = factoryProductionUpgradeLevel * factoryProductionBonusPerLevel
        effectivePassiveScoreAmount = basePassiveScoreAmount + totalBonus
    }

    // Coroutine für den AutoClicker
    private fun startAutoClicker() {
        autoClickJob?.cancel()
        if (!isAutoClickerActive || autoClickerInterval <= 0) {
            autoClickerCooldown = 0.0
            return
        }
        autoClickJob = viewModelScope.launch {
            while (isAutoClickerActive) {
                val delayMillis = (autoClickerInterval * 1000).toLong()
                if (delayMillis <= 0) break
                var timePassedMillis = 0L
                val uiUpdateRate = 100L
                while (timePassedMillis < delayMillis) {
                    if (!isAutoClickerActive) break
                    val remainingMillis = delayMillis - timePassedMillis
                    autoClickerCooldown = remainingMillis / 1000.0
                    delay(minOf(uiUpdateRate, remainingMillis.coerceAtLeast(0)))
                    timePassedMillis += minOf(uiUpdateRate, remainingMillis.coerceAtLeast(0)) // Korrekte Addition
                }
                if (!isAutoClickerActive) {
                    autoClickerCooldown = 0.0
                    break
                }
                autoClickerCooldown = 0.0
                onAerpClicked()
            }
            if (!isAutoClickerActive) autoClickerCooldown = 0.0
        }
    }


    // Coroutine für den passiven Score Generator
    private fun startPassiveScoreGenerator() {
        passiveScoreJob?.cancel()
        if (!isPassiveScoreGeneratorActive || passiveGeneratorInterval <= 0) { // Abbruchbedingung hinzugefügt
            passiveGeneratorCooldown = 0.0
            return
        }
        passiveScoreJob = viewModelScope.launch {
            while (isPassiveScoreGeneratorActive) {
                val delayMillis = (passiveGeneratorInterval * 1000).toLong()
                if (delayMillis <= 0) break // Sicherheitsabbruch

                var timePassedMillis = 0L
                val uiUpdateRate = 100L // Aktualisiere den Cooldown-Text alle 100ms

                while (timePassedMillis < delayMillis) {
                    if (!isPassiveScoreGeneratorActive) break
                    val remainingMillis = delayMillis - timePassedMillis
                    passiveGeneratorCooldown = remainingMillis / 1000.0 // Cooldown als Double
                    delay(minOf(uiUpdateRate, remainingMillis.coerceAtLeast(0)))
                    timePassedMillis += minOf(uiUpdateRate, remainingMillis.coerceAtLeast(0)) // Korrekte Addition
                }

                if (!isPassiveScoreGeneratorActive) {
                    passiveGeneratorCooldown = 0.0
                    break
                }
                passiveGeneratorCooldown = 0.0
                internalScore += effectivePassiveScoreAmount
                handleScoreChangeWithImmediateUpdate() // UI-Update auslösen
            }
            if (!isPassiveScoreGeneratorActive) {
                passiveGeneratorCooldown = 0.0
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

package com.example.myfirstgame // Or the correct package

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.max
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
    private var internalScore by mutableStateOf(0.0) // Interner Score als Double
    var displayedScore by mutableIntStateOf(0)       // Score für die UI als Int
        private set

    // Hilfsfunktion, um den UI-Score zu aktualisieren, wenn sich der interne Score ändert
    private fun updateDisplayedScore() {
        displayedScore = internalScore.roundToInt()
    }
    var clickMultiplier by mutableStateOf(1.0) // Startwert als Double
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
    var autoClickerCooldown by mutableIntStateOf(0)
        private set
    var autoClickerInterval by mutableStateOf(10.0) // Sekunden, jetzt veränderbar
        private set
    private val baseAutoClickerInterval = 10.0 // Ursprüngliches Intervall
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
    var effectivePassiveScoreAmount by mutableStateOf(basePassiveScoreAmount) // Ist jetzt Double
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
    fun onAerpClicked() { // Der Score wird als Int addiert, clickMultiplier ist jetzt Double
        internalScore += clickMultiplier.roundToInt() // Runden auf den nächsten Integer für die Score-Anzeige
        updateDisplayedScore()
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
           if (!isAutoClickerActive) { // Sicherstellen, dass der Job nur gestartet wird, wenn er aktiv sein soll
               autoClickerCooldown = 0
               return
           }
           autoClickJob = viewModelScope.launch {
               while (isAutoClickerActive) { // Überprüfe weiterhin, ob er aktiv sein soll
                   // Cooldown für die UI
                   // Wir verwenden Double für den Cooldown, um Zehntelsekunden anzuzeigen, wenn das Intervall < 1s wird.
                   // Aber die Schleife selbst läuft in 1-Sekunden-Schritten für den Delay.
                   // Die Cooldown-Anzeige kann genauer sein, wenn gewünscht.
                   // Hier vereinfacht: Cooldown in ganzen Sekunden für den Delay, aber das Intervall ist ein Double.
                   val intervalInWholeSeconds = autoClickerInterval.roundToInt()
                   for (i in intervalInWholeSeconds downTo 1) {
                       if (!isAutoClickerActive) break // Beende den Countdown, wenn deaktiviert
                       autoClickerCooldown = i
                       delay(1000) // 1 Sekunde warten
                   }
                   if (!isAutoClickerActive) { // Erneut prüfen, falls während des Delays deaktiviert
                       autoClickerCooldown = 0
                       break
                   }
                   autoClickerCooldown = 0
                   onAerpClicked()
                   // Warte die exakte Intervallzeit (abzüglich der bereits vergangenen Sekunde des letzten Cooldown-Schritts, falls Intervall > 1s)
                   // Für Intervalle < 1s oder präzisere Timings könnte man hier komplexere Logik verwenden.
                   // Einfacher Ansatz: Nach dem Klick erneut den Cooldown-Zyklus starten.
                   // Die delay(1000) oben sorgt bereits für die Basisfrequenz.
                   // Wenn autoClickerInterval z.B. 0.5s ist, würde die Schleife oben nicht viel machen,
                   // und onAerpClicked() würde effektiv alle ~1 Sekunde aufgerufen. Das muss angepasst werden.

                   // Überarbeitete Logik für den Delay basierend auf dem Double-Intervall:
                   if (autoClickerInterval > 0) {
                       val delayMillis = (autoClickerInterval * 1000).toLong()
                       // Cooldown-Anzeige für die UI (optional detaillierter)
                       var remainingCooldown = autoClickerInterval
                       while (remainingCooldown > 0) {
                           if (!isAutoClickerActive) break
                           autoClickerCooldown = remainingCooldown.roundToInt() // Für die UI-Anzeige
                           val currentDelay = minOf(1000L, (remainingCooldown * 1000).toLong())
                           delay(currentDelay)
                           remainingCooldown -= currentDelay / 1000.0
                       }
                       if (!isAutoClickerActive) {
                           autoClickerCooldown = 0
                           break
                       }
                       autoClickerCooldown = 0
                       // onAerpClicked() wird jetzt nur einmal pro Zyklus nach dem Delay aufgerufen
                   }
               }
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
    }
}

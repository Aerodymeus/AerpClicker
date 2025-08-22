package com.example.myfirstgame

import android.annotation.SuppressLint
import android.content.res.Configuration // Import für Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration // Import für LocalConfiguration
import androidx.compose.ui.res.stringResource // Import für stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview // Import für Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch



class UI : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AerpClickerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AerpClickerApp(gameViewModel: GameViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ShopMenu(gameViewModel = gameViewModel)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.top_bar_title)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(id = R.string.menu_content_description))
                        }
                    }
                )
            }
        ) { paddingValues ->
            GameScreen(
                modifier = Modifier.padding(paddingValues),
                gameViewModel = gameViewModel
            )
        }
    }
}

@SuppressLint("StringFormatMatches", "DefaultLocale") // Nötig für die Formatierung des Multiplikators
@Composable
fun GameScreen(modifier: Modifier = Modifier, gameViewModel: GameViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val mainContent = @Composable {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight() // Füllt die Höhe des ihm zugewiesenen Raums
        ) {
            Text(
                text = stringResource(id = R.string.score_text, gameViewModel.displayedScore),
                fontSize = 32.sp, // Keep only one fontSize
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { gameViewModel.onAerpClicked() },
                modifier = Modifier.size(200.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.click_me_button),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center // Text zentrieren
                )
            }
        }
    }

    val cooldownsContent = @Composable {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            if (gameViewModel.isAutoClickerActive && gameViewModel.autoClickerCooldown > 0) {
                // Zeige das aktuelle Intervall vielleicht auch hier an, oder nur den Cooldown
                val cooldownText = String.format("%.1f", gameViewModel.autoClickerCooldown) // Formatiere das Double
                Text(
                    text = stringResource(id = R.string.cooldown_auto_clicker_prefix) +
                            " $cooldownText" + stringResource(id = R.string.cooldown_suffix),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (gameViewModel.isPassiveScoreGeneratorActive && gameViewModel.passiveGeneratorCooldown > 0) {
                Text(
                    text = stringResource(id = R.string.cooldown_aerp_factory_prefix) + " " +
                            "${gameViewModel.passiveGeneratorCooldown}" +
                            stringResource(id = R.string.cooldown_suffix),
                    fontSize = 16.sp
                )
            }
        }
    }

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp), // Etwas horizontales Padding für die gesamte Reihe
            verticalAlignment = Alignment.CenterVertically // Vertikal zentrieren in der Reihe
        ) {
            // Box für den Hauptinhalt (Zähler und Button)
            // Diese Box wird durch die Spacer zentriert.
            Box(
                modifier = Modifier.weight(1f), // Nimmt den zentralen, flexiblen Bereich ein
                contentAlignment = Alignment.Center // Zentriert den Inhalt der Box (also mainContent)
            ) {
                mainContent()
            }

            // Box für die Cooldowns auf der rechten Seite
            // Nimmt eine feste oder proportionale Breite ein, je nach Bedarf.
            // Hier verwenden wir keine weight, damit es sich an seinen Inhalt anpasst oder eine feste Breite hat.
            Box(
                modifier = Modifier.wrapContentWidth(Alignment.End) // Passt sich der Breite des Inhalts an und ist rechtsbündig
                // Alternativ eine feste Breite: .width(150.dp) oder .requiredWidth(150.dp)
            ) {
                cooldownsContent()
            }
        }
    } else { // Portrait-Modus
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center // Zentriert mainContent
            ) {
                mainContent()
            }
            // Cooldowns unten im Portrait-Modus
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                // Die innere Column von cooldownsContent wird sich basierend auf isLandscape ausrichten
                cooldownsContent()
            }
        }
    }
}

@SuppressLint("StringFormatMatches") // Nötig für die Formatierung des Multiplikators
@Composable
fun ShopMenu(gameViewModel: GameViewModel) {
    data class ShopItemData(
        val name: String,
        val cost: Int,
        val onBuy: () -> Unit,
        val canAfford: Boolean,
        val currentMultiplier: Double? = null, // Für Klick-Multiplikator
        val currentProduction: Double? = null, // NEU: Für aktuelle Produktion (Aerp-Fabrik)
        val isActive: Boolean? = null, // Für AutoClicker, Aerp-Fabrik, Fabrik-Upgrade
        val description: String? = null,
        val requiresBaseItemActive: Boolean? = null, // NEU: Für das Fabrik-Upgrade, um anzuzeigen, ob die Basis-Fabrik benötigt wird
        val currentLevel: Int? = null,
        val currentInterval: Double? = null // NEU für Intervall-Upgrade
    )

    val shopItemsList = listOf(
        ShopItemData(
            name = stringResource(id = R.string.shop_item_click_boost), // NEUER NAME
            cost = gameViewModel.clickBoostCost,                         // NEUE KOSTEN-VARIABLE
            currentMultiplier = gameViewModel.clickMultiplier,           // Multiplikator (jetzt Double)
            onBuy = { gameViewModel.buyClickBoostUpgrade() },            // NEUE KAUF-FUNKTION
            canAfford = gameViewModel.displayedScore >= gameViewModel.clickBoostCost,
            description = stringResource(id = R.string.shop_item_click_boost_description), // NEUE BESCHREIBUNG
            currentLevel = gameViewModel.clickBoostLevel                 // NEUE LEVEL-VARIABLE
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_auto_aerper),
            isActive = gameViewModel.isAutoClickerActive,
            onBuy = { gameViewModel.buyAutoClickerUpgrade() },
            canAfford = gameViewModel.displayedScore >= gameViewModel.autoClickerCost && !gameViewModel.isAutoClickerActive,
            description = stringResource(id = R.string.shop_item_auto_aerper_description, gameViewModel.autoClickerInterval),
            cost = gameViewModel.autoClickerCost
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade),
            cost = gameViewModel.autoClickerIntervalUpgradeCost,
            onBuy = { gameViewModel.buyAutoClickerIntervalUpgrade() },
            // Kann gekauft werden, wenn genug Score, der Auto-Clicker aktiv ist UND das Intervall noch verbessert werden kann
            canAfford = gameViewModel.displayedScore >= gameViewModel.autoClickerIntervalUpgradeCost &&
                    gameViewModel.isAutoClickerActive && // Benötigt aktiven Auto-Clicker
                    gameViewModel.autoClickerInterval > gameViewModel.minAutoClickerInterval, // Nur wenn nicht am Minimum
            isActive = gameViewModel.isAutoClickerActive, // Um ggf. Info anzuzeigen, dass Basis-Item benötigt wird
            description = when {
                !gameViewModel.isAutoClickerActive -> stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade_requires_auto_clicker)
                gameViewModel.autoClickerInterval <= gameViewModel.minAutoClickerInterval ->
                    stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade_description_max_reached, gameViewModel.minAutoClickerInterval)
                else -> stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade_description, gameViewModel.autoClickerInterval)
            },
            currentLevel = gameViewModel.autoClickerIntervalUpgradeLevel,
            currentInterval = gameViewModel.autoClickerInterval, // Um das aktuelle Intervall anzuzeigen
            requiresBaseItemActive = !gameViewModel.isAutoClickerActive // Zeigt an, ob der Basis-Autoklicker benötigt wird
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_aerp_factory),
            isActive = gameViewModel.isPassiveScoreGeneratorActive,
            currentProduction = gameViewModel.effectivePassiveScoreAmount, // Zeige die *effektive* Produktion an
            onBuy = { gameViewModel.buyPassiveScoreGenerator() },
            canAfford = gameViewModel.displayedScore >= gameViewModel.passiveScoreGeneratorCost && !gameViewModel.isPassiveScoreGeneratorActive,
            description = if (gameViewModel.isPassiveScoreGeneratorActive) {
                stringResource(id = R.string.shop_item_aerp_factory_description_active, gameViewModel.effectivePassiveScoreAmount)
            } else {
                stringResource(id = R.string.shop_item_aerp_factory_description_inactive, gameViewModel.effectivePassiveScoreAmount)
                   },
            cost = gameViewModel.passiveScoreGeneratorCost
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_factory_upgrade),
            cost = gameViewModel.factoryUpgradeCost,
            // 'isActive' wird nicht mehr benötigt, wir verwenden 'currentLevel'
            currentLevel = gameViewModel.factoryUpgradeLevel,
            onBuy = { gameViewModel.buyFactoryUpgrade() },
            // Die Bedingung '!gameViewModel.isFactoryUpgradeActive' wird entfernt
            canAfford = gameViewModel.displayedScore >= gameViewModel.factoryUpgradeCost &&
                    gameViewModel.isPassiveScoreGeneratorActive,
            requiresBaseItemActive = !gameViewModel.isPassiveScoreGeneratorActive,
            description = if (!gameViewModel.isPassiveScoreGeneratorActive) {
                stringResource(id = R.string.shop_item_factory_upgrade_description_requires_factory)
            } else {
                stringResource(id = R.string.shop_item_factory_upgrade_description_available)
            },
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(id = R.string.shop_title), fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Etwas mehr Abstand zwischen den Items
        ) {
            items(shopItemsList) { itemData ->
                ShopItem(
                    name = itemData.name,
                    cost = itemData.cost,
                    onBuy = itemData.onBuy,
                    canAfford = itemData.canAfford,
                    currentMultiplier = itemData.currentMultiplier,
                    currentProduction = itemData.currentProduction, // Übergeben
                    isActive = itemData.isActive,
                    description = itemData.description,
                    requiresBaseItemActive = itemData.requiresBaseItemActive, // Übergeben
                    currentLevel = itemData.currentLevel,
                    currentInterval = itemData.currentInterval,
                    gameViewModel = gameViewModel
                )
            }
        }
    }
}

@SuppressLint("StringFormatMatches", "DefaultLocale")
@Composable
fun ShopItem(
    name: String,
    cost: Int,
    onBuy: () -> Unit,
    canAfford: Boolean,
    currentMultiplier: Double? = null,
    currentProduction: Double? = null,
    isActive: Boolean? = null,
    description: String? = null,
    requiresBaseItemActive: Boolean? = null,
    currentLevel: Int? = null,
    currentInterval: Double? = null,
    gameViewModel: GameViewModel
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(name, fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)

        if (description != null) {
            Text(
                description,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        /// Anzeige für Klick-Multiplikator
        if (currentMultiplier != null && currentLevel != null && currentLevel > 0 && name == stringResource(id = R.string.shop_item_click_boost)) {
            val formattedMultiplier = String.format("%.2f", currentMultiplier) // Multiplikator als Double formatieren
            Text(
                // "Aktueller Bonus: %.2fx"
                text = stringResource(id = R.string.shop_item_multiplier_prefix) + formattedMultiplier,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        // NEU: Anzeige für aktuelles Auto-Clicker Intervall
        if (currentInterval != null &&
            (name == stringResource(id = R.string.shop_item_auto_aerper) || name == stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade))) {
            if (gameViewModel.isAutoClickerActive) {
                val formattedInterval = String.format("%.1f", currentInterval)
                Text(
                    // String-Ressource für "Aktuelles Intervall: %.1fs"
                    text = stringResource(id = R.string.shop_item_current_interval_prefix) + formattedInterval + stringResource(id = R.string.shop_item_current_interval_suffix),
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        if (currentLevel != null) {
            Text(
                text = stringResource(id = R.string.shop_item_level_prefix) + "$currentLevel",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        // Fallback für Items, die nur Aktiv/Inaktiv Status haben (wie das Basis-AutoClicker Item oder Aerp-Fabrik)
        else if (isActive != null && (name == stringResource(id = R.string.shop_item_auto_aerper) || name == stringResource(id = R.string.shop_item_aerp_factory))) {
            Text(
                if (isActive) stringResource(id = R.string.shop_item_status_active)
                else stringResource(id = R.string.shop_item_status_inactive),
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }


        // Anzeige der Produktion der Fabrik (gerundet)
        if (currentProduction != null && name == stringResource(id = R.string.shop_item_aerp_factory) && isActive == true) {
            val formattedProduction = String.format("%.1f", currentProduction) // Produktion als Double formatieren
            Text(
                stringResource(id = R.string.shop_item_production_prefix) +
                        formattedProduction + // Verwende den formatierten Wert
                        stringResource(id = R.string.shop_item_production_suffix),
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }


        // Button-Logik
        val buttonEnabled = when (// Für das Basis AutoClicker Item: kann nur gekauft werden, wenn noch nicht aktiv und genug Aerps
            name) {
            stringResource(id = R.string.shop_item_auto_aerper) -> canAfford && isActive == false
            // Für das Basis AerpFactory Item: kann nur gekauft werden, wenn noch nicht aktiv und genug Aerps
            stringResource(id = R.string.shop_item_aerp_factory) -> canAfford && isActive == false
            // Für das AutoClicker Intervall Upgrade:
            stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade) -> canAfford && gameViewModel.isAutoClickerActive && gameViewModel.autoClickerInterval > gameViewModel.minAutoClickerInterval
            // Für andere Items (ClickBoost, FactoryUpgrade)
            else -> canAfford
        }

        val buttonText = when {
            // Basis AutoClicker oder AerpFactory bereits gekauft
            (name == stringResource(id = R.string.shop_item_auto_aerper) || name == stringResource(id = R.string.shop_item_aerp_factory)) && isActive == true ->
                stringResource(id = R.string.shop_item_bought_button)
            // AutoClicker Intervall Upgrade: Max erreicht
            name == stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade) &&
                    gameViewModel.isAutoClickerActive && // Nur wenn Basis aktiv
                    gameViewModel.autoClickerInterval <= gameViewModel.minAutoClickerInterval ->
                stringResource(id = R.string.shop_item_bought_button) // Zeige "Gekauft" oder "Max"
            // Items, die ein Basis-Item benötigen, das aber nicht aktiv ist
            requiresBaseItemActive == true -> stringResource(id = R.string.shop_item_buy_button_requires_base)
            // Items, die mehrfach gekauft/verbessert werden können
            currentLevel != null && currentLevel > 0 && (name == stringResource(id = R.string.shop_item_click_boost) || name == stringResource(id = R.string.shop_item_factory_upgrade) || name == stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade)) ->
                stringResource(id = R.string.shop_item_upgrade_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})"
            // Standard Kaufen-Button
            else -> stringResource(id = R.string.shop_item_buy_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})"
        }

        Button(
            onClick = onBuy,
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }
    }
}




@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Portrait Preview")
@Composable
fun DefaultPreviewPortrait() {
    MaterialTheme {
        // Für eine bessere Preview könntest du hier einen ViewModel mit bestimmten Zuständen erstellen
        val previewViewModel = GameViewModel()
        // Beispiel: Fabrik ist aktiv, Upgrade noch nicht
        // previewViewModel.score = 2000
        // previewViewModel.buyPassiveScoreGenerator() // Simuliere den Kauf der Fabrik
        // Optional: dummyGameViewModel.isAutoClickerActive = true // Für Preview-Zwecke
        AerpClickerApp(gameViewModel = previewViewModel)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "spec:width=640dp,height=360dp,dpi=480", name = "Landscape Preview")
@Composable
fun DefaultPreviewLandscape() {
    MaterialTheme {
        // Für eine bessere Preview könntest du hier einen ViewModel mit bestimmten Zuständen erstellen
        val previewViewModel = GameViewModel()
        AerpClickerApp(gameViewModel = previewViewModel)
    }
}
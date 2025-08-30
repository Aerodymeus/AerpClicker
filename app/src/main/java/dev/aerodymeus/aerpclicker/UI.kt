package dev.aerodymeus.aerpclicker

import android.annotation.SuppressLint
import android.content.res.Configuration // Import für Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import dev.aerodymeus.aerpclicker.ui.theme.AerpClickerTheme
import android.app.Application // Sicherstellen, dass dieser Import da ist
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext // Sicherstellen, dass dieser Import da ist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.OutlinedButton


enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

sealed class Screen {
    object Game : Screen()
    object Options : Screen()
    // object Shop : Screen() // Wenn du den Shop auch als separaten Screen behandeln würdest
}

class UI : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AerpClickerTheme {
                AerpClickerApp()
            }
        }
    }
}

@Composable
fun OptionsScreen(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel,
    gameViewModel: GameViewModel,
    currentThemeSetting: ThemeSetting // Den aktuellen Wert direkt empfangen
) {
    var showResetConfirmationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.options_title), // String Ressource erstellen
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.theme_selection_title), // String Ressource erstellen
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Radio Buttons für Theme-Auswahl
        ThemeSetting.entries.forEach { setting -> // ThemeSetting.values() für ältere Kotlin-Versionen
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (currentThemeSetting == setting),
                        onClick = { themeViewModel.setThemeSetting(setting) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (currentThemeSetting == setting),
                    onClick = { themeViewModel.setThemeSetting(setting) }
                )
                Text(
                    text = when (setting) {
                        ThemeSetting.LIGHT -> stringResource(R.string.theme_light) // String Ressource
                        ThemeSetting.DARK -> stringResource(R.string.theme_dark)   // String Ressource
                        ThemeSetting.SYSTEM -> stringResource(R.string.theme_system) // String Ressource
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp)) // Abstand hinzufügen

        Text(
            text = stringResource(R.string.game_data_title), // Neuer Titel für Spieldaten-Optionen
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedButton( // Oder Button, je nach gewünschtem Stil
            onClick = { showResetConfirmationDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset_game_progress_button))
        }

        // Bestätigungsdialog für das Zurücksetzen des Spielstands
        if (showResetConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmationDialog = false },
                title = { Text(stringResource(R.string.reset_dialog_title)) },
                text = { Text(stringResource(R.string.reset_dialog_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            gameViewModel.resetGameProgress() // Funktion im ViewModel aufrufen
                            showResetConfirmationDialog = false
                            // Optional: Navigiere zum GameScreen oder zeige eine Toast-Nachricht
                            // z.B. currentScreen = Screen.Game (wenn du Zugriff auf currentScreen hast)
                            // oder eine Snackbar anzeigen
                        }
                    ) {
                        Text(stringResource(R.string.reset_dialog_confirm_button))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetConfirmationDialog = false }
                    ) {
                        Text(stringResource(R.string.reset_dialog_dismiss_button))
                    }
                }
            )
            // Hier könntest du weitere Optionen hinzufügen
            // Spacer(Modifier.height(24.dp))
            // Text("Weitere Option...")
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
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            if (gameViewModel.isAutoClickerActive && gameViewModel.autoClickerCooldown > 0) {
                val cooldownText = String.format("%.1f", gameViewModel.autoClickerCooldown)
                Text(
                    text = stringResource(id = R.string.cooldown_auto_clicker_prefix) + " " +
                            cooldownText + stringResource(id = R.string.cooldown_suffix),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (gameViewModel.isPassiveScoreGeneratorActive && gameViewModel.passiveGeneratorCooldown > 0) {
                val cooldownText = String.format("%.1f", gameViewModel.passiveGeneratorCooldown) // Formatieren
                Text(
                    text = stringResource(id = R.string.cooldown_aerp_factory_prefix) + " " +
                            cooldownText + stringResource(id = R.string.cooldown_suffix),
                    fontSize = 16.sp
                )
            }
        }
    }

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { mainContent() }
            Box(modifier = Modifier.wrapContentWidth(Alignment.End)) { cooldownsContent() }
        }
    } else { // Portrait
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { mainContent() }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { cooldownsContent() }
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
        val currentMultiplier: Double? = null,
        val currentProduction: Double? = null, // Für Produktions-Upgrade
        val currentProductionBonus: Double? = null, // Für die Beschreibung des Produktions-Upgrades
        val isActive: Boolean? = null,
        val description: String? = null,
        val requiresBaseItemActive: Boolean? = null,
        val currentLevel: Int? = null,
        val currentInterval: Double? = null
    )

    val shopItemsList = listOf(
        ShopItemData(
            name = stringResource(id = R.string.shop_item_click_boost),
            cost = gameViewModel.clickBoostCost,
            currentMultiplier = gameViewModel.clickMultiplier,
            onBuy = { gameViewModel.buyClickBoostUpgrade() },
            canAfford = gameViewModel.internalScore >= gameViewModel.clickBoostCost,
            description = stringResource(id = R.string.shop_item_click_boost_description),
            currentLevel = gameViewModel.clickBoostLevel
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_auto_aerper),
            isActive = gameViewModel.isAutoClickerActive,
            onBuy = { gameViewModel.buyAutoClickerUpgrade() },
            canAfford = gameViewModel.internalScore >= gameViewModel.autoClickerCost && !gameViewModel.isAutoClickerActive,
            description = stringResource(id = R.string.shop_item_auto_aerper_description, gameViewModel.autoClickerInterval),
            cost = gameViewModel.autoClickerCost,
            currentInterval = gameViewModel.autoClickerInterval
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade),
            cost = gameViewModel.autoClickerIntervalUpgradeCost,
            onBuy = { gameViewModel.buyAutoClickerIntervalUpgrade() },
            canAfford = gameViewModel.internalScore >= gameViewModel.autoClickerIntervalUpgradeCost &&
                    gameViewModel.isAutoClickerActive &&
                    gameViewModel.autoClickerInterval > gameViewModel.minAutoClickerInterval,
            isActive = gameViewModel.isAutoClickerActive,
            description = when {
                !gameViewModel.isAutoClickerActive -> stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade_requires_auto_clicker)
                gameViewModel.autoClickerInterval <= gameViewModel.minAutoClickerInterval ->
                    stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade_description_max_reached, gameViewModel.minAutoClickerInterval)
                else -> stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade_description, gameViewModel.autoClickerInterval)
            },
            currentLevel = gameViewModel.autoClickerIntervalUpgradeLevel,
            currentInterval = gameViewModel.autoClickerInterval,
            requiresBaseItemActive = !gameViewModel.isAutoClickerActive
        ),
        ShopItemData( // Basis Aerp-Fabrik
            name = stringResource(id = R.string.shop_item_aerp_factory),
            isActive = gameViewModel.isPassiveScoreGeneratorActive,
            onBuy = { gameViewModel.buyPassiveScoreGenerator() },
            canAfford = gameViewModel.internalScore >= gameViewModel.passiveScoreGeneratorCost && !gameViewModel.isPassiveScoreGeneratorActive,
            description = if (gameViewModel.isPassiveScoreGeneratorActive) {
                stringResource(id = R.string.shop_item_aerp_factory_description_active, gameViewModel.effectivePassiveScoreAmount)
            } else {
                stringResource(id = R.string.shop_item_aerp_factory_description_inactive, gameViewModel.effectivePassiveScoreAmount) // Zeigt Basisproduktion
            },
            cost = gameViewModel.passiveScoreGeneratorCost,
            currentProduction = if (gameViewModel.isPassiveScoreGeneratorActive) gameViewModel.effectivePassiveScoreAmount else null,
            currentInterval = if (gameViewModel.isPassiveScoreGeneratorActive) gameViewModel.passiveGeneratorInterval else null // Zeige Intervall, wenn aktiv
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_factory_production_upgrade), // Neuer Name
            cost = gameViewModel.factoryProductionUpgradeCost,
            currentLevel = gameViewModel.factoryProductionUpgradeLevel,
            currentProductionBonus = gameViewModel.factoryProductionBonusPerLevel, // Für die Beschreibung
            onBuy = { gameViewModel.buyFactoryProductionUpgrade() },
            canAfford = gameViewModel.internalScore >= gameViewModel.factoryProductionUpgradeCost &&
                    gameViewModel.isPassiveScoreGeneratorActive,
            isActive = gameViewModel.isPassiveScoreGeneratorActive, // Um Status des Basis-Items zu kennen
            requiresBaseItemActive = !gameViewModel.isPassiveScoreGeneratorActive,
            description = when {
                !gameViewModel.isPassiveScoreGeneratorActive -> stringResource(id = R.string.shop_item_factory_production_upgrade_description_requires_factory)
                // Hier könnte man auch den aktuellen Bonus anzeigen, wenn das Item bereits gekauft wurde
                else -> stringResource(id = R.string.shop_item_factory_production_upgrade_description_available, gameViewModel.factoryProductionBonusPerLevel)
            }
        ),
        // NEU: Fabrik Intervall-Upgrade
        ShopItemData(
            name = stringResource(id = R.string.shop_item_factory_interval_upgrade),
            cost = gameViewModel.factoryIntervalUpgradeCost,
            onBuy = { gameViewModel.buyFactoryIntervalUpgrade() },
            canAfford = gameViewModel.internalScore >= gameViewModel.factoryIntervalUpgradeCost &&
                    gameViewModel.isPassiveScoreGeneratorActive &&
                    gameViewModel.passiveGeneratorInterval > gameViewModel.minPassiveGeneratorInterval,
            isActive = gameViewModel.isPassiveScoreGeneratorActive, // Hängt vom Basis-Item ab
            description = when {
                !gameViewModel.isPassiveScoreGeneratorActive -> stringResource(id = R.string.shop_item_factory_interval_upgrade_requires_factory)
                gameViewModel.passiveGeneratorInterval <= gameViewModel.minPassiveGeneratorInterval ->
                    stringResource(id = R.string.shop_item_factory_interval_upgrade_description_max_reached, gameViewModel.minPassiveGeneratorInterval)
                else -> stringResource(id = R.string.shop_item_factory_interval_upgrade_description, gameViewModel.passiveGeneratorInterval)
            },
            currentLevel = gameViewModel.factoryIntervalUpgradeLevel,
            currentInterval = gameViewModel.passiveGeneratorInterval, // Zeige aktuelles Intervall der Fabrik
            requiresBaseItemActive = !gameViewModel.isPassiveScoreGeneratorActive
        )
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(stringResource(id = R.string.shop_title), fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(shopItemsList) { itemData ->
                ShopItem(
                    name = itemData.name,
                    cost = itemData.cost,
                    onBuy = itemData.onBuy,
                    canAfford = itemData.canAfford,
                    currentMultiplier = itemData.currentMultiplier,
                    currentProduction = itemData.currentProduction,
                    currentProductionBonus = itemData.currentProductionBonus,
                    isActive = itemData.isActive,
                    description = itemData.description,
                    requiresBaseItemActive = itemData.requiresBaseItemActive,
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
    currentProductionBonus: Double? = null, // Für die Anzeige des Bonus' des Produktionsupgrades
    isActive: Boolean? = null,
    description: String? = null,
    requiresBaseItemActive: Boolean? = null,
    currentLevel: Int? = null,
    currentInterval: Double? = null,
    gameViewModel: GameViewModel
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            name,
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground // Farbe aus dem Theme verwenden
        )

        if (description != null) {
            Text(
                description,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        /// Anzeige für Klick-Multiplikator
        if (currentMultiplier != null && currentLevel != null && currentLevel > 0 && name == stringResource(id = R.string.shop_item_click_boost)) {
            val formattedMultiplier = String.format("%.2f", currentMultiplier) // Multiplikator als Double formatieren
            Text(
                // "Aktueller Bonus: %.2fx"
                text = stringResource(id = R.string.shop_item_multiplier_prefix) + " " + formattedMultiplier + " " + stringResource(id = R.string.shop_item_cost_suffix),
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        // Aktuelles Intervall für Auto-Klicker oder Fabrik
        if (currentInterval != null &&
            (name == stringResource(id = R.string.shop_item_auto_aerper) || // Basis Auto-Klicker
                    name == stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade) || // Auto-Klicker Intervall-Upgrade
                    name == stringResource(id = R.string.shop_item_aerp_factory) || // Basis Fabrik
                    name == stringResource(id = R.string.shop_item_factory_interval_upgrade) // Fabrik Intervall-Upgrade
                    ) && isActive == true // Nur anzeigen, wenn das zugehörige Basis-Item aktiv ist
        ) {
            val formattedInterval = String.format("%.1f", currentInterval)
            Text(stringResource(id = R.string.shop_item_current_interval_prefix) + " " + formattedInterval + stringResource(id = R.string.shop_item_current_interval_suffix),
                fontSize = 14.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
        }

        // Aktuelle Produktion der Fabrik (Basis oder nach Produktions-Upgrade)
        if (currentProduction != null && name == stringResource(id = R.string.shop_item_aerp_factory) && isActive == true) {
            val formattedProduction = String.format("%.1f", currentProduction)
            Text(stringResource(id = R.string.shop_item_production_prefix) + " " + formattedProduction + " " + stringResource(id = R.string.shop_item_production_suffix),
                fontSize = 14.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
        }
        // Anzeige des zusätzlichen Bonus für das Produktions-Upgrade
        if (currentProductionBonus != null && name == stringResource(id = R.string.shop_item_factory_production_upgrade) && isActive == true && gameViewModel.factoryProductionUpgradeLevel > 0) {
            val formattedBonus = String.format("%.1f", currentProductionBonus * gameViewModel.factoryProductionUpgradeLevel) // Gesamter Bonus
            Text( stringResource(id = R.string.shop_item_current_bonus_prefix) + " " + "+$formattedBonus" + " " + stringResource(id = R.string.shop_item_production_suffix),
                fontSize = 14.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
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


//        // Anzeige der Produktion der Fabrik (gerundet)
//        if (currentProduction != null && name == stringResource(id = R.string.shop_item_aerp_factory) && isActive == true) {
//            val formattedProduction = String.format("%.1f", currentProduction) // Produktion als Double formatieren
//            Text(
//                stringResource(id = R.string.shop_item_production_prefix) + " " +
//                        formattedProduction + " " +// Verwende den formatierten Wert
//                        stringResource(id = R.string.shop_item_production_suffix),
//                fontSize = 14.sp,
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(bottom = 4.dp)
//            )
//        }



        // Button-Logik
        val buttonEnabled = when (name) {
            stringResource(id = R.string.shop_item_auto_aerper) -> canAfford && isActive == false
            stringResource(id = R.string.shop_item_aerp_factory) -> canAfford && isActive == false
            stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade) ->
                canAfford && gameViewModel.isAutoClickerActive && gameViewModel.autoClickerInterval > gameViewModel.minAutoClickerInterval
            stringResource(id = R.string.shop_item_factory_production_upgrade) -> // Produktions-Upgrade
                canAfford && gameViewModel.isPassiveScoreGeneratorActive
            stringResource(id = R.string.shop_item_factory_interval_upgrade) -> // NEU: Fabrik Intervall-Upgrade
                canAfford && gameViewModel.isPassiveScoreGeneratorActive && gameViewModel.passiveGeneratorInterval > gameViewModel.minPassiveGeneratorInterval
            else -> canAfford // Für ClickBoost
        }

        val buttonText = when (name) {
            stringResource(id = R.string.shop_item_auto_aerper) -> if (isActive == true) stringResource(id = R.string.shop_item_bought_button) else stringResource(id = R.string.shop_item_buy_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})"
            stringResource(id = R.string.shop_item_aerp_factory) -> if (isActive == true) stringResource(id = R.string.shop_item_bought_button) else stringResource(id = R.string.shop_item_buy_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})"
            stringResource(id = R.string.shop_item_auto_clicker_interval_upgrade) -> when {
                requiresBaseItemActive == true && isActive == false -> stringResource(id = R.string.shop_item_buy_button_requires_base)
                gameViewModel.isAutoClickerActive && gameViewModel.autoClickerInterval <= gameViewModel.minAutoClickerInterval -> stringResource(id = R.string.shop_item_max_level_button)
                else -> stringResource(id = R.string.shop_item_upgrade_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})"
            }
            stringResource(id = R.string.shop_item_factory_production_upgrade) -> when { // Produktions-Upgrade
                requiresBaseItemActive == true && isActive == false -> stringResource(id = R.string.shop_item_buy_button_requires_base)
                else -> stringResource(id = R.string.shop_item_upgrade_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})"
            }
            stringResource(id = R.string.shop_item_factory_interval_upgrade) -> when { // NEU: Fabrik Intervall-Upgrade
                requiresBaseItemActive == true && isActive == false -> stringResource(id = R.string.shop_item_buy_button_requires_base)
                gameViewModel.isPassiveScoreGeneratorActive && gameViewModel.passiveGeneratorInterval <= gameViewModel.minPassiveGeneratorInterval -> stringResource(id = R.string.shop_item_max_level_button)
                else -> stringResource(id = R.string.shop_item_upgrade_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})"
            }
            else -> stringResource(id = R.string.shop_item_upgrade_button) + " (${stringResource(id = R.string.shop_item_cost_prefix)}$cost${stringResource(id = R.string.shop_item_cost_suffix)})" // Für ClickBoost
        }

        Button(
            onClick = onBuy,
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // Button-Hintergrund
                contentColor = MaterialTheme.colorScheme.onPrimary    // Button-Textfarbe
            )
        ) {
            Text(buttonText)
        }

        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = stringResource(id = R.string.shop_title),
            tint = MaterialTheme.colorScheme.primary // Icon-Farbe
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AerpClickerApp(
    gameViewModel: GameViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }
    val currentTheme by themeViewModel.currentThemeSetting.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Game) } // Zustand für aktuellen Screen

    // Wende das Theme dynamisch an
    AerpClickerTheme(
        darkTheme = when (currentTheme) {
            ThemeSetting.LIGHT -> false
            ThemeSetting.DARK -> true
            ThemeSetting.SYSTEM -> isSystemInDarkTheme()
        }
    ) {

            // Back-Handler Logik
            BackHandler(enabled = drawerState.isOpen && currentScreen == Screen.Game) { // Nur für Drawer im GameScreen        scope.launch {
                scope.launch {
                    drawerState.close()
                }
            }
            BackHandler(enabled = currentScreen == Screen.Options) { // Zurück vom OptionsScreen zum GameScreen
                currentScreen = Screen.Game
            }
            // BackHandler zum Anzeigen des Exit-Dialogs (nur aktiv, wenn Drawer geschlossen ist)
            BackHandler(enabled = drawerState.isClosed && currentScreen == Screen.Game && !showExitDialog) { // Verhindert erneutes Öffnen, wenn Dialog schon offen
                showExitDialog = true
            }

            if (showExitDialog) {
                val currentActivity =
                    LocalActivity.current as? ComponentActivity // Hole die Activity-Referenz hier
                AlertDialog(
                    onDismissRequest = {
                        showExitDialog = false
                    }, // Dialog schließen, wenn außerhalb geklickt wird
                    title = { Text(stringResource(id = R.string.exit_dialog_title)) },
                    text = { Text(stringResource(id = R.string.exit_dialog_text)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showExitDialog = false
                            currentActivity?.finish() // Sicheres Aufrufen von finish()
                        }) {
                            Text(stringResource(id = R.string.exit_dialog_confirm_button))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text(stringResource(id = R.string.exit_dialog_dismiss_button))
                        }
                    }
                )
            }



            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = currentScreen == Screen.Game, // Drawer nur im GameScreen öffnen
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
                                when (currentScreen) {
                                    Screen.Game -> {
                                        // Options-Icon auf der linken Seite im GameScreen
                                        IconButton(onClick = { currentScreen = Screen.Options }) {
                                            Icon(
                                                imageVector = Icons.Filled.Settings,
                                                contentDescription = stringResource(R.string.options_title)
                                            )
                                        }
                                    }

                                    Screen.Options -> {
                                        // Zurück-Icon auf der linken Seite im OptionsScreen
                                        IconButton(onClick = { currentScreen = Screen.Game }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.back_button_description)
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                // Shop-Button nur im GameScreen und jetzt allein in den Actions auf der rechten Seite
                                if (currentScreen == Screen.Game) {
                                    TextButton(
                                        onClick = { scope.launch { drawerState.open() } }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ShoppingCart,
                                            contentDescription = stringResource(id = R.string.shop_title)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(id = R.string.shop_title).uppercase())
                                    }
                                }
                                // Optional: Wenn du noch andere Icons rechts haben möchtest, kämen sie hierher.
                            }
                        )
                    }
                ) { paddingValues ->
                    // Wechsle den angezeigten Inhalt basierend auf currentScreen
                    when (currentScreen) {
                        is Screen.Game -> GameScreen(
                            modifier = Modifier.padding(paddingValues),
                            gameViewModel = gameViewModel
                        )

                        is Screen.Options -> OptionsScreen(
                            modifier = Modifier.padding(paddingValues),
                            themeViewModel = themeViewModel,
                            gameViewModel = gameViewModel,
                            currentThemeSetting = currentTheme // currentTheme direkt übergeben
                        )
                    }
                }
            }
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Portrait Preview")
@Composable
fun DefaultPreviewPortrait() {
    AerpClickerTheme { // Stelle sicher, dass du dein Haupt-Theme verwendest
        val context = LocalContext.current
        // Versuche, den echten ApplicationContext zu bekommen, wenn möglich
        val previewApplication = context.applicationContext as? Application
            ?: Application() // Fallback auf eine sehr einfache Instanz, wenn der Cast fehlschlägt

        val previewViewModel = GameViewModel(previewApplication)

        // Setze hier Testdaten für die Preview, falls gewünscht
        // Beispiel: previewViewModel.internalScore = 1234.0
        // previewViewModel.clickBoostLevel = 2
        // Wichtig: Du müsstest ggf. interne Funktionen aufrufen, um abgeleitete Werte
        // (wie Kosten) im ViewModel zu aktualisieren, wenn du Level direkt setzt.
        // Oder du erstellst eine Hilfsfunktion im ViewModel, um es für Previews zu initialisieren.

        AerpClickerApp(gameViewModel = previewViewModel)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "spec:width=640dp,height=360dp,dpi=480", name = "Landscape Preview")
@Composable
fun DefaultPreviewLandscape() {
    AerpClickerTheme {
        val context = LocalContext.current
        val previewApplication = context.applicationContext as? Application ?: Application()
        val previewViewModel = GameViewModel(previewApplication)
        AerpClickerApp(gameViewModel = previewViewModel)
    }
}





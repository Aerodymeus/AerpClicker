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
//import androidx.compose.ui.platform.LocalContext
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
                text = stringResource(id = R.string.score_text, gameViewModel.score),
                fontSize = 32.sp, // Keep only one fontSize
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { gameViewModel.onCookieClicked() },
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
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp), // Padding für die Cooldowns
            horizontalAlignment = Alignment.End, // Rechtsbündig
            verticalArrangement = Arrangement.Top // Oben in ihrem Bereich
        ) {
            if (gameViewModel.isAutoClickerActive && gameViewModel.autoClickerCooldown > 0) {
                Text(
                    text = stringResource(id = R.string.cooldown_auto_clicker_prefix) + " " +
                            "${gameViewModel.autoClickerCooldown}" +
                            stringResource(id = R.string.cooldown_suffix),
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


@Composable
fun ShopMenu(gameViewModel: GameViewModel) {
    data class ShopItemData(
        val name: String,
        val cost: Int,
        val onBuy: () -> Unit,
        val canAfford: Boolean,
        val currentMultiplier: Int? = null, // Für Klick-Multiplikator
        val currentProduction: Int? = null, // NEU: Für aktuelle Produktion (Aerp-Fabrik)
        val isActive: Boolean? = null, // Für AutoClicker, Aerp-Fabrik, Fabrik-Upgrade
        val description: String? = null,
        val requiresBaseItemActive: Boolean? = null, // NEU: Für das Fabrik-Upgrade, um anzuzeigen, ob die Basis-Fabrik benötigt wird
        val currentLevel: Int? = null
    )

    val shopItemsList = listOf(
        ShopItemData(
            name = stringResource(id = R.string.shop_item_double_aerps),
            cost = gameViewModel.doubleClickCost,
            currentMultiplier = gameViewModel.clickMultiplier,
            onBuy = { gameViewModel.buyDoubleClickUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.doubleClickCost,
            description = stringResource(id = R.string.shop_item_double_aerps_description),
            currentLevel = gameViewModel.doubleClickLevel
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_auto_aerper),
            isActive = gameViewModel.isAutoClickerActive,
            onBuy = { gameViewModel.buyAutoClickerUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.autoClickerCost && !gameViewModel.isAutoClickerActive,
            description = stringResource(id = R.string.shop_item_auto_aerper_description, gameViewModel.autoClickerInterval),
            cost = gameViewModel.autoClickerCost
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_aerp_factory),
            isActive = gameViewModel.isPassiveScoreGeneratorActive,
            currentProduction = gameViewModel.effectivePassiveScoreAmount, // Zeige die *effektive* Produktion an
            onBuy = { gameViewModel.buyPassiveScoreGenerator() },
            canAfford = gameViewModel.score >= gameViewModel.passiveScoreGeneratorCost && !gameViewModel.isPassiveScoreGeneratorActive,
            description = if (gameViewModel.isPassiveScoreGeneratorActive) {
                stringResource(id = R.string.shop_item_aerp_factory_description_active, gameViewModel.effectivePassiveScoreAmount)
            } else {
                stringResource(id = R.string.shop_item_aerp_factory_description_inactive, gameViewModel.effectivePassiveScoreAmount) // Zeige hier den Basiswert an, was die Fabrik produzieren WÜRDE
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
            canAfford = gameViewModel.score >= gameViewModel.factoryUpgradeCost &&
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
                    currentLevel = itemData.currentLevel
                    // gameViewModel übergeben
                )
            }
        }
    }
}


@Composable
fun ShopItem(
    name: String,
    cost: Int,
    onBuy: () -> Unit,
    canAfford: Boolean,
    currentMultiplier: Int? = null,
    currentProduction: Int? = null,
    isActive: Boolean? = null,
    description: String? = null,
    requiresBaseItemActive: Boolean? = null,
    currentLevel: Int? = null
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

        // Anzeige für Multiplikator und Produktion bleibt gleich...
        if (currentMultiplier != null) { /* ... */ }
        if (currentProduction != null && isActive == true) { /* ... */ }

        // Ersetze die "Aktiv/Inaktiv"-Anzeige durch eine Level-Anzeige
        if (currentLevel != null) {
            Text(
                text = stringResource(id = R.string.shop_item_level_prefix) + "$currentLevel",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (isActive != null) {
            // Fallback für andere Items, die weiterhin Aktiv/Inaktiv verwenden
            Text(
                if (isActive) stringResource(id = R.string.shop_item_status_active)
                else stringResource(id = R.string.shop_item_status_inactive),
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (currentProduction != null && isActive == true) {
            Text(
                stringResource(id = R.string.shop_item_production_prefix) +
                        " $currentProduction" + // Leerzeichen vor dem Wert
                        " " + stringResource(id = R.string.shop_item_production_suffix), // Leerzeichen vor dem Suffix
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodySmall
            )
        }

//        if (isActive != null) {
//            Text(
//                if (isActive) stringResource(id = R.string.shop_item_status_active)
//                else stringResource(id = R.string.shop_item_status_inactive),
//                fontSize = 14.sp,
//                style = MaterialTheme.typography.bodySmall,
//                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//            )
//        }

        if (requiresBaseItemActive == true && isActive == false) {
            Text(
                stringResource(id = R.string.shop_item_requires_base_prefix) +
                        " " + stringResource(id = R.string.shop_item_aerp_factory) + // Leerzeichen nach dem Prefix und vor dem Item-Namen
                        stringResource(id = R.string.shop_item_requires_base_suffix), // Leerzeichen hier nur, wenn Suffix nicht direkt anschließen soll
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Kosten anzeigen
        Text(
            stringResource(id = R.string.shop_item_cost_prefix) +  // Prefix
                    " $cost" +                                       // Leerzeichen vor dem Wert (Zahl)
                    " " + stringResource(id = R.string.shop_item_cost_suffix), // Leerzeichen vor dem Suffix ("Aerps")
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )

        Button(
            onClick = onBuy,
            enabled = if (currentLevel != null) { // Spezifische Logik für das Upgrade-Item
                canAfford
            } else { // Alte Logik für die anderen Items
                canAfford && (requiresBaseItemActive == null || !requiresBaseItemActive || isActive == true)
            },            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(
                when {
                    // Für das Upgrade-Item
                    currentLevel != null -> {
                        if (requiresBaseItemActive == true) {
                            stringResource(id = R.string.shop_item_buy_button_requires_base)
                        } else {
                            stringResource(id = R.string.shop_item_upgrade_button)
                        }
                    }
                    // Für die anderen Items (alte Logik)
                    isActive == true -> stringResource(id = R.string.shop_item_bought_button)
                    else -> stringResource(id = R.string.shop_item_buy_button)
                }
            )
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
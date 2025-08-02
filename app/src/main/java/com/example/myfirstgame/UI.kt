package com.example.myfirstgame

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch


class UI : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookieClickerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieClickerApp(gameViewModel: GameViewModel = viewModel()) {
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
                    title = { Text("Cookie Clicker") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menü öffnen")
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
                text = "Cookies: ${gameViewModel.score}",
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { gameViewModel.onCookieClicked() },
                modifier = Modifier.size(200.dp)
            ) {
                Text("Bekomme Kekse!", fontSize = 24.sp)
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
                    text = "Auto-Klicker: ${gameViewModel.autoClickerCooldown}s",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (gameViewModel.isPassiveScoreGeneratorActive && gameViewModel.passiveGeneratorCooldown > 0) {
                Text(
                    text = "Cookie-Fabrik: ${gameViewModel.passiveGeneratorCooldown}s",
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
    // Definieren einer Datenstruktur für die Shop-Items, um die Verwendung mit LazyColumn zu erleichtern.
    // Dies ist optional, kann aber helfen, den Code sauberer zu halten, wenn du viele Items hast.
    data class ShopItemData(
        val name: String,
        val cost: Int,
        val onBuy: () -> Unit,
        val canAfford: Boolean,
        val currentMultiplier: Int? = null,
        val isActive: Boolean? = null,
        val description: String? = null
    )

    val shopItemsList = listOf(
        ShopItemData(
            name = "Doppelte Klicks",
            cost = gameViewModel.doubleClickCost,
            currentMultiplier = gameViewModel.clickMultiplier,
            onBuy = { gameViewModel.buyDoubleClickUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.doubleClickCost
        ),
        ShopItemData(
            name = "Auto-Klicker (Alle 10 Sek.)",
            cost = gameViewModel.autoClickerCost,
            isActive = gameViewModel.isAutoClickerActive,
            onBuy = { gameViewModel.buyAutoClickerUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.autoClickerCost && !gameViewModel.isAutoClickerActive
        ),
        ShopItemData(
            name = "Cookie-Fabrik (+5 alle 10 Sek.)",
            cost = gameViewModel.passiveScoreGeneratorCost,
            isActive = gameViewModel.isPassiveScoreGeneratorActive,
            onBuy = { gameViewModel.buyPassiveScoreGenerator() },
            canAfford = gameViewModel.score >= gameViewModel.passiveScoreGeneratorCost && !gameViewModel.isPassiveScoreGeneratorActive,
            description = if (gameViewModel.isPassiveScoreGeneratorActive) "Produziert 5 Cookies alle 10 Sek." else null
        )
        // Füge hier weitere ShopItemData-Objekte für neue Items hinzu
    )

    Column(
        modifier = Modifier
            .fillMaxSize() // LazyColumn sollte die verfügbare Höhe ausfüllen
            .padding(16.dp)
    ) {
        Text("Shop", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(), // LazyColumn füllt die Breite aus
            verticalArrangement = Arrangement.spacedBy(16.dp) // Abstand zwischen den Items
        ) {
            items(shopItemsList) { itemData -> // Iteriere über die Liste der ShopItemData
                ShopItem(
                    name = itemData.name,
                    cost = itemData.cost,
                    onBuy = itemData.onBuy,
                    canAfford = itemData.canAfford,
                    currentMultiplier = itemData.currentMultiplier,
                    isActive = itemData.isActive,
                    description = itemData.description
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
    isActive: Boolean? = null,
    description: String? = null // Optionaler Parameter für zusätzliche Beschreibung
) {
    Column {
        Text(name, fontSize = 18.sp)
        Text("Kosten: $cost Cookies", fontSize = 14.sp)
        if (currentMultiplier != null) {
            Text("Aktueller Multiplikator: x$currentMultiplier", fontSize = 14.sp)
        }
        if (isActive != null) {
            Text(if (isActive) "Aktiv" else "Inaktiv", fontSize = 14.sp)
        }
        if (description != null) { // Zeige die Beschreibung an, wenn vorhanden
            Text(description, fontSize = 14.sp)
        }
        Button(
            onClick = onBuy,
            enabled = canAfford,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isActive == true) "Gekauft" else "Kaufen")
        }
    }
}

@Preview(showBackground = true, name = "Portrait Preview")
@Composable
fun DefaultPreviewPortrait() {
    MaterialTheme {
        CookieClickerApp()
    }
}

@Preview(showBackground = true, device = "spec:width=640dp,height=360dp,dpi=480", name = "Landscape Preview")
@Composable
fun DefaultPreviewLandscape() {
    MaterialTheme {
        CookieClickerApp() // Hier könntest du einen Test-ViewModel mit aktiven Cooldowns injecten für eine bessere Preview
    }
}
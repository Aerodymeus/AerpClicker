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
//import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // Import für stringResource
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
                    fontSize = 24.sp
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
                    text = stringResource(id = R.string.cooldown_auto_clicker_prefix) +
                            "${gameViewModel.autoClickerCooldown}" +
                            stringResource(id = R.string.cooldown_suffix),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (gameViewModel.isPassiveScoreGeneratorActive && gameViewModel.passiveGeneratorCooldown > 0) {
                Text(
                    text = stringResource(id = R.string.cooldown_aerp_factory_prefix) +
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
    //val context = LocalContext.current
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
            name = stringResource(id = R.string.shop_item_double_aerps),
            cost = gameViewModel.doubleClickCost,
            currentMultiplier = gameViewModel.clickMultiplier,
            onBuy = { gameViewModel.buyDoubleClickUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.doubleClickCost
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_auto_aerper),
            cost = gameViewModel.autoClickerCost,
            isActive = gameViewModel.isAutoClickerActive,
            onBuy = { gameViewModel.buyAutoClickerUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.autoClickerCost && !gameViewModel.isAutoClickerActive
        ),
        ShopItemData(
            name = stringResource(id = R.string.shop_item_aerp_factory),
            cost = gameViewModel.passiveScoreGeneratorCost,
            isActive = gameViewModel.isPassiveScoreGeneratorActive,
            onBuy = { gameViewModel.buyPassiveScoreGenerator() },
            canAfford = gameViewModel.score >= gameViewModel.passiveScoreGeneratorCost && !gameViewModel.isPassiveScoreGeneratorActive,
            description = if (gameViewModel.isPassiveScoreGeneratorActive) stringResource(id = R.string.shop_item_aerp_factory_description_active) else null
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(shopItemsList) { itemData ->
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
    description: String? = null
) {
    Column {
        Text(name, fontSize = 18.sp) // Name kommt bereits als String-Ressource aus ShopMenu
        Text(
            stringResource(id = R.string.shop_item_cost_prefix) +
                    "$cost" +
                    stringResource(id = R.string.shop_item_cost_suffix),
            fontSize = 14.sp
        )
        if (currentMultiplier != null) {
            Text(
                stringResource(id = R.string.shop_item_multiplier_prefix) + "$currentMultiplier",
                fontSize = 14.sp
            )
        }
        if (isActive != null) {
            Text(
                if (isActive) stringResource(id = R.string.shop_item_status_active)
                else stringResource(id = R.string.shop_item_status_inactive),
                fontSize = 14.sp
            )
        }
        if (description != null) {
            Text(description, fontSize = 14.sp) // Beschreibung kommt bereits als String-Ressource aus ShopMenu
        }
        Button(
            onClick = onBuy,
            enabled = canAfford,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isActive == true) stringResource(id = R.string.shop_item_bought_button)
                else stringResource(id = R.string.shop_item_buy_button)
            )
        }
    }
}

@Preview(showBackground = true, name = "Portrait Preview")
@Composable
fun DefaultPreviewPortrait() {
    MaterialTheme {
        AerpClickerApp()
    }
}

@Preview(showBackground = true, device = "spec:width=640dp,height=360dp,dpi=480", name = "Landscape Preview")
@Composable
fun DefaultPreviewLandscape() {
    MaterialTheme {
        AerpClickerApp() // Hier könntest du einen Test-ViewModel mit aktiven Cooldowns injecten für eine bessere Preview
    }
}
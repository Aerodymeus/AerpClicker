package com.example.myfirstgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Cookies: ${gameViewModel.score}",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { gameViewModel.onCookieClicked() },
            modifier = Modifier.size(200.dp) // Größe des Klick-Buttons
        ) {
            Text("Klick mich!", fontSize = 24.sp)
        }
    }
}

@Composable
fun ShopMenu(gameViewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Shop", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        ShopItem(
            name = "Doppelte Klicks",
            cost = gameViewModel.doubleClickCost,
            currentMultiplier = gameViewModel.clickMultiplier,
            onBuy = { gameViewModel.buyDoubleClickUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.doubleClickCost
        )

        Spacer(modifier = Modifier.height(16.dp))

        ShopItem(
            name = "Auto-Klicker (Alle 10 Sek.)",
            cost = gameViewModel.autoClickerCost,
            isActive = gameViewModel.isAutoClickerActive,
            onBuy = { gameViewModel.buyAutoClickerUpgrade() },
            canAfford = gameViewModel.score >= gameViewModel.autoClickerCost && !gameViewModel.isAutoClickerActive
        )
    }
}

@Composable
fun ShopItem(
    name: String,
    cost: Int,
    onBuy: () -> Unit,
    canAfford: Boolean,
    currentMultiplier: Int? = null, // Optional für den Multiplikator-Text
    isActive: Boolean? = null // Optional für den Auto-Klicker Status
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
        Button(
            onClick = onBuy,
            enabled = canAfford,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isActive == true) "Gekauft" else "Kaufen")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        CookieClickerApp()
    }
}
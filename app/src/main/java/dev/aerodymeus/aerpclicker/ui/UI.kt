package dev.aerodymeus.aerpclicker.ui

import android.provider.Settings
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aerodymeus.aerpclicker.BuildConfig
import dev.aerodymeus.aerpclicker.GameViewModel
import dev.aerodymeus.aerpclicker.R
import dev.aerodymeus.aerpclicker.ThemeViewModel
import dev.aerodymeus.aerpclicker.ui.theme.AerpClickerTheme
import kotlinx.coroutines.launch


@Composable
fun getAppVersion(): String {
    return BuildConfig.VERSION_NAME
}


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
            // Hole die ViewModels hier auf der höchsten Ebene.
            val themeViewModel: ThemeViewModel = viewModel()
            val gameViewModel: GameViewModel = viewModel()

            // Sammle die Zustände für Sprache und Theme.
            val currentThemeSetting by themeViewModel.currentThemeSetting.collectAsState()

            // Bestimme, ob der Dark Mode verwendet werden soll.
            val useDarkTheme = when (currentThemeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            // Rufe AerpClickerApp mit den gesammelten Werten auf.
            AerpClickerApp(
                gameViewModel = gameViewModel,
                themeViewModel = themeViewModel,
                darkTheme = useDarkTheme,
                currentThemeSetting = currentThemeSetting
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel,
    gameViewModel: GameViewModel,
    currentThemeSetting: ThemeSetting,
) {
    var showResetConfirmationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val feedbackData by gameViewModel.feedbackEvent.collectAsState()

    // Status der Berechtigung tracken
    var isNotificationEnabled by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Vor Android 13 immer an
            }
        )
    }

    // Der Launcher für den Dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificationEnabled = isGranted
        if (!isGranted) {
            // Optional: Dem Nutzer sagen, dass er es in den System-Einstellungen ändern muss,
            // falls er "Nie wieder fragen" geklickt hat.
        }
    }

    // NEU: LaunchedEffect, der reagiert, wenn feedbackData nicht null ist
    LaunchedEffect(feedbackData) {
        feedbackData?.let { data ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                this.data = "mailto:".toUri() // Nur E-Mail-Apps
                putExtra(Intent.EXTRA_EMAIL, arrayOf(data.recipient))
                putExtra(Intent.EXTRA_SUBJECT, data.subject)
                putExtra(Intent.EXTRA_TEXT, data.body)
            }

            // Sicherstellen, dass eine App existiert, die den Intent verarbeiten kann
            try {
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    // WICHTIG: Gib dem Nutzer Feedback, wenn keine E-Mail-App gefunden wurde
                    Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                // Fange unerwartete Fehler ab
                Toast.makeText(context, "Could not open email app.", Toast.LENGTH_SHORT).show()
            } finally {
                // Setze das Event zurück, damit es nicht erneut ausgelöst wird
                gameViewModel.onFeedbackEventHandled()
            }
        }
    }

    if (showResetConfirmationDialog) {
        ResetConfirmationDialog(onConfirm = {
            gameViewModel.resetGameProgress() // Assuming you have a reset function in your ViewModel
            Toast.makeText(context, "Game progress reset.", Toast.LENGTH_SHORT).show()
        },
            onDismiss = {
                showResetConfirmationDialog = false
            }
        )
    }


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

        var isThemeDropdownExpanded by remember { mutableStateOf(false) }

        // Die Box, die das Textfeld und das Dropdown-Menü zusammenhält
        ExposedDropdownMenuBox(
            expanded = isThemeDropdownExpanded,
            onExpandedChange = { isThemeDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Das Textfeld, das die aktuell ausgewählte Option anzeigt
            TextField(
                value = when (currentThemeSetting) {
                    ThemeSetting.LIGHT -> stringResource(R.string.theme_light)
                    ThemeSetting.DARK -> stringResource(R.string.theme_dark)
                    ThemeSetting.SYSTEM -> stringResource(R.string.theme_system)
                },
                onValueChange = {}, // Leer lassen, da das Feld nur anzeigt
                readOnly = true,    // Verhindert, dass der Nutzer Text eingibt
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isThemeDropdownExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor() // Wichtig, um das Menü am Textfeld auszurichten
                    .fillMaxWidth()
            )

            // Das eigentliche Dropdown-Menü, das bei Klick erscheint
            ExposedDropdownMenu(
                expanded = isThemeDropdownExpanded,
                onDismissRequest = { isThemeDropdownExpanded = false }
            ) {
                // Ein Menüpunkt für jede Theme-Einstellung
                ThemeSetting.entries.forEach { setting ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = when (setting) {
                                    ThemeSetting.LIGHT -> stringResource(R.string.theme_light)
                                    ThemeSetting.DARK -> stringResource(R.string.theme_dark)
                                    ThemeSetting.SYSTEM -> stringResource(R.string.theme_system)
                                }
                            )
                        },
                        onClick = {
                            themeViewModel.setThemeSetting(setting) // Aktion bei Auswahl
                            isThemeDropdownExpanded = false // Menü schließen
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp)) // Abstand hinzufügen

        Text(text = stringResource(R.string.options_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Benachrichtigungs-Option ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.options_notifications_title)) },
                supportingContent = { Text(stringResource(R.string.options_notifications_desc)) },
                trailingContent = {
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                // Wenn der User einschaltet, fragen wir nach der Berechtigung
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                // Wenn der User ausschaltet, schicken wir ihn in die Einstellungen,
                                // da Apps sich Berechtigungen nicht selbst entziehen können.
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            )
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
                        Text(
                            text=stringResource(R.string.reset_dialog_dismiss_button),
                        )
                    }
                }
            )
        }

        Spacer(Modifier.height(32.dp)) // Abstand hinzufügen

        Text(
            text = stringResource(R.string.feedback_title), // Neuer Titel für Spieldaten-Optionen
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedButton( // Oder Button, je nach gewünschtem Stil
            onClick = { gameViewModel.onSendFeedbackClicked() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.feedback_button))
        }

        // Hier könntest du weitere Optionen hinzufügen
            // Spacer(Modifier.height(24.dp))
            // Text("Weitere Option...")
        Spacer(Modifier.height(32.dp)) // Abstand hinzufügen

        Text(
            text=stringResource(R.string.credits_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text=stringResource(R.string.credits_text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Spacer, um die Versionsnummer vom Rest abzuheben.
        Spacer(Modifier.weight(1f))

        // Die App-Version anzeigen
        Text(
            text = stringResource(R.string.app_version, getAppVersion()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Etwas unauffälliger
        )
    }
}

@SuppressLint("StringFormatMatches", "DefaultLocale") // Nötig für die Formatierung des Multiplikators
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    useDarkTheme: Boolean,
    onShopButtonClicked: () -> Unit,
    onSettingsButtonClicked: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Box als äußerster com. google. android. gms. tagmanager. Container, um das Hintergrundbild und den Inhalt zu überlagern
    Box(modifier = modifier.fillMaxSize()) {
        // Prüfen, ob das dunkle Thema aktiv ist
        //val isDarkTheme = isSystemInDarkTheme()

        // Die Matrix zur Invertierung der Farben
        val invertColorsMatrix = ColorMatrix(
            floatArrayOf(
                -1f,  0f,  0f, 0f, 255f,
                0f, -1f,  0f, 0f, 255f,
                0f,  0f, -1f, 0f, 255f,
                0f,  0f,  0f, 1f,   0f
            )
        )
        // HINTERGRUNDBILD
        Image(
            painter = painterResource(id = R.drawable.aerp_button), // Dein Bildname hier
            contentDescription = stringResource(id = R.string.game_background_image_description), // Füge einen beschreibenden String in strings.xml hinzu
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop, // Oder eine andere ContentScale-Option (Crop ist oft gut für Hintergründe)
            colorFilter = if (useDarkTheme) ColorFilter.colorMatrix(invertColorsMatrix) else null
        )

        Button(
            onClick = onShopButtonClicked, // <<< 2. Den neuen Parameter hier verwenden
            modifier = Modifier
                .align(Alignment.TopEnd) // <<< 3. Richtet den Button oben rechts aus
                .padding(16.dp), // Fügt etwas Abstand zum Rand hinzu
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    //tint = MaterialTheme.colorScheme.primary // Sorgt für gute Sichtbarkeit
                )
                Spacer(Modifier.width(4.dp))

                Text(
                    text=stringResource(id = R.string.shop_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    //color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = onSettingsButtonClicked, // <<< 2. Den neuen Parameter hier verwenden
            modifier = Modifier
                .align(Alignment.TopStart) // <<< 3. Richtet den Button oben links aus
                .padding(16.dp),
            shape = RoundedCornerShape(100),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                //tint = MaterialTheme.colorScheme.primary // Sorgt für gute Sichtbarkeit
            )
        }
    }

    val mainContent = @Composable {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight() // Füllt die Höhe des ihm zugewiesenen Raums
        ) {
            Text(
                text = stringResource(id = R.string.score_text, gameViewModel.displayedScore),
                fontSize = 32.sp, // Keep only one fontSize
                color = Color.White,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    ) // Leichter Hintergrund für den Text
                    .padding(8.dp)
            )

            Button(
                onClick = { gameViewModel.onAerpClicked() },
                modifier = Modifier.size(200.dp)

            ) {
                Text(
                    text = stringResource(id = R.string.click_me_button),
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center // Text zentrieren
                )
            }
        }
    }

    val cooldownsContent = @Composable {
        Column(
            modifier = Modifier
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            if (gameViewModel.isAutoClickerActive && gameViewModel.autoClickerCooldown > 0) {
                val cooldownText = String.format("%.1f", gameViewModel.autoClickerCooldown)
                Text(
                    text = stringResource(id = R.string.cooldown_auto_clicker_prefix) + " " +
                            cooldownText + stringResource(id = R.string.cooldown_suffix),
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp),

                )
            }
            if (gameViewModel.isPassiveScoreGeneratorActive && gameViewModel.passiveGeneratorCooldown > 0) {
                val cooldownText = String.format("%.1f", gameViewModel.passiveGeneratorCooldown) // Formatieren
                Text(
                    text = stringResource(id = R.string.cooldown_aerp_factory_prefix) + " " +
                            cooldownText + stringResource(id = R.string.cooldown_suffix),
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp),
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
fun ShopMenu(
    gameViewModel: GameViewModel,
    onCloseClicked: () -> Unit,
) {
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
        val currentInterval: Double? = null,
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
        .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Abstand zur Liste darunter
        ) {
            // 1. Der Zurück-Button (Icon)
            IconButton(onClick = onCloseClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Das Standard-Zurück-Icon
                    contentDescription = stringResource(id = R.string.back_button_description) // Wiederverwendbarer Text für Barrierefreiheit
                )
            }

            // Etwas Abstand zwischen Icon und Titel
            Spacer(modifier = Modifier.width(16.dp))

            // 2. Der Titel
            Text(
                text = stringResource(id = R.string.shop_title),
                style = MaterialTheme.typography.headlineMedium // Passt gut zur TopAppBar
            )

        }
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
    gameViewModel: GameViewModel,
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
    gameViewModel: GameViewModel, // ViewModel wird jetzt übergeben
    themeViewModel: ThemeViewModel, // ViewModel wird jetzt übergeben
    currentThemeSetting: ThemeSetting, // Theme-Einstellung wird übergeben
    darkTheme: Boolean, // Dark-Mode-Einstellung wird übergeben
) {

    // 1. Hole alle notwendigen Zustände aus den ViewModels
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Game) }
    val activity = LocalContext.current as? ComponentActivity
    val context = LocalContext.current

    // 2. Bestimme, ob der Dark Mode basierend auf der Einstellung verwendet werden soll
    val useDarkTheme = when (currentThemeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
    }


    // Launcher für die Berechtigung
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Hier könntest du den Status optional in einem DataStore speichern
    }

    // NEU: Abfrage beim allerersten Start der App
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                // Dies triggert den System-Dialog beim ersten Start
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    // 3. Wende das Theme zentral an. Alles andere ist darin verschachtelt.
    AerpClickerTheme(darkTheme = useDarkTheme) {

        if (showExitDialog) {
            ExitConfirmationDialog(
                onDismiss = { showExitDialog = false },
                onConfirm = {  activity?.finish() }
            )
        }

        // Back-Handler für den Exit-Dialog (nur im GameScreen aktiv)
        BackHandler(enabled = currentScreen == Screen.Game && !showExitDialog) {
            showExitDialog = true
        }

        // Back-Handler für den Options-Screen
        BackHandler(enabled = currentScreen == Screen.Options) {
            currentScreen = Screen.Game
        }



        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = currentScreen == Screen.Game, // Gesten nur im GameScreen erlauben
            drawerContent = {
                ModalDrawerSheet {
                    ShopMenu(
                        gameViewModel = gameViewModel,
                        onCloseClicked = { scope.launch { drawerState.close() } }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.top_bar_title)) },
                        navigationIcon = {
                            if (currentScreen == Screen.Options) {
                                // Zurück-Pfeil im Options-Screen
                                IconButton(onClick = { currentScreen = Screen.Game }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back_button_description)
                                    )
                                }
                            }
                        },
                        actions = {

                        }
                    )
                }
            ) { paddingValues ->
                // 4. Zeige den korrekten Screen basierend auf dem 'currentScreen'-Zustand
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (currentScreen) {
                        is Screen.Game -> GameScreen(
                            gameViewModel = gameViewModel,
                            useDarkTheme = useDarkTheme,
                            onShopButtonClicked = { scope.launch { drawerState.open() } },
                            onSettingsButtonClicked = {
                                currentScreen = Screen.Options // This is the fix!
                            }
                        )

                        is Screen.Options -> OptionsScreen(
                            themeViewModel = themeViewModel,
                            gameViewModel = gameViewModel,
                            currentThemeSetting = currentThemeSetting
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.reset_dialog_title)) }, // Create this string resource
        text = { Text(text = stringResource(R.string.reset_dialog_message)) }, // Create this string resource
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(stringResource(R.string.reset_dialog_confirm_button)) // Create this string resource
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.reset_dialog_dismiss_button)) // Create this string resource
            }
        }
    )
}

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Dialog schließen, wenn außerhalb geklickt wird
        title = { Text(stringResource(id = R.string.exit_dialog_title)) },
        text = { Text(stringResource(id = R.string.exit_dialog_text)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text(stringResource(id = R.string.exit_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.exit_dialog_dismiss_button))
            }
        }
    )
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
        //Beispiel:
        //previewViewModel.internalScore = 1234.0
        //previewViewModel.clickBoostLevel = 2
        // Wichtig: Du müsstest ggf. interne Funktionen aufrufen, um abgeleitete Werte
        // (wie Kosten) im ViewModel zu aktualisieren, wenn du Level direkt setzt.
        // Oder du erstellst eine Hilfsfunktion im ViewModel, um es für Previews zu initialisieren.

        AerpClickerApp(
            gameViewModel = previewViewModel,
            themeViewModel = ThemeViewModel(previewApplication),
            currentThemeSetting = ThemeSetting.SYSTEM,
            darkTheme = false
        )
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
        AerpClickerApp(gameViewModel = previewViewModel,
            themeViewModel = ThemeViewModel(previewApplication),
            currentThemeSetting = ThemeSetting.SYSTEM,
            darkTheme = false
        )
    }
}





package dev.aerodymeus.aerpclicker.ui

import android.provider.Settings
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aerodymeus.aerpclicker.BuildConfig
import dev.aerodymeus.aerpclicker.GameViewModel
import dev.aerodymeus.aerpclicker.R
import dev.aerodymeus.aerpclicker.ThemeViewModel
import dev.aerodymeus.aerpclicker.ui.theme.AerpClickerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

data class ClickFeedback(val id: Long, val x: Float, val y: Float, val value: Double)

@Composable
fun ClickFeedbackEffect(feedback: ClickFeedback, onAnimationFinished: () -> Unit) {
    val animatedY = remember { Animatable(feedback.y) }
    val animatedAlpha = remember { Animatable(1f) }

    LaunchedEffect(feedback.id) {
        // Start the upward movement
        val movement = launch {
            animatedY.animateTo(
                targetValue = feedback.y - 150f,
                animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
            )
        }

        // Wait for 1 second before starting the fade-out
        delay(1000.milliseconds)

        // Fade out the text
        animatedAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
        )

        // Wait for movement to finish if it hasn't already (though it should be done)
        movement.join()

        // Notify that the animation is complete so the item can be removed
        onAnimationFinished()
    }

    // Format the text to show actual points (with decimals if necessary)
    val displayText = remember(feedback.value) {
        if (feedback.value % 1.0 == 0.0) {
            "+${feedback.value.toInt()}"
        } else {
            String.format(Locale.getDefault(), "+%.2f", feedback.value)
        }
    }

    Text(
        text = displayText,
        color = Color.White,
        fontSize = 24.sp,
        modifier = Modifier
            .offset { IntOffset(feedback.x.toInt() - 20, animatedY.value.toInt()) }
            .graphicsLayer(alpha = animatedAlpha.value)
    )
}



enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

/**
 * Hilfsfunktion, um die Theme-Einstellung in einen booleschen Wert aufzulösen.
 */
@Composable
fun ThemeSetting.isDark(): Boolean = when (this) {
    ThemeSetting.LIGHT -> false
    ThemeSetting.DARK -> true
    ThemeSetting.SYSTEM -> isSystemInDarkTheme()
}

/**
 * Hilfsfunktion, um die Theme-Einstellung in einen Anzeigenamen aufzulösen.
 */
@Composable
fun ThemeSetting.getLabel(): String = when (this) {
    ThemeSetting.LIGHT -> stringResource(R.string.theme_light)
    ThemeSetting.DARK -> stringResource(R.string.theme_dark)
    ThemeSetting.SYSTEM -> stringResource(R.string.theme_system)
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

            // Rufe AerpClickerApp mit den gesammelten Werten auf.
            AerpClickerApp(
                gameViewModel = gameViewModel,
                themeViewModel = themeViewModel,
                currentThemeSetting = currentThemeSetting,
                isDarkTheme = currentThemeSetting.isDark()
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    var showResetConfirmationDialog by remember { mutableStateOf(false) }
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

    // Berechtigungsstatus aktualisieren, wenn die App wieder in den Vordergrund kommt
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    isNotificationEnabled = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Der Launcher für den Dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificationEnabled = isGranted
    }

    // LaunchedEffect, der reagiert, wenn feedbackData nicht null ist
    LaunchedEffect(feedbackData) {
        feedbackData?.let { data ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                this.data = "mailto:".toUri() // Nur E-Mail-Apps
                putExtra(Intent.EXTRA_EMAIL, arrayOf(data.recipient))
                putExtra(Intent.EXTRA_SUBJECT, data.subject)
                putExtra(Intent.EXTRA_TEXT, data.body)
            }

            try {
                // Auf modernen Android-Versionen (API 30+) ist try-catch oft zuverlässiger
                // als resolveActivity, es sei denn, man definiert <queries> im Manifest.
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
            } finally {
                // Setze das Event zurück, damit es nicht erneut ausgelöst wird
                gameViewModel.onFeedbackEventHandled()
            }
        }
    }

    // Zentraler Bestätigungsdialog für das Zurücksetzen
    if (showResetConfirmationDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                gameViewModel.resetGameProgress()
                showResetConfirmationDialog = false
                Toast.makeText(context, R.string.reset_game_progress_button, Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                showResetConfirmationDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.options_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Theme Auswahl ---
        var isThemeDropdownExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = isThemeDropdownExpanded,
            onExpandedChange = { isThemeDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentThemeSetting.getLabel(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.theme_selection_title)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isThemeDropdownExpanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isThemeDropdownExpanded,
                onDismissRequest = { isThemeDropdownExpanded = false }
            ) {
                ThemeSetting.entries.forEach { setting ->
                    DropdownMenuItem(
                        text = {
                            Text(text = setting.getLabel())
                        },
                        onClick = {
                            themeViewModel.setThemeSetting(setting)
                            isThemeDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // --- Benachrichtigungen ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.options_notifications_title)) },
                supportingContent = { Text(stringResource(R.string.options_notifications_desc)) },
                trailingContent = {
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            )
            Spacer(Modifier.height(32.dp))
        }

        // --- Spieldaten ---
        Text(
            text = stringResource(R.string.game_data_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedButton(
            onClick = { showResetConfirmationDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset_game_progress_button))
        }

        Spacer(Modifier.height(32.dp))

        // --- Feedback ---
        Text(
            text = stringResource(R.string.feedback_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedButton(
            onClick = { gameViewModel.onSendFeedbackClicked() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.feedback_button))
        }

        Spacer(Modifier.height(32.dp))

        // --- Credits ---
        Text(
            text = stringResource(R.string.credits_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.credits_text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        // --- Version ---
        Text(
            text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@SuppressLint("StringFormatMatches", "DefaultLocale") // Nötig für die Formatierung des Multiplikators
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    isDarkTheme: Boolean,
    onShopButtonClicked: () -> Unit,
    onShopBuildingsButtonClicked: () -> Unit,
    onSettingsButtonClicked: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val feedbacks = remember { mutableStateListOf<ClickFeedback>() }

    // Die Matrix zur Invertierung der Farben - nur einmal erstellen
    val invertColorsMatrix = remember {
        ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    // Box als äußerster Container, um das Hintergrundbild und den Inhalt zu überlagern
    // jetzt mit pointerInput, damit man überall auf den Bildschirm klicken kann und die Position erhält
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.changedToDown()) {
                                change.consume()
                                val newFeedback = ClickFeedback(
                                    id = System.nanoTime() + change.id.value,
                                    x = change.position.x,
                                    y = change.position.y,
                                    value = gameViewModel.clickMultiplier
                                )
                                feedbacks.add(newFeedback)
                                gameViewModel.onAerpClicked()
                            }
                        }
                    }
                }
            }
    ) {
        // HINTERGRUNDBILD
        Image(
            painter = painterResource(id = R.drawable.aerp_button_bg),
            contentDescription = stringResource(id = R.string.game_background_image_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = if (isDarkTheme) ColorFilter.colorMatrix(invertColorsMatrix) else null
        )

        // Visual Feedbacks Overlay
        feedbacks.forEach { feedback ->
            key(feedback.id) {
                ClickFeedbackEffect(feedback = feedback) {
                    feedbacks.remove(feedback)
                }
            }
        }

        // Shop Button
        Button(
            onClick = onShopButtonClicked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                )
                Spacer(Modifier.width(4.dp))

                Text(
                    text=stringResource(id = R.string.shop_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        Button(
            onClick = onShopBuildingsButtonClicked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 16.dp, vertical = 64.dp),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.House,
                    contentDescription = null,
                )
                Spacer(Modifier.width(4.dp))

                Text(
                    text=stringResource(id = R.string.shop_title_buildings).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        // Settings Button
        Button(
            onClick = onSettingsButtonClicked,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = RoundedCornerShape(100),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
            )
        }

        // Spielinhalt und Cooldowns
        val mainContent = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = stringResource(id = R.string.score_text, gameViewModel.displayedScore),
                    fontSize = 32.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                )
            }
        }

        val cooldownsContent = @Composable {
            Column(
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp),
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
                    val cooldownText = String.format("%.1f", gameViewModel.passiveGeneratorCooldown)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { mainContent() }
                Box(modifier = Modifier.wrapContentWidth(Alignment.End)) { cooldownsContent() }
            }
        } else { // Portrait
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { mainContent() }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { cooldownsContent() }
            }
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
        val currentProduction: Double? = null, // Für Productions-Upgrade
        val currentProductionBonus: Double? = null, // Für die Beschreibung des Productions-Upgrades
        val isActive: Boolean? = null,
        val description: String? = null,
        val requiresBaseItemActive: Boolean? = null,
        val currentLevel: Int? = null,
        val currentInterval: Double? = null,
    )

    val upgradeItems = listOf(
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

            // Etwas Abstand zwischen Icon and Titel
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
            items(upgradeItems) { itemData ->
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

//    ShopMenuContent(
//        title = stringResource(id = R.string.shop_title),
//        items = upgradeItems, // Du müsstest die ShopItemData Logik hier nutzen
//        onCloseClicked = onCloseClicked,
//        gameViewModel = gameViewModel
//    )

}

@Composable
fun BuildingsMenu(
    gameViewModel: GameViewModel,
    onCloseClicked: () -> Unit,
) {
    data class ShopItemData(
        val name: String,
        val cost: Int,
        val onBuy: () -> Unit,
        val canAfford: Boolean,
        val currentMultiplier: Double? = null,
        val currentProduction: Double? = null, // Für Productions-Upgrade
        val currentProductionBonus: Double? = null, // Für die Beschreibung des Productions-Upgrades
        val isActive: Boolean? = null,
        val description: String? = null,
        val requiresBaseItemActive: Boolean? = null,
        val currentLevel: Int? = null,
        val currentInterval: Double? = null,
    )
    // Hier definieren wir nur die "Building" Items
    val buildingItems = listOf(
        // ... (Aerp-Fabrik, Factory Production, Factory Interval)
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
            name = stringResource(id = R.string.shop_item_auto_aerper),
            isActive = gameViewModel.isAutoClickerActive,
            onBuy = { gameViewModel.buyAutoClickerUpgrade() },
            canAfford = gameViewModel.internalScore >= gameViewModel.autoClickerCost && !gameViewModel.isAutoClickerActive,
            description = stringResource(id = R.string.shop_item_auto_aerper_description, gameViewModel.autoClickerInterval),
            cost = gameViewModel.autoClickerCost,
            currentInterval = gameViewModel.autoClickerInterval
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

            // Etwas Abstand zwischen Icon and Titel
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
            items(buildingItems) { itemData ->
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


//    ShopMenuContent(
//        title = stringResource(id = R.string.shop_title_buildings),
//        items = buildingItems,
//        onCloseClicked = onCloseClicked,
//        gameViewModel = gameViewModel
//    )

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
    currentProductionBonus: Double? = null, // Für die Anzeige des Bonus des Produktionsupgrades
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

        // Aktuelle Produktion der Fabrik (Basis oder nach Productions-Upgrade)
        if (currentProduction != null && name == stringResource(id = R.string.shop_item_aerp_factory) && isActive == true) {
            val formattedProduction = String.format("%.1f", currentProduction)
            Text(stringResource(id = R.string.shop_item_production_prefix) + " " + formattedProduction + " " + stringResource(id = R.string.shop_item_production_suffix),
                fontSize = 14.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
        }
        // Anzeige des zusätzlichen Bonus für das Productions-Upgrade
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
            stringResource(id = R.string.shop_item_factory_production_upgrade) -> // Productions-Upgrade
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
            stringResource(id = R.string.shop_item_factory_production_upgrade) -> when { // Productions-Upgrade
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
    gameViewModel: GameViewModel,
    themeViewModel: ThemeViewModel,
    currentThemeSetting: ThemeSetting,
    isDarkTheme: Boolean, // Umbenannt für bessere Konvention
) {

    // 1. Hole alle notwendigen Zustände aus den ViewModel
    val shopDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val buildingsDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Game) }
    val activity = LocalActivity.current as? ComponentActivity
    val context = LocalContext.current
    val currentVersion = BuildConfig.VERSION_NAME
    val savedVersion by gameViewModel.lastVersionName.collectAsState(initial = null)


    // Launcher für die Berechtigung
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(), fun(isGranted: Boolean) {
            // Hier könntest du den Status optional in einem DataStore speichern
        }
    )

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

    LaunchedEffect(savedVersion) {
        // Wenn savedVersion null ist, laden wir noch aus dem DataStore
        if (savedVersion != null) {
            if (savedVersion != currentVersion) {
                // Die Versionen unterscheiden sich → Update erkannt!

                // Prüfen, ob wir die Berechtigung haben
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

                    gameViewModel.showUpdateNotification(context)
                }

                // Jetzt die neue Version speichern, damit die Nachricht nur 1x kommt
                gameViewModel.updateSavedVersionName(currentVersion)
            }
        } else {
            // Erster Start der App überhaupt (oder DataStore leer)
            // wir speichern die aktuelle Version ohne Nachricht
            gameViewModel.updateSavedVersionName(currentVersion)
        }
    }

    // 3. Wende das Theme zentral an. Alles andere ist darin verschachtelt.
    AerpClickerTheme(darkTheme = isDarkTheme) {

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



        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = buildingsDrawerState,
                gesturesEnabled = false,
                drawerContent = {
                    // Den Inhalt des Drawers wieder auf LTR setzen, damit Text normal aussieht
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ModalDrawerSheet {
                            BuildingsMenu(
                                gameViewModel = gameViewModel,
                                onCloseClicked = { scope.launch { buildingsDrawerState.close() } }
                            )
                        }
                    }
                }
            ) {
                // Den restlichen Inhalt der App wieder auf LTR setzen
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

                    // --- LINKER DRAWER (Upgrades) ---
                    ModalNavigationDrawer(
                        drawerState = shopDrawerState,
                        gesturesEnabled = false,
                        drawerContent = {
                            ModalDrawerSheet {
                                ShopMenu(
                                    gameViewModel = gameViewModel,
                                    onCloseClicked = { scope.launch { shopDrawerState.close() } }
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
                                    }
                                )
//                                    actions = {
//
//                                    }
                            }

                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                when (currentScreen) {
                                    is Screen.Game -> GameScreen(
                                        gameViewModel = gameViewModel,
                                        isDarkTheme = isDarkTheme,
                                        onShopButtonClicked = { scope.launch { shopDrawerState.open() } },
                                        onShopBuildingsButtonClicked = { scope.launch { buildingsDrawerState.open() } }, // Jetzt verknüpft!
                                        onSettingsButtonClicked = { currentScreen = Screen.Options }
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
            ?: object : Application() {
                override fun getApplicationContext(): Context = this
                override fun getPackageName(): String = context.packageName
                override fun getFilesDir(): java.io.File = context.filesDir
            }

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
            isDarkTheme = false
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "spec:width=640dp,height=360dp,dpi=480", name = "Landscape Preview")
@Composable
fun DefaultPreviewLandscape() {
    AerpClickerTheme {
        val context = LocalContext.current
        val previewApplication = context.applicationContext as? Application
            ?: object : Application() {
                override fun getApplicationContext(): Context = this
                override fun getPackageName(): String = context.packageName
                override fun getFilesDir(): java.io.File = context.filesDir
            }
        val previewViewModel = GameViewModel(previewApplication)
        AerpClickerApp(gameViewModel = previewViewModel,
            themeViewModel = ThemeViewModel(previewApplication),
            currentThemeSetting = ThemeSetting.SYSTEM,
            isDarkTheme = false
        )
    }
}

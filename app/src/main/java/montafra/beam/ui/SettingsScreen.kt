package montafra.beam.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import montafra.beam.BatteryViewModel
import montafra.beam.R
import montafra.beam.applyNightMode
import montafra.beam.settingsName
import montafra.beam.settingsUpdateInd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, vm: BatteryViewModel = viewModel()) {
    val data by vm.data.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences(settingsName, Context.MODE_PRIVATE) }

    var indicatorEntries by remember {
        mutableStateOf(prefs.getStringSet("indicatorEntries", null) ?: emptySet())
    }
    var currentScalar by remember { mutableFloatStateOf(prefs.getFloat("currentScalar", 1f)) }
    var invertCurrent by remember { mutableStateOf(prefs.getBoolean("invertCurrent", false)) }
    var themeMode by remember { mutableStateOf(prefs.getString("themeMode", "system") ?: "system") }
    var customColorValue by remember { mutableIntStateOf(prefs.getInt("themeColorValue", colorSwatches[6])) }

    fun saveIndicatorEntries() {
        prefs.edit().putStringSet("indicatorEntries", indicatorEntries).commit()
        context.sendBroadcast(Intent().setPackage(context.packageName).setAction(settingsUpdateInd))
    }

    fun saveWorkarounds() {
        prefs.edit()
            .putFloat("currentScalar", currentScalar)
            .putBoolean("invertCurrent", invertCurrent)
            .commit()
        context.sendBroadcast(
            Intent().setPackage(context.packageName).setAction(settingsUpdateInd)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ico_back),
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Theme
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(stringResource(R.string.themeMode))
                Spacer(Modifier.height(8.dp))
                val modeOptions = listOf(
                    stringResource(R.string.themeModeSystem),
                    stringResource(R.string.themeModeLight),
                    stringResource(R.string.themeModeDark),
                    stringResource(R.string.themeModeOled),
                )
                val modeKeys = listOf("system", "light", "dark", "oled")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    modeOptions.forEachIndexed { i, label ->
                        SegmentedButton(
                            selected = themeMode == modeKeys[i],
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                themeMode = modeKeys[i]
                                prefs.edit().putString("themeMode", themeMode).commit()
                                applyNightMode(themeMode)
                            },
                            shape = SegmentedButtonDefaults.itemShape(i, modeOptions.size),
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionHeader(stringResource(R.string.themeColor))
                Spacer(Modifier.height(8.dp))
                val colorOptions = listOf(
                    stringResource(R.string.themeColorAuto),
                    stringResource(R.string.themeColorCustom),
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    colorOptions.forEachIndexed { i, label ->
                        SegmentedButton(
                            selected = if (i == 0) customColorValue == -1 else customColorValue != -1,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (i == 0) {
                                    customColorValue = -1
                                    prefs.edit().putInt("themeColorValue", -1).commit()
                                } else {
                                    val color = if (customColorValue != -1) customColorValue else colorSwatches[11]
                                    customColorValue = color
                                    prefs.edit().putInt("themeColorValue", color).commit()
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(i, colorOptions.size),
                            label = { Text(label) },
                        )
                    }
                }
                AnimatedVisibility(
                    visible = customColorValue != -1,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        ColorSwatchPicker(
                            selectedColor = customColorValue.takeIf { it != -1 },
                            onColorSelected = { color ->
                                customColorValue = color
                                prefs.edit().putInt("themeColorValue", color).commit()
                            },
                        )
                    }
                }
            }

            // Status Bar Indicator
            item {
                SectionHeader(stringResource(R.string.statusBarIndicator))
                Spacer(Modifier.height(8.dp))
                val metricLabels = listOf("A", "Ah", "°C", "V", "Wh", "%")
                val metricKeys   = listOf("A", "Ah", "C",  "V", "Wh", "%")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    metricKeys.forEachIndexed { i, key ->
                        FilterChip(
                            selected = key in indicatorEntries,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                indicatorEntries = if (key in indicatorEntries)
                                    indicatorEntries - key
                                else
                                    indicatorEntries + key
                                saveIndicatorEntries()
                            },
                            label = { Text(metricLabels[i]) },
                        )
                    }
                }
            }

            // Workarounds
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(stringResource(R.string.workarounds))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.workaroundsDesc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.currentScalar), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                val scalarOptions = listOf("0.001×", "1×", "1000×")
                val scalarValues = listOf(0.001f, 1f, 1000f)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    scalarOptions.forEachIndexed { i, label ->
                        SegmentedButton(
                            selected = currentScalar == scalarValues[i],
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentScalar = scalarValues[i]
                                saveWorkarounds()
                            },
                            shape = SegmentedButtonDefaults.itemShape(i, scalarOptions.size),
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                ListItem(
                    headlineContent = { Text(stringResource(R.string.invertCurrent)) },
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.invertCurrentDesc),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = invertCurrent,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                invertCurrent = it
                                saveWorkarounds()
                            },
                        )
                    },
                )
            }

            // Live preview — shows effect of workaround settings
            item {
                androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.charging), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(data.charging, style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.power), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(data.power, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

private val colorSwatches = listOf(
    0xFFB3261E, 0xFFC94B0C, 0xFFF0A500,
    0xFF386A20, 0xFF00696B, 0xFF0061A4,
    0xFF5F4AA6, 0xFFB5006D, 0xFF9C4046,
    0xFF795548, 0xFF546E7A, 0xFF0080FF,
).map { it.toInt() }

@Composable
private fun ColorSwatchPicker(selectedColor: Int?, onColorSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colorSwatches.chunked(6).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { color ->
                    ColorSwatch(color, selected = selectedColor == color) { onColorSelected(color) }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Int, selected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
            .padding(if (selected) 2.5.dp else 0.dp)
            .clip(CircleShape)
            .background(Color(color))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
    )
}

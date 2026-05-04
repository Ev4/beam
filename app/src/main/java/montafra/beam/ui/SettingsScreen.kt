package montafra.beam.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import montafra.beam.R
import montafra.beam.StatusService
import montafra.beam.settingsName
import montafra.beam.settingsUpdateInd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences(settingsName, Context.MODE_PRIVATE) }
    var showDonateDialog by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(prefs.getBoolean("notificationEnabled", true)) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val setNotificationEnabled = { enabled: Boolean ->
        notificationEnabled = enabled
        prefs.edit().putBoolean("notificationEnabled", enabled).commit()
        if (enabled) {
            context.startForegroundService(Intent(context, StatusService::class.java))
        } else {
            context.sendBroadcast(Intent(settingsUpdateInd).setPackage(context.packageName))
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
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
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Notification toggle + Advanced Settings in one item so AnimatedVisibility
            // controls spacing too — no phantom gap from spacedBy when card is hidden.
            item {
                Column {
                    Card(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            setNotificationEnabled(!notificationEnabled)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ico_notification),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.notification),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = stringResource(R.string.notificationEnableDesc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { enabled ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    setNotificationEnabled(enabled)
                                },
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = notificationEnabled,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(220)) + fadeOut(tween(180)),
                    ) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            SettingsNavCard(
                                iconRes = R.drawable.ico_tune,
                                title = stringResource(R.string.advancedSettings),
                                description = stringResource(R.string.notificationDesc),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate("settings/notification")
                                },
                            )
                        }
                    }
                }
            }
            item {
                SettingsNavCard(
                    iconRes = R.drawable.ico_theme,
                    title = stringResource(R.string.theme),
                    description = stringResource(R.string.themeDesc),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate("settings/theme")
                    },
                )
            }
            item {
                SettingsNavCard(
                    iconRes = R.drawable.ico_settings,
                    title = stringResource(R.string.workarounds),
                    description = stringResource(R.string.workaroundsShortDesc),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate("settings/workarounds")
                    },
                )
            }

            // About
            item {
                val version = remember {
                    try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" }
                    catch (_: Exception) { "" }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.about),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.supportMe)) },
                    supportingContent = { Text("BTC · XMR · Lightning") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ico_donate),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDonateDialog = true
                    },
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.sourceCode)) },
                    supportingContent = { Text("github.com/montafra/beam") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ico_github),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/montafra/beam"))
                        )
                    },
                )
                ListItem(
                    headlineContent = { Text("Beam $version") },
                    supportingContent = { Text("No ads · No tracking · No data collection") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ico_info),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                setData(Uri.fromParts("package", context.packageName, null))
                            }
                        )
                    },
                )
            }
        }
    }

    if (showDonateDialog) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        AlertDialog(
            onDismissRequest = { showDonateDialog = false },
            title = { Text(stringResource(R.string.supportMe)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DonateRow(
                        "BTC",
                        "sp1qqfzps48q94usuqwhfcp082kg3pphr9zyh32cg4h4q84rvr6pa3d6vq56w3trm5cs5rgw5g3wcravusunh39utwfy9p2fe7e4g774r66rwcagqpmy",
                        clipboardManager,
                    )
                    DonateRow(
                        "XMR",
                        "876wwukGWhU9H6qez4Qmt5gTBBmdKzoDg3zvT33QCwjy9e7jS7MVjQySUCpNhoVrFcF15AicUJ4VaVrTKAXGMu5D7yUbqFs",
                        clipboardManager,
                    )
                    DonateRow("Lightning", "monta@cake.cash", clipboardManager)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDonateDialog = false }) { Text("Close") }
            },
        )
    }
}

@Composable
private fun SettingsNavCard(
    iconRes: Int,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun SubLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

internal val colorSwatches = listOf(
    0xFFB3261E, 0xFFC94B0C, 0xFFF0A500,
    0xFF386A20, 0xFF00696B, 0xFF0061A4,
    0xFF5F4AA6, 0xFFB5006D, 0xFF9C4046,
    0xFF795548, 0xFF546E7A, 0xFF0080FF,
).map { it.toInt() }

@Composable
internal fun ColorSwatchPicker(selectedColor: Int?, onColorSelected: (Int) -> Unit) {
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
private fun DonateRow(label: String, address: String, clipboard: ClipboardManager) {
    val haptic = LocalHapticFeedback.current
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                clipboard.setPrimaryClip(ClipData.newPlainText(label, address))
            }) {
                Icon(
                    painter = painterResource(R.drawable.ico_copy),
                    contentDescription = stringResource(R.string.copy),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
internal fun ColorSwatch(color: Int, selected: Boolean, onClick: () -> Unit) {
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

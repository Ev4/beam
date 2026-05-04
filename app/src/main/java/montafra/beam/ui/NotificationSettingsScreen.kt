package montafra.beam.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import montafra.beam.R
import montafra.beam.settingsName
import montafra.beam.settingsUpdateInd

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences(settingsName, Context.MODE_PRIVATE) }

    var indicatorEntries by remember {
        mutableStateOf(prefs.getStringSet("indicatorEntries", null) ?: emptySet())
    }
    var notificationIndicator by remember {
        mutableStateOf(prefs.getString("notificationIndicator", "W") ?: "W")
    }

    fun saveIndicatorEntries() {
        prefs.edit().putStringSet("indicatorEntries", indicatorEntries).commit()
        context.sendBroadcast(Intent().setPackage(context.packageName).setAction(settingsUpdateInd))
    }

    fun saveNotificationIndicator() {
        prefs.edit().putString("notificationIndicator", notificationIndicator).commit()
        context.sendBroadcast(Intent().setPackage(context.packageName).setAction(settingsUpdateInd))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification)) },
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
            item {
                SubLabel(stringResource(R.string.notificationIcon))
                Spacer(Modifier.height(6.dp))
                val iconLabels = listOf("W", "A", "Ah", "°C", "V", "Wh", "%")
                val iconKeys   = listOf("W", "A", "Ah", "C",  "V", "Wh", "%")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    iconKeys.forEachIndexed { i, key ->
                        FilterChip(
                            selected = notificationIndicator == key,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                notificationIndicator = key
                                saveNotificationIndicator()
                            },
                            label = { Text(iconLabels[i]) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SubLabel(stringResource(R.string.statusBarIndicator))
                Spacer(Modifier.height(6.dp))
                val metricLabels = listOf("W", "A", "Ah", "°C", "V", "Wh", "%")
                val metricKeys   = listOf("W", "A", "Ah", "C",  "V", "Wh", "%")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

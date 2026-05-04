package montafra.beam

import android.Manifest.permission
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import montafra.beam.ui.MainScreen
import montafra.beam.ui.NotificationSettingsScreen
import montafra.beam.ui.SettingsScreen
import montafra.beam.ui.ThemeSettingsScreen
import montafra.beam.ui.WorkaroundsSettingsScreen
import montafra.beam.ui.theme.BeamTheme
import montafra.beam.ui.theme.rememberThemePrefs

const val namespace = "montafra.beam"
const val batteryDataReq = "$namespace.battery-data-req"
const val batteryDataResp = "$namespace.battery-data-resp"
const val intervalMs = 1_250L
const val noteChannelId = "$namespace.status.v4"
const val noteId = 1
const val settingsName = "settings"
const val settingsUpdateInd = "$namespace.settings-update-ind"

class MainActivity : ComponentActivity() {
    enum class Perm { Granted, Denied, NotAsked }

    private fun getPerm(name: String): Perm {
        val settings = getSharedPreferences(settingsName, MODE_PRIVATE)
        return when {
            checkSelfPermission(name) == PackageManager.PERMISSION_GRANTED -> Perm.Granted
            settings.getBoolean("${name}_ASKED", false) -> Perm.Denied
            else -> Perm.NotAsked
        }
    }

    private fun requestPerm(name: String) {
        getSharedPreferences(settingsName, MODE_PRIVATE)
            .edit().putBoolean("${name}_ASKED", true).apply()
        requestPermissions(arrayOf(name), 0)
    }

    @Suppress("DEPRECATION")
    private fun serviceRunning(): Boolean {
        val serviceName = StatusService::class.java.name
        val mgr = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return mgr.getRunningServices(Int.MAX_VALUE).any { it.service.className == serviceName }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (getPerm(permission.POST_NOTIFICATIONS) == Perm.NotAsked)
            requestPerm(permission.POST_NOTIFICATIONS)
        if (!serviceRunning())
            startForegroundService(Intent(this, StatusService::class.java))

        setContent {
            val themePrefs by rememberThemePrefs()
            BeamTheme(themePrefs) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = {
                        slideIn(tween(340, easing = LinearOutSlowInEasing)) { IntOffset(it.width, 0) }
                    },
                    exitTransition = {
                        slideOut(tween(280, easing = FastOutLinearInEasing)) { IntOffset(-it.width / 3, 0) }
                    },
                    popEnterTransition = {
                        slideIn(tween(340, easing = LinearOutSlowInEasing)) { IntOffset(-it.width / 3, 0) }
                    },
                    popExitTransition = {
                        slideOut(tween(280, easing = FastOutLinearInEasing)) { IntOffset(it.width, 0) }
                    },
                ) {
                    composable("main") { MainScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                    composable("settings/theme") { ThemeSettingsScreen(navController) }
                    composable("settings/notification") { NotificationSettingsScreen(navController) }
                    composable("settings/workarounds") { WorkaroundsSettingsScreen(navController) }
                }
            }
        }
    }
}

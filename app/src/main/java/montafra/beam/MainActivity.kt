package montafra.beam

import android.Manifest.permission
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import montafra.beam.ui.MainScreen
import montafra.beam.ui.SettingsScreen
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
                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 1.10f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 1.10f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.92f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                    },
                ) {
                    composable("main") { MainScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                }
            }
        }
    }
}

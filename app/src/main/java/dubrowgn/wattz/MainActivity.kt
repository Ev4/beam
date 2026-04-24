package dubrowgn.wattz

import android.Manifest.permission
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dubrowgn.wattz.ui.MainScreen
import dubrowgn.wattz.ui.SettingsScreen
import dubrowgn.wattz.ui.theme.WattzTheme
import dubrowgn.wattz.ui.theme.rememberThemePrefs

const val namespace = "dubrowgn.wattz"
const val batteryDataReq = "$namespace.battery-data-req"
const val batteryDataResp = "$namespace.battery-data-resp"
const val intervalMs = 1_250L
const val noteChannelId = "$namespace.status.v2"
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
            WattzTheme(themePrefs) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                    exitTransition = { slideOutHorizontally { -it } + fadeOut() },
                    popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
                    popExitTransition = { slideOutHorizontally { it } + fadeOut() },
                ) {
                    composable("main") { MainScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                }
            }
        }
    }
}

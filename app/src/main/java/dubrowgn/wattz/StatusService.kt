package dubrowgn.wattz

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.graphics.drawable.Icon
import android.os.IBinder
import android.util.Log
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class StatusService : Service() {
    private lateinit var battery: Battery
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private var indicatorEntries: Set<String> = emptySet()
    private val metricOrder = listOf("A", "Ah", "C", "V", "Wh", "%")
    private lateinit var noteIntent: PendingIntent
    private lateinit var noteMgr: NotificationManager
    private var pluggedInAt: ZonedDateTime? = null
    private lateinit var snapshot: BatterySnapshot
    private val task = PeriodicTask({ update() }, intervalMs)

    private fun debug(msg: String) {
        Log.d(this::class.java.name, msg)
    }

    private inner class MsgReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                batteryDataReq -> updateData()
                settingsUpdateInd -> {
                    loadSettings()
                    update()
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    pluggedInAt = ZonedDateTime.now()
                    update()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    pluggedInAt = null
                    update()
                }
                Intent.ACTION_SCREEN_OFF -> task.stop()
                Intent.ACTION_SCREEN_ON -> task.start()
            }
        }
    }

    private fun loadSettings() {
        val settings = getSharedPreferences(settingsName, MODE_MULTI_PROCESS)
        battery.currentScalar = settings.getFloat("currentScalar", 1f).toDouble()
        battery.invertCurrent = settings.getBoolean("invertCurrent", false)
        indicatorEntries = settings.getStringSet("indicatorEntries", null) ?: emptySet()
    }

    private fun metricLabel(key: String) = getString(when (key) {
        "A"        -> R.string.current
        "Ah", "Wh" -> R.string.energy
        "C"        -> R.string.temperature
        "V"        -> R.string.voltage
        "%"        -> R.string.chargeLevel
        else       -> R.string.power
    })

    private fun metricValue(key: String) = fmt(when (key) {
        "A"  -> snapshot.amps
        "Ah" -> snapshot.energyAmpHours
        "C"  -> snapshot.celsius
        "V"  -> snapshot.volts
        "Wh" -> snapshot.energyWattHours
        "%"  -> snapshot.levelPercent
        else -> snapshot.watts
    })

    private fun metricUnit(key: String) = if (key == "C") "°C" else key

    private fun init() {
        battery = Battery(applicationContext)
        snapshot = battery.snapshot()

        noteMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        noteMgr.createNotificationChannel(
            NotificationChannel(
                noteChannelId,
                "Power Status",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Continuously displays current battery power consumption"
            }
        )

        noteIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        registerReceiver(
            MsgReceiver(),
            IntentFilter().apply {
                addAction(batteryDataReq)
                addAction(settingsUpdateInd)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            },
            RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        debug("onStartCommand()")

        super.onStartCommand(intent, flags, startId)

        init()
        loadSettings()
        task.start()

        try {
            startForeground(noteId, buildNotification())
        } catch (e: Exception) {
            error("Failed to foreground StatusService: ${e.message}")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        debug("onDestroy()")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun renderIcon(value: String, unit: String): Icon {
        val density = resources.displayMetrics.density
        val w = (48f * density).toInt()
        val bitmap = Bitmap.createBitmap(w, w, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            typeface = Typeface.DEFAULT_BOLD
            style = Paint.Style.FILL
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }

        val maxWidth = w * 0.92f
        paint.textSize = 40f * density
        val measured = paint.measureText(value)
        if (measured > maxWidth) paint.textSize *= maxWidth / measured
        canvas.drawText(value, w / 2f, w * 0.62f, paint)

        paint.textSize = 18f * density
        canvas.drawText(unit, w / 2f, w * 0.94f, paint)

        return Icon.createWithBitmap(bitmap)
    }

    private fun buildNotification(): Notification {
        val wattsValue = fmt(snapshot.watts)
        val timeText = when (val seconds = snapshot.secondsUntilCharged) {
            null -> ""
            0.0  -> getString(R.string.fullyCharged)
            else -> "${fmtSeconds(seconds)} until full charge"
        }

        val builder = Notification.Builder(this, noteChannelId)
            .setContentTitle("$wattsValue W")
            .setSmallIcon(renderIcon(wattsValue, "W"))
            .setContentIntent(noteIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        val entries = indicatorEntries.filter { it != "W" }.sortedBy { metricOrder.indexOf(it) }

        if (entries.isEmpty()) {
            builder.setStyle(null).setContentText(timeText)
        } else {
            val style = Notification.InboxStyle()
            entries.forEach { key ->
                style.addLine("${metricLabel(key)}  ${metricValue(key)}${metricUnit(key)}")
            }
            if (timeText.isNotEmpty()) style.setSummaryText(timeText)
            builder
                .setStyle(style)
                .setContentText(entries.joinToString("  ") { k -> "${metricValue(k)}${metricUnit(k)}" })
        }

        return builder.build()
    }

    private fun updateData() {
        val plugType = snapshot.plugType?.name?.lowercase()
        val indeterminate = getString(R.string.indeterminate)
        val fullyCharged = getString(R.string.fullyCharged)
        val no = getString(R.string.no)
        val yes = getString(R.string.yes)

        val intent = Intent()
            .setPackage(packageName)
            .setAction(batteryDataResp)
            .putExtra("charging",
                when (snapshot.charging) {
                    true -> if (plugType == null) yes else "$yes ($plugType)"
                    false -> no
                }
            )
            .putExtra("chargeLevel", fmt(snapshot.levelPercent) + "%")
            .putExtra("chargingSince",
                when (val pluggedInAt = pluggedInAt) {
                    null -> indeterminate
                    else -> LocalDateTime
                        .ofInstant(pluggedInAt.toInstant(), pluggedInAt.zone)
                        .format(dateFmt)
                }
            )
            .putExtra("current", fmt(snapshot.amps) + "A")
            .putExtra("energy",
                "${fmt(snapshot.energyWattHours)}Wh (${fmt(snapshot.energyAmpHours)}Ah)"
            )
            .putExtra("power", fmt(snapshot.watts) + "W")
            .putExtra("temperature", fmt(snapshot.celsius) + "°C")
            .putExtra("timeToFullCharge",
                when (val seconds = snapshot.secondsUntilCharged) {
                    null -> indeterminate
                    0.0 -> fullyCharged
                    else -> fmtSeconds(seconds)
                }
            )
            .putExtra("voltage", fmt(snapshot.volts) + "V")

        applicationContext.sendBroadcast(intent)
    }

    private fun update() {
        debug("update()")

        snapshot = battery.snapshot()
        noteMgr.notify(noteId, buildNotification())
        updateData()
    }
}

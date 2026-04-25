package montafra.beam

import android.os.Handler
import android.os.Looper

class PeriodicTask(private val callback: () -> Unit, private val intervalMs: Long) {
    private val ticker = Handler(Looper.getMainLooper())
    private val runnable = Runnable { start() }

    fun stop() {
        ticker.removeCallbacks(runnable)
    }

    private fun tick() {
        callback()
        ticker.postDelayed(runnable, intervalMs)
    }

    fun start() {
        stop()
        tick()
    }
}
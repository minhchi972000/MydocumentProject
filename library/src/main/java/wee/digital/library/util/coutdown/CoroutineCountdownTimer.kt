package wee.digital.library.util.coutdown

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

abstract class CoroutineCountdownTimer(private var intervalMillis: Long = 10000) {

    private var startTime: Long = 0
    private var job: Job? = null
    var isCountdown: Boolean = false
    var onFinished: (() -> Unit)? = null

    fun start(lifecycleOwner: LifecycleOwner) {
        start(lifecycleOwner.lifecycleScope)
    }

    open fun start(scope: CoroutineScope = CoroutineScope(Dispatchers.Main)) {
        cancel()
        startTime = System.currentTimeMillis()
        isCountdown = true
        job = scope.launch(Dispatchers.Main) {
            var remainMillis: Long
            do {
                remainMillis = System.currentTimeMillis() - startTime
                onTicks(intervalMillis + 500 - remainMillis)
                delay(50)
            } while (remainMillis <= intervalMillis + 500)
            isCountdown = false
            onCancel()
            onFinished?.invoke()
            onFinished()
        }
    }

    fun cancel() {
        job?.cancel(null)
        isCountdown = false
        onCancel()
    }

    abstract fun onTicks(remainMillis: Long)

    open fun onCancel() = Unit

    open fun onFinished() = Unit

}
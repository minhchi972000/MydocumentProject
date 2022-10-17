package wee.digital.library.util.coutdown

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger


abstract class NumberCountdownTimer {

    private val countdown = AtomicInteger(0)
    private val startValue: Int
    private val endValue: Int
    private val periodic: Long

    constructor(startValue: Int, endValue: Int = 0, periodic: Long = 1000) {
        this.startValue = startValue
        this.endValue = endValue
        this.periodic = periodic
    }

    private var job: Job? = null
    var isCountdown: Boolean = false
    var onFinished: (() -> Unit)? = null

    fun start(lifecycleOwner: LifecycleOwner) {
        start(lifecycleOwner.lifecycleScope)
    }

    open fun start(scope: CoroutineScope = CoroutineScope(Dispatchers.Main)) {
        cancel()
        countdown.set(startValue)
        isCountdown = true
        job = scope.launch(Dispatchers.Main) {
            do {
                onCountdown(countdown.get())
                delay(periodic)
            } while (countdown.decrementAndGet() >= endValue)
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

    abstract fun onCountdown(remain: Int)

    open fun onCancel() = Unit

    open fun onFinished() = Unit
}
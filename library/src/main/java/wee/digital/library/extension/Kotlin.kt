package wee.digital.library.extension

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

val isOnUiThread: Boolean get() = Looper.myLooper() == Looper.getMainLooper()

fun LifecycleOwner.onMain(interval: Long = 0, block: () -> Unit) {
    lifecycleScope.launch {
        if (interval > 0) {
            delay(interval)
        }
        withContext(Dispatchers.Main) {
            block()
        }
    }
}

fun onMain(interval: Long = 0, block: () -> Unit) {
    GlobalScope.launch(Dispatchers.Default) {
        if (interval > 0) {
            delay(interval)
        }
        withContext(Dispatchers.Main) {
            block()
        }
    }
}

typealias ResultLiveData<T> = SingleLiveData<Result<T>>

typealias ResultFlow<T> = Flow<Result<T>>

fun <T : Any> flowResult(block: suspend FlowCollector<Result<T>>.() -> T): Flow<Result<T>> {
    return flow {
        emit(runCatching { block() })
    }.flowOn(Dispatchers.IO)
}

fun CoroutineScope.repeat(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return this.launch(context, start) {
        while (true) kotlin.runCatching{
            block()
        }
    }
}

fun <T> ioFlow(block: () -> T?): Flow<T?> {
    return flow {
        try {
            emit(withContext(Dispatchers.IO) { block() })
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.Main)
}

fun <T> Flow<T>.onMain(block: (T) -> Unit) {
    onEach {
        block(it)
    }.launchIn(GlobalScope)
}

val String?.validTag: String
    get() {
        if (isNullOrEmpty()) return ""
        if (this.length > 23) {
            return this.substring(0, 23)
        }
        return this
    }

val KClass<*>.validTag: String get() = this.simpleName.validTag

fun sampleTryFlow() {
    flowResult<String> {
        "Hello World"
    }.onMain {
        toast(it.getOrNull())
    }
}

fun Closeable?.safeClose() {
    try {
        this?.close()
    } catch (ignored: Exception) {
    }
}

fun Number.toByteArray(): ByteArray {
    when (this) {
        is Long -> {
            return ByteBuffer.allocate(Long.SIZE_BYTES).putLong(this).array()
        }
        is Int -> {
            return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
        }
        is Double -> {
            return ByteBuffer.allocate(Double.SIZE_BYTES).putDouble(this).array()
        }
        is Float -> {
            return ByteBuffer.allocate(Float.SIZE_BYTES).putFloat(this).array()
        }
        is Short -> {
            return ByteBuffer.allocate(Short.SIZE_BYTES).putShort(this).array()
        }
        is Byte -> {
            return byteArrayOf(this)
        }
        else -> {
            return byteArrayOf()
        }
    }
}

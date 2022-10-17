package wee.digital.library.extension

import android.app.Activity
import android.os.Looper
import android.view.Window
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

inline fun <T> LiveData<T?>.observe(owner: LifecycleOwner, crossinline block: (t: T?) -> Unit) {
    this.observe(owner, Observer {
        block(it)
    })
}

/**
 * Live data only trigger when data change for multi observer
 */
open class SingleLiveData<T> : MediatorLiveData<T> {

    constructor() : super()

    constructor(value: T) : super() {
        this.value = value
    }

    private val observers = ConcurrentHashMap<LifecycleOwner, MutableSet<ObserverWrapper<T>>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper = ObserverWrapper(observer)
        val set = observers[owner]
        set?.apply {
            @Suppress("UNCHECKED_CAST")
            add(wrapper as ObserverWrapper<T>)
        } ?: run {
            val newSet = Collections.newSetFromMap(ConcurrentHashMap<ObserverWrapper<T>, Boolean>())
            @Suppress("UNCHECKED_CAST")
            newSet.add(wrapper as ObserverWrapper<T>?)
            observers[owner] = newSet
        }
        super.observe(owner, wrapper)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        observers.remove(owner)
        super.removeObservers(owner)
    }

    override fun removeObserver(observer: Observer<in T>) {
        observers.forEach {
            @Suppress("UNCHECKED_CAST")
            if (it.value.remove(observer as Observer<T>)) {
                if (it.value.isEmpty()) {
                    observers.remove(it.key)
                }
                return@forEach
            }
        }
        super.removeObserver(observer)
    }

    @MainThread
    override fun setValue(t: T?) {
        observers.forEach { it.value.forEach { wrapper -> wrapper.newValue() } }
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.setValue(t)
            } else {
                super.postValue(t)
            }
        } catch (e: Exception) {
            super.postValue(t)
        }
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

    val isFalse  : Boolean get() = value!=true

    fun clear(){
        value = null
    }

    private class ObserverWrapper<R>(private val observer: Observer<R>) : Observer<R> {

        private val pending = AtomicBoolean(false)

        override fun onChanged(t: R?) {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }

        fun newValue() {
            pending.set(true)
        }
    }

}

fun <R, T : LiveData<R>> T.single(): T {
    val result = SingleLiveData<R>()
    result.addSource(this) {
        @Suppress("UNCHECKED_CAST")
        result.value = it as R
    }
    @Suppress("UNCHECKED_CAST")
    return result as T
}

/**
 * Live data only trigger when data equal true
 */
class EventLiveData : MediatorLiveData<Boolean>() {

    val isTrue get() = value == true

    val isFalse get() = value != true

    private val observers = ConcurrentHashMap<LifecycleOwner, MutableSet<ObserverWrapper>>()

    fun hasEvent() {
        this.postValue(true)
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in Boolean>) {
        val wrapper = ObserverWrapper(observer)
        val set = observers[owner]
        set?.apply {
            @Suppress("UNCHECKED_CAST")
            add(wrapper)
        } ?: run {
            val newSet = Collections.newSetFromMap(ConcurrentHashMap<ObserverWrapper, Boolean>())
            @Suppress("UNCHECKED_CAST")
            newSet.add(wrapper as ObserverWrapper?)
            observers[owner] = newSet
        }
        super.observe(owner, wrapper)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        observers.remove(owner)
        super.removeObservers(owner)
    }

    override fun removeObserver(observer: Observer<in Boolean>) {
        observers.forEach {
            @Suppress("UNCHECKED_CAST")
            if (it.value.remove(observer as Observer<Boolean>)) {
                if (it.value.isEmpty()) {
                    observers.remove(it.key)
                }
                return@forEach
            }
        }
        super.removeObserver(observer)
    }

    @MainThread
    override fun setValue(t: Boolean?) {
        observers.forEach { it.value.forEach { wrapper -> wrapper.newValue() } }
        super.setValue(t)
    }

    inner class ObserverWrapper(private val observer: Observer<in Boolean>) : Observer<Boolean> {

        private val pending = AtomicBoolean(false)

        override fun onChanged(t: Boolean?) {
            if (pending.compareAndSet(true, false)) {
                if (t == true) {
                    observer.onChanged(t)
                }
            }
        }

        fun newValue() {
            pending.set(true)
        }
    }

}

/**
 * Live data only trigger when data change if value none null
 */
class NonNullLiveData<T> : MediatorLiveData<T>()

fun <T> NonNullLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let(observer)
    })
}

fun <T> LiveData<T?>.nonNull(): NonNullLiveData<T> {
    val mediator: NonNullLiveData<T> = NonNullLiveData()
    mediator.addSource(this) { data ->
        data?.let {
            mediator.value = data
        }
    }
    return mediator
}

fun <R, T : LiveData<R>> T.noneNull(): T {
    val result = NonNullLiveData<R>()
    result.addSource(this) {
        @Suppress("UNCHECKED_CAST")
        result.value = it as R
    }
    @Suppress("UNCHECKED_CAST")
    return result as T
}

/**
 *
 */
fun LifecycleOwner.requireActivity(): Activity? {
    return (this as? Fragment)?.requireActivity() ?: (this as? Activity)
}

fun LifecycleOwner.requireWindow(): Window? {
    return requireActivity()?.window
}

fun LifecycleOwner.onPause(block: () -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            block()
        }
    })
}

fun LifecycleOwner.onDestroy(block: () -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            block()
        }
    })
}

fun <T : ViewModel> ViewModelStoreOwner.viewModel(cls: KClass<T>): T {
    return ViewModelProvider(this).get(cls.java)
}

fun <T : ViewModel> ViewModelStoreOwner.lazyViewModel(cls: KClass<T>): Lazy<T> {
    return lazy { viewModel(cls) }
}

fun <T : ViewModel> Fragment.activityVM(cls: KClass<T>): T {
    return ViewModelProvider(requireActivity()).get(cls.java)
}

fun <T : ViewModel> Fragment.lazyActivityVM(cls: KClass<T>): Lazy<T> {
    return lazy { ViewModelProvider(requireActivity()).get(cls.java) }
}

fun <T : ViewModel> Fragment.newActivityVM(cls: KClass<T>): T {
    return ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[cls.java]
}








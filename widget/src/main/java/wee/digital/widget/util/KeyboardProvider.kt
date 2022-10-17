package wee.digital.widget.util

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.PopupWindow
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import wee.digital.widget.R


/**
 * -------------------------------------------------------------------------------------------------
 * Manifest:
 * <activity android:name=".ui.main.MainActivity"
 * android:windowSoftInputMode="adjustNothing"/>
 *
 * Fragment:
 * keyboardProvider = KeyboardProvider(activity!!)
 * view?.post { keyboardProvider!!.start() }
 *
 * Activity:
 * keyboardProvider = KeyboardProvider(this)
 * post { keyboardProvider!!.start() }
 * -------------------------------------------------------------------------------------------------
 */
class KeyboardProvider constructor(private val activity: Activity) : PopupWindow(activity) {

    interface KeyboardListener {
        fun onKeyboardShow(height: Int, orientation: Int)

        fun onKeyboardHide(orientation: Int)
    }

    private var keyboardListener: KeyboardListener? = null

    /** The cached landscape height of the keyboard  */
    private var landscapeHeight: Int = 0

    /** The cached portrait height of the keyboard  */
    private var portraitHeight: Int = 0

    private val popupView: View?

    private val parentView: View

    /**
     * Get the screen orientation
     *
     * @return the screen orientation
     */
    private val screenOrientation: Int
        get() = activity.resources.configuration.orientation

    init {

        val inflater = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.popupView = inflater.inflate(R.layout.keyboard_popup, null, false)
        contentView = popupView
        @Suppress("DEPRECATION")
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED

        parentView = activity.findViewById(android.R.id.content)

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT

        popupView!!.viewTreeObserver.addOnGlobalLayoutListener {
            handleOnGlobalLayout()
        }
    }

    /**
     * Start the KeyboardProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    fun observer(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                Handler(Looper.getMainLooper())
                    .postDelayed({
                        start()
                    }, 2000)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                close()
            }
        })
    }

    fun start() {
        if (!isShowing && parentView.windowToken != null) {
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    fun close() {
        this.keyboardListener = null
        dismiss()
    }

    /**
     * Popup window itself is as big as the window of the Activity.
     * The keyboard can then be calculated by extracting the popup view bottom
     * from the activity window height.
     */
    private fun handleOnGlobalLayout() {

        val screenSize = Point()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            screenSize.x = windowMetrics.bounds.width()
            screenSize.y = windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getSize(screenSize)
        }

        val rect = Rect()
        popupView!!.getWindowVisibleDisplayFrame(rect)

        val orientation = screenOrientation
        val keyboardHeight = screenSize.y - rect.bottom
        when {
            keyboardHeight == 0 -> {
                keyboardListener?.onKeyboardHide(orientation)
            }
            orientation == Configuration.ORIENTATION_PORTRAIT -> {
                if (portraitHeight == keyboardHeight) return
                portraitHeight = keyboardHeight
                keyboardListener?.onKeyboardShow(keyboardHeight, orientation)
            }
            else -> {
                if (landscapeHeight == keyboardHeight) return
                landscapeHeight = keyboardHeight
                keyboardListener?.onKeyboardShow(keyboardHeight, orientation)
            }
        }
    }
}
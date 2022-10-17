package wee.digital.sample.ui.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.library.extension.foreachCatching
import wee.digital.library.util.Logger
import kotlinx.coroutines.Job
import wee.digital.library.extension.validTag
import kotlin.reflect.KClass

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(),
    BaseView {

    protected val log: Logger by lazy { Logger(this::class) }

    val vb: VB by lazy {
        @Suppress("UNCHECKED_CAST")
        inflating().invoke(layoutInflater) as VB
    }

    abstract fun inflating(): (LayoutInflater) -> ViewBinding

    /**
     * [AppCompatActivity] implements
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        hideKeyboard()
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(vb.root)
        onViewCreated()
        onLiveDataObserve()
    }

    override fun onPause() {
        super.onPause()
        uiJobList.forEach { it.cancel(null) }
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_pop_enter, R.anim.activity_pop_exit)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        /*if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }*/
        return super.dispatchTouchEvent(event)
    }

    /**
     * [BaseView] implements
     */
    final override val baseActivity: BaseActivity<*>? get() = this

    final override val lifecycleOwner: LifecycleOwner get() = this

    final override val uiJobList: MutableList<Job> = mutableListOf()

    /**
     * Keyboard utils
     */
    override fun hideKeyboard() {
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
    }

    override fun showKeyboard() {
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.ime())
    }

    protected fun inputModeAdjustResize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            vb.root.setOnApplyWindowInsetsListener { _, windowInsets ->
                val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                vb.root.setPadding(0, 0, 0, imeHeight)
                windowInsets
            }
        } else {
            @Suppress("DEPRECATION")
            baseActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    protected fun inputModeAdjustNothing() {
        baseActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    /**
     * SupportFragmentManager
     */
    private fun <T : Fragment> FragmentManager.findFragment(cls: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return this.fragments.find { it::class.simpleName == cls.simpleName } as? T?
    }

    private fun <T : Fragment> FragmentManager.findFragment(fragment: T): T? {
        return findFragment(fragment::class)
    }

    /**
     * Dialog util
     */
    private var dialogAnimationTag: String? = null

    fun showAlertDialog(block: (AlertDialog.Builder.() -> Unit)? = null) {
        val dialog = AlertDialog.Builder(this)
        block?.invoke(dialog)
        dialog.create().show()
    }

    fun show(dialog: DialogFragment, tag: String? = null) {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            try {
                // return if [LockFragment] is animate show
                val sTag = tag?.validTag ?: dialog::class.validTag
                when (dialogAnimationTag) {
                    sTag -> {
                        return@launchWhenResumed
                    }
                }
                val sfm = (baseActivity as? FragmentActivity)?.supportFragmentManager
                    ?: throw NullPointerException("supportFragmentManager not found")
                val existFragment: Fragment? = if (!tag.isNullOrEmpty()) {
                    sfm.findFragmentByTag(tag.validTag)
                } else {
                    sfm.findFragment(dialog)
                }
                if (existFragment != null) {
                    throw NullPointerException("${dialog::class.simpleName} dialog was shown, tag: ${tag?.validTag}")
                }
                dialogAnimationTag = sTag
                launch(400) {
                    dialogAnimationTag = null
                }
                dialog.show(sfm, sTag)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    fun dismissDialog(tag: String) {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            try {
                val sfm = (baseActivity as? FragmentActivity)?.supportFragmentManager
                    ?: throw NullPointerException("supportFragmentManager not found")
                val fragment = sfm.findFragmentByTag(tag.validTag) as? DialogFragment
                fragment?.dialog?.dismiss()
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    fun dismissAllDialogs() {
        try {
            val sfm = this.supportFragmentManager
            sfm.fragments.foreachCatching {
                if (it is DialogFragment) {
                    it.dismissAllowingStateLoss()
                }
            }
        } catch (e: Exception) {

        }
    }

    fun dismissAllExcept(fragment: Fragment) {
        try {
            val fragmentName = fragment::class.java.name
            val sfm = (baseActivity as? FragmentActivity)?.supportFragmentManager ?: return
            sfm.fragments.foreachCatching {
                if (it is DialogFragment && it::class.java.name != fragmentName) {
                    it.dismissAllowingStateLoss()
                }
            }
        } catch (ex: Exception) {
            log.d(ex.message.toString())
        }
    }

}
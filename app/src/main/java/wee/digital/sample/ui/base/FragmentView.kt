package wee.digital.sample.ui.base

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import wee.digital.sample.log
import wee.digital.library.extension.foreachCatching
import wee.digital.library.extension.hideKeyboard
import wee.digital.library.extension.showKeyboard

interface FragmentView : BaseView {

    val fragment: Fragment get() = this as Fragment

    /**
     * [BaseView] implements
     */
    override val baseActivity: BaseActivity<*>? get() = fragment.requireActivity() as? BaseActivity<*>

    override val lifecycleOwner: LifecycleOwner get() = fragment

    override fun activityNavController(): NavController? {
        return baseActivity?.activityNavController()
    }

    /**
     * Keyboard utils
     */
    override fun hideKeyboard() {
        fragment.requireActivity().hideKeyboard()
    }

    override fun showKeyboard() {
        fragment.requireActivity().showKeyboard()
    }

    fun inputModeAdjustResize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fragment.view?.setOnApplyWindowInsetsListener { _, windowInsets ->
                val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                fragment.view?.setPadding(0, 0, 0, imeHeight)
                windowInsets
            }
        } else {
            @Suppress("DEPRECATION")
            baseActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    fun inputModeAdjustNothing() {
        baseActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    fun onCreateView() = Unit

    /**
     * [FragmentView] utils
     */
    fun <T : ViewBinding> viewBinding(block: (LayoutInflater) -> ViewBinding): Lazy<T> {
        return lazy {
            @Suppress("UNCHECKED_CAST")
            block.invoke(fragment.layoutInflater) as T
        }
    }

    fun requestFocus(v: View?) {
        launch(240) { v?.requestFocus() }
    }

    /**
     * Back press handle
     */
    val backPressedCallback: OnBackPressedCallback

    fun getBackPressCallBack(): OnBackPressedCallback {
        return object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }

    fun onBackPressed()

    /**
     * LifecycleScope
     */
    val lifecycleScope get() = fragment.lifecycleScope

    fun addObserver(observer: LifecycleObserver) {
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    /**
     * Navigation
     */
    fun childNavigate(@IdRes actionId: Int, block: (NavBuilder.() -> Unit)? = null) {
        fragment.findNavController().navigate(actionId, block)
    }

    fun childPopBackStack(@IdRes fragmentId: Int = 0, inclusive: Boolean = false) {
        if (fragmentId != 0) {
            fragment.findNavController().popBackStack(fragmentId, inclusive)
        } else {
            fragment.findNavController().popBackStack()
        }
    }

    fun childRemoveFragments(@IdRes vararg actionId: Int) {
        fragment.findNavController().removeFragments(*actionId)
    }

    fun mainNavigate(@IdRes actionId: Int, block: (NavBuilder.() -> Unit)? = null) {
        activityNavController()?.navigate(actionId, block)
    }

    fun mainRemoveFragments(@IdRes vararg actionId: Int) {
        activityNavController()?.removeFragments(*actionId)
    }

    fun mainPopBackStack(@IdRes fragmentId: Int = 0, inclusive: Boolean = false) {
        if (fragmentId != 0) {
            activityNavController()?.popBackStack(fragmentId, inclusive)
        } else {
            activityNavController()?.popBackStack()
        }
    }

    /**
     * Dialog utils
     */
    fun showAlertDialog(block: (AlertDialog.Builder.() -> Unit)? = null) {
        baseActivity?.showAlertDialog(block)
    }

    fun show(dialog: DialogFragment, tag: String? = null) {
        baseActivity?.show(dialog, tag)
    }

    fun dismissDialog(tag: String) {
        baseActivity?.dismissDialog(tag)
    }

    fun dismissAllExceptSelf() {
        baseActivity?.dismissAllExcept(fragment)
    }

    fun dismissAllDialogs() {
        baseActivity?.dismissAllDialogs()
    }

}
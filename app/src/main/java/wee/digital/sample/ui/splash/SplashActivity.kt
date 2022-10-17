package wee.digital.sample.ui.splash

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wee.digital.library.extension.darkSystemWidgets
import wee.digital.library.extension.start
import wee.digital.sample.databinding.SplashBinding
import wee.digital.sample.ui.base.BaseActivity
import wee.digital.sample.ui.main.MainActivity

/**
 * Splash
 * @see <a href="">Document</a>
 * @see <a href="">Design</a>
 */
class SplashActivity : BaseActivity<SplashBinding>() {

    override fun inflating(): (LayoutInflater) -> ViewBinding {
        return SplashBinding::inflate
    }

    override fun onViewCreated() {
        darkSystemWidgets()
        lifecycleScope.launch {
            delay(300)
            start(MainActivity::class)
            finish()
        }
    }

    override fun onLiveDataObserve() {

    }

}
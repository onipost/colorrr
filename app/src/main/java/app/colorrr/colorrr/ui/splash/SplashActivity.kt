package app.colorrr.colorrr.ui.splash

import android.content.Intent
import android.os.Bundle
import app.colorrr.colorrr.R
import kotlinx.android.synthetic.main.activity_splash.*
import android.view.animation.LinearInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseActivity
import app.colorrr.colorrr.ui.login.LoginActivity
import app.colorrr.colorrr.ui.main.MainActivity

class SplashActivity : BaseActivity<SplashViewModel>(), SplashInterface {
    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.runAnimation()
        this.viewModel.setListener(this)
        this.viewModel.startLoad()
    }

    override fun getLayoutID(): Int {
        return R.layout.activity_splash
    }

    override fun getViewModel(): SplashViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = SplashViewModel()

        return this.viewModel
    }

    override fun openLoginScreen() {
        startActivity(Intent(applicationContext, LoginActivity::class.java))
    }

    override fun openMainScreen() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }

    override fun onEmptyRepo() {
        Utils.showAlertDialogInt(this, R.string.Error, R.string.unfortunately_youre_not_connected_to_internet_and_we_dont_have_any_data_cached_on_your_device_so_wer, R.string.Ok)
    }

    private fun runAnimation() {
        val rotate = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 15000
        rotate.interpolator = LinearInterpolator()
        rotate.repeatCount = Animation.INFINITE
        loaderFlower.startAnimation(rotate)
    }
}

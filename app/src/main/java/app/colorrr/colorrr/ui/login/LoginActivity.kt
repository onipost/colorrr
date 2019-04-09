package app.colorrr.colorrr.ui.login

import android.content.Intent
import android.os.Bundle
import app.colorrr.colorrr.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.login_create.LoginCreateActivity
import app.colorrr.colorrr.ui.login_email.LoginEmailActivity
import app.colorrr.colorrr.ui.main.MainActivity

class LoginActivity : BaseActivity<LoginViewModel>(), LoginInterface {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewModel.setListener(this)
        this.viewModel.getTos()

        login_facebook.onClick {
            showLoader(R.string.signing_in_with_facebook)
            viewModel.signUpFacebook(this@LoginActivity)
        }

        login_skip.onClick {
            showLoader(R.string.creating_account)
            viewModel.signUpAnonymous()
        }
        login_create.onClick { startActivity(Intent(applicationContext, LoginCreateActivity::class.java)) }
        login_email.onClick { startActivity(Intent(applicationContext, LoginEmailActivity::class.java)) }
        tos.onClick { animateTosWindow(false) }
        tos_close.onClick { animateTosWindow(true) }
    }

    override fun onBackPressed() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun getLayoutID(): Int {
        return R.layout.activity_login
    }

    override fun getViewModel(): LoginViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = LoginViewModel()

        return this.viewModel
    }

    override fun onFacebookLoginSuccess() {
        this.hideLoader()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onFacebookLoginCancel() {
        this.hideLoader()
    }

    override fun onFacebookLoginError(error: String) {
        this.hideLoader()
        Utils.showAlertDialogString(this, R.string.Error, error, R.string.Ok)
    }

    override fun onSkip(success: Boolean, error: String) {
        this.hideLoader()
        if (success)
            startActivity(Intent(applicationContext, MainActivity::class.java))
        else
            Utils.showAlertDialogString(this, R.string.Error, error, R.string.Ok)
    }

    override fun onGetTos(data: String) {
        tos_content.text = data
    }

    private fun animateTosWindow(reverse: Boolean) {
        val mDuration = 300L
        AnimationWorker.animateOverflowFade(tos_stub, reverse, mDuration)
        if (!reverse)
            AnimationWorker.animateHeight(tos_window, 0, Utils.getWindowHeight(applicationContext), mDuration)
        else
            AnimationWorker.animateHeight(tos_window, Utils.getWindowHeight(applicationContext), 0, mDuration)
    }
}
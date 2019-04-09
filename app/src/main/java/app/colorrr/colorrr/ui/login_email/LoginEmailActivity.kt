package app.colorrr.colorrr.ui.login_email

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.ui.base.BaseActivity
import app.colorrr.colorrr.ui.main.MainActivity
import kotlinx.android.synthetic.main._toolbar.*
import kotlinx.android.synthetic.main.activity_login_email.*

class LoginEmailActivity : BaseActivity<LoginEmailViewModel>(), LoginEmailInterface {
    private lateinit var viewModel: LoginEmailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewModel.setListener(this)
        setSupportActionBar(toolbar)
        toolbar_text.text = applicationContext.getString(R.string.email_login)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        lost_password.setOnClickListener {
            this.hideKeyboard()
            this.showLoader(R.string.resetting_password)
            val email = email.text.toString()
            this.viewModel.lostPassword(email)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.confirm_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.confirm) {
            this.hideKeyboard()
            this.showLoader(R.string.logging_in)
            this.viewModel.login(email.text.toString(), password.text.toString())
        }
        return true
    }

    override fun onLogin(result: Boolean, message: String) {
        this.hideLoader()
        if (result) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        } else {
            Utils.showAlertDialogString(this, R.string.Error, message, R.string.Ok)
        }
    }

    override fun onLostPassword(result: Boolean, message: String) {
        this.hideLoader()
        if (result) {
            val res =
                this.getString(R.string.well_send_email_with_instructions_to_email_in_case_it_associated_with_some_account)
                    .replace("%s", email.text.toString())
            Utils.showAlertDialogString(this, R.string.Success, res, R.string.Ok)
        } else {
            Utils.showAlertDialogString(this, R.string.Error, message, R.string.Ok)
        }
    }

    override fun getLayoutID(): Int {
        return R.layout.activity_login_email
    }

    override fun getViewModel(): LoginEmailViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = LoginEmailViewModel()

        return this.viewModel
    }
}
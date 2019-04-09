package app.colorrr.colorrr.ui.login_email

import android.text.TextUtils
import app.colorrr.colorrr.App
import app.colorrr.colorrr.R
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.entity.Session
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.ui.base.BaseViewModel
import app.colorrr.colorrr.api.ApiParser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class LoginEmailViewModel : BaseViewModel<LoginEmailInterface>() {
    private var app = App.getInstance()
    private var loadLock: Boolean = false

    fun login(email: String, password: String) {
        if (this.loadLock)
            return

        if (!this.validation(email, password))
            return

        if (!app.checkInternetConnection()) {
            getListener()?.onLogin(false, app.getString(R.string.no_internet_connection))
            return
        }

        this.loadLock = true
        val disposable = ApiWorker.workerUser.login(email, password)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    doAsync {
                        app.repository.sessionRepository.refreshUser(it["user"] as UserCurrent)
                        app.repository.sessionRepository.refreshSessionInDb(it["session"] as Session)
                        uiThread {
                            getListener()?.onLogin(true, "")
                            loadLock = false
                        }
                    }
                },
                onError = {
                    val message = if (it.message == "3.0")
                        app.getString(R.string.no_account_found_with_provided_email_and_password_combination_please_make_sure_youve_entered_correct)
                    else
                        ApiParser.parseError(it)

                    getListener()?.onLogin(false, message)
                    this.loadLock = false
                }
            )
    }

    fun lostPassword(email: String) {
        if (this.loadLock)
            return

        if (!this.validateEmail(email)) {
            getListener()?.onLostPassword(false, app.getString(R.string.please_enter_valid_email_address))
            return
        }

        if (!app.checkInternetConnection()) {
            getListener()?.onLostPassword(false, app.getString(R.string.no_internet_connection))
            return
        }

        this.loadLock = true
        val disposable = ApiWorker.workerUser.resetPassword(email)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    getListener()?.onLostPassword(true, "")
                    this.loadLock = false
                },
                onError = {
                    val message = ApiParser.parseError(it)
                    getListener()?.onLostPassword(false, message)
                    this.loadLock = false
                }
            )
    }

    private fun validation(email: String, password: String): Boolean {
        if (!this.validateEmail(email)) {
            getListener()?.onLogin(false, app.getString(R.string.please_enter_valid_email_address))
            return false
        }

        if (password.length < 6) {
            getListener()?.onLogin(false, app.getString(R.string.please_enter_valid_password_it_must_be_at_least_6_characters_length))
            return false
        }

        return true
    }

    private fun validateEmail(target: String): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}
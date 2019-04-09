package app.colorrr.colorrr.ui.login

interface LoginInterface {
    fun onFacebookLoginSuccess()

    fun onFacebookLoginCancel()

    fun onFacebookLoginError(error: String)

    fun onSkip(success: Boolean, error: String)

    fun onGetTos(data: String)
}
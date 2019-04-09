package app.colorrr.colorrr.ui.login_email

interface LoginEmailInterface {
    fun onLogin(result: Boolean, message: String)

    fun onLostPassword(result: Boolean, message: String)
}
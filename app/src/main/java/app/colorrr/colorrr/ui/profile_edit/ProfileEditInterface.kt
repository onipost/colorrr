package app.colorrr.colorrr.ui.profile_edit

interface ProfileEditInterface {
    fun onUserUpdated()

    fun onUpdateUserError(error: String)
}
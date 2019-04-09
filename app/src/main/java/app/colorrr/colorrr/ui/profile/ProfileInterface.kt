package app.colorrr.colorrr.ui.profile

import app.colorrr.colorrr.entity.UserCurrent

interface ProfileInterface {
    fun onProfileArrived(user: UserCurrent?, currentUserID: Int)
}
package app.colorrr.colorrr.ui.profile_followers

import app.colorrr.colorrr.entity.Follower

interface ProfileFollowersInterface {
    fun onFollowersArrived(refreshList: Boolean, data: List<Follower>)

    fun onError(error: String)
}
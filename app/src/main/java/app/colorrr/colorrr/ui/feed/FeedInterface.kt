package app.colorrr.colorrr.ui.feed

import app.colorrr.colorrr.entity.Feed

interface FeedInterface {
    fun onDataArrived(refreshList: Boolean, data: List<Feed>)

    fun onFollowAndLike(success: Boolean, error: String)

    fun onReport(success: Boolean, error: String)

    fun onDelete(position: Int, success: Boolean, error: String)
}
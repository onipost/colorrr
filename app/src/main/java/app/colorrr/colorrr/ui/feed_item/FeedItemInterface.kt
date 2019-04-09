package app.colorrr.colorrr.ui.feed_item

import app.colorrr.colorrr.entity.Comment
import app.colorrr.colorrr.entity.Feed

interface FeedItemInterface {
    fun onPostArrived(post: Feed)

    fun onCommentsArrived(refreshList: Boolean, list: List<Comment>)

    fun onLike(success: Boolean, error: String)

    fun onFollow(success: Boolean, error: String)

    fun onReport(success: Boolean, message: String)

    fun onDelete(success: Boolean, message: String)

}

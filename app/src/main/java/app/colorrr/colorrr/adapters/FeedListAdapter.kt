package app.colorrr.colorrr.adapters

import android.app.Activity
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Feed
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.GlideLoader
import app.colorrr.colorrr.system.Utils

class FeedListAdapter(
    private val activity: Activity,
    var data: ArrayList<Feed>,
    private var userID: Int,
    private var openPostClickListener: ItemClickListener,
    private var followClickListener: ItemClickListener,
    private var likeClickListener: ItemClickListener,
    private var reportClickListener: ItemClickListener,
    private var continueClickListener: ItemClickListener,
    private var deleteClickListener: ItemClickListener,
    private var openProfileListener: ItemClickListener,
    private var loadMoreListener: LoadMoreListener
) : RecyclerView.Adapter<FeedListAdapter.ViewHolder>() {

    fun addData(list: List<Feed>) {
        val list2: ArrayList<Feed> = ArrayList()
        list.forEach { list2.add(it) }
        //val list2: ArrayList<Feed> = list.filter { it.id != 0 }
        for (i in 0 until this.data.size) {
            val dataItem = this.data[i]
            val filtered = list2.filter { it.id == dataItem.id }
            if (filtered.isNotEmpty()) {
                this.data[i] = filtered[0]
            }
            list2.removeAll(filtered)
        }

        this.data.addAll(list2)
        this.notifyDataSetChanged()
    }

    fun clearData() {
        val dataSetSize = this.data.size
        this.data.clear()
        this.notifyItemRangeRemoved(0, dataSetSize)
    }

    fun removeItem(position: Int) {
        this.data.removeAt(position)
        this.notifyItemRemoved(position)
        this.notifyItemRangeChanged(position, this.data.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity.applicationContext).inflate(R.layout.item_feed, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        val animator = AnimationWorker.animateRotateInfinite(holder.mPlaceholder, AnimationWorker.LOAD_PLACEHOLDER)
        GlideLoader.load(
            this.activity.applicationContext,
            item.linkPreview,
            holder.mPostPreview,
            holder.mPlaceholder,
            animator
        )
        holder.mPostTime.text = Utils.getTimeAgo(item.timestamp)
        holder.mPostLikesCount.text = item.likesCount.toString()
        holder.mPostCommentsCount.text = item.commentsCount.toString()

        holder.mPostLiked.setImageResource(if (item.liked == 1) R.drawable.like else R.drawable.button_like)
        holder.mFollow.setImageResource(if (item.followed == 1) R.drawable.follow_ok else R.drawable.follow)

        holder.mFollow.tag = item.userID
        holder.mUserName.text = item.userName
        if (item.userAvatar != "")
            GlideLoader.load(this.activity.applicationContext, item.userAvatar, holder.mUserAvatar)
        else
            holder.mUserAvatar.setImageResource(R.drawable.mask2)

        holder.mUserAvatar.setOnClickListener { this.openProfileListener.onClick(position, item) }
        holder.mPostPreview.setOnClickListener { this.openPostClickListener.onClick(position, item) }
        holder.mComment.setOnClickListener { this.openPostClickListener.onClick(position, item) }
        holder.mFollow.setOnClickListener { this.followClickListener.onClick(position, item) }
        holder.mPostLiked.setOnClickListener { this.likeClickListener.onClick(position, item) }
        holder.mInfo.setOnClickListener { view ->
            val popup = PopupMenu(this.activity, view)
            popup.inflate(if (userID != item.userID) R.menu.feed_item_menu else R.menu.feed_item_user_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.report -> {
                        reportClickListener.onClick(position, item)
                        true
                    }
                    R.id.continue_draw -> {
                        continueClickListener.onClick(position, item)
                        true
                    }
                    R.id.delete -> {
                        deleteClickListener.onClick(position, item)
                        true
                    }
                    else -> true
                }
            }
            popup.show()
        }

        holder.mPostReColor.setOnClickListener {
            //TODO handle
        }

        if (position == data.size - 1)
            this.loadMoreListener.onLoad()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mPostTime: TextView = itemView.findViewById(R.id.post_time) as TextView
        var mPostPreview: ImageView = itemView.findViewById(R.id.post_preview) as ImageView
        var mPostLiked: ImageView = itemView.findViewById(R.id.post_liked) as ImageView
        var mPostLikesCount: TextView = itemView.findViewById(R.id.post_likes_count) as TextView
        var mPostCommentsCount: TextView = itemView.findViewById(R.id.post_comments_count) as TextView
        var mPostReColor: ImageView = itemView.findViewById(R.id.post_recolor) as ImageView

        var mUserName: TextView = itemView.findViewById(R.id.user_name) as TextView
        var mUserAvatar: ImageView = itemView.findViewById(R.id.user_avatar) as ImageView
        var mFollow: ImageView = itemView.findViewById(R.id.follow) as ImageView
        var mPlaceholder: ImageView = itemView.findViewById(R.id.placeholder) as ImageView

        var mInfo: ImageView = itemView.findViewById(R.id.info) as ImageView
        var mComment: LinearLayout = itemView.findViewById(R.id.comments) as LinearLayout
    }

    interface ItemClickListener {
        fun onClick(position: Int, item: Feed)
    }

    interface LoadMoreListener {
        fun onLoad()
    }
}
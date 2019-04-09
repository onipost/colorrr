package app.colorrr.colorrr.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Follower
import app.colorrr.colorrr.system.GlideLoader
import app.colorrr.colorrr.system.Utils
import org.jetbrains.anko.backgroundResource

class FollowersListAdapter(
    val context: Context,
    var data: ArrayList<Follower>,
    private var userID: Int,
    private var openProfileListener: ItemClickListener,
    private var followClickListener: ItemClickListener,
    private var loadMoreListener: LoadMoreListener
) : RecyclerView.Adapter<FollowersListAdapter.ViewHolder>() {

    fun addData(list: List<Follower>) {
        val list2: ArrayList<Follower> = ArrayList()
        list.forEach { list2.add(it) }
        for (i in 0 until this.data.size) {
            val dataItem = this.data[i]
            val filtered = list2.filter { it.userID == dataItem.userID }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.context).inflate(R.layout.item_follower, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.mUserName.text = item.userName
        if (item.userAvatar != "")
            GlideLoader.load(this.context, item.userAvatar, holder.mUserAvatar)
        else
            holder.mUserAvatar.setImageResource(R.drawable.mask2)

        holder.mUserAvatar.setOnClickListener { this.openProfileListener.onClick(position, item) }

        if (item.isFollowed == 1) {
            holder.mButton.setTextColor(Utils.getColorStateList(this.context, R.color.button_white_text))
            holder.mButton.backgroundResource = R.drawable.button_colored2
            holder.mButton.text = this.context.getString(R.string.Unfollow)
        } else {
            holder.mButton.setTextColor(Utils.getColorStateList(this.context, R.color.button_colored_text))
            holder.mButton.backgroundResource = R.drawable.button_white2
            holder.mButton.text = this.context.getString(R.string.Follow)
        }

        holder.mButton.visibility = if (item.userID == this.userID) View.GONE else View.VISIBLE
        holder.mButton.setOnClickListener { this.followClickListener.onClick(position, item) }

        if (position == data.size - 2)
            this.loadMoreListener.onLoad()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mButton: Button = itemView.findViewById(R.id.follow) as Button
        var mUserName: TextView = itemView.findViewById(R.id.user_name) as TextView
        var mUserAvatar: ImageView = itemView.findViewById(R.id.user_avatar) as ImageView
    }

    interface ItemClickListener {
        fun onClick(position: Int, item: Follower)
    }

    interface LoadMoreListener {
        fun onLoad()
    }
}
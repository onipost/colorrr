package app.colorrr.colorrr.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Image
import app.colorrr.colorrr.entity.ImagePublished
import app.colorrr.colorrr.entity.ImageUnfinished
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.GlideLoader

class ProfileImagesListAdapter(private var c: Context, private var data: ArrayList<Image>, private var loadMore: ProfileImageLoadMoreListener) :
    RecyclerView.Adapter<ProfileImagesListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(c).inflate(R.layout.item_profile_image, parent, false))
    }

    override fun getItemCount(): Int {
        return this.data.size
    }

    override fun onBindViewHolder(holder: ProfileImagesListAdapter.ViewHolder, position: Int) {
        val item = data[position]
        val animator = AnimationWorker.animateRotateInfinite(holder.mPlaceholder, AnimationWorker.LOAD_PLACEHOLDER)
        if (item is ImagePublished) {
            GlideLoader.load(c, item.linkPreview, holder.mCover, holder.mPlaceholder, animator)
            holder.mPublished.visibility = View.VISIBLE
        }

        if (item is ImageUnfinished) {
            GlideLoader.load(c, item.linkPreview, holder.mCover, holder.mPlaceholder, animator)
            holder.mPublished.visibility = View.GONE
        }

        holder.mContainer.setOnClickListener {  }

        if (position == this.data.size - 4)
            this.loadMore.onNeedMore()
    }

    inner class ViewHolder(item: View): RecyclerView.ViewHolder(item) {
        var mContainer: RelativeLayout = itemView.findViewById(R.id.container) as RelativeLayout
        var mCover: ImageView = itemView.findViewById(R.id.cover) as ImageView
        var mPlaceholder: ImageView = itemView.findViewById(R.id.placeholder) as ImageView
        var mPublished: ImageView = itemView.findViewById(R.id.published) as ImageView
    }

    fun addData(list: ArrayList<Image>) {
        this.data.addAll(list)
        this.notifyDataSetChanged()
    }

    interface ProfileImageLoadMoreListener {
        fun onNeedMore()
    }
}
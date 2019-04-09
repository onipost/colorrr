package app.colorrr.colorrr.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.ImageToCategory
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.GlideLoader

class CategoryImagesListAdapter(
    val context: Context,
    var data: ArrayList<ImageToCategory>,
    private var listener: View.OnClickListener
) :
    RecyclerView.Adapter<CategoryImagesListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_categories_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = data[position]

        holder.mPremium.visibility = if (image.premium_image == 1) View.VISIBLE else View.GONE
        val imageLink = image.unfinishedLinkPreview ?: image.link_preview

        val animator = AnimationWorker.animateRotateInfinite(holder.mPlaceholder, AnimationWorker.LOAD_PLACEHOLDER)
        GlideLoader.load(context, imageLink, holder.mCover, holder.mPlaceholder, animator)
        holder.mCoverContainer.tag = image.id
        holder.mCoverContainer.setOnClickListener(listener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mCoverContainer: RelativeLayout = itemView.findViewById(R.id.cover_container) as RelativeLayout
        var mCover: ImageView = itemView.findViewById(R.id.cover) as ImageView
        var mPremium: ImageView = itemView.findViewById(R.id.premium) as ImageView
        var mPlaceholder: ImageView = itemView.findViewById(R.id.placeholder) as ImageView
    }
}
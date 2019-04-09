package app.colorrr.colorrr.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Category
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.GlideLoader

class FeaturedCategoriesAdapter(val context: Context, var data: ArrayList<Category>, private var listener: View.OnClickListener) :
    RecyclerView.Adapter<FeaturedCategoriesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_featured_categories_item, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = data[position]

        val animator = AnimationWorker.animateRotateInfinite(holder.mPlaceholder, AnimationWorker.LOAD_PLACEHOLDER)
        GlideLoader.load(context, category.cover_image, holder.mCover, holder.mPlaceholder, animator)
        holder.mTitle.text = category.name
        holder.mCoverContainer.tag = category.id
        holder.mCoverContainer.setOnClickListener(listener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTitle: TextView = itemView.findViewById(R.id.title) as TextView
        var mCover: ImageView = itemView.findViewById(R.id.cover) as ImageView
        var mCoverContainer: RelativeLayout = itemView.findViewById(R.id.cover_container) as RelativeLayout
        var mPlaceholder: ImageView = itemView.findViewById(R.id.placeholder) as ImageView
    }
}
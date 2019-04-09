package app.colorrr.colorrr.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.ImageOriginal
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ImageItemView constructor(
    c: Context,
    private var itemWidth: Int,
    attrs: AttributeSet? = null, defStyle: Int = 0
) : RelativeLayout(c, attrs, defStyle) {
    private var mView: View? = null
    private var mCover: ImageView? = null
    private var mPremium: ImageView? = null
    private var mListener: ImageItemClickListener? = null

    init {
        this.init(c)
    }

    private fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.mView = inflater.inflate(R.layout.item_images_list, this)
        this.mView?.let {
            val ll = RelativeLayout.LayoutParams(itemWidth, itemWidth)
            it.layoutParams = ll
            this.mCover = it.findViewById(R.id.cover)
            this.mPremium = it.findViewById(R.id.premium)
        }

    }

    fun setData(data: ImageOriginal) {
        this.mCover?.let {
            Glide.with(context).load(data.link_preview).transition(DrawableTransitionOptions.withCrossFade())
                .into(it)//.apply(requestOption).into(holder.mCover)
            it.tag = data.id
            it.setOnClickListener { view -> mListener?.onImageClick(view.tag as Int) }
        }
        mPremium?.visibility = if (data.premium_image == 1) View.VISIBLE else View.GONE
    }

    fun setListener(listener: ImageItemClickListener) {
        this.mListener = listener
    }

    interface ImageItemClickListener {
        fun onImageClick(id: Int)
    }
}
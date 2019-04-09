package app.colorrr.colorrr.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.ImageToCategory
import android.widget.*
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.GlideLoader
import org.jetbrains.anko.layoutInflater

class ImagesListAdapter(private var c: Context, private var data: ArrayList<ImageToCategory>) :
    ArrayAdapter<ImageToCategory>(c, R.layout.item_images_list) {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): ImageToCategory? {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return data[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = data[position]
        val mView: View = convertView ?: c.layoutInflater.inflate(R.layout.item_images_list, parent, false)
        mView.layoutParams = RelativeLayout.LayoutParams(parent.width / 2, parent.width / 2)

        val mRoot = mView.findViewById<RelativeLayout>(R.id.root)
        val mCover = mView.findViewById<ImageView>(R.id.cover)
        val mPremium = mView.findViewById<ImageView>(R.id.premium)
        val mPlaceholder = mView.findViewById<ImageView>(R.id.placeholder)

        mRoot.tag = item.id
        mPremium.visibility = if (item.premium_image == 1) View.VISIBLE else View.GONE

        val animator = AnimationWorker.animateRotateInfinite(mPlaceholder, AnimationWorker.LOAD_PLACEHOLDER)
        GlideLoader.load(context, item.link_preview, mCover, mPlaceholder, animator)

        return mView
    }
}
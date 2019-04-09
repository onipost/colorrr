package app.colorrr.colorrr.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.FeaturedCategoriesAdapter
import app.colorrr.colorrr.entity.Category

class FeaturedCategoriesView : LinearLayout {
    private var mView: View? = null
    private var mRecyclerView: RecyclerView? = null

    private var listener: ItemClickListener? = null
    private var adapter: FeaturedCategoriesAdapter? = null
    private var data: ArrayList<Category> = ArrayList()

    constructor(context: Context) : super(context) {
        this.init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.init(context)
    }

    fun setData(data: ArrayList<Category>) {
        this.data.clear()
        this.data.addAll(data)
        this.adapter?.notifyDataSetChanged()
    }

    fun setListener(listener: ItemClickListener) {
        this.listener = listener
    }

    private fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.mView = inflater.inflate(R.layout.view_featured_categories, this)
        this.adapter = FeaturedCategoriesAdapter(
            context,
            data,
            OnClickListener { view -> this.listener?.onClick(view.tag as Int) })

        this.mRecyclerView = this.mView?.findViewById(R.id.root)
        this.mRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        this.mRecyclerView?.adapter = this.adapter
    }

    interface ItemClickListener {
        fun onClick(id: Int)
    }
}
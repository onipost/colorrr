package app.colorrr.colorrr.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.CategoryImagesListAdapter
import app.colorrr.colorrr.entity.*

class CategoryImagesListView : LinearLayout {
    private var mView: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var mSeeMore: TextView? = null
    private var mTitle: TextView? = null

    private var listener: ItemClickListener? = null
    private var adapter: CategoryImagesListAdapter? = null
    private var data: ArrayList<ImageToCategory> = ArrayList()
    private var categoryID: Int = 0

    constructor(context: Context) : super(context) {
        this.init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.init(context)
    }

    private fun setImagesData(data: List<ImageToCategory>) {
        this.data.clear()
        this.data.addAll(data as ArrayList<ImageToCategory>)
        this.adapter?.notifyDataSetChanged()
    }

    fun setData(category: CategoryAndImages) {
        this.categoryID = category.category.id ?: 0
        this.mTitle?.text = category.category.name
        this.tag = category.category.id ?: 0
        this.setImagesData(category.images)
    }

    fun setListener(listener: ItemClickListener) {
        this.listener = listener
    }

    private fun initViews(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.mView = inflater.inflate(R.layout.view_categories_list, this)
        this.mTitle = this.mView?.findViewById(R.id.title)
        this.mSeeMore = this.mView?.findViewById(R.id.see_all)
        this.mRecyclerView = this.mView?.findViewById(R.id.root)
        this.mRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun init(context: Context) {
        this.initViews(context)

        this.adapter = CategoryImagesListAdapter(context, data, OnClickListener { view ->
            this.listener?.onItemClick(view?.tag as Int)
        })
        this.mRecyclerView?.adapter = this.adapter
        this.mSeeMore?.setOnClickListener { this.listener?.onSeeMoreClick(this.categoryID) }
    }

    interface ItemClickListener {
        fun onItemClick(id: Int)

        fun onSeeMoreClick(categoryID: Int)
    }
}
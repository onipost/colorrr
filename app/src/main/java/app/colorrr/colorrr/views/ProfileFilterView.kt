package app.colorrr.colorrr.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.colorrr.colorrr.R
import org.jetbrains.anko.backgroundDrawable

class ProfileFilterView : LinearLayout {
    private var view: View? = null
    private var filterClickListener: FilterItemClick? = null

    constructor(context: Context) : super(context) {
        this.init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.init(context)
    }

    private fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val darkishPink = ContextCompat.getColor(context, R.color.darkish_pink)
        val pinkishGrey = ContextCompat.getColor(context, R.color.pinkish_grey)

        this.view = inflater.inflate(R.layout.view_profile_filter, this)
        this.view?.let {
            val container = it.findViewById<LinearLayout>(R.id.container)
            for (i in 0 until container.childCount) {
                container.getChildAt(i).setOnClickListener { child ->
                    val childTv = child as TextView
                    childTv.setTextColor(darkishPink)
                    childTv.backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.category_filter_border)

                    refreshToDefault(child, container, pinkishGrey)
                    filterClickListener?.onClick(child.id)
                }
            }
        }
    }

    fun setFilter(filter: Int) {
        val darkishPink = ContextCompat.getColor(context, R.color.darkish_pink)
        val pinkishGrey = ContextCompat.getColor(context, R.color.pinkish_grey)
        this.view?.let {
            val container = it.findViewById<LinearLayout>(R.id.container)
            for (i in 0 until container.childCount) {
                val child = container.getChildAt(i) as TextView
                if (child.tag == filter.toString()) {
                    child.setTextColor(darkishPink)
                    child.backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.category_filter_border)
                    refreshToDefault(child, container, pinkishGrey)
                    break
                }
            }
        }
    }

    private fun refreshToDefault(view: View, container: LinearLayout, defaultColor: Int) {
        for (i in 0 until container.childCount) {
            if (view != container.getChildAt(i)) {
                val item = container.getChildAt(i) as TextView
                item.setTextColor(defaultColor)
                item.backgroundDrawable = null
            }
        }
    }

    fun setFilterItemClick(listener: FilterItemClick) {
        this.filterClickListener = listener
    }

    interface FilterItemClick {
        fun onClick(id: Int)
    }
}
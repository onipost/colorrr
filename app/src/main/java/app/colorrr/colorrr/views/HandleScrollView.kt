package app.colorrr.colorrr.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView

class HandleScrollView : ScrollView {
    private var onBottomReachedListener: OnBottomReachedListener? = null
    private var onTopReachedListener: OnTopReachedListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        val view = getChildAt(childCount - 1) as View

        if (view.bottom - scrollY <= 2 * height) {
            onBottomReachedListener?.onBottomReached()
        } else if (scrollY == 0) {
            onTopReachedListener?.onTopReached()
        }

        super.onScrollChanged(l, t, oldl, oldt)
    }

    fun setBottomReachedListener(listener: OnBottomReachedListener) {
        this.onBottomReachedListener = listener
    }


    fun setTopReachedListener(listener: OnTopReachedListener) {
        this.onTopReachedListener = listener
    }

    interface OnBottomReachedListener {
        fun onBottomReached()
    }

    interface OnTopReachedListener {
        fun onTopReached()
    }
}
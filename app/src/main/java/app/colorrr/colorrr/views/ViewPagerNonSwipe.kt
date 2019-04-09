package app.colorrr.colorrr.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class ViewPagerNonSwipe : ViewPager {
    private var paging = false

    constructor(context: Context) : super(context) {
        this.paging = false
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.paging = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return this.paging && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return this.paging && super.onInterceptTouchEvent(event)
    }

    fun setSelectedItem(item: Int) {
        super.setCurrentItem(item, false)
    }

    fun setPagingpaging(paging: Boolean) {
        this.paging = paging
    }
}
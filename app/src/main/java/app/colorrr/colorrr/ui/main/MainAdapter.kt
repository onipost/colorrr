package app.colorrr.colorrr.ui.main

import android.os.Parcelable
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class MainAdapter(private var pages: List<View>) : PagerAdapter() {
    override fun getCount(): Int {
        return pages.size
    }

    override fun instantiateItem(collection: View, position: Int): Any {
        val v = pages[position]
        (collection as ViewPager).addView(v, 0)
        return v
    }

    override fun destroyItem(collection: View, position: Int, view: Any) {
        (collection as ViewPager).removeView(view as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun finishUpdate(arg0: View) {}

    override fun saveState(): Parcelable? {
        return null
    }

    override fun startUpdate(arg0: View) {}
}
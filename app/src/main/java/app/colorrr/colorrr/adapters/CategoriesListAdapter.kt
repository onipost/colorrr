package app.colorrr.colorrr.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.CategoryAndImages
import app.colorrr.colorrr.views.CategoryImagesListView

class CategoriesListAdapter(
    private var c: Context,
    private var data: ArrayList<CategoryAndImages>,
    private var listener: CategoryImagesListView.ItemClickListener
) :
    ArrayAdapter<CategoryAndImages>(c, R.layout.view_categories_list) {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): CategoryAndImages? {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return data[position].category.id?.toLong() ?: 0L
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = data[position]
        val mView: View = convertView ?: CategoryImagesListView(c)

        mView as CategoryImagesListView
        mView.setData(item)
        mView.setListener(listener)
        return mView
    }
}
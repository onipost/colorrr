package app.colorrr.colorrr.ui.categories

import android.os.Bundle
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.CategoriesListAdapter
import app.colorrr.colorrr.entity.Category
import app.colorrr.colorrr.entity.CategoryAndImages
import app.colorrr.colorrr.ui.base.BaseFragment
import app.colorrr.colorrr.ui.images_list.ImagesListFragment
import app.colorrr.colorrr.views.CategoryFilterView
import app.colorrr.colorrr.views.CategoryImagesListView
import app.colorrr.colorrr.views.FeaturedCategoriesView
import kotlinx.android.synthetic.main.fragment_categories.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk27.coroutines.onScrollListener
import org.jetbrains.anko.support.v4.toast

class CategoriesFragment : BaseFragment<CategoriesViewModel>(),
    CategoriesInterface,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var viewModel: CategoriesViewModel
    private var categories = ArrayList<CategoryAndImages>()
    private var adapter: CategoriesListAdapter? = null
    private var mFeaturedView: FeaturedCategoriesView? = null

    /*
    * LIFECYCLE
    * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getActivityCustom()?.getString(R.string.Collections)
        this.initView()
        this.bindListeners()
    }

    override fun onResume() {
        super.onResume()
        this.viewModel.setListener(this)
        this.viewModel.getData(true)
    }

    override fun onPause() {
        super.onPause()
        this.viewModel.setListener(null)
    }

    override fun onDestroy() {
        this.viewModel.onDestroy()
        super.onDestroy()
    }

    /*
    * BASE FRAGMENT METHODS
    * */
    override fun getLayoutID(): Int {
        return R.layout.fragment_categories
    }

    override fun getViewModel(): CategoriesViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = CategoriesViewModel()

        return this.viewModel
    }

    /*
    * VM-FRAGMENT INTERFACE METHODS
    * */
    override fun onDataArrived(refreshList: Boolean, data: HashMap<String, Any>) {
        if (!data.containsKey("featured") || (data["featured"] as ArrayList<*>).size == 0) {
            this.mFeaturedView?.visibility = View.GONE
        } else {
            this.mFeaturedView?.visibility = View.VISIBLE
            this.mFeaturedView?.setData(data["featured"] as ArrayList<Category>)
        }

        val categoriesList = data["categories"] as ArrayList<CategoryAndImages>
        if (refreshList) {
            this.categories.clear()
            this.adapter?.clear()
        }

        this.categories.addAll(categoriesList)
        this.adapter?.notifyDataSetChanged()
    }

    /*
    * UI LISTENERS
    * */
    override fun onRefresh() {
        this.viewModel.refresh(swipe_refresh)
    }

    /*
    * METHODS AND LISTENERS OF THIS FRAGMENT
    * */
    private fun openImagesListFragment(id: Int) {
        val fragment = ImagesListFragment()
        val bundle = Bundle()
        bundle.putInt("id", id)
        fragment.arguments = bundle
        getNavigationController()?.pushFragment(fragment)
    }

    private fun initView() {
        filter.setFilter(this.viewModel.getFilter())
        getActivityCustom()?.let {
            val header = context?.layoutInflater?.inflate(R.layout.fragment_categories_header, null)
            grid.addHeaderView(header, false)
            this.mFeaturedView = header?.findViewById(R.id.featured_view)
            this.mFeaturedView?.setListener(object : FeaturedCategoriesView.ItemClickListener {
                override fun onClick(id: Int) {
                    openImagesListFragment(id)
                }
            })
            grid.numColumns = 1
            this.adapter = CategoriesListAdapter(it.applicationContext, categories, categoriesViewListener)
            grid.adapter = this.adapter
        }

        grid.onScrollListener {
            onScroll { _, firstVisibleItem, visibleItemCount, totalItemCount ->
                if (firstVisibleItem + visibleItemCount >= totalItemCount)
                    viewModel.loadMore()
            }
        }
    }

    private fun bindListeners() {
        filter.setFilterItemClick(object : CategoryFilterView.FilterItemClick {
            override fun onClick(id: Int) {
                viewModel.filter(id, swipe_refresh)
            }
        })

        swipe_refresh.setOnRefreshListener(this)
    }

    private val categoriesViewListener = object : CategoryImagesListView.ItemClickListener {
        override fun onItemClick(id: Int) {
            toast("item id: $id")
        }

        override fun onSeeMoreClick(categoryID: Int) {
            openImagesListFragment(categoryID)
        }
    }
}
package app.colorrr.colorrr.ui.images_list

import android.os.Bundle
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.R
import app.colorrr.colorrr.adapters.ImagesListAdapter
import app.colorrr.colorrr.entity.Category
import app.colorrr.colorrr.entity.ImageToCategory
import app.colorrr.colorrr.system.AnimationWorker
import app.colorrr.colorrr.system.GlideLoader
import app.colorrr.colorrr.ui.base.BaseFragment
import kotlinx.android.synthetic.main._toolbar.*
import kotlinx.android.synthetic.main.fragment_images_list.*
import kotlinx.android.synthetic.main.fragment_images_list_header.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk27.coroutines.onScrollListener


class ImagesListFragment : BaseFragment<ImagesListViewModel>(), ImagesListInterface,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var viewModel: ImagesListViewModel
    private var images = ArrayList<ImageToCategory>()
    private var adapter: ImagesListAdapter? = null

    /*
    * LIFECYCLE
    * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initVM(savedInstanceState)
        this.initGrid()
        this.bindListeners()
    }

    override fun onDestroy() {
        this.viewModel.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("id", this.viewModel.getCategoryID())
    }

    /*
    * BASE FRAGMENT METHODS
    * */
    override fun getLayoutID(): Int {
        return R.layout.fragment_images_list
    }

    override fun getViewModel(): ImagesListViewModel {
        if (!::viewModel.isInitialized)
            this.viewModel = ImagesListViewModel()

        return this.viewModel
    }

    /*
    * VM-FRAGMENT INTERFACE METHODS
    * */
    override fun onCategoryArrived(category: Category) {
        toolbar_text.text = category.name
        desc_category.text = category.description

        val animator = AnimationWorker.animateRotateInfinite(placeholder, AnimationWorker.LOAD_PLACEHOLDER)
        getActivityCustom()?.let {
            GlideLoader.load(it.applicationContext, category.cover_image, cover_category, placeholder, animator)
        }
    }

    override fun onImagesArrived(refreshList: Boolean, list: List<ImageToCategory>) {
        if (refreshList) {
            this.images.clear()
            this.adapter?.clear()
        }

        this.images.addAll(list)
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
    private fun initVM(savedInstanceState: Bundle?) {
        this.viewModel.setListener(this)
        this.viewModel.setCategoryID(arguments?.getInt("id") ?: 0)
        savedInstanceState?.let { this.viewModel.setCategoryID(it.getInt("id")) }
    }

    private fun bindListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
        swipe_refresh.setOnRefreshListener(this)
    }

    private fun initGrid() {
        getActivityCustom()?.let {
            val header = context?.layoutInflater?.inflate(R.layout.fragment_images_list_header, null)
            grid.addHeaderView(header, false)
            grid.numColumns = 2
            this.adapter = ImagesListAdapter(it.applicationContext, this.images)
            grid.adapter = this.adapter
        }

        grid.onScrollListener {
            onScroll { _, firstVisibleItem, visibleItemCount, totalItemCount ->
                if (firstVisibleItem + visibleItemCount >= totalItemCount){
                    viewModel.loadMore()
                }
            }
        }
    }
}
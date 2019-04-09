package app.colorrr.colorrr.ui.images_list

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.App
import app.colorrr.colorrr.entity.Category
import app.colorrr.colorrr.entity.ImageToCategory
import app.colorrr.colorrr.repository.ImageRepository
import app.colorrr.colorrr.repository.Repository
import app.colorrr.colorrr.ui.base.BaseViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlin.collections.ArrayList

class ImagesListViewModel : BaseViewModel<ImagesListInterface>() {
    private var repository: Repository = App.getInstance().repository
    private var categoryDisposable: Disposable? = null
    private var imagesDisposable: Disposable? = null
    private var categoryID: Int = 0
    private var category: Category? = null
    private var loadLock: Boolean = false
    private var loadEnd: Boolean = false
    private var start = 0

    fun onDestroy() {
        this.categoryDisposable?.dispose()
        this.imagesDisposable?.dispose()
    }

    fun setCategoryID(categoryID: Int) {
        this.loadLock = true
        this.categoryID = categoryID
        this.categoryDisposable = this.repository.categoriesRepository
            .getCategory(categoryID)
            .filter { it.isNotEmpty() }
            .subscribeBy {
                this.category = it[0]
                getListener()?.onCategoryArrived(it[0])
                this.getData(true)
            }
    }

    fun getCategoryID(): Int {
        return this.categoryID
    }

    private fun getData(
        incrementStart: Boolean,
        start: Int = this.start,
        limit: Int = repository.imagesRepository.getLimit()
    ) {
        this.imagesDisposable?.dispose()
        this.imagesDisposable = repository.imagesRepository
            .getImagesByCategoryId(this.categoryID, start, limit)
            .subscribeBy(
                onNext = {
                    val images = it as ArrayList<ImageToCategory>
                    if (images.size > 0 && incrementStart)
                        this.start += repository.imagesRepository.getLimit()

                    if (images.size == 0)
                        this.loadEnd = true

                    this.loadLock = false
                    getListener()?.onImagesArrived(!incrementStart, it)
                },
                onError = { this.loadLock = false }
            )
    }

    fun loadMore() {
        if (!this.loadLock && !this.loadEnd) {
            this.loadLock = true
            repository.imagesRepository.loadData(
                this.start,
                this.categoryID,
                object : ImageRepository.ImagesLoadListener {
                    override fun onLoad() {
                        getData(true)
                    }
                })
        }
    }

    fun refresh(swipeRefreshLayout: SwipeRefreshLayout) {
        if (!this.loadLock) {
            this.loadLock = true
            repository.imagesRepository.refresh(
                this.categoryID,
                this.start,
                object : ImageRepository.RefreshImagesListener {
                    override fun onRefresh(limit: Int) {
                        getData(false, 0, limit)
                        swipeRefreshLayout.isRefreshing = false
                    }
                })
        }
    }
}
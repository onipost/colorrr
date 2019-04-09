package app.colorrr.colorrr.ui.categories

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.App
import app.colorrr.colorrr.repository.CategoryRepository
import app.colorrr.colorrr.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

class CategoriesViewModel : BaseViewModel<CategoriesInterface>() {
    private val repository = App.getInstance().repository
    private var dataDisposable: Disposable? = null
    private var loadEnd: Boolean = false
    private var loadLock: Boolean = false
    var start: Int = 0

    fun onDestroy() {
        this.dataDisposable?.dispose()
    }

    fun getFilter(): Int {
        return repository.categoriesRepository.getFilter()
    }

    fun getData(
        incrementStart: Boolean,
        start: Int = this.start,
        limit: Int = repository.categoriesRepository.getLimit()
    ) {
        this.dataDisposable?.dispose()
        this.dataDisposable = repository.categoriesRepository
            .getData(start, limit)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                val categories = it["categories"] as ArrayList<*>
                if (categories.size > 0 && incrementStart)
                    this.start += repository.categoriesRepository.getLimit()

                if(categories.size == 0)
                    this.loadEnd = true

                this.loadLock = false
                getListener()?.onDataArrived(!incrementStart, it)
            }
    }

    fun loadMore() {
        if (!this.loadLock && !this.loadEnd) {
            this.loadLock = true
            repository.categoriesRepository.loadData(object : CategoryRepository.LoadCategoriesListener {
                override fun onLoad() {
                    getData(true)
                }
            })
        }
    }

    fun refresh(swipeRefreshLayout: SwipeRefreshLayout) {
        if (!this.loadLock) {
            this.loadLock = true
            repository.categoriesRepository.refresh(this.start, object : CategoryRepository.RefreshCategoriesListener {
                override fun onRefresh(limit: Int) {
                    getData(false, 0, limit)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
        }
    }

    fun filter(id: Int, swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.isRefreshing = true
        this.start = 0
        repository.categoriesRepository.setFilter(id)
        repository.categoriesRepository.loadData(object : CategoryRepository.LoadCategoriesListener {
            override fun onLoad() {
                swipeRefreshLayout.isRefreshing = false
                dataDisposable?.dispose()
                dataDisposable = repository.categoriesRepository
                    .getData(0, repository.categoriesRepository.getLimit())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy {
                        if (it["categories"] is ArrayList<*> && (it["categories"] as ArrayList<*>).size > 0)
                            start += repository.categoriesRepository.getLimit()

                        getListener()?.onDataArrived(true, it)
                    }
            }
        })
    }
}
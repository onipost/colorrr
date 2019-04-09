package app.colorrr.colorrr.ui.feed

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.App
import app.colorrr.colorrr.entity.Feed
import app.colorrr.colorrr.ui.base.BaseViewModel
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.repository.FeedRepository
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.UserCurrent
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class FeedViewModel : BaseViewModel<FeedInterface>() {
    private val app = App.getInstance()
    private var dataDisposable: Disposable? = null
    private var loadEnd: Boolean = false
    private var loadLock: Boolean = false
    private var refreshList: Boolean = false
    var start: Int = 0

    fun getFilter(): Int {
        return app.repository.feedRepository.getFilter()
    }

    fun getLoginUserID(): Int {
        return app.repository.sessionRepository.getUserID()
    }

    fun getInternetConnection(): Boolean {
        return app.checkInternetConnection()
    }

    fun getString(id: Int): String {
        return app.getStringById(id)
    }

    fun getUserID(): Int {
        return app.repository.sessionRepository.getUserID()
    }

    fun follow(item: Feed) {
        this.reverseFollow(item)
        getListener()?.onFollowAndLike(true, "")
        if (app.checkInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            var mDisposable: Disposable? = null
            mDisposable = ApiWorker.workerUser.follow(item.userID, item.followed, sessionID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        this.changeLocalFollowedData(item.userID, item.followed)
                        mDisposable?.dispose()
                    },
                    onError = { getListener()?.onFollowAndLike(false, it.localizedMessage) }
                )
        } else {
            this.reverseFollow(item)
            val error = if (item.followed == 0) {
                app.getString(R.string.unable_to_follow_user_no_internet_connection_available_please_connect_to_internet_and_try_again)
            } else {
                app.getString(R.string.unable_to_unfollow_user_no_internet_connection_available_please_connect_to_internet_and_try_again)
            }
            getListener()?.onFollowAndLike(false, error)
        }
    }

    fun like(item: Feed) {
        this.reverseLike(item)
        getListener()?.onFollowAndLike(true, "")
        if (this.getInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            var mDisposable: Disposable? = null
            mDisposable = ApiWorker.workerFeed.like(item.id ?: 0, item.liked, sessionID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        app.repository.feedRepository.updateAsync(item)
                        mDisposable?.dispose()
                    },
                    onError = { getListener()?.onFollowAndLike(false, it.localizedMessage) }
                )
        } else {
            this.reverseLike(item)
            getListener()?.onFollowAndLike(
                false,
                this.getString(R.string.unable_to_like_post_no_internet_connection_available_please_connect_to_internet_and_try_again)
            )
        }
    }

    fun reportItem(item: Feed) {
        if (app.checkInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            var mDisposable: Disposable? = null
            mDisposable = ApiWorker.workerFeed.report(item.id ?: 0, sessionID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        getListener()?.onReport(true, "")
                        mDisposable?.dispose()
                    },
                    onError = { getListener()?.onReport(false, it.localizedMessage) }
                )
        } else {
            getListener()?.onReport(
                false,
                app.getString(R.string.unable_to_report_post_no_internet_connection_available_please_connect_to_internet_and_try_again)
            )
        }
    }

    fun deleteItem(position: Int, item: Feed) {
        if (app.checkInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            var mDisposable: Disposable? = null
            mDisposable = ApiWorker.workerFeed.delete(item.id ?: 0, sessionID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        //TODO remove from finished user table!!!
                        app.repository.feedRepository.deleteAsync(item)
                        mDisposable?.dispose()
                    },
                    onError = { getListener()?.onDelete(position, false, it.localizedMessage) }
                )
        } else {
            getListener()?.onDelete(
                position,
                false,
                app.getString(R.string.unable_to_delete_your_drawing_no_internet_connection_available_please_connect_to_internet_and_try_ag)
            )
        }
    }

    fun getData(swipeRefreshLayout: SwipeRefreshLayout? = null) {
        this.dataDisposable?.dispose()
        this.dataDisposable = app.repository.feedRepository
            .getData()
            .subscribeBy {
                if (it.isEmpty())
                    this.loadEnd = true

                this.loadLock = false
                getListener()?.onDataArrived(refreshList, it)
                if (refreshList) {
                    swipeRefreshLayout?.isRefreshing = false
                    refreshList = false
                }
            }
    }

    fun loadMore() {
        //TODO listen internet connection and at connect loadEnd = false in all lists
        if (!this.loadLock && !this.loadEnd) {
            this.loadLock = true
            app.repository.feedRepository.loadData()
        }
    }

    fun loadUserProfile(userID: Int): Observable<UserCurrent> {
        return Observable.create { observable ->
            ApiWorker.workerUser
                .getUserByID(userID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        app.repository.sessionRepository.addCachedUser(it)
                        observable.onNext(it)
                    },
                    onError = { observable.onError(it) })
        }
    }

    fun refresh(swipeRefreshLayout: SwipeRefreshLayout) {
        if (!this.loadLock) {
            this.loadLock = true
            app.repository.feedRepository.refresh(this.start, object : FeedRepository.RefreshFeedListener {
                override fun onRefresh(limit: Int) {
                    swipeRefreshLayout.isRefreshing = false
                }
            })
        }
    }

    fun filter(id: Int, swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.isRefreshing = true
        this.setFilter(id)
        this.refreshList = true
        this.getData(swipeRefreshLayout)
    }

    private fun reverseLike(item: Feed) {
        if (item.liked == 1) {
            item.liked = 0
            item.likesCount--
        } else {
            item.liked = 1
            item.likesCount++
        }
    }

    private fun reverseFollow(item: Feed) {
        item.followed = if (item.followed == 1) 0 else 1
    }

    private fun changeLocalFollowedData(userID: Int, followed: Int) {
        var mDisposable: Disposable? = null
        mDisposable = Flowable.fromCallable { app.repository.feedRepository.getDataByUser(userID) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { list ->
                    list.forEach { this.reverseFollow(it) }

                    app.repository.feedRepository.updateAll(list)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy {}

                    app.repository.sessionRepository.changeUserFollowedCount(followed)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy {}
                    getListener()?.onFollowAndLike(true, "")
                    mDisposable?.dispose()
                }
            )
    }

    private fun setFilter(id: Int) {
        val feedRepository = app.repository.feedRepository
        this.start = 0
        this.loadEnd = false
        val filter = when (id) {
            R.id.filter_all -> feedRepository.FILTER_ALL
            R.id.filter_popular -> feedRepository.FILTER_POPULAR
            R.id.filter_followed -> feedRepository.FILTER_FOLLOWS
            else -> feedRepository.FILTER_ALL
        }

        feedRepository.setFilter(filter)
    }
}
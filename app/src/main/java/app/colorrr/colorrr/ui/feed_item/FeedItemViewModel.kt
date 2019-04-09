package app.colorrr.colorrr.ui.feed_item

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.App
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Feed
import app.colorrr.colorrr.repository.Repository
import app.colorrr.colorrr.ui.base.BaseViewModel
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.repository.FeedRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.jetbrains.anko.doAsync

class FeedItemViewModel : BaseViewModel<FeedItemInterface>() {
    private var app = App.getInstance()
    private var repository: Repository = App.getInstance().repository
    private var postDisposable: Disposable? = null
    private var commentsDisposable: Disposable? = null
    private var postID: Int = 0
    private var post: Feed? = null
    private var loadLock: Boolean = false
    private var loadEnd: Boolean = false
    private var start = 0

    fun onDestroy() {
        this.postDisposable?.dispose()
        this.commentsDisposable?.dispose()
    }

    fun setPostID(postID: Int) {
        this.loadLock = true
        this.postID = postID
        this.postDisposable = repository.feedRepository
            .getFeedItem(postID)
            .filter { it.isNotEmpty() }
            .subscribeBy {
                this.post = it[0]
                this.loadLock = false
                getListener()?.onPostArrived(it[0])
                this.loadMore()
            }
    }

    fun getPostID(): Int {
        return this.postID
    }

    fun getUserID(): Int {
        return repository.sessionRepository.getUserID()
    }

    fun getPost(): Feed? {
        return this.post
    }

    fun getString(id: Int): String {
        return app.getStringById(id)
    }

    fun getInternetConnection(): Boolean {
        return app.checkInternetConnection()
    }

    private fun getData(
        incrementStart: Boolean,
        start: Int = this.start,
        limit: Int = repository.feedRepository.getLimitComments()
    ) {
        this.commentsDisposable?.dispose()
        this.commentsDisposable = repository.feedRepository
            .getDataComments(start, limit, this.postID)
            .subscribeBy(
                onNext = {
                    if (it.isNotEmpty() && incrementStart)
                        this.start += repository.feedRepository.getLimitComments()

                    if (it.isEmpty())
                        this.loadEnd = true

                    this.loadLock = false
                    getListener()?.onCommentsArrived(!incrementStart, it)
                },
                onError = { this.loadLock = false }
            )
    }

    fun loadUserProfile(userID: Int): Observable<UserCurrent> {
        return Observable.create { observable ->
            ApiWorker.workerUser
                .getUserByID(userID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        repository.sessionRepository.addCachedUser(it)
                        observable.onNext(it)
                    },
                    onError = { observable.onError(it) })
        }
    }

    fun loadMore() {
        if (!this.loadLock && !this.loadEnd) {
            this.loadLock = true
            repository.feedRepository.loadCommentsData(
                this.start,
                this.postID,
                object : FeedRepository.LoadFeedListener {
                    override fun onLoad() {
                        getData(true)
                    }
                })
        }
    }

    fun refresh(swipeRefreshLayout: SwipeRefreshLayout) {
        if (!this.loadLock) {
            this.loadLock = true
            repository.feedRepository.refreshComments(
                this.start,
                this.postID,
                object : FeedRepository.RefreshFeedListener {
                    override fun onRefresh(limit: Int) {
                        getData(false, 0, limit)
                        swipeRefreshLayout.isRefreshing = false
                    }
                })
        }
    }

    fun setLike() {
        post?.let { item ->
            if (app.checkInternetConnection()) {
                this.post = this.reverseLike(item)
                val sessionID = repository.sessionRepository.getSessionID()
                val likeDisposable = ApiWorker.workerFeed.like(item.id ?: 0, item.liked, sessionID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            repository.feedRepository.updateAsync(item)
                            getListener()?.onLike(true, "")
                        },
                        onError = {
                            this.post = this.reverseLike(item)
                            getListener()?.onLike(false, it.localizedMessage)
                        }
                    )
            } else {
                getListener()?.onLike(
                    false,
                    app.getStringById(R.string.unable_to_like_post_no_internet_connection_available_please_connect_to_internet_and_try_again)
                )
            }
        }
    }

    fun follow() {
        post?.let { item ->
            if (app.checkInternetConnection()) {
                this.reverseFollow(item)
                val sessionID = repository.sessionRepository.getSessionID()
                val followDisposable = ApiWorker.workerUser.follow(item.userID, item.followed, sessionID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            repository.feedRepository.updateFeedFollows(item.userID)
                            getListener()?.onFollow(true, "")
                        },
                        onError = {
                            this.reverseFollow(item)
                            getListener()?.onFollow(false, it.localizedMessage)
                        })
            } else {
                val messageID = if (item.followed == 0) {
                    R.string.unable_to_follow_user_no_internet_connection_available_please_connect_to_internet_and_try_again
                } else {
                    R.string.unable_to_unfollow_user_no_internet_connection_available_please_connect_to_internet_and_try_again
                }
                getListener()?.onFollow(false, app.getStringById(messageID))
            }
        }
    }

    fun sendReport() {
        if (app.checkInternetConnection()) {
            post?.let { item ->
                val sessionID = repository.sessionRepository.getSessionID()
                val reportDisposable = ApiWorker.workerFeed.report(item.id ?: 0, sessionID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            getListener()?.onReport(
                                true,
                                app.getStringById(R.string.thank_you_for_reporting_item_well_review_and_remove_it_from_colorrr_comminity_if_required)
                            )
                        },
                        onError = { getListener()?.onReport(false, it.localizedMessage) }
                    )
            }
        } else {
            getListener()?.onReport(
                false,
                app.getStringById(R.string.unable_to_report_post_no_internet_connection_available_please_connect_to_internet_and_try_again)
            )
        }
    }

    fun delete() {
        if (app.checkInternetConnection()) {
            post?.let { item ->
                val sessionID = repository.sessionRepository.getSessionID()
                val deleteDisposable = ApiWorker.workerFeed.delete(item.id ?: 0, sessionID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = {
                            repository.feedRepository.deleteAsync(item)
                            getListener()?.onDelete(true, "")
                        },
                        onError = { getListener()?.onReport(false, it.localizedMessage) }
                    )
            }
        } else {
            getListener()?.onReport(
                false,
                app.getStringById(R.string.unable_to_delete_your_drawing_no_internet_connection_available_please_connect_to_internet_and_try_ag)
            )
        }
    }

    private fun reverseLike(item: Feed?): Feed? {
        item?.let {
            if (it.liked == 1) {
                it.liked = 0
                it.likesCount--
            } else {
                it.liked = 1
                it.likesCount++
            }
        }

        return item
    }

    private fun reverseFollow(item: Feed) {
        item.followed = if (item.followed == 1) 0 else 1
    }
}
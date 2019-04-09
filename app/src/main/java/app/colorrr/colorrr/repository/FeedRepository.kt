package app.colorrr.colorrr.repository

import android.util.Log
import app.colorrr.colorrr.database.dao.FeedDao
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.entity.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.UnknownHostException

class FeedRepository(private val feedDao: FeedDao) {
    val FILTER_ALL = 0
    val FILTER_POPULAR = 1
    val FILTER_FOLLOWS = 2

    private var startAll: Int = 0
    private var startPopular: Int = 0
    private var startFollows: Int = 0
    private var limit: Int = 5
    private var limitComments: Int = 5
    private var filter: Int = this.FILTER_ALL
    private var app = App.getInstance()

    init {
        this.initRepo()
    }

    fun setFilter(filter: Int) {
        when (filter) {
            this.FILTER_ALL -> this.startAll = this.limit
            this.FILTER_POPULAR -> this.startPopular = this.limit
            this.FILTER_FOLLOWS -> this.startFollows = this.limit
            else -> this.startAll = this.limit
        }
        this.filter = filter
    }

    fun getFilter(): Int {
        return this.filter
    }

    fun getLimitComments(): Int {
        return this.limitComments
    }

    fun getFeedItem(id: Int): Flowable<List<Feed>> {
        return feedDao.get(id).observeOn(AndroidSchedulers.mainThread())
    }

    fun getDataByUser(userID: Int): List<Feed> {
        return feedDao.getByUser(userID)
    }

    fun getData(): Flowable<List<Feed>> {
        val observable = when (filter) {
            this.FILTER_ALL -> this.feedDao.getAll()
            this.FILTER_POPULAR -> this.feedDao.getPopular()
            this.FILTER_FOLLOWS -> this.feedDao.getByFollows()
            else -> this.feedDao.getAll()
        }
        return observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { Flowable.just(this.getSublist(it)) }
    }

    fun getDataComments(start: Int, limit: Int, postID: Int): Flowable<List<Comment>> {
        return Flowable.fromCallable { this.feedDao.getComments(start, limit, postID) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadData(listener: LoadFeedListener? = null) {
        if (app.checkInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            val observable = when {
                this.filter == this.FILTER_ALL -> {
                    ApiWorker.workerFeed.loadFeed(this.startAll, this.limit, sessionID)
                }
                this.filter == this.FILTER_FOLLOWS -> {
                    ApiWorker.workerFeed.loadFeedByFollows(this.startFollows, this.limit, sessionID)
                }
                else -> {
                    ApiWorker.workerFeed.loadFeedPopular(this.startPopular, this.limit, sessionID)
                }
            }

            val loadDisposable = observable.observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onNext = {
                    val comments = ArrayList<Comment>()
                    val feed = ArrayList<Feed>()
                    this.addToArrays(it, feed, comments)

                    doAsync {
                        insertFeed(feed, comments)
                        uiThread {
                            when (filter) {
                                FILTER_ALL -> startAll += limit
                                FILTER_FOLLOWS -> startFollows += limit
                                FILTER_POPULAR -> startPopular += limit
                            }
                            listener?.onLoad()
                        }
                    }
                },
                onError = { listener?.onLoad() }
            )
        } else {
            listener?.onLoad()
        }
    }

    fun loadCommentsData(start: Int, postID: Int, listener: LoadFeedListener) {
        if (app.checkInternetConnection()) {
            val loadCommentsDisposable = ApiWorker.workerFeed
                .loadComments(start, this.limitComments, postID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        doAsync {
                            insertComments(it)
                            uiThread { listener.onLoad() }
                        }
                    },
                    onError = { listener.onLoad() }
                )
        } else {
            listener.onLoad()
        }
    }

    fun refresh(limit: Int, listener: RefreshFeedListener) {
        if (app.checkInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            val observable = when {
                this.filter == this.FILTER_ALL -> {
                    ApiWorker.workerFeed.loadFeed(0, limit, sessionID)
                }
                this.filter == this.FILTER_FOLLOWS -> {
                    ApiWorker.workerFeed.loadFeedByFollows(0, limit, sessionID)
                }
                else -> {
                    ApiWorker.workerFeed.loadFeedPopular(0, limit, sessionID)
                }
            }

            val refreshDisposable = observable.observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onNext = {
                    val comments = ArrayList<Comment>()
                    val feed = ArrayList<Feed>()
                    this.addToArrays(it, feed, comments)

                    doAsync {
                        dropFeed()
                        insertFeed(feed, comments)
                        uiThread { listener.onRefresh(limit) }
                    }
                },
                onError = { listener.onRefresh(limit) }
            )
        } else {
            listener.onRefresh(limit)
        }
    }

    fun refreshComments(limit: Int, postID: Int, listener: RefreshFeedListener) {
        if (app.checkInternetConnection()) {
            val commentsRefreshDisposable = ApiWorker.workerFeed
                .loadComments(0, limit, postID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        doAsync {
                            dropComments(it)
                            insertComments(it)
                            uiThread { listener.onRefresh(limit) }
                        }
                    },
                    onError = { listener.onRefresh(limit) }
                )
        } else {
            listener.onRefresh(limit)
        }
    }

    private fun getSublist(list: List<Feed>): List<Feed> {
        return when (this.filter) {
            this.FILTER_ALL -> list.subList(Math.max(0, this.startAll - this.limit), Math.min(this.startAll, list.size))
            this.FILTER_POPULAR -> list.subList(Math.max(0, this.startPopular - this.limit), Math.min(this.startPopular, list.size))
            this.FILTER_FOLLOWS -> list.subList(Math.max(0, this.startFollows - this.limit), Math.min(this.startFollows, list.size))
            else -> list.subList(Math.max(0, this.startAll - this.limit), this.startAll)
        }
    }

    private fun initRepo() {
        doAsync {
            if (app.checkInternetConnection())
                dropFeed()

            uiThread {
                initAll()
                initPopular()
                initFollows()
            }
        }
    }

    private fun initAll() {
        val allDisposable = ApiWorker.workerFeed
            .loadFeed(this.startAll, this.limit, this.app.repository.sessionRepository.getSessionID())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    val comments = ArrayList<Comment>()
                    val feed = ArrayList<Feed>()
                    this.addToArrays(it, feed, comments)

                    doAsync {
                        insertFeed(feed, comments)
                        startAll += limit
                        uiThread { app.repository.countDownLoadTargets() }
                    }
                },
                onError = {
                    startAll += limit

                    if (it is UnknownHostException)
                        Log.d("API_WORKER", "feed: no network")

                    this.app.repository.countDownLoadTargets()
                }
            )
    }

    private fun initPopular() {
        val popularDisposable = ApiWorker.workerFeed
            .loadFeedPopular(this.startPopular, this.limit, this.app.repository.sessionRepository.getSessionID())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    val comments = ArrayList<Comment>()
                    val feed = ArrayList<Feed>()
                    this.addToArrays(it, feed, comments)

                    doAsync {
                        insertFeed(feed, comments)
                        startPopular += limit
                        uiThread { app.repository.countDownLoadTargets() }
                    }
                },
                onError = {
                    startPopular += limit

                    if (it is UnknownHostException)
                        Log.d("API_WORKER", "feed: no network")

                    this.app.repository.countDownLoadTargets()
                }
            )
    }

    private fun initFollows() {
        val followsDisposable = ApiWorker.workerFeed
            .loadFeedByFollows(this.startFollows, this.limit, this.app.repository.sessionRepository.getSessionID())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    val comments = ArrayList<Comment>()
                    val feed = ArrayList<Feed>()
                    this.addToArrays(it, feed, comments)

                    doAsync {
                        insertFeed(feed, comments)
                        startFollows += limit
                        uiThread { app.repository.countDownLoadTargets() }
                    }
                },
                onError = {
                    startFollows += limit

                    if (it is UnknownHostException)
                        Log.d("API_WORKER", "feed: no network")

                    this.app.repository.countDownLoadTargets()
                }
            )
    }

    private fun addToArrays(data: List<HashMap<String, Any>>, feed: ArrayList<Feed>, comments: ArrayList<Comment>) {
        data.forEach { dataItem ->
            feed.add(dataItem["feed"] as Feed)
            if (dataItem.containsKey("comment"))
                comments.add(dataItem["comment"] as Comment)
        }
    }

    private fun dropFeed(feed: List<Feed>? = null, comments: List<Comment>? = null) {
        this.dropComments(comments)

        feed?.let { feedItems ->
            feedItems.forEach { this.feedDao.delete(it) }
        } ?: run {
            this.feedDao.deleteAll()
        }
    }

    private fun dropComments(comments: List<Comment>? = null) {
        comments?.let { commentsItems ->
            commentsItems.forEach { this.feedDao.deleteComment(it) }
        } ?: run {
            this.feedDao.deleteCommentsAll()
        }
    }

    private fun insertComments(comments: List<Comment>) {
        this.feedDao.insertCommentsAll(comments)
    }

    private fun insertFeed(feed: List<Feed>, comments: List<Comment>) {
        this.insertComments(comments)
        this.feedDao.insertAll(feed)
    }

    fun updateAsync(item: Feed) {
        doAsync { feedDao.update(item) }
    }

    fun updateAll(list: List<Feed>): Observable<Boolean> {
        return Observable.create {
            feedDao.updateAll(list)
            it.onNext(true)
        }
    }

    fun updateFeedFollows(userID: Int) {
        doAsync {
            val feed = feedDao.getByUser(userID)
            feed.forEach {
                //TODO transaction
                it.followed = if (it.followed == 1) 0 else 1
                feedDao.update(it)
            }
        }
    }

    fun updateFeedByUserData(user: User?) {
        user?.let { userObject ->
            doAsync {
                val feed = feedDao.getByUser(userObject.userID)
                feed.forEach {
                    it.userName = userObject.name
                    it.userEmail = userObject.email
                    it.userAvatar = userObject.image
                }

                feedDao.updateAll(feed)
            }
        }
    }

    fun deleteAsync(item: Feed) {
        doAsync {
            val comments = feedDao.getComments(item.id ?: 0)
            comments.forEach { feedDao.deleteComment(it) }
            feedDao.delete(item)
        }
    }

    interface LoadFeedListener {
        fun onLoad()
    }

    interface RefreshFeedListener {
        fun onRefresh(limit: Int)
    }
}
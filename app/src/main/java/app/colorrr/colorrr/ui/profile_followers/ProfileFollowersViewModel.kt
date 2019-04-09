package app.colorrr.colorrr.ui.profile_followers

import app.colorrr.colorrr.ui.base.BaseViewModel
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Feed
import app.colorrr.colorrr.entity.Follower
import app.colorrr.colorrr.entity.UserCurrent
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class ProfileFollowersViewModel : BaseViewModel<ProfileFollowersInterface>() {
    var TYPE_FOLLOWERS = 0
    var TYPE_FOLLOWED = 1

    private var app = App.getInstance()
    private var currentUserID: Int = app.repository.sessionRepository.getUserID()
    private var userID: Int = this.currentUserID
    private var followersList: PublishSubject<ArrayList<Follower>> = PublishSubject.create()
    private var start: Int = 0
    private var limit: Int = 5
    private var loadLock = false
    private var loadEnd = false
    private var type = TYPE_FOLLOWERS
    private var dataDisposable: Disposable? = null

    fun onDestroy() {
        this.start = 0
        this.userID = this.currentUserID
        this.loadEnd = false
        this.loadLock = false
        followersList.onNext(ArrayList())
    }

    fun setProfileID(userID: Int?) {
        userID?.let { this.userID = it }
    }

    fun getProfileID(): Int {
        return this.userID
    }

    fun getLoginUserID(): Int {
        return this.currentUserID
    }

    fun setType(type: Int?) {
        type?.let { this.type = it }
    }

    fun isAnother(): Boolean {
        return this.userID == this.currentUserID
    }

    fun getString(id: Int?): String {
        return id?.let { app.getStringById(id) } ?: run { "" }
    }

    fun getToolbarText(): String {
        return when {
            //TODO strings lowercase
            this.type == TYPE_FOLLOWERS -> this.getString(R.string.FOLLOWERS)
            this.type == TYPE_FOLLOWED -> this.getString(R.string.Followed)
            else -> this.getString(R.string.FOLLOWERS)
        }
    }

    fun getEmptyText(): String {
        val stringID: Int = if (this.type == TYPE_FOLLOWERS) {
            if (this.currentUserID != this.userID) R.string.nobody_followed_user_at_the_moment else R.string.nobody_followed_you_at_the_moment_its_time_to_create_and_publish_new_work
        } else {
            if (this.currentUserID != this.userID) R.string.user_is_not_following_anyone_at_the_moment else R.string.youre_not_following_any_user_at_the_moment_its_time_to_explore_community_feed_and_follow_great_artis
        }
        return app.getString(stringID)
    }

    fun getBackProfileStringId(): Int {
        return when (this.type) {
            TYPE_FOLLOWED -> R.string.Followed
            TYPE_FOLLOWERS -> R.string.FOLLOWERS
            else -> R.string.FOLLOWERS
        }
    }

    fun loadUserProfile(userID: Int): Observable<UserCurrent> {
        return Observable.create { observable ->
            if (userID != this.currentUserID) {
                if (app.checkInternetConnection()) {
                    ApiWorker.workerUser
                        .getUserByID(userID)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onNext = {
                                app.repository.sessionRepository.addCachedUser(it)
                                observable.onNext(it)
                            },
                            onError = { observable.onError(it) })
                } else {
                    observable.onError(app.getThrowable(R.string.unable_to_load_users_profile_no_internet_connection_available_please_connect_to_internet_and_try_aga))
                }
            } else {
                val user = app.repository.sessionRepository.getUserCurrent().value
                user?.let { observable.onNext(it) }
                    ?: run { observable.onError(app.getThrowable(R.string.unknown_error_occurred_please_try_again_later)) }
            }
        }
    }

    fun loadData() {
        if (!this.loadLock && !this.loadEnd) {
            this.loadLock = true
            val observable = this.getFollowersListObservable(this.start, this.limit)
            this.dataDisposable?.dispose()
            this.dataDisposable = observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        this.start += this.limit
                        this.loadLock = false

                        if (it.isEmpty() || it.size < this.limit)
                            this.loadEnd = true

                        followersList.onNext(it as ArrayList)
                        getListener()?.onFollowersArrived(false, it)
                    },
                    onError = {
                        this.loadLock = false
                        getListener()?.onError(it.localizedMessage)
                    }
                )
        }
    }

    fun refresh() {
        if (!this.loadLock) {
            this.loadLock = true
            val observable = this.getFollowersListObservable(0, this.start)
            this.dataDisposable?.dispose()
            this.dataDisposable = observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        this.loadLock = false
                        followersList.onNext(it as ArrayList)
                        getListener()?.onFollowersArrived(true, it)
                    },
                    onError = {
                        this.loadLock = false
                        getListener()?.onError(it.localizedMessage)
                    }
                )
        }
    }

    fun follow(item: Follower) {
        this.reverseFollow(item)
        getListener()?.onFollowersArrived(false, ArrayList())
        if (app.checkInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            var mDisposable: Disposable? = null
            mDisposable = ApiWorker.workerUser.follow(item.userID, item.isFollowed, sessionID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        this.changeLocalFollowedData(item.userID, item.isFollowed)
                        mDisposable?.dispose()
                    },
                    onError = { getListener()?.onError(it.localizedMessage) }
                )
        } else {
            this.reverseFollow(item)
            val error = if (item.isFollowed == 0) {
                app.getString(R.string.unable_to_follow_user_no_internet_connection_available_please_connect_to_internet_and_try_again)
            } else {
                app.getString(R.string.unable_to_unfollow_user_no_internet_connection_available_please_connect_to_internet_and_try_again)
            }
            getListener()?.onError(error)
        }
    }

    private fun reverseFollow(item: Follower) {
        item.isFollowed = if (item.isFollowed == 1) 0 else 1
    }

    private fun reverseFollow(item: Feed) {
        item.followed = if (item.followed == 1) 0 else 1
    }

    private fun changeLocalFollowedData(userID: Int, followed: Int) {
        var mDisposable: Disposable? = null
        mDisposable = Flowable.fromCallable { app.repository.feedRepository.getDataByUser(userID) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext { mDisposable?.dispose() }
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
                }
            )
    }

    private fun getFollowersListObservable(
        start: Int,
        limit: Int
    ): Observable<List<Follower>> {
        val sessionID = app.repository.sessionRepository.getSessionID()
        return when {
            this.type == TYPE_FOLLOWERS -> ApiWorker.workerUser.loadFollowers(
                start,
                limit,
                userID,
                sessionID
            )
            this.type == TYPE_FOLLOWED -> ApiWorker.workerUser.loadFollowed(
                start,
                limit,
                userID,
                sessionID
            )
            else -> ApiWorker.workerUser.loadFollowers(start, limit, this.userID, sessionID)
        }
    }
}
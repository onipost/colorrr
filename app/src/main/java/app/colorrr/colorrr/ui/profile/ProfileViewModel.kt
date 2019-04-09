package app.colorrr.colorrr.ui.profile

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.colorrr.colorrr.App
import app.colorrr.colorrr.entity.Image
import app.colorrr.colorrr.entity.ImagePublished
import app.colorrr.colorrr.entity.ImageUnfinished
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class ProfileViewModel : BaseViewModel<ProfileInterface>() {
    var FILTER_ALL = 0
    var FILTER_UNFINISHED = 1
    var FILTER_PUBLISHED = 2

    private var app = App.getInstance()
    private var profile: UserCurrent? = null
    private var currentUserID: Int = app.repository.sessionRepository.getUserID()
    private var userID: Int = this.currentUserID
    private var filter: Int = FILTER_ALL
    private var loadEnd: Boolean = false
    private var loadLock: Boolean = false
    private var refreshList: Boolean = false
    private var profileDisposable: Disposable? = null

    fun onDestroy() {
        this.profile = null
        this.userID = this.currentUserID
        this.filter = FILTER_ALL
        this.profileDisposable?.dispose()
    }

    fun setProfileID(userID: Int?) {
        userID?.let { if (it > 0) this.userID = it }

        if (this.userID != this.currentUserID) {
            //some one user
            this.profile = app.repository.sessionRepository.getCachedUser(this.userID)
            this.userID = this.profile?.userID ?: -1
            getListener()?.onProfileArrived(this.profile, this.currentUserID)
        } else {
            //current user
            this.profileDisposable = app.repository.sessionRepository
                .getUserCurrent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    this.profile = it
                    getListener()?.onProfileArrived(it, this.currentUserID)
                }
        }
    }

    fun getFilter(): Int {
        return this.filter
    }

    fun getProfileID(): Int {
        return this.profile?.userID ?: -1
    }

    fun getString(id: Int?): String {
        return id?.let { app.getStringById(id) } ?: run { "" }
    }

    fun getInternetConnection(): Boolean {
        return app.checkInternetConnection()
    }

    fun removeCached() {
        app.repository.sessionRepository.removeCachedUser(this.userID)
    }

    fun isAnother(): Boolean {
        return this.userID != this.currentUserID
    }

    fun isRoot(): Boolean {
        return app.repository.sessionRepository.getCachedUsersSize() == 0
    }

    fun getImagesListByFilter(): ArrayList<Image> {
        this.profile?.let {
            return when (this.filter) {
                this.FILTER_ALL -> this.getAllProfileImages(it)
                this.FILTER_UNFINISHED -> it.unfinishedImages as ArrayList<Image>
                this.FILTER_PUBLISHED -> it.publishedImages as ArrayList<Image>
                else -> this.getAllProfileImages(it)
            }
        } ?: return ArrayList()
    }

    fun filter(id: Int, swipeRefreshLayout: SwipeRefreshLayout) {
        /*swipeRefreshLayout.isRefreshing = true
        this.setFilter(id)
        this.refreshList = true
        this.getData(swipeRefreshLayout)*/
    }

    private fun getAllProfileImages(user: UserCurrent): ArrayList<Image> {
        val correctUnfinishedList = ArrayList<ImageUnfinished>()
        val list = ArrayList<Image>()
        list.addAll(user.publishedImages)
        user.unfinishedImages.forEach { item ->
            val filterResult = list.filter { (it as ImagePublished).id == item.feedID }.size
            if (filterResult == 0)
                correctUnfinishedList.add(item)
        }

        list.addAll(correctUnfinishedList)

        return list
    }

    /*private fun setFilter(id: Int) {
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
    }*/
}
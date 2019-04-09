package app.colorrr.colorrr.repository

import android.util.Log
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.*
import app.colorrr.colorrr.database.dao.SessionDao
import app.colorrr.colorrr.database.dao.UserDao
import app.colorrr.colorrr.entity.*
import app.colorrr.colorrr.entity.UserCurrent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList

class SessionRepository(private val sessionDao: SessionDao, private val userDao: UserDao) {
    private var app = App.getInstance()
    private var session: Session? = null
    private var user: BehaviorSubject<UserCurrent> = BehaviorSubject.create()
    private var cachedUser = ArrayList<UserCurrent>()

    fun initSession(): Observable<List<Session>> {
        //TODO remove before production
        //this.sessionDao.insert(Session(23, "2aa6a2a427d85f5f82df16a0704102235c4823d32a63a"))
        return sessionDao.getSession().observeOn(AndroidSchedulers.mainThread())
    }

    fun setSession(data: List<Session>) {
        if (!data.isEmpty()) {
            this.session = data[0]
            this.loadUserData()
        } else {
            this.app.repository.countDownLoadTargets()
        }
    }

    fun isSessionActive(): Boolean {
        return this.session != null
    }

    fun getSessionID(): String {
        return this.session?.sessionID ?: ""
    }

    fun getUserCurrent(): BehaviorSubject<UserCurrent> {
        return this.user
    }

    fun getUserID(): Int {
        return this.session?.userID ?: 0
    }

    fun getCachedUsersSize(): Int {
        return this.cachedUser.size
    }

    fun getCachedUser(id: Int): UserCurrent? {
        val filtered = this.cachedUser.filter { it.userID == id }
        return if (filtered.isNotEmpty()) {
            filtered[filtered.size - 1]
        } else {
            null
        }
    }

    fun addCachedUser(user: UserCurrent) {
        this.cachedUser.add(user)
    }

    fun removeCachedUser(id: Int) {
        this.cachedUser = this.cachedUser.filter { it.userID != id } as ArrayList
    }

    fun refreshUser(user: UserCurrent) {
        this.user.onNext(user)
        this.userDao.deleteUser()
        this.userDao.insertUser(user as User)

        this.userDao.deletePublished()
        this.userDao.insertPublished(user.publishedImages)

        this.userDao.deleteUnfinished()
        this.userDao.insertUnfinished(user.unfinishedImages)

        this.userDao.deletePremium()
        this.userDao.insertPremium(user.premiumImages)
    }

    fun getUnfinishedFull(): List<ImageToCategory> {
        return this.userDao.getUnfinishedFull()
    }

    fun insertUnfinishedInDb(images: List<ImageUnfinished>) {
        this.userDao.insertUnfinished(images)
    }

    fun refreshSessionInDb(session: Session) {
        this.sessionDao.clear()
        this.sessionDao.insert(session)
    }

    fun updateUser(user: UserCurrent?) {
        user?.let {
            doAsync { userDao.update(it as User) }
            this.user.onNext(it)
        }
    }

    fun changeUserFollowedCount(increment: Int): Observable<Boolean> {
        return Observable.create { observable ->
            val user = this.user.value
            user?.let {
                if (increment == 1)
                    it.followedCount++
                else
                    it.followedCount--

                this.updateUser(it)
            }

            observable.onNext(true)
        }
    }

    private fun loadUserData() {
        val disposable = ApiWorker.workerUser.getUser(Locale.getDefault().language, this.session?.sessionID ?: "")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    doAsync {
                        refreshUser(it)
                        uiThread { app.repository.countDownLoadTargets() }
                    }
                },
                onError = {
                    if (it is UnknownHostException)
                        Log.d("API_WORKER", "user: no network")

                    if (it.message == ApiWorker.ERROR_INVALID_ARGUMENTS)
                        doAsync {
                            sessionDao.clear()
                            uiThread { app.repository.countDownLoadTargets() }
                        }
                    else
                        app.repository.countDownLoadTargets()
                }
            )
    }
}
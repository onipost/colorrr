package app.colorrr.colorrr.repository

import android.util.Log
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.*
import app.colorrr.colorrr.database.dao.SystemDao
import app.colorrr.colorrr.entity.TosPrivacy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.UnknownHostException
import java.util.*

class SystemRepository(private val systemDao: SystemDao) {
    private var app = App.getInstance()
    private var tosPrivacy: TosPrivacy? = null

    init {
        this.loadSystemData()
    }

    fun getTos(): String {
        return this.tosPrivacy?.tos ?: ""
    }

    fun getPrivacy(): String {
        return this.tosPrivacy?.privacy ?: ""
    }

    private fun loadSystemData() {
        val disposable = ApiWorker.workerSystem
            .getSystemData(Locale.getDefault().language)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    doAsync {
                        refresh(it)
                        uiThread { app.repository.countDownLoadTargets() }
                    }
                },
                onError = {
                    if (it is UnknownHostException)
                        Log.d("API_WORKER", "system data: no network")

                    this.app.repository.countDownLoadTargets()
                }
            )
    }

    private fun refresh(data: TosPrivacy) {
        this.tosPrivacy = data
        this.systemDao.delete()
        this.systemDao.insert(data)
    }
}
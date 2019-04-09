package app.colorrr.colorrr.repository

import android.util.Log
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.database.dao.PaletteDao
import app.colorrr.colorrr.entity.Palette
import app.colorrr.colorrr.entity.Session
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.UnknownHostException

class PaletteRepository(private val paletteDao: PaletteDao) {
    private var app = App.getInstance()

    init {
        this.initRepo()
    }

    private fun initRepo() {
        val disposable = ApiWorker.workerPalette
            .getSystemPalettesList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    doAsync {
                        refreshAdminPalettes(it)
                        uiThread { app.repository.countDownLoadTargets() }
                    }
                },
                onError = {
                    if (it is UnknownHostException) {
                        Log.d("API_WORKER", "palettes: no network")
                        this.checkDbData()
                    }

                    if (it.message == ApiWorker.ERROR_INVALID_ARGUMENTS)
                        this.checkDbData()

                    this.app.repository.countDownLoadTargets()
                }
            )
    }

    fun loadUserData(session: List<Session>) {
        if (session.isNotEmpty() && session[0].userID > 0 && session[0].sessionID != "") {
            val disposable = ApiWorker.workerPalette
                .getUserPalettesList(session[0].userID, session[0].sessionID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { this.refreshUserPalettes(it) },
                    onComplete = { this.app.repository.countDownLoadTargets() },
                    onError = {
                        if (it is UnknownHostException)
                            Log.d("API_WORKER", "user palettes: no network")

                        this.app.repository.countDownLoadTargets()
                    }
                )
        } else {
            this.app.repository.countDownLoadTargets()
        }
    }

    private fun checkDbData() {
        val disposable = this.paletteDao.getSystem()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { list ->
                if (list.isEmpty())
                    this.app.repository.incrementNoDataCounter()
            }
    }

    private fun refreshAdminPalettes(items: List<Palette>) {
        this.paletteDao.deleteAdminItems()
        this.paletteDao.insertAll(items)
    }

    private fun refreshUserPalettes(items: List<Palette>) {
        this.paletteDao.deleteUserItems()
        this.paletteDao.insertAll(items)
    }
}
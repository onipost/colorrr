package app.colorrr.colorrr.ui.splash

import app.colorrr.colorrr.App
import app.colorrr.colorrr.repository.Repository
import app.colorrr.colorrr.ui.base.BaseViewModel

class SplashViewModel : BaseViewModel<SplashInterface>() {
    fun startLoad() {
        App.getInstance().repository.setListenerLoadEnd(object : Repository.SystemDataLoadEndListener {
            override fun onEmptyRepository() {
                getListener()?.onEmptyRepo()
            }

            override fun onLoadEnd() {
                if (App.getInstance().repository.sessionRepository.isSessionActive())
                    getListener()?.openMainScreen()
                else
                    getListener()?.openLoginScreen()
            }
        })
    }
}
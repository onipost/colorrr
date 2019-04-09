package app.colorrr.colorrr

import android.app.Application
import android.content.Context
import app.colorrr.colorrr.businessLogic.BusinessLogic
import app.colorrr.colorrr.repository.Repository
import android.net.NetworkInfo
import android.net.ConnectivityManager
import androidx.multidex.MultiDexApplication

class App : MultiDexApplication() {
    private lateinit var businessLogic: BusinessLogic
    lateinit var repository: Repository

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        this.repository = Repository(this.applicationContext)
        this.businessLogic = BusinessLogic()
    }

    companion object {
        @Volatile
        private var instance: App? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: App().also { instance = it }
        }
    }

    fun getStringById(stringID: Int): String {
        return applicationContext.getString(stringID)
    }

    fun getThrowable(stringID: Int): Throwable {
        return Throwable(applicationContext.getString(stringID))
    }

    fun checkInternetConnection(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting ?: false
    }
}
package app.colorrr.colorrr.repository

import android.content.Context
import android.util.Log
import app.colorrr.colorrr.database.AppDB
import io.reactivex.rxkotlin.subscribeBy

class Repository(val context: Context) {
    private var database: AppDB = AppDB.getInstance(this.context)

    var sessionRepository = SessionRepository(database.sessionDao(), database.userDao())
    lateinit var paletteRepository: PaletteRepository
    lateinit var categoriesRepository: CategoryRepository
    lateinit var imagesRepository: ImageRepository
    lateinit var systemRepository: SystemRepository
    lateinit var feedRepository: FeedRepository

    @Volatile
    private var loadTargets = 8
    @Volatile
    private var noDataCounter = 0
    private var loadTargetListener = object : SystemDataLoadListener {
        override fun onLoadSource() {
            if (loadTargets == 0) {
                if (noDataCounter > 0)
                    loadEndListener?.onEmptyRepository()
                else
                    loadEndListener?.onLoadEnd()
            }
        }
    }

    var loadEndListener: SystemDataLoadEndListener? = null

    init {
        val sessionDisposable = this.sessionRepository.initSession().subscribeBy {
            this.sessionRepository.setSession(it)
            //TODO improve error handling
            this.systemRepository = SystemRepository(this.database.systemDao())
            this.categoriesRepository = CategoryRepository(this.database.categoryDao())
            this.paletteRepository = PaletteRepository(this.database.paletteDao())
            this.paletteRepository.loadUserData(it)
            this.imagesRepository = ImageRepository(this.database.imageDao())
            this.feedRepository = FeedRepository(this.database.feedDao())
        }
    }

    fun setListenerLoadEnd(loadListener: SystemDataLoadEndListener) {
        this.loadEndListener = loadListener
    }

    fun incrementNoDataCounter() {
        this.noDataCounter++
    }

    fun countDownLoadTargets() {
        Log.d("API_WORKER", "count down")
        this.loadTargets--
        this.loadTargetListener.onLoadSource()
    }

    interface SystemDataLoadListener {
        fun onLoadSource()
    }

    interface SystemDataLoadEndListener {
        fun onLoadEnd()

        fun onEmptyRepository()
    }
}
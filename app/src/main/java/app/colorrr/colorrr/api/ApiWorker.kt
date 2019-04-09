package app.colorrr.colorrr.api

import app.colorrr.colorrr.api.category.ApiCategory
import app.colorrr.colorrr.api.category.WorkerCategory
import app.colorrr.colorrr.api.feed.ApiFeed
import app.colorrr.colorrr.api.feed.WorkerFeed
import app.colorrr.colorrr.api.files.ApiFiles
import app.colorrr.colorrr.api.files.WorkerFiles
import app.colorrr.colorrr.api.image.ApiImage
import app.colorrr.colorrr.api.image.WorkerImages
import app.colorrr.colorrr.api.palette.ApiPalette
import app.colorrr.colorrr.api.palette.WorkerPalette
import app.colorrr.colorrr.api.system.ApiSystem
import app.colorrr.colorrr.api.system.WorkerSystem
import app.colorrr.colorrr.api.user.ApiUser
import app.colorrr.colorrr.api.user.WorkerUser
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiWorker {
    const val ERROR_STATUS = "error"
    const val ERROR_UNKNOWN = "0.0"
    const val ERROR_INVALID_ARGUMENTS = "1.0"
    const val ERROR_DB = "2.0"
    const val ERROR_INVALID_CREDENTIALS = "3.0"
    const val ERROR_NOT_AUTHORIZED = "4.0"

    private lateinit var apiPalette: ApiPalette
    private lateinit var apiCategory: ApiCategory
    private lateinit var apiImage: ApiImage
    private lateinit var apiUser: ApiUser
    private lateinit var apiSystem: ApiSystem
    private lateinit var apiFiles: ApiFiles
    private lateinit var apiFeed: ApiFeed
    private const val BASE_API_PATH = "https://api.colorrr.app"
    const val BASE_API_FILES_PATH = "https://api.colorrr.app/files/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(this.BASE_API_PATH)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()


    var workerSystem: WorkerSystem
    var workerUser: WorkerUser
    var workerCategory: WorkerCategory
    var workerPalette: WorkerPalette
    var workerFiles: WorkerFiles
    var workerImages: WorkerImages
    var workerFeed: WorkerFeed

    init {
        this.workerSystem = WorkerSystem(this.getApiSystem())
        this.workerUser = WorkerUser(this.getApiUser())
        this.workerCategory = WorkerCategory(this.getApiCategory())
        this.workerPalette = WorkerPalette(this.getApiPalette())
        this.workerFiles = WorkerFiles(this.getApiFiles())
        this.workerImages = WorkerImages(this.getApiImage())
        this.workerFeed = WorkerFeed(this.getApiFeed())
    }

    private fun getApiPalette(): ApiPalette {
        if (!::apiPalette.isInitialized)
            this.apiPalette = this.retrofit.create(ApiPalette::class.java)

        return this.apiPalette
    }

    private fun getApiCategory(): ApiCategory {
        if (!::apiCategory.isInitialized)
            this.apiCategory = this.retrofit.create(ApiCategory::class.java)

        return this.apiCategory
    }

    private fun getApiImage(): ApiImage {
        if (!::apiImage.isInitialized)
            this.apiImage = this.retrofit.create(ApiImage::class.java)

        return this.apiImage
    }

    private fun getApiUser(): ApiUser {
        if (!::apiUser.isInitialized)
            this.apiUser = this.retrofit.create(ApiUser::class.java)

        return this.apiUser
    }

    private fun getApiSystem(): ApiSystem {
        if (!::apiSystem.isInitialized)
            this.apiSystem = this.retrofit.create(ApiSystem::class.java)

        return this.apiSystem
    }

    private fun getApiFiles(): ApiFiles {
        if (!::apiFiles.isInitialized)
            this.apiFiles = this.retrofit.create(ApiFiles::class.java)

        return this.apiFiles
    }

    private fun getApiFeed(): ApiFeed{
        if (!::apiFeed.isInitialized)
            this.apiFeed = this.retrofit.create(ApiFeed::class.java)

        return this.apiFeed
    }
}
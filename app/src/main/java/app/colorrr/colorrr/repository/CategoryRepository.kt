package app.colorrr.colorrr.repository

import android.util.Log
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.*
import app.colorrr.colorrr.database.dao.CategoryDao
import app.colorrr.colorrr.entity.*
import app.colorrr.colorrr.R
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.UnknownHostException

class CategoryRepository(private val categoryDao: CategoryDao) {
    val FILTER_ALL = 0
    val FILTER_POPULAR = 1
    val FILTER_PREMIUM = 2
    val FILTER_FREE = 3
    val FILTER_RECOMMENDED = 4
    val FILTER_KIDS = 5

    private var start: Int = 0
    private var limit: Int = 5
    private var filter: Int = this.FILTER_ALL
    private var app = App.getInstance()

    init {
        this.initRepo()
    }

    private fun initRepo() {
        val disposable = ApiWorker.workerCategory
            .loadCategories(
                this.app.repository.sessionRepository.getUserID(),
                this.app.repository.sessionRepository.getSessionID(),
                this.limit
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    val imagesOriginal = ArrayList<ImageOriginal>()
                    val imagesUnfinished = ArrayList<ImageUnfinished>()
                    val categories = ArrayList<Category>()
                    this.putToArrays(it, categories, imagesOriginal, imagesUnfinished)
                    doAsync {
                        dropImagesAndCategoriesInDb()
                        insertImagesAndCategories(categories, imagesOriginal, imagesUnfinished)
                        start += limit + getFeaturesCount(categories)
                        uiThread { app.repository.countDownLoadTargets() }
                    }
                },
                onError = {
                    if (it is UnknownHostException) {
                        Log.d("API_WORKER", "categories: no network")
                        this.checkDbData()
                    }

                    if (it.message == ApiWorker.ERROR_INVALID_ARGUMENTS)
                        this.checkDbData()

                    this.app.repository.countDownLoadTargets()
                }
            )
    }

    private fun checkDbData() {
        val disposable = this.categoryDao.getAllCheck()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { list ->
                if (list.isEmpty())
                    this.app.repository.incrementNoDataCounter()
            }
    }

    fun getLimit(): Int {
        return this.limit
    }

    fun setFilter(id: Int) {
        this.start = 0
        when (id) {
            R.id.filter_all -> this.filter = this.FILTER_ALL
            R.id.filter_popular -> this.filter = this.FILTER_POPULAR
            R.id.filter_premium -> this.filter = this.FILTER_PREMIUM
            R.id.filter_free -> this.filter = this.FILTER_FREE
            R.id.filter_recommended -> this.filter = this.FILTER_RECOMMENDED
            R.id.filter_for_kids -> this.filter = this.FILTER_KIDS
            else -> this.filter = this.FILTER_ALL
        }
    }

    fun getFilter(): Int {
        return this.filter
    }

    private fun getFeaturesCount(categories: ArrayList<Category>): Int {
        return categories.filter { it.featured == 1 }.size
    }

    fun getCategory(id: Int): Flowable<List<Category>> {
        return categoryDao.get(id).observeOn(AndroidSchedulers.mainThread())
    }

    fun getData(start: Int, limit: Int): Flowable<HashMap<String, Any>> {
        return Flowables.zip(this.getCategories(start, limit, this.filter).flatMap {
            val result = HashMap<String, Any>()
            result["categories"] = it
            Flowable.just(result)
        }, this.getFeaturedCategories().flatMap {
            val result = HashMap<String, Any>()
            result["featured"] = it
            Flowable.just(result)
        }) { first: HashMap<String, Any>, second: HashMap<String, Any> ->
            val result = HashMap<String, Any>()
            result["categories"] = first["categories"] ?: HashMap<String, Any>()
            result["featured"] = second["featured"] ?: HashMap<String, Any>()
            result
        }
    }

    private fun getCategories(start: Int, limit: Int, filter: Int): Flowable<List<CategoryAndImages>> {
        return Flowable.fromCallable { this.getCategoriesWithImages(start, limit, filter) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getFeaturedCategories(): Flowable<List<Category>> {
        return Flowable.fromCallable { categoryDao.getFeaturedCategories() }.flatMap { list ->
            val result = ArrayList<Category>()
            list.forEach {
                val images = getImages(it.id ?: -1, filter)
                if (images.isNotEmpty())
                    result.add(it)
            }
            Flowable.just(result as List<Category>)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getCategoriesWithImages(start: Int, limit: Int, filter: Int): List<CategoryAndImages> {
        val result = ArrayList<CategoryAndImages>()
        val categories = categoryDao.getCategories(start, limit)
        categories.forEach {
            val images = getImages(it.id ?: -1, filter)
            if (images.isNotEmpty())
                result.add(CategoryAndImages(it, images))
        }

        return result
    }

    private fun getImages(categoryID: Int, filter: Int): List<ImageToCategory> {
        val imagesRepository = app.repository.imagesRepository
        val startImages = 0
        val limitImages = 20

        return when (filter) {
            FILTER_ALL ->
                imagesRepository.getImagesByCategoryIdNonRx(categoryID, startImages, limitImages)
            FILTER_POPULAR -> imagesRepository.getImagesByCategoryIdNonRxPopular(
                categoryID,
                startImages,
                limitImages
            )
            FILTER_PREMIUM -> imagesRepository.getImagesByCategoryIdNonRxPremium(
                categoryID,
                startImages,
                limitImages
            )
            FILTER_FREE -> imagesRepository.getImagesByCategoryIdNonRxFree(
                categoryID,
                startImages,
                limitImages
            )
            FILTER_RECOMMENDED -> {
                val related = ArrayList<Int>()
                val unfinishedList = app.repository.sessionRepository.getUnfinishedFull()
                unfinishedList.forEach { item -> related.addAll(item.related) }
                imagesRepository.getImagesByCategoryIdNonRxRecommended(
                    categoryID,
                    startImages,
                    limitImages,
                    related
                )
            }
            FILTER_KIDS -> imagesRepository.getImagesByCategoryIdNonRxKids(
                categoryID,
                startImages,
                limitImages
            )
            else -> imagesRepository.getImagesByCategoryIdNonRx(categoryID, startImages, limitImages)
        }
    }

    fun loadData(listener: LoadCategoriesListener) {
        if (app.checkInternetConnection()) {
            val userID = app.repository.sessionRepository.getUserID()
            val sessionID = app.repository.sessionRepository.getSessionID()
            val loadDisposable = ApiWorker.workerCategory
                .loadMoreCategories(userID, sessionID, this.start, this.limit, this.filter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { data ->
                        val imagesOriginal = ArrayList<ImageOriginal>()
                        val imagesUnfinished = ArrayList<ImageUnfinished>()
                        val categories = ArrayList<Category>()
                        this.putToArrays(data, categories, imagesOriginal, imagesUnfinished)
                        doAsync {
                            dropCategories(categories)
                            insertImagesAndCategories(categories, imagesOriginal, imagesUnfinished)
                            uiThread {
                                start += limit + getFeaturesCount(categories)
                                listener.onLoad()
                            }
                        }
                    },
                    onError = {
                        Log.d("CATEGORIES_ERROR", it.toString())
                        listener.onLoad()
                    }
                )
        } else {
            listener.onLoad()
        }
    }

    fun refresh(limit: Int, refreshListener: CategoryRepository.RefreshCategoriesListener) {
        if (app.checkInternetConnection()) {
            val userID = app.repository.sessionRepository.getUserID()
            val sessionID = app.repository.sessionRepository.getSessionID()
            val refreshDisposable = ApiWorker.workerCategory
                .loadMoreCategories(userID, sessionID, 0, limit, this.filter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { data ->
                        val imagesOriginal = ArrayList<ImageOriginal>()
                        val imagesUnfinished = ArrayList<ImageUnfinished>()
                        val categories = ArrayList<Category>()
                        this.putToArrays(data, categories, imagesOriginal, imagesUnfinished)
                        doAsync {
                            dropImagesAndCategoriesInDb()
                            insertImagesAndCategories(categories, imagesOriginal, imagesUnfinished)
                            uiThread { refreshListener.onRefresh(limit) }
                        }
                    },
                    onError = { refreshListener.onRefresh(limit) }
                )
        } else {
            refreshListener.onRefresh(limit)
        }
    }

    private fun putToArrays(
        data: ArrayList<HashMap<String, Any>>,
        categories: ArrayList<Category>,
        imagesOriginal: ArrayList<ImageOriginal>,
        imagesUnfinished: ArrayList<ImageUnfinished>
    ) {
        data.forEach { item ->
            val imagesData = item["images"] as ArrayList<HashMap<String, Image>>
            for (i in 0 until imagesData.size) {
                val image = imagesData[i]
                imagesOriginal.add(image["original"] as ImageOriginal)
                if (image.containsKey("unfinished"))
                    imagesUnfinished.add(image["unfinished"] as ImageUnfinished)
            }
            categories.add(item["category"] as Category)
        }
    }


    private fun dropCategories(list: List<Category>? = null) {
        list?.let { images ->
            images.forEach { this.categoryDao.delete(it) }
        } ?: run {
            this.categoryDao.deleteAll()
        }
    }

    private fun dropImagesAndCategoriesInDb() {
        this.dropCategories()
        app.repository.imagesRepository.dropImagesFromDb()
    }

    private fun insertImagesAndCategories(
        categories: List<Category>,
        images: List<ImageOriginal>,
        imagesUnfinished: List<ImageUnfinished>
    ) {
        categoryDao.insertAll(categories)
        app.repository.imagesRepository.insertImagesInDb(images)
        app.repository.sessionRepository.insertUnfinishedInDb(imagesUnfinished)
    }

    interface LoadCategoriesListener {
        fun onLoad()
    }

    interface RefreshCategoriesListener {
        fun onRefresh(limit: Int)
    }
}
package app.colorrr.colorrr.repository

import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.database.dao.ImageDao
import app.colorrr.colorrr.entity.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ImageRepository(private val imageDao: ImageDao) {
    private var app = App.getInstance()
    private var limit: Int = 3

    fun getImagesByCategoryId(categoryID: Int, start: Int, limit: Int): Flowable<List<ImageToCategory>> {
        val filter = app.repository.categoriesRepository.getFilter()
        return Flowable.fromCallable { this.getImages(categoryID, start, limit, filter) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getImages(categoryID: Int, start: Int, limit: Int, filter: Int): List<ImageToCategory> {
        val categoriesRepository = app.repository.categoriesRepository
        return when (filter) {
            categoriesRepository.FILTER_ALL -> this.getImagesByCategoryIdNonRx(categoryID, start, limit)
            categoriesRepository.FILTER_POPULAR -> this.getImagesByCategoryIdNonRxPopular(categoryID, start, limit)
            categoriesRepository.FILTER_PREMIUM -> this.getImagesByCategoryIdNonRxPremium(categoryID, start, limit)
            categoriesRepository.FILTER_FREE -> this.getImagesByCategoryIdNonRxFree(categoryID, start, limit)
            categoriesRepository.FILTER_RECOMMENDED -> {
                val related = ArrayList<Int>()
                val unfinishedList = app.repository.sessionRepository.getUnfinishedFull()
                unfinishedList.forEach { item -> related.addAll(item.related) }
                this.getImagesByCategoryIdNonRxRecommended(categoryID, start, limit, related)
            }
            categoriesRepository.FILTER_KIDS -> this.getImagesByCategoryIdNonRxKids(categoryID, start, limit)
            else -> this.getImagesByCategoryIdNonRx(categoryID, start, limit)
        }
    }

    fun getImagesByCategoryIdNonRx(categoryID: Int, start: Int, limit: Int): List<ImageToCategory> {
        return imageDao.getByCategoryIdLimit(categoryID, start, limit)
    }

    fun getImagesByCategoryIdNonRxPopular(categoryID: Int, start: Int, limit: Int): List<ImageToCategory> {
        return imageDao.getByCategoryIdLimitPopular(categoryID, start, limit)
    }

    fun getImagesByCategoryIdNonRxPremium(categoryID: Int, start: Int, limit: Int): List<ImageToCategory> {
        return imageDao.getByCategoryIdLimitPremium(categoryID, start, limit)
    }

    fun getImagesByCategoryIdNonRxFree(categoryID: Int, start: Int, limit: Int): List<ImageToCategory> {
        return imageDao.getByCategoryIdLimitFree(categoryID, start, limit)
    }

    fun getImagesByCategoryIdNonRxRecommended(
        categoryID: Int,
        start: Int,
        limit: Int,
        related: ArrayList<Int>
    ): List<ImageToCategory> {
        return imageDao.getByCategoryIdLimitRecommended(categoryID, start, limit, related)
    }

    fun getImagesByCategoryIdNonRxKids(categoryID: Int, start: Int, limit: Int): List<ImageToCategory> {
        return imageDao.getByCategoryIdLimitKids(categoryID, start, limit)
    }

    fun getLimit(): Int {
        return this.limit
    }

    fun loadData(imagesStart: Int, categoryID: Int, loadListener: ImagesLoadListener) {
        if (app.checkInternetConnection()) {
            val sessionID = app.repository.sessionRepository.getSessionID()
            val userID = app.repository.sessionRepository.getUserID()
            val loadDisposable = ApiWorker.workerImages
                .getImagesList(imagesStart, limit, categoryID, sessionID, userID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        val imagesOriginal = ArrayList<ImageOriginal>()
                        val imagesUnfinished = ArrayList<ImageUnfinished>()
                        this.putToArrays(it, imagesOriginal, imagesUnfinished)
                        doAsync {
                            insertData(imagesOriginal, imagesUnfinished)
                            uiThread { loadListener.onLoad() }
                        }
                    },
                    onError = { loadListener.onLoad() }
                )
        } else {
            loadListener.onLoad()
        }
    }

    fun refresh(categoryID: Int, limit: Int, listener: RefreshImagesListener) {
        if (app.checkInternetConnection()) {
            val userID = app.repository.sessionRepository.getUserID()
            val sessionID = app.repository.sessionRepository.getSessionID()
            val refreshDisposable = ApiWorker.workerImages
                .getImagesList(0, limit, categoryID, sessionID, userID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        val imagesOriginal = ArrayList<ImageOriginal>()
                        val imagesUnfinished = ArrayList<ImageUnfinished>()
                        this.putToArrays(it, imagesOriginal, imagesUnfinished)
                        doAsync {
                            dropImagesFromDb(imagesOriginal)
                            insertData(imagesOriginal, imagesUnfinished)
                            uiThread { listener.onRefresh(limit) }
                        }
                    },
                    onError = { listener.onRefresh(limit) }
                )
        } else {
            listener.onRefresh(limit)
        }
    }

    private fun putToArrays(
        data: List<HashMap<String, Image>>,
        imagesOriginal: ArrayList<ImageOriginal>,
        imagesUnfinished: ArrayList<ImageUnfinished>
    ) {
        data.forEach { item ->
            imagesOriginal.add(item["original"] as ImageOriginal)
            if (item.containsKey("unfinished"))
                imagesUnfinished.add(item["unfinished"] as ImageUnfinished)
        }
    }

    private fun getImagesToCategoriesList(items: List<ImageOriginal>): ArrayList<ImagesToCategories> {
        val result = ArrayList<ImagesToCategories>()
        items.forEach { result.add(ImagesToCategories(null, it.id, it.category_id)) }
        return result
    }

    private fun insertData(imagesOriginal: ArrayList<ImageOriginal>, imagesUnfinished: ArrayList<ImageUnfinished>) {
        this.insertImagesInDb(imagesOriginal)
        app.repository.sessionRepository.insertUnfinishedInDb(imagesUnfinished)
    }

    fun insertImagesInDb(items: List<ImageOriginal>) {
        this.dropImagesFromDb(items)
        this.imageDao.insertAll(items)
        this.imageDao.insertImagesToCategories(this.getImagesToCategoriesList(items))
    }

    fun dropImagesFromDb(list: List<ImageOriginal>? = null) {
        list?.let { images ->
            images.forEach {
                this.imageDao.deleteImageToCategory(it.id, it.category_id)
                this.imageDao.delete(it)
            }
        } ?: run {
            this.imageDao.deleteImagesToCategories()
            this.imageDao.deleteAll()
        }
    }

    interface ImagesLoadListener {
        fun onLoad()
    }

    interface RefreshImagesListener {
        fun onRefresh(limit: Int)
    }
}
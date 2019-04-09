package app.colorrr.colorrr.api.category

import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.ApiParser
import app.colorrr.colorrr.entity.api.*
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable
import java.util.*
import kotlin.collections.ArrayList

class WorkerCategory(private val apiCategory: ApiCategory) {

    fun loadCategories(userID: Int, sessionID: String, limit: Int): Observable<ArrayList<HashMap<String, Any>>> {
        val localeCode = Locale.getDefault().language
        return this.apiCategory.getListRx(CategoriesListQuery(0, limit, 1, 0, 20, 0, localeCode, sessionID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<HashMap<String, Any>>()
                for (item: Any in it.result)
                    result.add(ApiParser.parseCategory(userID, item as LinkedTreeMap<*, *>))

                Observable.fromArray(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun loadMoreCategories(
        userID: Int,
        sessionID: String,
        start: Int,
        limit: Int,
        filter: Int
    ): Observable<ArrayList<HashMap<String, Any>>> {
        val localeCode = Locale.getDefault().language
        val categoriesRepository = App.getInstance().repository.categoriesRepository
        val query = when (filter) {
            categoriesRepository.FILTER_ALL -> CategoriesListQuery(start, limit, 1, 0, 20, 0, localeCode, sessionID)
            categoriesRepository.FILTER_POPULAR -> {
                val q = CategoriesListQuery(start, limit, 1, 0, 20, 0, localeCode, sessionID)
                q.popular = 1
                q
            }
            categoriesRepository.FILTER_PREMIUM -> {
                val q = CategoriesListQuery(start, limit, 1, 0, 20, 0, localeCode, sessionID)
                q.premium = 1
                q
            }
            categoriesRepository.FILTER_FREE -> {
                val q = CategoriesListQuery(start, limit, 1, 0, 20, 0, localeCode, sessionID)
                q.premium = 0
                q
            }
            categoriesRepository.FILTER_RECOMMENDED -> {
                val q = CategoriesListQuery(start, limit, 1, 0, 20, 0, localeCode, sessionID)
                q.recommended = 1
                q
            }
            categoriesRepository.FILTER_KIDS -> {
                val q = CategoriesListQuery(start, limit, 1, 0, 20, 0, localeCode, sessionID)
                q.for_child = 1
                q
            }
            else -> CategoriesListQuery(start, limit, 1, 0, 20, 0, localeCode, sessionID)
        }
        return this.apiCategory.getListRx(query)
            .flatMap {
                if (it.error_code == null && it.result is ArrayList<*>) {
                    val result = ArrayList<HashMap<String, Any>>()
                    for (item: Any in it.result)
                        result.add(ApiParser.parseCategory(userID, item as LinkedTreeMap<*, *>))

                    Observable.fromArray(result)
                } else {
                    throw Throwable(it.error_code.toString())
                }
            }
    }
}
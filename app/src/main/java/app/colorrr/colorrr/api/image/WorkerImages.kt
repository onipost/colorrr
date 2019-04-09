package app.colorrr.colorrr.api.image

import app.colorrr.colorrr.api.ApiParser
import app.colorrr.colorrr.entity.Image
import app.colorrr.colorrr.entity.api.ImagesListQuery
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable
import java.util.*
import kotlin.collections.HashMap

class WorkerImages(private val apiImage: ApiImage) {
    fun getImagesList(start: Int, limit: Int, categoryID: Int, sessionID: String, userID: Int): Observable<List<HashMap<String, Image>>> {
        val localeCode = Locale.getDefault().language
        return this.apiImage.getListRx(ImagesListQuery(start, limit, categoryID, 0, localeCode, sessionID))
            .flatMap {
                if (it.error_code == null && it.result is ArrayList<*>) {
                    val result = ArrayList<HashMap<String, Image>>()
                    for (item: Any in it.result)
                        result.add(ApiParser.parseImage(userID, item as LinkedTreeMap<*, *>))

                    Observable.fromArray(result)
                } else {
                    throw Throwable(it.error_code.toString())
                }
            }
    }
}
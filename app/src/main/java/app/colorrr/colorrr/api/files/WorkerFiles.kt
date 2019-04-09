package app.colorrr.colorrr.api.files

import app.colorrr.colorrr.api.ApiParser
import app.colorrr.colorrr.entity.api.LoadFileQuery
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable

class WorkerFiles(private val apiFiles: ApiFiles) {
    fun loadFile(data: String, path: String): Observable<HashMap<String, String>> {
        return this.apiFiles.loadFileRx(LoadFileQuery(data, path)).flatMap {
            if (it.error_code == null && it.result is LinkedTreeMap<*, *>) {
                val result = ApiParser.parseFileLoad(it.result)
                Observable.just(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }
}
package app.colorrr.colorrr.api.system

import app.colorrr.colorrr.api.ApiParser
import app.colorrr.colorrr.entity.TosPrivacy
import app.colorrr.colorrr.entity.api.SystemQuery
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable

class WorkerSystem(private val apiSystem: ApiSystem) {

    fun getSystemData(localeCode: String): Observable<TosPrivacy> {
        return this.apiSystem.getSystemRx(SystemQuery(localeCode)).flatMap {
            if (it.error_code == null && it.result is LinkedTreeMap<*, *>)
                Observable.just(ApiParser.parseSystem(it.result))
            else
                throw Throwable(it.error_code.toString())
        }
    }
}
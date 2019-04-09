package app.colorrr.colorrr.api.palette

import app.colorrr.colorrr.api.ApiParser
import app.colorrr.colorrr.entity.Palette
import app.colorrr.colorrr.entity.api.PaletteListQuery
import app.colorrr.colorrr.entity.api.PaletteUserListQuery
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable
import java.util.*

class WorkerPalette(private val apiPalette: ApiPalette) {
    fun getSystemPalettesList(): Observable<List<Palette>> {
        val localeCode = Locale.getDefault().language
        return this.apiPalette.getListRx(PaletteListQuery(localeCode)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<Palette>()
                for (item: Any in it.result)
                    result.add(ApiParser.parsePalette(item as LinkedTreeMap<*, *>))

                Observable.fromArray(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun getUserPalettesList(userID: Int, sessionID: String): Observable<List<Palette>> {
        val localeCode = Locale.getDefault().language
        return this.apiPalette.getUserListRx(PaletteUserListQuery(userID, localeCode, sessionID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<Palette>()
                for (item: Any in it.result)
                    result.add(ApiParser.parsePalette(item as LinkedTreeMap<*, *>))

                Observable.fromArray(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }
}
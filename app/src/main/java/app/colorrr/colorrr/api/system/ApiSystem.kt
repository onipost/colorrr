package app.colorrr.colorrr.api.system

import app.colorrr.colorrr.entity.api.ApiResponse
import app.colorrr.colorrr.entity.api.SystemQuery
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiSystem {
    @POST("settings/get_settings")
    fun getSystemRx(
        @Body data: SystemQuery
    ): Observable<ApiResponse>
}
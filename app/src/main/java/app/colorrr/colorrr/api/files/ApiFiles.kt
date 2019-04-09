package app.colorrr.colorrr.api.files

import app.colorrr.colorrr.entity.api.ApiResponse
import app.colorrr.colorrr.entity.api.LoadFileQuery
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiFiles {
    @POST("files/upload")
    fun loadFileRx(
        @Body data: LoadFileQuery
    ): Observable<ApiResponse>
}
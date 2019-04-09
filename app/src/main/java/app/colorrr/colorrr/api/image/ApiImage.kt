package app.colorrr.colorrr.api.image

import app.colorrr.colorrr.entity.api.ApiResponse
import app.colorrr.colorrr.entity.api.ImagesListQuery
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiImage {
    @POST("images/list")
    fun getListRx(
        @Body data: ImagesListQuery
    ): Observable<ApiResponse>
}
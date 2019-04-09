package app.colorrr.colorrr.api.category

import app.colorrr.colorrr.entity.api.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCategory {
    @POST("categories/list_test")
    fun getListRx(
        @Body data: CategoriesListQuery
    ): Observable<ApiResponse>
}
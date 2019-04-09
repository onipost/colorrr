package app.colorrr.colorrr.api.palette

import app.colorrr.colorrr.entity.api.ApiResponse
import app.colorrr.colorrr.entity.api.PaletteListQuery
import app.colorrr.colorrr.entity.api.PaletteUserListQuery
import io.reactivex.Observable
import retrofit2.http.*


interface ApiPalette {
    @POST("palette/get_list")
    fun getListRx(
        @Body data: PaletteListQuery
    ): Observable<ApiResponse>

    @POST("palette/get_list")
    fun getUserListRx(
        @Body data: PaletteUserListQuery
    ): Observable<ApiResponse>
}
package app.colorrr.colorrr.api.feed

import app.colorrr.colorrr.entity.api.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiFeed {
    @POST("newsletter/get_list")
    fun getFeedRx(
        @Body data: FeedQuery
    ): Observable<ApiResponse>

    @POST("newsletter/get_list_by_follows")
    fun getFeedByFollowsRx(
        @Body data: FeedQuery
    ): Observable<ApiResponse>

    @POST("newsletter/get_list_by_likes_valid")
    fun getFeedByLikesRx(
        @Body data: FeedQuery
    ): Observable<ApiResponse>

    @POST("newsletter/comments_get")
    fun getFeedCommentsRx(
        @Body data: FeedCommentsQuery
    ): Observable<ApiResponse>

    @POST("newsletter/set_like")
    fun like(
        @Body data: FeedLikeQuery
    ): Observable<ApiResponse>

    @POST("newsletter/report")
    fun report(
        @Body data: FeedReportQuery
    ): Observable<ApiResponse>

    @POST("newsletter/delete")
    fun delete(
        @Body data: FeedDeleteQuery
    ): Observable<ApiResponse>
}
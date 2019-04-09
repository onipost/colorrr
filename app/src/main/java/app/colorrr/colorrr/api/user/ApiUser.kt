package app.colorrr.colorrr.api.user

import app.colorrr.colorrr.entity.api.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiUser {
    @POST("users/get")
    fun getUserRx(
        @Body data: UserQuery
    ): Observable<ApiResponse>

    @POST("users/get")
    fun getUserByIdRx(
        @Body data: UserByIdQuery
    ): Observable<ApiResponse>

    @POST("users/signin")
    fun loginRx(
        @Body data: LoginQuery
    ): Observable<ApiResponse>

    @POST("users/reset_password")
    fun resetPasswordRx(
        @Body data: ResetPasswordQuery
    ): Observable<ApiResponse>

    @POST("users/signup")
    fun createUserRx(
        @Body data: CreateUserQuery
    ): Observable<ApiResponse>

    @POST("users/signup_anonymous")
    fun createUserAnonymousRx(
        @Body data: CreateUserAnonymousQuery
    ): Observable<ApiResponse>

    @POST("users/update")
    fun updateUserRx(
        @Body data: UpdateUserQuery
    ): Observable<ApiResponse>

    @POST("users/password")
    fun updatePasswordRx(
        @Body data: UpdateUserPasswordQuery
    ): Observable<ApiResponse>

    @POST("users/follow")
    fun followRx(
        @Body data: FollowUserQuery
    ): Observable<ApiResponse>

    @POST("users/followers_list")
    fun getFollowersRx(
        @Body data: FollowersListQuery
    ): Observable<ApiResponse>

    @POST("users/followed_list")
    fun getFollowedRx(
        @Body data: FollowersListQuery
    ): Observable<ApiResponse>
}
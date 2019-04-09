package app.colorrr.colorrr.api.user

import app.colorrr.colorrr.api.ApiParser
import app.colorrr.colorrr.entity.Follower
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.entity.api.*
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WorkerUser(private val apiUser: ApiUser) {

    fun getUser(locale: String, sessionID: String): Observable<UserCurrent> {
        return this.apiUser.getUserRx(UserQuery(locale, sessionID)).flatMap {
            if (it.error_code == null && it.result is LinkedTreeMap<*, *>)
                Observable.just(ApiParser.parseUser(it.result))
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun getUserByID(userID: Int): Observable<UserCurrent> {
        val locale = Locale.getDefault().language
        return this.apiUser.getUserByIdRx(UserByIdQuery(locale, userID)).flatMap {
            if (it.error_code == null && it.result is LinkedTreeMap<*, *>)
                Observable.just(ApiParser.parseUser(it.result))
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun login(email: String, password: String): Observable<HashMap<*, *>> {
        val localeCode = Locale.getDefault().language
        return this.apiUser.loginRx(LoginQuery(email, password, localeCode)).flatMap {
            if (it.error_code == null && it.result is LinkedTreeMap<*, *>) {
                val result = ApiParser.parseUserSingIn(it.result)
                Observable.just(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun resetPassword(email: String): Observable<String> {
        return this.apiUser.resetPasswordRx(ResetPasswordQuery(email)).flatMap {
            if (it.error_code == null && it.result is Boolean)
                Observable.just("true")
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun createUser(
        name: String,
        email: String,
        password: String,
        facebookID: String,
        imagePath: String
    ): Observable<HashMap<String, Any>> {
        val localeCode = Locale.getDefault().language
        return this.apiUser.createUserRx(CreateUserQuery(name, email, password, facebookID, imagePath, localeCode))
            .flatMap {
                if (it.error_code == null && it.result is LinkedTreeMap<*, *>) {
                    val result = ApiParser.parseUserSingUp(it.result)
                    Observable.just(result)
                } else {
                    throw Throwable(it.error_code.toString())
                }
            }
    }

    fun createUserAnonymous(): Observable<HashMap<String, Any>> {
        val localeCode = Locale.getDefault().language
        return this.apiUser.createUserAnonymousRx(CreateUserAnonymousQuery(localeCode)).flatMap {
            if (it.error_code == null && it.result is LinkedTreeMap<*, *>) {
                val result = ApiParser.parseUserSingUp(it.result)
                Observable.just(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun updateUser(name: String, email: String, imagePath: String, sessionID: String): Observable<String> {
        return this.apiUser.updateUserRx(UpdateUserQuery(name, email, imagePath, sessionID)).flatMap {
            if (it.error_code == null && it.result is Boolean)
                Observable.just("true")
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun updatePassword(password: String, newPassword: String, sessionID: String): Observable<Boolean> {
        return this.apiUser.updatePasswordRx(UpdateUserPasswordQuery(password, newPassword, sessionID)).flatMap {
            if (it.error_code == null && it.result is Boolean)
                Observable.just(true)
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun follow(userID: Int, state: Int, sessionID: String): Observable<Boolean> {
        return this.apiUser.followRx(FollowUserQuery(userID, state, sessionID)).flatMap {
            if (it.error_code == null && it.result is Boolean)
                Observable.just(true)
            else
                throw Throwable(it.error_code.toString())
        }
    }

    fun loadFollowers(start: Int, limit: Int, userID: Int, sessionID: String): Observable<List<Follower>> {
        return this.apiUser.getFollowersRx(FollowersListQuery(start, limit, userID, sessionID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<Follower>()
                for (item: Any in it.result)
                    result.add(ApiParser.parseFollower(item as LinkedTreeMap<*, *>))

                Observable.just(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }

    fun loadFollowed(start: Int, limit: Int, userID: Int, sessionID: String): Observable<List<Follower>> {
        return this.apiUser.getFollowedRx(FollowersListQuery(start, limit, userID, sessionID)).flatMap {
            if (it.error_code == null && it.result is ArrayList<*>) {
                val result = ArrayList<Follower>()
                for (item: Any in it.result)
                    result.add(ApiParser.parseFollower(item as LinkedTreeMap<*, *>))

                Observable.just(result)
            } else {
                throw Throwable(it.error_code.toString())
            }
        }
    }
}
package app.colorrr.colorrr.ui.login

import android.app.Activity
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.entity.Session
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.ui.base.BaseViewModel
import app.colorrr.colorrr.api.ApiParser
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.jetbrains.anko.doAsync
import java.util.*
import android.content.Intent
import com.facebook.*
import org.json.JSONObject
import android.os.Bundle
import io.reactivex.Observable
import kotlin.collections.HashMap
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import app.colorrr.colorrr.R
import app.colorrr.colorrr.system.Utils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import org.jetbrains.anko.uiThread

class LoginViewModel : BaseViewModel<LoginInterface>() {
    private var app = App.getInstance()
    private var callbackManager: CallbackManager? = null
    private var loadLock = false

    fun getTos() {
        getListener()?.onGetTos(app.repository.systemRepository.getTos())
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    fun signUpFacebook(activity: Activity) {
        if (this.loadLock)
            return

        if (!app.checkInternetConnection()) {
            getListener()?.onFacebookLoginError(app.getString(R.string.no_internet_connection))
            return
        }

        this.loadLock = true
        val disposable = this.facebookAuth(activity)
            .flatMap {
                this.apiUserCreate(it)
            }.flatMap {
                this.loadFile(it)
            }.flatMap {
                this.updateUser(it)
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe {}
    }

    fun signUpAnonymous() {
        if (this.loadLock)
            return

        if (!app.checkInternetConnection()) {
            getListener()?.onSkip(false, app.getString(R.string.no_internet_connection))
            return
        }

        this.loadLock = true
        val disposable = ApiWorker.workerUser.createUserAnonymous()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    val user = it["user"] as UserCurrent
                    val session = it["session"] as Session
                    doAsync {
                        refreshInDB(user, session)
                        uiThread {
                            getListener()?.onSkip(true, "")
                            loadLock = false
                        }
                    }
                },
                onError = {
                    getListener()?.onSkip(false, ApiParser.parseError(it))
                    this.loadLock = false
                }
            )
    }

    private fun facebookAuth(activity: Activity): Observable<HashMap<String, Any?>> {
        return Observable.create<HashMap<String, Any?>> { subscriber ->
            FacebookSdk.setApplicationId("907987066040622")
            FacebookSdk.sdkInitialize(activity.applicationContext)
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult?) {
                    val token: AccessToken = AccessToken.getCurrentAccessToken()
                    val profileRequest = GraphRequest.newMeRequest(token) { result, _ ->
                        val facebookResult = HashMap<String, Any?>()
                        facebookResult["id"] = result.getString("id")

                        val nameArr = result.getString("name").split(" ")
                        val name = if (nameArr.size >= 2)
                            String.format("%s %s.", nameArr[0], nameArr[1].substring(0, 1))
                        else
                            nameArr[0]
                        facebookResult["name"] = name

                        val pictureJson = result.get("picture") as JSONObject
                        val pictureData = pictureJson.get("data") as JSONObject
                        if (!pictureData.getBoolean("is_silhouette")) {
                            doAsync {
                                facebookResult["picture"] = getBitmapFromURL(pictureData.getString("url"))
                                uiThread { subscriber.onNext(facebookResult) }
                            }
                        } else {
                            subscriber.onNext(facebookResult)
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,picture.width(512).height(512)")
                    profileRequest.parameters = parameters
                    profileRequest.executeAsync()
                }

                override fun onCancel() {
                    getListener()?.onFacebookLoginCancel()
                    loadLock = false
                }

                override fun onError(error: FacebookException?) {
                    getListener()?.onFacebookLoginError(error?.localizedMessage ?: "")
                    loadLock = false
                }

            })

            LoginManager.getInstance().logOut()
            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"))
        }
    }

    private fun apiUserCreate(value: HashMap<String, Any?>): Observable<HashMap<String, Any>> {
        return Observable.create<HashMap<String, Any>> { subscriber ->
            ApiWorker.workerUser.createUser(value["name"].toString(), "", "", value["id"].toString(), "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { data ->
                        val user = data["user"] as UserCurrent
                        val session = data["session"] as Session
                        val newUser = data["newUser"] as Boolean

                        doAsync {
                            refreshInDB(user, session)
                            uiThread {
                                if (!newUser) {
                                    getListener()?.onFacebookLoginSuccess()
                                    loadLock = false
                                } else if (value.containsKey("picture") && value["picture"] != null) {
                                    data["picture"] = value["picture"] as Bitmap
                                    subscriber.onNext(data)
                                } else {
                                    getListener()?.onFacebookLoginSuccess()
                                    loadLock = false
                                }
                            }
                        }
                    },
                    onError = {
                        getListener()?.onFacebookLoginError(ApiParser.parseError(it))
                        this.loadLock = false
                    }
                )
        }
    }

    private fun loadFile(value: HashMap<String, Any>): Observable<HashMap<String, Any>> {
        return Observable.create<HashMap<String, Any>> { subscriber ->
            val user = value["user"] as UserCurrent
            val path = "avatars/user_${user.userID}_${System.currentTimeMillis() / 1000}.jpg"
            ApiWorker.workerFiles.loadFile(Utils.bitmapToBase64(value["picture"] as Bitmap), path)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        user.image = it["path"].toString()
                        value["user"] = user
                        subscriber.onNext(value)
                    },
                    onError = {
                        getListener()?.onFacebookLoginError(ApiParser.parseError(it))
                        this.loadLock = false
                    }
                )
        }
    }

    private fun updateUser(value: HashMap<String, Any>): Observable<HashMap<String, Any>> {
        return Observable.create<HashMap<String, Any>> {
            val user = value["user"] as UserCurrent
            val session = value["session"] as Session
            ApiWorker.workerUser.updateUser(user.name, user.email, user.image, session.sessionID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        doAsync {
                            refreshInDB(user, session)
                            uiThread {
                                getListener()?.onFacebookLoginSuccess()
                                loadLock = false
                            }
                        }
                    },
                    onError = {
                        getListener()?.onFacebookLoginError(ApiParser.parseError(it))
                        this.loadLock = false
                    }
                )
        }
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun refreshInDB(user: UserCurrent, session: Session) {
        app.repository.sessionRepository.refreshSessionInDb(session)
        app.repository.sessionRepository.refreshUser(user)
    }
}
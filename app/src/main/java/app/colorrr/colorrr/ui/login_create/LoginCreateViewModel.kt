package app.colorrr.colorrr.ui.login_create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.colorrr.colorrr.App
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.system.Utils
import app.colorrr.colorrr.R
import app.colorrr.colorrr.entity.Session
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.ui.base.BaseViewModel
import app.colorrr.colorrr.api.ApiParser
import com.yalantis.ucrop.UCrop
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pl.aprilapps.easyphotopicker.EasyImage
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.lang.Exception

class LoginCreateViewModel : BaseViewModel<LoginCreateInterface>() {
    private var userPhoto = BehaviorSubject.create<Bitmap>()
    private val userObs = ReplaySubject.create<UserCurrent>()
    private var session: Session? = null
    private var app = App.getInstance()
    private var loadLock: Boolean = false
    val PERMISSION_CALL = 191
    val IMAGE_PICKER = 101

    fun init() {
        val disposable = this.userObs.observeOn(AndroidSchedulers.mainThread()).subscribeBy(
            onNext = {
                if (it.image == "" && userPhoto.value != null)
                    this.loadPhoto(it, userPhoto.value!!)
                else if (it.image != "")
                    this.updateUser(it)
            },
            onError = {
                getListener()?.onCreateUser(false, ApiParser.parseError(it))
                this.loadLock = false
            }
        )
    }

    fun getPhoto(): BehaviorSubject<Bitmap> {
        return this.userPhoto
    }

    fun handleOnActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == UCrop.REQUEST_CROP && resultCode == AppCompatActivity.RESULT_OK -> {
                data?.let { dataIntent ->
                    UCrop.getOutput(dataIntent)?.let {
                        val image = File(it.path)
                        val bitmap = BitmapFactory.decodeFile(image.absolutePath, BitmapFactory.Options())
                        this.addToGallery(activity, image)
                        this.userPhoto.onNext(bitmap)
                    }
                }
            }
            resultCode == UCrop.RESULT_ERROR && resultCode == AppCompatActivity.RESULT_OK -> {
                data?.let { UCrop.getError(it)?.printStackTrace() }
            }
            else -> {
                EasyImage.handleActivityResult(requestCode, resultCode, data, activity, object : EasyImage.Callbacks {
                    override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                        imageFile?.let { openCropper(activity, imageFile) }
                    }

                    override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                        e?.printStackTrace()
                    }

                    override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                        if (source === EasyImage.ImageSource.CAMERA)
                            EasyImage.lastlyTakenButCanceledPhoto(activity)?.delete()
                    }
                })
            }
        }
    }

    fun selectPhoto(activity: Activity) {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(activity, *perms)) {
            EasyImage.openChooserWithGallery(activity, activity.getString(R.string.select_a_photo), IMAGE_PICKER)
        } else {
            val window =
                PermissionRequest.Builder(activity, PERMISSION_CALL, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .setRationale("Please provide this permissions").setPositiveButtonText(R.string.Ok)
                    .setNegativeButtonText(R.string.Cancel).build()
            EasyPermissions.requestPermissions(window)
        }
    }

    fun openCropper(activity: Activity, imageFile: File) {
        val imageUri = Uri.fromFile(File((imageFile.absolutePath)))
        val pinkColor = ContextCompat.getColor(activity.applicationContext, R.color.mediumPink)
        val options = UCrop.Options()
        options.setToolbarColor(pinkColor)
        options.setStatusBarColor(pinkColor)
        options.setActiveWidgetColor(pinkColor)
        options.setToolbarCancelDrawable(R.drawable.back)
        UCrop.of(imageUri, imageUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(256, 256)
            .start(activity)
    }

    fun createUser(name: String, email: String, reEmail: String, password: String, rePassword: String) {
        if (this.loadLock)
            return

        if (!this.validation(name, email, reEmail, password, rePassword))
            return

        if (!app.checkInternetConnection()) {
            getListener()?.onCreateUser(false, app.getString(R.string.no_internet_connection))
            return
        }

        this.loadLock = true
        val disposable = ApiWorker.workerUser.createUser(name, email, password, "", "")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    val user = it["user"] as UserCurrent
                    session = it["session"] as Session
                    doAsync {
                        refreshInDB(user)
                        uiThread {
                            if (userPhoto.value != null)
                                userObs.onNext(user)
                            else {
                                getListener()?.onCreateUser(true, "")
                                loadLock = false
                            }
                        }
                    }
                },
                onError = {
                    val message = if (it.message == "3.0")
                        app.getString(R.string.email_address_already_used_by_another_account)
                    else
                        ApiParser.parseError(it)

                    this.userObs.onError(Throwable(message))
                    this.loadLock = false
                }
            )
    }

    private fun loadPhoto(user: UserCurrent, data: Bitmap) {
        val path = "avatars/user_${user.userID}_${System.currentTimeMillis() / 1000}.jpg"
        val disposable = ApiWorker.workerFiles.loadFile(Utils.bitmapToBase64(data), path)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    user.image = it["path"] ?: ""
                    this.userObs.onNext(user)
                },
                onError = { this.userObs.onError(it) }
            )
    }

    private fun updateUser(user: UserCurrent) {
        val disposable = ApiWorker.workerUser.updateUser(user.name, user.email, user.image, session?.sessionID ?: "")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    doAsync {
                        refreshInDB(user)
                        uiThread {
                            getListener()?.onCreateUser(true, "")
                            loadLock = false
                        }
                    }
                },
                onError = { this.userObs.onError(it) }
            )
    }

    private fun refreshInDB(user: UserCurrent) {
        session?.let { app.repository.sessionRepository.refreshSessionInDb(it) }
        app.repository.sessionRepository.refreshUser(user)
    }

    private fun addToGallery(activity: Activity, imageFile: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(imageFile)
        activity.sendBroadcast(mediaScanIntent)
    }

    private fun validation(
        name: String,
        email: String,
        reEmail: String,
        password: String,
        rePassword: String
    ): Boolean {
        if (name.isEmpty()) {
            getListener()?.onCreateUser(false, app.getString(R.string.please_enter_valid_name_to_display_in_the_app))
            return false
        }

        if (!this.validateEmail(email)) {
            getListener()?.onCreateUser(false, app.getString(R.string.please_enter_valid_email_address))
            return false
        }

        if (email != reEmail) {
            getListener()?.onCreateUser(
                false,
                app.getString(R.string.email_address_and_email_address_confirmation_are_not_metch)
            )
            return false
        }

        if (password.length < 6) {
            getListener()?.onCreateUser(
                false,
                app.getString(R.string.please_enter_valid_password_it_must_be_at_least_6_characters_length)
            )
            return false
        }

        if (password != rePassword) {
            getListener()?.onCreateUser(false, app.getString(R.string.password_and_password_confirmation_are_not_metch))
            return false
        }

        return true
    }

    private fun validateEmail(target: String): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}
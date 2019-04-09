package app.colorrr.colorrr.ui.profile_edit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.colorrr.colorrr.App
import app.colorrr.colorrr.entity.UserCurrent
import app.colorrr.colorrr.ui.base.BaseViewModel
import app.colorrr.colorrr.R
import app.colorrr.colorrr.api.ApiWorker
import app.colorrr.colorrr.system.Utils
import com.yalantis.ucrop.UCrop
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import pl.aprilapps.easyphotopicker.EasyImage
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.lang.Exception

class ProfileEditViewModel : BaseViewModel<ProfileEditInterface>() {
    val PERMISSION_CALL = 191
    val IMAGE_PICKER = 101

    private val app = App.getInstance()
    val user = BehaviorSubject.create<UserCurrent>()
    val userPhoto = BehaviorSubject.create<Bitmap>()
    private var newPassword: String = ""
    private var password: String = ""
    private var loadLock: Boolean = false

    fun init() {
        var mDisposable: Disposable? = null
        mDisposable = app.repository.sessionRepository.getUserCurrent()
            .doAfterNext { mDisposable?.dispose() }
            .subscribeBy { this.user.onNext(it) }
    }

    fun updateUser(name: String, email: String, reEmail: String, password: String, newPassword: String) {
        if (this.loadLock)
            return

        if (!this.validation(name, email, reEmail, password, newPassword))
            return

        if (!app.checkInternetConnection()) {
            getListener()?.onUpdateUserError(app.getString(R.string.no_internet_connection))
            return
        }

        this.user.value?.name = name
        this.user.value?.email = email
        this.password = password
        this.newPassword = newPassword
        this.user.value?.let { user ->
            this.userPhoto.value?.let { bitmap ->
                this.loadPhoto(user, bitmap)
            } ?: run {
                this.updateData(user)
            }
        }
    }

    fun handleOnActivityResult(
        fragment: Fragment,
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
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
                        imageFile?.let { openCropper(fragment, imageFile) }
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

    fun openCropper(activity: Fragment, imageFile: File) {
        val imageUri = Uri.fromFile(File((imageFile.absolutePath)))
        val pinkColor = ContextCompat.getColor(app.applicationContext, R.color.mediumPink)
        val options = UCrop.Options()
        options.setToolbarColor(pinkColor)
        options.setStatusBarColor(pinkColor)
        options.setActiveWidgetColor(pinkColor)
        options.setToolbarCancelDrawable(R.drawable.back)
        UCrop.of(imageUri, imageUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(256, 256)
            .start(app.applicationContext, activity)
    }

    fun selectPhoto(fragment: Fragment) {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(app.applicationContext, *perms)) {
            EasyImage.openChooserWithGallery(
                fragment,
                app.applicationContext?.getString(R.string.select_a_photo),
                IMAGE_PICKER
            )
        } else {
            val window =
                PermissionRequest.Builder(fragment, PERMISSION_CALL, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .setRationale("Please provide this permissions").setPositiveButtonText(R.string.Ok)
                    .setNegativeButtonText(R.string.Cancel).build()
            EasyPermissions.requestPermissions(window)
        }
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
        newPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            getListener()?.onUpdateUserError(app.getString(R.string.please_enter_valid_name_to_display_in_the_app))
            return false
        }

        if (!this.validateEmail(email)) {
            getListener()?.onUpdateUserError(app.getString(R.string.please_enter_valid_email_address))
            return false
        }

        if (email != reEmail) {
            getListener()?.onUpdateUserError(app.getString(R.string.email_address_and_email_address_confirmation_are_not_metch))
            return false
        }

        if (password.length < 6) {
            getListener()?.onUpdateUserError(app.getString(R.string.please_enter_current_password_it_must_be_at_least_6_characters_length))
            return false
        }

        if (newPassword.isNotEmpty() && newPassword.length < 6) {
            getListener()?.onUpdateUserError(app.getString(R.string.please_enter_valid_password_it_must_be_at_least_6_characters_length))
            return false
        }

        return true
    }

    private fun validateEmail(target: String): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun updateData(user: UserCurrent) {
        user.image = user.image.replace(ApiWorker.BASE_API_FILES_PATH, "")
        val sessionID = app.repository.sessionRepository.getSessionID()
        var mDisposable: Disposable? = null
        mDisposable = ApiWorker.workerUser.updateUser(user.name, user.email, user.image, sessionID)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext { mDisposable?.dispose() }
            .subscribeBy(
                onNext = {
                    if (this.newPassword.isNotEmpty()) {
                        this.updatePassword(this.password, this.newPassword)
                    } else {
                        this.updateLocalData()
                    }
                },
                onError = { getListener()?.onUpdateUserError(it.localizedMessage) }
            )
    }

    private fun updatePassword(password: String, newPassword: String) {
        val sessionID = app.repository.sessionRepository.getSessionID()
        var mDisposable: Disposable? = null
        mDisposable = ApiWorker.workerUser.updatePassword(password, newPassword, sessionID)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext { mDisposable?.dispose() }
            .subscribeBy(
                onNext = { this.updateLocalData() },
                onError = { getListener()?.onUpdateUserError(it.localizedMessage) }
            )
    }

    private fun loadPhoto(user: UserCurrent, data: Bitmap) {
        val path = "avatars/user_${user.userID}_${System.currentTimeMillis() / 1000}.jpg"
        var mDisposable: Disposable? = null
        mDisposable = ApiWorker.workerFiles.loadFile(Utils.bitmapToBase64(data), path)
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext { mDisposable?.dispose() }
            .subscribeBy(
                onNext = {
                    user.image = it["path"] ?: ""
                    this.updateData(user)
                },
                onError = { getListener()?.onUpdateUserError(it.localizedMessage) }
            )
    }

    private fun updateLocalData() {
        this.user.value?.image = ApiWorker.BASE_API_FILES_PATH + this.user.value?.image
        app.repository.sessionRepository.updateUser(this.user.value)
        app.repository.feedRepository.updateFeedByUserData(this.user.value)
        getListener()?.onUserUpdated()
    }
}